package cn.novelweb.tool.download.snail.net.codec;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.utils.StringUtils;

/**
 * <p>字符串消息处理器</p>
 * 
 * @author acgist
 */
public final class StringMessageCodec extends MessageCodec<ByteBuffer, String> {

	/**
	 * @param messageCodec 下一个消息处理器
	 */
	public StringMessageCodec(IMessageCodec<String> messageCodec) {
		super(messageCodec);
	}

	@Override
	protected void doDecode(ByteBuffer buffer, InetSocketAddress address) throws NetException {
		final String message = StringUtils.ofByteBuffer(buffer); // 读取字符串
		this.doNext(message, address);
	}

}
