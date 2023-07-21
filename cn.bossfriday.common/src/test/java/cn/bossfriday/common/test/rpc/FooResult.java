package cn.bossfriday.common.test.rpc;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FooResult {
    private Foo request;

    private int code;

    private String msg;
}
