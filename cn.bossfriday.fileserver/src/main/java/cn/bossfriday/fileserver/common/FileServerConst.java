package cn.bossfriday.fileserver.common;

public class FileServerConst {
    /**
     * storage
     */
    public static final int DEFAULT_STORAGE_ENGINE_VERSION = 1;
    public static final int MAX_STORAGE_VERSION = 255;
    public static final int DOWNLOAD_CHUNK_SIZE = 32 * 1024;
    public static final String STORAGE_FILE_EXTENSION_NAME = "data";

    /**
     * URL
     */
    public static final String URL_UPLOAD_FULL = "full";
    public static final String URL_UPLOAD_BASE64 = "base64";
    public static final String URL_UPLOAD_RANGE = "range";
    public static final String URL_DOWNLOAD = "download";

    /**
     * actors
     */
    public static final String ACTOR_FS_TRACKER = "fs_tracker";
    public static final String ACTOR_FS_TMP_FILE = "fs_tmpFile";
    public static final String ACTOR_FS_DEL_TMP_FILE = "fs_delTmpFile";
    public static final String ACTOR_FS_UPLOAD = "fs_up";
    public static final String ACTOR_FS_DOWNLOAD = "fs_down";

    /**
     * file
     */
    public static final String FILE_PATH_TMP = "tmp";
    public static final String FILE_UPLOADING_TMP_FILE_EXT = "ing";
    public static final String FILE_DEFAULT_EXT = "octetstream";

    /**
     * http header
     */
    public static final String HEADER_FILE_TOTAL_SIZE = "X-File-Total-Size";
}
