# 1. 整体说明

nubybear为孩子给她一个毛绒棕熊取的名字，它当前只是一个用Java开发的分布式高性能文件服务，初衷为用一个实例去检验和完善ActorRpc。目前刚刚开始基于此去实现一个IM原型，但是有点纠结消息存储的选型问题，需要选择一个高效的支持SCAN范围查询的KV-NoSQL存储，目前纠结Cassandra、Pika 、SSDB 或者干脆自研。一因此IM原型可能先不支持历史消息存储，只是单纯的把离线消息（最近7天的消息）存放在Redis中。

最后申明：建设进度随缘（主要看业余时间），因为本来就是交流学习项目，最多成为一个IM及文件服务的“脚手架”参考或者二开项目。

# 2 项目说明
1、由于只是想做一个IM及文件服务的“脚手架”参考或者二开项目，因此希望系统尽肯能少的去依赖中间件，初步的想法是系统的启动和运行只依赖一个ZK即可，那么只能尽量的将一些原本应该持久到DB或者配置中心的信息放到本地配置文件中。目前服务启动配置文件为：cn.bossriday.common中的SystemConfig.yaml；其他全局配置或者业务数据放在：cn.bossfirday.im.common项目中的GlobalConfigAll.yaml。

2、启动方式：cn.bossfriday.boot.Bootstrap中的main为启动所有服务入口（当前配置为连接本机ZK）。如果只想启动某一个服务，可以运行各个服务自己的ApplicationBootstrap。

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

## 2.2 cn.bossfriday.im.common
IM系统公共组件/类等公用代码；

## 2.3 cn.bossfriday.im.protocol
**项目说明**
* IM系统接入协议栈及payload实体定义，协议为基于TCP的私有协议，该协议可以认为是一个非标的MQTT协议，例如：变更消息类型（4位16种消息类型的含义）、扩展剩余长度（标准的MQTT协议固定头只有两字节，消息最大长度较小）。消息体payload使用PB序列化方式；
* 详细介绍：**[https://blog.csdn.net/camelials/article/details/136879608](https://blog.csdn.net/camelials/article/details/136879608)**

## 2.4. cn.bossfriday.fileserver

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

## 2.5 cn.bossfriday.im.access（建设中）
IM系统接入服务，协议使用非标MQTT；

## 2.6 cn.bossfriday.im.api（建设中）
IM系统接口服务，负责IM系统对外API提供，同时提供导航接口（不单独搞一个IM导航服务了）；


