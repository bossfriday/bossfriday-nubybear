package cn.bossfriday.common.http;

import cn.bossfriday.common.exception.ServiceRuntimeException;

import java.util.HashMap;

/**
 * HttpProcessorMapper
 *
 * @author chenx
 */
public class HttpProcessorMapper {

    private static HashMap<String, Class<? extends IHttpProcessor>> httpProcessorMapper = new HashMap<>();

    /**
     * putHttpProcessor
     *
     * @param apiRouteKey
     * @param httpProcessor
     */
    public static Class<? extends IHttpProcessor> putHttpProcessor(String apiRouteKey, Class<? extends IHttpProcessor> httpProcessor) {
        return httpProcessorMapper.putIfAbsent(apiRouteKey, httpProcessor);
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

        Class<? extends IHttpProcessor> processor = httpProcessorMapper.get(apiRouteKey);

        return processor.newInstance();
    }

    /**
     * contains
     *
     * @param apiRouteKey
     * @return
     */
    public static boolean contains(String apiRouteKey) {
        return httpProcessorMapper.containsKey(apiRouteKey);
    }
}
