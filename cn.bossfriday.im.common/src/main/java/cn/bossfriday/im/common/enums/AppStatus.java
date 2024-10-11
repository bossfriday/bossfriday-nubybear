package cn.bossfriday.im.common.enums;

import java.util.Objects;

/**
 * AppStatus
 *
 * @author chenx
 */
public enum AppStatus {

    /**
     * disabled
     */
    DISABLED(0, false),

    /**
     * OK
     */
    OK(1, true);

    private int code;
    private boolean isValid;

    AppStatus(int code, boolean isValid) {
        this.code = code;
        this.isValid = isValid;
    }

    public int getCode() {
        return this.code;
    }

    public boolean isValid() {
        return this.isValid;
    }

    /**
     * getByCode
     *
     * @param code
     * @return
     */
    public static AppStatus getByCode(int code) {
        for (AppStatus entry : AppStatus.values()) {
            if (entry.getCode() == code) {
                return entry;
            }
        }

        return null;
    }

    /**
     * isAppStatusValid
     *
     * @param code
     * @return
     */
    public static boolean isAppStatusValid(int code) {
        AppStatus appStatus = getByCode(code);
        if (Objects.isNull(appStatus)) {
            return false;
        }

        return appStatus.isValid;
    }
}
