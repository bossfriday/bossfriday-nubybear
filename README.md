# 1. 整体说明
nubybear是我家孩子的一只棕熊毛绒玩偶，没有其他意思。 该项目的目的为：用一些实际的项目来验证actor-rpc，使其达到商用标准，之前actor-rpc项目原则上不优先维护。  

# 2. cn.bossfriday.common
* actor-rpc，相关说明详见：https://github.com/bossfriday/actor-rpc#readme
* 公共util类

# 3. cn.bossfriday.fileserver
【doing】**一个用Java开发的分布式高性能文件服务**

## 3.1 设计预期
* 1、【效率】使用netty实现http file server。
* 2、【效率】使用ActorRpc做为RPC组件。
* 3、【效率】文件存储落盘采用：零拷贝+顺序写盘，以最大化提升落盘速度。
* 4、【服务器资源占用】thunk读写机制保障内存占用较小：文件下载每次从落盘文件中读取一小块thunk内容；文件上传不聚合httpRequest为FullHttpRequest，上传数据处理为每次处理一下块thunk内容。
* 4、【功能】功能规划：普通上传、断点上传、Base64上传（客户端截屏使用）、普通下载、断点下载、文件删除
* 6、【安全】文件下载地址防止暴力穷举。
* 7、【安全】文件内容以一定数据结构存储与落盘文件中，服务端无法直接还远原始文件。

# 4. cn.bossfriday.jmeter
* actor-rpc压力测试   
* 【todo】文件服务压力测试

# 5. cn.bossfriday.mocks**
* 项目所需挡板。
