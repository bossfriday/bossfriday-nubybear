package cn.bossfriday.common.plugin;

import lombok.Getter;
import lombok.Setter;

/**
 * PluginElement
 *
 * @author chenx
 */
public class PluginElement {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String path;

    @Getter
    @Setter
    private String main;
}
