package com.wjb.blibli.util;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.wjb.blibli.domain.exception.ConditionException;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

@Repository
public class FastDFSUtil {

    @Autowired
    //客户端与服务器交互的实体类
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    //断点续传依赖服务提供实现断点续传基本方法
    private AppendFileStorageClient appendFileStorageClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String DEFAULT_GROUP = "group1";

    //key前缀
    private static final String PATH_KEY = "path-key:";

    private static final String UPLOADED_SIZE_KEY = "uploaded-size-key:";

    private static final String UPLOADED_NUMBER_KEY = "uploaded-number-key:";

    @Value("${fdfs.http.storage-addr}")
    private String httpFdfaStorageAddr;

    //文件分片大小
    private static final int SLICE_SIZE = 1024 * 1024 * 2;

    //下面方法会需要文件类型，这里方法用来获取
    //MultipartFile又spring提供
    public String getFileType(MultipartFile file) {

        if (file == null) {
            throw new ConditionException("inlegel file！");
        }

        String fileName = file.getOriginalFilename();
        //截取文件名分析文件类型
        int index = fileName.lastIndexOf(".");
        String fileType = fileName.substring(index + 1);

        return fileType;
    }

    //上传
    public String uploadCommFile(MultipartFile file) throws IOException {
        //metaData存储file相关信息
        Set<MetaData> metaDataSet = new HashSet<>();
        //可以自定义设置MetaData
        //MetaData metaData = new MetaData();
        String fileType = this.getFileType(file);
        //uploadFile需要传入inputStream size type 和 metadata
        //文件上传成功后的所有路径信息
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType, metaDataSet);
        //返回存储相对路径
        return storePath.getPath();

    }

    //上传断点续传第一个文件分片
    public String uploadAppenderFile(MultipartFile file) throws IOException {
        String fileType = this.getFileType(file);
        //上传第一个分片返回一个存储路径
        //参数组名 inputStream size type
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }

    //断点上传剩下的文件分片
    //参数文件 路径 偏移量（从哪里继续添加）
    public void modifyAppenderFile(MultipartFile file, String filePath, long offset) throws IOException {
        //组名 路径 inputStream size 偏移量（从哪里继续添加）
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, filePath, file.getInputStream(), file.getSize(), offset);
    }

    //通过分片来上传文件
    //参数文件 file加密的md5（跟其他文件进行唯一区分，秒传功能开发） 当前文件片数 总共多少片文件
    public String uploadFileBySlices(MultipartFile file, String fileMd5, Integer sliceNumber, Integer totalNumber) throws IOException {
        if (file == null || sliceNumber == null || totalNumber == null) {
            throw new ConditionException("wrong param！");
        }
        //第一个断点续传完成后返回路径存储redis的key
        String pathKey = PATH_KEY + fileMd5;
        String uploadedSizeKey = UPLOADED_SIZE_KEY + fileMd5;
        String uploadedNoKey = UPLOADED_NUMBER_KEY + fileMd5;
        String uploadedSizeStr = redisTemplate.opsForValue().get(uploadedSizeKey);
        Long uploadedSize = 0L;
        //判断是不是第一个分片
        if (!StringUtil.isNullOrEmpty(uploadedSizeStr)) {
            //如果不是需要获取到文件的大小
            uploadedSize = Long.valueOf(uploadedSizeStr);
        }
        //上传的是第一个分片调用uploadAppenderFile
        if (sliceNumber == 1) {
            //上传后获取到路径
            String path = this.uploadAppenderFile(file);
            //判断是否上传成功
            if (StringUtil.isNullOrEmpty(path)) {
                throw new ConditionException("upload fail！");
            }
            //存储path到redis有序所有分片储存路径相同所以只需要储存一次
            redisTemplate.opsForValue().set(pathKey, path);
            redisTemplate.opsForValue().set(uploadedNoKey, "1");
        } else {
            String path = redisTemplate.opsForValue().get(pathKey);
            if (StringUtil.isNullOrEmpty(path)) {
                throw new ConditionException("upload fail！");
            }
            //继续上传文件分片
            this.modifyAppenderFile(file, path, uploadedSize);
            //将uploadedNoKey进行+1操作
            redisTemplate.opsForValue().increment(uploadedNoKey);
        }
        //计算目前为止上传文件大小
        uploadedSize += file.getSize();
        //存储更新文件大小和上传序号
        redisTemplate.opsForValue().set(uploadedSizeKey, String.valueOf(uploadedSize));

        //判断上传过程是否可以结束 结束则清空redis
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
        Integer uploadedNo = Integer.valueOf(uploadedNoStr);

        //当分片总数达到文件分片总数结束上传
        String path = "";
        if (uploadedNo.equals(totalNumber)) {
            //赋值返回路径
            path = redisTemplate.opsForValue().get(pathKey);
            List<String> keyList = Arrays.asList(uploadedNoKey, pathKey, uploadedSizeKey);
            //删除所有redis中的key
            redisTemplate.delete(keyList);
        }
        //返回文件路径
        return path;
    }

    //文件分片
    public void convertFileToSlices(MultipartFile multipartFile) throws Exception {
        //获取文件原始名
        String fileName = multipartFile.getOriginalFilename();
        //获取类型
        String fileType = this.getFileType(multipartFile);
        //转换成file类型
        File file = this.multipartFileToFile(multipartFile);
        //文件长度
        long fileLength = file.length();
        //生成排序文件名
        int count = 1;
        //按SLICE_SIZE(1024 * 1024 * 2)分片大小进行循环 文件有多少SLICE_SIZE循环多少次
        for (int i = 0; i < fileLength; i += SLICE_SIZE) {
            //RandomAccessFile java的文件处理类
            //可读写 支持随机访问参数1文件2权限（这里是读r）
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            //seek方法定位到访问的本次处理起始位置（每次文件位置更换1024 * 1024 * 2）
            randomAccessFile.seek(i);
            //创建一个byte数组用于接收读取数据
            byte[] bytes = new byte[SLICE_SIZE];
            //使用RandomAccessFile的read读取一定数量的数据 参数byte数组大小即读取量
            //这里返回len 最后文件分片大小可能不等于SLICE_SIZE
            //所以要读取出实际大小bytes数组的大小 存储在len
            int len = randomAccessFile.read(bytes);
            //定义存储路径 位置+第几个文件+.+文件类型
            String path = "D:\\testFile\\" + count + "." + fileType;
            //新建File传入地址 返回一个file文件（用于写入）
            File slice = new File(path);
            //创建FileOutputStream进行文件输出写入 参数file即将操作的文件
            FileOutputStream fos = new FileOutputStream(slice);
            //使用FileOutputStream写入file数据 参数1byte数组数据源2起始点3终止点
            fos.write(bytes, 0, len);
            //关闭FileOutputStream
            fos.close();
            //关闭RandomAccessFile
            randomAccessFile.close();
            //写入序号++
            count++;
        }
        //删除临时文件
        file.delete();
    }

    //multFile转File
    public File multipartFileToFile(MultipartFile multipartFile) throws Exception {
        String originalFileName = multipartFile.getOriginalFilename();
        //区分名称和类型
        String[] fileName = originalFileName.split("\\.");
        //生成临时文件参数1文件名称2文件类型
        File file = File.createTempFile(fileName[0], "." + fileName[1]);
        //转换
        multipartFile.transferTo(file);
        return file;
    }

    //删除
    public void deleteFile(String filePath) {
        //根据一个路径删除对应文件
        fastFileStorageClient.deleteFile(filePath);
    }

    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) throws Exception {
        //获取fastDfs中的视频数据
        FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP, url);
        //获取视频的总大小
        long totalFileSize = fileInfo.getFileSize();
        //获取文件实际所在路径
        String path = httpFdfaStorageAddr + url;
        //获取所有header
        Enumeration<String> headerNames = request.getHeaderNames();
        //放header的map
        Map<String, Object> headers = new HashMap<>();
        //遍历headers
        while (headerNames.hasMoreElements()) {
            //有值则获取
            String header = headerNames.nextElement();
            //将headername和header存入map
            headers.put(header, request.getHeader(header));
        }
        //分片处理需要特殊处理的参数
        //拼接range
        String rangeStr = request.getHeader("Range");
        //存放分片开始和结束的位置
        String[] range;
        //如果是空进行初始赋值
        if (StringUtil.isNullOrEmpty(rangeStr)) {
            rangeStr = "bytes=0-" + (totalFileSize - 1);
        }
        //如果不是第一个则将range拆分（bytes 开始 结束）
        range = rangeStr.split("bytes=|-");
        long beagin = 0;
        if (range.length >= 2) {
            //说明只有开始没有结束
            beagin = Long.parseLong(range[1]);
        }
        long end = totalFileSize - 1;
        if (range.length >= 3) {
            //有结束
            end = Long.parseLong(range[2]);
        }
        //计算分片长度
        long len = (end - beagin) + 1;
        //拼接content（告诉前段返回的range在什么范围）
        String content = "bytes" + " " + beagin + "-" + end + "/" + totalFileSize;
        //相应参数设置
        response.setHeader("Content-range", content);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "video/mp4");
        response.setContentLength((int)len);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        //请求方法
        //get方法返回输出流OutputStream参数1链接2请求头3响应
        HttpUtil.get(url, headers, response);
    }
}
