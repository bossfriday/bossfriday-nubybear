package cn.bossfriday.jmeter.sampler;

import cn.bossfriday.common.AbstractServiceBootstrap;
import cn.bossfriday.common.conf.ServiceConfig;
import cn.bossfriday.common.router.ClusterNode;
import cn.bossfriday.common.router.ClusterRouterFactory;
import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.router.RoutableBeanFactory;
import cn.bossfriday.common.rpc.actor.ActorRef;
import cn.bossfriday.jmeter.NubyBearSamplerBuilder;
import cn.bossfriday.jmeter.common.Const;
import cn.bossfriday.jmeter.common.SamplerConfig;
import cn.bossfriday.jmeter.rpc.modules.Foo;
import org.apache.jmeter.samplers.SampleResult;

import java.util.concurrent.atomic.AtomicInteger;

import static cn.bossfriday.jmeter.common.Const.FOO_METHOD_NAME;
import static cn.bossfriday.jmeter.common.Const.FOO_SERVER_METHOD_NAME;

@NubyBearSamplerBuilder.SamplerType(behaviorName = Const.GUI_BEHAVIOR_ACTOR_RPC)
public class RpcSampler extends BaseSampler {
    private static AtomicInteger sampleIndex;
    private static ActorRef sender;
    private static AbstractServiceBootstrap serviceBootstrap;
    private static ServiceConfig serviceConfig;
    private static String sampleLabel;

    public RpcSampler(SamplerConfig config) {
        super(config);
    }

    @Override
    public void testStarted() {
        try {
            sampleIndex = new AtomicInteger(0);
            sampleLabel = this.config.getBehaviorName();

            // init serviceBootstrap
            ClusterNode clusterNode = new ClusterNode(this.config.getNodeName(), this.config.getVirtualNodesNum(), this.config.getHost(), this.config.getPort());
            serviceConfig = new ServiceConfig(this.config.getSystemName(), this.config.getZkAddress(), clusterNode, null);
            serviceBootstrap = new AbstractServiceBootstrap() {
                @Override
                protected void start() {

                }

                @Override
                protected void stop() {

                }
            };
            serviceBootstrap.startup(serviceConfig);

            // init sender
            sender = ClusterRouterFactory.getClusterRouter().getActorSystem().actorOf(FOO_METHOD_NAME);
            log.info("RpcSampler.testStarted() done.");
        } catch (Exception ex) {
            this.isTestStartedError = true;
            log.error("RpcSampler.testStarted() error!", ex);
        }
    }

    @Override
    public SampleResult sample() {
        SampleResult result = new SampleResult();
        result.setSampleLabel(sampleLabel);
        if (this.isTestStartedError) {
            return this.sampleFailedByTestStartedError();
        }

        try {
            int i = sampleIndex.getAndIncrement();
            Foo foo = Foo.builder().id(String.valueOf(i)).name(sampleLabel).age(i).timestamp(System.currentTimeMillis()).build();

            result.sampleStart();
            RoutableBean bean = RoutableBeanFactory.buildKeyRouteBean(String.valueOf(i), FOO_SERVER_METHOD_NAME, foo);
            ClusterRouterFactory.getClusterRouter().routeMessage(bean, sender);
            result.sampleEnd();

            result.setSuccessful(true);
            result.setResponseCode("200");
            result.setResponseMessage("OK");
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
