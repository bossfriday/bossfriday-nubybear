package cn.bossfriday.common.hashing;

import cn.bossfriday.common.utils.MurmurHashUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 一致性哈希路由
 * Hash算法：murmur64（ketama也很常用，暂不考虑设置哈希算法）
 */
public class ConsistentHashRouter<T extends BaseClusterNode> {
    @Getter
    @Setter
    private List<T> clusterNodes;

    private TreeMap<Long, T> hashRingNodes;

    public ConsistentHashRouter(List<T> clusterNodes) throws Exception {
        this.clusterNodes = clusterNodes;
        refresh();
    }

    /**
     * refresh（上层代码保障线程安全，因为这里保障不了clusterNodes读取的线程安全）
     */
    public void refresh() throws Exception {
        if (clusterNodes == null || clusterNodes.size() == 0)
            throw new Exception("clusterNodes is null or empty!");

        Collections.sort(clusterNodes, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.compareTo(o2);
            }
        });

        if (hashRingNodes == null) {
            hashRingNodes = new TreeMap<Long, T>();
        } else {
            hashRingNodes.clear();
        }

        for (T node : clusterNodes) {
            if (node.getVirtualNodesNum() <= 0) {
                continue;
            }

            if (node.getVirtualNodesNum() > 100) {
                throw new Exception("node.virtualNodesNum must less than 100!"); // 虚拟节点数设置较大将影响一定性能（设置为成千上万也没有必要）
            }

            List<String> nodeMethods = node.methods;
            if (nodeMethods == null || nodeMethods.size() == 0) {
                continue;
            }

            for (String method : nodeMethods) {
                for (int n = 0; n < node.getVirtualNodesNum(); n++) {
                    long key = getKey(node.getName(), method, n);
                    if (hashRingNodes.containsKey(key)) {
                        throw new Exception("duplicated hash ring node! (name:" + node.getName() + ", method:" + method + ", virtualShardNum:" + n + ")");
                    }

                    hashRingNodes.put(key, node);
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
    public T getRouter(String key) throws Exception {
        SortedMap<Long, T> tail = hashRingNodes.tailMap(hash(key)); // 沿环的顺时针找到一个虚拟节点
        if (tail.size() == 0) {
            return hashRingNodes.get(hashRingNodes.firstKey());
        }

        return tail.get(tail.firstKey());
    }

    private Long getKey(String nodeName, String method, int virtualShardNum) throws Exception {
        return hash(nodeName + "-" + method + "-" + virtualShardNum);
    }

    private Long hash(String key) throws Exception {
        return MurmurHashUtil.hash64(key);
    }
}
