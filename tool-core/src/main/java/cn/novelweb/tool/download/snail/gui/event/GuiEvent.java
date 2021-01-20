package cn.novelweb.tool.download.snail.gui.event;

import cn.novelweb.tool.download.snail.context.GuiContext;
import cn.novelweb.tool.download.snail.context.GuiContext.Mode;

/**
 * <p>GUI事件</p>
 * 
 * @author acgist
 */
public abstract class GuiEvent {

	/**
	 * <p>GUI事件类型</p>
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * <p>显示窗口</p>
		 */
		SHOW,
		/**
		 * <p>隐藏窗口</p>
		 */
		HIDE,
		/**
		 * <p>退出窗口</p>
		 */
		EXIT,
		/**
		 * <p>创建窗口</p>
		 */
		BUILD,
		/**
		 * <p>窗口消息</p>
		 */
		ALERT,
		/**
		 * <p>提示消息</p>
		 */
		NOTICE,
		/**
		 * <p>种子文件选择</p>
		 */
		TORRENT,
		/**
		 * <p>响应消息</p>
		 */
		RESPONSE,
		/**
		 * <p>刷新任务列表：添加、删除</p>
		 */
		REFRESH_TASK_LIST,
		/**
		 * <p>刷新任务状态：开始、暂停</p>
		 */
		REFRESH_TASK_STATUS;
		
	}

	/**
	 * <p>事件类型</p>
	 */
	protected final Type type;
	/**
	 * <p>事件名称</p>
	 */
	protected final String name;
	
	/**
	 * <p>GUI事件</p>
	 * 
	 * @param type 事件类型
	 * @param name 事件名称
	 */
	protected GuiEvent(Type type, String name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * <p>执行GUI事件</p>
	 * 
	 * @param mode 运行模式
	 * @param args 参数
	 */
	public final void execute(GuiContext.Mode mode, Object ... args) {
		if(mode == Mode.NATIVE) {
			this.executeNative(args);
		} else {
			this.executeExtend(args);
		}
	}
	
	/**
	 * <p>执行本地GUI事件</p>
	 * 
	 * @param args 参数
	 */
	protected abstract void executeNative(Object ... args);
	
	/**
	 * <p>执行扩展GUI事件</p>
	 * 
	 * @param args 参数
	 */
	protected abstract void executeExtend(Object ... args);
	
	/**
	 * <p>获取事件类型</p>
	 * 
	 * @return 事件类型
	 */
	public Type type() {
		return this.type;
	}
	
	/**
	 * <p>获取事件名称</p>
	 * 
	 * @return 事件名称
	 */
	public String name() {
		return this.name;
	}
	
}
