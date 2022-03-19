package cn.bossfriday.jmeter.sampler;

import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.router.RoutableBeanFactory;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.jmeter.NubyBearSamplerBuilder;
import cn.bossfriday.jmeter.common.Const;
import cn.bossfriday.jmeter.common.SamplerConfig;
import cn.bossfriday.jmeter.rpc.modules.Foo;
import org.apache.jmeter.samplers.SampleResult;

@NubyBearSamplerBuilder.SamplerType(behaviorName = Const.GUI_BEHAVIOR_ACTOR_RPC)
public class RpcSampler extends BaseSampler {
    private ActorRef actor;
    private RoutableBean routableBean;

    public RpcSampler(SamplerConfig config) {
        super(config);
    }

    @Override
    public void testStarted() {
        try{
            actor = ClusterRouterFactory.getClusterRouter().getActorSystem().actorOf("fooClient");
        } catch (Exception ex) {
            this.isTestStartedError = true;
            log.error("RpcSampler.testStarted() error!", ex);
        }
    }

    @Override
    public SampleResult sample() {
        SampleResult result = new SampleResult();
        result.setSampleLabel(this.config.getBehaviorName());
        if (isTestStartedError)
            return sampleFailedByTestStartedError();

        try {

        } catch (Exception ex) {
            BaseSampler.setSampleResult("500", "Exception:" + ex.getMessage(), false, result);
            log.error("RpcSampler.sample() error!", ex);
        }

        return result;
    }

    @Override
    public void testEnded() {

    }
}
