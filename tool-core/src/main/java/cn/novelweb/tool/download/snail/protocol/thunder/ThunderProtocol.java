package cn.novelweb.tool.download.snail.protocol.thunder;

import cn.novelweb.tool.download.snail.context.ProtocolContext;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.downloader.IDownloader;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.protocol.Protocol;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Optional;

/**
 * <p>迅雷协议</p>
 * 
 * @author acgist
 */
public final class ThunderProtocol extends Protocol {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThunderProtocol.class);
	
	private static final ThunderProtocol INSTANCE = new ThunderProtocol();
	
	public static final ThunderProtocol getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>迅雷链接拼接前缀：{@value}</p>
	 */
	private static final String THUNDER_PREFIX = "AA";
	/**
	 * <p>迅雷链接拼接后缀：{@value}</p>
	 */
	private static final String THUNDER_SUFFIX = "ZZ";

	private ThunderProtocol() {
		super(Type.THUNDER);
	}
	
	@Override
	public String name() {
		return "迅雷链接";
	}
	
	@Override
	public boolean available() {
		return true;
	}
	
	@Override
	public IDownloader buildDownloader(ITaskSession taskSession) {
		return null;
	}
	
	@Override
	public synchronized ITaskSession buildTaskSession(final String url) throws DownloadException {
		final String sourceUrl = this.sourceUrl(url);
		LOGGER.debug("迅雷原始链接：{}", sourceUrl);
		final Optional<Protocol> realProtocol = ProtocolContext.getInstance().protocol(sourceUrl);
		if(!realProtocol.isPresent()) {
			throw new DownloadException("不支持的下载链接：" + url);
		}
		return realProtocol.get().buildTaskSession(sourceUrl);
	}
	
	/**
	 * <p>迅雷链接转换原始链接</p>
	 * 
	 * @param url 迅雷链接
	 * 
	 * @return 原始链接
	 */
	public String sourceUrl(String url) {
		final String prefix = Protocol.Type.THUNDER.prefix(url);
		String sourceUrl = url.substring(prefix.length());
		sourceUrl = new String(Base64.getMimeDecoder().decode(sourceUrl)); // getMimeDecoder防止长度非4的整数倍导致的异常
		sourceUrl = sourceUrl.substring(THUNDER_PREFIX.length(), sourceUrl.length() - THUNDER_SUFFIX.length());
		return sourceUrl;
	}
	
	/**
	 * <p>原始链接转换迅雷链接</p>
	 * 
	 * @param url 原始链接
	 * 
	 * @return 迅雷链接
	 */
	public String thunderUrl(String url) {
		String thunderUrl = THUNDER_PREFIX + url + THUNDER_SUFFIX;
		thunderUrl = Base64.getMimeEncoder().encodeToString(thunderUrl.getBytes());
		thunderUrl = Protocol.Type.THUNDER.defaultPrefix() + thunderUrl;
		return StringUtils.trimAllBlank(thunderUrl);
	}
	
}
