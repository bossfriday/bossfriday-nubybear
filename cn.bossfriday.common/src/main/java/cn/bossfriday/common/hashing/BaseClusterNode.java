package cn.bossfriday.common.hashing;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * BaseClusterNode
 *
 * @author chenx
 */
public abstract class BaseClusterNode<T extends BaseClusterNode> {

    /**
     * 节点名称（不重）
     */
    @Getter
    @Setter
    protected String name;

    @Getter
    @Setter
    protected List<String> methods;

    /**
     * 虚拟节点数（路由权重控制使用）
     */
    @Getter
    @Setter
    protected int virtualNodesNum;

    protected BaseClusterNode(String name, int virtualNodesNum) {
        this.name = name;
        this.virtualNodesNum = virtualNodesNum;
        this.methods = new ArrayList<>();
    }

    /**
     * compareTo（排序用）
     *
     * @param node
     * @return
     */
    protected abstract int compareTo(T node);
}
