package cn.novelweb.tool.download.snail.context;

import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.IContext;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.gui.event.GuiEvent;
import cn.novelweb.tool.download.snail.gui.event.GuiEvent.Type;
import cn.novelweb.tool.download.snail.net.IMessageSender;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.pojo.message.ApplicationMessage;
import cn.novelweb.tool.download.snail.utils.StringUtils;

/**
 * <p>GUI上下文</p>
 * 
 * @author acgist
 */
public final class GuiContext implements IContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GuiContext.class);
	
	private static final GuiContext INSTANCE = new GuiContext();
	
	public static final GuiContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>运行模式</p>
	 * 
	 * @author acgist
	 */
	public enum Mode {
		
		/**
		 * <p>本地模式：本地GUI</p>
		 * <p>本地GUI：JavaFX</p>
		 */
		NATIVE,
		/**
		 * <p>后台模式：扩展GUI</p>
		 * <p>扩展GUI：自定义实现，通过系统消息和系统通知来完成系统管理和任务管理。</p>
		 * 
		 * @see cn.novelweb.tool.download.snail.pojo.message.ApplicationMessage.Type
		 */
		EXTEND;
		
	}
	
	/**
	 * <p>消息类型</p>
	 * 
	 * @author acgist
	 */
	public enum MessageType {
		
		/**
		 * <p>普通</p>
		 */
		NONE,
		/**
		 * <p>提示</p>
		 */
		INFO,
		/**
		 * <p>警告</p>
		 */
		WARN,
		/**
		 * <p>确认</p>
		 */
		CONFIRM,
		/**
		 * <p>错误</p>
		 */
		ERROR;
		
	}
	
	/**
	 * <p>启动参数：运行模式</p>
	 */
	private static final String ARGS_MODE = "mode";
	
	/**
	 * <p>运行模式</p>
	 * <p>默认：本地GUI</p>
	 */
	private Mode mode = Mode.NATIVE;
	/**
	 * <p>种子文件选择列表（B编码）</p>
	 */
	private String files;
	/**
	 * <p>扩展GUI阻塞锁</p>
	 * <p>使用扩展GUI时阻止程序关闭</p>
	 */
	private final Object lock = new Object();
	/**
	 * <p>事件Map</p>
	 * <p>事件类型=事件</p>
	 */
	private final Map<GuiEvent.Type, GuiEvent> events = new EnumMap<>(GuiEvent.Type.class);
	/**
	 * <p>扩展GUI消息代理</p>
	 */
	private IMessageSender extendGuiMessageSender;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private GuiContext() {
	}
	
	/**
	 * <p>注册GUI事件</p>
	 * 
	 * @param event GUI事件
	 */
	public static final void register(GuiEvent event) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("注册GUI事件：{}-{}", event.type(), event.name());
		}
		INSTANCE.events.put(event.type(), event);
	}

	/**
	 * <p>初始化GUI上下文</p>
	 * 
	 * @param args 启动参数
	 * 
	 * @return GuiContext
	 */
	public GuiContext init(String ... args) {
		if(args == null) {
			// 没有参数
		} else {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info("启动参数：{}", String.join(",", args));
			}
			String value;
			for (String arg : args) {
				// 运行模式
				value = StringUtils.argValue(arg, ARGS_MODE);
				if(Mode.EXTEND.name().equalsIgnoreCase(value)) {
					this.mode = Mode.EXTEND;
				}
			}
			LOGGER.info("运行模式：{}", this.mode);
		}
		return this;
	}
	
	/**
	 * <p>显示窗口</p>
	 * 
	 * @return GuiContext
	 */
	public GuiContext show() {
		return this.event(Type.SHOW);
	}
	
	/**
	 * <p>隐藏窗口</p>
	 * 
	 * @return GuiContext
	 */
	public GuiContext hide() {
		return this.event(Type.HIDE);
	}
	
	/**
	 * <p>退出窗口</p>
	 * 
	 * @return GuiContext
	 */
	public GuiContext exit() {
		return this.event(Type.EXIT);
	}

	/**
	 * <p>创建窗口</p>
	 * 
	 * @return GuiContext
	 */
	public GuiContext build() {
		return this.event(Type.BUILD);
	}
	
	/**
	 * <p>窗口消息</p>
	 * 
	 * @param title 标题
	 * @param message 内容
	 * 
	 * @return GuiContext
	 */
	public GuiContext alert(String title, String message) {
		return this.alert(title, message, GuiContext.MessageType.INFO);
	}

	/**
	 * <p>窗口消息</p>
	 * 
	 * @param title 标题
	 * @param message 内容
	 * @param type 类型
	 * 
	 * @return GuiContext
	 */
	public GuiContext alert(String title, String message, GuiContext.MessageType type) {
		return this.event(Type.ALERT, title, message, type);
	}
	
	/**
	 * <p>提示消息</p>
	 * 
	 * @param title 标题
	 * @param message 内容
	 * 
	 * @return GuiContext
	 */
	public GuiContext notice(String title, String message) {
		return this.notice(title, message, GuiContext.MessageType.INFO);
	}
	
	/**
	 * <p>提示消息</p>
	 * 
	 * @param title 标题
	 * @param message 内容
	 * @param type 类型
	 * 
	 * @return GuiContext
	 */
	public GuiContext notice(String title, String message, GuiContext.MessageType type) {
		return this.event(Type.NOTICE, title, message, type);
	}
	
	/**
	 * <p>种子文件选择</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return GuiContext
	 */
	public GuiContext torrent(ITaskSession taskSession) {
		return this.event(Type.TORRENT, taskSession);
	}
	
	/**
	 * <p>响应消息</p>
	 * 
	 * @param message 消息
	 * 
	 * @return GuiContext
	 */
	public GuiContext response(String message) {
		return this.event(Type.RESPONSE, message);
	}
	
	/**
	 * <p>刷新任务列表</p>
	 * 
	 * @return GuiContext
	 */
	public GuiContext refreshTaskList() {
		return this.event(Type.REFRESH_TASK_LIST);
	}
	
	/**
	 * <p>刷新任务状态</p>
	 * 
	 * @return GuiContext
	 */
	public GuiContext refreshTaskStatus() {
		return this.event(Type.REFRESH_TASK_STATUS);
	}

	/**
	 * <p>执行事件</p>
	 * 
	 * @param type 类型
	 * @param args 参数
	 * 
	 * @return GuiContext
	 */
	public GuiContext event(GuiEvent.Type type, Object ... args) {
		if(type == null) {
			LOGGER.warn("未知GUI事件：{}", type);
			return this;
		}
		final GuiEvent event = this.events.get(type);
		if(event == null) {
			LOGGER.warn("GUI事件没有注册：{}", type);
			return this;
		}
		event.execute(this.mode, args);
		return this;
	}
	
	/**
	 * <p>获取种子文件选择列表</p>
	 * 
	 * @return 种子文件选择列表（B编码）
	 */
	public String files() {
		return this.files;
	}
	
	/**
	 * <p>设置种子文件选择列表</p>
	 * 
	 * @param files 种子文件选择列表（B编码）
	 */
	public void files(String files) {
		this.files = files;
	}
	
	/**
	 * <p>注册扩展GUI消息代理</p>
	 * 
	 * @param extendGuiMessageSender 扩展GUI消息代理
	 * 
	 * @return 是否注册成功
	 */
	public boolean extendGuiMessageHandler(IMessageSender extendGuiMessageSender) {
		if(this.mode == Mode.NATIVE) {
			LOGGER.debug("已经启用本地GUI：忽略注册扩展GUI消息代理");
			return false;
		} else {
			LOGGER.debug("注册扩展GUI消息代理");
			this.extendGuiMessageSender = extendGuiMessageSender;
			return true;
		}
	}
	
	/**
	 * <p>发送扩展GUI消息</p>
	 * 
	 * @param message 扩展GUI消息
	 */
	public void sendExtendGuiMessage(ApplicationMessage message) {
		if(message == null) {
			LOGGER.warn("扩展GUI消息错误：{}", message);
			return;
		}
		if(this.extendGuiMessageSender != null) {
			try {
				this.extendGuiMessageSender.send(message.toString());
			} catch (NetException e) {
				LOGGER.error("发送扩展GUI消息异常", e);
			}
		} else {
			LOGGER.warn("扩展GUI消息代理没有注册");
		}
	}
	
	/**
	 * <p>添加扩展GUI阻塞锁</p>
	 */
	public void lock() {
		synchronized (this.lock) {
			try {
				this.lock.wait(Long.MAX_VALUE);
			} catch (InterruptedException e) {
				LOGGER.debug("线程等待异常", e);
				Thread.currentThread().interrupt();
			}
		}
	}
	
	/**
	 * <p>释放扩展GUI阻塞锁</p>
	 */
	public void unlock() {
		synchronized (this.lock) {
			this.lock.notifyAll();
		}
	}

}
