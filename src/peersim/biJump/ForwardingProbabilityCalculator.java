package peersim.biJump;

import java.util.Arrays;
/**
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/11/4
 */
public class ForwardingProbabilityCalculator {

    // 定义 sigmoid 函数
    public static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    // 定义 g(x) 函数
    public static double g(double x, double epsilon) {
        if (Math.abs(x) <= epsilon) {
            return 0;
        } else {
            return Math.abs(2 * sigmoid(x * 1000) - 1);
//            return 1;
        }

    }
}
