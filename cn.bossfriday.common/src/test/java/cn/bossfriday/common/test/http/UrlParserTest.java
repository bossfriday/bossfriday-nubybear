package cn.bossfriday.common.test.http;

import cn.bossfriday.common.http.UrlParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * UrlParserTest
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class UrlParserTest {
    @Before
    public void mockInit() {

    }

    @Test
    public void isMatchTest1() throws URISyntaxException {
        String version = "v1";
        String applicationId = "abc1234";
        String tmp = "/official/" + version + "/" + applicationId + "/media/Upload";
        UrlParser parser = new UrlParser("/official/{version}/{applicationId}/Media/upload");
        URI uri = new URI(tmp);
        boolean isMatch = parser.isMatch(uri);
        Assert.assertTrue(isMatch);

        Map<String, String> argsMap = parser.parsePath(uri);
        System.out.println(argsMap.size());
        Assert.assertEquals(argsMap.get("version"), version);
        Assert.assertEquals(argsMap.get("applicationId"), applicationId);
    }

    @Test
    public void isMatchTest2() throws URISyntaxException {
        String tmp = "/official/Media/upload";
        UrlParser parser = new UrlParser("/official/media/upload");
        URI uri = new URI(tmp);
        boolean isMatch = parser.isMatch(uri);
        Assert.assertTrue(isMatch);
    }
}
