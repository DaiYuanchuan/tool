package cn.novelweb.tool.video.format;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUnit;
import cn.novelweb.tool.video.format.callback.ProgressCallback;
import cn.novelweb.tool.video.pojo.Preset;
import cn.novelweb.tool.video.pojo.VideoParameters;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;

import java.io.*;

/**
 * <p>视频格式转换</p>
 * <p>2020-02-20 19:05</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class FormatConversion {

    private static boolean isInit = false;

    /**
     * 初始化获取视频文件的各种参数
     *
     * @param filePath 文件路径
     * @return 视频参数 VideoParameters 实体类
     */
    public static VideoParameters init(String filePath) {
        File file = new File(filePath);
        return init(file);
    }

    /**
     * 获取视频文件各种参数
     *
     * @param file 视频文件
     * @return 视频参数 VideoParameters 实体类
     */
    public static VideoParameters init(File file) {
        if (!file.isFile()) {
            log.error("它不是一个标准的文件");
            return null;
        }
        FFmpegFrameGrabber fFmpegFrameGrabber = new FFmpegFrameGrabber(file);
        try {
            fFmpegFrameGrabber.start();
            VideoParameters videoParameters = VideoParameters.builder()
                    .width(fFmpegFrameGrabber.getImageWidth())
                    .height(fFmpegFrameGrabber.getImageHeight())
                    .videoCodec(fFmpegFrameGrabber.getVideoCodec())
                    .audioCodec(fFmpegFrameGrabber.getAudioCodec())
                    .format(fFmpegFrameGrabber.getFormat())
                    .sampleRate(fFmpegFrameGrabber.getSampleRate())
                    .frameRate(fFmpegFrameGrabber.getFrameRate())
                    .videoBitrate(fFmpegFrameGrabber.getVideoBitrate())
                    .audioBitrate(fFmpegFrameGrabber.getAudioBitrate())
                    .pixelFormat(fFmpegFrameGrabber.getPixelFormat())
                    .audioChannels(fFmpegFrameGrabber.getAudioChannels())
                    .videoLengthTime(fFmpegFrameGrabber.getLengthInTime() / (1000 * 1000))
                    .lengthInFrames(fFmpegFrameGrabber.getLengthInFrames())
                    .lengthInAudioFrames(fFmpegFrameGrabber.getLengthInAudioFrames())
                    .videoStream(fFmpegFrameGrabber.getVideoStream())
                    .videoOptions(fFmpegFrameGrabber.getVideoOptions())
                    .videoCodecName(fFmpegFrameGrabber.getVideoCodecName())
                    .metadata(fFmpegFrameGrabber.getMetadata())
                    .timeout(fFmpegFrameGrabber.getTimeout())
                    .sensorPattern(fFmpegFrameGrabber.getSensorPattern())
                    .sampleMode(fFmpegFrameGrabber.getSampleMode())
                    .aspectRatio(fFmpegFrameGrabber.getAspectRatio())
                    .numBuffers(fFmpegFrameGrabber.getNumBuffers())
                    .maxDelay(fFmpegFrameGrabber.getMaxDelay())
                    .imageScalingFlags(fFmpegFrameGrabber.getImageScalingFlags())
                    .gamma(fFmpegFrameGrabber.getGamma())
                    .formatContext(fFmpegFrameGrabber.getFormatContext())
                    .bitsPerPixel(fFmpegFrameGrabber.getBitsPerPixel())
                    .timestamp(fFmpegFrameGrabber.getTimestamp())
                    .audioStream(fFmpegFrameGrabber.getAudioStream())
                    .gopSize((int) fFmpegFrameGrabber.getFrameRate() * 2)
                    .videoQuality(-1)
                    .audioQuality(-1)
                    .preset(Preset.slow)
                    .build();
            fFmpegFrameGrabber.stop();
            isInit = true;
            return videoParameters;
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 视频转码、任意格式转换
     *
     * @param input      需要转换的视频文件
     * @param output     转换完成后保存到的文件位置
     * @param parameters 转换参数设置
     * @param callback   任务进度的回调接口
     */
    public static void converter(File input, File output, VideoParameters parameters,
                                 ProgressCallback callback) {
        if (!input.isFile()) {
            log.error("它不是一个标准的文件");
            return;
        }

        if (output.isFile()) {
            log.error("需要输出的文件已存在");
            return;
        }

        if (!isInit) {
            log.error("请先调用FormatConversion.init()方法初始化参数");
            return;
        }
        try {
            // 获取视频源信息
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
            grabber.start();
            // 流媒体输出地址,分辨率(长,高)
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output, parameters.getWidth(), parameters.getHeight());
            recorder.setVideoCodec(parameters.getVideoCodec());
            recorder.setAudioCodec(parameters.getAudioCodec());
            recorder.setFormat(parameters.getFormat());
            recorder.setSampleRate(parameters.getSampleRate());
            recorder.setFrameRate(parameters.getFrameRate());
            recorder.setVideoBitrate(parameters.getVideoBitrate());
            recorder.setAudioChannels(parameters.getAudioChannels());
            recorder.setGopSize(parameters.getGopSize());
            recorder.setVideoQuality(parameters.getVideoQuality());
            recorder.setAudioQuality(parameters.getAudioQuality());
            recorder.setVideoOption("preset", parameters.getPreset().toString());
            if (parameters.getAudioBitrate() <= 0) {
                recorder.setAudioBitrate(192000);
            } else {
                recorder.setAudioBitrate(parameters.getAudioBitrate());
            }
            recorder.start();
            // 下一秒的时间戳
            long nextSecondTimestamp = System.currentTimeMillis() + DateUnit.SECOND.getMillis();
            // 最小百分比值
            double minPercentage = 0.001;
            Frame capturedFrame;
            while ((capturedFrame = grabber.grabFrame()) != null) {
                // 如果当前的时间戳 >= 下一秒的时间戳
                if (System.currentTimeMillis() >= nextSecondTimestamp) {
                    // 每秒计算完成进度
                    double timestamp = Convert.convert(double.class, grabber.getTimestamp());
                    double lengthInTime = Convert.convert(double.class, grabber.getLengthInTime());
                    double resultValue = timestamp / lengthInTime;
                    if (resultValue < minPercentage) {
                        resultValue = 0;
                    }
                    if (callback != null) {
                        callback.progress(resultValue);
                    }
                    nextSecondTimestamp = System.currentTimeMillis() + DateUnit.SECOND.getMillis();
                }
                recorder.setTimestamp(grabber.getTimestamp());
                recorder.record(capturedFrame);
                capturedFrame.clone();
            }
            grabber.stop();
            grabber.release();
            grabber.close();
            recorder.stop();
            recorder.release();
            recorder.close();
            if (callback != null) {
                callback.progress(1);
            }
            System.gc();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            log.error("获取视频源异常:" + e.getMessage());
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
            log.error("构建流媒体输出地址异常:" + e.getMessage());
        }
    }

    /**
     * 任意格式的视频转换为mp4
     *
     * @param inputPath  需要转换的源视频路径
     * @param outputPath 转换后输出的视频路径
     * @param callback   任务进度的回调接口
     */
    public static void converterToMp4(String inputPath, String outputPath, ProgressCallback callback) {
        converterToMp4(new File(inputPath), new File(outputPath), callback);
    }

    /**
     * 任意格式的视频转换为mp4
     *
     * @param input    需要转换的源视频文件
     * @param output   转换后输出的视频文件
     * @param callback 任务进度的回调接口
     */
    public static void converterToMp4(File input, File output, ProgressCallback callback) {
        // 定义初始化参数
        VideoParameters parameters = FormatConversion.init(input);
        if (parameters == null) {
            return;
        }
        parameters.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        parameters.setFormat("mp4");
        parameters.setAudioCodec(86018);
        converter(input, output, parameters, callback);
    }
}
