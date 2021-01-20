package cn.novelweb.tool.download.snail.gui.event.adapter;

import cn.novelweb.tool.download.snail.gui.event.GuiEventMessage;
import cn.novelweb.tool.download.snail.pojo.message.ApplicationMessage;

/**
 * <p>GUI窗口消息事件</p>
 * 
 * @author acgist
 */
public class AlertEventAdapter extends GuiEventMessage {

	protected AlertEventAdapter() {
		super(Type.ALERT, "窗口消息事件", ApplicationMessage.Type.ALERT);
	}

}
