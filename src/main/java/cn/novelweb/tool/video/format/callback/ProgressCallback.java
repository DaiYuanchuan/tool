package cn.novelweb.tool.video.format.callback;

/**
 * <p>转换进度回调函数</p>
 * <p>2020-02-21 14:25</p>
 *
 * @author Dai Yuanchuan
 **/
public interface ProgressCallback {

    /**
     * 转换进度
     * 从0~1
     *
     * @param pro 对应的double类型的进度条
     */
    void progress(double pro);

}
