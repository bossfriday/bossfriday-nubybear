package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ProtostuffCodecUtil
 *
 * @author chenx
 */
public class ProtostuffCodecUtil {

    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    private ProtostuffCodecUtil() {

    }

    /**
     * serialize
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clazz);
            if (schema == null) {
                throw new ServiceRuntimeException(clazz.getName() + " Schema is null!");
            }

            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    /**
     * deserialize
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data, Class<T> clazz) {
        Schema<T> schema = getSchema(clazz);
        if (schema == null) {
            throw new ServiceRuntimeException(clazz.getName() + " Schema is null!");
        }

        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, obj, schema);

        return obj;
    }

    /**
     * getSchema
     *
     * @param clazz
     * @param <T>
     * @return
     */
    private static <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
        if (Objects.isNull(schema)) {
            schema = RuntimeSchema.getSchema(clazz);
            if (Objects.nonNull(schema)) {
                schemaCache.put(clazz, schema);
            }
        }

        return schema;
    }
}
