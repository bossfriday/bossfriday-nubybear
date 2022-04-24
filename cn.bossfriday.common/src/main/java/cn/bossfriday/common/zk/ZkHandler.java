package cn.bossfriday.common.zk;

import cn.bossfriday.common.Const;
import cn.bossfriday.common.utils.ByteUtil;
import cn.bossfriday.common.utils.GsonUtil;
import cn.bossfriday.common.utils.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.List;

@Slf4j
public class ZkHandler {
    private String zkAddress;
    private CuratorFramework client = null;

    public ZkHandler(String zkAddress) throws Exception {
        this.zkAddress = zkAddress;
        client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000)
                .retryPolicy(new BoundedExponentialBackoffRetry(1000, 10000, 1800))     // 重连机制
                .build();
        client.start();
        client.blockUntilConnected();
    }

    /**
     * addPersistedNode
     */
    public void addPersistedNode(String path, Object obj) throws Exception {
        this.addNode(path, obj, CreateMode.PERSISTENT);
        log.info("ZkHandler.addPersistedNode() done, path=" + path + ", " + obj.toString());
    }

    /**
     * addEphemeralNode
     */
    public void addEphemeralNode(String path, Object obj) throws Exception {
        this.addNode(path, obj, CreateMode.EPHEMERAL);
        log.info("ZkHandler.addEphemeralNode() done, path=" + path + ", " + obj.toString());
    }

    /**
     * updateNode
     */
    public void updateNode(String path, Object obj) throws Exception {
        String data = "";
        if (obj != null) {
            if (obj instanceof String) {
                data = (String) obj;
            } else {
                data = GsonUtil.beanToJson(obj);
            }
        }

        this.client.setData().forPath(path, ByteUtil.string2Bytes(data));
        log.info("ZkHandler.updateNode() done, path=" + path + ", " + obj.toString());
    }

    /**
     * deleteNode
     */
    public void deleteNode(String path) throws Exception {
        if (StringUtils.isBlank(path)) {
            throw new Exception("zkNodePath is blank!");
        }

        client.delete().deletingChildrenIfNeeded().forPath(path);
        log.info("ZkHandler.updateNode() done, path=" + path);
    }

    /**
     * getChildNodeList
     */
    public List<String> getChildNodeList(String path) throws Exception {
        try {
            return this.client.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            // ignore: just no node.
        } catch (Exception ex) {
            throw ex;
        }

        return null;
    }

    /**
     * addListener4Node
     */
    public void addListener4Node(String path, final ZkNodeChangeListener listener) throws Exception {
        if (listener == null) {
            throw new Exception("listener is null!");
        }

        final NodeCache nodeCache = new NodeCache(client, path, false);
        nodeCache.start(true);
        nodeCache.getListenable().addListener(() -> {
            byte[] bytes = nodeCache.getCurrentData().getData();
            listener.changed(bytes);
        }, ThreadPoolUtil.getThreadPool(Const.ZK_CLIENT_THREAD_POOL));
    }

    /**
     * addListener4Children
     */
    public void addListener4Children(String path, final ZkChildrenChangeListener listener) throws Exception {
        if (listener == null) {
            throw new Exception("listener is null!");
        }

        final PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        childrenCache.getListenable().addListener((client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    listener.added(event.getData().getPath(), event.getData().getData());
                    break;
                case CHILD_REMOVED:
                    listener.removed(event.getData().getPath(), event.getData().getData());
                    break;
                case CHILD_UPDATED:
                    listener.updated(event.getData().getPath(), event.getData().getData());
                    break;
                case INITIALIZED:
                    listener.connectInitialized();
                    break;
                case CONNECTION_LOST:
                    listener.connectLost();
                case CONNECTION_RECONNECTED:
                    listener.reconnected();
                    break;
                case CONNECTION_SUSPENDED:
                    listener.suspended();
                    break;
                default:
                    break;
            }
        }, ThreadPoolUtil.getThreadPool(Const.ZK_CLIENT_THREAD_POOL));
    }

    /**
     * checkExist
     */
    public boolean checkExist(String path) throws Exception {
        if (path != null) {
            Stat stat = this.client.checkExists().forPath(path);
            return stat != null;
        }

        return false;
    }

    /**
     * setData
     */
    public boolean setData(String path, String json) throws Exception {
        if (path == null) {
            throw new Exception("path is null");
        }

        String data = "";
        if (StringUtils.isNotEmpty(json)) {
            data = json;
        }

        Stat stat = this.client.setData().forPath(path, ByteUtil.string2Bytes(data));
        return stat != null;
    }

    /**
     * getData
     */
    public String getData(String path) throws Exception {
        if (path == null) {
            throw new Exception("path is null");
        }

        byte[] bytes = this.client.getData().forPath(path);
        if (bytes != null) {
            return ByteUtil.bytes2String(bytes);
        }

        return null;
    }

    public <T> T getData(String path, Class<T> cls) throws Exception {
        return GsonUtil.gsonToBean(this.getData(path), cls);
    }

    private void addNode(String path, Object obj, CreateMode mode) throws Exception {
        if (obj == null) {
            throw new Exception("input obj is null!");
        }

        String jsonStr = "";
        if (obj instanceof String) {
            jsonStr = (String) obj;
        } else {
            jsonStr = GsonUtil.beanToJson(obj);
        }

        this.client.create().creatingParentsIfNeeded().withMode(mode).forPath(path, ByteUtil.string2Bytes(jsonStr));
    }
}
