package peersim.biJump;
import java.io.*;

/**
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/10/12
 */
public class SerializationUtil {
    // 将对象序列化为字节数组
    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            return bos.toByteArray();
        }
    }

    // 将字节数组反序列化为对象
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }
}
