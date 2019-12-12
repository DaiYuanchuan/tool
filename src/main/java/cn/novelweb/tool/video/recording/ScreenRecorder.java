package cn.novelweb.tool.video.recording;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.thread.ThreadUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>屏幕录制程序</p>
 * <p>录屏软件</p>
 * <p>2019-12-10 14:19</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScreenRecorder {

    /**
     * 从系统中获取图像、屏幕分辨率、屏幕色彩模型、全屏的时候获得屏幕大小等等
     */
    private static final Toolkit TOOLKIT = Toolkit.getDefaultToolkit();

    /**
     * 帧记录器
     * 视频类
     */
    private static FFmpegFrameRecorder recorder;

    /**
     * 用于访问许多音频格式和通道配置常量
     */
    private static AudioFormat audioFormat;

    /**
     * 用于生成本机系统输入事件
     * 这里主要是用这个类来创建屏幕截图
     */
    private static Robot robot;

    /**
     * 提供特定于数据行的附加信息
     * 这些信息包括:
     * 数据线支持的音频格式
     * 内部缓冲区的最小和最大尺寸
     */
    private static DataLine.Info dataLineInfo;

    /**
     * 打开并开始捕获音频
     * 对所选音频设备进行更多的控制
     */
    private static TargetDataLine line;

    /**
     * 设置一个用于屏幕录制的线程池
     */
    private static ScheduledThreadPoolExecutor screenThreadPool;

    /**
     * 设置一个用于抓取音频的线程池
     */
    private static ScheduledThreadPoolExecutor audioThreadPool;

    /**
     * 记录暂停时间的参数
     */
    private long pauseTime = 0;

    /**
     * 需要录制的屏幕宽度
     * 默认为当前屏幕宽度
     */
    private int width = TOOLKIT.getScreenSize().width;

    /**
     * 需要录制的屏幕高度
     * 默认为当前屏幕高度
     */
    private int height = TOOLKIT.getScreenSize().height;

    /**
     * 视频编解码器、视频的压缩方式、视频解码
     * 默认id为:12
     */
    private int videoCodec = avcodec.AV_CODEC_ID_MPEG4;

    /**
     * 音讯编解码器;音频编码格式;音频编解码;
     * 默认id为:86018
     */
    private int audioCodec = avcodec.AV_CODEC_ID_AAC;

    /**
     * 确定视频格式
     * 可以是flv、mov、mp4、m4a、3gp、3g2、mj2、h264、ogg
     * 默认是:mp4
     */
    private String format = "mp4";

    /**
     * 音频采样率是指录音设备在一秒钟内对声音信号的采样次数
     * 采样频率越高声音的还原就越真实越自然
     * 在当今的主流采集卡上,采样频率一般共分为11025Hz、22050Hz、24000Hz、44100Hz、48000Hz五个等级
     * 8000Hz一般为电话所用采样率,对于人的说话已经足够
     * 11025Hz一般为AM调幅广播的声音品质
     * 22050Hz和24000Hz一般为FM调频广播所用采样率
     * 32000Hz一般为miniDV、数码视频camcorder、DAT (LP mode)所用采样率
     * 44100Hz一般为音频(CD、VCD、超级VCD、MP3)所用采样率
     * 48000Hz一般为miniDV、数字电视、DVD、DAT、电影和专业音频所用的数字声音所用采样率
     * 这个数值也要看你的声卡可以支持的数值
     * 默认是:48000Hz
     */
    private int sampleRate = 48000;

    /**
     * 帧频,帧速率,是指每秒钟刷新的图片的帧数
     * 也可以理解为图形处理器每秒钟能够刷新几次
     * 对影片内容而言,帧速率指每秒所显示的静止帧格数
     * 要生成平滑连贯的动画效果,帧速率一般不小于8
     * 而电影的帧速率为24fps,但如果你只是录屏5FPS也是可以的
     * 如果太高的话会出现声音和画面对不上的情况
     * 默认是:10FPS
     */
    private int frameRate = 10;

    /**
     * 关键帧间隔,一般与帧率相同或者是视频帧率的两倍
     * 默认是:frameRate * 2
     */
    private int gopSize = this.frameRate * 2;

    /**
     * 视频质量
     * 默认是:0最高质量
     */
    private int videoQuality = 0;

    /**
     * 音频的质量
     * 默认是:0最高质量
     */
    private int audioQuality = 0;

    /**
     * 设定固定视频比特率
     * 2000kb/s,720P视频的合理比特率范围
     * 默认是:2000000
     */
    private int videoBitrate = 2000000;

    /**
     * 设置固定的音频比特率
     * 默认是:192000 192Kbps
     */
    private int audioBitrate = 192000;

    /**
     * 设置内容速率因子,这是一个x264的动态比特率参数
     * 它能够在复杂场景下(使用不同比特率,即可变比特率)保持视频质量
     * 比特率越高视频越清晰,视频体积也会变大,需要根据实际选择合理范围
     * 设置固定位元率系数,可以设置为0
     * 默认是:25
     */
    private String crf = "25";


    /**
     * 权衡 视频质量 和 编码速度 的值
     * 参考值:ultrafast(终极快),superfast(超级快),veryfast(非常快),faster(很快),
     * fast(快),medium(中等),slow(慢),slower(很慢),veryslow(非常慢)
     * ultrafast(终极快)提供最少的压缩（低编码器CPU）和最大的视频流大小
     * veryslow(非常慢)提供最佳的压缩（高编码器CPU）的同时降低视频流的大小
     * 默认是:slow
     */
    private String preset = "slow";

    /**
     * 设置像素格式
     * YUV420一帧的大小size=width×height×1.5 Byte
     * 默认是:0 AV_PIX_FMT_YUV420P
     */
    private int pixelFormat = 0;

    /**
     * 设置音频的声道
     * 可选值为:2(立体声);1(单声道);0(无音频);
     * 默认是:2 双通道(立体声)
     */
    private int audioChannels = 2;

    /**
     * 录取的视频需要保存到的目录
     * 这个是文件夹,文件目录,不是文件
     * 默认是:当前系统工作目录
     */
    private String saveTo = System.getProperty("user.dir");

    /**
     * 录屏的同时是否需要录音
     * 需要有录音设备的支持
     * 默认是:true需要
     */
    private boolean isAudioRecorder = true;

    /**
     * 设置核心线程池的大小
     * 默认是你当前CPU的核心数的一半
     * 如果你是单核CPU,线程池大小为1
     */
    private int corePoolSize = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(int videoCodec) {
        this.videoCodec = videoCodec;
    }

    public int getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(int audioCodec) {
        this.audioCodec = audioCodec;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public int getGopSize() {
        return gopSize;
    }

    public void setGopSize(int gopSize) {
        this.gopSize = gopSize;
    }

    public int getVideoQuality() {
        return videoQuality;
    }

    public void setVideoQuality(int videoQuality) {
        this.videoQuality = videoQuality;
    }

    public int getAudioQuality() {
        return audioQuality;
    }

    public void setAudioQuality(int audioQuality) {
        this.audioQuality = audioQuality;
    }

    public int getVideoBitrate() {
        return videoBitrate;
    }

    public void setVideoBitrate(int videoBitrate) {
        this.videoBitrate = videoBitrate;
    }

    public int getAudioBitrate() {
        return audioBitrate;
    }

    public void setAudioBitrate(int audioBitrate) {
        this.audioBitrate = audioBitrate;
    }

    public String getCrf() {
        return crf;
    }

    public void setCrf(String crf) {
        this.crf = crf;
    }

    public String getPreset() {
        return preset;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    public int getPixelFormat() {
        return pixelFormat;
    }

    public void setPixelFormat(int pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    public int getAudioChannels() {
        return audioChannels;
    }

    public void setAudioChannels(int audioChannels) {
        this.audioChannels = audioChannels;
    }

    public String getSaveTo() {
        return saveTo;
    }

    public void setSaveTo(String saveTo) {
        this.saveTo = saveTo;
    }

    public boolean isAudioRecorder() {
        return isAudioRecorder;
    }

    public void setAudioRecorder(boolean audioRecorder) {
        isAudioRecorder = audioRecorder;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * 初始化状态代号
     * 用来与当前状态做判断的
     */
    private static final String init = "init";

    /**
     * 开始录制时的状态代号
     * 用来与当前状态做判断的
     */
    private static final String start = "start";

    /**
     * 暂停录制时的状态代号
     */
    private static final String pause = "pause";

    /**
     * 结束录制时的状态代号
     */
    private static final String stop = "stop";

    /**
     * 用来记录当前的运行时的状态
     * 默认为:初始化状态init
     */
    private static String current = init;

    /**
     * 初始化格式、编码器、比特率、采样率;分配AVPacket空间
     * 每次开启录制前需要且只能调用一次初始化方法
     *
     * @param file 保存的录屏文件的文件名(不要到后缀,这里的后缀默认为.mp4)
     */
    private void init(Object file) {
        String fileName = this.saveTo + File.separatorChar + file.toString() + ".mp4";
        // 指定文件名、分辨率
        recorder = new FFmpegFrameRecorder(fileName, this.width, this.height);
        recorder.setVideoCodec(this.videoCodec);
        recorder.setFormat(this.format);
        recorder.setSampleRate(this.sampleRate);
        recorder.setFrameRate(this.frameRate);
        recorder.setVideoQuality(this.videoQuality);
        recorder.setVideoOption("crf", this.crf);
        recorder.setVideoOption("preset", this.preset);
        recorder.setVideoBitrate(this.videoBitrate);
        recorder.setPixelFormat(this.pixelFormat);
        recorder.setAudioChannels(this.audioChannels);
        recorder.setAudioOption("crf", "0");
        recorder.setAudioQuality(this.audioQuality);
        recorder.setAudioCodec(this.audioCodec);
        recorder.setGopSize(this.gopSize);
        recorder.setAudioBitrate(this.audioBitrate);
        // 重定向当前状态为 start
        current = start;
        try {
            // 初始化Robot
            robot = new Robot();
            // 创建并设置编码器、打开编码器、申请必要的编码缓存区
            recorder.start();
        } catch (Exception e) {
            log.error("初始化编码器异常:" + e.getMessage());
        }
    }

    /**
     * 开始录屏
     * 默认使用当前系统的时间
     */
    public void start() {
        start(String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 开始录屏
     */
    public void start(String file) {
        if (StringUtils.isBlank(file)) {
            file = String.valueOf(System.currentTimeMillis());
        }
        if (current.equals(init)) {
            init(file);
        }
        // 判断当前状态是否等于 start
        if (!current.equals(start) && !current.equals(pause)) {
            return;
        }
        // 重定向当前状态为:stop
        current = stop;
        // 指定开启录音
        if (isAudioRecorder) {
            ThreadUtil.execute(this::soundRecorder);
        }
        // 开启一个录屏线程
        screenThreadPool = new ScheduledThreadPoolExecutor(this.corePoolSize, new ThreadFactoryBuilder().build());
        // 截屏的大小
        final Rectangle rectangle = new Rectangle(this.width, this.height);
        if (pauseTime == 0) {
            pauseTime = System.currentTimeMillis();
        }
        long startTime = System.currentTimeMillis();
        screenThreadPool.scheduleAtFixedRate(() -> {
            try {
                // 创建一个截屏的图片
                BufferedImage screenCapture = robot.createScreenCapture(rectangle);
                // 声明一个BufferedImage用重绘截图
                // BufferedImage.TYPE_3BYTE_BGR表示具有8位RGB颜色分量的图像
                // 对应到一个窗口风格的BGR颜色模型与颜色蓝色，绿色，和红色存储在3个字节中
                BufferedImage videoImg = new BufferedImage(this.width, this.height, BufferedImage.TYPE_3BYTE_BGR);
                // 创建videoImg的Graphics2D
                Graphics2D videoGraphics = videoImg.createGraphics();
                videoGraphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
                videoGraphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
                videoGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                // 重绘截图
                videoGraphics.drawImage(screenCapture, 0, 0, null);
                Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
                Frame frame = java2dConverter.convert(videoImg);
                long videoTs = 1000L * (System.currentTimeMillis() - startTime - (System.currentTimeMillis() - pauseTime));
                if (videoTs > recorder.getTimestamp()) {
                    recorder.setTimestamp(videoTs);
                }
                // 录制视频
                recorder.record(frame);
                frame.clone();
                // 资源释放
                videoGraphics.dispose();
                videoImg.flush();
                screenCapture.flush();
            } catch (Exception e) {
                log.error("视频录制线程异常:" + e.getMessage());
            }
        }, 1000 / this.frameRate, 1000 / this.frameRate, TimeUnit.MILLISECONDS);
        System.gc();
    }

    /**
     * 抓取声音、声音的录制
     */
    private void soundRecorder() {
        audioFormat = new AudioFormat(this.sampleRate, 16, this.audioChannels, true, false);
        dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        try {
            line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            line.open(audioFormat);
            line.start();
            final int sampleRate = (int) audioFormat.getSampleRate();
            final int numChannels = audioFormat.getChannels();
            // 开始初始化音频缓冲区
            int audioBufferSize = sampleRate * numChannels;
            final byte[] audioBytes = new byte[audioBufferSize];

            // 初始化音频的线程池
            audioThreadPool = new ScheduledThreadPoolExecutor(this.corePoolSize, new ThreadFactoryBuilder().build());
            audioThreadPool.scheduleAtFixedRate(() -> {
                try {
                    // 开始读取行
                    int nBytesRead = line.read(audioBytes, 0, line.available());

                    // 由于在AudioFormat中指定了16位,因此需要将读取的byte[]转换为short[]
                    int nSamplesRead = nBytesRead / 2;
                    // 初始化短[]数组
                    short[] samples = new short[nSamplesRead];

                    // 将short[]封装到一个ShortBuffer中，并将它传递给recordSamples
                    ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                    ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
                    // 开始记录样本
                    recorder.recordSamples(sampleRate, numChannels, sBuff);
                } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
                    log.error("声音抓取线程异常:" + e.getMessage());
                }
            }, 1000 / this.frameRate, 1000 / this.frameRate, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("声音抓取方法体异常:" + e.getMessage());
        }
    }

    /**
     * 暂停录制
     */
    public void pause() {
        // 当前正在录屏
        if (!current.equals(stop)) {
            return;
        }
        current = pause;
        // 关闭用于录屏的线程
        if (screenThreadPool != null) {
            screenThreadPool.shutdown();
        }
        // 如果同时开启了录音,还需要关闭用于录音的线程
        stopAudio();
        screenThreadPool = null;
        pauseTime = System.currentTimeMillis();
        System.gc();
    }

    /**
     * 停止录制,同时生成录屏文件
     */
    public void stop() {
        // 当前正在录屏
        if (!current.equals(stop) && !current.equals(pause)) {
            return;
        }
        current = init;
        try {
            if (screenThreadPool != null) {
                screenThreadPool.shutdownNow();
            }
            recorder.stop();
            recorder.release();
            recorder.close();
            recorder = null;
            // 如果同时开启了录音,还需要关闭用于录音的线程
            stopAudio();
            screenThreadPool = null;
            pauseTime = 0;
            System.gc();
        } catch (Exception e) {
            log.error("stop方法体执行异常:" + e.getMessage());
        }
    }

    /**
     * 关闭用于录音的线程
     */
    private void stopAudio() {
        if (this.isAudioRecorder) {
            if (audioThreadPool != null) {
                audioThreadPool.shutdown();
            }
            if (line != null) {
                line.stop();
                line.close();
            }
            dataLineInfo = null;
            audioFormat = null;
            line = null;
            audioThreadPool = null;
        }
    }
}
