package cn.bossfriday.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

/**
 * GsonUtil
 *
 * @author chenx
 */
public class GsonUtil {

    private static Gson gson = null;

    static {
        if (gson == null) {
            gson = new GsonBuilder().disableHtmlEscaping().create();
        }
    }

    private GsonUtil() {

    }

    /**
     * toJson
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        String gsonString = null;
        if (gson != null) {
            gsonString = gson.toJson(object);
        }

        return gsonString;
    }

    /**
     * gsonToBean
     *
     * @param gsonString
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String gsonString, Class<T> cls) {
        T t = null;
        if (gson != null) {
            t = gson.fromJson(gsonString, cls);
        }

        return t;
    }

    /**
     * gsonToList
     *
     * @param gsonString
     * @param <T>
     * @return
     */
    public static <T> List<T> gsonToList(String gsonString) {
        List<T> list = null;
        if (gson != null) {
            list = gson.fromJson(gsonString, new TypeToken<List<T>>() {
            }.getType());
        }

        return list;
    }


    /**
     * gsonToListMaps
     *
     * @param gsonString
     * @param <T>
     * @return
     */
    public static <T> List<Map<String, T>> gsonToListMaps(String gsonString) {
        List<Map<String, T>> list = null;
        if (gson != null) {
            list = gson.fromJson(gsonString,
                    new TypeToken<List<Map<String, T>>>() {
                    }.getType());
        }

        return list;
    }

    /**
     * gsonToMaps
     *
     * @param gsonString
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> gsonToMaps(String gsonString) {
        Map<String, T> map = null;
        if (gson != null) {
            map = gson.fromJson(gsonString, new TypeToken<Map<String, T>>() {
            }.getType());
        }

        return map;
    }
}
