package peersim.biJump;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;

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

        return false;
    }
}
