package com.wjb.blibli.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * MD5加密
 * 单向加密算法
 * 特点：加密速度快，不需要秘钥，但是安全性不高，需要搭配随机盐值使用
 *
 */
public class MD5Util {

	public static String sign(String content, String salt, String charset) {
		content = content + salt;
		return DigestUtils.md5Hex(getContentBytes(content, charset));
	}

	public static boolean verify(String content, String sign, String salt, String charset) {
		content = content + salt;
		String mysign = DigestUtils.md5Hex(getContentBytes(content, charset));
		return mysign.equals(sign);
	}

	private static byte[] getContentBytes(String content, String charset) {
		if (!"".equals(charset)) {
			try {
				return content.getBytes(charset);
			} catch (UnsupportedEncodingException var3) {
				throw new RuntimeException("MD5签名过程中出现错误,指定的编码集错误");
			}
		} else {
			return content.getBytes();
		}
	}

	//获取文件md5加密后的字符串
	public static String getFileMD5(MultipartFile file) throws Exception {
		//获取文件的inputSteam
		InputStream fis = file.getInputStream();
		//ByteArrayOutputStream将file inputSteam写入数据到内存用于操作
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//创建byte数组接收file数据
		byte[] buffer = new byte[1024];
		int byteRead;
		//有数据则读 将file数据读如byte数组
		//byteRead读取长度
		while((byteRead = fis.read(buffer)) > 0){
			//ByteArrayOutputStream写入数据参数1byte数组2起始点3结束点
			baos.write(buffer, 0, byteRead);
		}
		fis.close();
		//加密文件二进制流
		return DigestUtils.md5Hex(baos.toByteArray());
	}
}