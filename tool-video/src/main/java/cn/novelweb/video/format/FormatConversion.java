package cn.novelweb.video.format;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.novelweb.video.format.callback.ProgressCallback;
import cn.novelweb.video.format.callback.VideoFrameDrawingCallback;
import cn.novelweb.video.pojo.Preset;
import cn.novelweb.video.pojo.VideoParameters;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>视频格式转换</p>
 * <p>2020-02-20 19:05</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class FormatConversion {

    static {
        // 设置日志级别
        avutil.av_log_set_level(avutil.AV_LOG_ERROR);
    }

    /**
     * 获取视频文件的各种参数
     *
     * @param filePath 文件路径
     * @return 视频参数 VideoParameters 实体类
     */
    public static VideoParameters getVideoParameters(String filePath) {
        File file = new File(filePath);
        return getVideoParameters(file);
    }

    /**
     * 获取视频文件各种参数
     *
     * @param file 视频文件
     * @return 视频参数 VideoParameters 实体类
     */
    public static VideoParameters getVideoParameters(File file) {
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
     * 任意格式的视频转换为h264编码的mp4格式
     * 部分情况下可能会转换失败
     * 转换失败时请尝试使用 VideoEditing.converterToMp4()
     *
     * @param inputPath  需要转换的源视频路径
     * @param outputPath 转换后输出的视频路径
     * @param callback   任务进度的回调接口
     */
    public static void converterToMp4(String inputPath, String outputPath, ProgressCallback callback) {
        converterToMp4(new File(inputPath), new File(outputPath), callback);
    }

    /**
     * 任意格式的视频转换为h264编码的mp4格式
     * 部分情况下可能会转换失败
     * 转换失败时请尝试使用 VideoEditing.converterToMp4()
     *
     * @param input    需要转换的源视频文件
     * @param output   转换后输出的视频文件
     * @param callback 任务进度的回调接口
     */
    public static void converterToMp4(File input, File output, ProgressCallback callback) {
        // 定义初始化参数
        VideoParameters parameters = FormatConversion.getVideoParameters(input);
        if (parameters == null) {
            return;
        }
        parameters.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        parameters.setFormat("mp4");
        parameters.setAudioCodec(86018);
        converter(input, output, parameters, callback);
    }

    // ==================================================== 视频取帧 ====================================================

    /**
     * 异步视频指定间隔帧采样
     * 指定抽取帧的间隔数 ，对视频进行抽帧，将抽取出来的帧转为图片文件，保存至参数output
     *
     * @param input    需要进行抽帧的源视频文件
     * @param output   帧转为图片文件后需要保存到的图片路径
     * @param interval 抽帧间隔，默认值25，最大不能超过当前视频总帧数
     */
    public static void asyncVideoSamplingIntervalFrame(String input, String output, Integer interval) {
        // 图片输出路径
        File imagesOutputPath = FileUtil.mkdir(output);
        File inputFile = new File(input);
        asyncVideoSamplingIntervalFrame(inputFile, interval, (image, images, index, count) -> {
            try {
                ImageIO.write(image, "png", FileUtil.file(imagesOutputPath, String.format("%d.png", index)));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    /**
     * 异步视频指定间隔帧采样
     * 指定抽取帧的间隔数 ，对视频进行抽帧，将抽取出来的帧转为图片文件，保存至参数output
     *
     * @param input    需要进行抽帧的源视频文件
     * @param interval 抽帧间隔，默认值25，最大不能超过当前视频总帧数
     * @param callback 任务信息回调接口
     */
    public static void asyncVideoSamplingIntervalFrame(File input, Integer interval, VideoFrameDrawingCallback callback) {
        if (!input.exists()) {
            log.error("file {} does not exist", input.getName());
            return;
        }

        if (callback == null) {
            log.error("callback interface is null");
            return;
        }

        ThreadUtil.execAsync(() -> videoSamplingIntervalFrame(input, interval, callback));
    }

    /**
     * 异步视频指定帧采样
     * 指定需要进行采样的帧，对视频进行抽帧，将抽取的帧转为图片文件，保存至参数output
     *
     * @param input  需要进行抽帧的源视频文件
     * @param output 转换后输出的视频文件
     * @param frame  需要进行抽取的帧数组
     */
    public static void asyncVideoSpecifiedFrameSampling(String input, String output, Integer... frame) {
        // 图片输出路径
        File imagesOutputPath = FileUtil.mkdir(output);
        asyncVideoSpecifiedFrameSampling(new File(input), (image, images, index, count) -> {
            try {
                ImageIO.write(image, "png", FileUtil.file(imagesOutputPath, String.format("%d.png", index)));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }, frame);
    }

    /**
     * 异步视频指定帧采样
     * 指定需要进行采样的帧，对视频进行抽帧，将抽取的帧转为图片文件，保存至参数output
     *
     * @param input    需要进行抽帧的源视频文件
     * @param callback 任务进度的回调接口
     * @param frame    需要进行抽取的帧数组
     */
    public static void asyncVideoSpecifiedFrameSampling(File input, VideoFrameDrawingCallback callback, Integer... frame) {
        if (!input.exists()) {
            log.error("file {} does not exist", input.getName());
            return;
        }

        if (callback == null) {
            log.error("callback interface is null");
            return;
        }

        if (ArrayUtil.isEmpty(frame)) {
            log.error("frame array is empty");
            return;
        }
        ThreadUtil.execAsync(() -> {
            try {
                // 对需要进行抽取的帧数组正序排序后去重
                List<Integer> frameList = Arrays.stream(frame).sorted().distinct().collect(Collectors.toList());
                // FFmpeg帧捕获器
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
                grabber.start();
                videoSpecifiedFrameSampling(grabber, frameList, callback);
            } catch (FrameGrabber.Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    /**
     * 视频中间帧取样
     *
     * @param input         需要进行取样的源视频文件
     * @param floatingValue 浮动值 0 ~ 1，对应着百分比，取到中间帧后 依据此值 进行前后一定比例的浮动
     * @param number        需要进行取样的帧数量
     * @param callback      任务进度的回调接口
     */
    public static void videoInBetweenSampling(File input, double floatingValue, int number, VideoFrameDrawingCallback callback) {
        if (!input.exists()) {
            log.error("file {} does not exist", input.getName());
            return;
        }

        if (callback == null) {
            log.error("callback interface is null");
            return;
        }

        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
            grabber.start();

            // 获取当前视频总帧数
            int totalVideoFrames = grabber.getLengthInFrames();

            // 获取到的中间帧
            int inBetween = totalVideoFrames / 2;

            // 浮动值处于 0 ~ 1 之间
            floatingValue = Math.min(1, Math.max(floatingValue, 0));
            // 浮动值
            int percentage = Convert.convert(int.class, inBetween * floatingValue);

            // 需要进行抽取的帧数组
            List<Integer> frame = new ArrayList<>();
            for (int i = 0; i < number; i++) {
                // 获得指定范围内的随机数
                frame.add(RandomUtil.randomInt(inBetween - percentage, inBetween + percentage));
            }
            // 对需要进行抽取的帧数组正序排序后去重
            videoSpecifiedFrameSampling(grabber, frame.stream().sorted().distinct().collect(Collectors.toList()), callback);
        } catch (FrameGrabber.Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    // ==================================================== private ====================================================

    /**
     * 同步视频指定帧采样
     * 指定需要进行采样的帧，对视频进行抽帧，将抽取出来的帧转为BufferedImage，通过callback回调接口转出
     *
     * @param grabber   FFmpeg帧捕获器
     * @param frameList 需要进行抽取的帧数组
     * @param callback  任务进度的回调接口
     */
    private static void videoSpecifiedFrameSampling(FFmpegFrameGrabber grabber, List<Integer> frameList, VideoFrameDrawingCallback callback) {
        try {
            // 获取数组中最大值
            int maxValue = frameList.stream().max(Integer::compareTo).orElse(5);

            // 图像buffer流信息集合
            List<BufferedImage> bufferedImages = new ArrayList<>();
            // 线程安全的自增
            AtomicInteger atomicInteger = new AtomicInteger(0);

            Frame capturedFrame;
            for (int i = 1; i <= maxValue; i++) {
                // 获取到该帧的图片流
                capturedFrame = grabber.grabImage();
                // 如果当前帧包含在了指定帧内，则进行images绘制
                boolean isContains = frameList.contains(i) && (capturedFrame != null && capturedFrame.image != null);
                if (isContains) {
                    taskCallback(capturedFrame, bufferedImages, callback, atomicInteger, frameList.size());
                }
            }
            // 释放捕获器
            grabber.stop();
            grabber.release();
            grabber.close();
        } catch (FrameGrabber.Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 同步视频指定间隔帧采样
     * 指定抽取帧的间隔数 ，对视频进行抽帧，将抽取出来的帧转为BufferedImage，通过callback回调接口转出
     *
     * @param input    需要进行抽帧的源视频文件
     * @param interval 抽帧间隔，默认值25，最大不能超过当前视频总帧数
     * @param callback 任务信息回调接口
     */
    private static void videoSamplingIntervalFrame(File input, Integer interval, VideoFrameDrawingCallback callback) {
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
            grabber.start();
            // 获取当前视频总帧数
            int totalVideoFrames = grabber.getLengthInFrames();
            // 默认抽取帧数间隔为
            int defaultInterval = 25;
            // 对 interval 间隔帧做校验
            interval = interval == null ? Math.min(totalVideoFrames, defaultInterval) : Math.min(totalVideoFrames, Math.max(interval, 1));
            // 图像buffer流信息集合
            List<BufferedImage> bufferedImages = new ArrayList<>();
            // 线程安全的自增
            AtomicInteger atomicInteger = new AtomicInteger(0);
            // 总次数(一共生成的图片数量)
            int count = totalVideoFrames / interval;
            Frame capturedFrame;
            for (int i = 1; i <= totalVideoFrames; i++) {
                // 获取到该帧的图片流
                capturedFrame = grabber.grabImage();
                // 根据当前抽帧间隔，只获取间隔的倍数
                boolean isMultiple = (i % interval == 0) && (capturedFrame != null && capturedFrame.image != null);
                if (isMultiple) {
                    taskCallback(capturedFrame, bufferedImages, callback, atomicInteger, count);
                }
            }
            // 释放捕获器
            grabber.stop();
            grabber.release();
            grabber.close();
        } catch (FrameGrabber.Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 视频帧截取成功后的任务回调方法
     *
     * @param capturedFrame  当前截取的帧
     * @param bufferedImages 当前截取的帧转为图像buffer流的信息集合
     * @param callback       任务信息回调接口
     * @param atomicInteger  线程安全的自增
     * @param count          总次数(一共生成的图片数量)
     */
    private static void taskCallback(Frame capturedFrame, List<BufferedImage> bufferedImages,
                                     VideoFrameDrawingCallback callback, AtomicInteger atomicInteger, int count) {
        // 绘制图片流信息
        BufferedImage image = Java2DFrameUtils.toBufferedImage(capturedFrame);
        // 绘制图片
        bufferedImages.add(image);
        // 任务回调
        ThreadUtil.execAsync(() -> callback.frameDrawingInfo(image, bufferedImages, atomicInteger.incrementAndGet(), count));
    }
}
