package com.imooc.framework.utils;

import java.security.MessageDigest;

/**
 * FileName: SHA1
 * Founder: LiuGuiLin
 * Profile: 哈希计算
 */
public class SHA1 {

    /**
     * 融云加密算法
     */
    public static String sha1(String data){
        StringBuilder buf = new StringBuilder();
        try{
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(data.getBytes());
            byte[] bits = md.digest();
            for (int bit : bits) {
                int a = bit;
                if (a < 0) a += 256;
                if (a < 16) buf.append("0");
                buf.append(Integer.toHexString(a));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return buf.toString();
    }
}
