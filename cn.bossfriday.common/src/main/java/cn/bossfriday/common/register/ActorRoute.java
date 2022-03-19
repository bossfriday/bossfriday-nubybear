package cn.bossfriday.common.register;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActorRoute {
    String[] methods();

    int min() default 2;

    int max() default 8;

    String poolName() default "";

    int poolSize() default 4;
}
