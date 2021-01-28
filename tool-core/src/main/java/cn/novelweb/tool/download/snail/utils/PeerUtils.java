package cn.novelweb.tool.download.snail.utils;

import cn.novelweb.tool.download.snail.config.SystemConfig;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Peer工具</p>
 * 
 * @author acgist
 */
public final class PeerUtils {
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private PeerUtils() {
	}
	
	/**
	 * <p>allowedFast固定值：{@value}</p>
	 */
	private static final int ALLOWED_FAST_MASK = 0xFFFFFF00;
	/**
	 * <p>allowedFast快速允许Piece长度（k）：{@value}</p>
	 */
	private static final int ALLOWED_FAST_K = 10;
	/**
	 * <p>allowedFast快速允许循环长度：{@value}</p>
	 */
	private static final int ALLOWED_FAST_LOOP_LENGTH = 5;

	/**
	 * @param bytes 数据
	 * 
	 * @return IP=端口
	 * 
	 * @see #read(ByteBuffer)
	 */
	public static Map<String, Integer> read(byte[] bytes) {
		if(bytes == null) {
			return null;
		}
		final ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return read(buffer);
	}
	
	/**
	 * <p>读取IP和端口</p>
	 * 
	 * @param buffer 数据
	 * 
	 * @return IP=端口
	 */
	public static Map<String, Integer> read(ByteBuffer buffer) {
		if(buffer == null) {
			return null;
		}
		final Map<String, Integer> data = new HashMap<>();
		while (buffer.remaining() >= SystemConfig.IP_PORT_LENGTH) {
			final String ip = NetUtils.intToIP(buffer.getInt());
			final int port = NetUtils.portToInt(buffer.getShort());
			data.put(ip, port);
		}
		return data;
	}
	
	/**
	 * <p>计算快速允许Piece索引</p>
	 * <p>协议链接：http://www.bittorrent.org/beps/bep_0006.html</p>
	 * 
	 * @param pieceSize Piece数量：种子Piece总量
	 * @param ipAddress IP地址：Peer地址
	 * @param infoHash InfoHash
	 * 
	 * @return 快速允许Piece索引
	 */
	public static int[] allowedFast(int pieceSize, String ipAddress, byte[] infoHash) {
		final int ipValue = NetUtils.ipToInt(ipAddress);
		ByteBuffer buffer = ByteBuffer.allocate(24); // IP(4) + InfoHash(20)
		buffer.putInt(ALLOWED_FAST_MASK & ipValue);
		buffer.put(infoHash);
		int size = 0;
		// 选择数据长度
		final int length = Math.min(ALLOWED_FAST_K, pieceSize);
		final int[] seqs = new int[length];
		while(size < length) {
			buffer = ByteBuffer.wrap(StringUtils.sha1(buffer.array()));
			for (int index = 0; index < ALLOWED_FAST_LOOP_LENGTH && size < length; index++) {
				final int seq = (int) (Integer.toUnsignedLong(buffer.getInt()) % pieceSize);
				if(ArrayUtils.indexOf(seqs, 0, size, seq) == ArrayUtils.NONE_INDEX) {
					seqs[size++] = seq;
				}
			}
		}
		return seqs;
	}
	
	/**
	 * <p>HTTP传输编码（PeerId、InfoHashHex）</p>
	 * <p>全部编码：忽略不用编码字符</p>
	 * 
	 * @param hex PeerId、InfoHashHex
	 * 
	 * @return HTTP传输编码结果
	 */
	public static String urlEncode(String hex) {
		int index = 0;
		final int length = hex.length();
		final StringBuilder builder = new StringBuilder();
		do {
			builder.append("%").append(hex.substring(index, index + 2));
			index += 2;
		} while (index < length);
		return builder.toString();
	}
	
	/**
	 * <p>HTTP传输编码（PeerId、InfoHash）</p>
	 * <p>不用编码字符：0-9、a-z、A-Z、'.'、'-'、'_'、'~'</p>
	 * <p>协议链接：https://wiki.theory.org/index.php/BitTorrentSpecification#Tracker_HTTP.2FHTTPS_Protocol</p>
	 * 
	 * @param bytes PeerId、InfoHash
	 * 
	 * @return HTTP传输编码结果
	 */
	public static String urlEncode(byte[] bytes) {
		char value;
		String valueHex;
		final StringBuilder builder = new StringBuilder();
		for (int index = 0; index < bytes.length; index++) {
			value = (char) bytes[index];
			if(
				(value >= '0' && value <= '9') ||
				(value >= 'a' && value <= 'z') ||
				(value >= 'A' && value <= 'Z') ||
				value == '.' ||
				value == '-' ||
				value == '_' ||
				value == '~'
			) {
				builder.append(value);
			} else {
				valueHex = Integer.toHexString(value & 0xFF);
				builder.append("%");
				if(valueHex.length() < 2) {
					builder.append("0");
				}
				builder.append(valueHex);
			}
		}
		return builder.toString();
	}
	
}
