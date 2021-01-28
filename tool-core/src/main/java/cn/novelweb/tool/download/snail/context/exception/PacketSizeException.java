package cn.novelweb.tool.download.snail.context.exception;

import cn.novelweb.tool.download.snail.config.SystemConfig;

/**
 * <p>网络包大小异常</p>
 * 
 * @see SystemConfig#MAX_NET_BUFFER_LENGTH
 * 
 * @author acgist
 */
public final class PacketSizeException extends NetException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * <p>验证网络包大小</p>
	 * 
	 * @param length 网络包大小
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see SystemConfig#MAX_NET_BUFFER_LENGTH
	 */
	public static void verify(short length) throws PacketSizeException {
		if(length < 0 || length > SystemConfig.MAX_NET_BUFFER_LENGTH) {
			throw new PacketSizeException(length);
		}
	}
	
	/**
	 * <p>验证网络包大小</p>
	 * 
	 * @param length 网络包大小
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see SystemConfig#MAX_NET_BUFFER_LENGTH
	 */
	public static void verify(int length) throws PacketSizeException {
		if(length < 0 || length > SystemConfig.MAX_NET_BUFFER_LENGTH) {
			throw new PacketSizeException(length);
		}
	}

	public PacketSizeException() {
		super("网络包大小异常");
	}
	
	/**
	 * @param size 网络包大小
	 */
	public PacketSizeException(int size) {
		super("网络包大小错误：" + size);
	}

	/**
	 * @param message 错误信息
	 */
	public PacketSizeException(String message) {
		super(message);
	}

	/**
	 * @param cause 原始异常
	 */
	public PacketSizeException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public PacketSizeException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
