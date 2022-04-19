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
    public static final String ACTOR_HTTP_FILE_SERVER = "httpFileServ";
    public static final String ACTOR_WRITE_TMP_FILE = "wTmpFile";

    /**
     * file
     */
    public static final String FILE_PATH_TMP = "tmp";
    public static final String FILE_UPLOADING_TMP_FILE_EXT ="ing";

    /**
     * http header
     */
    public static final String HEADER_FILE_TOTAL_SIZE = "X-File-Total-Size";
}
