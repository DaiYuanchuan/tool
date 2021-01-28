package cn.novelweb.tool.download.snail.utils;

import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * <p>URL工具</p>
 * 
 * @author acgist
 */
public final class UrlUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(UrlUtils.class);
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private UrlUtils() {
	}
	
	/**
	 * <p>URL编码</p>
	 * 
	 * @param content 待编码内容
	 * 
	 * @return 编码后内容
	 */
	public static String encode(String content) {
		if(StringUtils.isEmpty(content)) {
			return content;
		}
		try {
			return URLEncoder
				.encode(content, SystemConfig.DEFAULT_CHARSET)
				.replace("+", "%20"); // 空格编码变成加号：加号解码变成空格
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("URL编码异常：{}", content, e);
		}
		return content;
	}
	
	/**
	 * <p>URL解码</p>
	 * 
	 * @param content 待解码内容
	 * 
	 * @return 解码后内容
	 */
	public static String decode(String content) {
		if(StringUtils.isEmpty(content)) {
			return content;
		}
		try {
			return URLDecoder.decode(content, SystemConfig.DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("URL解码异常：{}", content, e);
		}
		return content;
	}
	
	/**
	 * <p>获取跳转链接完整路径</p>
	 * <p>支持协议：HTTP</p>
	 * 
	 * @param source 原始页面链接
	 * @param target 目标页面链接
	 * 
	 * @return 完整链接
	 */
	public static String redirect(final String source, String target) {
		Objects.requireNonNull(source, "原始页面链接不能为空");
		Objects.requireNonNull(target, "目标页面链接不能为空");
		// 去掉引号
		target = target.trim();
		if(target.startsWith("\"")) {
			target = target.substring(1);
		}
		if(target.endsWith("\"")) {
			target = target.substring(0, target.length() - 1);
		}
		// 执行跳转
		if(Protocol.Type.HTTP.verify(target)) {
			// 完整连接
			return target;
		} else if(target.startsWith("/")) {
			// 绝对目录链接
			final String prefix = Protocol.Type.HTTP.prefix(source);
			final int index = source.indexOf('/', prefix.length());
			if(index > prefix.length()) {
				return source.substring(0, index) + target;
			} else {
				return source + target;
			}
		} else {
			// 相对目录链接
			final String prefix = Protocol.Type.HTTP.prefix(source);
			final int index = source.lastIndexOf('/');
			if(index > prefix.length()) {
				return source.substring(0, index) + "/" + target;
			} else {
				return source + "/" + target;
			}
		}
	}
	
}
