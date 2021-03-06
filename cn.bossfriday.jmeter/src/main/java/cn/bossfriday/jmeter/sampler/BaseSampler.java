package cn.bossfriday.jmeter.sampler;

import cn.bossfriday.jmeter.common.SamplerConfig;
import org.apache.http.client.config.RequestConfig;
import org.apache.jmeter.samplers.SampleResult;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public abstract class BaseSampler {
    protected static final Logger log = LoggingManager.getLoggerForClass();
    protected SamplerConfig config;
    public boolean isTestStartedError = false;
    protected static final int sampleTimeoutSecond = 10;
    protected static RequestConfig httpRequestConfig;
    protected static String sampleLabel;

    public BaseSampler(SamplerConfig config) {
        this.config = config;
        sampleLabel = config.getBehaviorName();
        httpRequestConfig = RequestConfig.custom().setConnectTimeout(sampleTimeoutSecond * 1000)
                .setConnectionRequestTimeout(sampleTimeoutSecond * 1000)
                .setSocketTimeout(sampleTimeoutSecond * 1000)
                .build();
    }

    /**
     * testStarted
     */
    public abstract void testStarted();

    /**
     * sample
     */
    public abstract SampleResult sample();

    /**
     * testEnded
     */
    public abstract void testEnded();

    /**
     * sampleFailedByTestStartedError
     */
    public SampleResult sampleFailedByTestStartedError() {
        SampleResult result = new SampleResult();
        result.setSampleLabel(config.getBehaviorName());
        result.sampleStart();
        result.setSuccessful(false);
        result.setResponseCode("999");
        result.setResponseMessage("sampleFailedByTestStartedError");
        result.sampleEnd();

        return result;
    }

    /**
     * setSampleResult
     */
    public static void setSampleResult(String respCode, String respMessage, boolean isSuccess, SampleResult result) {
        if (result == null) {
            result = new SampleResult();
            log.warn("The input SampleResult is null.");
        }

        if (result.getStartTime() == 0L)
            result.sampleStart();

        result.setSuccessful(isSuccess);
        result.setResponseCode(respCode);
        result.setResponseMessage(respMessage);

        if (result.getEndTime() == 0L)
            result.sampleEnd();
    }
}
