package cn.novelweb.tool.download.snail.gui.event.adapter;

import cn.novelweb.tool.download.snail.context.GuiContext;
import cn.novelweb.tool.download.snail.gui.event.GuiEvent;

/**
 * <p>GUI退出窗口事件</p>
 * 
 * @author acgist
 */
public class ExitEventAdapter extends GuiEvent {

	protected ExitEventAdapter() {
		super(Type.EXIT, "退出窗口事件");
	}

	@Override
	protected void executeNative(Object ... args) {
		this.executeExtend(args);
	}
	
	@Override
	protected void executeExtend(Object ... args) {
		GuiContext.getInstance().unlock();
	}
	
}
