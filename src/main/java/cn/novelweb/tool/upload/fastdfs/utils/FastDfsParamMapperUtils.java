package cn.novelweb.tool.upload.fastdfs.utils;

import cn.novelweb.tool.upload.fastdfs.exception.FastDfsColumnMapException;
import cn.novelweb.tool.upload.fastdfs.mapper.FieldMateData;
import cn.novelweb.tool.upload.fastdfs.mapper.ObjectMateData;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Param对象与byte映射器 工具</p>
 * <p>2020-02-03 16:28</p>
 *
 * @author LiZW
 **/
@Slf4j
public class FastDfsParamMapperUtils {

    private FastDfsParamMapperUtils() {
    }

    /**
     * 对象映射缓存
     */
    private static Map<String, ObjectMateData> mapCache = new HashMap<>();

    /**
     * 将byte解码为对象
     */
    public static <T> T map(byte[] content, Class<T> genericType, Charset charset) {
        // 获取映射对象
        ObjectMateData objectMap = getObjectMap(genericType);
        if (log.isDebugEnabled()) {
            objectMap.dumpObjectMateData();
        }
        try {
            return mapByIndex(content, genericType, objectMap, charset);
        } catch (InstantiationException ie) {
            Log.debug("Cannot instantiate: ", ie);
            throw new FastDfsColumnMapException(ie);
        } catch (IllegalAccessException iae) {
            Log.debug("Illegal access: ", iae);
            throw new FastDfsColumnMapException(iae);
        } catch (InvocationTargetException ite) {
            Log.debug("Cannot invoke method: ", ite);
            throw new FastDfsColumnMapException(ite);
        }
    }

    /**
     * 获取对象映射定义
     */
    public static <T> ObjectMateData getObjectMap(Class<T> genericType) {
        if (null == mapCache.get(genericType.getName())) {
            // 还未缓存过
            mapCache.put(genericType.getName(), new ObjectMateData(genericType));
        }
        return mapCache.get(genericType.getName());
    }

    /**
     * 按列顺序映射
     */
    private static <T> T mapByIndex(byte[] content, Class<T> genericType, ObjectMateData objectMap, Charset charset)
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
        List<FieldMateData> mappingFields = objectMap.getFieldList();
        T obj = genericType.newInstance();
        for (FieldMateData field : mappingFields) {
            // 设置属性值
            if (log.isTraceEnabled()) {
                log.trace("设置值是 " + field + field.getValue(content, charset));
            }
            ReflectionsUtils.setFieldValue(obj, field.getFieldName(), field.getValue(content, charset));
        }
        return obj;
    }

    /**
     * 序列化为Byte
     */
    public static byte[] toByte(Object object, Charset charset) {
        ObjectMateData objectMap = getObjectMap(object.getClass());
        try {
            return convertFieldToByte(objectMap, object, charset);
        } catch (NoSuchMethodException ie) {
            Log.debug("Cannot invoke get methed: ", ie);
            throw new FastDfsColumnMapException(ie);
        } catch (IllegalAccessException iae) {
            Log.debug("Illegal access: ", iae);
            throw new FastDfsColumnMapException(iae);
        } catch (InvocationTargetException ite) {
            Log.debug("Cannot invoke method: ", ite);
            throw new FastDfsColumnMapException(ite);
        }
    }

    /**
     * 将属性转换为byte
     */
    private static byte[] convertFieldToByte(ObjectMateData objectMap, Object object, Charset charset)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<FieldMateData> mappingFields = objectMap.getFieldList();
        // 获取报文长度 (固定长度+动态长度)
        int size = objectMap.getFieldsSendTotalByteSize(object, charset);
        byte[] result = new byte[size];
        int offSize = 0;
        for (FieldMateData field : mappingFields) {
            byte[] fieldByte = field.toByte(object, charset);
            if (null != fieldByte) {
                System.arraycopy(fieldByte, 0, result, offSize, fieldByte.length);
                offSize += fieldByte.length;
            }
        }
        return result;
    }

}
