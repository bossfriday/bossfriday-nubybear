package cn.bossfriday.fileserver.test;

import cn.bossfriday.common.combo.Combo2;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
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

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cn.bossfriday.fileserver.common.FileServerConst.HEADER_FILE_TOTAL_SIZE;
import static cn.bossfriday.fileserver.common.FileServerConst.HEADER_FILE_TRANSACTION_ID;

@Slf4j
public class FileUploadTest {

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 1; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
//                        normalUpload();
//                        download();
                        base64Upload();
//                        rangeUpload();
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
            httpGet = new HttpGet("http://127.0.0.1:18086/resource/v1/3wqd6rCzGrRzN3kq22fiVhH9MRQidss378QyfEKez81YtrrENah9X8ANBEv3");
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
     * normalUpload
     *
     * @throws Exception
     */
    private static void normalUpload() throws Exception {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        CloseableHttpResponse httpResponse = null;
        File file = new File("files/UploadTest中文123.pdf");
        try {
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost("http://127.0.0.1:18086/full/v1/normal");
            httpPost.addHeader(HEADER_FILE_TOTAL_SIZE, String.valueOf(file.length()));
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("upfile", file, ContentType.create("application/x-zip-compressed"), URLEncoder.encode(file.getName(), "UTF-8"));
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);

            // execute
            httpResponse = httpClient.execute(httpPost);
            System.out.println(EntityUtils.toString(httpResponse.getEntity()));
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

    /**
     * rangeUpload
     *
     * @throws Exception
     */
    private static void rangeUpload() throws Exception {
        File localFile = new File("files/UploadTest中文123.pdf");
        int chunkSize = 128 * 1024;
        int fileTotalSize = (int) localFile.length();
        int chunkCount = fileTotalSize % chunkSize == 0 ? (fileTotalSize / chunkSize) : (fileTotalSize / chunkSize + 1);
        String fileTransactionId = UUID.randomUUID().toString();

        // 测试一下httpClient连接复用情况下fileServer处理是否符合逾期（N个断点上传请求复用一个httpClient）
        CloseableHttpClient httpClient = HttpClients.createDefault();
        for (int i = 0; i < chunkCount; i++) {
            int beginOffset = i * chunkSize;
            int endOffset = (i + 1) * chunkSize - 1;
            if (endOffset > fileTotalSize) {
                endOffset = fileTotalSize - 1;
            }

            String range = "bytes=" + beginOffset + "-" + endOffset;
            int rangeLength = endOffset - beginOffset + 1;
            byte[] rangeData = new byte[rangeLength];
            readFile(localFile, beginOffset, rangeData);

            HttpPost httpPost = null;
            CloseableHttpResponse httpResponse = null;
            try {
                httpPost = new HttpPost("http://127.0.0.1:18086/range/v1/normal");
                httpPost.addHeader(HttpHeaderNames.CONNECTION.toString(), "Keep-Alive");
                httpPost.addHeader(HttpHeaderNames.RANGE.toString(), range);
                httpPost.addHeader(HEADER_FILE_TRANSACTION_ID, fileTransactionId);
                httpPost.addHeader(HEADER_FILE_TOTAL_SIZE, String.valueOf(fileTotalSize));
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                builder.addBinaryBody("upfile", rangeData, ContentType.create("application/x-zip-compressed"), URLEncoder.encode(localFile.getName(), "UTF-8"));
                HttpEntity entity = builder.build();
                httpPost.setEntity(entity);
                httpResponse = httpClient.execute(httpPost);
                HttpEntity respEntity = httpResponse.getEntity();
                if (respEntity == null) {
                    String responseRangeHeaderValue = httpResponse.getHeaders(HttpHeaderNames.CONTENT_RANGE.toString())[0].getValue();
                    System.out.println(httpResponse.getStatusLine().getStatusCode() + ":" + responseRangeHeaderValue);
                } else {
                    System.out.println(EntityUtils.toString(respEntity));
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
            }
        }

        try {
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * base64Upload
     *
     * @throws Exception
     */
    public static void base64Upload() throws Exception {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        CloseableHttpResponse httpResponse = null;
        Combo2<Integer, String> base64Combo = getBase64Combo();
        String base64String = base64Combo.getV2();
        try {
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost("http://127.0.0.1:18086/base64/v1/normal?ext=jpg");
            httpPost.addHeader(HttpHeaderNames.CONNECTION.toString(), "Keep-Alive");
            httpPost.addHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain; charset=UTF-8");
            httpPost.setEntity(new StringEntity(base64String, StandardCharsets.UTF_8));
            httpResponse = httpClient.execute(httpPost);
            System.out.println(EntityUtils.toString(httpResponse.getEntity()));
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

    /**
     * getBase64Combo
     *
     * @return
     * @throws Exception
     */
    private static Combo2<Integer, String> getBase64Combo() throws Exception {
        File file = new File("files/Base64UploadTest.jpg");
        try (FileInputStream in = new FileInputStream(file)) {
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);

            return new Combo2<>(size, Base64.encodeBase64String(buffer));
        }
    }

    /**
     * byte2file
     *
     * @param file
     * @param data
     */
    public static void byte2file(File file, byte[] data) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * readFile
     *
     * @param file
     * @param offset
     * @param targetBytes
     * @throws Exception
     */
    public static void readFile(File file, long offset, byte[] targetBytes) throws Exception {
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(file, "r");
            raf.seek(offset);
            raf.readFully(targetBytes);
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
