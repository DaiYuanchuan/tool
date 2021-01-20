package cn.novelweb.tool.download.snail.net.application;

import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.GuiContext;
import cn.novelweb.tool.download.snail.context.GuiContext.Mode;
import cn.novelweb.tool.download.snail.context.SystemContext;
import cn.novelweb.tool.download.snail.context.TaskContext;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.context.exception.PacketSizeException;
import cn.novelweb.tool.download.snail.format.BEncodeDecoder;
import cn.novelweb.tool.download.snail.format.BEncodeEncoder;
import cn.novelweb.tool.download.snail.net.TcpMessageHandler;
import cn.novelweb.tool.download.snail.net.codec.IMessageCodec;
import cn.novelweb.tool.download.snail.net.codec.LineMessageCodec;
import cn.novelweb.tool.download.snail.net.codec.StringMessageCodec;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.pojo.message.ApplicationMessage;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>系统消息代理</p>
 * 
 * @author acgist
 */
public final class ApplicationMessageHandler extends TcpMessageHandler implements IMessageCodec<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMessageHandler.class);
	
	/**
	 * <p>多条消息分隔符：{@value}</p>
	 */
	private static final String SEPARATOR = SystemConfig.LINE_SEPARATOR_COMPAT;
	
	public ApplicationMessageHandler() {
		final LineMessageCodec lineMessageCodec = new LineMessageCodec(this, SEPARATOR);
		final StringMessageCodec stringMessageCodec = new StringMessageCodec(lineMessageCodec);
		this.messageCodec = stringMessageCodec;
	}
	
	/**
	 * <p>发送系统消息</p>
	 * 
	 * @param message 系统消息
	 */
	public void send(ApplicationMessage message) {
		try {
			this.send(message.toString());
		} catch (NetException e) {
			LOGGER.error("发送系统消息异常", e);
		}
	}
	
	@Override
	public void send(String message, String charset) throws NetException {
		super.send(this.messageCodec.encode(message), charset);
	}
	
	@Override
	public void onMessage(String message) {
		message = message.trim();
		if(StringUtils.isEmpty(message)) {
			LOGGER.warn("系统消息错误：{}", message);
			return;
		}
		final ApplicationMessage applicationMessage = ApplicationMessage.valueOf(message);
		if(applicationMessage == null) {
			LOGGER.warn("系统消息错误（格式）：{}", message);
			return;
		}
		this.execute(applicationMessage);
	}
	
	/**
	 * <p>处理系统消息</p>
	 * 
	 * @param message 系统消息
	 */
	private void execute(ApplicationMessage message) {
		if(message.getType() == null) {
			LOGGER.warn("系统消息错误（未知类型）：{}", message.getType());
			return;
		}
		LOGGER.debug("处理系统消息：{}", message);
		switch (message.getType()) {
		case GUI:
			this.onGui();
			break;
		case TEXT:
			this.onText(message);
			break;
		case CLOSE:
			this.onClose();
			break;
		case NOTIFY:
			this.onNotify();
			break;
		case SHUTDOWN:
			this.onShutdown();
			break;
		case TASK_NEW:
			this.onTaskNew(message);
			break;
		case TASK_LIST:
			this.onTaskList();
			break;
		case TASK_START:
			this.onTaskStart(message);
			break;
		case TASK_PAUSE:
			this.onTaskPause(message);
			break;
		case TASK_DELETE:
			this.onTaskDelete(message);
			break;
		case SHOW:
			this.onShow();
			break;
		case HIDE:
			this.onHide();
			break;
		case ALERT:
			this.onAlert(message);
			break;
		case NOTICE:
			this.onNotice(message);
			break;
		case REFRESH:
			this.onRefresh();
			break;
		case RESPONSE:
			this.onResponse(message);
			break;
		default:
			LOGGER.warn("系统消息错误（类型未适配）：{}", message.getType());
			break;
		}
	}
	
	/**
	 * <p>GUI注册</p>
	 * <p>将当前连接的消息代理注册为GUI消息代理，需要使用{@linkplain Mode#EXTEND 后台模式}启动。</p>
	 */
	private void onGui() {
		final boolean success = GuiContext.getInstance().extendGuiMessageHandler(this);
		if(success) {
			this.send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
		} else {
			this.send(ApplicationMessage.response(ApplicationMessage.FAIL));
		}
	}

	/**
	 * <p>文本消息</p>
	 * <p>原样返回文本</p>
	 * 
	 * @param message 系统消息
	 */
	private void onText(ApplicationMessage message) {
		this.send(ApplicationMessage.response(message.getBody()));
	}
	
	/**
	 * <p>关闭连接</p>
	 */
	private void onClose() {
		this.close();
	}
	
	/**
	 * <p>唤醒窗口</p>
	 */
	private void onNotify() {
		GuiContext.getInstance().show();
	}
	
	/**
	 * <p>关闭程序</p>
	 */
	private void onShutdown() {
		SystemContext.shutdown();
	}
	
	/**
	 * <p>新建任务</p>
	 * <dl>
	 * 	<dt>body：Map（B编码）</dt>
	 * 	<dd>url：下载链接</dd>
	 * 	<dd>files：种子文件选择列表（B编码）</dd>
	 * </dl>
	 * 
	 * @param message 系统消息
	 */
	private void onTaskNew(ApplicationMessage message) {
		final String body = message.getBody();
		try {
			final BEncodeDecoder decoder = BEncodeDecoder.newInstance(body);
			decoder.nextMap();
			if(decoder.isEmpty()) { // 空数据返回失败
				this.send(ApplicationMessage.response(ApplicationMessage.FAIL));
				return;
			}
			final String url = decoder.getString("url");
			final String files = decoder.getString("files");
			synchronized (this) {
				GuiContext.getInstance().files(files); // 设置选择文件
				TaskContext.getInstance().download(url); // 开始下载任务
			}
			this.send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
		} catch (NetException | DownloadException e) {
			LOGGER.debug("新建任务异常：{}", body, e);
			this.send(ApplicationMessage.response(e.getMessage()));
		}
	}

	/**
	 * <p>任务列表</p>
	 * <p>返回任务列表（B编码）</p>
	 */
	private void onTaskList() {
		final List<Map<String, Object>> list = TaskContext.getInstance().allTask().stream()
			.map(ITaskSession::taskMessage)
			.collect(Collectors.toList());
		final String body = BEncodeEncoder.encodeListString(list);
		this.send(ApplicationMessage.response(body));
	}

	/**
	 * <p>开始任务</p>
	 * <p>body：任务ID</p>
	 * 
	 * @param message 系统消息
	 */
	private void onTaskStart(ApplicationMessage message) {
		final Optional<ITaskSession> optional = this.selectTaskSession(message);
		if(!optional.isPresent()) {
			this.send(ApplicationMessage.response(ApplicationMessage.FAIL));
		} else {
			try {
				optional.get().start();
				this.send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
			} catch (DownloadException e) {
				this.send(ApplicationMessage.response(e.getMessage()));
			}
		}
	}
	
	/**
	 * <p>暂停任务</p>
	 * <p>body：任务ID</p>
	 * 
	 * @param message 系统消息
	 */
	private void onTaskPause(ApplicationMessage message) {
		final Optional<ITaskSession> optional = this.selectTaskSession(message);
		if(!optional.isPresent()) {
			this.send(ApplicationMessage.response(ApplicationMessage.FAIL));
		} else {
			optional.get().pause();
			this.send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
		}
	}
	
	/**
	 * <p>删除任务</p>
	 * <p>body：任务ID</p>
	 * 
	 * @param message 系统消息
	 */
	private void onTaskDelete(ApplicationMessage message) {
		final Optional<ITaskSession> optional = this.selectTaskSession(message);
		if(!optional.isPresent()) {
			this.send(ApplicationMessage.response(ApplicationMessage.FAIL));
		} else {
			optional.get().delete();
			this.send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
		}
	}
	
	/**
	 * <p>显示窗口</p>
	 */
	private void onShow() {
		GuiContext.getInstance().show();
	}
	
	/**
	 * <p>隐藏窗口</p>
	 */
	private void onHide() {
		GuiContext.getInstance().hide();
	}
	
	/**
	 * <p>提示窗口</p>
	 * 
	 * @param message 系统消息
	 */
	private void onAlert(ApplicationMessage message) {
		final String body = message.getBody();
		final BEncodeDecoder decoder = BEncodeDecoder.newInstance(body);
		try {
			decoder.nextMap();
			final String type = decoder.getString("type");
			final String title = decoder.getString("title");
			final String content = decoder.getString("message");
			GuiContext.getInstance().alert(title, content, GuiContext.MessageType.valueOf(type));
		} catch (PacketSizeException e) {
			LOGGER.warn("处理提示窗口异常", e);
		}
	}
	
	/**
	 * <p>提示消息</p>
	 * 
	 * @param message 系统消息
	 */
	private void onNotice(ApplicationMessage message) {
		final String body = message.getBody();
		final BEncodeDecoder decoder = BEncodeDecoder.newInstance(body);
		try {
			decoder.nextMap();
			final String type = decoder.getString("type");
			final String title = decoder.getString("title");
			final String content = decoder.getString("message");
			GuiContext.getInstance().notice(title, content, GuiContext.MessageType.valueOf(type));
		} catch (PacketSizeException e) {
			LOGGER.warn("处理提示消息异常", e);
		}
	}
	
	/**
	 * <p>刷新任务</p>
	 */
	private void onRefresh() {
		GuiContext.getInstance().refreshTaskList();
	}
	
	/**
	 * <p>响应消息</p>
	 * 
	 * @param message 系统消息
	 */
	private void onResponse(ApplicationMessage message) {
		GuiContext.getInstance().response(message.getBody());
	}
	
	/**
	 * <p>获取任务信息</p>
	 * <p>body：任务ID</p>
	 * 
	 * @param message 系统消息
	 * 
	 * @return 任务信息
	 */
	private Optional<ITaskSession> selectTaskSession(ApplicationMessage message) {
		final String body = message.getBody(); // 任务ID
		return TaskContext.getInstance().allTask().stream()
			.filter(session -> session.getId().equals(body))
			.findFirst();
	}

}
