package peersim.biJump;
import peersim.core.CommonState;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**pu
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/10/18
 */
public class RandomTextGeneratorFromFile {
    public static final int MIN_TEXT_LENGTH = 200;
    public static final int MAX_TEXT_LENGTH = 1000;
    private String text;
    // 读取资源目录下的txt文件内容为字符串
    private void loadTextFromFile(String fileName) {
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(RandomTextGeneratorFromFile.class.getResourceAsStream("/" + fileName), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        text = content.toString();
        return;
    }

    // 生成指定长度的随机文本片段
    public byte[] generateRandomText(int length) {
        Random random = new Random();
        int startIndex = random.nextInt(text.length() - length);

        // 处理文本边界，避免打断单词
        int endIndex = startIndex + length;
        if (endIndex > text.length()) {
            endIndex = text.length();
        }

        // 确保不打断单词，找到最近的空格或标点符号
        while (endIndex < text.length() && text.charAt(endIndex) != ' ' && text.charAt(endIndex) != '.') {
            endIndex++;
        }
        byte[] rnd_bytes = text.substring(startIndex, endIndex).trim().getBytes();
        rnd_bytes[0] = 1;
        return rnd_bytes;
    }
    public byte[] generateRandomText(){
        int length = CommonState.r.nextInt(MAX_TEXT_LENGTH - MIN_TEXT_LENGTH) + MIN_TEXT_LENGTH;
        return generateRandomText(length);
    }
    public static void main(String[] args) {
        // 读取文件中的内容
        RandomTextGeneratorFromFile rtg = new RandomTextGeneratorFromFile("WarandPeace.txt");
        // 生成50个字符的随机文本片段
        int length = 300;
        byte[]  randomText = rtg.generateRandomText(length);
        System.out.println("随机生成的文字片段: \n" + new String(randomText));
    }

    public RandomTextGeneratorFromFile(String fileName) {
        // 读取文件中的内容
        loadTextFromFile(fileName);
        if (text.isEmpty()) {
            System.out.println("文件内容为空或未找到文件");
            return;
        }
    }
    public RandomTextGeneratorFromFile() {
        // 读取文件中的内容

        loadTextFromFile("WarandPeace.txt");
        if (text.isEmpty()) {
            System.out.println("文件内容为空或未找到文件");
            return;
        }
    }

}
