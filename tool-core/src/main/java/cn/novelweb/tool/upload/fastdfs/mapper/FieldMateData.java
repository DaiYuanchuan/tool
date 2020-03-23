package cn.novelweb.tool.upload.fastdfs.mapper;

import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.exception.FastDfsColumnMapException;
import cn.novelweb.tool.upload.fastdfs.model.MateData;
import cn.novelweb.tool.upload.fastdfs.utils.BytesUtil;
import cn.novelweb.tool.upload.fastdfs.utils.MetadataMapperUtils;
import cn.novelweb.tool.upload.fastdfs.utils.ReflectionsUtils;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.sql.Date;
import java.util.Set;

/**
 * <p>属性映射MateData定义</p>
 * <p>2016/11/20 1:48</p>
 *
 * @author LiZW
 **/
public class FieldMateData {

    /**
     * 列
     */
    private Field field;

    /**
     * 列索引
     */
    private int index;

    /**
     * 单元最大长度
     */
    private int max;

    /**
     * 单元长度
     */
    private int size;

    /**
     * 列偏移量
     */
    private int offSize;

    /**
     * 动态属性类型
     */
    private DynamicFieldType dynamicFieldType;

    /**
     * 构造函数
     */
    FieldMateData(Field mateField, int offSize) {
        FastDfsColumn column = mateField.getAnnotation(FastDfsColumn.class);
        this.field = mateField;
        this.index = column.index();
        this.max = column.max();
        this.size = getFieldSize(field);
        this.offSize = offSize;
        this.dynamicFieldType = column.dynamicField();
        // 如果强制设置了最大值，以最大值为准
        if (this.max > 0 && this.size > this.max) {
            this.size = this.max;
        }
    }

    /**
     * 获取Field大小
     */
    private int getFieldSize(Field field) {
        if (String.class == field.getType()) {
            return this.max;
        } else if (long.class == field.getType()) {
            return OtherConstants.DFS_PROTO_PKG_LEN_SIZE;
        } else if (int.class == field.getType()) {
            return OtherConstants.DFS_PROTO_PKG_LEN_SIZE;
        } else if (java.util.Date.class == field.getType()) {
            return OtherConstants.DFS_PROTO_PKG_LEN_SIZE;
        } else if (byte.class == field.getType()) {
            return 1;
        } else if (boolean.class == field.getType()) {
            return 1;
        } else if (Set.class == field.getType()) {
            return 0;
        }
        throw new FastDfsColumnMapException(field.getName() + "获取Field大小时未识别的FastDFSColumn类型" + field.getType());
    }

    /**
     * 获取值
     */
    public Object getValue(byte[] bs, Charset charset) {
        if (String.class == field.getType()) {
            if (isDynamicField()) {
                return (new String(bs, offSize, bs.length - offSize, charset)).trim();
            }
            return (new String(bs, offSize, size, charset)).trim();
        } else if (long.class == field.getType()) {
            return BytesUtil.buff2long(bs, offSize);
        } else if (int.class == field.getType()) {
            return (int) BytesUtil.buff2long(bs, offSize);
        } else if (java.util.Date.class == field.getType()) {
            return new Date(BytesUtil.buff2long(bs, offSize) * 1000);
        } else if (byte.class == field.getType()) {
            return bs[offSize];
        } else if (boolean.class == field.getType()) {
            return bs[offSize] != 0;
        }
        throw new FastDfsColumnMapException(field.getName() + "获取值时未识别的FdfsColumn类型" + field.getType());
    }

    /**
     * 获取真实属性
     */
    int getRealSize() {
        // 如果是动态属性
        if (isDynamicField()) {
            return 0;
        }
        return size;
    }

    /**
     * 将属性值转换为byte
     */
    public byte[] toByte(Object bean, Charset charset) {
        Object value = this.getFieldValue(bean);
        if (isDynamicField()) {
            return getDynamicFieldByteValue(value, charset);
        } else if (String.class.equals(field.getType())) {
            // 如果是动态属性
            return BytesUtil.objString2Byte((String) value, max, charset);
        } else if (long.class.equals(field.getType())) {
            return BytesUtil.long2buff((Long) value);
        } else if (int.class.equals(field.getType())) {
            return BytesUtil.long2buff((Integer) value);
        } else if (Date.class.equals(field.getType())) {
            throw new FastDfsColumnMapException("Date 还不支持");
        } else if (byte.class.equals(field.getType())) {
            byte[] result = new byte[1];
            result[0] = (Byte) value;
            return result;
        } else if (boolean.class.equals(field.getType())) {
            throw new FastDfsColumnMapException("boolean 还不支持");
        }
        throw new FastDfsColumnMapException("将属性值转换为byte时未识别的FdfsColumn类型" + field.getName());
    }

    /**
     * 获取动态属性值
     */
    @SuppressWarnings("unchecked")
    private byte[] getDynamicFieldByteValue(Object value, Charset charset) {
        switch (dynamicFieldType) {
            case allRestByte:
                // 如果是打包剩余的所有Byte
                return objString2Byte(value, charset);
            case mateData:
                // 如果是文件mateData
                return MetadataMapperUtils.toByte((Set<MateData>) value, charset);
            default:
                return BytesUtil.objString2Byte((String) value, charset);
        }
    }

    private byte[] objString2Byte(Object value, Charset charset) {
        return BytesUtil.objString2Byte((String) value, charset);
    }

    /**
     * 获取单元对应值
     */
    private Object getFieldValue(Object bean) {
        return ReflectionsUtils.getFieldValue(bean, field.getName());
    }

    /**
     * 获取动态属性长度
     */
    @SuppressWarnings("unchecked")
    int getDynamicFieldByteSize(Object bean, Charset charset) {
        Object value = ReflectionsUtils.getFieldValue(bean, field.getName());
        if (null == value) {
            return 0;
        }
        switch (dynamicFieldType) {
            // 如果是打包剩余的所有Byte
            case allRestByte:
                return ((String) value).getBytes(charset).length;
            // 如果是文件mateData
            case mateData:
                return MetadataMapperUtils.toByte((Set<MateData>) value, charset).length;
            default:
                return getFieldSize(field);
        }
    }

    /**
     * 是否动态属性
     */
    public boolean isDynamicField() {
        return (!DynamicFieldType.NULL.equals(dynamicFieldType));
    }

    public String getFieldName() {
        return field.getName();
    }

    public Field getField() {
        return field;
    }

    public int getIndex() {
        return index;
    }

    public int getMax() {
        return max;
    }

    public int getSize() {
        return size;
    }

    public int getOffSize() {
        return offSize;
    }

    public DynamicFieldType getDynamicFieldType() {
        return dynamicFieldType;
    }

    @Override
    public String toString() {
        return "FieldMateData{" +
                "field=" + getFieldName() +
                ", index=" + index +
                ", max=" + max +
                ", size=" + size +
                ", offSize=" + offSize +
                ", dynamicFieldType=" + dynamicFieldType +
                '}';
    }
}
