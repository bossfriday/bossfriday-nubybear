package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.BizException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * XmlParserUtil
 *
 * @author chenx
 */
public class XmlParserUtil {

    private XmlParserUtil() {

    }

    /**
     * parse
     *
     * @param path
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T parse(String path, Class<T> type) throws IOException, JAXBException {
        InputStream input;
        File file = new File(path);
        if (file.exists()) {
            input = new FileInputStream(file);
        } else {
            input = type.getClassLoader().getResourceAsStream(path);
        }

        return parse(input, type);
    }

    /**
     * parse
     *
     * @param input
     * @param type
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T parse(InputStream input, Class<T> type) throws JAXBException, IOException {
        if (input == null) {
            throw new BizException("input is null!");
        }

        if (type == null) {
            throw new BizException("type is null!");
        }

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(type);
            Unmarshaller um = jaxbContext.createUnmarshaller();

            return (T) um.unmarshal(input);
        } finally {
            input.close();
        }
    }
}
