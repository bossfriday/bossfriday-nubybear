package cn.bossfriday.common.utils;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClassLoaderUtil
 *
 * @author chenx
 */
public class ClassLoaderUtil {

    private static final Pattern PATTERN = Pattern.compile("\\d+");

    private ClassLoaderUtil() {

    }

    /**
     * getAllClass
     *
     * @param jarFilePath
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T> List<Class<? extends T>> getAllClass(String jarFilePath, Class<T> type) throws IOException, ClassNotFoundException {
        List<Class<? extends T>> result = new ArrayList<>();
        jarFilePath = String.format("jar:file:%s!/", jarFilePath);
        URL url = new URL(jarFilePath);
        JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName().replace('/', '.').replace("$", ".");
            if (name.lastIndexOf(".class") == name.length() - 6) {
                name = name.substring(0, name.lastIndexOf(".class"));
                String cName = name.substring(name.lastIndexOf(".") + 1);
                Matcher matcher = PATTERN.matcher(cName);
                if (matcher.matches()) {
                    continue;
                }

                Class<?> c = ClassLoaderUtil.class.getClassLoader().loadClass(name);
                if (c != null && type.isAssignableFrom(c)) {
                    result.add((Class<? extends T>) c);
                }
            }
        }

        return result;
    }
}
