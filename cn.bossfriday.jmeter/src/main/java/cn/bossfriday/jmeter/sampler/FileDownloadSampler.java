package cn.bossfriday.jmeter.sampler;

import cn.bossfriday.jmeter.NubyBearSamplerBuilder;
import cn.bossfriday.jmeter.common.Const;
import cn.bossfriday.jmeter.common.HttpApiHelper;
import cn.bossfriday.jmeter.common.SamplerConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jmeter.samplers.SampleResult;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@NubyBearSamplerBuilder.SamplerType(behaviorName = Const.GUI_BEHAVIOR_FILE_DOWNLOAD)
public class FileDownloadSampler extends BaseSampler {

    private static AtomicLong sampleIndex;
    private static String fileApiUri;
    private static List<String> urlList;
    private static int urlListSize;

    public FileDownloadSampler(SamplerConfig config) {
        super(config);
    }

    @Override
    public void testStarted() {
        try {
            sampleIndex = new AtomicLong(0);
            fileApiUri = HttpApiHelper.getUrl(config.getFileServerRoot(), "", false);
            initDownloadUrlList();
            log.info("FileDownloadSampler.testStarted() done, urlListSize=" + urlListSize);
        } catch (Exception ex) {
            isTestStartedError = true;
            log.error("FileDownloadSampler.testStarted() error!", ex);
        }
    }

    @Override
    public SampleResult sample() {
        SampleResult result = new SampleResult();
        result.setSampleLabel(this.sampleLabel);
        if (isTestStartedError)
            return sampleFailedByTestStartedError();

        CloseableHttpClient httpClient = null;
        HttpGet httpGet = null;
        CloseableHttpResponse httpResponse = null;
        InputStream in = null;
        try {
            String downUrl = getDownloadUrl();
            httpClient = HttpApiHelper.getHttpClient(false);
            httpGet = new HttpGet(fileApiUri + downUrl);
            httpGet.setConfig(httpRequestConfig);
            httpGet.addHeader("Connection", "Keep-Alive");

            result.sampleStart();
            httpResponse = httpClient.execute(httpGet);
            result.sampleEnd();

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                result.setSuccessful(false);
                result.setResponseCode(String.valueOf(statusCode));
                result.setResponseMessage("Failed");
                log.error("download failed!(statusCode=" + statusCode + ")");

                return result;
            }

            result.setSuccessful(true);
            result.setResponseCode("200");
            result.setResponseMessage("OK");

            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                in = entity.getContent();
                while (in.read() > 0) {
                    // 空跑（打压端不存储文件）
                }
            }

            log.info("down(" + sampleIndex.get() + ")  elapse " + result.getTime() + ", " + downUrl);
        } catch (Exception ex) {
            BaseSampler.setSampleResult("500", "Exception:" + ex.getMessage(), false, result);
            log.error("FileDownloadSampler.sample() error!", ex);
        } finally {
            try {
                if (in != null)
                    in.close();

            } catch (Exception ex) {
                log.warn("close stream error!(" + ex.getMessage() + ")");
            }

            if (httpGet != null)
                httpGet.releaseConnection();

            if (httpResponse != null)
                try {
                    httpResponse.close();
                } catch (Exception e) {
                    log.error("httpResponse close error!", e);
                }

            if (httpClient != null)
                try {
                    httpClient.close();
                } catch (Exception e) {
                    log.error("httpClient close error!", e);
                }
        }

        return result;
    }

    @Override
    public void testEnded() {

    }

    private void initDownloadUrlList() throws Exception {
        urlList = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("out-upload-result.csv")), "UTF-8"));
            String lineTxt = null;
            while ((lineTxt = br.readLine()) != null) {
                String[] contentArr = lineTxt.split(",");
                if (contentArr.length >= 3) {
                    String url = contentArr[3];
                    if (!StringUtils.isEmpty(url))
                        urlList.add(contentArr[3]);
                }
            }
        } finally {
            urlListSize = urlList.size();
            if (br != null)
                br.close();
        }
    }

    private static synchronized String getDownloadUrl() throws Exception {
        if (urlList == null || urlList.size() == 0)
            throw new Exception("urlList is null or empty!");

        String url = "";
        if (sampleIndex.get() < urlListSize) {
            url = urlList.get((int) sampleIndex.get());
        } else {
            url = urlList.get((int) (sampleIndex.get() % urlListSize));
        }

        sampleIndex.incrementAndGet();

        return url;
    }
}
