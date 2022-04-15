package cn.bossfriday.fileserver.store.core;

public interface ICodec<T> {
    /**
     * serialize
     *
     * @return
     * @throws Exception
     */
    byte[] serialize() throws Exception;

    /**
     * deserialize
     *
     * @param bytes
     * @return
     * @throws Exception
     */
    T deserialize(byte[] bytes) throws Exception;
}
