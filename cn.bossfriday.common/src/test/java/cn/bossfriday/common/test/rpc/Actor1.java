package cn.bossfriday.common.test.rpc;

import cn.bossfriday.common.rpc.actor.BaseUntypedActor;

/**
 * 类描述 （建议用中文）
 *
 * @author chenx
 * @date 2022/10/25
 */
public class Actor1 extends BaseUntypedActor {
    @Override
    public void onMsgReceive(Object msg) {
        try {
            if (msg instanceof FooResult) {
                FooResult result = (FooResult) msg;
                System.out.println("method2 process done, " + result.toString());

                return;
            }
        } finally {
            msg = null;
        }
    }
}