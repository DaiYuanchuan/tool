package cn.novelweb.tool.upload.fastdfs.model;

import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.exception.FastDfsUnsupportedStorePathException;
import cn.novelweb.tool.upload.fastdfs.mapper.DynamicFieldType;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;
import org.apache.commons.lang.Validate;

import java.io.Serializable;

/**
 * <p>存储文件的路径信息</p>
 * <p>2016/11/20 11:28</p>
 *
 * @author LiZW
 **/
public class StorePath implements Serializable {

    /**
     * 解析路径 分隔符
     */
    private static final String SPLIT_GROUP_NAME_AND_FILENAME_SEPARATOR = "/";

    /**
     * group
     */
    private static final String SPLIT_GROUP_NAME = "group";

    @FastDfsColumn(index = 0, max = OtherConstants.DFS_GROUP_NAME_MAX_LEN)
    private String group;

    @FastDfsColumn(index = 1, dynamicField = DynamicFieldType.allRestByte)
    private String path;

    /**
     * 存储文件路径
     */
    public StorePath() {
    }

    /**
     * 存储文件路径
     *
     * @param group group名称
     * @param path  路径
     */
    public StorePath(String group, String path) {
        this.group = group;
        this.path = path;
    }


    /**
     * 获取文件全路径
     */
    public String getFullPath() {
        return this.group.concat(SPLIT_GROUP_NAME_AND_FILENAME_SEPARATOR).concat(this.path);
    }

    /**
     * 从Url当中解析存储路径对象
     *
     * @param filePath 有效的路径样式为(group/path) 或者 (http://ip/group/path),路径地址必须包含group
     */
    public static StorePath parseFromUrl(String filePath) {
        Validate.notNull(filePath, "解析文件路径不能为空");
        // 获取group起始位置
        int groupStartPos = getGroupStartPos(filePath);
        String groupAndPath = filePath.substring(groupStartPos);
        int pos = groupAndPath.indexOf(SPLIT_GROUP_NAME_AND_FILENAME_SEPARATOR);
        if ((pos <= 0) || (pos == groupAndPath.length() - 1)) {
            throw new FastDfsUnsupportedStorePathException("解析文件路径错误,有效的路径样式为(group/path) 而当前解析路径为".concat(filePath));
        }
        String group = groupAndPath.substring(0, pos);
        String path = groupAndPath.substring(pos + 1);
        return new StorePath(group, path);
    }

    /**
     * 获得group起始位置
     */
    private static int getGroupStartPos(String filePath) {
        int pos = filePath.indexOf(SPLIT_GROUP_NAME);
        if ((pos == -1)) {
            throw new FastDfsUnsupportedStorePathException("解析文件路径错误,被解析路径url没有group,当前解析路径为".concat(filePath));
        }
        return pos;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "StorePath{" +
                "group='" + group + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

}
