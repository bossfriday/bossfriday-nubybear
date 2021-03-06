package cn.bossfriday.fileserver.engine.entity;

import cn.bossfriday.common.utils.GsonUtil;
import cn.bossfriday.common.utils.MurmurHashUtil;
import cn.bossfriday.fileserver.engine.core.ICodec;
import cn.bossfriday.fileserver.engine.impl.v1.MetaDataHandler;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

@Slf4j
@Data
@Builder
public class MetaDataIndex implements ICodec<MetaDataIndex> {
    public static final int HASH_CODE_LENGTH = 4;

    private String clusterNode;         // 集群节点
    private int storeEngineVersion;     // 存储引擎版本
    private String namespace;           // 存储空间
    private int time;                   // 上传时间戳
    private long offset;                // 落盘文件偏移量
    private int metaDataLength;         // 元数据长度（不包含FileDta）
    private String fileExtName;         // 文件扩展名

    public MetaDataIndex() {

    }

    public MetaDataIndex(String clusterNode, int storeEngineVersion, String namespace, int time, long offset, int metaDataLength, String fileExtName) {
        this.clusterNode = clusterNode;
        this.storeEngineVersion = storeEngineVersion;
        this.namespace = namespace;
        this.time = time;
        this.offset = offset;
        this.metaDataLength = metaDataLength;
        this.fileExtName = fileExtName;
    }

    @Override
    public byte[] serialize() throws Exception {
        ByteArrayOutputStream out = null;
        DataOutputStream dos = null;
        try {
            out = new ByteArrayOutputStream();
            dos = new DataOutputStream(out);

            int hashInt = hash(this.clusterNode, this.namespace, this.time, this.offset);
            dos.writeInt(hashInt);
            dos.writeUTF(clusterNode);
            dos.writeByte((byte) storeEngineVersion);
            dos.writeUTF(namespace);
            dos.writeInt(time);
            dos.writeLong(offset);
            dos.writeInt(metaDataLength);
            dos.writeUTF(fileExtName);

            return out.toByteArray();
        } finally {
            try {
                if (dos != null)
                    dos.close();

                if (out != null)
                    out.close();
            } catch (Exception ex) {
                log.warn("serialize release error!", ex);
            }
        }
    }

    @Override
    public MetaDataIndex deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream in = null;
        DataInputStream dis = null;
        try {
            in = new ByteArrayInputStream(bytes);
            dis = new DataInputStream(in);

            int hashInt = dis.readInt();
            String clusterNode = dis.readUTF();
            int storeEngineVersion = Byte.toUnsignedInt(dis.readByte());
            String namespace = dis.readUTF();
            int time = dis.readInt();
            long offset = dis.readLong();
            int metaDataLength = dis.readInt();
            String fileExtName = dis.readUTF();

            return MetaDataIndex.builder()
                    .clusterNode(clusterNode)
                    .storeEngineVersion(storeEngineVersion)
                    .namespace(namespace)
                    .time(time)
                    .offset(offset)
                    .metaDataLength(metaDataLength)
                    .fileExtName(fileExtName)
                    .build();
        } finally {
            try {
                if (dis != null)
                    dis.close();

                if (in != null)
                    in.close();
            } catch (Exception ex) {
                log.warn("deserialize release error!", ex);
            }
        }
    }

    /**
     * hash64
     */
    public long hash64() throws Exception {
        return hash64(this.namespace, this.time, this.offset);
    }

    /**
     * hash64
     */
    public static long hash64(String namespace, int time, long offset) throws Exception {
        return MurmurHashUtil.hash64(namespace + time + offset);
    }

    /**
     * hash（业务逻辑验证和使用，仅为使下载地址的生成散列更开）
     */
    private static int hash(String clusterNode, String namespace, int time, long offset) throws Exception {
        String key = clusterNode + namespace + time + offset;
        return MurmurHashUtil.hash32(key);
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }

    public static void main(String[] args) throws Exception {
        MetaDataIndex index = new MetaDataIndex("clusterNode", 1, "normal", 20220101, 10000L, 200, "jpg");
        System.out.println(index.toString());
        byte[] data = index.serialize();
        MetaDataIndex index1 = new MetaDataIndex().deserialize(data);
        System.out.println(index1.toString());

        MetaDataHandler metaDataHandler = new MetaDataHandler();
        String str = metaDataHandler.downloadUrlEncode(index);
        System.out.println(str);
    }
}
