package cn.novelweb.tool.download.snail.protocol.hls;

import cn.novelweb.tool.download.snail.context.HlsContext;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.downloader.IDownloader;
import cn.novelweb.tool.download.snail.downloader.hls.HlsDownloader;
import cn.novelweb.tool.download.snail.net.http.HttpClient;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.pojo.bean.M3u8;
import cn.novelweb.tool.download.snail.pojo.wrapper.MultifileSelectorWrapper;
import cn.novelweb.tool.download.snail.protocol.Protocol;
import cn.novelweb.tool.download.snail.utils.FileUtils;

/**
 * <p>HLS协议</p>
 * <p>协议链接：https://tools.ietf.org/html/rfc8216</p>
 * 
 * @author acgist
 */
public final class HlsProtocol extends Protocol {

//	private static final Logger LOGGER = LoggerFactory.getLogger(HlsProtocol.class);
	
	private static final HlsProtocol INSTANCE = new HlsProtocol();
	
	public static HlsProtocol getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>M3U8信息</p>
	 */
	private M3u8 m3u8;
	
	private HlsProtocol() {
		super(Type.HLS);
	}

	@Override
	public String name() {
		return "HLS";
	}

	@Override
	public boolean available() {
		return true;
	}

	@Override
	public IDownloader buildDownloader(ITaskSession taskSession) {
		return HlsDownloader.newInstance(taskSession);
	}
	
	@Override
	protected void prep() throws DownloadException {
		try {
			this.buildM3u8();
		} catch (NetException e) {
			throw new DownloadException("网络异常", e);
		}
	}
	
	@Override
	protected void buildSize() throws DownloadException {
		this.taskEntity.setSize(0L);
	}
	
	@Override
	protected void done() throws DownloadException {
		this.buildFolder();
		this.selectFiles();
	}

	/**
	 * <p>获取M3U8信息</p>
	 * 
	 * @throws NetException 网络异常
	 * @throws DownloadException 下载异常
	 */
	private void buildM3u8() throws NetException, DownloadException {
		final String response = HttpClient
			.newInstance(this.url)
			.get()
			.responseToString();
		final M3u8 m3u8 = M3u8Builder.newInstance(response, this.url).build();
		if(m3u8.getType() == M3u8.Type.M3U8) {
			this.url = m3u8.maxRateLink();
			this.buildM3u8();
		} else if(m3u8.getType() == M3u8.Type.STREAM) {
			throw new DownloadException("不支持直播流媒体下载");
		} else {
			this.m3u8 = m3u8;
		}
	}
	
	/**
	 * <p>创建下载目录</p>
	 */
	private void buildFolder() {
		FileUtils.buildFolder(this.taskEntity.getFile(), false);
	}
	
	/**
	 * <p>保持下载文件列表</p>
	 */
	private void selectFiles() {
		final MultifileSelectorWrapper wrapper = MultifileSelectorWrapper.newEncoder(this.m3u8.getLinks());
		this.taskEntity.setDescription(wrapper.serialize());
	}
	
	@Override
	protected void release(boolean success) {
		if(success) {
			// 成功添加管理
			HlsContext.getInstance().m3u8(this.taskEntity.getId(), this.m3u8);
		}
		super.release(success);
		this.m3u8 = null;
	}

}
