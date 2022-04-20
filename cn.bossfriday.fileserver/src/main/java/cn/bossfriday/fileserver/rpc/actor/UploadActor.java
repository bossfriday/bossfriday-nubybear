package cn.bossfriday.fileserver.rpc.actor;

import cn.bossfriday.common.register.ActorRoute;
import cn.bossfriday.common.rpc.actor.TypedActor;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResult;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.common.FileServerConst.ACTOR_FS_UPLOAD;

@Slf4j
@ActorRoute(methods = ACTOR_FS_UPLOAD)
public class UploadActor extends TypedActor<WriteTmpFileResult> {
    @Override
    public void onMessageReceived(WriteTmpFileResult msg) throws Exception {
        try {
            MetaDataIndex metaDataIndex = StorageEngine.getInstance().upload(msg);
            // todo : tell uploadResult...
        } catch (Exception ex) {

        } finally {
            msg = null;
        }
    }
}
