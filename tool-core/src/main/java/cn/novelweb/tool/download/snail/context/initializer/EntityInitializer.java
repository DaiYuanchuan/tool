package cn.novelweb.tool.download.snail.context.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.context.EntityContext;

/**
 * <p>初始化实体上下文</p>
 * 
 * @author acgist
 */
public final class EntityInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(EntityInitializer.class);
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private EntityInitializer() {
	}
	
	public static EntityInitializer newInstance() {
		return new EntityInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.debug("初始化实体上下文");
		EntityContext.getInstance().load();
	}
	
}
