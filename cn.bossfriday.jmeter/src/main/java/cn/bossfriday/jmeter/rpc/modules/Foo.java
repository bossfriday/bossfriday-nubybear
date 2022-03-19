package cn.bossfriday.jmeter.rpc.modules;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Foo {
    private String id;

    private String name;

    private Integer age;

    private Long timestamp;

}
