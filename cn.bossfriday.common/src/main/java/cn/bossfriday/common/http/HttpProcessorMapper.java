package cn.bossfriday.common.http;

import cn.bossfriday.common.exception.ServiceRuntimeException;

import java.util.HashMap;

/**
 * HttpProcessorMapper
 *
 * @author chenx
 */
public class HttpProcessorMapper {

    private static HashMap<String, Class<? extends IHttpProcessor>> processorMapper = new HashMap<>();

    private HttpProcessorMapper() {
        // do nothing
    }

    /**
     * putHttpProcessor
     *
     * @param apiRouteKey
     * @param httpProcessor
     */
    public static Class<? extends IHttpProcessor> putHttpProcessor(String apiRouteKey, Class<? extends IHttpProcessor> httpProcessor) {
        return processorMapper.putIfAbsent(apiRouteKey, httpProcessor);
    }

    /**
     * getHttpProcessor
     *
     * @param apiRouteKey
     * @return
     */
    public static IHttpProcessor getHttpProcessor(String apiRouteKey) throws InstantiationException, IllegalAccessException {
        if (!contains(apiRouteKey)) {
            throw new ServiceRuntimeException("IHttpProcessor not existed! apiRouteKey=" + apiRouteKey);
        }

        Class<? extends IHttpProcessor> processor = processorMapper.get(apiRouteKey);

        return processor.newInstance();
    }

    /**
     * contains
     *
     * @param apiRouteKey
     * @return
     */
    public static boolean contains(String apiRouteKey) {
        return processorMapper.containsKey(apiRouteKey);
    }
}
