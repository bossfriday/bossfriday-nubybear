package cn.bossfriday.jmeter.rpc.modules;

import lombok.Data;

@Data
public class FooResult {
    private int code;

    private String msg;

    private Long time;

    public FooResult() {

    }

    public FooResult(int code, String msg, Long time) {
        this.code = code;
        this.msg = msg;
        this.time = time;
    }
}
