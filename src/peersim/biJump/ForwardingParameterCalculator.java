package peersim.biJump;
import peersim.core.Control;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Control;
/**
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/11/4
 */


public class ForwardingParameterCalculator implements Control {
    public static double a; // 渐近线斜率
    public static double epsilon; // 收敛阈值
    public static double alpha; // 随机游走步长比例参数
    public static double init; // 初始值

    public ForwardingParameterCalculator(String prefix) {
        a = Configuration.getDouble(prefix + ".a");
        epsilon = Configuration.getDouble(prefix + ".epsilon");
        alpha = Configuration.getDouble(prefix + ".alpha");
        init = Configuration.getDouble(prefix + ".init");
    }

    public boolean execute() {
        return false;
    }

    // 定义 cos_1 函数
    public static double cos1(double x) {
        if (x == 0) {
            return 0;
        } else {
            return Math.cos(1 / x);
        }
    }

    // 定义 f_converge 函数
    public static double fConverge(double x, double a) {
        return cos1(x) * a * x;
    }

    // 定义独立的迭代函数
    public static double iterate(double xi) {
        Random random = new Random();
        double r = random.nextDouble() * 2 - 1;  // 生成 -1 到 1 之间的随机数
        double newXi = Math.abs(xi + xi * alpha * r);
        newXi = fConverge(newXi, a);

        return Math.abs(newXi);
    }

    public static Object[] convergeTimes(double a, double epsilon, double alpha) {
        // 设置初始点
        double initialPoint = 0.7;
        double xi = initialPoint;
        int times = 0;
        List<Double> xiValues = new ArrayList<>();  // 用于存储每次迭代的 xi 值

        while (Math.abs(xi) > epsilon) {
            times++;
            double newXi = iterate(xi);
            if (Double.isNaN(newXi)) {
                break;  // 如果返回 NaN，则停止迭代
            }
            xi = newXi;
            xiValues.add(xi);
        }

        return new Object[]{times, xiValues};
    }

    public static void main(String[] args) {
        // 示例用法
        double a = 0.5;
        double epsilon = 0.001;
        double alpha = 0.1;

        Object[] result = convergeTimes(a, epsilon, alpha);
        int times = (int) result[0];
        List<Double> xiValues = (List<Double>) result[1];

        System.out.println("迭代次数: " + times);
        System.out.println("每次迭代的 xi 值: " + xiValues);
    }

}
