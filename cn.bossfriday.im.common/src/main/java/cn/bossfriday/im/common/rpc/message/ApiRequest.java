package cn.bossfriday.im.common.rpc.message;

import cn.bossfriday.common.router.RoutableBean;
import cn.bossfriday.common.router.RoutableBeanFactory;
import cn.bossfriday.common.utils.GsonUtil;
import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.im.common.rpc.ActorContext;
import cn.bossfriday.im.common.rpc.IRpcMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * ApiRequest
 *
 * @author chenx
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiRequest implements IRpcMessage {

    private long appId;

    private String requesterId;

    private String userName;

    private String method;

    private String targetResourceId;

    private Object payload;

    private String logId;

    @Override
    public RoutableBean<Object> getRoutableBean() {
        return RoutableBeanFactory.buildResourceIdRouteBean(this.appId, this.method, this.targetResourceId, this.payload);
    }

    @Override
    public ActorContext getActorContext() {
        return ActorContext.builder()
                .appId(this.appId)
                .method(this.method)
                .targetResourceId(this.targetResourceId)
                .requesterId(this.requesterId)
                .sendTime(System.currentTimeMillis())
                .userId(this.requesterId)
                .userName(this.userName)
                .logId(StringUtils.isEmpty(this.logId) ? UUIDUtil.getShortString() : this.logId)
                .build();
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
