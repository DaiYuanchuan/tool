package cn.novelweb.video.format.callback;

import java.io.InputStream;

/**
 * <p>视频抽取帧绘制图片任务回调接口</p>
 * <p>2021-06-23 14:29</p>
 *
 * @author Dan
 **/
public interface VideoFrameDrawingCallback {

    /**
     * 视频抽取帧绘制图片任务回调接口
     *
     * @param inputStream 当前图片流信息
     * @param index       当前图片流下标
     * @param isEnd       是否完成帧的获取
     */
    void frameDrawingInfo(InputStream inputStream, int index, boolean isEnd);

}
