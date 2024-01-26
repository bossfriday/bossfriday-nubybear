package cn.bossfriday.common.http;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.http.model.Range;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RangeParser
 * 仅支持firstBytePos和lastBytePos必须写全的Range解析
 *
 * @author chenx
 */
public class RangeParser {

    private static final String RANGE_START_WITH = "bytes=";
    private static final String RANGES_SEPARATOR = ",";
    private static final String RANGE_SEPARATOR = "-";
    private static final String INVALID_RANGE_VALUE = "Invalid range value: ";

    private RangeParser() {

    }

    /**
     * parse
     *
     * @param input
     * @return
     */
    public static List<Range> parse(final String input) {
        List<Range> rangeList = new ArrayList<>();

        String inputNormalized = input.toLowerCase().trim();
        if (!inputNormalized.startsWith(RANGE_START_WITH)) {
            throw new ServiceRuntimeException("Range header value must start with bytes=");
        }

        String[] rangesString = inputNormalized.substring(RANGE_START_WITH.length()).split(RANGES_SEPARATOR);
        for (String rangeString : rangesString) {
            if (rangeString.indexOf(RANGE_SEPARATOR) == -1) {
                throw new ServiceRuntimeException(INVALID_RANGE_VALUE + input);
            }

            String[] values = rangeString.split(RANGE_SEPARATOR);
            if (values.length != 2) {
                throw new ServiceRuntimeException(INVALID_RANGE_VALUE + input);
            }

            rangeList.add(buildRange(values));
        }

        if (CollectionUtils.isEmpty(rangeList)) {
            throw new ServiceRuntimeException(INVALID_RANGE_VALUE + input);
        }

        return rangeList;
    }

    /**
     * parseAndGetFirstRange
     *
     * @param input
     * @return
     */
    public static Range parseAndGetFirstRange(final String input) {
        return parse(input).get(0);
    }

    /**
     * buildRange
     *
     * @param values
     * @return
     */
    private static Range buildRange(final String[] values) {
        try {
            return new Range(Long.parseLong(values[0].trim()), Long.parseLong(values[1].trim()));
        } catch (NumberFormatException e) {
            throw new ServiceRuntimeException("Invalid range value, unable to parse numeric values " + e.getMessage());
        }
    }
}
