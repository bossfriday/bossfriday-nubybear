package cn.bossfriday.common.test.id;

import cn.bossfriday.common.id.SystemIdWorker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * SystemIdWorkerTest
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class SystemIdWorkerTest {

    @Before
    public void mockInit() {

    }

    @Test
    public void test() {
        long id1 = SystemIdWorker.nextId();
        String key = SystemIdWorker.getKey(id1);
        long id2 = SystemIdWorker.getId(key);
        System.out.println("id1=" + id1 + ", id2=" + id2 + ", key=" + key);
        Assert.assertEquals(id2, id1);
    }
}
