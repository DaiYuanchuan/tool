package cn.novelweb.tool.video.pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.javacv.FrameGrabber;

import java.util.Map;

/**
 * <p>需要设置的各种视频参数</p>
 * <p>2020-02-20 22:04</p>
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class VideoParameters {

    @ApiModelProperty(value = "视频的宽度")
    private int width;

    @ApiModelProperty(value = "视频的高度")
    private int height;

    @ApiModelProperty(value = "视频编解码器、视频的压缩方式、视频解码")
    private int videoCodec;

    @ApiModelProperty(value = "音讯编解码器;音频编码格式;音频编解码;")
    private int audioCodec;

    @ApiModelProperty(value = "确定视频格式[如:flv、mp4]")
    private String format;

    @ApiModelProperty(value = "音频采样率")
    private int sampleRate;

    @ApiModelProperty(value = "帧频,帧速率")
    private double frameRate;

    @ApiModelProperty(value = "设定固定视频比特率")
    private int videoBitrate;

    @ApiModelProperty(value = "设置固定的音频比特率")
    private int audioBitrate;

    @ApiModelProperty(value = "设置像素格式")
    private int pixelFormat;

    @ApiModelProperty(value = "设置音频的声道[2(立体声);1(单声道);0(无音频)]")
    private int audioChannels;

    @ApiModelProperty(value = "视频的时长[单位:秒]")
    private long videoLengthTime;

    @ApiModelProperty(value = "框架长度")
    private int lengthInFrames;

    @ApiModelProperty(value = "音频帧长度")
    private int lengthInAudioFrames;

    @ApiModelProperty(value = "视频流")
    private int videoStream;

    @ApiModelProperty(value = "视频选项;显示选项")
    private Map<String, String> videoOptions;

    @ApiModelProperty(value = "视频编解码器名称")
    private String videoCodecName;

    @ApiModelProperty(value = "元数据;元资料")
    private Map<String, String> metadata;

    @ApiModelProperty(value = "超时")
    private int timeout;

    @ApiModelProperty(value = "传感器模式")
    private long sensorPattern;

    @ApiModelProperty(value = "采样模式;取样方式")
    private FrameGrabber.SampleMode sampleMode;

    @ApiModelProperty(value = "屏幕纵横比;屏幕长宽比")
    private double aspectRatio;

    @ApiModelProperty(value = "屏幕纵横比;屏幕长宽比")
    private double numBuffers;

    @ApiModelProperty(value = "最大延迟")
    private int maxDelay;

    @ApiModelProperty(value = "图像缩放标志")
    private int imageScalingFlags;

    @ApiModelProperty(value = "伽马分布")
    private double gamma;

    @ApiModelProperty(value = "格式上下文")
    private AVFormatContext formatContext;

    @ApiModelProperty(value = "每个像素的位数")
    private int bitsPerPixel;

    @ApiModelProperty(value = "时间戳")
    private long timestamp;

    @ApiModelProperty(value = "音频流;声音串流")
    private int audioStream;

    @ApiModelProperty(value = "关键帧间隔,一般与帧率相同或者是视频帧率的两倍")
    private int gopSize;

    @ApiModelProperty(value = "视频质量")
    private int videoQuality = -1;

    @ApiModelProperty(value = "音频的质量")
    private int audioQuality = -1;

    @ApiModelProperty(value = "权衡 视频质量 和 编码速度 的值")
    private Preset preset = Preset.slow;
}
