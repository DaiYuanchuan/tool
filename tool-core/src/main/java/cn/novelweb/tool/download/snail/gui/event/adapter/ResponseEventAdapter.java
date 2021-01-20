package cn.novelweb.tool.download.snail.gui.event.adapter;

import cn.novelweb.tool.download.snail.context.GuiContext;
import cn.novelweb.tool.download.snail.context.GuiContext.Mode;
import cn.novelweb.tool.download.snail.gui.event.GuiEvent;
import cn.novelweb.tool.download.snail.gui.event.GuiEventArgs;
import cn.novelweb.tool.download.snail.pojo.message.ApplicationMessage;

/**
 * <p>GUI响应消息事件</p>
 * 
 * @author acgist
 */
public class ResponseEventAdapter extends GuiEventArgs {

	protected ResponseEventAdapter() {
		super(GuiEvent.Type.RESPONSE, "响应消息事件");
	}

	@Override
	protected final void executeExtend(Mode mode, Object... args) {
		if(!this.check(args, 1)) {
			return;
		}
		final String message = (String) this.getArg(args, 0);
		if(mode == Mode.NATIVE) {
			this.executeNativeExtend(message);
		} else {
			this.executeExtendExtend(message);
		}
	}

	/**
	 * <p>本地消息</p>
	 * 
	 * @param message 消息
	 */
	protected void executeNativeExtend(String message) {
		this.executeExtendExtend(message);
	}
	
	/**
	 * <p>扩展消息</p>
	 * 
	 * @param message 消息
	 */
	protected void executeExtendExtend(String message) {
		final ApplicationMessage applicationMessage = ApplicationMessage.message(ApplicationMessage.Type.RESPONSE, message);
		GuiContext.getInstance().sendExtendGuiMessage(applicationMessage);
	}
	
}
