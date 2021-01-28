package cn.novelweb.tool.download.snail.context.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.context.NatContext;

/**
 * <p>初始化NAT</p>
 * 
 * @author acgist
 */
public final class NatInitializer extends Initializer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NatInitializer.class);
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private NatInitializer() {
	}
	
	public static NatInitializer newInstance() {
		return new NatInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.debug("初始化NAT");
		NatContext.getInstance().register();
	}

}
