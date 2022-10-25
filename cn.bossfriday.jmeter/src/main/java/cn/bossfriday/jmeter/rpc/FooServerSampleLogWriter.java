package cn.bossfriday.jmeter.rpc;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.*;

public class FooServerSampleLogWriter {

    private static final Logger log = LoggingManager.getLoggerForClass();
    private static BufferedWriter bw;

    private FooServerSampleLogWriter() {
        
    }

    static {
        try {
            File file = new File("fooServerSampleLog.log");
            if (file.exists()) {
                file.delete();
            }

            FileOutputStream fos = new FileOutputStream(file);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            file.createNewFile();
            FooServerSampleLogWriter.writeSampleResultLog(bw, "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,Latency,IdleTime,Connect");
        } catch (Exception ex) {
            log.error("FooServerSampleLogWriter init error!", ex);
        }

    }

    /**
     * writeSampleResultLog
     *
     * @param bw
     * @param line
     * @throws Exception
     */
    public static void writeSampleResultLog(BufferedWriter bw, String line) throws IOException {
        bw.write(line + "\r\n");
        bw.flush();
    }

    public static void writeSampleResultLog(long timeStamp,
                                            long elapsed,
                                            String label,
                                            String responseCode,
                                            String responseMessage,
                                            String threadName,
                                            boolean success) throws IOException {
        String line = getLineContent(timeStamp, elapsed, label, responseCode, responseMessage, threadName, success);
        bw.write(line + "\r\n");
        bw.flush();
    }

    /**
     * getLineContent
     *
     * @param timeStamp
     * @param elapsed
     * @param label
     * @param responseCode
     * @param responseMessage
     * @param threadName
     * @param success
     * @return
     */
    public static String getLineContent(long timeStamp,
                                        long elapsed,
                                        String label,
                                        String responseCode,
                                        String responseMessage,
                                        String threadName,
                                        boolean success
    ) {
        return timeStamp + "," + elapsed + "," + label + "," + responseCode + "," + responseMessage + "," + threadName + ",," + String.valueOf(success).toUpperCase()
                + ",,0,0,1,1,0,0,0";

    }
}
