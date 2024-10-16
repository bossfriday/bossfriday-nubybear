package cn.bossfriday.boot;

import cn.bossfriday.common.PluginBootstrap;
import cn.bossfriday.common.rpc.actor.BaseUntypedActor;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Bootstrap
 *
 * @author chenx
 */
public class SystemClusterBootstrap {

    public static void main(String[] args) throws Exception {
        String className = "cn.bossfriday.im.access.common.conf.ImAccessConfig";
        Class<?> clazz = Class.forName(className);
        System.out.println(className.toString());

        List<Class<? extends BaseUntypedActor>> actorClassList = new ArrayList<>();
        Set<Class<? extends BaseUntypedActor>> set = new Reflections().getSubTypesOf(BaseUntypedActor.class);
        actorClassList.addAll(set);
        System.out.println(actorClassList.size());

        Set<Class<? extends PluginBootstrap>> set2 = new Reflections().getSubTypesOf(PluginBootstrap.class);
        System.out.println(set2.size());
    }
}
