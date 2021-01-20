package cn.novelweb.tool.download.snail.gui.event.adapter;

import cn.novelweb.tool.download.snail.context.GuiContext;
import cn.novelweb.tool.download.snail.gui.event.GuiEvent;

/**
 * <p>GUI创建窗口事件</p>
 * 
 * @author acgist
 */
public class BuildEventAdapter extends GuiEvent {
	
	protected BuildEventAdapter() {
		super(Type.BUILD, "创建窗口事件");
	}
	
	@Override
	protected void executeNative(Object ... args) {
		this.executeExtend(args);
	}

	@Override
	protected void executeExtend(Object ... args) {
		GuiContext.getInstance().lock();
	}
	
}
