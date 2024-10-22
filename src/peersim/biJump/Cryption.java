package peersim.biJump;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import javax.crypto.Cipher;
import java.security.*;


/**
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/10/12
 */
public class Cryption {
    public static final int AES_KEY_SIZE = 16; // 16 bytes = 128 bits
    public static final int AES_CTR_IV_SIZE = 16;
    public static final int AES_BLOCK_SIZE = 16;
    private KeyPair keyPair;

    // Constructor to initialize and generate keys
    public Cryption() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        keyPairGenerator.initialize(256); // 使用256位曲线
        this.keyPair = keyPairGenerator.generateKeyPair();
    }

    // Public key encryption method (using the provided public key)
    public byte[] encryptWithPublicKey(PublicKey publicKey, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    // Private key decryption method (using the instance's private key)
    public byte[] decryptWithPrivateKey(byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        return cipher.doFinal(encryptedData);
    }

    public byte[]  generateAESKey(byte[] seed) {
        // Convert the BigInteger into a byte array
        byte[] keyBytes = seed;

        // Ensure keyBytes is 16 bytes (128 bits) for AES-128. Trim or pad with zeros if necessary.
        if (keyBytes.length > AES_KEY_SIZE) {
            keyBytes = Arrays.copyOfRange(keyBytes, 0, AES_KEY_SIZE);  // Trim to 16 bytes
        } else if (keyBytes.length < AES_KEY_SIZE) {
            byte[] paddedKey = new byte[AES_KEY_SIZE];
            System.arraycopy(keyBytes, 0, paddedKey, AES_KEY_SIZE - keyBytes.length, keyBytes.length);
            keyBytes = paddedKey;  // Pad with leading zeros
        }
        return keyBytes;
    }


    /** ==================== AES-CTR Encryption/Decryption ==================== **/
    // 计数器递增函数
    private void incrementCounter(byte[] counterBlock) {
        for (int i = counterBlock.length - 1; i >= 0; i--) {
            counterBlock[i]++;
            if (counterBlock[i] != 0) {
                break;
            }
        }
    }
    private static byte[] cipherAESWithCounter(SecretKeySpec secretKey, byte[] counter) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(counter);
    }

    // 辅助函数，将字节数组转换为十六进制字符串
    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    // 辅助函数，将字节数组转换为二进制字符串
    public String bytesToBinary(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0')).append(" ");
        }
        return sb.toString();
//        return bytesToHex(bytes);
    }

    public byte[] aesCTREncryptProcess(byte[] plaintext, byte[] aesKeyCombined) throws Exception {
        byte[] aesKey = Arrays.copyOfRange(aesKeyCombined, 0, AES_KEY_SIZE); // AES key
        byte[] iv = Arrays.copyOfRange(aesKeyCombined, AES_KEY_SIZE, AES_KEY_SIZE + AES_CTR_IV_SIZE); // IV
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);

        // 打印明文（以二进制格式）
//        System.out.println(bytesToBinary(plaintext) + " ---- Plaintext (Binary)");

        // 初始化加密输出缓冲区
        byte[] encryptedData = new byte[plaintext.length];
        int offset = 0;

        // CTR模式的密钥流生成和异或运算
        for (int i = 0; i < plaintext.length; i += AES_BLOCK_SIZE) {
            // 加密当前计数器值以生成密钥流
            byte[] counterBlock = ivParams.getIV().clone(); // 从IV生成计数器块
            incrementCounter(counterBlock); // 增加计数器的值
            byte[] keyStream = cipherAESWithCounter(secretKey, counterBlock); // 生成密钥流

            // 打印密钥流（以二进制格式）
//            System.out.println(bytesToBinary(keyStream) + " ---- Generated key stream (Binary)");

            // 将明文块与密钥流块进行异或
            for (int j = 0; j < AES_BLOCK_SIZE && offset + j < plaintext.length; j++) {
                encryptedData[offset + j] = (byte) (plaintext[offset + j] ^ keyStream[j]);
            }

            // 打印每个块的异或结果（以二进制格式）
//            System.out.println(bytesToBinary(encryptedData) + " ---- XOR result (Binary)");

            offset += AES_BLOCK_SIZE;
        }

        return encryptedData;
    }

    public byte[] aesCTRDecryptProcess(byte[] ciphertext, byte[] aesKeyCombined) throws Exception {
        byte[] aesKey = Arrays.copyOfRange(aesKeyCombined, 0, AES_KEY_SIZE); // AES key
        byte[] iv = Arrays.copyOfRange(aesKeyCombined, AES_KEY_SIZE, AES_KEY_SIZE + AES_CTR_IV_SIZE); // IV
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        // 打印密文（以二进制格式）
//        System.out.println(bytesToBinary(ciphertext) + " ---- Ciphertext (Binary)");

        // 初始化解密输出缓冲区
        byte[] decryptedData = new byte[ciphertext.length];
        int offset = 0;

        // CTR模式的密钥流生成和异或运算
        for (int i = 0; i < ciphertext.length; i += AES_BLOCK_SIZE) {
            // 加密当前计数器值以生成密钥流
            byte[] counterBlock = ivParams.getIV().clone(); // 从IV生成计数器块
            incrementCounter(counterBlock); // 增加计数器的值
            byte[] keyStream = cipherAESWithCounter(secretKey, counterBlock); // 生成密钥流

            // 打印密钥流（以二进制格式）
//            System.out.println(bytesToBinary(keyStream) + " ---- Generated key stream (Binary)");

            // 将密文块与密钥流块进行异或
            for (int j = 0; j < AES_BLOCK_SIZE && offset + j < ciphertext.length; j++) {
                decryptedData[offset + j] = (byte) (ciphertext[offset + j] ^ keyStream[j]);
            }

            // 打印每个块的异或结果（以二进制格式）
//            System.out.println(bytesToBinary(decryptedData) + " ---- XOR result (Binary)");

            offset += AES_BLOCK_SIZE;
        }

        return decryptedData;
    }








    // AES-CTR encryption
    public byte[] aesCTREncrypt(byte[] plaintext, byte[] aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(aesKey, "AES");
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);
        byte[] ciphertext = cipher.doFinal(plaintext);

        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        return combined;
    }

    // AES-CTR decryption
    public byte[] aesCTRDecrypt(byte[] ciphertext, byte[] aesKey) throws Exception {
        // 提取IV
        byte[] iv = new byte[16];
        byte[] actualCiphertext = new byte[ciphertext.length - iv.length];

        System.arraycopy(ciphertext, 0, iv, 0, iv.length);
        System.arraycopy(ciphertext, iv.length, actualCiphertext, 0, actualCiphertext.length);

        SecretKeySpec secretKey = new SecretKeySpec(aesKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
        return cipher.doFinal(actualCiphertext);
    }


    // Getter for public key
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }


}