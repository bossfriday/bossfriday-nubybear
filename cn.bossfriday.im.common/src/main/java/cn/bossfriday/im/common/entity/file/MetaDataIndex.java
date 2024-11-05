package cn.bossfriday.im.common.entity.file;

import cn.bossfriday.common.utils.MurmurHashUtil;
import cn.bossfriday.im.common.codec.ICodec;
import lombok.*;

import java.io.*;

/**
 * MetaDataIndex
 *
 * @author chenx
 */
@ToString
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetaDataIndex implements ICodec<MetaDataIndex> {

    public static final int HASH_CODE_LENGTH = 4;

    /**
     * 集群节点
     */
    private String clusterNode;

    /**
     * 存储引擎版本
     */
    private int storeEngineVersion;

    /**
     * 存储空间
     */
    private String storageNamespace;

    /**
     * 上传时间戳
     */
    private int time;

    /**
     * 落盘文件偏移量
     */
    private long offset;

    /**
     * 元数据长度（不包含FileData）
     */
    private int metaDataLength;

    /**
     * 文件扩展名
     */
    private String fileExtName;

    @Override
    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(out)) {
            int hashInt = hash(this.clusterNode, this.storageNamespace, this.time, this.offset);
            dos.writeInt(hashInt);
            dos.writeUTF(this.clusterNode);
            dos.writeByte((byte) this.storeEngineVersion);
            dos.writeUTF(this.storageNamespace);
            dos.writeInt(this.time);
            dos.writeLong(this.offset);
            dos.writeInt(this.metaDataLength);
            dos.writeUTF(this.fileExtName);

            return out.toByteArray();
        }
    }

    @Override
    public MetaDataIndex deserialize(byte[] bytes) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(in)) {
            /**
             * just read an int here
             * 严格来说这里还需要做防篡改校验（下载URL主要保障防暴力穷举即可，防篡改意愿并不强烈）
             */
            dis.readInt();
            String nodeValue = dis.readUTF();
            int versionValue = Byte.toUnsignedInt(dis.readByte());
            String namespaceValue = dis.readUTF();
            int timeValue = dis.readInt();
            long offsetValue = dis.readLong();
            int metaDataLengthValue = dis.readInt();
            String fileExtNameValue = dis.readUTF();

            return MetaDataIndex.builder()
                    .clusterNode(nodeValue)
                    .storeEngineVersion(versionValue)
                    .storageNamespace(namespaceValue)
                    .time(timeValue)
                    .offset(offsetValue)
                    .metaDataLength(metaDataLengthValue)
                    .fileExtName(fileExtNameValue)
                    .build();
        }
    }

    /**
     * hash64（相比hash32哈希碰撞几率进一步降低）
     */
    public long hash64() {
        return hash64(this.storageNamespace, this.time, this.offset);
    }

    /**
     * hash64（相比hash32哈希碰撞几率进一步降低）
     *
     * @param namespace
     * @param time
     * @param offset
     * @return
     */
    public static long hash64(String namespace, int time, long offset) {
        return MurmurHashUtil.hash64(namespace + time + offset);
    }

    /**
     * hash
     * 这里仅为使下载地址的生成散列更开
     *
     * @param clusterNode
     * @param namespace
     * @param time
     * @param offset
     * @return
     */
    private static int hash(String clusterNode, String namespace, int time, long offset) {
        String key = clusterNode + namespace + time + offset;
        return MurmurHashUtil.hash32(key);
    }
}
