package cn.novelweb.tool.download.snail.gui.event.adapter;

import cn.novelweb.tool.download.snail.context.GuiContext;
import cn.novelweb.tool.download.snail.gui.event.GuiEvent;
import cn.novelweb.tool.download.snail.pojo.message.ApplicationMessage;

/**
 * <p>GUI隐藏窗口事件</p>
 * 
 * @author acgist
 */
public class HideEventAdapter extends GuiEvent {

	protected HideEventAdapter() {
		super(Type.HIDE, "隐藏窗口事件");
	}

	@Override
	protected void executeNative(Object ... args) {
		this.executeExtend(args);
	}
	
	@Override
	protected void executeExtend(Object ... args) {
		final ApplicationMessage message = ApplicationMessage.message(ApplicationMessage.Type.HIDE);
		GuiContext.getInstance().sendExtendGuiMessage(message);
	}
	
}
