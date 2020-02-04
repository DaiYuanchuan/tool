package cn.novelweb.tool.upload.fastdfs.mapper;

import java.lang.annotation.*;

/**
 * <p>传输参数定义标签</p>
 * <p>2016/11/20 1:35</p>
 *
 * @author LiZW
 **/
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FastDfsColumn {

    /**
     * 映射顺序(从0开始)
     */
    int index() default 0;

    /**
     * String最长度
     */
    int max() default 0;

    /**
     * 动态属性
     */
    DynamicFieldType dynamicField() default DynamicFieldType.NULL;

}
