package cn.novelweb.tool.video.edit;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>添加视频水印时设置水印位置</p>
 * <p>2020-02-26 00:19</p>
 *
 * @author Dai Yuanchuan
 **/
public enum WatermarkLocation {

    /**
     * 左上角
     */
    leftUp,

    /**
     * 右上角
     */
    rightUp,

    /**
     * 左下角
     */
    leftLower,

    /**
     * 右下角
     */
    rightLower,

    /**
     * 中间
     */
    middle;

    public static Map<WatermarkLocation, String> map = new HashMap<>(5);

    static {
        map.put(WatermarkLocation.leftUp, "overlay=10:10");
        map.put(WatermarkLocation.rightUp, "overlay=main_w-overlay_w-10:10");
        map.put(WatermarkLocation.leftLower, "overlay=10:main_h-overlay_h-10");
        map.put(WatermarkLocation.rightLower, "overlay=main_w-overlay_w-10:main_h-overlay_h-10");
        map.put(WatermarkLocation.middle, "overlay=main_w/2-overlay_w/2:main_h/2-overlay_h/2");
    }

}
