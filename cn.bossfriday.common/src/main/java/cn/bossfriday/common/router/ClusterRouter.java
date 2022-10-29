package cn.bossfriday.common.router;

import cn.bossfriday.common.Const;
import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.hashing.ConsistentHashRouter;
import cn.bossfriday.common.rpc.ActorSystem;
import cn.bossfriday.common.rpc.actor.ActorMsgPayloadCodec;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import cn.bossfriday.common.rpc.interfaces.IActorMsgDecoder;
import cn.bossfriday.common.rpc.interfaces.IActorMsgEncoder;
import cn.bossfriday.common.utils.GsonUtil;
import cn.bossfriday.common.zk.ZkChildrenChangeListener;
import cn.bossfriday.common.zk.ZkHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ClusterRouter
 *
 * @author chenx
 */
@Slf4j
public class ClusterRouter {

    private String basePath;
    private String clusterNodeHomePath;

    private ClusterNode currentNode;
    private ActorSystem actorSystem;
    private ZkHandler zkHandler;
    private ConsistentHashRouter<ClusterNode> consistentHashRouter;

    /**
     * key: nodeName
     */
    private HashMap<String, ClusterNode> clusterNodeHashMap;

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = this.rwLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = this.rwLock.writeLock();

    private static final String JOINT_MARK = "$$";

    public ClusterRouter(String systemName,
                         String zkAddress,
                         String nodeName,
                         String host,
                         int port,
                         int virtualNodesNum) throws InterruptedException {
        this.zkHandler = new ZkHandler(zkAddress);
        this.currentNode = new ClusterNode(nodeName, virtualNodesNum, host, port);
        this.basePath = "/" + systemName;
        this.clusterNodeHomePath = this.basePath + "/" + Const.ZK_PATH_CLUSTER_NODE;

        this.initActorSystem(nodeName, host, port);
        this.discoveryService();
        this.onClusterChanged();
    }

    /**
     * startActorSystem
     */
    public void startActorSystem() {
        if (!this.actorSystem.isStarted()) {
            this.actorSystem.start();
        }
    }

    public ActorSystem getActorSystem() {
        return this.actorSystem;
    }

