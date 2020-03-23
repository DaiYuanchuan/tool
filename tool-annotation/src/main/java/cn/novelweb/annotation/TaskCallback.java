package cn.novelweb.annotation;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * <p>反射执行任务</p>
 * <p>通过反射
 * 异步执行所有实现了回调接口的实现类中的方法</p>
 * <p>需要传入 对应的接口类，参数</p>
 * <p>2019-12-05 13:33</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class TaskCallback {

    /**
     * 记录扫描到的包
     */
    private static Set<Class<?>> packageName;

    /**
     * 异步执行回调接口的实现类
     *
     * @param superClass 需要实现的接口的类
     * @param args       参数
     */
    public static void callback(Class<?> superClass, Object... args) {
        // 如果扫描到的包是空的
        if (packageName == null || packageName.isEmpty()) {
            // 扫描指定包路径下所有指定类或接口的子类或实现类
            packageName = ClassUtil.scanPackageBySuper("", superClass);
        }
        if (packageName.isEmpty()) {
            log.info("If you want to get processing results, implement the {} interface", superClass.getName());
            return;
        }
        for (Object name : packageName) {
            // 异步执行加载类、加载回调方法等等
            ThreadUtil.execAsync(() -> {
                String pk = name.toString().substring(name.toString().indexOf(" ") + 1);
                if (StrUtil.isBlank(pk)) {
                    log.error("Failed to get class name");
                    return;
                }
                // 加载这个类
                final Class<?> clazz = ClassLoaderUtil.loadClass(pk);
                if (null == clazz) {
                    log.error("Load class with name of [{}] fail !", pk);
                    return;
                }

                // 尝试遍历并调用此类的所有的构造方法，直到构造成功并返回
                Object obj = ReflectUtil.newInstanceIfPossible(clazz);

                // 加载回调方法
                ReflectUtil.invoke(obj, "complete", args);
            });
        }
    }
}
