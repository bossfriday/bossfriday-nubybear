package cn.bossfriday.fileserver.engine.entity;

import cn.bossfriday.fileserver.engine.core.ICodec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

@Slf4j
public class StorageFileHeader implements ICodec<StorageFileHeader> {
    public static final int BIG_FILE_HEADER_LENGTH = 8;

    @Getter
    @Setter
    private long capacity;

    public StorageFileHeader(long capacity) {
        this.capacity = capacity;
    }

    @Override
    public byte[] serialize() throws Exception {
        ByteArrayOutputStream out = null;
        DataOutputStream dos = null;

        try {
            out = new ByteArrayOutputStream();
            dos = new DataOutputStream(out);
            dos.writeLong(this.capacity);

            return out.toByteArray();
        } catch (Exception e) {
            log.error("StorageFileHeader.serialize() error", e);
            return null;
        } finally {
            try {
                if (dos != null)
                    dos.close();

                if (out != null)
                    out.close();
            } catch (Exception e2) {
                log.error("serialize release error!", e2);
            }
        }
    }

    @Override
    public StorageFileHeader deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream in = null;
        DataInputStream dis = null;

        try {
            in = new ByteArrayInputStream(bytes);
            dis = new DataInputStream(in);

            return new StorageFileHeader(dis.readLong());
        } catch (Exception e) {
            log.error("StorageFileHeader.deserialize() error!", e);
            return null;
        } finally {
            try {
                if (dis != null)
                    dis.close();

                if (in != null)
                    in.close();
            } catch (Exception e2) {
                log.error("deserialize release error!", e2);
            }
        }
    }
}
