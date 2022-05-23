package com.wjb.blibli.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.wjb.blibli.domain.exception.ConditionException;

import java.util.Calendar;
import java.util.Date;

public class TokenUtil {

    private static final String ISSUER = "签发者";

    public static String generateToken(Long userId) throws Exception {
        //使用JWT的RSA算法获取秘钥
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        //设置秘钥过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        //过期时间为30秒
        calendar.add(Calendar.SECOND,30);
        //给JWT添加相关信息
        return JWT.create()
                //唯一身份表示
                .withKeyId(String.valueOf(userId))
                //签发者
                .withIssuer(ISSUER)
                //过期时间
                .withExpiresAt(calendar.getTime())
                //生成并加密
                .sign(algorithm);
    }

    public static Long verifyToken(String token){
        try {
            //使用JWT的RSA算法获取秘钥
            Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
            //使用算法进行JWT解密
            //使用算法生成验证类
            JWTVerifier verifier = JWT.require(algorithm).build();
            //获取解密后的JWT
            DecodedJWT jwt = verifier.verify(token);
            //获取相关的userId
            String userId = jwt.getKeyId();
            return Long.valueOf(userId);
        }catch(TokenExpiredException e){//当抛出token过期异常
            throw new ConditionException("555", "token过期");
        }catch(Exception e){
            throw new ConditionException("非法用户token!");
        }

    }

    //将calendar.add(Calendar.DAY_OF_WEEK,1)刷新token时长设置为7天
    public static String generateRefreshToken(Long userId) throws Exception{
        //使用JWT的RSA算法获取秘钥
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        //设置秘钥过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        //过期时间为30秒
        calendar.add(Calendar.DAY_OF_MONTH,7);
        //给JWT添加相关信息
        return JWT.create()
                //唯一身份表示
                .withKeyId(String.valueOf(userId))
                //签发者
                .withIssuer(ISSUER)
                //过期时间
                .withExpiresAt(calendar.getTime())
                //生成并加密
                .sign(algorithm);
    }
}
