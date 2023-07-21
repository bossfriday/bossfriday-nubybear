package cn.bossfriday.common.test.rpc;


import cn.bossfriday.common.rpc.actor.ActorRef;

/**
 * 不带路由策略测试actorSystem基础功能
 */
public class Bootstrap {
    public static void main(String[] args) throws Exception {
        BarCluster.getInstance().start();

        long begin = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Foo foo = Foo.builder().id(String.valueOf(i)).name("foo" + i).age(i).desc("Foo is a fuck oriented object!").build();
            onRequestReceived(foo);
        }
        System.out.println("takeUpTime:" + (System.currentTimeMillis() - begin));
    }

    /**
     * onRequestReceived：模拟接入服务收到一个请求的处理
     */
    private static void onRequestReceived(Foo foo) throws Exception {
        ActorRef from = BarCluster.getInstance().getActorSystem().actorOf("method1");
        ActorRef to = BarCluster.getInstance().getActorSystem().actorOf("method2");
        to.tell(foo, from);  // rpc to actor2
    }
}
