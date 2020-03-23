package cn.novelweb.tool.upload.fastdfs.constant;

/**
 * <p>FastDFS协议未分类的常量</p>
 * <p>2020-02-03 15:24</p>
 *
 * @author LiZW
 **/
public class OtherConstants {

    /**
     * for overwrite all old metadata
     */
    public static final byte STORAGE_SET_METADATA_FLAG_OVERWRITE = 'O';

    /**
     * for replace, insert when the meta item not exist, otherwise update it
     */
    public static final byte STORAGE_SET_METADATA_FLAG_MERGE = 'M';
    public static final int DFS_PROTO_PKG_LEN_SIZE = 8;
    public static final int DFS_PROTO_CMD_SIZE = 1;
    public static final int DFS_PROTO_CONNECTION_LEN = 4;
    public static final int DFS_GROUP_NAME_MAX_LEN = 16;
    public static final int DFS_IP_ADDR_SIZE = 16;
    public static final int DFS_DOMAIN_NAME_MAX_SIZE = 128;
    public static final int DFS_VERSION_SIZE = 6;
    public static final int DFS_STORAGE_ID_MAX_SIZE = 16;

    public static final String DFS_RECORD_SEPARATOR = "\u0001";
    public static final String DFS_FIELD_SEPARATOR = "\u0002";

    public static final int TRACKER_QUERY_STORAGE_FETCH_BODY_LEN = DFS_GROUP_NAME_MAX_LEN + DFS_IP_ADDR_SIZE - 1 + DFS_PROTO_PKG_LEN_SIZE;
    public static final int TRACKER_QUERY_STORAGE_STORE_BODY_LEN = DFS_GROUP_NAME_MAX_LEN + DFS_IP_ADDR_SIZE + DFS_PROTO_PKG_LEN_SIZE;

    /**
     * 报文头中命令位置
     */
    public static final int PROTO_HEADER_CMD_INDEX = DFS_PROTO_PKG_LEN_SIZE;

    /**
     * 报文头中状态码位置
     */
    public static final int PROTO_HEADER_STATUS_INDEX = DFS_PROTO_PKG_LEN_SIZE + 1;

    public static final byte DFS_FILE_EXT_NAME_MAX_LEN = 6;
    public static final byte DFS_FILE_PREFIX_MAX_LEN = 16;
    private static final byte DFS_FILE_PATH_LEN = 10;
    private static final byte DFS_FILENAME_BASE64_LENGTH = 27;
    private static final byte DFS_TRUNK_FILE_INFO_LEN = 16;

    private static final long INFINITE_FILE_SIZE = 256 * 1024L * 1024 * 1024 * 1024 * 1024L;
    public static final long APPENDER_FILE_SIZE = INFINITE_FILE_SIZE;
    public static final long TRUNK_FILE_MARK_SIZE = 512 * 1024L * 1024 * 1024 * 1024 * 1024L;
    private static final long NORMAL_LOGIC_FILENAME_LENGTH = DFS_FILE_PATH_LEN + DFS_FILENAME_BASE64_LENGTH + DFS_FILE_EXT_NAME_MAX_LEN + 1;
    public static final long TRUNK_LOGIC_FILENAME_LENGTH = NORMAL_LOGIC_FILENAME_LENGTH + DFS_TRUNK_FILE_INFO_LEN;

}
