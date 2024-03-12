# 1. 整体说明

nubybear为孩子给她一个毛绒棕熊取的名字，它当前只是一个用Java开发的分布式高性能文件服务，初衷为用一个实例去检验和完善ActorRpc。目前刚刚开始基于此去实现一个IM原型，但是有点纠结消息存储的选型问题，需要选择一个高效的支持SCAN范围查询的KV-NoSQL存储，目前纠结Cassandra、Pika 、SSDB 或者干脆自研。一因此IM原型可能先不支持历史消息存储，只是单纯的把离线消息（最近7天的消息）存放在Redis中。

最后申明：建设进度随缘（主要看业余时间），因为本来就是交流学习项目，最多成为一个IM及文件服务的“脚手架”参考或者二开项目。

# 2 项目说明

## 2.1 cn.bossfriday.common

**项目说明**

* Actor-Rpc详细介绍：**[https://blog.csdn.net/camelials/article/details/123327236](https://blog.csdn.net/camelials/article/details/123327236)**
* Actor-Rpc性能测试：**[https://blog.csdn.net/camelials/article/details/123614068](https://blog.csdn.net/camelials/article/details/123614068)**
* 公共组件及工具；

**Actor-Rpc设计预期**
* 使用protostuff序列化（.proto文件编写恶心，与Protocol Buffer性能几乎接近）
* 使用Netty进行通讯（同节点RPC不走网络，直接入收件箱队列）；
* 路由策略：随机路由、指定Key路由、资源Id路由、强制路由
* 使用ZK进行集群状态管理
* 使用自定义注解进行服务注册及辅助控制（线程数量、方法名称设置等）

## 2.2. cn.bossfriday.fileserver

**项目说明**

* 高性能分布式文件服务；
* 详细介绍及性能测试：**[https://blog.csdn.net/camelials/article/details/124613041](https://blog.csdn.net/camelials/article/details/124613041)**

**设计预期**

* 【效率】使用netty实现http file server（不聚合httpRequest为FullHttpRequest）。--这些天又回顾了Netty的官方示例，在一些问题的解决中一度也是很苦恼（之前写的文件服务使用了HttpObjectAggregator聚合）
* 【效率】使用ActorRpc做为RPC组件。
* 【效率】文件存储落盘采用：零拷贝+顺序写盘，以最大化提升落盘速度。临时文件写入使用零拷贝、存储文件读取采用带环形缓存区RandomAccessFile操作（存储文件读取暂时排除使用MappedByteBuffer，因为MappedByteBuffer使用不当会导致JVM Crash，完成后看压测结果再决定）。--存储文件读取最没有使用带Buffer的RandomAccessFile，而是同样使用零拷贝。当前读写均使用零拷贝，写均保障顺序写盘。
* 【服务器资源占用】chunk读写机制保障内存占用较小：chunk by chunk发送下载数据、chunk by chunk处理上传数据。 --由于ActorDispatcher内部封装了线程池，保障不了chunkData的串行处理，目前是应用层面做了处理（详见：StorageDispatcher），有点纠结是否将该逻辑下层到RPC层面。好像有人吐槽这是Actor模型的一个弊端，其实根据资源ID去保障执行线程的一致性，这个问题就解了。
* 【功能】功能规划：普通上传、Base64上传（客户端截屏使用）、普通下载、文件删除、支持2G以上大文件上传下载（断点上传、下载）
* 【扩展】主要步骤使用接口束行，依据版本获取实例，如果找不到则使用默认实现。这样做目的是为了达到类似装饰者模式的效果。
* 【安全】文件下载地址防止暴力穷举。
* 【安全】文件内容以一定数据结构存储与落盘文件中，服务端无法直接还远原始文件。

## 2.3 cn.bossfriday.jmeter

**项目说明**

* 压力测试；**后续考虑废弃**  ：使用https://github.com/bossfriday/bossfriday-jmeter 进行替代。

## 2.4 cn.bossfriday.im.access（建设中）
IM系统接入服务，计划使用netty实现一个基于TCP的私有协议的长连接接入服务；

## 2.5 cn.bossfriday.im.common（建设中）
IM系统公共组件/类等公用代码；

## 2.6 cn.bossfriday.im.navigator（建设中）
IM系统导航服务，负责客户端接入地址及全局配置下发（根据用户ID做一致性哈希计算得到接入服务cn.bossfriday.im.access的地址）；

## 2.7 cn.bossfriday.im.protocol（建设中）
IM系统接入协议栈及payload实体定义，协议为基于TCP的私有协议，该协议可以认为是一个非标的MQTT协议，例如：扩展剩余长度（标准的MQTT协议固定头只有两字节，消息最大长度较小）。消息体payload使用PB序列化方式；

# 3. Release Note

## 3.1 cn.bossfriday.fileserver

* 【2022-05-06】**原型发布，普通全量上传**：由于使用了chunk分片机制 + actorRPC，即使大文件不做断点上传，也不至于使文件服务炸线程和内存。chunked下载（完成）、压力测试（完成）。
* 【2022-11-10】**Base64上传**：客户端截屏等小文件上传使用（由于对不完整的部分base64字符串解码可能失败，因此需要先在httpServer层面自行进行聚合后再对完整信息进行base64解码，这是base64上传只适用于小文件上传的原因）。
* 【2022-11-15】**断点上传**：标记删除，因此不能释放磁盘空间。
* 【2022-11-21】**文件删除**
* 【2024-02-22】**过期文件自动清理**：物理删除，可以释放磁盘空间。
* 【2024-02-20】**异常恢复**：服务异常重启，断电宕机等情况下数据恢复（主要是未落盘的临时文件落盘，不过由于采用了零拷贝+顺序写的机制，临时文件落盘非常高效，一般情况很难撞上）；
* 【TBD暂不考虑实现】**文件副本高可用**：集群下的主从、文件副本等；主要思路为：由于文件都是按照时间和空间一直往后写，因此可以借鉴binlog的机制，当主发送变化时，立刻给对应的从发一个数据变更通知，从收到该通知之后立刻向主进行同步，从永远追主，只到追平。因此为了实现该机制，需要在落盘的同时去写一个类似的binlog：metaDataIndexLog。 另外，为了支持更好的容灾，可以考虑多主多从或者一主多从；

> **备注：**
> * **高可用暂时不考虑实现的主要原因是实现了之后其实可以直接商用（目前只是考虑供学习交流使用）。几年前实现那版以上都进行了实现，并且经历了上百家私有部署IM的项目及公司内部使用的考验；当前的完成情况可以认为是一个准商用水平，因为在多节点部署的情况下，只要不是所有的节点都挂了，功能不受影响，唯一影响的是宕机节点的文件下载（可以通过手工的方式将宕机节点的数据复制到另外的服务器上，即可恢复）。**
> * **接口均没有鉴权（一般来说删除接口需要鉴权、上传接口酌情鉴权），需要鉴权的自行实现。**
> * **ActorRPC放弃Disruptor改回最初使用的LinkedBlockingQueue。原因：环形缓存区内存不会释放，不利于传输文件这种大消息体的应用场景。另外形缓存区容量如果不大Disruptor性能与LinkedBlockingQueue差异不大。**

