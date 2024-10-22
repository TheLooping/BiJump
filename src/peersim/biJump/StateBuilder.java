package peersim.biJump;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Comparator;

/**
 *
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/10/12
 */
public class StateBuilder implements peersim.core.Control {
    private static final String PAR_PROT = "protocol";
    private static final String PAR_TRANSPORT = "transport";
    private String prefix;
    private int bijump_pid;
    private int transport_pid;

    public StateBuilder(String prefix) {
        this.prefix = prefix;
        bijump_pid = Configuration.getPid(prefix + "." + PAR_PROT);
        transport_pid = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
        UniformRandomGenerator.seed = Configuration.getLong("random.seed");
    }

    public boolean execute() {
        // 给所有节点根据nodeID重新排序

        Network.sort(new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                BiJumpProtocol p1 = (BiJumpProtocol) (((Node) o1).getProtocol(bijump_pid));
                BiJumpProtocol p2 = (BiJumpProtocol) (((Node) o2).getProtocol(bijump_pid));
                return p1.nodeId.compareTo(p2.nodeId);
            }
        });

        // 写入nodeId.txt文件
        File file = new File("nodeId.txt");
        if (file.exists()) {
            file.delete();
        }
        FileWriter writer = null;
        BufferedWriter bw = null;
        try {
            writer = new FileWriter(file);
            bw = new BufferedWriter(writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < Network.size(); i++) {
            try {
                bw.write(String.format("%04d, %s\n", i, ((BiJumpProtocol) (Network.get(i).getProtocol(bijump_pid))).nodeId.toString()));
                bw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return false;
    }
}
