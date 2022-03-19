package cn.bossfriday.jmeter.rpc.modules;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FooResult {
    private Foo request;

    private int code;

    private String msg;
}
