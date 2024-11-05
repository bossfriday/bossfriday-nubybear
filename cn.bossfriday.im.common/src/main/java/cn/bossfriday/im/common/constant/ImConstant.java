package cn.bossfriday.im.common.constant;

/**
 * ImConstant
 *
 * @author chenx
 */
public class ImConstant {

    private ImConstant() {
        // do nothing
    }

    /**
     * Actor
     */
    public static final String ACTOR_POOL_NAME_API = "actor_pool_api";

    /**
     * Qos
     */
    public static final int QOS_AT_MOST_ONCE = 0;
    public static final int QOS_AT_LEAST_ONCE = 1;
    public static final int QOS_EXACTLY_ONCE = 2;

    /**
     * Methods
     */
    public static final String METHOD_USER_GET_TOKEN = "getToken";
}
