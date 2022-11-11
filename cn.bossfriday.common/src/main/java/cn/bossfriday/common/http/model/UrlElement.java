package cn.bossfriday.common.http.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * UrlElement
 *
 * @author chenx
 */
@Getter
@AllArgsConstructor
public class UrlElement {

    /**
     * type
     */
    private UrlElementType type;

    /**
     * name
     */
    private String name;
}
