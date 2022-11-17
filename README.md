# Release Note
* 普通全量上传（完成）
* Base64上传（完成）
* 断点上传（完成）
* Thunked下载（完成）
* 压力测试（完成）
* 指定文件删除（TODO）
* 过期文件自动清理（TODO）
* 异常恢复（TODO）：服务异常重启，断电宕机等情况下数据恢复；
* 高可用（TODO）：集群下的主从、文件副本等；

## 备注
* ActorRPC放弃Disruptor改回最初使用的LinkedBlockingQueue。原因：环形缓存区内存不会释放，不利于传输文件这种大消息体的应用场景。另外形缓存区容量如果不大Disruptor性能与LinkedBlockingQueue差异不大。

## 性能
### 环境说明
* 服务器：8核16G 云服务器：1台；
* 文件大小：100K
* 压测工具：JMeter
* 网络环境：内网（使用1台与文件服务内网互通的打压机压测）

### 100K文件上传
* 秒吞吐量：2455.19
* 平均延时：27.53 毫秒
[![O1lfyt.png](https://s1.ax1x.com/2022/05/08/O1lfyt.png)](https://imgtu.com/i/O1lfyt)

### 100K文件下载
* 秒吞吐量：3513.70
* 响应平均延时：2.64 毫秒
[![O11Tnx.png](https://s1.ax1x.com/2022/05/08/O11Tnx.png)](https://imgtu.com/i/O11Tnx)


# 1. 整体说明
nubybear是我家孩子的一只棕熊毛绒玩偶，没有其他意思。 该项目的目的为：用一些实际的项目来验证actor-rpc，使其达到商用标准，之前actor-rpc项目原则上不优先维护。  

# 2. cn.bossfriday.common
* actor-rpc，相关说明详见：https://github.com/bossfriday/actor-rpc#readme
* 公共util类

# 3. cn.bossfriday.fileserver
【doing】**一个用Java开发的分布式高性能文件服务**，开源出来给有缘人抄作业吧。

## 3.1 设计预期
* 【效率】使用netty实现http file server（不聚合httpRequest为FullHttpRequest）。--这些天又回顾了Netty的官方示例，在一些问题的解决中一度也是很苦恼（之前写的文件服务使用了HttpObjectAggregator聚合）
* 【效率】使用ActorRpc做为RPC组件。
* 【效率】文件存储落盘采用：零拷贝+顺序写盘，以最大化提升落盘速度。临时文件写入使用零拷贝、存储文件读取采用带环形缓存区RandomAccessFile操作（存储文件读取暂时排除使用MappedByteBuffer，因为MappedByteBuffer使用不当会导致JVM Crash，完成后看压测结果再决定）。--存储文件读取最没有使用带Buffer的RandomAccessFile，而是同样使用零拷贝。当前读写均使用零拷贝，写均保障顺序写盘。
* 【服务器资源占用】thunk读写机制保障内存占用较小：thunk by thunk发送下载数据、thunk by thunk处理上传数据。 --由于ActorDispatcher内部封装了线程池，保障不了thunkData的串行处理，目前是应用层面做了处理（详见：StorageDispatcher），有点纠结是否将该逻辑下层到RPC层面。好像有人吐槽这是Actor模型的一个弊端，其实根据资源ID去保障执行线程的一致性，这个问题就解了。
* 【功能】功能规划：普通上传、Base64上传（客户端截屏使用）、普通下载、文件删除、支持2G以上大文件上传下载（断点上传、下载）
* 【扩展】主要步骤使用接口束行，依据版本获取实例，如果找不到则使用默认实现。这样做目的是为了达到类似装饰者模式的效果。
* 【安全】文件下载地址防止暴力穷举。
* 【安全】文件内容以一定数据结构存储与落盘文件中，服务端无法直接还远原始文件。

## 3.2 当前完成情况
上面提到的设计初衷除了功能以外，其他的已经全部实现。功能上，目前只实现了普通上传和下载（数据结构支持支持2G以上的大文件存储，offset为long类型）。虽然功能上未完成的很多，不过ActorRPC验证的目的已经达成，所以目前没有什么动力去完善功能了（后续找时间慢慢加吧。。。），例如：支持主从高可用、服务意外终止恢复、断点上传、Base64字符串上传、文件过期清理……


# 4. cn.bossfriday.jmeter
* actor-rpc压力测试   
文件服务压力测试
**后续考虑废弃** 使用https://github.com/bossfriday/bossfriday-jmeter 进行替代
