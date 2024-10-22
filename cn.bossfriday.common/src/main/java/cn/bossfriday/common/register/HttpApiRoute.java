package cn.bossfriday.common.register;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HttpApiRoute
 *
 * @author chenx
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpApiRoute {

    /**
     * apiRouteKey
     * 先不做API的集群节点一致性路由（目前还没有这种强需求）
     *
     * @return
     */
    String apiRouteKey() default "";
}
