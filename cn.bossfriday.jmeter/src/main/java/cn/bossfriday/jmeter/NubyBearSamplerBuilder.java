package cn.bossfriday.jmeter;

import cn.bossfriday.common.rpc.actor.UntypedActor;
import cn.bossfriday.jmeter.common.SamplerConfig;
import cn.bossfriday.jmeter.sampler.BaseSampler;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class NubyBearSamplerBuilder {
    @Documented
    @Target({ElementType.TYPE})
    @Retention(RUNTIME)
    public @interface SamplerType {
        String behaviorName() default "";
    }

    public static String[] behaviorNames;

    private static final Logger log = LoggerFactory.getLogger(NubyBearSamplerBuilder.class);
    private static final String reflectPrefix = "cn.bossfriday.jmeter.sampler";
    private static ConcurrentHashMap<String, Class<?>> clazzMap = new ConcurrentHashMap<>();    // K:behaviorName, V:Class<?>

    static {
        // init clazzMap
        Set<Class<? extends BaseSampler>> clazzes = new Reflections().getSubTypesOf(BaseSampler.class);
        for (Class<?> clazz : clazzes) {
            SamplerType samplerType = clazz.getAnnotation(SamplerType.class);
            if (!clazzMap.containsKey(samplerType.behaviorName())) {
                clazzMap.put(samplerType.behaviorName(), clazz);
                log.info("load sampler class " + samplerType.behaviorName() + " done");
            } else {
                log.warn("duplicated behaviorName:" + samplerType.behaviorName());
            }
        }

        // init behaviorNames
        behaviorNames = new String[clazzMap.size()];
        int x = 0;
        for (String key : clazzMap.keySet()) {
            behaviorNames[x] = key;
            x++;
        }

        // sort
        int size = behaviorNames.length;
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < behaviorNames.length; j++) {
                if (behaviorNames[i].compareTo(behaviorNames[j]) > 0) {
                    String temp = behaviorNames[i];
                    behaviorNames[i] = behaviorNames[j];
                    behaviorNames[j] = temp;
                }
            }
        }
    }

    /**
     * getSampler
     */
    public static BaseSampler getSampler(SamplerConfig config) throws Exception {
        String behaviorName = config.getBehaviorName();
        if (!clazzMap.containsKey(config.getBehaviorName()))
            throw new Exception("invalid behaviorName!(" + behaviorName + ")");

        return (BaseSampler) clazzMap.get(behaviorName).getConstructor(SamplerConfig.class).newInstance(config);
    }
}
