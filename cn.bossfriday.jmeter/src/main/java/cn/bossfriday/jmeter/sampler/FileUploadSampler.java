package cn.bossfriday.jmeter.sampler;

import cn.bossfriday.jmeter.NubyBearSamplerBuilder;
import cn.bossfriday.jmeter.common.Const;
import cn.bossfriday.jmeter.common.HttpApiHelper;
import cn.bossfriday.jmeter.common.SamplerConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.jmeter.samplers.SampleResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NubyBearSamplerBuilder.SamplerType(behaviorName = Const.GUI_BEHAVIOR_FILE_UPLOAD)
public class FileUploadSampler extends BaseSampler {

    private static final String uploadResultFilePath = "./out-upload-result.csv";
    private static final String uploadResponseReg = "\\{\"rc_url\":(.*?)\\{\"path\":\"(.*?)\",\"type\":(.*?)$";

    private static AtomicLong sampleIndex;
    private static BufferedWriter bw;
    private static File localFile;
    private static String fileApiUri;

    public FileUploadSampler(SamplerConfig config) {
        super(config);
    }

    @Override
    public void testStarted() {
        try {
            sampleIndex = new AtomicLong(0);
            File outFile = new File(this.uploadResultFilePath);
            if (outFile.exists()) {
                outFile.delete();
            }

            FileOutputStream fos = new FileOutputStream(outFile);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            outFile.createNewFile();

            localFile = new File(config.getLoalFileName());
            if (!localFile.exists())
                throw new Exception("Local file not existed! " + localFile.getAbsolutePath());

            String serverRoot = config.getFileServerRoot();
            if (!serverRoot.endsWith("/")) {
                serverRoot += "/";
            }

            fileApiUri = HttpApiHelper.getUrl(serverRoot, "upload/normal/full/v1/", false);
        } catch (Exception ex) {
            isTestStartedError = true;
            log.error("FileUploadSampler.testStarted() error!", ex);
        }
    }

    @Override
    public SampleResult sample() {
        SampleResult result = new SampleResult();
        result.setSampleLabel(this.sampleLabel);
        if (isTestStartedError)
            return sampleFailedByTestStartedError();

        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        CloseableHttpResponse httpResponse = null;
        try {
            long currentSampleIndex = sampleIndex.incrementAndGet();
            httpClient = HttpApiHelper.getHttpClient(false);
            httpPost = new HttpPost(fileApiUri);
            httpPost.setConfig(httpRequestConfig);

            httpPost.addHeader("X-File-Total-Size", String.valueOf(localFile.length()));
            httpPost.addHeader("Connection", "Keep-Alive");
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("upfile", localFile, ContentType.create("application/x-zip-compressed"), URLEncoder.encode(localFile.getName(), "UTF-8"));
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);

            result.sampleStart();
            httpResponse = httpClient.execute(httpPost);
            result.sampleEnd();

            String statusCode = String.valueOf(httpResponse.getStatusLine().getStatusCode());
            if (!statusCode.equals("200")) {
                result.setSuccessful(false);
                result.setResponseCode(statusCode);
                result.setResponseMessage(statusCode);
                log.error("upload failed!(statusCode=" + statusCode + ")");

                return result;
            }

            result.setSuccessful(true);
            result.setResponseCode("200");
            result.setResponseMessage("OK");

            String downloadUrl = "";
            HttpEntity respEntity = httpResponse.getEntity();
            if (respEntity != null) {
                String responseBody = EntityUtils.toString(httpResponse.getEntity());
                if (!StringUtils.isEmpty(responseBody)) {
                    downloadUrl = getDownloadUrl(responseBody);
                }
            }

            String line = config.getLoalFileName() + "," + statusCode + "," + String.valueOf(result.getTime()) + "," + downloadUrl + "," + result.getStartTime();
            writeOutFile(bw, line);
            log.info(currentSampleIndex + "," + line);
        } catch (Exception ex) {
            BaseSampler.setSampleResult("500", "Exception:" + ex.getMessage(), false, result);
            log.error("FileUploadSampler.sample() error!", ex);
        } finally {
            if (httpPost != null)
                httpPost.releaseConnection();

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
        try {
            bw.close();
        } catch (Exception ex) {
            log.warn("bw close error!(" + ex.getMessage() + ")");
        }

        log.info("FileUploadSampler.testEnded()");
    }

    /**
     * getDownloadUrl
     *
     * @param responseBody
     * @return
     */
    private static String getDownloadUrl(String responseBody) {
        if (!Pattern.matches(uploadResponseReg, responseBody)) {
            return "";
        }
        Pattern pattern = Pattern.compile(uploadResponseReg);
        Matcher matcher = pattern.matcher(responseBody);

        String path = "";
        while (matcher.find()) {
            path = matcher.group(2);
        }

        return path;
    }

    /**
     * writeOutFile
     *
     * @param bw
     * @param line
     */
    private static void writeOutFile(BufferedWriter bw, String line) {
        try {
            bw.write(line + "\r\n");
            bw.flush();
        } catch (Exception e) {
            log.error("writeOutFile error!", e);
        }
    }
}
