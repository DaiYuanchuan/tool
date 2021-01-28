package cn.novelweb.tool.download.snail.format;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.format.BEncodeDecoder.Type;

/**
 * <p>B编码编码器</p>
 * <p>支持数据类型：Number、String、byte[]</p>
 * <pre>
 * encoder
 * 	.newList().put("1").put("2").flush()
 * 	.newMap().put("a", "b").put("c", "d").flush()
 * 	.toString();
 * 
 * encoder
 * 	.write(List.of("a", "b"))
 * 	.write(Map.of("1", "2"))
 * 	.toString();
 * </pre>
 * 
 * @author acgist
 */
public final class BEncodeEncoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(BEncodeEncoder.class);
	
	/**
	 * <p>List</p>
	 */
	private List<Object> list;
	/**
	 * <p>Map</p>
	 */
	private Map<String, Object> map;
	/**
	 * <p>数据类型</p>
	 */
	private BEncodeDecoder.Type type;
	/**
	 * <p>输出数据（不需要关闭）</p>
	 */
	private final ByteArrayOutputStream outputStream;
	
	private BEncodeEncoder() {
		this.outputStream = new ByteArrayOutputStream();
	}
	
	public static BEncodeEncoder newInstance() {
		return new BEncodeEncoder();
	}

	/**
	 * <p>新建List</p>
	 * 
	 * @return {@link BEncodeEncoder}
	 */
	public BEncodeEncoder newList() {
		this.type = Type.LIST;
		this.list = new ArrayList<>();
		return this;
	}
	
	/**
	 * <p>新建Map</p>
	 * 
	 * @return {@link BEncodeEncoder}
	 */
	public BEncodeEncoder newMap() {
		this.type = Type.MAP;
		this.map = new LinkedHashMap<>();
		return this;
	}
	
	/**
	 * <p>向List中添加数据</p>
	 * 
	 * @param value 数据
	 * 
	 * @return {@link BEncodeEncoder}
	 */
	public BEncodeEncoder put(Object value) {
		if(this.type == Type.LIST) {
			this.list.add(value);
		}
		return this;
	}
	
	/**
	 * <p>向List中添加数据</p>
	 * 
	 * @param list 数据
	 * 
	 * @return {@link BEncodeEncoder}
	 */
	public BEncodeEncoder put(List<?> list) {
		if(this.type == Type.LIST) {
			this.list.addAll(list);
		}
		return this;
	}
	
	/**
	 * <p>向Map中添加数据</p>
	 * 
	 * @param key 键
	 * @param value 值
	 * 
	 * @return {@link BEncodeEncoder}
	 */
	public BEncodeEncoder put(String key, Object value) {
		if(this.type == Type.MAP) {
			this.map.put(key, value);
		}
		return this;
	}
	
	/**
	 * <p>向Map中添加数据</p>
	 * 
	 * @param map 数据
	 * 
	 * @return {@link BEncodeEncoder}
	 */
	public BEncodeEncoder put(Map<String, Object> map) {
		if(this.type == Type.MAP) {
			this.map.putAll(map);
		}
		return this;
	}

	/**
	 * <p>将数据写入字符流</p>
	 * 
	 * @return {@link BEncodeEncoder}
	 */
	public BEncodeEncoder flush() {
		if(this.type == Type.MAP) {
			this.writeMap(this.map);
		} else if(this.type == Type.LIST) {
			this.writeList(this.list);
		} else {
			LOGGER.warn("B编码错误（未知类型）：{}", this.type);
		}
		return this;
	}
	
	/**
	 * <p>写入字节数组</p>
	 * 
	 * @param bytes 数据
	 * 
	 * @return {@link BEncodeEncoder}
	 */
	public BEncodeEncoder write(byte[] bytes) {
		try {
			if(bytes != null) {
				this.outputStream.write(bytes);
			}
		} catch (IOException e) {
			LOGGER.error("B编码输出异常", e);
		}
		return this;
	}
	
	/**
	 * <p>写入字符</p>
	 * 
	 * @param value 数据
	 */
	private void write(char value) {
		this.outputStream.write(value);
	}
	
	/**
	 * <p>写入B编码List</p>
	 * 
	 * @param list 数据
	 * 
	 * @return {@link BEncodeEncoder}
	 */
	private BEncodeEncoder writeList(List<?> list) {
		if(list == null) {
			return this;
		}
		this.write(BEncodeDecoder.TYPE_L);
		list.forEach(this::writeObject);
		this.write(BEncodeDecoder.TYPE_E);
		return this;
	}
	
	/**
	 * <p>写入B编码Map</p>
	 * 
	 * @param map 数据
	 * 
	 * @return {@link BEncodeEncoder}
	 */
	private BEncodeEncoder writeMap(Map<?, ?> map) {
		if(map == null) {
			return this;
		}
		this.write(BEncodeDecoder.TYPE_D);
		map.forEach((key, value) -> {
			this.writeObject(key);
			this.writeObject(value);
		});
		this.write(BEncodeDecoder.TYPE_E);
		return this;
	}
	
	/**
	 * <p>写入B编码数据</p>
	 * 
	 * @param value 数据
	 */
	private void writeObject(Object value) {
		if(value instanceof String) {
			this.writeBytes(((String) value).getBytes());
		} else if(value instanceof Number) {
			this.writeNumber((Number) value);
		} else if(value instanceof byte[]) {
			this.writeBytes((byte[]) value);
		} else if(value instanceof Map) {
			this.writeMap((Map<?, ?>) value);
		} else if(value instanceof List) {
			this.writeList((List<?>) value);
		} else if(value == null) {
			this.writeBytes(new byte[0]);
		} else {
			this.writeBytes(value.toString().getBytes());
		}
	}
	
	/**
	 * <p>写入B编码数值</p>
	 * 
	 * @param number 数据
	 */
	private void writeNumber(Number number) {
		this.write(BEncodeDecoder.TYPE_I);
		this.write(number.toString().getBytes());
		this.write(BEncodeDecoder.TYPE_E);
	}
	
	/**
	 * <p>写入B编码字节数组</p>
	 * 
	 * @param bytes 数据
	 */
	private void writeBytes(byte[] bytes) {
		this.write(String.valueOf(bytes.length).getBytes());
		this.write(BEncodeDecoder.SEPARATOR);
		this.write(bytes);
	}
	
	/**
	 * <p>获取字符数据</p>
	 * 
	 * @return 字符数据
	 */
	public byte[] bytes() {
		return this.outputStream.toByteArray();
	}

	/**
	 * <p>将数据转为字符串</p>
	 * 
	 * @return 字符串
	 */
	@Override
	public String toString() {
		return new String(this.bytes());
	}
	
	/**
	 * <p>List转为B编码字节数组</p>
	 * 
	 * @param list List
	 * 
	 * @return B编码字节数组
	 */
	public static byte[] encodeList(List<?> list) {
		return newInstance().writeList(list).bytes();
	}
	
	/**
	 * <p>List转为B编码字符串</p>
	 * 
	 * @param list List
	 * 
	 * @return B编码字符串
	 */
	public static String encodeListString(List<?> list) {
		return new String(encodeList(list));
	}
	
	/**
	 * <p>Map转为B编码字节数组</p>
	 * 
	 * @param map Map
	 * 
	 * @return B编码字节数组
	 */
	public static byte[] encodeMap(Map<?, ?> map) {
		return newInstance().writeMap(map).bytes();
	}
	
	/**
	 * <p>Map转为B编码字符串</p>
	 * 
	 * @param map Map
	 * 
	 * @return B编码字符串
	 */
	public static String encodeMapString(Map<?, ?> map) {
		return new String(encodeMap(map));
	}
	
}
