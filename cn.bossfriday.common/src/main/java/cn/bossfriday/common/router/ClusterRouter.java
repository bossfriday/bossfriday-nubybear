package cn.bossfriday.common.router;

import cn.bossfriday.common.Const;
import cn.bossfriday.common.hashing.ConsistentHashRouter;
import cn.bossfriday.common.rpc.ActorSystem;
import cn.bossfriday.common.rpc.actor.ActorMsgPayloadCodec;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.common.rpc.actor.UntypedActor;
import cn.bossfriday.common.rpc.interfaces.IActorMsgDecoder;
import cn.bossfriday.common.rpc.interfaces.IActorMsgEncoder;
import cn.bossfriday.common.utils.GsonUtil;
import cn.bossfriday.common.zk.ZkChildrenChangeListener;
import cn.bossfriday.common.zk.ZkHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class ClusterRouter {
    private String basePath;
    private String clusterNodeHomePath;

    private ClusterNode currentNode;
    private ActorSystem actorSystem;
    private ZkHandler zkHandler;
    private ConsistentHashRouter consistentHashRouter;
    private HashMap<String, ClusterNode> clusterNodeHashMap;    // key: nodeName

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    private final String JOINT_MARK = "$$";

    public ClusterRouter(String systemName,
                         String zkAddress,
                         String nodeName,
                         String host,
                         int port,
                         int virtualNodesNum) throws Exception {
        this.zkHandler = new ZkHandler(zkAddress);
        this.currentNode = new ClusterNode(nodeName, virtualNodesNum, host, port);
        this.basePath = "/" + systemName;
        this.clusterNodeHomePath = basePath + "/" + Const.ZK_PATH_CLUSTER_NODE;

        initActorSystem(nodeName, host, port);
        discoveryService();
        onClusterChanged();
    }

    /**
     * startActorSystem
     */
    public void startActorSystem() throws Exception {
        if (!this.actorSystem.isStarted()) {
            this.actorSystem.start();
        }
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    /**
     * 服务注册
     */
    public void registryService() throws Exception {
        this.writeLock.lock();
        try {
            if (currentNode.getMethods() == null || currentNode.getMethods().size() == 0) {
                log.warn(currentNode.getName() + " hasn't methods");
                return;
            }

            String zkNodePath = this.basePath + "/" + Const.ZK_PATH_CLUSTER_NODE + "/" + currentNode.getName();
            if (zkHandler.checkExist(zkNodePath)) {
                zkHandler.deleteNode(zkNodePath);
            }

            String value = GsonUtil.beanToJson(currentNode);
            zkHandler.addEphemeralNode(zkNodePath, value);
            log.info("registryService() done, path:" + zkNodePath + " , value:" + value);
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * 服务发现
     */
    public void discoveryService() {
        this.writeLock.lock();
        try {
            List<String> clusterNodeNameList = this.zkHandler.getChildNodeList(clusterNodeHomePath);
            if (clusterNodeNameList == null || clusterNodeNameList.size() == 0) {
                return;
            }

            List<ClusterNode> clusterNodes = new ArrayList<>();
            clusterNodeHashMap = new HashMap<>();
            for (String nodeName : clusterNodeNameList) {
                ClusterNode clusterNode = this.zkHandler.getData(clusterNodeHomePath + "/" + nodeName, ClusterNode.class);
                if (clusterNode == null)
                    throw new Exception("getClusterNode from zk failed!(nodeName:" + nodeName + ")");

                clusterNodes.add(clusterNode);

                if (clusterNodeHashMap.containsKey(clusterNode.getName()))
                    throw new Exception("duplicated nodeName!(" + clusterNode.getName() + ")");

                clusterNodeHashMap.put(clusterNode.getName(), clusterNode);
            }

            if (consistentHashRouter == null) {
                consistentHashRouter = new ConsistentHashRouter(clusterNodes);
                return;
            }

            consistentHashRouter.setClusterNodes(clusterNodes);
            consistentHashRouter.refresh();
        } catch (Exception ex) {
            log.error("loadClusterNode() error!", ex);
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * registerActor
     */
    public void registerActor(String method, Class<? extends UntypedActor> cls, int min, int max) throws Exception {
        validateMethod(method);
        this.actorSystem.registerActor(method, min, max, cls);
        this.currentNode.addMethod(method);
    }

    public void registerActor(String method, Class<? extends UntypedActor> cls, int min, int max, ExecutorService pool) throws Exception {
        validateMethod(method);
        this.actorSystem.registerActor(method, min, max, pool, cls);
        this.currentNode.addMethod(method);
    }

    /**
     * getTargetClusterNode（资源Id路由）
     * 备注：
     * 这里取巧偷懶未进行多级路由，因此要求每个节点都部署全量服务。如果要支持多数据中心或者集群服务拆分部署等则一定要进行多级路由。
     */
    public ClusterNode getTargetClusterNode(String method, String targetResourceId) {
        return getTargetClusterNode(method + JOINT_MARK + targetResourceId);
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
            if (!clusterNodeHashMap.containsKey(nodeName))
                throw new Exception("clusterNode not existed or alive!");

            return clusterNodeHashMap.get(nodeName);
        } catch (Exception ex) {
            log.error("forceGetTargetClusterNode() error!", ex);
        } finally {
            this.readLock.unlock();
        }

        return null;
    }

    /**
     * getTargetNode（指定Key路由）
     */
    public ClusterNode getTargetClusterNode(String routeKey) {
        this.readLock.lock();
        try {
            return (ClusterNode) consistentHashRouter.getRouter(routeKey);
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
     * @throws Exception
     */
    public String routeMessage(RoutableBean routableBean, ActorRef sender) throws Exception {
        if (routableBean == null)
            throw new Exception("routeMsg is null!");

        if (sender == null)
            sender = ActorRef.noSender();

        ClusterNode targetClusterNode = null;
        if (routableBean.getRouteType() == RouteType.RandomRoute.getValue()
                || routableBean.getRouteType() == RouteType.KeyRoute.getValue()) {
            targetClusterNode = getTargetClusterNode(routableBean.getRouteKey());   // 随机路由如何随机生成routeKey由上层代码控制，这里直接使用其随机结果。
        } else if (routableBean.getRouteType() == RouteType.ForceRoute.getValue()) {
            targetClusterNode = forceGetTargetClusterNode(routableBean.getTargetClusterNode());
        } else if (routableBean.getRouteType() == RouteType.ResourceIdRoute.getValue()) {
            targetClusterNode = getTargetClusterNode(routableBean.getMethod(), routableBean.getTargetResourceId());
        } else {
            throw new Exception("unknown routeType!");
        }

        if (targetClusterNode == null)
            throw new Exception("routeMessage() failed by getTargetClusterNode() failed!");

        ActorRef actor = this.actorSystem.actorOf(targetClusterNode.getHost(), targetClusterNode.getPort(), routableBean.getMethod());
        if (actor == null)
            throw new Exception("routeMessage() failed by actorOf() failed!");

        actor.tell(routableBean.getPayload(), sender);
        return targetClusterNode.getName();
    }

    /**
     * getTargetNodeList（集群广播使用）
     */
    public List<ClusterNode> getTargetNodeList() {
        this.readLock.lock();
        try {
            return consistentHashRouter.getClusterNodes();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * 集群变化
     */
    private void onClusterChanged() {
        try {
            final ClusterRouter cluster = this;
            this.zkHandler.addListener4Children(clusterNodeHomePath, new ZkChildrenChangeListener() {
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

    private void initActorSystem(String nodeName, String host, int port) {
        this.actorSystem = ActorSystem.create(nodeName, new InetSocketAddress(host, port));
        actorSystem.setMsgEncoder(new IActorMsgEncoder() {
            @Override
            public byte[] encode(Object obj) {
                return ActorMsgPayloadCodec.encode(obj);
            }
        });
        actorSystem.setMsgDecoder(new IActorMsgDecoder() {
            @Override
            public Object decode(byte[] bytes) {
                return ActorMsgPayloadCodec.decode(bytes);
            }
        });
    }

    private void validateMethod(String method) throws Exception {
        if (method.indexOf(JOINT_MARK) >= 0) {
            throw new Exception("method shouldn't indexOf " + JOINT_MARK);
        }

        return;
    }
}
