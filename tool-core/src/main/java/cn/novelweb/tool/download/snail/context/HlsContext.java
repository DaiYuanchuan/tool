package cn.novelweb.tool.download.snail.context;

import cn.novelweb.tool.download.snail.IContext;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.pojo.bean.M3u8;
import cn.novelweb.tool.download.snail.pojo.session.HlsSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>HLS上下文</p>
 * 
 * @author acgist
 */
public final class HlsContext implements IContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(HlsContext.class);
	
	private static final HlsContext INSTANCE = new HlsContext();
	
	public static HlsContext getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>M3U8</p>
	 * <p>任务ID=M3U8</p>
	 */
	private final Map<String, M3u8> m3u8s;
	/**
	 * <p>HLS任务信息</p>
	 * <p>任务ID=HLS任务信息</p>
	 */
	private final Map<String, HlsSession> sessions;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private HlsContext() {
		this.m3u8s = new ConcurrentHashMap<>();
		this.sessions = new ConcurrentHashMap<>();
	}

	/**
	 * <p>设置M3U8</p>
	 * 
	 * @param id 任务ID
	 * @param m3u8 M3U8
	 */
	public void m3u8(String id, M3u8 m3u8) {
		if(id != null && m3u8 != null) {
			this.m3u8s.put(id, m3u8);
		}
	}
	
	/**
	 * <p>获取HLS任务信息</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return HLS任务信息
	 */
	public HlsSession hlsSession(ITaskSession taskSession) {
		final String id = taskSession.getId();
		final M3u8 m3u8 = this.m3u8s.get(id);
		return this.sessions.computeIfAbsent(id, key -> HlsSession.newInstance(m3u8, taskSession));
	}
	
	/**
	 * <p>删除HLS任务信息</p>
	 * 
	 * @param taskSession HLS任务信息
	 */
	public void remove(ITaskSession taskSession) {
		LOGGER.debug("移除HLS任务：{}", taskSession.getName());
		final String id = taskSession.getId();
		this.m3u8s.remove(id);
		this.sessions.remove(id);
	}
	
}
