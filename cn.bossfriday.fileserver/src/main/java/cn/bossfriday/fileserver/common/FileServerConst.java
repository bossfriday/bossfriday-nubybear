package cn.bossfriday.fileserver.common;

public class FileServerConst {
    /**
     * chunk
     */
    public static final int CHUNK_SIZE = 64 * 1024;

    /**
     * storage engine version
     */
    public static final int STORAGE_ENGINE_VERSION_0 = 0;

    /**
     * URL
     */
    public static final String URL_UPLOAD_FULL = "full";
    public static final String URL_UPLOAD_BASE64 = "base64";
    public static final String URL_UPLOAD_RANGE = "range";

    /**
     * actors
     */
    public static final String ACTOR_FS_TRACKER = "fs_tracker";
    public static final String ACTOR_FS_TMP_FILE = "fs_tmpFile";
    public static final String ACTOR_FS_UPLOAD = "fs_upload";

    /**
     * file
     */
    public static final String FILE_PATH_TMP = "tmp";
    public static final String FILE_UPLOADING_TMP_FILE_EXT ="ing";
    public static final String FILE_DEFAULT_EXT ="octetstream";

    /**
     * http header
     */
    public static final String HEADER_FILE_TOTAL_SIZE = "X-File-Total-Size";
}
