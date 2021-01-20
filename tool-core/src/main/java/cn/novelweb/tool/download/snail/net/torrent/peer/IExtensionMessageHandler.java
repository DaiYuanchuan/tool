package cn.novelweb.tool.download.snail.net.torrent.peer;

import java.nio.ByteBuffer;

import cn.novelweb.tool.download.snail.context.exception.NetException;

/**
 * <p>扩展协议接口</p>
 * 
 * @author acgist
 */
public interface IExtensionMessageHandler {

	/**
	 * <p>处理扩展消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 */
	void onMessage(ByteBuffer buffer) throws NetException;
	
}
