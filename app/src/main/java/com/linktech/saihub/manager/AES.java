package com.linktech.saihub.manager;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {


    //偏移量
    public static final String VIPARA = "wallet_2020_Byte";   //AES 为16bytes. DES 为8bytes


    public static final String VIPARA_DATAMIGRATION = "A-16-Byte-String";   //AES 为16bytes. DES 为8bytes

    //编码方式
    public static final String CODE_TYPE = "UTF-8";
    //填充类型
//    public static final String AES_TYPE = "AES/ECB/PKCS5Padding";
//    public static final String AES_TYPE = "AES/ECB/PKCS7Padding";
    public static final String AES_TYPE = "AES/CBC/PKCS7Padding";
    //此类型 加密内容,密钥必须为16字节的倍数位,否则抛异常,需要字节补全再进行加密
    //public static final String AES_TYPE = "AES/ECB/NoPadding";
    //java 不支持ZeroPadding
    //public static final String AES_TYPE = "AES/CBC/ZeroPadding";
    //私钥  加密的key
    private static final String AES_KEY = "16-Bytes--String16-Bytes--String";   //AES固定格式为128/192/256 bits.即：16/24/32bytes。DES固定格式为128bits，即8bytes。
    //字符补全
    private static final String[] consult = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G"};

    public static String encrypt(String cleartext, String aesKey, String iv) {
        //加密方式： AES128(CBC/PKCS5Padding) + Base64, 私钥：1111222233334444
        try {
//            IvParameterSpec zeroIv = createIV(iv);
            IvParameterSpec zeroIv = new IvParameterSpec(iv.getBytes());
            //两个参数，第一个为私钥字节数组， 第二个为加密方式 AES或者DES
            SecretKeySpec key = createKey(aesKey);
//            SecretKeySpec key = new SecretKeySpec(aesKey.getBytes(), "AES");
            //实例化加密类，参数为加密方式，要写全
            Cipher cipher = Cipher.getInstance(AES_TYPE); //PKCS5Padding比PKCS7Padding效率高，PKCS7Padding可支持IOS加解密
            //初始化，此方法可以采用三种方式，按加密算法要求来添加。（1）无第三个参数（2）第三个参数为SecureRandom random = new SecureRandom();中random对象，随机数。(AES不可采用这种方法)（3）采用此代码中的IVParameterSpec
            //加密时使用:ENCRYPT_MODE;  解密时使用:DECRYPT_MODE;
            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv); //CBC类型的可以在第三个参数传递偏移量zeroIv,ECB没有偏移量
            //加密操作,返回加密后的字节数组，然后需要编码。主要编解码方式有Base64, HEX, UUE,7bit等等。此处看服务器需要什么编码方式
            byte[] encryptedData = cipher.doFinal(cleartext.getBytes(CODE_TYPE));
//            Base64.Encoder encoder = Base64.getEncoder();
//            byte[] encode = encoder.encode(encryptedData);
            return Base64.encodeToString(encryptedData, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    //--创建密钥
    private static SecretKeySpec createKey(String password) {
        byte[] data = null;
        if (password == null) {
            password = "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(password);
        String s = null;
        while (sb.length() < 32) {
            sb.append(" ");//--密码长度不够32补足到32
        }
        s = sb.substring(0, 32);//--截取32位密码
    /*    try {
            data = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
        data = s.getBytes();
        return new SecretKeySpec(data, "AES");
    }

    //--创建偏移量
    private static IvParameterSpec createIV(String iv) {
        byte[] data = null;
        if (iv == null) {
            iv = "";
        }
        StringBuffer sb = new StringBuffer(16);
        sb.append(iv);
        String s = null;
        while (sb.length() < 16) {
            sb.append(" ");//--偏移量长度不够16补足到16
        }
        s = sb.substring(0, 16);//--截取16位偏移量
     /*   try {
            data = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
        data = s.getBytes();
        return new IvParameterSpec(data);
    }


    /**
     * 加密
     *
     * @param cleartext
     * @return
     */
    public static String encrypt(String cleartext) {
        return encrypt(cleartext, AES_KEY, VIPARA);
    }

    /**
     * 解密
     *
     * @param content
     * @return
     */
    public static String decrypt(String content) {
        return decrypt(content, AES_KEY, VIPARA);
    }

    /**
     * 解密
     *
     * @return
     */
    public static String decrypt(String content, String aesKey, String iv) {
        try {
            byte[] byteMi = Base64.decode(content, Base64.NO_WRAP);
//            IvParameterSpec zeroIv = createIV(iv);
//            byte[] byteMi = new BASE64Encoder().decodeBuffer(encrypted);
            IvParameterSpec zeroIv = new IvParameterSpec(iv.getBytes());
            SecretKeySpec key = createKey(aesKey);
          /*  SecretKeySpec key = new SecretKeySpec(
                    aesKey.getBytes(), "AES");*/
            Cipher cipher = Cipher.getInstance(AES_TYPE);
            //与加密时不同MODE:Cipher.DECRYPT_MODE
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);  //CBC类型的可以在第三个参数传递偏移量zeroIv,ECB没有偏移量
            byte[] decryptedData = cipher.doFinal(byteMi);
            return new String(decryptedData, CODE_TYPE);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
//    exact quiz deposit trim grief road arena clerk talent kitchen accident analyst,cb38c44f39f7a849745719d0897bb5c606985db6b737eef1a8714cfec86846f8,12345678aA,3; ,281613ec07be6b651dff79323f9d61669a85e662f2cfef5c30e9451ba8b67752,12345678aA,4

}
