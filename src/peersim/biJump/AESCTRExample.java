package peersim.biJump;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class AESCTRExample {
    public static final int AES_CTR_IV_SIZE = 16;
    public static final int AES_BLOCK_SIZE = 16;
    // AES-CTR加密函数，带有详细的过程输出
    public byte[] aesCTREncryptProcess(byte[] plaintext, byte[] aesKey, byte[] iv) throws Exception {
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

    public byte[] aesCTRDecryptProcess(byte[] ciphertext, byte[] aesKey, byte[] iv) throws Exception {
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
//        return sb.toString();
        return bytesToHex(bytes);
    }

    // AES-CTR加密函数，传入iv
    public byte[] aesCTREncrypt(byte[] plaintext, byte[] aesKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);
        return cipher.doFinal(plaintext);
    }

    // AES-CTR解密函数，传入iv
    public byte[] aesCTRDecrypt(byte[] ciphertext, byte[] aesKey, byte[] iv) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(aesKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
        return cipher.doFinal(ciphertext);
    }

    // 生成AES密钥
    public static SecretKey generateAESKey(int keySize) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(keySize);
        return keyGen.generateKey();
    }

    // 生成随机IV
    public static byte[] generateIV() {
        byte[] iv = new byte[AES_CTR_IV_SIZE]; // AES块大小为128位，即16字节
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    public static void main(String[] args) throws Exception {
        // 原始数据
        String originalText = "hello! This is a test message. AES-CTR encryption and decryption example. This project simulates for anonymous communication system.";
        byte[] originalData = originalText.getBytes();

        // 生成两个不同的密钥
        SecretKey key1 = generateAESKey(128);
        SecretKey key2 = generateAESKey(128);

        // 生成随机IV
        byte[] iv1 = generateIV();
        byte[] iv2 = generateIV();


        AESCTRExample aesExample = new AESCTRExample();
//        System.out.println(aesExample.bytesToBinary(iv1) + " ---- iv1");

        // 使用第一个密钥和IV进行加密
        // 详细过程

//        System.out.println(aesExample.bytesToBinary(originalData) + " ---- originalData");
//        System.out.println(aesExample.bytesToBinary(key1.getEncoded()) + " ---- key1");
//        System.out.println(aesExample.bytesToBinary(iv1) + " ---- iv1");
        byte[] encryptedDataProcess = aesExample.aesCTREncryptProcess(originalData, key1.getEncoded(), iv1);
        System.out.println(aesExample.bytesToBinary(encryptedDataProcess));
        System.out.println("-----------");

        byte[] encryptedDataProcess2 = aesExample.aesCTREncryptProcess(encryptedDataProcess, key2.getEncoded(), iv2);
        System.out.println(aesExample.bytesToBinary(encryptedDataProcess2));
        System.out.println("----------->>>>>>>>>>>>>>>>");

        System.out.println("----------------------------正序解密---------------------------------");
        // 使用第二个密钥和IV2对数据进行解密
        byte[] decryptedData1 = aesExample.aesCTRDecryptProcess(encryptedDataProcess2, key2.getEncoded(), iv2);
//        System.out.println(new String(decryptedData1) + " ---- Decrypted with key2");
        byte[] decryptedData2 = aesExample.aesCTRDecryptProcess(decryptedData1, key1.getEncoded(), iv1);
        System.out.println(new String(decryptedData2) + " ---- Decrypted with key1");

        System.out.println("----------------------------乱序解密---------------------------------");
        // 使用第二个密钥和IV1对数据进行解密（IV错误，解密结果将错误）
        decryptedData1 = aesExample.aesCTRDecryptProcess(encryptedDataProcess2, key1.getEncoded(), iv1);
//        System.out.println(new String(decryptedData1) + " ---- Decrypted with key1");
        decryptedData2 = aesExample.aesCTRDecryptProcess(decryptedData1, key2.getEncoded(), iv2);
        System.out.println(new String(decryptedData2) + " ---- Decrypted with key2");










//        System.out.println(aesExample.bytesToBinary(originalData) + " ---- originalData");
//        System.out.println(aesExample.bytesToBinary(key1.getEncoded()) + " ---- key1");
//        System.out.println(aesExample.bytesToBinary(iv1) + " ---- iv");
//        byte[] encryptedData1 = aesExample.aesCTREncrypt(originalData, key1.getEncoded(), iv1);
//        System.out.println(aesExample.bytesToBinary(encryptedData1));






//        // 使用第二个密钥和不同的IV对已经加密的数据再次加密
//        byte[] encryptedData2 = aesExample.aesCTREncrypt(encryptedData1, key2.getEncoded(), iv2);
//        System.out.println("Encrypted with key2: " + Base64.getEncoder().encodeToString(encryptedData2));
//
//        System.out.println("----------------------------正序解密---------------------------------");
//        // 使用第二个密钥和IV2对数据进行解密
//        byte[] decryptedData1 = aesExample.aesCTRDecrypt(encryptedData2, key2.getEncoded(), iv2);
//        System.out.println("Decrypted with key2: " + Base64.getEncoder().encodeToString(decryptedData1));
//
//        // 使用第一个密钥和IV1对数据进行解密
//        byte[] finalDecryptedData = aesExample.aesCTRDecrypt(decryptedData1, key1.getEncoded(), iv1);
//        String decryptedText = new String(finalDecryptedData);
//        System.out.println("Decrypted with key1: " + decryptedText);

//        System.out.println("----------------------------乱序解密---------------------------------");
//        // 使用第二个密钥和IV1对数据进行解密（IV错误，解密结果将错误）
//        decryptedData1 = aesExample.aesCTRDecrypt(encryptedData2, key1.getEncoded(), iv1);
//        System.out.println("Decrypted with key1: " + Base64.getEncoder().encodeToString(decryptedData1));
//
//        // 使用第一个密钥和IV2对数据进行解密（IV错误，解密结果将错误）
//        finalDecryptedData = aesExample.aesCTRDecrypt(decryptedData1, key2.getEncoded(), iv2);
//        System.out.println("Decrypted with key2: " + Base64.getEncoder().encodeToString(finalDecryptedData));
    }
}
