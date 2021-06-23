package cn.novelweb.video.format.callback;

import java.awt.image.BufferedImage;
import java.util.List;

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
     * @param image  当前图片流信息
     * @param images 当前任务的图片流信息集合
     * @param index  当前图片流下标
     * @param count  当前任务需要截取的帧总数量
     */
    void frameDrawingInfo(BufferedImage image, List<BufferedImage> images, int index, int count);

}
