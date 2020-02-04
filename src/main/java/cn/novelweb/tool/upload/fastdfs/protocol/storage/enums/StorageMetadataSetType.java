package cn.novelweb.tool.upload.fastdfs.protocol.storage.enums;

/**
 * <p>元数据设置方式</p>
 * <p>2020-02-03 16:51</p>
 *
 * @author LiZW
 **/
public enum StorageMetadataSetType {

    /**
     * 覆盖
     */
    STORAGE_SET_METADATA_FLAG_OVERWRITE('O'),

    /**
     * 没有的条目增加，有则条目覆盖
     */
    STORAGE_SET_METADATA_FLAG_MERGE('M');

    private byte type;

    StorageMetadataSetType(char type) {
        this.type = (byte) type;
    }

    public byte getType() {
        return type;
    }

}
