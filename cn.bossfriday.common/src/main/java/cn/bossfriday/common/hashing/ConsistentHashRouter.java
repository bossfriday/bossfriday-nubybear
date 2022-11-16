package cn.bossfriday.common.hashing;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.utils.MurmurHashUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * ConsistentHashRouter(一致性哈希路由)
 * Hash算法：murmur64（ketama也很常用，暂不考虑设置哈希算法）
 *
 * @author chenx
 */
public class ConsistentHashRouter<T extends BaseClusterNode> {

    @Getter
    @Setter
    private List<T> clusterNodes;

    private TreeMap<Long, T> hashRingNodes;

    public ConsistentHashRouter(List<T> clusterNodes) {
        this.clusterNodes = clusterNodes;
        this.refresh();
    }

    /**
     * refresh（上层代码保障线程安全，因为这里保障不了clusterNodes读取的线程安全）
     */
    public void refresh() {
        this.refreshPrepare();

        for (T node : this.clusterNodes) {
            // 虚拟节点数设置较大将影响一定性能（设置为成千上万也没有必要）
            if (node.getVirtualNodesNum() > 100) {
                throw new BizException("node.virtualNodesNum must less than 100!");
            }

            List<String> nodeMethods = node.methods;
            if (node.getVirtualNodesNum() <= 0 || CollectionUtils.isEmpty(nodeMethods)) {
                continue;
            }

            for (String method : nodeMethods) {
                for (int n = 0; n < node.getVirtualNodesNum(); n++) {
                    long key = this.getKey(node.getName(), method, n);
                    if (this.hashRingNodes.containsKey(key)) {
                        throw new BizException("duplicated hash ring node! (name:" + node.getName() + ", method:" + method + ", virtualShardNum:" + n + ")");
                    }

                    this.hashRingNodes.put(key, node);
                }
            }
        }
    }

    /**
     * 获取路由信息
     *
     * @param key
     * @return
     */
    public T getRouter(String key) {
        // 沿环的顺时针找到一个虚拟节点
        SortedMap<Long, T> tail = this.hashRingNodes.tailMap(this.hash(key));
        if (tail.size() == 0) {
            return this.hashRingNodes.get(this.hashRingNodes.firstKey());
        }

        return tail.get(tail.firstKey());
    }

    /**
     * getKey
     *
     * @param nodeName
     * @param method
     * @param virtualShardNum
     * @return
     * @throws Exception
     */
    private Long getKey(String nodeName, String method, int virtualShardNum) {
        return this.hash(nodeName + "-" + method + "-" + virtualShardNum);
    }

    /**
     * hash
     *
     * @param key
     * @return
     * @throws Exception
     */
    private Long hash(String key) {
        return MurmurHashUtil.hash64(key);
    }

    /**
     * refreshPrepare
     */
    private void refreshPrepare() {
        if (CollectionUtils.isEmpty(this.clusterNodes)) {
            throw new BizException("clusterNodes is null or empty!");
        }

        Collections.sort(this.clusterNodes, (o1, o2) -> o1.compareTo(o2));

        if (this.hashRingNodes == null) {
            this.hashRingNodes = new TreeMap<>();
        } else {
            this.hashRingNodes.clear();
        }
    }
}
