package cn.novelweb.tool.download.snail.format;

import cn.novelweb.tool.download.snail.context.exception.PacketSizeException;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>B编码解码器</p>
 * 
 * <table border="1">
 * 	<caption>类型</caption>
 * 	<tr>
 * 		<th>符号</th>
 * 		<th>类型</th>
 * 	</tr>
 * 	<tr>
 * 		<td align="center">{@code i}</td>
 * 		<td>数值：{@link Long}</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="center">{@code l}</td>
 * 		<td>列表：{@link List}</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="center">{@code d}</td>
 * 		<td>字典：{@link Map}</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="center">{@code e}</td>
 * 		<td>结尾</td>
 * 	</tr>
 * </table>
 * <p>所有类型除了Long，其他均为byte[]，需要自己进行类型转换。</p>
 * <p>使用以下方法进行解析：{@link #nextType()}、{@link #nextMap()}、{@link #nextList()}</p>
 * 
 * @author acgist
 */
public final class BEncodeDecoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(BEncodeDecoder.class);
	
	/**
	 * <p>B编码数据类型</p>
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * <p>Map</p>
		 */
		MAP,
		/**
		 * <p>List</p>
		 */
		LIST,
		/**
		 * <p>未知</p>
		 */
		NONE;
		
	}
	
	/**
	 * <p>结尾：{@value}</p>
	 */
	public static char TYPE_E = 'e';
	/**
	 * <p>数值：{@value}</p>
	 */
	public static char TYPE_I = 'i';
	/**
	 * <p>List：{@value}</p>
	 */
	public static char TYPE_L = 'l';
	/**
	 * <p>Map：{@value}</p>
	 */
	public static char TYPE_D = 'd';
	/**
	 * <p>分隔符：{@value}</p>
	 */
	public static char SEPARATOR = ':';
	
	/**
	 * <p>数据类型</p>
	 */
	private Type type;
	/**
	 * <p>List</p>
	 */
	private List<Object> list;
	/**
	 * <p>Map</p>
	 */
	private Map<String, Object> map;
	/**
	 * <p>原始数据（不需要关闭）</p>
	 */
	private final ByteArrayInputStream inputStream;
	
	/**
	 * @param bytes 数据
	 */
	private BEncodeDecoder(byte[] bytes) {
		Objects.requireNonNull(bytes, "B编码内容错误");
		if(bytes.length < 2) {
			throw new IllegalArgumentException("B编码内容错误");
		}
		this.inputStream = new ByteArrayInputStream(bytes);
	}
	
	/**
	 * <p>创建B编码解码器</p>
	 * 
	 * @param bytes 数据
	 * 
	 * @return B编码解码器
	 */
	public static BEncodeDecoder newInstance(byte[] bytes) {
		return new BEncodeDecoder(bytes);
	}
	
	/**
	 * <p>创建B编码解码器</p>
	 * 
	 * @param content 数据
	 * 
	 * @return B编码解码器
	 */
	public static BEncodeDecoder newInstance(String content) {
		Objects.requireNonNull(content, "B编码内容错误");
		return new BEncodeDecoder(content.getBytes());
	}
	
	/**
	 * <p>创建B编码解码器</p>
	 * 
	 * @param buffer 数据
	 * 
	 * @return B编码解码器
	 */
	public static BEncodeDecoder newInstance(ByteBuffer buffer) {
		Objects.requireNonNull(buffer, "B编码内容错误");
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return new BEncodeDecoder(bytes);
	}
	
	/**
	 * <p>判断是否没有数据</p>
	 * 
	 * @return 是否没有数据
	 */
	public boolean isEmpty() {
		if(this.type == Type.LIST) {
			return this.list == null;
		} else if(this.type == Type.MAP) {
			return this.map == null;
		} else {
			return true;
		}
	}
	
	/**
	 * <p>判断是否含有数据</p>
	 * 
	 * @return 是否含有数据
	 */
	public boolean isNotEmpty() {
		return !this.isEmpty();
	}
	
	/**
	 * <p>获取下一个数据类型</p>
	 * <p>同时解析下一个数据</p>
	 * 
	 * @return 下一个数据类型
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	public Type nextType() throws PacketSizeException {
		// 是否含有数据
		final boolean noneData = this.inputStream == null || this.inputStream.available() <= 0;
		if(noneData) {
			LOGGER.warn("B编码没有数据");
			this.type = Type.NONE;
			return this.type;
		}
		final char charType = (char) this.inputStream.read();
		switch (charType) {
		case TYPE_D:
			this.map = readMap(this.inputStream);
			this.type = Type.MAP;
			break;
		case TYPE_L:
			this.list = readList(this.inputStream);
			this.type = Type.LIST;
			break;
		default:
			LOGGER.warn("B编码错误（未知类型）：{}", charType);
			this.type = Type.NONE;
			break;
		}
		return this.type;
	}
	
	/**
	 * <p>获取下一个List</p>
	 * <p>如果下一个数据类型不是{@link List}返回空{@link List}</p>
	 * 
	 * @return 下一个List
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	public List<Object> nextList() throws PacketSizeException {
		final BEncodeDecoder.Type nextType = this.nextType();
		if(nextType == Type.LIST) {
			return this.list;
		}
		return Collections.emptyList();
	}
	
	/**
	 * <p>获取下一个Map</p>
	 * <p>如果下一个数据类型不是{@link Map}返回空{@link Map}</p>
	 * 
	 * @return 下一个Map
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	public Map<String, Object> nextMap() throws PacketSizeException {
		final BEncodeDecoder.Type nextType = this.nextType();
		if(nextType == Type.MAP) {
			return this.map;
		}
		return Collections.emptyMap();
	}
	
	/**
	 * <p>读取剩余所有数据</p>
	 * 
	 * @return 剩余所有数据
	 */
	public byte[] oddBytes() {
		if(this.inputStream == null) {
			return new byte[0];
		}
		return inputStream2byte(this.inputStream);
	}

	/**
	 * 功能描述:
	 *
	 * @param inputStream 输入流
	 * @return byte[] 数组
	 * @author xiaobu
	 * @date 2019/3/28 16:03
	 * @version 1.0
	 */
	@SneakyThrows
	public static byte[] inputStream2byte(InputStream inputStream) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int rc = 0;
		while ((rc = inputStream.read(buff, 0, 1024)) > 0) {
			byteArrayOutputStream.write(buff, 0, rc);
		}
		return byteArrayOutputStream.toByteArray();
	}


	/**
	 * <p>读取剩余所有数据并转为字符串</p>
	 * 
	 * @return 剩余所有数据字符串
	 */
	public String oddString() {
		final byte[] bytes = this.oddBytes();
		return new String(bytes);
	}

	/**
	 * <p>读取数值</p>
	 * 
	 * @param inputStream 数据
	 * 
	 * @return 数值
	 * 
	 * @see #TYPE_I
	 */
	private static final Long readLong(ByteArrayInputStream inputStream) {
		int index;
		char indexChar;
		final StringBuilder valueBuilder = new StringBuilder();
		while((index = inputStream.read()) != -1) {
			indexChar = (char) index;
			if(indexChar == TYPE_E) {
				final String number = valueBuilder.toString();
				if(!StringUtils.isNumeric(number)) {
					throw new IllegalArgumentException("B编码错误（数值）：" + number);
				}
				return Long.valueOf(number);
			} else {
				valueBuilder.append(indexChar);
			}
		}
		return 0L;
	}
	
	/**
	 * <p>读取List</p>
	 * 
	 * @param inputStream 数据
	 * 
	 * @return List
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see #TYPE_L
	 */
	private static final List<Object> readList(ByteArrayInputStream inputStream) throws PacketSizeException {
		int index;
		char indexChar;
		final List<Object> list = new ArrayList<>();
		final StringBuilder lengthBuilder = new StringBuilder();
		while ((index = inputStream.read()) != -1) {
			indexChar = (char) index;
			switch (indexChar) {
			case TYPE_E:
				return list;
			case TYPE_I:
				list.add(readLong(inputStream));
				break;
			case TYPE_L:
				list.add(readList(inputStream));
				break;
			case TYPE_D:
				list.add(readMap(inputStream));
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				lengthBuilder.append(indexChar);
				break;
			case SEPARATOR:
				if(lengthBuilder.length() > 0) {
					final byte[] bytes = readBytes(lengthBuilder, inputStream);
					list.add(bytes);
				} else {
					LOGGER.warn("B编码错误（长度）：{}", lengthBuilder);
				}
				break;
			default:
				LOGGER.warn("B编码错误（未知类型）：{}", indexChar);
				break;
			}
		}
		return list;
	}
	
	/**
	 * <p>读取Map</p>
	 * 
	 * @param inputStream 数据
	 * 
	 * @return Map
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see #TYPE_D
	 */
	private static final Map<String, Object> readMap(ByteArrayInputStream inputStream) throws PacketSizeException {
		int index;
		char indexChar;
		String key = null;
		final Map<String, Object> map = new LinkedHashMap<>();
		final StringBuilder lengthBuilder = new StringBuilder();
		while ((index = inputStream.read()) != -1) {
			indexChar = (char) index;
			switch (indexChar) {
			case TYPE_E:
				return map;
			case TYPE_I:
				if(key != null) {
					map.put(key, readLong(inputStream));
					key = null;
				} else {
					LOGGER.warn("B编码key为空跳过（I）");
				}
				break;
			case TYPE_L:
				if(key != null) {
					map.put(key, readList(inputStream));
					key = null;
				} else {
					LOGGER.warn("B编码key为空跳过（L）");
				}
				break;
			case TYPE_D:
				if(key != null) {
					map.put(key, readMap(inputStream));
					key = null;
				} else {
					LOGGER.warn("B编码key为空跳过（D）");
				}
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				lengthBuilder.append(indexChar);
				break;
			case SEPARATOR:
				if(lengthBuilder.length() > 0) {
					final byte[] bytes = readBytes(lengthBuilder, inputStream);
					if (key == null) {
						key = new String(bytes);
					} else {
						map.put(key, bytes);
						key = null;
					}
				} else {
					LOGGER.warn("B编码错误（长度）：{}", lengthBuilder);
				}
				break;
			default:
				LOGGER.warn("B编码错误（未知类型）：{}", indexChar);
				break;
			}
		}
		return map;
	}
	
	/**
	 * <p>读取符合长度的字节数组</p>
	 * 
	 * @param lengthBuilder 字节数组长度
	 * @param inputStream 数据
	 * 
	 * @return 字节数组
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	private static final byte[] readBytes(StringBuilder lengthBuilder, ByteArrayInputStream inputStream) throws PacketSizeException {
		final String number = lengthBuilder.toString();
		if(!StringUtils.isNumeric(number)) {
			throw new IllegalArgumentException("B编码错误（数值）：" + number);
		}
		final int length = Integer.parseInt(number);
		PacketSizeException.verify(length);
		lengthBuilder.setLength(0); // 清空长度
		final byte[] bytes = new byte[length];
		try {
			final int readLength = inputStream.read(bytes);
			if(readLength != length) {
				LOGGER.warn("B编码错误（读取长度和实际长度不符）：{}-{}", length, readLength);
			}
		} catch (IOException e) {
			LOGGER.error("B编码读取异常", e);
		}
		return bytes;
	}
	
	/**
	 * <p>获取对象</p>
	 * 
	 * @param key 键
	 * 
	 * @return 对象
	 */
	public Object get(String key) {
		return get(this.map, key);
	}
	
	/**
	 * <p>获取对象</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 对象
	 */
	public static Object get(Map<?, ?> map, String key) {
		if(map == null) {
			return null;
		}
		return map.get(key);
	}
	
	/**
	 * <p>获取字节</p>
	 * 
	 * @param key 键
	 * 
	 * @return 字节
	 */
	public Byte getByte(String key) {
		return getByte(this.map, key);
	}
	
	/**
	 * <p>获取字节</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 字节
	 */
	public static Byte getByte(Map<?, ?> map, String key) {
		final Long value = getLong(map, key);
		if(value == null) {
			return null;
		}
		return value.byteValue();
	}
	
	/**
	 * <p>获取数值</p>
	 * 
	 * @param key 键
	 * 
	 * @return 数值
	 */
	public Integer getInteger(String key) {
		return getInteger(this.map, key);
	}
	
	/**
	 * <p>获取数值</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 数值
	 */
	public static Integer getInteger(Map<?, ?> map, String key) {
		final Long value = getLong(map, key);
		if(value == null) {
			return null;
		}
		return value.intValue();
	}
	
	/**
	 * <p>获取数值</p>
	 * 
	 * @param key 键
	 * 
	 * @return 数值
	 */
	public Long getLong(String key) {
		return getLong(this.map, key);
	}
	
	/**
	 * <p>获取数值</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 数值
	 */
	public static Long getLong(Map<?, ?> map, String key) {
		if(map == null) {
			return null;
		}
		return (Long) map.get(key);
	}
	
	/**
	 * <p>获取字符串</p>
	 * 
	 * @param key 键
	 * 
	 * @return 字符串
	 */
	public String getString(String key) {
		return getString(this.map, key);
	}
	
	/**
	 * <p>获取字符串</p>
	 * 
	 * @param key 键
	 * @param encoding 编码
	 * 
	 * @return 字符串
	 */
	public String getString(String key, String encoding) {
		return getString(this.map, key, encoding);
	}
	
	/**
	 * <p>获取字符串</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 字符串
	 */
	public static String getString(Map<?, ?> map, String key) {
		return getString(map, key, null);
	}
	
	/**
	 * <p>获取字符串</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * @param encoding 编码
	 * 
	 * @return 字符串
	 */
	public static String getString(Map<?, ?> map, String key, String encoding) {
		final byte[] bytes = getBytes(map, key);
		if(bytes == null) {
			return null;
		}
		return StringUtils.getStringCharset(bytes, encoding);
	}
	
	/**
	 * <p>获取字符数组</p>
	 * 
	 * @param key 键
	 * 
	 * @return 字符数组
	 */
	public byte[] getBytes(String key) {
		return getBytes(this.map, key);
	}
	
	/**
	 * <p>获取字符数组</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 字符数组
	 */
	public static byte[] getBytes(Map<?, ?> map, String key) {
		if(map == null) {
			return null;
		}
		return (byte[]) map.get(key);
	}
	
	/**
	 * <p>获取集合</p>
	 * 
	 * @param key 键
	 * 
	 * @return 集合
	 */
	public List<Object> getList(String key) {
		return getList(this.map, key);
	}
	
	/**
	 * <p>获取集合</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 集合
	 */
	public static List<Object> getList(Map<?, ?> map, String key) {
		if(map == null) {
			return Collections.emptyList();
		}
		final List<?> result = (List<?>) map.get(key);
		if(result == null) {
			return Collections.emptyList();
		}
		return result.stream()
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>获取Map</p>
	 * 
	 * @param key 键
	 * 
	 * @return Map
	 */
	public Map<String, Object> getMap(String key) {
		return getMap(this.map, key);
	}
	
	/**
	 * <p>获取Map</p>
	 * <p>使用LinkedHashMap防止乱序（乱序后计算的Hash值将会改变）</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return Map
	 */
	public static Map<String, Object> getMap(Map<?, ?> map, String key) {
		if(map == null) {
			return Collections.emptyMap();
		}
		final Map<?, ?> result = (Map<?, ?>) map.get(key);
		if(result == null) {
			return Collections.emptyMap();
		}
		Map<String, Object> maps = new LinkedHashMap<>();
		result.forEach((k, v) -> {
			if (k != null) {
				maps.put(k.toString(), v);
			}
		});
		return maps;
	}
	
}
