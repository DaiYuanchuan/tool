package cn.novelweb.tool.download.snail.gui.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.context.GuiContext;
import cn.novelweb.tool.download.snail.context.GuiContext.Mode;

/**
 * <p>GUI变长参数事件</p>
 * 
 * @author acgist
 */
public abstract class GuiEventArgs extends GuiEvent {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GuiEventArgs.class);

	/**
	 * <p>GUI事件扩展</p>
	 * 
	 * @param type 事件类型
	 * @param name 事件名称
	 */
	protected GuiEventArgs(Type type, String name) {
		super(type, name);
	}
	
	@Override
	protected final void executeNative(Object ... args) {
		this.executeExtend(Mode.NATIVE, args);
	}

	@Override
	protected final void executeExtend(Object ... args) {
		this.executeExtend(Mode.EXTEND, args);
	}
	
	/**
	 * <p>校验参数</p>
	 * 
	 * @param args 参数
	 * @param length 参数长度
	 * 
	 * @return 是否校验成功
	 */
	protected final boolean check(Object[] args, int length) {
		return this.check(args, length, length);
	}
	
	/**
	 * <p>校验参数</p>
	 * 
	 * @param args 参数
	 * @param minLength 最小参数长度
	 * @param maxLength 最大参数长度
	 * 
	 * @return 是否校验成功
	 */
	protected final boolean check(Object[] args, int minLength, int maxLength) {
		if(args == null) {
			LOGGER.warn("{}参数格式错误：{}", args);
			return false;
		}
		if(args.length < minLength || args.length > maxLength) {
			LOGGER.warn("{}参数格式错误（长度）：{}", args);
			return false;
		}
		return true;
	}
	
	/**
	 * <p>获取参数</p>
	 * 
	 * @param args 参数
	 * @param index 参数序号
	 * 
	 * @return 参数
	 */
	protected final Object getArg(Object[] args, int index) {
		return this.getArg(args, index, null);
	}
	
	/**
	 * <p>获取参数</p>
	 * 
	 * @param args 参数
	 * @param index 参数序号
	 * @param defaultValue 默认值
	 * 
	 * @return 参数
	 */
	protected final Object getArg(Object[] args, int index, Object defaultValue) {
		return args.length > index ? args[index] : defaultValue;
	}
	
	/**
	 * <p>执行变长参数GUI事件</p>
	 * 
	 * @param mode 运行模式
	 * @param args 变长参数
	 */
	protected abstract void executeExtend(GuiContext.Mode mode, Object ... args);

}
