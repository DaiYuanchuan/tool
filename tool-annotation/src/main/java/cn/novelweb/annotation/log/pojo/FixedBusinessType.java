package cn.novelweb.annotation.log.pojo;

/**
 * <p>定义一些常用的业务类型</p>
 * <p>这个类不允许实例化</p>
 * <p>2019-12-05 20:02</p>
 *
 * @author Dai Yuanchuan
 **/
public class FixedBusinessType {

    private FixedBusinessType() {
    }

    public static final String OTHER = "其他";
    public static final String INSERT = "新增";
    public static final String DELETE = "删除";
    public static final String UPDATE = "更新";
    public static final String SELECT = "查找";
    public static final String GRANT = "授权";
    public static final String REVOKE = "撤消";
    public static final String EXPORT = "导出";
    public static final String IMPORT = "导入";
    public static final String FORCE = "强制退出";
    public static final String AUTO_GENERATE = "自动生成";
    public static final String CLEAN = "清空数据";
    public static final String UPLOAD = "上传数据";
    public static final String SEARCH = "搜索";
    public static final String SETTING = "设置";

}
