package cn.bossfriday.common.router;

import cn.bossfriday.common.hashing.BaseClusterNode;
import cn.bossfriday.common.utils.ByteUtil;
import cn.bossfriday.common.utils.GsonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * ClusterNode
 *
 * @author chenx
 */
@Slf4j
public class ClusterNode extends BaseClusterNode<ClusterNode> {

    @Getter
    @Setter
    protected String host;

    @Getter
    @Setter
    protected int port;

    public ClusterNode() {
        super("", 0);
    }

    public ClusterNode(String name, int virtualNodesNum, String host, int port) {
        super(name, virtualNodesNum);
        this.host = host;
        this.port = port;
    }

    /**
     * addMethod
     */
    public void addMethod(String method) {
        this.methods.add(method);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClusterNode) {
            ClusterNode node = (ClusterNode) obj;
            return this.host.equals(node.getHost()) && this.port == node.getPort();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.hashCode();
    }

    @Override
    protected int compareTo(ClusterNode node) {
        int int1 = ByteUtil.ipToInt(this.host);
        int int2 = ByteUtil.ipToInt(node.getHost());
        if (int1 > int2) {
            return 1;
        } else if (int1 == int2) {
            if (this.port > node.getPort()) {
                return 1;
            } else if (this.port == node.getPort()) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }
}
