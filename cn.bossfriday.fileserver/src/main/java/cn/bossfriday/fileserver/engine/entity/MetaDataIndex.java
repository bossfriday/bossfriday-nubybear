package cn.bossfriday.fileserver.engine.entity;

import cn.bossfriday.common.utils.Base58Util;
import cn.bossfriday.common.utils.MurmurHashUtil;
import cn.bossfriday.fileserver.engine.core.ICodec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

@Slf4j
@Builder
public class MetaDataIndex implements ICodec<MetaDataIndex> {
    public static final int HASH_CODE_LENGTH = 4;

    @Getter
    @Setter
    private String clusterNode;         // 集群节点

    @Getter
    @Setter
    private byte storeEngineVersion;    // 存储引擎版本

    @Getter
    @Setter
    private String namespace;           // 存储空间

    @Getter
    @Setter
    private int time;                   // 时间 yyyyMMdd

    @Getter
    @Setter
    private long offset;                // 偏移量

    public MetaDataIndex() {

    }

    public MetaDataIndex(String clusterNode, byte storeEngineVersion, String namespace, int time, long offset) {
        this.clusterNode = clusterNode;
        this.storeEngineVersion = storeEngineVersion;
        this.namespace = namespace;
        this.time = time;
        this.offset = offset;
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
            dos.writeByte(storeEngineVersion);
            dos.writeUTF(namespace);
            dos.writeInt(time);
            dos.writeLong(offset);

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
            byte storeEngineVersion = dis.readByte();
            String namespace = dis.readUTF();
            int time = dis.readInt();
            long offset = dis.readLong();

            return MetaDataIndex.builder()
                    .clusterNode(clusterNode)
                    .storeEngineVersion(storeEngineVersion)
                    .namespace(namespace)
                    .time(time)
                    .offset(offset)
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
     * hash（业务逻辑验证和使用，仅为使下载地址的生成散列更开）
     */
    private static int hash(String clusterNode, String namespace, int time, long offset) throws Exception {
        String key = clusterNode + namespace + time + offset;
        return MurmurHashUtil.hash32(key);
    }

    @Override
    public String toString() {
        return "MetaDataIndex{" +
                "clusterNode='" + clusterNode + '\'' +
                ", storeEngineVersion=" + Byte.toUnsignedInt(storeEngineVersion) +
                ", namespace=" + namespace +
                ", time=" + time +
                ", offset=" + offset +
                '}';
    }

    public static void main(String[] args) throws Exception {
        MetaDataIndex index = new MetaDataIndex("clusterNode", (byte) 1, "normal", 20221012, 10000L);
        System.out.println(index.toString());
        byte[] data = index.serialize();
        //MetaDataIndex index1 = new MetaDataIndex().deserialize(data);
        //System.out.println(index1.toString());

        long begin = System.currentTimeMillis();
        for(int i=0;i<1000000;i++) {
            String base58String = Base58Util.encode(data);
            byte[] data2 = Base58Util.decode(base58String);
//        MetaDataIndex index1 = new MetaDataIndex().deserialize(data2);
        }
        System.out.println(System.currentTimeMillis() - begin);

    }
}
