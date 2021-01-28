package cn.novelweb.tool.download.snail.pojo.wrapper;

import cn.novelweb.tool.download.snail.context.exception.PacketSizeException;
import cn.novelweb.tool.download.snail.format.BEncodeDecoder;
import cn.novelweb.tool.download.snail.format.BEncodeEncoder;
import cn.novelweb.tool.download.snail.utils.CollectionUtils;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>多文件选择包装器</p>
 * 
 * @author acgist
 */
public final class MultifileSelectorWrapper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MultifileSelectorWrapper.class);

	/**
	 * <p>编码器</p>
	 */
	private BEncodeEncoder encoder;
	/**
	 * <p>解码器</p>
	 */
	private BEncodeDecoder decoder;

	private MultifileSelectorWrapper() {
	}

	/**
	 * <p>创建编码器</p>
	 * 
	 * @param list 选择文件列表
	 * 
	 * @return 包装器
	 */
	public static MultifileSelectorWrapper newEncoder(List<String> list) {
		final MultifileSelectorWrapper wrapper = new MultifileSelectorWrapper();
		if(CollectionUtils.isNotEmpty(list)) {
			wrapper.encoder = BEncodeEncoder.newInstance();
			wrapper.encoder.newList().put(list);
		}
		return wrapper;
	}
	
	/**
	 * <p>解析器</p>
	 * 
	 * @param value 选择文件列表（B编码）
	 * 
	 * @return 包装器
	 */
	public static MultifileSelectorWrapper newDecoder(String value) {
		final MultifileSelectorWrapper wrapper = new MultifileSelectorWrapper();
		if(StringUtils.isNotEmpty(value)) {
			wrapper.decoder = BEncodeDecoder.newInstance(value);
		}
		return wrapper;
	}
	
	/**
	 * <p>编码选择文件</p>
	 * 
	 * @return 选择文件列表（B编码）
	 */
	public String serialize() {
		if(this.encoder == null) {
			return null;
		}
		return encoder.flush().toString();
	}

	/**
	 * <p>解析选择文件</p>
	 * 
	 * @return 选择文件列表
	 */
	public List<String> deserialize() {
		if(this.decoder == null) {
			return new ArrayList<>();
		}
		try {
			return this.decoder.nextList().stream()
				.filter(Objects::nonNull)
				.map(object -> StringUtils.getString(object))
				.collect(Collectors.toList());
		} catch (PacketSizeException e) {
			LOGGER.error("解析选择文件异常", e);
		}
		return new ArrayList<>();
	}

}
