package cn.bossfriday.jmeter.common;

import lombok.Getter;
import lombok.Setter;

public class SamplerConfig {
    @Getter
    @Setter
    private String systemName;

    @Getter
    @Setter
    private String nodeName;

    @Getter
    @Setter
    private String zkAddress;

    @Getter
    @Setter
    private String host;

    @Getter
    @Setter
    private int port;

    @Getter
    @Setter
    private int virtualNodesNum;

    @Getter
    @Setter
    private String fileServerRoot;

    @Getter
    @Setter
    private String loalFileName;

    @Getter
    @Setter
    private String behaviorName;
}
