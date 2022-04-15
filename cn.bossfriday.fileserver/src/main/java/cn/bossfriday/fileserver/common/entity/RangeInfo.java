package cn.bossfriday.fileserver.common.entity;

import lombok.Data;

@Data
public class RangeInfo {
    private int begin;
    private int end;

    public RangeInfo() {

    }

    public RangeInfo(String range) {
        // todo: range解析
    }
}
