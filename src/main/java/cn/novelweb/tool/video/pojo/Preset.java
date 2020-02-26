package cn.novelweb.tool.video.pojo;

/**
 * <p>权衡 视频质量 和 编码速度 的枚举值</p>
 * <p>ultrafast(终极快),superfast(超级快),veryfast(非常快),faster(很快),</p>
 * <p>fast(快),medium(中等),slow(慢),slower(很慢),veryslow(非常慢)</p>
 * <p>ultrafast(终极快)提供最少的压缩（低编码器CPU）和最大的视频流大小</p>
 * <p>veryslow(非常慢)提供最佳的压缩（高编码器CPU）的同时降低视频流的大小</p>
 * <p>2020-02-21 15:58</p>
 *
 * @author Dai Yuanchuan
 **/
public enum Preset {

    /**
     * 终极快
     */
    ultrafast,

    /**
     * 超级快
     */
    superfast,

    /**
     * 非常快
     */
    veryfast,

    /**
     * 很快
     */
    faster,

    /**
     * 快
     */
    fast,

    /**
     * 中等
     */
    medium,

    /**
     * 慢
     */
    slow,

    /**
     * 很慢
     */
    slower,

    /**
     * 非常慢
     */
    veryslow

}
