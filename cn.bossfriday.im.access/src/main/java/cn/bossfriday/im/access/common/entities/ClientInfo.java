package cn.bossfriday.im.access.common.entities;

import cn.bossfriday.common.utils.GsonUtil;
import cn.bossfriday.im.protocol.enums.ClientType;
import lombok.Getter;
import lombok.Setter;

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
    private String sdkVersion;

    @Getter
    @Setter
    private String apiVersion;

    public ClientInfo() {

    }

    public ClientInfo(String clientType, String sdkVersion, String apiVersion) {
        this.clientType = ClientType.getClientType(clientType);
        this.sdkVersion = sdkVersion;
        this.apiVersion = apiVersion;
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