    /**
     * registryService 服务注册
     *
     * @throws Exception
     */
    public void registryService() throws Exception {
        this.writeLock.lock();
        try {
            if (CollectionUtils.isEmpty(this.currentNode.getMethods())) {
                log.warn(this.currentNode.getName() + " hasn't methods");
                return;
            }

            String zkNodePath = this.basePath + "/" + Const.ZK_PATH_CLUSTER_NODE + "/" + this.currentNode.getName();
            if (this.zkHandler.checkExist(zkNodePath)) {
                this.zkHandler.deleteNode(zkNodePath);
            }

            String value = GsonUtil.beanToJson(this.currentNode);
            this.zkHandler.addEphemeralNode(zkNodePath, value);
            log.info("registryService() done, path:" + zkNodePath + " , value:" + value);
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * discoveryService 服务发现
     */
    public void discoveryService() {
        this.writeLock.lock();
        try {
            List<String> clusterNodeNameList = this.zkHandler.getChildNodeList(this.clusterNodeHomePath);
            if (CollectionUtils.isEmpty(clusterNodeNameList)) {
                return;
            }

            List<ClusterNode> clusterNodes = new ArrayList<>();
            this.clusterNodeHashMap = new HashMap<>(16);
            for (String nodeName : clusterNodeNameList) {
                ClusterNode clusterNode = this.zkHandler.getData(this.clusterNodeHomePath + "/" + nodeName, ClusterNode.class);
                if (clusterNode == null) {
                    throw new BizException("getClusterNode from zk failed!(nodeName:" + nodeName + ")");
                }

                clusterNodes.add(clusterNode);

                if (this.clusterNodeHashMap.containsKey(clusterNode.getName())) {
                    throw new BizException("duplicated nodeName!(" + clusterNode.getName() + ")");
                }

                this.clusterNodeHashMap.put(clusterNode.getName(), clusterNode);
            }

            if (this.consistentHashRouter == null) {
                this.consistentHashRouter = new ConsistentHashRouter<>(clusterNodes);
                return;
            }

            this.consistentHashRouter.setClusterNodes(clusterNodes);
            this.consistentHashRouter.refresh();
        } catch (Exception ex) {
            log.error("loadClusterNode() error!", ex);
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * registerActor
     *
     * @param method
     * @param cls
     * @param min
     * @param max
     */
    public void registerActor(String method, Class<? extends BaseUntypedActor> cls, int min, int max) {
        this.validateMethod(method);
        this.actorSystem.registerActor(method, min, max, cls);
        this.currentNode.addMethod(method);
    }

    /**
     * registerActor
     *
     * @param method
     * @param cls
     * @param min
     * @param max
     * @param pool
     */
    public void registerActor(String method, Class<? extends BaseUntypedActor> cls, int min, int max, ExecutorService pool) {
        this.validateMethod(method);
        this.actorSystem.registerActor(method, min, max, pool, cls);
        this.currentNode.addMethod(method);
    }

    /**
     * getTargetClusterNode（资源Id路由）
     * 这里取巧偷懶未进行多级路由，因此要求每个节点都部署全量服务。
     * 如果要支持多数据中心或者集群服务拆分部署等则一定要进行多级路由。
     *
     * @param method
     * @param targetResourceId
     * @return
     */
    public ClusterNode getTargetClusterNode(String method, String targetResourceId) {
        return this.getTargetClusterNode(method + JOINT_MARK + targetResourceId);
    }

    /**
     * forceGetTargetClusterNode（强制路由）
     *
     * @param nodeName
     * @return
     */
    public ClusterNode forceGetTargetClusterNode(String nodeName) {
        this.readLock.lock();
        try {
            if (!this.clusterNodeHashMap.containsKey(nodeName)) {
                throw new BizException("clusterNode not existed or alive!");
            }

            return this.clusterNodeHashMap.get(nodeName);
        } catch (Exception ex) {
            log.error("forceGetTargetClusterNode() error!", ex);
        } finally {
            this.readLock.unlock();
        }

        return null;
    }

    /**
     * getTargetClusterNode（指定Key路由）
     *
     * @param routeKey
     * @return
     */
    public ClusterNode getTargetClusterNode(String routeKey) {
        this.readLock.lock();
        try {
            return this.consistentHashRouter.getRouter(routeKey);
        } catch (Exception e) {
            log.error("getTargetClusterNode() error!", e);
        } finally {
            this.readLock.unlock();
        }

        return null;
    }

    /**
     * routeMessage
     *
     * @param routableBean
     * @param sender
     * @return
     */
    public String routeMessage(RoutableBean routableBean, ActorRef sender) {
        if (routableBean == null) {
            throw new BizException("routeMsg is null!");
        }

        if (sender == null) {
            sender = ActorRef.noSender();
        }

        ClusterNode targetClusterNode = null;
        if (routableBean.getRouteType() == RouteType.RANDOM_ROUTE.getValue()
                || routableBean.getRouteType() == RouteType.KEY_ROUTE.getValue()) {
            // 随机路由如何随机生成routeKey由上层代码控制，这里直接使用其随机结果。
            targetClusterNode = this.getTargetClusterNode(routableBean.getRouteKey());
        } else if (routableBean.getRouteType() == RouteType.FORCE_ROUTE.getValue()) {
            targetClusterNode = this.forceGetTargetClusterNode(routableBean.getTargetClusterNode());
        } else if (routableBean.getRouteType() == RouteType.RESOURCE_ID_ROUTE.getValue()) {
            targetClusterNode = this.getTargetClusterNode(routableBean.getMethod(), routableBean.getTargetResourceId());
        } else {
            throw new BizException("unknown routeType!");
        }

        if (targetClusterNode == null) {
            throw new BizException("routeMessage() failed by getTargetClusterNode() failed!");
        }

        ActorRef actor = this.actorSystem.actorOf(targetClusterNode.getHost(), targetClusterNode.getPort(), routableBean.getMethod());
        if (actor == null) {
            throw new BizException("routeMessage() failed by actorOf() failed!");
        }

        actor.tell(routableBean.getPayload(), sender);
        return targetClusterNode.getName();
    }

    /**
     * getTargetNodeList（集群广播使用）
     */
    public List<ClusterNode> getTargetNodeList() {
        this.readLock.lock();
        try {
            return this.consistentHashRouter.getClusterNodes();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * onClusterChanged 集群变化
     */
    private void onClusterChanged() {
        try {
            final ClusterRouter cluster = this;
            this.zkHandler.addListener4Children(this.clusterNodeHomePath, new ZkChildrenChangeListener() {
                @Override
                public void added(String path, byte[] data) {
                    cluster.discoveryService();
                }

                @Override
                public void updated(String path, byte[] data) {
                    this.added(path, data);
                }

                @Override
                public void removed(String path, byte[] data) {
                    this.added(path, data);
                }

                @Override
                public void connectInitialized() {
                    // just ignore
                }

                @Override
                public void reconnected() {
                    // just ignore
                }

                @Override
                public void suspended() {
                    // just ignore
                }

                @Override
                public void connectLost() {
                    // just ignore
                }
            });
        } catch (Exception e) {
            log.error("ClusterRouter.onClusterNodeChanged() error!", e);
        }
    }

    /**
     * initActorSystem
     *
     * @param nodeName
     * @param host
     * @param port
     */
    private void initActorSystem(String nodeName, String host, int port) {
        this.actorSystem = ActorSystem.create(nodeName, new InetSocketAddress(host, port));
        this.actorSystem.setMsgEncoder(new IActorMsgEncoder() {
            @Override
            public byte[] encode(Object obj) {
                return ActorMsgPayloadCodec.encode(obj);
            }
        });
        this.actorSystem.setMsgDecoder(new IActorMsgDecoder() {
            @Override
            public Object decode(byte[] bytes) {
                return ActorMsgPayloadCodec.decode(bytes);
            }
        });
    }

    /**
     * validateMethod
     *
     * @param method
     */
    private void validateMethod(String method) {
        if (method.indexOf(JOINT_MARK) >= 0) {
            throw new BizException("method shouldn't indexOf " + JOINT_MARK);
        }
    }
}
