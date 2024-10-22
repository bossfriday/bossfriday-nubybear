package cn.bossfriday.im.api.test.common;

import cn.bossfriday.im.api.common.enums.ApiRequestType;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * ApiRequestTypeTest
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiRequestTypeTest {

    @Before
    public void mockInit() {

    }

    @Test
    public void findTest() throws URISyntaxException {
        URI uri = new URI("/api/v2/user/getToken");
        ApiRequestType apiRequestType = ApiRequestType.find(HttpMethod.POST.name(), uri);
        System.out.println(apiRequestType.name());
        Assert.assertEquals(ApiRequestType.USER_GET_TOKEN, apiRequestType);
    }
}
