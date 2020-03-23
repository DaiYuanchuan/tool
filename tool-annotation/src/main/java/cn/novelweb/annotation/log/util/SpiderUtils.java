package cn.novelweb.annotation.log.util;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>分析 User-Agent 获取蜘蛛类型工具类</p>
 * <p>2019-12-03 22:46</p>
 *
 * @author Dai Yuanchuan
 **/
public class SpiderUtils {

    /**
     * 分析蜘蛛类型
     *
     * @param userAgent 用户请求的 userAgent
     * @return 返回分析结果 ， 未能查到结果时返回 空字符串
     */
    public static String parseSpiderType(String userAgent) {
        if (StringUtils.isBlank(userAgent)) {
            return "";
        }
        Map<String, String> spiderList = spiderList();
        for (Map.Entry<String, String> entry : spiderList.entrySet()) {
            String spiderSign = entry.getKey();
            if (userAgent.contains(spiderSign)
                    || userAgent.equalsIgnoreCase(spiderSign)
                    || userAgent.toLowerCase().contains(spiderSign.toLowerCase())) {
                return entry.getValue();
            }
        }
        return "";
    }

    /**
     * 目前已知的蜘蛛列表
     *
     * @return 返回蜘蛛列表MAP
     */
    private static Map<String, String> spiderList() {
        Map<String, String> spider = new HashMap<>(61);
        spider.put("Baiduspider-image", "百度图片搜索");
        spider.put("Baiduspider-video", "百度视频搜索");
        spider.put("Baiduspider-news", "百度新闻搜索");
        spider.put("Baiduspider-favo", "百度搜藏");
        spider.put("Baiduspider-cpro", "百度联盟");
        spider.put("Baiduspider-sfkr", "百度竞价蜘蛛");
        spider.put("Baiduspider-ads", "百度商务搜索");
        spider.put("Baidu-YunGuanCe", "百度云观测");
        spider.put("Baiduspider", "百度");
        spider.put("www.baidu.com", "百度");
        spider.put("baidu.com", "百度");
        spider.put("Google Web Preview", "谷歌");
        spider.put("Google Search Console", "谷歌站长工具");
        spider.put("Google-Site-Verification", "谷歌站长验证");
        spider.put("Googlebot-Mobile", "谷歌手机搜索");
        spider.put("Googlebot-Image", "谷歌图片搜索");
        spider.put("AppEngine-Google", "谷歌");
        spider.put("Mediapartners", "谷歌");
        spider.put("FeedBurner", "谷歌");
        spider.put("Googlebot", "谷歌");
        spider.put("Google", "谷歌");
        spider.put("google.com", "谷歌");
        spider.put("YoudaoBot", "网易有道");
        spider.put("YodaoBot", "网易有道");
        spider.put("360Spider", "360");
        spider.put("bingbot", "必应");
        spider.put("Yahoo", "雅虎");
        spider.put("Sosospider", "腾讯搜搜");
        spider.put("Sosoimagespider", "搜索图片");
        spider.put("Sogou", "搜狗蜘蛛");
        spider.put("msnbot", "MSN蜘蛛");
        spider.put("YisouSpider", "一搜蜘蛛");
        spider.put("ia_archiver", "Alexa蜘蛛");
        spider.put("EasouSpider", "宜sou蜘蛛");
        spider.put("JikeSpider", "即刻蜘蛛");
        spider.put("EtaoSpider", "一淘网蜘蛛");
        spider.put("AdsBot", "Adwords");
        spider.put("Speedy", "entireweb");
        spider.put("YandexBot", "YandexBot");
        spider.put("AhrefsBot", "AhrefsBot");
        spider.put("ezooms.bot", "ezooms.bot");
        spider.put("Java", "Java程序");
        spider.put("Mnogosearch", "MnoGoSearch搜索引擎（PHP）");
        spider.put("Morfeus Fucking Scanner", "PHP漏洞扫描器");
        spider.put("project25499", "Project 25499扫描器");
        spider.put("25499", "Project 25499扫描器");
        spider.put("James BOT", "JamesBOT搜索引擎");
        spider.put("cognitiveseo", "JamesBOT搜索引擎");
        spider.put("Iframely", "URL Meta Debugger插件");
        spider.put("muhstik-scan", "僵尸网络-挖矿软件");
        spider.put("muhstik", "僵尸网络-挖矿软件");
        spider.put("SEMrushBot", "站点分析蜘蛛");
        spider.put("python-requests", "python爬虫");
        spider.put("python", "python爬虫");
        spider.put("Test Certificate Info", "测试证书信息");
        spider.put("w3m/0.5.3+git20180125", "w3m");
        spider.put("wget", "wget");
        spider.put("curl", "curl");
        spider.put("gnu.org/gnu/wget", "wget");
        spider.put("WinHTTP", "WinHTTP");
        spider.put("WordPress", "WordPress");
        spider.put("Xenu Link Sleuth", "死链接检测工具");
        return spider;
    }
}
