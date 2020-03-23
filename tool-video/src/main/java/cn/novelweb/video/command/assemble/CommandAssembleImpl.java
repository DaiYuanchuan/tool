package cn.novelweb.video.command.assemble;

import cn.hutool.core.map.MapUtil;
import cn.novelweb.tool.upload.fastdfs.utils.Log;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * <p>默认命令组装器的实现</p>
 * <p>2020-02-24 22:36</p>
 *
 * @author Dai Yuanchuan
 **/
public class CommandAssembleImpl implements CommandAssemble {


    @Override
    public String assemble(Map<String, String> paramMap) {
        try {
            if (MapUtil.isEmpty(paramMap)) {
                Log.debug("命令值参数为NULL,组装命令失败");
                return null;
            }
            // 获取 Fast Forward Moving Picture Experts Group 路径
            String path = paramMap.get("FFMPEGPath");
            if (StringUtils.isNotBlank(path)) {
                // -i：输入流地址或者文件绝对地址
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(path);
                stringBuilder.append(" -i ");
                // 是否有必输项:输入地址,输出地址,应用名.
                // twoPart:0-推一个元码流;1-推一个自定义推流;2-推两个流(一个是自定义，一个是元码)
                boolean isRequiredField = paramMap.containsKey("input") && paramMap.containsKey("output") &&
                        paramMap.containsKey("appName") && paramMap.containsKey("twoPart");
                if (isRequiredField) {
                    String input = paramMap.get("input");
                    String output = paramMap.get("output");
                    String appName = paramMap.get("appName");
                    String twoPart = paramMap.get("twoPart");
                    String codec = paramMap.get("codec");
                    codec = (codec == null ? "h264" : codec);
                    // 输入地址
                    stringBuilder.append(input);
                    // 当twoPart为0时，只推一个元码流
                    boolean isZero = "0".equals(twoPart);
                    if (isZero) {
                        stringBuilder.append(" -vcodec ").append(codec)
                                .append(" -f flv -an ").append(output).append(appName);
                    } else {
                        // -f:转换格式,默认flv
                        String fmt = paramMap.get("fmt");
                        if (StringUtils.isNotBlank(fmt)) {
                            stringBuilder.append(" -f ").append(fmt);
                        }
                        // -r :帧率，默认25；-g :帧间隔
                        String fps = paramMap.get("fps");
                        if (StringUtils.isNotBlank(fps)) {
                            stringBuilder.append(" -r ").append(fps)
                                    .append(" -g ").append(fps);
                        }
                        // -s 分辨率 默认是原分辨率
                        String rs = paramMap.get("rs");
                        if (StringUtils.isNotBlank(rs)) {
                            stringBuilder.append(" -s ").append(rs);
                        }
                        // 输出地址 + 发布的应用名
                        stringBuilder.append(" -an ").append(output).append(appName);
                        // 当twoPart为2时推两个流，一个自定义流，一个元码流
                        boolean isTwo = "2".equals(twoPart);
                        if (isTwo) {
                            // 一个视频源，可以有多个输出，第二个输出为拷贝源视频输出，不改变视频的各项参数并且命名为应用名+HD
                            stringBuilder.append(" -vcodec copy  -f flv -an ")
                                    .append(output).append(appName).append("HD");
                        }
                    }
                    return stringBuilder.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
