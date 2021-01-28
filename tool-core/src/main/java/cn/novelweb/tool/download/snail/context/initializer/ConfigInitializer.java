package cn.novelweb.tool.download.snail.context.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.config.DownloadConfig;
import cn.novelweb.tool.download.snail.config.SystemConfig;

/**
 * <p>初始化配置</p>
 * 
 * @author acgist
 */
public final class ConfigInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigInitializer.class);
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private ConfigInitializer() {
	}
	
	public static ConfigInitializer newInstance() {
		return new ConfigInitializer();
	}

	@Override
	protected void init() {
		LOGGER.debug("初始化配置");
		SystemConfig.getInstance();
		DownloadConfig.getInstance();
	}

}
