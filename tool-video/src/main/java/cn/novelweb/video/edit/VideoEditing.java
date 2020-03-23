package cn.novelweb.video.edit;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.novelweb.tool.date.DateUtils;
import cn.novelweb.video.command.CommandLineOperations;
import cn.novelweb.video.command.assemble.CommandBuilderFactory;
import cn.novelweb.video.format.callback.ProgressCallback;
import cn.novelweb.video.pojo.CommandTask;
import cn.novelweb.video.pojo.ProgramConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <p>视频编辑</p>
 * <p>此类使用原生 F F M P E G 命令对视频进行各类编辑操作</p>
 * <p>该类需要配合对应系统版本的Fast Forward Moving Picture Experts Group</p>
 * <p>下载编译好的对应的系统版本:https://ffmpeg.zeranoe.com/builds/</p>
 * <p>注:如果你是在windows下操作,需要使用相对路径,在Windows下操作不能携带盘符</p>
 * <p>如文件: D:\\video\\Alitalia\\alitalia.mp4 需要转成 /video/Alitalia/alitalia.mp4</p>
 * <p>同时需要注意FFMPEG执行文件的根目录要与视频文件在同一个盘符下,否则会找不到文件.</p>
 * <p>任何路径中不能存在空格！！！</p>
 * <pre>此类实现对视频的字幕添加、水印添加、视频指定时间截取、
 * 分离视频音频流、每一秒截取一张图片、指定时间帧截图、指定时间将视频帧制作成GIF</pre>
 * <p>2020-02-25 19:34</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class VideoEditing {

    /**
     * 初始化配置类信息
     *
     * @param programConfig 初始化配置
     */
    public static void init(ProgramConfig programConfig) {
        CommandLineOperations.init(programConfig);
    }

    /**
     * 添加视频字幕
     * windows系统文件路径格式如下:
     * 原来是: D:\\video\\Alitalia\\alitalia.mp4
     * 支持的格式为: /video/Alitalia/alitalia.mp4
     *
     * @param subtitles 视频字幕文件路径
     * @param input     需要转换的源视频文件路径
     * @param output    转换后输出的视频文件路径
     * @param callback  任务进度的回调接口
     */
    public static void addSubtitles(String subtitles, String input, String output, ProgressCallback callback) {
        // 生成随机taskId
        String taskId = System.currentTimeMillis() + RandomUtil.randomNumbers(10);
        CommandLineOperations.start(taskId, CommandBuilderFactory.create()
                .add("-i", input)
                .add("-vf", "subtitles=" + subtitles)
                .add("-y")
                .add(output));
        getProgress(taskId, input, callback);
    }

    /**
     * 任意格式的视频转换为h264编码的mp4格式
     *
     * @param input    需要转换的源视频文件路径
     * @param output   转换后输出的视频文件路径
     * @param callback 任务进度的回调接口
     */
    public static void converterToMp4(String input, String output, ProgressCallback callback) {
        // 生成随机taskId
        String taskId = System.currentTimeMillis() + RandomUtil.randomNumbers(10);
        CommandLineOperations.start(taskId, CommandBuilderFactory.create()
                .add("-i", input)
                .add("-vcodec", "h264")
                .add("-y")
                .add(output));
        getProgress(taskId, input, callback);
    }

    /**
     * 添加视频字幕的同时,将视频转码为可用于浏览器播放的mp4格式
     *
     * @param subtitles 视频字幕文件路径
     * @param input     需要转换的源视频文件路径
     * @param output    转换后输出的视频文件路径
     * @param callback  任务进度的回调接口
     */
    public static void addSubtitlesToMp4(String subtitles, String input, String output, ProgressCallback callback) {
        output = StrUtil.appendIfMissing(output, ".mp4", ".mp4");
        // 生成随机taskId
        String taskId = System.currentTimeMillis() + RandomUtil.randomNumbers(10);
        CommandLineOperations.start(taskId, CommandBuilderFactory.create()
                .add("-i", input)
                .add("-vf", "subtitles=" + subtitles)
                .add("-vcodec", "h264")
                .add("-y")
                .add(output));
        getProgress(taskId, input, callback);
    }

    /**
     * 添加视频水印
     *
     * @param watermark 需要添加水印图片
     * @param location  需要添加到的水印位置
     * @param input     需要转换的源视频文件路径
     * @param output    转换后输出的视频文件路径
     * @param callback  任务进度的回调接口
     */
    public static void addWatermark(String watermark, WatermarkLocation location,
                                    String input, String output, ProgressCallback callback) {
        // 生成随机taskId
        String taskId = System.currentTimeMillis() + RandomUtil.randomNumbers(10);
        CommandLineOperations.start(taskId, CommandBuilderFactory.create()
                .add("-i", input)
                .add("-i", watermark)
                .add("-filter_complex", WatermarkLocation.map.get(location))
                .add("-codec copy -y")
                .add(output));
        getProgress(taskId, input, callback);
    }

    /**
     * 剪切视频、视频剪辑、视频剪切
     * 从某时间间隔，剪切一段视频。
     * 如:[startTime=3600,duration=10]
     * 表示从第3600秒开始，向后截取10秒钟的视频
     *
     * @param startTime 开始剪切的时间[单位:秒]
     * @param duration  持续剪切的时间[单位:秒]
     * @param input     需要转换的源视频文件路径
     * @param output    转换后输出的视频文件路径
     * @param callback  任务进度的回调接口
     */
    public static void clipVideo(String startTime, String duration,
                                 String input, String output, ProgressCallback callback) {
        // 生成随机taskId
        String taskId = System.currentTimeMillis() + RandomUtil.randomNumbers(10);
        CommandLineOperations.start(taskId, CommandBuilderFactory.create()
                .add("-i", input)
                .add("-ss", startTime)
                .add("-t", duration)
                .add("-codec copy -y")
                .add(output));
        getProgress(taskId, input, callback);
    }

    /**
     * 视频分离视频流和音频流
     *
     * @param input         需要分离的的源视频文件路径
     * @param videoStream   需要输出的视频流的路径[如果值为null,则不分离视频流]
     * @param audioStream   需要输出的音频流的路径[如果值为null,则不分离音频流]
     * @param videoCallback 视频流分离的任务进度回调接口
     * @param audioCallback 音频流分离的任务进度回调接口
     */
    public static void separation(String input, String videoStream, String audioStream, ProgressCallback videoCallback, ProgressCallback audioCallback) {
        // 分流视频流
        if (StringUtils.isNotBlank(videoStream)) {
            // 生成随机taskId
            String taskId = System.currentTimeMillis() + RandomUtil.randomNumbers(10);
            CommandLineOperations.start(taskId, CommandBuilderFactory.create()
                    .add("-i", input)
                    .add("-vcodec copy -an")
                    .add(videoStream));
            getProgress(taskId, input, videoCallback);
        }
        // 分离音频流
        if (StringUtils.isNotBlank(audioStream)) {
            // 生成随机taskId
            String taskId = System.currentTimeMillis() + RandomUtil.randomNumbers(10);
            CommandLineOperations.start(taskId, CommandBuilderFactory.create()
                    .add("-i", input)
                    .add("-vcodec copy -vn")
                    .add(audioStream));
            getProgress(taskId, input, audioCallback);
        }
    }

    /**
     * 分离源视频中的视频流
     *
     * @param input       需要分离的的源视频文件路径
     * @param videoStream 需要输出的视频流的路径[如果值为null,则不分离视频流]
     * @param callback    任务进度的回调接口
     */
    public static void separationVideoStream(String input, String videoStream, ProgressCallback callback) {
        separation(input, videoStream, null, callback, null);
    }

    /**
     * 分离源视频中的音频流
     *
     * @param input       需要分离的的源视频文件路径
     * @param audioStream 需要输出的音频流的路径[如果值为null,则不分离音频流]
     * @param callback    音频流分离的任务进度回调接口
     */
    public static void separationAudioStream(String input, String audioStream, ProgressCallback callback) {
        separation(input, null, audioStream, null, callback);
    }

    /**
     * 抓取视频的一些帧,存为jpeg图片
     * 每一秒截取一张图片
     * 一分钟的视频将会截取60张图片
     * 60张图片会保存在output参数中
     * 这里的output参数值必须是已存在的文件夹
     *
     * @param input     需要转换的源视频文件路径
     * @param output    转成图片后的输出文件路径[这里是文件夹的路径,而且文件夹必须存在]
     * @param frequency 频率,表示每一秒几帧
     * @param quality   表示存储jpeg的图像质量，一般2是高质量。
     * @param callback  任务进度的回调接口
     */
    public static void grabbingFrameToJpg(String input, String output, double frequency, int quality, ProgressCallback callback) {
        output = StrUtil.appendIfMissing(output, "/", "/");
        // 生成随机taskId
        String taskId = System.currentTimeMillis() + RandomUtil.randomNumbers(10);
        CommandLineOperations.start(taskId, CommandBuilderFactory.create()
                .add("-i", input)
                .add("-r", String.valueOf(frequency))
                .add("-q:v", String.valueOf(quality)).add("-f", "image2")
                .add(output + "%01d.jpeg"));
        getProgress(taskId, input, callback);
    }

    /**
     * 抓取视频的一些帧,存为jpeg图片
     * 指定抓取时间、持续时间
     *
     * @param input     需要转换的源视频文件路径
     * @param output    转成图片后的输出文件路径[这里是文件夹的路径,而且文件夹必须存在]
     * @param startTime 开始时间[如:00:00:20或者20,表示从第20s时间开始]
     * @param duration  持续时间[如:10,表示从startTime开始往下10s,每隔1s就抓frequency]
     * @param frequency 频率,表示每一秒几帧
     * @param quality   表示存储jpeg的图像质量，一般2是高质量。
     * @param callback  任务进度的回调接口
     */
    public static void grabbingFrameToJpg(String input, String output, String startTime, String duration,
                                          double frequency, int quality, ProgressCallback callback) {
        output = StrUtil.appendIfMissing(output, "/", "/");
        // 生成随机taskId
        String taskId = System.currentTimeMillis() + RandomUtil.randomNumbers(10);
        CommandLineOperations.start(taskId, CommandBuilderFactory.create()
                .add("-i", input)
                .add("-ss", startTime)
                .add("-t", duration)
                .add("-r", String.valueOf(frequency))
                .add("-q:v", String.valueOf(quality))
                .add("-f", "image2")
                .add(output + "%01d.jpeg"));
        getProgress(taskId, input, callback);
    }

    /**
     * 抓取视频的一些帧,存为GIF动态图片
     * 指定抓取时间、持续时间
     *
     * @param input           需要转换的源视频文件路径
     * @param output          转成GIF动态图片后的输出文件路径
     * @param resolutionRatio GIF动态图片的分辨率,通过此参数控制git质量和大小[如:640x360]
     * @param startTime       开始时间[如:00:00:20或者20,表示从第20s时间开始]
     * @param duration        持续时间[如:10,表示从startTime开始截取时长为10秒的片段转化为GIF动态图片]
     * @param callback        任务进度的回调接口
     */
    public static void grabbingFrameToGif(String input, String output, String resolutionRatio, String startTime, String duration, ProgressCallback callback) {
        // 生成随机taskId
        String taskId = System.currentTimeMillis() + RandomUtil.randomNumbers(10);
        CommandLineOperations.start(taskId, CommandBuilderFactory.create()
                .add("-i", input)
                .add("-ss", startTime)
                .add("-t", duration)
                .add("-s", resolutionRatio)
                .add(output));
        getProgress(taskId, input, callback);
    }

    /**
     * 删除音轨
     * 指定需要保留的音轨
     *
     * @param input    需要转换的源视频文件路径
     * @param output   转换后的输出文件路径
     * @param needKeep 需要保留第几个音轨[值为1时,指定保留第一个音轨,其他全部删除]
     * @param callback 任务进度回调
     */
    public static void deleteSoundTrack(String input, String output, int needKeep, ProgressCallback callback) {
        // 生成随机taskId
        String taskId = System.currentTimeMillis() + RandomUtil.randomNumbers(10);
        CommandLineOperations.start(taskId, CommandBuilderFactory.create()
                .add("-i", input)
                .add("-map", "0:0")
                .add("-map", "0:" + needKeep)
                .add("-vcodec", "copy")
                .add("-acodec", "copy")
                .add(output));
        getProgress(taskId, input, callback);
    }

    /**
     * 删除音轨
     * 指定需要保留的音轨
     * 删除音轨的同时 将视频文件转换为 H264 编码的 MP4 格式.
     *
     * @param input    需要转换的源视频文件路径
     * @param output   转换后的输出文件路径
     * @param needKeep 需要保留第几个音轨[值为1时,指定保留第一个音轨,其他全部删除]
     * @param callback 任务进度回调
     */
    public static void deleteSoundTrackToMp4(String input, String output, int needKeep, ProgressCallback callback) {
        // 生成随机taskId
        String taskId = System.currentTimeMillis() + RandomUtil.randomNumbers(10);
        CommandLineOperations.start(taskId, CommandBuilderFactory.create()
                .add("-i", input)
                .add("-map", "0:0")
                .add("-map", "0:" + needKeep)
                .add("-vcodec", "h264")
                .add("-vcodec", "copy")
                .add("-acodec", "copy")
                .add("-y")
                .add(output));
        getProgress(taskId, input, callback);
    }

    /**
     * 获取任务进度回调
     *
     * @param taskId   任务id
     * @param input    需要转换的源视频文件路径
     * @param callback 任务进度的回调接口
     */
    private static void getProgress(String taskId, String input, ProgressCallback callback) {
        CommandTask commandTask = CommandLineOperations.get(taskId);
        if (commandTask == null) {
            log.error("任务id: {} 查询失败", taskId);
            return;
        }
        // 视频参数
        FFmpegFrameGrabber fFmpegFrameGrabber = new FFmpegFrameGrabber(input);
        try {
            fFmpegFrameGrabber.start();
            long duration = fFmpegFrameGrabber.getLengthInTime() / 1000;
            fFmpegFrameGrabber.stop();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(commandTask.getProcess().getErrorStream()));
            // 最小百分比值
            double minPercentage = 0.001;
            // 命令行消息
            String message;
            if (callback != null) {
                callback.progress(0.0);
            }
            while ((message = bufferedReader.readLine()) != null) {
                // 当前已完成的时间
                String timeStamp = ReUtil.get("time=.*? ", message, 0);
                if (StringUtils.isNotBlank(timeStamp) && duration > 0) {
                    timeStamp = ReUtil.delAll("time=", timeStamp.trim());
                    double progressCompleted = Convert.convert(double.class, DateUtils.getTimeConversion(timeStamp));
                    double toTalCompleted = Convert.convert(double.class, duration);
                    double resultValue = progressCompleted / toTalCompleted;
                    if (resultValue < minPercentage) {
                        resultValue = 0;
                    }
                    if (callback != null) {
                        callback.progress(resultValue);
                    }
                }
            }
            if (callback != null) {
                callback.progress(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
