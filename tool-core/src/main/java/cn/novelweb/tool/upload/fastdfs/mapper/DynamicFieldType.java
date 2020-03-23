package cn.novelweb.tool.upload.fastdfs.mapper;

/**
 * 动态属性类型<br/>
 * 可以为空的属性-不发送该报文<br/>
 * 剩余的所有byte-将该字段全部写入到最后的byte当中<br/>
 * <p>2016/11/20 1:37</p>
 *
 * @author LiZW
 **/
public enum DynamicFieldType {

    /**
     * 非动态属性
     */
    NULL,

    /**
     * 剩余的所有Byte
     */
    allRestByte,

    /**
     * 可空的属性
     */
    nullable,

    /**
     * 文件元数据Set
     */
    mateData

}
