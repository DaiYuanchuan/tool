package cn.novelweb.tool.upload.fastdfs.mapper;

import cn.novelweb.tool.upload.fastdfs.exception.FastDfsColumnMapException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>映射对象元数据,映射对象元数据必须由{@code @FastDfsColumn}注解</p>
 * <p>2020-02-03 15:19</p>
 *
 * @author LiZW
 **/
@Slf4j
public class ObjectMateData {

    /**
     * 映射对象类名
     */
    private String className;

    /**
     * 映射列(全部)
     */
    private List<FieldMateData> fieldList;

    /**
     * 动态计算列(部分)fieldList包含dynamicFieldList
     */
    private List<FieldMateData> dynamicFieldList = new ArrayList<>();

    /**
     * FieldsTotalSize
     */
    private int fieldsTotalSize = 0;

    /**
     * 映射对象元数据构造函数
     */
    public <T> ObjectMateData(Class<T> genericType) {
        // 获得对象类名
        this.className = genericType.getName();
        this.fieldList = praseFieldList(genericType);
        // 校验映射定义
        validateFieldListDefine();
    }

    /**
     * 解析映射对象数据映射情况
     */
    private <T> List<FieldMateData> praseFieldList(Class<T> genericType) {
        Field[] fields = genericType.getDeclaredFields();
        List<FieldMateData> mateData = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FastDfsColumn.class)) {
                FieldMateData fieldMateData = new FieldMateData(field, fieldsTotalSize);
                mateData.add(fieldMateData);
                // 计算偏移量
                fieldsTotalSize += fieldMateData.getRealSize();
                // 如果是动态计算列
                if (fieldMateData.isDynamicField()) {
                    dynamicFieldList.add(fieldMateData);
                }
            }
        }
        return mateData;
    }

    /**
     * 检查数据列定义，为了减少编码的错误，检查数据列定义是否存在列名相同或者索引定义相同(多个大于0相同的)的
     */
    private void validateFieldListDefine() {
        for (FieldMateData field : fieldList) {
            validateFieldItemDefineByIndex(field);
        }
    }

    /**
     * 检查按索引映射
     */
    private void validateFieldItemDefineByIndex(FieldMateData field) {
        for (FieldMateData fieldMateData : fieldList) {
            if (!field.equals(fieldMateData) && (field.getIndex() == fieldMateData.getIndex())) {
                Object[] param = {className, field.getFieldName(), fieldMateData.getFieldName(), field.getIndex()};
                log.warn("在类{}映射定义中{}与{}索引定义相同为{}(请检查是否为程序错误)", param);
            }
        }
    }

    public String getClassName() {
        return className;
    }

    public List<FieldMateData> getFieldList() {
        return Collections.unmodifiableList(fieldList);
    }

    /**
     * 是否有动态数据列
     */
    private boolean hasDynamicField() {
        for (FieldMateData field : fieldList) {
            if (field.isDynamicField()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取动态数据列长度
     */
    private int getDynamicFieldSize(Object obj, Charset charset) {
        int size = 0;
        for (FieldMateData field : dynamicFieldList) {
            size = size + field.getDynamicFieldByteSize(obj, charset);
        }
        return size;
    }

    /**
     * 获取固定参数对象总长度
     */
    public int getFieldsFixTotalSize() {
        if (hasDynamicField()) {
            throw new FastDfsColumnMapException(className + "类中有Dynamic字段, 不支持操作getFieldsTotalSize");
        }
        return fieldsTotalSize;
    }

    /**
     * 获取需要发送的报文长度
     */
    public int getFieldsSendTotalByteSize(Object bean, Charset charset) {
        if (!hasDynamicField()) {
            return fieldsTotalSize;
        } else {
            int dynamicFieldSize = getDynamicFieldSize(bean, charset);
            return fieldsTotalSize + dynamicFieldSize;
        }
    }

    /**
     * 导出调试信息
     */
    public void dumpObjectMateData() {
        if (log.isTraceEnabled()) {
            log.trace("#----------------------------------------------------------------------------------------------------------------------------------");
            log.trace("# dump class={}", className);
            log.trace("#----------------------------------------------------------------------------------------------------------------------------------");
            for (FieldMateData md : fieldList) {
                log.trace(md.toString());
            }
        }
    }
}
