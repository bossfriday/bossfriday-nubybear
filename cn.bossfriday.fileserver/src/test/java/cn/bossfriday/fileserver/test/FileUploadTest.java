package cn.bossfriday.fileserver.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.net.URLEncoder;

@Slf4j
public class FileUploadTest {
    public static void main(String[] args) throws Exception {
        for(int i=0;i<10;i++) {
            upload();
        }
    }

    private static void upload() throws Exception {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        CloseableHttpResponse httpResponse = null;
        File file = new File("D:/tmp/ServerApi开发指南.pdf");
        try {
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost("http://127.0.0.1:18086/upload/normal/full/v1/");
            httpPost.addHeader("X-File-Total-Size", String.valueOf(file.length()));
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("upfile", file, ContentType.create("application/x-zip-compressed"), URLEncoder.encode(file.getName(), "UTF-8"));
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            httpResponse = httpClient.execute(httpPost);
            int respCode = httpResponse.getStatusLine().getStatusCode();
            System.out.println("respCode:" + respCode);
            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            System.out.println(responseBody);
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

        log.info("done");
    }
}
