package cn.novelweb.tool.download.snail.context.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.config.TrackerConfig;
import cn.novelweb.tool.download.snail.context.TrackerContext;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.net.torrent.tracker.TrackerServer;

/**
 * <p>初始化Tracker</p>
 * 
 * @author acgist
 */
public final class TrackerInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerInitializer.class);
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private TrackerInitializer() {
	}
	
	public static TrackerInitializer newInstance() {
		return new TrackerInitializer();
	}
	
	@Override
	protected void init() throws DownloadException {
		LOGGER.debug("初始化Tracker");
		TrackerConfig.getInstance();
		TrackerServer.getInstance();
		TrackerContext.getInstance();
	}

}
