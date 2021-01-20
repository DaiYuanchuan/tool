package cn.novelweb.tool.download.snail.gui.event.adapter;

import cn.novelweb.tool.download.snail.context.GuiContext;
import cn.novelweb.tool.download.snail.gui.event.GuiEvent;
import cn.novelweb.tool.download.snail.pojo.message.ApplicationMessage;

/**
 * <p>GUI显示窗口事件</p>
 * 
 * @author acgist
 */
public class ShowEventAdapter extends GuiEvent {

	protected ShowEventAdapter() {
		super(Type.SHOW, "显示窗口事件");
	}
	
	@Override
	protected void executeNative(Object... args) {
		this.executeExtend(args);
	}

	@Override
	protected void executeExtend(Object ... args) {
		final ApplicationMessage message = ApplicationMessage.message(ApplicationMessage.Type.SHOW);
		GuiContext.getInstance().sendExtendGuiMessage(message);
	}

}
