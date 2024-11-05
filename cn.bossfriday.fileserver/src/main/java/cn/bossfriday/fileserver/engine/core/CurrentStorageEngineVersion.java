package cn.bossfriday.fileserver.engine.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static cn.bossfriday.im.common.constant.FileServerConstant.DEFAULT_STORAGE_ENGINE_VERSION;

/**
 * CurrentStorageEngineVersion
 *
 * @author chenx
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentStorageEngineVersion {

    /**
     * version
     *
     * @return
     */
    int version() default DEFAULT_STORAGE_ENGINE_VERSION;
}
