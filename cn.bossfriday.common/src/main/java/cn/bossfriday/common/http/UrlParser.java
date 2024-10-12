package cn.bossfriday.common.http;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.http.model.UrlElement;
import cn.bossfriday.common.http.model.UrlElementType;
import lombok.NonNull;
import org.apache.commons.codec.Charsets;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.bossfriday.common.SystemConstant.PATH_DELIMITER;

/**
 * UrlParser
 *
 * @author chenx
 */
public class UrlParser {

    final List<UrlElement> urlElements = new ArrayList<>();

    public UrlParser(@NonNull final String template) {
        this.validate(template);
        final String[] pathElements = template.split(PATH_DELIMITER);

        for (final String element : pathElements) {
            if (this.isAttribute(element)) {
                final String elementName = element.substring(1, element.length() - 1);
                this.urlElements.add(new UrlElement(UrlElementType.ATTRIBUTE, elementName));
            } else {
                this.urlElements.add(new UrlElement(UrlElementType.FIXED, element));
            }
        }
    }

    /**
     * parsePath
     *
     * @param uri
     * @return
     */
    public Map<String, String> parsePath(@NonNull final URI uri) {
        String path = uri.getPath();
        this.validate(path);
        final Map<String, String> pathArgsMap = new HashMap<>(16);
        final String[] pathElements = path.split(PATH_DELIMITER);
        if (pathElements.length != this.urlElements.size()) {
            return pathArgsMap;
        }

        // i= 1: ignore the 0th element, it's always empty!
        for (int i = 1; i < this.urlElements.size(); i++) {
            final String element = pathElements[i];
            final UrlElement urlElement = this.urlElements.get(i);

            switch (urlElement.getType()) {
                case FIXED:
                    if (!element.equals(urlElement.getName())) {
                        return pathArgsMap;
                    }

                    break;

                case ATTRIBUTE:
                    pathArgsMap.put(urlElement.getName(), element);
                    break;

                default:
                    throw new ServiceRuntimeException("invalid RestUrlTemplateElement !");
            }
        }

        return pathArgsMap;
    }

    /**
     * parseQuery
     *
     * @param uri
     * @return
     */
    public Map<String, String> parseQuery(@NonNull final URI uri) {
        String query = uri.getQuery();
        final Map<String, String> queryArgsMap = new HashMap<>(16);
        if (StringUtils.isEmpty(query)) {
            return queryArgsMap;
        }

        List<NameValuePair> params = URLEncodedUtils.parse(uri, Charsets.UTF_8);
        for (NameValuePair param : params) {
            queryArgsMap.putIfAbsent(param.getName(), param.getValue());
        }

        return queryArgsMap;
    }

    /**
     * getArgsValue
     *
     * @param argsMap
     * @param name
     * @return if args not existed, throw BizException;
     */
    public static String getArgsValue(Map<String, String> argsMap, String name) {
        if (MapUtils.isEmpty(argsMap) || !argsMap.containsKey(name)) {
            throw new ServiceRuntimeException("URI args not existed, name:" + name + " !");
        }

        return argsMap.get(name);
    }

    /**
     * validate
     *
     * @param path
     */
    private void validate(@NonNull final String path) {
        if (!path.startsWith(PATH_DELIMITER)) {
            throw new ServiceRuntimeException("A template must start with " + PATH_DELIMITER);
        }
    }

    /**
     * isAttribute
     *
     * @param str
     * @return
     */
    private boolean isAttribute(@NonNull final String str) {
        return str.startsWith("{") && str.endsWith("}");
    }
}
