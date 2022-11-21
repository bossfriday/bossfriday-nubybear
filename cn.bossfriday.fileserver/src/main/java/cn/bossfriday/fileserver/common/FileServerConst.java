package cn.bossfriday.fileserver.common;

/**
 * FileServerConst
 *
 * @author chenx
 */
public class FileServerConst {

    private FileServerConst() {

    }

    /**
     * storage
     */
    public static final int DEFAULT_STORAGE_ENGINE_VERSION = 1;
    public static final long DEFAULT_LRU_DURATION = 1000L * 3600 * 2;
    public static final long STORAGE_FILE_CHANNEL_LRU_DURATION = 1000L * 3600 * 8;
    public static final int MAX_STORAGE_VERSION = 255;
    public static final int DOWNLOAD_CHUNK_SIZE = 32 * 1024;
    public static final String STORAGE_FILE_EXTENSION_NAME = "data";

    /**
     * URL
     */
    public static final String URL_UPLOAD_FULL = "full";
    public static final String URL_UPLOAD_BASE64 = "base64";
    public static final String URL_UPLOAD_RANGE = "range";
    public static final String URL_PREFIX_STORAGE_VERSION = "v";
    public static final String URL_RESOURCE = "resource";

    public static final String URI_ARGS_NAME_STORAGE_NAMESPACE = "storageNamespace";
    public static final String URI_ARGS_NAME_UPLOAD_TYPE = "uploadType";
    public static final String URI_ARGS_NAME_ENGINE_VERSION = "engineVersion";
    public static final String URI_ARGS_NAME_META_DATA_INDEX_STRING = "metaDataIndexString";
    public static final String URI_ARGS_NAME_EXT = "ext";

    /**
     * actors
     */
    public static final String ACTOR_PREFIX_FS = "fs-";
    public static final String ACTOR_FS_TRACKER = ACTOR_PREFIX_FS + "tracker";
    public static final String ACTOR_FS_TMP_FILE = ACTOR_PREFIX_FS + "tmpFile";
    public static final String ACTOR_FS_DEL_TMP_FILE = ACTOR_PREFIX_FS + "delTmpFile";
    public static final String ACTOR_FS_UPLOAD = ACTOR_PREFIX_FS + "upload";
    public static final String ACTOR_FS_DOWNLOAD = ACTOR_PREFIX_FS + "download";
    public static final String ACTOR_FS_DELETE = ACTOR_PREFIX_FS + "delete";

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
    public static final String HEADER_FILE_TRANSACTION_ID = "X-File-TransactionId";

    /**
     * tip message
     */
    public static final String TIP_MSG_INVALID_ENGINE_VERSION = "invalid engine version!";
}
