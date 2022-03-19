package cn.bossfriday.jmeter;

import cn.bossfriday.jmeter.common.SamplerConfig;
import cn.bossfriday.jmeter.sampler.BaseSampler;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import static cn.bossfriday.jmeter.common.Const.*;

public class NubyBearSampler extends AbstractSampler implements TestStateListener {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static BaseSampler sampler;
    private static SamplerConfig config;

    public NubyBearSampler() {
        this.setName(SAMPLER_NAME);
    }

    @Override
    public SampleResult sample(Entry entry) {
        try {
            if (sampler.isTestStartedError) {
                return sampler.sampleFailedByTestStartedError();
            }

            return sampler.sample();
        } catch (Exception ex) {
            SampleResult result = new SampleResult();
            BaseSampler.setSampleResult("500", "Exception:" + ex.getMessage(), false, new SampleResult());
            log.error("sample() error!", ex);

            return result;
        }
    }

    @Override
    public void testStarted() {
        try {
            config = new SamplerConfig();
            config.setSystemName(getSystemName());
            config.setNodeName(getNodeName());
            config.setZkAddress(getZkAddress());
            config.setHost(getHost());
            config.setPort(getPort());
            config.setVirtualNodesNum(getVirtualNodesNum());
            config.setBehaviorName(getBehaviorName());

            sampler = NubyBearSamplerBuilder.getSampler(config);
            sampler.testStarted();
        } catch (Exception e) {
            if (sampler != null)
                sampler.isTestStartedError = true;
            log.error("testStarted() error!", e);
        }
    }

    @Override
    public void testStarted(String s) {

    }

    @Override
    public void testEnded() {
        sampler.testEnded();
    }

    @Override
    public void testEnded(String s) {

    }

    public String getSystemName() {
        return this.getPropertyAsString(GUID_SYSTEM_NAME);
    }

    public String getNodeName() {
        return this.getPropertyAsString(GUID_NODE_NAME);
    }

    public String getZkAddress() {
        return this.getPropertyAsString(GUID_ZK_ADDRESS);
    }

    public String getHost() {
        return this.getPropertyAsString(GUID_HOST);
    }

    public Integer getPort() {
        return Integer.parseInt(this.getPropertyAsString(GUID_PORT));
    }

    public Integer getVirtualNodesNum() {
        return Integer.parseInt(this.getPropertyAsString(GUID_VIRTUAL_NODES_NUM));
    }

    public String getBehaviorName() {
        return this.getPropertyAsString(GUI_BEHAVIOR_NAME);
    }
}
