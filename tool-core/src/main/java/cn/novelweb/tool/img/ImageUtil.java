package cn.novelweb.tool.img;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>图片处理工具类</p>
 * <p>2020-03-25 16:48</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class ImageUtil extends ImgUtil {

    /**
     * 支持的图片类型数组
     */
    private static final String[] IMAGES_TYPE = {"jpg", "png"};

    /**
     * 背景移除
     * 图片去底工具
     * 纯色图片变矢量图工具
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
        if (!input.exists()) {
            log.error("没有要进行操作的图片");
            return false;
        }
        // 获取图片类型
        String type = FileTypeUtil.getType(input);
        // 类型对比
        if (!ArrayUtil.contains(IMAGES_TYPE, type)) {
            log.error("文件类型不被支持");
            return false;
        }

        // 容差值 最大255 最小0
        tolerance = Math.min(255, Math.max(tolerance, 0));

        try {
            // 获取图片左上、中上、右上、右中、右下、下中、左下、左中、8个像素点rgb的16进制值
            BufferedImage bufferedImage = ImageIO.read(input);
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
                        rgb = ((alpha + 1) << 24) | (rgb & 0x00ffffff);
                    }
                    image.setRGB(x, y, rgb);
                }
            }
            graphics.drawImage(image, 0, 0, imageIcon.getImageObserver());
            return ImageIO.write(image, FileUtil.extName(input.getAbsolutePath()), output);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
}
