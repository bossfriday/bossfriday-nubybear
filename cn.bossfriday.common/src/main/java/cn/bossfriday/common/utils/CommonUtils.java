package cn.bossfriday.common.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * CommonUtils
 *
 * @author chenx
 */
public class CommonUtils {

    private CommonUtils() {
        // do nothing
    }

    /**
     * printSeparatedLog
     *
     * @param logger
     * @param info
     */
    public static void printSeparatedLog(Logger logger, String info) {
        if (Objects.isNull(logger)) {
            return;
        }

        String separator = getSeparator(info);
        logger.info(separator);
        logger.info(info);
        logger.info(separator);
    }

    /**
     * getSeparator
     */
    private static String getSeparator(String info) {
        if (StringUtils.isEmpty(info)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < info.length(); i++) {
            sb.append("=");
        }

        return sb.toString();
    }
}
