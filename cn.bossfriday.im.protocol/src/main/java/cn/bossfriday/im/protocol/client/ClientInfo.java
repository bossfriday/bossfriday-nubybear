package cn.bossfriday.im.protocol.client;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.GsonUtil;
import cn.bossfriday.im.protocol.enums.ClientType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * ClientInfo
 *
 * @author chenx
 */
public class ClientInfo {

    @Getter
    @Setter
    private ClientType clientType;

    @Getter
    @Setter
    private String deviceId;

    @Getter
    @Setter
    private String sdkVersion;

    public ClientInfo() {

    }

    public ClientInfo(ClientType clientType, String deviceId, String sdkVersion) {
        this.clientType = clientType;
        this.deviceId = deviceId;
        this.sdkVersion = sdkVersion;
    }

    public ClientInfo(String clientType, String deviceId, String sdkVersion) {
        this.clientType = ClientType.getClientType(clientType);
        this.deviceId = deviceId;
        this.sdkVersion = sdkVersion;
    }

    /**
     * toWill
     *
     * @return
     */
    public static String toWill(ClientInfo clientInfo) {
        if (Objects.isNull(clientInfo)) {
            throw new ServiceRuntimeException("clientInfo is null!");
        }

        if (Objects.isNull(clientInfo.getClientType())) {
            throw new ServiceRuntimeException("clientInfo.clientType is null!");
        }

        if (StringUtils.isEmpty(clientInfo.getDeviceId())) {
            throw new ServiceRuntimeException("clientInfo.deviceInfo is empty!");
        }

        if (StringUtils.isEmpty(clientInfo.getSdkVersion())) {
            throw new ServiceRuntimeException("clientInfo.sdkVersion is empty!");
        }

        return String.format("%s$$%s$$%s", clientInfo.getClientType().getPlatform(), clientInfo.getDeviceId(), clientInfo.getSdkVersion());
    }

    /**
     * fromWill
     *
     * @param will
     * @return
     */
    public static ClientInfo fromWill(String will) {
        if (StringUtils.isEmpty(will)) {
            return null;
        }

        String[] arr = will.split("\\$\\$");
        if (arr.length != 3) {
            return null;
        }

        String clientType = arr[0];
        String deviceInfo = arr[1];
        String sdkVersion = arr[2];

        return new ClientInfo(clientType, deviceInfo, sdkVersion);
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
