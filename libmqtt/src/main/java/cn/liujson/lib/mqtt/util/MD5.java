package cn.liujson.lib.mqtt.util;


import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * MD5加密工具类（实现版）
 * MD5加密算法（Message-Digest Algorithm）是非对称加密算法
 * 优点：基于hash算法实现不可逆，压缩性，不容易修改，容易计算
 * 缺点：穷举法可以破解
 *
 * @author Liujs
 * @date 2019/9/23
 */
public class MD5 {

    private static final String MD5_ALGORITHM = "MD5";


    /**
     * 对字符串进行MD5加密(32位)
     *
     * @param source    字节数组
     * @param uppercase 是否转为大写字符串
     * @return 加密后的字符串
     */
    public static String encode(byte[] source, boolean uppercase) {
        if (source == null) {
            throw new NullPointerException("source must not be null");
        }
        try {
            // 获得MD5摘要对象
            MessageDigest messageDigest = MessageDigest.getInstance(MD5_ALGORITHM);
            // 使用指定的字节数组更新摘要信息
            messageDigest.update(source);
            // messageDigest.digest()
            String result = Hex.encodeHexString(messageDigest.digest());
            return uppercase ? result.toUpperCase() : result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对字符串进行MD5加密(32位)
     *
     * @param source    需要加密的原字符串
     * @param uppercase 是否转为大写字符串
     * @return 加密后的字符串
     */
    public static String encode(String source, String encoding, boolean uppercase) {
        return encode(source.getBytes(Charset.forName(encoding)), uppercase);
    }

    /**
     * 对字符串进行MD5加密(32位)
     *
     * @param source 需要加密的原字符串
     * @return 加密后的字符串
     */
    public static String encode(String source) {
        return encode(source.getBytes(Charset.defaultCharset()), false);
    }

    /**
     * 对字符串进行MD5加密(16位)
     *
     * @param source    需要加密的原字符串
     * @param encoding  指定编码类型
     * @param uppercase 是否转为大写字符串
     * @return 加密后的字符串
     */
    public static String encode16(String source, String encoding, boolean uppercase) {
        return encode(source, encoding, uppercase).substring(8, 24);
    }

    /**
     * 对字符串进行MD5加密(16位)
     *
     * @param source 需要加密的原字符串
     * @return 加密后的字符串
     */
    public static String encode16(String source) {
        return encode(source).substring(8, 24);
    }

}
