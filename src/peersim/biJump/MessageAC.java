package peersim.biJump;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/10/12
 */
public class MessageAC extends SimpleEvent {
    public static int MSG_AC = 16; // anonymous communication
    private static final int TTL_LIMIT = 4; // 消息最大跳数
    private static long ID_GENERATOR = 0;

    public boolean isInitiator; // 是否是发起者发出
    public long id; // 消息ID
    public long ackID; // 回复消息ID


    public BigInteger srcID; // 源ID
    public BigInteger destID; // 目的ID
    public BigInteger nextHopID; // 下一跳ID

    public byte[] encryptedNextHopKey; // 下一跳aes密钥（已被公钥加密）
    public byte[] encryptedNext2HopKey; // 下下跳aes密钥（已被公钥加密）

    public byte[] encryptedRealDestKey; // 真实目的地aes密钥（已被公钥加密）

    public byte[] encryptedRealDest; // 真实目的地（已被加密）
    public byte[] encryptedRealSrc; // 真实源（已被加密）

    public byte[] body = null; // 消息体
    public boolean isResponseFirstHop;// 是否是响应第一跳

    public int ttl = 0;
    public MessageAC() {
        super(MSG_AC);
        this.id = ++ID_GENERATOR;
        this.ttl = TTL_LIMIT;
        this.isInitiator = true;
        this.isResponseFirstHop = false;
    }
    public MessageAC(BigInteger srcID, BigInteger destID, byte[] body) {
        this();
        this.srcID = srcID;
        this.destID = destID;
        this.body = body;
    }

    public MessageAC clone(byte[] new_body) {
        return new MessageAC(srcID, destID, new_body);
    }
    // 赋值：将第 i 到 j 个字节赋值为 xxx
    public void setBytes(int i, int j, byte[] value) {
        if (i < 0 || j >= body.length || i > j || value.length != (j - i + 1)) {
            throw new IllegalArgumentException("Invalid index or value length");
        }
        System.arraycopy(value, 0, body, i, value.length);
    }


    // 取值：获取第 i 到 j 个字节
    public byte[] getBytes(int i, int j) {
        if (i < 0 || j >= body.length || i > j) {
            throw new IllegalArgumentException("Invalid index");
        }
        byte[] result = new byte[j - i + 1];
        System.arraycopy(body, i, result, 0, result.length);
        return result;
    }
    // 转换：byte[] 转 int
    public int bytesToInt(byte[] bytes, int offset) {
        if (bytes.length < offset + 4) {
            throw new IllegalArgumentException("Not enough bytes to convert to int");
        }
        return ByteBuffer.wrap(bytes, offset, 4).getInt();
    }

    public byte[] intToBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF); // 转换为无符号整数
            if (hex.length() < 2) {
                hexString.append('0'); // 如果只有一位，前面补零
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase(); // 转换为大写
    }

    public String toString(int myPid) {
        return String.format("""
                        MessageAC: id=%d, ackID=%d, ttl=%d,\s
                        srcID=%s, destID=%s, nextHopID=%s,\s
                        encryptedRealDest=%s, encryptedNextHopKey=%s, encryptedNext2HopKey=%s,\s
                        encryptedRealSrc=%s, encryptedRealDestKey=%s,\s
                        """,
                id, ackID, ttl,
                BiJumpProtocol.nodeIdtoNodeIndex(srcID, myPid), BiJumpProtocol.nodeIdtoNodeIndex(destID, myPid), BiJumpProtocol.nodeIdtoNodeIndex(nextHopID, myPid),
                bytesToHex(encryptedRealDest), bytesToHex(encryptedNextHopKey), bytesToHex(encryptedNext2HopKey),
                bytesToHex(encryptedRealSrc), bytesToHex(encryptedRealDestKey));
    }

    public String toString() {
        return String.format("""
                        MessageAC: id=%d, ackID=%d, ttl=%d,\s
                        srcID=%s, destID=%s, nextHopID=%s,\s
                        encryptedRealDest=%s,\s
                        encryptedNextHopKey=%s,\s
                        encryptedNext2HopKey=%s,\s
                        encryptedRealSrc=%s,\s
                        encryptedRealDestKey=%s,\s
                        """,
                id, ackID, ttl, srcID, destID, nextHopID,
                bytesToHex(encryptedRealDest), bytesToHex(encryptedNextHopKey), bytesToHex(encryptedNext2HopKey),
                bytesToHex(encryptedRealSrc), bytesToHex(encryptedRealDestKey));

    }



}
