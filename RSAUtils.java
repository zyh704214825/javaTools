import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/*
 * @Description: RSA加密算法工具类
 * @Author: 张亚辉 zyh1410@gmail.com
 * @Date: 2019/7/7
 */
@Slf4j
public class RSAUtils {

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);
    /**
     * 加密算法RSA
     */
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * 签名算法
     */
    private static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * 获取公钥的key
     */
    private static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 获取私钥的key
     */
    private static final String PRIVATE_KEY = "RSAPrivateKey";


    private static final int KEY_SIZE = 1024;
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = KEY_SIZE / 8 - 11;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = KEY_SIZE / 8;

    /**
     * 生成密钥对(公钥和私钥)
     *
     * @return 返回秘钥对
     */
    public static Map<String, Object> genKeyPair(){
        Map<String, Object> keyMap = new HashMap<>(2);
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(1024);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            keyMap.put(PUBLIC_KEY,  new String(Base64.encode(publicKey.getEncoded())));
            keyMap.put(PRIVATE_KEY, new String(Base64.encode(privateKey.getEncoded())));
            return keyMap;
        } catch (Exception e) {
            log.error("Failure to generate key pairs");
            e.printStackTrace();
        }
        return keyMap;
    }

    /**
     *
     * 用私钥对信息生成数字签名
     * </p>
     *
     * @param encryptedData 已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @return 返回数字签名
     */
    public static String sign(String encryptedData, String privateKey){
        try {
            byte[] data= Base64.decode(encryptedData);
            byte[] keyBytes = Base64.decode(privateKey);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateK);
            signature.update(data);
            return  new String(Base64.encode(signature.sign()));
        }catch (Exception e){
            log.error("Failure to generate digital signature");
            return null;
        }
    }

    /**
     *
     * 校验数字签名
     *
     * @param encryptedData 已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @param sign 数字签名
     * @return 返回判断
     */
    public static boolean verify(String encryptedData, String publicKey, String sign){
        try {
            byte[] data=Base64.decode(encryptedData);
            byte[] keyBytes = Base64.decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicK = keyFactory.generatePublic(keySpec);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicK);
            signature.update(data);
            return signature.verify(Base64.decode(sign));
        }catch (Exception e){
            log.error("Failure to verify digital signature");
            return false;
        }
    }


    /**
     *
     * 私钥解密
     * @param data 已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @return 返回解密字符串
     */
    public static String decryptByPrivateKey(String data, String privateKey){
        try {
            byte[] encryptedData=Base64.decode(data);
            byte[] keyBytes = Base64.decode(privateKey);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, privateK);
            int inputLen = encryptedData.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_DECRYPT_BLOCK;
            }
            String result =new String(out.toByteArray(), DEFAULT_CHARSET);
            out.close();
            return result;
        }catch (Exception e){
            log.error("Data decryption failure");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 公钥加密
     * @param encryptedData 源数据
     * @param publicKey 公钥(BASE64编码)
     * @return 返回加密数据
     */
    public static String encryptByPublicKey(String encryptedData, String publicKey) {
        try {
            byte[] data=encryptedData.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = Base64.decode(publicKey);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            Key publicK = keyFactory.generatePublic(x509KeySpec);

            // 对数据加密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicK);
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_ENCRYPT_BLOCK;
            }
            String result = new String(Base64.encode(out.toByteArray()));
            out.close();
            return result;
        }catch (Exception e){
            log.error("Data Encryption Failure");
            e.printStackTrace();
            return null;
        }

    }

}
