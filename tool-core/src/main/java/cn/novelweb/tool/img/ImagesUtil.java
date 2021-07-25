package cn.novelweb.tool.img;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReUtil;
import cn.novelweb.config.ConstantConfiguration;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.*;

/**
 * <p>图片处理工具类</p>
 * <p>2020-03-31 11:07</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class ImagesUtil {

    /**
     * 支持的图片类型数组
     * 其他格式不保证效果
     */
    public static String[] IMAGES_TYPE = {"jpg", "png"};

    // -------------------------------------------------------------------------- 图片压缩

    /**
     * 压缩至指定图片尺寸(例如：横500高500)，保持图片不变形，多余部分裁剪掉
     *
     * @param input  图片流
     * @param suffix 文件后缀 jpg、png等等
     * @param width  压缩至:宽度(最小为1)
     * @param height 压缩至:高度(最小为1)
     * @return 返回图片的byte类型数组
     */
    public static byte[] compressPicturesOutByte(InputStream input, String suffix, Integer width, Integer height) {
        ByteArrayOutputStream byteArrayOutputStream = compressPicturesOutStream(input, suffix, width, height);
        return byteArrayOutputStream == null ? null : byteArrayOutputStream.toByteArray();
    }

    /**
     * 压缩至指定图片尺寸(例如：横500高500)，保持图片不变形，多余部分裁剪掉
     *
     * @param input  图片流
     * @param suffix 文件后缀 jpg、png等等
     * @param width  压缩至:宽度(最小为1)
     * @param height 压缩至:高度(最小为1)
     * @return 返回图片的字节数组输出流
     */
    public static ByteArrayOutputStream compressPicturesOutStream(InputStream input, String suffix, Integer width, Integer height) {
        if (input == null) {
            log.error("图片为null");
            return null;
        }
        // 设置宽高最小值为1
        width = Math.max(width, 1);
        height = Math.max(height, 1);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            //压缩至指定图片尺寸（例如：横500高500），保持图片不变形，多余部分裁剪掉
            BufferedImage image = ImageIO.read(input);
            Thumbnails.Builder<BufferedImage> builder;
            // 定义宽高变量
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            boolean isRatio = 0.75 != (float) imageWidth / imageHeight;
            if (isRatio) {
                if (imageWidth > imageHeight) {
                    image = Thumbnails.of(image).height(height).asBufferedImage();
                } else {
                    image = Thumbnails.of(image).width(width).asBufferedImage();
                }
                builder = Thumbnails.of(image).sourceRegion(net.coobird.thumbnailator.geometry.Positions.CENTER, width, height).size(width, height);
            } else {
                builder = Thumbnails.of(image).size(width, height);
            }
            builder.outputFormat(suffix).toOutputStream(outputStream);
            return outputStream;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            log.error("压缩指定宽高图片出错:{}", e.getMessage());
        }
        return null;
    }

    /**
     * 压缩指定 网络 图片 大小(例如：横500高500)，保持图片不变形，多余部分裁剪掉
     *
     * @param url    网络图片URL
     * @param width  压缩至:宽度(最小为10)
     * @param height 压缩至:高度(最小为10)
     * @return 返回图片的byte类型数组
     */
    public static byte[] compressNetworkPicturesOutByte(String url, Integer width, Integer height) {
        ByteArrayOutputStream byteArrayOutputStream = compressNetworkPicturesOutStream(url, width, height);
        return byteArrayOutputStream == null ? null : byteArrayOutputStream.toByteArray();
    }

    /**
     * 压缩指定 网络 图片 大小(例如：横500高500)，保持图片不变形，多余部分裁剪掉
     *
     * @param url    网络图片URL
     * @param width  压缩至:宽度(最小为10)
     * @param height 压缩至:高度(最小为10)
     * @return 返回图片的字节数组输出流
     */
    public static ByteArrayOutputStream compressNetworkPicturesOutStream(String url, Integer width, Integer height) {
        if (ReUtil.get(ConstantConfiguration.URL_REGULARIZATION, url, 0) == null) {
            log.error("url不符合规则");
            return null;
        }
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new URL(url).openStream())) {
            return compressPicturesOutStream(bufferedInputStream, FileTypeUtil.getType(new URL(url).openStream()),
                    Math.max(width, 1), Math.max(height, 1));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("压缩网络图片出错:{}", e.getMessage());
            return null;
        }
    }

    /**
     * 按照比例进行缩放...图片尺寸不变
     *
     * @param imgFile 文件流
     * @param suffix  文件后缀 jpg、png等等
     * @param scaling 缩放比例(1为最高质量,大于1就是变大,小于1就是缩小)
     * @return 返回图片的byte类型数组
     */
    public static byte[] scaleToScaleOutByte(InputStream imgFile, String suffix, double scaling) {
        ByteArrayOutputStream byteArrayOutputStream = scaleToScaleOutStream(imgFile, suffix, scaling);
        return byteArrayOutputStream == null ? null : byteArrayOutputStream.toByteArray();
    }

    /**
     * 按照比例进行缩放...图片尺寸不变
     *
     * @param imgFile 文件流
     * @param suffix  文件后缀 jpg、png等等
     * @param scaling 缩放比例(1为最高质量,大于1就是变大,小于1就是缩小)
     * @return 返回图片的字节数组输出流
     */
    public static ByteArrayOutputStream scaleToScaleOutStream(InputStream imgFile, String suffix, double scaling) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(imgFile).scale(scaling).outputFormat(suffix).toOutputStream(outputStream);
            return outputStream;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("压缩网络图片出错:{}", e.getMessage());
            return null;
        }
    }

    /**
     * 不按照比例，指定大小进行缩放
     *
     * @param imgFile   文件流
     * @param suffix    文件后缀 jpg、png等等
     * @param width     压缩至:宽
     * @param height    压缩至:高
     * @param isScaling 是否按照比例缩放(true按照比例缩放 : false不按照比例缩放)
     * @return 返回图片的byte类型数组
     */
    public static byte[] scaleToScaleOutByte(InputStream imgFile, String suffix, Integer width, Integer height, boolean isScaling) {
        ByteArrayOutputStream byteArrayOutputStream = scaleToScaleOutStream(imgFile, suffix, width, height, isScaling);
        return byteArrayOutputStream == null ? null : byteArrayOutputStream.toByteArray();
    }

    /**
     * 不按照比例，指定大小进行缩放
     *
     * @param imgFile   文件流
     * @param suffix    文件后缀 jpg、png等等
     * @param width     压缩至:宽
     * @param height    压缩至:高
     * @param isScaling 是否按照比例缩放(true按照比例缩放 : false不按照比例缩放)
     * @return 返回图片的字节数组输出流
     */
    public static ByteArrayOutputStream scaleToScaleOutStream(InputStream imgFile, String suffix, Integer width, Integer height, boolean isScaling) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(imgFile).size(width, height).keepAspectRatio(isScaling).outputFormat(suffix).toOutputStream(outputStream);
            return outputStream;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("不按照比例缩放图片出错:{}", e.getMessage());
            return null;
        }
    }

    /**
     * 压缩图片大小 图片尺寸保持不变
     *
     * @param imgFile          文件流
     * @param compressionRatio 压缩比例[设置缩略图的缩放因子,值大于0.0]
     * @param scaling          缩放比例[取值范围 0.0 ~ 1.0 之间]
     * @return 返回图片的byte类型数组
     */
    public static byte[] compressionSizeOutByte(InputStream imgFile, String suffix, double compressionRatio, double scaling) {
        ByteArrayOutputStream byteArrayOutputStream = compressionSizeOutStream(imgFile, suffix, compressionRatio, scaling);
        return byteArrayOutputStream == null ? null : byteArrayOutputStream.toByteArray();
    }

    /**
     * 压缩图片大小 图片尺寸保持不变
     *
     * @param imgFile          文件流
     * @param compressionRatio 压缩比例[设置缩略图的缩放因子,值大于0.0]
     * @param scaling          缩放比例[取值范围 0.0 ~ 1.0 之间]
     * @return 返回图片的字节数组输出流
     */
    public static ByteArrayOutputStream compressionSizeOutStream(InputStream imgFile, String suffix, double compressionRatio, double scaling) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            compressionRatio = Math.max(compressionRatio, 0.0);
            scaling = Math.min(1.0, Math.max(scaling, 0.0));
            Thumbnails.of(imgFile).scale(compressionRatio).outputQuality(scaling).outputFormat(suffix).toOutputStream(outputStream);
            return outputStream;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("压缩图片大小图片尺寸不变出错:{}", e.getMessage());
            return null;
        }
    }

    /**
     * 转换图片格式
     * 改变图片文件的输出格式、可以保持图片大小不变
     *
     * @param imgFile    文件流
     * @param formatName 包含格式非正式名称的 String：如JPG、JPEG、GIF等
     * @param scaling    缩放比例(1为最高质量,大于1就是变大,小于1就是缩小)
     * @return 返回图片的byte类型数组
     */
    public static byte[] convertOutByte(InputStream imgFile, String formatName, double scaling) {
        ByteArrayOutputStream byteArrayOutputStream = convertOutStream(imgFile, formatName, scaling);
        return byteArrayOutputStream == null ? null : byteArrayOutputStream.toByteArray();
    }

    /**
     * 转换图片格式
     * 改变图片文件的输出格式、可以保持图片大小不变
     *
     * @param imgFile    文件流
     * @param formatName 包含格式非正式名称的 String：如JPG、JPEG、GIF等
     * @param scaling    缩放比例(1为最高质量,大于1就是变大,小于1就是缩小)
     * @return 返回图片的字节数组输出流
     */
    public static ByteArrayOutputStream convertOutStream(InputStream imgFile, String formatName, double scaling) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            scaling = Math.max(scaling, 0.0);
            Thumbnails.of(imgFile).scale(scaling).outputFormat(formatName).toOutputStream(outputStream);
            return outputStream;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("转换图片格式出错:{}", e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------- 图片背景图替换

    /**
     * 背景移除
     * 图片去底工具
     * 将 "纯色背景的图片" 还原成 "透明背景的图片"
     * 将纯色背景的图片转成矢量图
     * 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色
     * 再加入一定的容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param inputPath  要处理图片的路径
     * @param outputPath 输出图片的路径
     * @param tolerance  容差值[根据图片的主题色,加入容差值,值的范围在0~255之间]
     * @return 返回处理结果 true:图片处理完成 false:图片处理失败
     */
    public static boolean backgroundRemoval(String inputPath, String outputPath, int tolerance) {
        return backgroundRemoval(new File(inputPath), new File(outputPath), tolerance);
    }

    /**
     * 背景移除
     * 图片去底工具
     * 将 "纯色背景的图片" 还原成 "透明背景的图片"
     * 将纯色背景的图片转成矢量图
     * 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色
     * 再加入一定的容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param input     需要进行操作的图片
     * @param output    最后输出的文件
     * @param tolerance 容差值[根据图片的主题色,加入容差值,值的取值范围在0~255之间]
     * @return 返回处理结果 true:图片处理完成 false:图片处理失败
     */
    public static boolean backgroundRemoval(File input, File output, int tolerance) {
        return backgroundRemoval(input, output, null, tolerance);
    }

    /**
     * 背景移除
     * 图片去底工具
     * 将 "纯色背景的图片" 还原成 "透明背景的图片"
     * 将纯色背景的图片转成矢量图
     * 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色
     * 再加入一定的容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param input     需要进行操作的图片
     * @param output    最后输出的文件
     * @param override  指定替换成的背景颜色 为null时背景为透明
     * @param tolerance 容差值[根据图片的主题色,加入容差值,值的取值范围在0~255之间]
     * @return 返回处理结果 true:图片处理完成 false:图片处理失败
     */
    public static boolean backgroundRemoval(File input, File output, Color override, int tolerance) {
        if (fileTypeValidation(input, IMAGES_TYPE)) {
            return false;
        }
        try {
            // 获取图片左上、中上、右上、右中、右下、下中、左下、左中、8个像素点rgb的16进制值
            BufferedImage bufferedImage = ImageIO.read(input);
            return ImageIO.write(backgroundRemoval(bufferedImage, override, tolerance), "png", output);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 背景移除
     * 图片去底工具
     * 将 "纯色背景的图片" 还原成 "透明背景的图片"
     * 将纯色背景的图片转成矢量图
     * 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色
     * 再加入一定的容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param bufferedImage 需要进行处理的图片流
     * @param override      指定替换成的背景颜色 为null时背景为透明
     * @param tolerance     容差值[根据图片的主题色,加入容差值,值的取值范围在0~255之间]
     * @return 返回处理好的图片流
     */
    public static BufferedImage backgroundRemoval(BufferedImage bufferedImage, Color override, int tolerance) {
        // 容差值 最大255 最小0
        tolerance = Math.min(255, Math.max(tolerance, 0));
        // 绘制icon
        ImageIcon imageIcon = new ImageIcon(bufferedImage);
        BufferedImage image = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(),
                BufferedImage.TYPE_4BYTE_ABGR);
        // 绘图工具
        Graphics graphics = image.getGraphics();
        graphics.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
        // 需要删除的RGB元素
        String[] removeRgb = getRemoveRgb(bufferedImage);
        // 获取图片的大概主色调
        String mainColor = getMainColor(bufferedImage);
        int alpha = 0;
        for (int y = image.getMinY(); y < image.getHeight(); y++) {
            for (int x = image.getMinX(); x < image.getWidth(); x++) {
                // 获取像素的16进制
                int rgb = image.getRGB(x, y);
                String hex = rgbToHex((rgb & 0xff0000) >> 16, (rgb & 0xff00) >> 8, (rgb & 0xff));
                boolean isTrue = ArrayUtil.contains(removeRgb, hex) ||
                        areColorsWithinTolerance(hexToRgb(mainColor), new Color(Integer.parseInt(hex.substring(1), 16)), tolerance);
                if (isTrue) {
                    rgb = override == null ? ((alpha + 1) << 24) | (rgb & 0x00ffffff) : override.getRGB();
                }
                image.setRGB(x, y, rgb);
            }
        }
        graphics.drawImage(image, 0, 0, imageIcon.getImageObserver());
        return image;
    }

    /**
     * 背景移除
     * 图片去底工具
     * 将 "纯色背景的图片" 还原成 "透明背景的图片"
     * 将纯色背景的图片转成矢量图
     * 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色
     * 再加入一定的容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param outputStream 需要进行处理的图片字节数组流
     * @param override     指定替换成的背景颜色 为null时背景为透明
     * @param tolerance    容差值[根据图片的主题色,加入容差值,值的取值范围在0~255之间]
     * @return 返回处理好的图片流
     */
    public static BufferedImage backgroundRemoval(ByteArrayOutputStream outputStream, Color override, int tolerance) {
        try {
            return backgroundRemoval(ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray())), override, tolerance);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取要删除的 RGB 元素
     * 分别获取图片左上、中上、右上、右中、右下、下中、左下、左中、8个像素点rgb的16进制值
     *
     * @param image 图片流
     * @return String数组 包含 各个位置的rgb数值
     */
    private static String[] getRemoveRgb(BufferedImage image) {
        // 获取图片流的宽和高
        int width = image.getWidth() - 1;
        int height = image.getHeight() - 1;
        // 左上
        int leftUpPixel = image.getRGB(1, 1);
        String leftUp = rgbToHex((leftUpPixel & 0xff0000) >> 16, (leftUpPixel & 0xff00) >> 8, (leftUpPixel & 0xff));
        // 上中
        int upMiddlePixel = image.getRGB(width / 2, 1);
        String upMiddle = rgbToHex((upMiddlePixel & 0xff0000) >> 16, (upMiddlePixel & 0xff00) >> 8, (upMiddlePixel & 0xff));
        // 右上
        int rightUpPixel = image.getRGB(width, 1);
        String rightUp = rgbToHex((rightUpPixel & 0xff0000) >> 16, (rightUpPixel & 0xff00) >> 8, (rightUpPixel & 0xff));
        // 右中
        int rightMiddlePixel = image.getRGB(width, height / 2);
        String rightMiddle = rgbToHex((rightMiddlePixel & 0xff0000) >> 16, (rightMiddlePixel & 0xff00) >> 8, (rightMiddlePixel & 0xff));
        // 右下
        int lowerRightPixel = image.getRGB(width, height);
        String lowerRight = rgbToHex((lowerRightPixel & 0xff0000) >> 16, (lowerRightPixel & 0xff00) >> 8, (lowerRightPixel & 0xff));
        // 下中
        int lowerMiddlePixel = image.getRGB(width / 2, height);
        String lowerMiddle = rgbToHex((lowerMiddlePixel & 0xff0000) >> 16, (lowerMiddlePixel & 0xff00) >> 8, (lowerMiddlePixel & 0xff));
        // 左下
        int leftLowerPixel = image.getRGB(1, height);
        String leftLower = rgbToHex((leftLowerPixel & 0xff0000) >> 16, (leftLowerPixel & 0xff00) >> 8, (leftLowerPixel & 0xff));
        // 左中
        int leftMiddlePixel = image.getRGB(1, height / 2);
        String leftMiddle = rgbToHex((leftMiddlePixel & 0xff0000) >> 16, (leftMiddlePixel & 0xff00) >> 8, (leftMiddlePixel & 0xff));
        // 需要删除的RGB元素
        return new String[]{leftUp, upMiddle, rightUp, rightMiddle, lowerRight, lowerMiddle, leftLower, leftMiddle};
    }

    /**
     * RGB颜色值转换成十六进制颜色码
     *
     * @param r 红(R)
     * @param g 绿(G)
     * @param b 蓝(B)
     * @return 返回字符串形式的 十六进制颜色码 如
     */
    public static String rgbToHex(int r, int g, int b) {
        // rgb 小于 255
        boolean isRgb = (0 <= r && r <= 255) && (0 <= g && g <= 255) && (0 <= b && b <= 255);
        if (isRgb) {
            return String.format("#%02X%02X%02X", r, g, b);
        } else {
            log.error("RGB颜色值只能为0~255之间的整数");
            return "";
        }
    }

    /**
     * 十六进制颜色码转RGB颜色值
     *
     * @param hex 十六进制颜色码
     * @return 返回 RGB颜色值
     */
    public static Color hexToRgb(String hex) {
        return new Color(Integer.parseInt(hex.substring(1), 16));
    }


    /**
     * 判断颜色是否在容差范围内
     * 对比两个颜色的相似度，判断这个相似度是否小于 tolerance 容差值
     *
     * @param color1    颜色1
     * @param color2    颜色2
     * @param tolerance 容差值
     * @return 返回true:两个颜色在容差值之内 false: 不在
     */
    public static boolean areColorsWithinTolerance(Color color1, Color color2, int tolerance) {
        return areColorsWithinTolerance(color1, color2, new Color(tolerance, tolerance, tolerance));
    }

    /**
     * 判断颜色是否在容差范围内
     * 对比两个颜色的相似度，判断这个相似度是否小于 tolerance 容差值
     *
     * @param color1    颜色1
     * @param color2    颜色2
     * @param tolerance 容差色值
     * @return 返回true:两个颜色在容差值之内 false: 不在
     */
    public static boolean areColorsWithinTolerance(Color color1, Color color2, Color tolerance) {
        return (color1.getRed() - color2.getRed() < tolerance.getRed() && color1
                .getRed() - color2.getRed() > -tolerance.getRed())
                && (color1.getBlue() - color2.getBlue() < tolerance
                .getBlue() && color1.getBlue() - color2.getBlue() > -tolerance
                .getBlue())
                && (color1.getGreen() - color2.getGreen() < tolerance
                .getGreen() && color1.getGreen()
                - color2.getGreen() > -tolerance.getGreen());
    }

    /**
     * 获取图片大概的主题色
     * 循环所有的像素点,取出出现次数最多的一个像素点的RGB值
     *
     * @param input 图片文件路径
     * @return 返回一个图片的大概的色值 一个16进制的颜色码
     */
    public static String getMainColor(String input) {
        return getMainColor(new File(input));
    }

    /**
     * 获取图片大概的主题色
     * 循环所有的像素点,取出出现次数最多的一个像素点的RGB值
     *
     * @param input 图片文件
     * @return 返回一个图片的大概的色值 一个16进制的颜色码
     */
    public static String getMainColor(File input) {
        try {
            return getMainColor(ImageIO.read(input));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取图片大概的主题色
     * 循环所有的像素点,取出出现次数最多的一个像素点的RGB值
     *
     * @param bufferedImage 图片流
     * @return 返回一个图片的大概的色值 一个16进制的颜色码
     */
    public static String getMainColor(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            log.error("图片流是空的");
            return "";
        }

        // 存储图片的所有RGB元素
        List<String> list = new ArrayList<>();
        for (int y = bufferedImage.getMinY(); y < bufferedImage.getHeight(); y++) {
            for (int x = bufferedImage.getMinX(); x < bufferedImage.getWidth(); x++) {
                int pixel = bufferedImage.getRGB(x, y);
                list.add(((pixel & 0xff0000) >> 16) + "-" + ((pixel & 0xff00) >> 8) + "-" + (pixel & 0xff));
            }
        }

        Map<String, Integer> map = new HashMap<>(list.size());
        for (String string : list) {
            Integer integer = map.get(string);
            if (integer == null) {
                integer = 1;
            } else {
                integer++;
            }
            map.put(string, integer);
        }
        String max = "";
        long num = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            Integer temp = entry.getValue();
            if (StringUtils.isBlank(max) || temp > num) {
                max = key;
                num = temp;
            }
        }
        String[] strings = max.split("-");
        // rgb 的数量只有3个
        int rgbLength = 3;
        if (strings.length == rgbLength) {
            return rgbToHex(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]),
                    Integer.parseInt(strings[2]));
        }
        return "";
    }

    // -------------------------------------------------------------------------- 图片裁剪

    /**
     * 图片指定 X轴，Y轴  裁剪、切割
     * 给定 X轴、Y轴、裁剪后的 宽、高
     * 进行指定位置的裁剪
     *
     * @param input  指定需要进行操作的图片文件
     * @param x      x轴 图片从指定 x轴 开始切割
     * @param y      y轴 图片从指定 y轴 开始切割
     * @param width  图片需要切割的 宽度
     * @param height 图片需要切割的 高度
     * @return 返回切割后的图片流 java.awt.image.BufferedImage
     */
    public static BufferedImage cutPictureOutStream(File input, Integer x, Integer y, Integer width, Integer height) {
        if (fileTypeValidation(input, IMAGES_TYPE)) {
            return null;
        }

        // 设置默认值
        x = Math.max(x, 0);
        y = Math.max(y, 0);
        width = Math.max(width, 0);
        height = Math.max(height, 0);

        // 取得图片读入器
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(FileTypeUtil.getType(input));
        ImageReader reader = readers.next();
        // 取得图片读入流
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(new FileInputStream(input))) {
            reader.setInput(imageInputStream, true);
            // 图片参数对象
            ImageReadParam param = reader.getDefaultReadParam();
            Rectangle rectangle = new Rectangle(x, y, width, height);
            param.setSourceRegion(rectangle);
            return reader.read(0, param);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 图片指定 X轴，Y轴  裁剪、切割
     * 给定 X轴、Y轴、裁剪后的 宽、高
     * 进行指定位置的裁剪
     *
     * @param input  指定需要进行操作的图片文件
     * @param x      x轴 图片从指定 x轴 开始切割
     * @param y      y轴 图片从指定 y轴 开始切割
     * @param width  图片需要切割的 宽度
     * @param height 图片需要切割的 高度
     * @return 返回切割后的图片字节数组
     */
    public static byte[] cutPictureOutByte(File input, Integer x, Integer y, Integer width, Integer height) {
        BufferedImage bufferedImage = cutPictureOutStream(input, x, y, width, height);
        if (bufferedImage == null) {
            return null;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, FileTypeUtil.getType(input), out);
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 图片指定 X轴，Y轴  裁剪、切割
     * 给定 X轴、Y轴、裁剪后的 宽、高
     * 进行指定位置的裁剪
     *
     * @param input  指定需要进行操作的图片文件
     * @param output 指定切割后需要输出的文件
     * @param x      x轴 图片从指定 x轴 开始切割
     * @param y      y轴 图片从指定 y轴 开始切割
     * @param width  图片需要切割的 宽度
     * @param height 图片需要切割的 高度
     * @return 返回布尔值 文件是否写入成功标识 true:文件成功写入 false:文件写入失败
     */
    public static boolean cutPictureOutFile(File input, File output, Integer x, Integer y, Integer width, Integer height) {
        BufferedImage bufferedImage = cutPictureOutStream(input, x, y, width, height);
        if (bufferedImage == null) {
            return false;
        }
        try {
            return ImageIO.write(bufferedImage, FileTypeUtil.getType(input), output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // -------------------------------------------------------------------------- 图片拼接、合并

    /**
     * 图片拼接
     * 多张图片进行 横向或纵向拼接
     * 根据需求图像的宽高需要保持一致
     * 注意:横向合并时 高度必须要保持一致
     * 纵向合并时 宽度必须要保持一致
     *
     * @param files                需要进行拼接的文件数组
     * @param horizontalOrVertical 布尔值 true:进行横向拼接 false:进行纵向拼接
     * @return 返回拼接后的图片流 java.awt.image.BufferedImage
     */
    public static BufferedImage mergeOutStream(File[] files, boolean horizontalOrVertical) {
        boolean eligible = false;
        for (File file : files) {
            eligible = fileTypeValidation(file, IMAGES_TYPE);
        }
        if (eligible) {
            return null;
        }
        // 图片流数组
        BufferedImage[] images = new BufferedImage[files.length];
        int[][] imageArrays = new int[files.length][];
        for (int i = 0; i < files.length; i++) {
            try {
                images[i] = ImageIO.read(files[i]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            int width = images[i].getWidth();
            int height = images[i].getHeight();
            imageArrays[i] = new int[width * height];
            imageArrays[i] = images[i].getRGB(0, 0, width, height, imageArrays[i], 0, width);
        }
        // 获取最高的 高、宽
        int newHeight = 0, newWidth = 0;
        for (BufferedImage image : images) {
            if (horizontalOrVertical) {
                // 图片横向拼接
                newHeight = Math.max(newHeight, image.getHeight());
                newWidth += image.getWidth();
            } else {
                // 图片纵向拼接
                newWidth = Math.max(newWidth, image.getWidth());
                newHeight += image.getHeight();
            }
        }

        // 判断宽高是否存在值
        boolean judgeWidthAndHeight = (horizontalOrVertical && newWidth < 1) || (!horizontalOrVertical && newHeight < 1);
        if (judgeWidthAndHeight) {
            return null;
        }

        // 生成新图片流
        BufferedImage newPictureStream = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        int height = 0, width = 0;
        // 进行图片拼接操作
        for (int i = 0; i < images.length; i++) {
            if (horizontalOrVertical) {
                // 横向合并时 高度必须要保持一致
                newPictureStream.setRGB(width, 0, images[i].getWidth(), newHeight, imageArrays[i], 0, images[i].getWidth());
                width += images[i].getWidth();
            } else {
                // 纵向合并时 宽度必须要保持一致
                newPictureStream.setRGB(0, height, newWidth, images[i].getHeight(), imageArrays[i], 0, newWidth);
                height += images[i].getHeight();
            }
        }
        return newPictureStream;
    }

    /**
     * 图片拼接
     * 多张图片进行 横向或纵向拼接
     *
     * @param files                需要进行拼接的文件路径数组
     * @param horizontalOrVertical 布尔值 true:进行横向拼接 false:进行纵向拼接
     * @return 返回拼接后的图片流 java.awt.image.BufferedImage
     */
    public static BufferedImage mergeOutStream(String[] files, boolean horizontalOrVertical) {
        return mergeOutStream(arrayTypeConversion(files), horizontalOrVertical);
    }

    /**
     * 图片拼接
     * 多张图片进行 横向或纵向拼接
     *
     * @param files                需要进行拼接的文件数组
     * @param horizontalOrVertical 布尔值 true:进行横向拼接 false:进行纵向拼接
     * @return 返回切割后的图片字节数组
     */
    public static byte[] mergeOutByte(File[] files, boolean horizontalOrVertical) {
        BufferedImage bufferedImage = mergeOutStream(files, horizontalOrVertical);
        if (bufferedImage == null) {
            return null;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, FileTypeUtil.getType(files[0]), out);
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 图片拼接
     * 多张图片进行 横向或纵向拼接
     *
     * @param files                需要进行拼接的文件路径数组
     * @param horizontalOrVertical 布尔值 true:进行横向拼接 false:进行纵向拼接
     * @return 返回切割后的图片字节数组
     */
    public static byte[] mergeOutByte(String[] files, boolean horizontalOrVertical) {
        return mergeOutByte(arrayTypeConversion(files), horizontalOrVertical);
    }

    /**
     * 图片拼接
     * 多张图片进行 横向或纵向拼接
     *
     * @param files                需要进行拼接的文件数组
     * @param output               指定图片拼接后需要输出的文件
     * @param horizontalOrVertical 布尔值 true:进行横向拼接 false:进行纵向拼接
     * @return 返回布尔值 文件是否写入成功标识 true:文件成功写入 false:文件写入失败
     */
    public static boolean mergeOutFile(File[] files, File output, boolean horizontalOrVertical) {
        BufferedImage bufferedImage = mergeOutStream(files, horizontalOrVertical);
        if (bufferedImage == null) {
            return false;
        }
        try {
            return ImageIO.write(bufferedImage, FileTypeUtil.getType(files[0]), output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 图片拼接
     * 多张图片进行 横向或纵向拼接
     *
     * @param files                需要进行拼接的文件路径数组
     * @param output               指定图片拼接后需要输出的文件
     * @param horizontalOrVertical 布尔值 true:进行横向拼接 false:进行纵向拼接
     * @return 返回布尔值 文件是否写入成功标识 true:文件成功写入 false:文件写入失败
     */
    public static boolean mergeOutFile(String[] files, String output, boolean horizontalOrVertical) {
        return mergeOutFile(arrayTypeConversion(files), new File(output), horizontalOrVertical);
    }

    // -------------------------------------------------------------------------- 图片工具类私有方法

    /**
     * 文件类型验证
     * 根据给定文件类型数据，验证给定文件类型.
     *
     * @param input      需要进行验证的文件
     * @param imagesType 文件包含的类型数组
     * @return 返回布尔值 false:给定文件的文件类型在文件数组中  true:给定文件的文件类型 不在给定数组中。
     */
    private static boolean fileTypeValidation(File input, String[] imagesType) {
        if (!input.exists()) {
            log.error("给定文件为空");
            return true;
        }
        // 获取图片类型
        String type = FileTypeUtil.getType(input);
        // 类型对比
        if (!ArrayUtil.contains(imagesType, type)) {
            log.error("文件类型{}不支持", type);
            return true;
        }
        return false;
    }

    /**
     * 数组类型转换
     * 将String数组类型的 文件路径数组
     * 转为File[]
     *
     * @param files 文件路径字符串数组
     * @return 返回文件数组
     */
    private static File[] arrayTypeConversion(String[] files) {
        File[] fileArray = new File[files.length];
        for (int i = 0; i < files.length; i++) {
            fileArray[i] = new File(files[i]);
        }
        return fileArray;
    }
}
