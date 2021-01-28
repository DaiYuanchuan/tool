package cn.novelweb.tool.download.snail.context.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.config.DhtConfig;
import cn.novelweb.tool.download.snail.context.DhtContext;
import cn.novelweb.tool.download.snail.context.NodeContext;

/**
 * <p>初始化DHT</p>
 * 
 * @author acgist
 */
public final class DhtInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DhtInitializer.class);
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private DhtInitializer() {
	}
	
	public static DhtInitializer newInstance() {
		return new DhtInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.debug("初始化DHT");
		DhtConfig.getInstance();
		DhtContext.getInstance();
		NodeContext.getInstance();
	}

}
