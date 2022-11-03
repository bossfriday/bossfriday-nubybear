package cn.bossfriday.fileserver.test;

import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cn.bossfriday.fileserver.common.FileServerConst.HEADER_FILE_TOTAL_SIZE;

@Slf4j
public class FileUploadTest {
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 1; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        upload();
//                        download();
//                        base64Upload();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        threadPool.shutdown();
    }

    /**
     * 下载
     * 直接用浏览器就能下载（这里只是为了做些验证）
     *
     * @throws Exception
     */
    private static void download() throws Exception {
        CloseableHttpClient httpClient = null;
        HttpGet httpGet = null;
        CloseableHttpResponse httpResponse = null;
        InputStream in = null;
        //OutputStream out = null;
        try {
            httpClient = HttpClients.createDefault();
            httpGet = new HttpGet("http://127.0.0.1:18086/download/v1/PuHWp76aDrEBkvGrR7PEybqzUgHE9bifzAcozs1MVUvkeCWi8ZDFgF6cSWsN.pdf");
            httpGet.addHeader("Connection", "Keep-Alive");
            httpResponse = httpClient.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                in = entity.getContent();
                while (in.read() > 0) {
                    // 空转（本地不存储文件）
                }
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

            } catch (Exception ex) {
                log.warn("close stream error!(" + ex.getMessage() + ")");
            }

            if (httpGet != null) {
                httpGet.releaseConnection();
            }

            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (Exception e) {
                    log.error("httpResponse close error!", e);
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    log.error("httpClient close error!", e);
                }
            }
        }
    }

    /**
     * 上传
     *
     * @throws Exception
     */
    private static void upload() throws Exception {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        CloseableHttpResponse httpResponse = null;
        File file = new File("D:/tmp/ServerApi开发指南.pdf");
        try {
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost("http://127.0.0.1:18086/full/v1/normal");

            // header
            httpPost.addHeader(HttpHeaderNames.CONNECTION.toString(), "Keep-Alive");
            httpPost.addHeader(HEADER_FILE_TOTAL_SIZE, String.valueOf(file.length()));

            // multipart body
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("upfile", file, ContentType.create("application/x-zip-compressed"), URLEncoder.encode(file.getName(), "UTF-8"));
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);

            // execute
            httpResponse = httpClient.execute(httpPost);
            int respCode = httpResponse.getStatusLine().getStatusCode();
            System.out.println("respCode:" + respCode);
            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            System.out.println(responseBody);
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }

            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (Exception e) {
                    log.error("httpResponse close error!", e);
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    log.error("httpClient close error!", e);
                }
            }
        }

        log.info("done");
    }

    public static void base64Upload() throws Exception {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        CloseableHttpResponse httpResponse = null;
        String base64 = getBase64String();
        try {
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost("http://127.0.0.1:18086/v1/normal/base64?ext=png");
            httpPost.addHeader("Connection", "Keep-Alive");
            httpPost.setEntity(new StringEntity(base64, Charset.forName("UTF-8")));

            long beginTimeMillis = System.currentTimeMillis();
            httpResponse = httpClient.execute(httpPost);
            long endTimeMillis = System.currentTimeMillis();

            int status = httpResponse.getStatusLine().getStatusCode();
            HttpEntity respEntity = httpResponse.getEntity();
            if (respEntity != null) {
                log.info("elapsed " + String.valueOf(endTimeMillis - beginTimeMillis) + " ms, status=" + status + "," + EntityUtils.toString(httpResponse.getEntity()));
            } else {
                log.info("elapsed " + String.valueOf(endTimeMillis - beginTimeMillis) + " ms, status=" + status);
            }
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }

            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (Exception e) {
                    log.error("httpResponse close error!", e);
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    log.error("httpClient close error!", e);
                }
            }
        }
    }

    private static String getBase64String() {
        String str = "";
        File file = new File("d:/tmp/base64.txt");
        try {
            FileInputStream in = new FileInputStream(file);
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            str = new String(buffer, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return str;
    }

}
