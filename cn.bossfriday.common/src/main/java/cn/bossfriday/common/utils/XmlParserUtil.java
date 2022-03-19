package cn.bossfriday.common.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class XmlParserUtil {
    /**
     * parse
     *
     * @param path
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T parse(String path, Class<T> type) throws Exception {
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
    public static <T> T parse(InputStream input, Class<T> type) throws Exception {
        if (input == null)
            throw new Exception("input is null!");

        if (type == null)
            throw new Exception("type is null!");

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(type);
            Unmarshaller um = jaxbContext.createUnmarshaller();

            return (T) um.unmarshal(input);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    // ignore this exception
                }
            }
        }
    }
}
