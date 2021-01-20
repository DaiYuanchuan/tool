package cn.novelweb.tool.download.snail.gui.event.adapter;

import cn.novelweb.tool.download.snail.context.GuiContext;
import cn.novelweb.tool.download.snail.gui.event.GuiEvent;
import cn.novelweb.tool.download.snail.pojo.message.ApplicationMessage;

/**
 * <p>GUI刷新任务状态事件</p>
 * 
 * @author acgist
 */
public class RefreshTaskStatusEventAdapter extends GuiEvent {

	protected RefreshTaskStatusEventAdapter() {
		super(Type.REFRESH_TASK_STATUS, "刷新任务状态事件");
	}

	@Override
	protected void executeNative(Object... args) {
		this.executeExtend(args);
	}
	
	@Override
	protected void executeExtend(Object ... args) {
		final ApplicationMessage message = ApplicationMessage.message(ApplicationMessage.Type.REFRESH);
		GuiContext.getInstance().sendExtendGuiMessage(message);
	}

}
