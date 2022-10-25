package cn.bossfriday.common.register;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ActorRegister
 *
 * @author chenx
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActorRoute {

    /**
     * methods
     *
     * @return
     */
    String[] methods();

    /**
     * min
     *
     * @return
     */
    int min() default 2;

    /**
     * max
     *
     * @return
     */
    int max() default 8;

    /**
     * poolName
     *
     * @return
     */
    String poolName() default "";

    /**
     * poolSize
     *
     * @return
     */
    int poolSize() default 4;
}
