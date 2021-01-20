package cn.novelweb.tool.download.snail.gui.event.adapter;

import cn.novelweb.tool.download.snail.gui.event.GuiEventMessage;
import cn.novelweb.tool.download.snail.pojo.message.ApplicationMessage;

/**
 * <p>GUI提示消息事件</p>
 * 
 * @author acgist
 */
public class NoticeEventAdapter extends GuiEventMessage {

	protected NoticeEventAdapter() {
		super(Type.NOTICE, "提示消息事件", ApplicationMessage.Type.NOTICE);
	}

}
