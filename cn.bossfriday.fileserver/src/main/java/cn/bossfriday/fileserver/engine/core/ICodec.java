package cn.bossfriday.fileserver.engine.core;

import java.io.IOException;

/**
 * ICodec
 *
 * @author chenx
 */
public interface ICodec<T> {

    /**
     * serialize
     *
     * @return
     * @throws IOException
     */
    byte[] serialize() throws IOException;

    /**
     * deserialize
     *
     * @param bytes
     * @return
     * @throws IOException
     */
    T deserialize(byte[] bytes) throws IOException;
}
