package cn.bossfriday.im.api.common.enums;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.http.UrlParser;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.bossfriday.im.api.common.ApiConstant.HTTP_URL_ARGS_API_VERSION;

/**
 * ApiRequestType
 *
 * @author chenx
 */
public enum ApiRequestType {

    /**
     * client api
     */
    CLIENT_NAV("clientNav", HttpMethod.POST.name(), new UrlParser(String.format("/api/{%s}/client/nav", HTTP_URL_ARGS_API_VERSION))),

    /**
     * user api
     */
    USER_GET_TOKEN("userGetToken", HttpMethod.POST.name(), new UrlParser(String.format("/api/{%s}/user/getToken", HTTP_URL_ARGS_API_VERSION))),
    ;

    @Getter
    private String apiRouteKey;

    @Getter
    private String httpMethod;

    @Getter
    private UrlParser urlParser;

    ApiRequestType(String apiRouteKey, String httpMethod, UrlParser urlParser) {
        this.apiRouteKey = apiRouteKey;
        this.httpMethod = httpMethod;
        this.urlParser = urlParser;
    }

    private static final Map<String, List<ApiRequestType>> API_REQUEST_TYPE_MAP = Maps.newHashMap();

    static {
        for (ApiRequestType entry : ApiRequestType.values()) {
            List<ApiRequestType> apiRequestTypeList = API_REQUEST_TYPE_MAP.get(entry.httpMethod);
            if (Objects.isNull(apiRequestTypeList)) {
                apiRequestTypeList = Lists.newArrayList();
                API_REQUEST_TYPE_MAP.put(entry.httpMethod, apiRequestTypeList);
            }

            apiRequestTypeList.add(entry);
        }
    }

    /**
     * getByMethod
     *
     * @param httpMethod
     * @return
     */
    public static List<ApiRequestType> getByMethod(String httpMethod) {
        if (StringUtils.isEmpty(httpMethod)) {
            throw new ServiceRuntimeException("httpMethod is empty!");
        }

        return API_REQUEST_TYPE_MAP.get(httpMethod);
    }

    /**
     * find
     *
     * @param httpMethod
     * @param uri
     * @return
     */
    public static ApiRequestType find(String httpMethod, URI uri) {
        List<ApiRequestType> list = ApiRequestType.getByMethod(httpMethod);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        for (ApiRequestType entry : list) {
            if (entry.getUrlParser().isMatch(uri)) {
                return entry;
            }
        }

        return null;
    }
}
