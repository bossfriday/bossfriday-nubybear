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
    private boolean isOk;

    AppStatus(int code, boolean isOk) {
        this.code = code;
        this.isOk = isOk;
    }

    public int getCode() {
        return this.code;
    }

    public boolean isOk() {
        return this.isOk;
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
     * isOkApp
     *
     * @param code
     * @return
     */
    public static boolean isAppOk(int code) {
        AppStatus appStatus = getByCode(code);
        if (Objects.isNull(appStatus)) {
            return false;
        }

        return appStatus.isOk;
    }
}
