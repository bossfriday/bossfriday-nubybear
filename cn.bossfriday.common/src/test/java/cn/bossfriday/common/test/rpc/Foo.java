package cn.bossfriday.common.test.rpc;

import cn.bossfriday.common.utils.GsonUtil;
import cn.bossfriday.common.utils.ProtostuffCodecUtil;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
@Builder
public class Foo {
    private String id;

    private String name;

    private Integer age;

    private String desc;

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 100; i++) {
            final int index = i;
            threadPool.execute(() -> {
                Foo foo = Foo.builder().id(String.valueOf(index)).name("foo").age(100).desc("Foo is a fuck oriented object!").build();
                byte[] data = ProtostuffCodecUtil.serialize(foo);
                Foo result = ProtostuffCodecUtil.deserialize(data, Foo.class);
                System.out.println(result.toString());
                System.out.println(GsonUtil.toJson(foo));
            });
        }
    }
}
