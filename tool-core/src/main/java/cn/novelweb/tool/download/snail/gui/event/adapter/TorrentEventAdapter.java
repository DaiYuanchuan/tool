package cn.novelweb.tool.download.snail.gui.event.adapter;

import cn.novelweb.tool.download.snail.context.GuiContext;
import cn.novelweb.tool.download.snail.context.GuiContext.Mode;
import cn.novelweb.tool.download.snail.context.TorrentContext;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.context.exception.PacketSizeException;
import cn.novelweb.tool.download.snail.format.BEncodeDecoder;
import cn.novelweb.tool.download.snail.gui.event.GuiEventArgs;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.pojo.bean.Torrent;
import cn.novelweb.tool.download.snail.pojo.bean.TorrentFile;
import cn.novelweb.tool.download.snail.pojo.bean.TorrentInfo;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>GUI种子文件选择事件</p>
 * 
 * @author acgist
 */
public class TorrentEventAdapter extends GuiEventArgs {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentEventAdapter.class);
	
	protected TorrentEventAdapter() {
		super(Type.TORRENT, "种子文件选择事件");
	}

	@Override
	protected final void executeExtend(GuiContext.Mode mode, Object ... args) {
		if(!this.check(args, 1)) {
			return;
		}
		final ITaskSession taskSession = (ITaskSession) this.getArg(args, 0);
		if(mode == Mode.NATIVE) {
			this.executeNativeExtend(taskSession);
		} else {
			this.executeExtendExtend(taskSession);
		}
	}
	
	/**
	 * <p>本地消息</p>
	 * 
	 * @param taskSession 任务信息
	 */
	protected void executeNativeExtend(ITaskSession taskSession) {
		this.executeExtendExtend(taskSession);
	}
	
	/**
	 * <p>扩展消息</p>
	 * 
	 * @param taskSession 任务信息
	 */
	protected void executeExtendExtend(ITaskSession taskSession) {
		final String files = GuiContext.getInstance().files();
		if(StringUtils.isEmpty(files)) {
			LOGGER.debug("种子文件选择没有文件信息：{}", files);
			return;
		}
		try {
			final BEncodeDecoder decoder = BEncodeDecoder.newInstance(files);
			final Torrent torrent = TorrentContext.getInstance().newTorrentSession(taskSession.getTorrent()).torrent();
			// 选择文件列表
			final List<String> selectFiles = decoder.nextList().stream()
				.map(StringUtils::getString)
				.collect(Collectors.toList());
			// 选择文件大小
			final long size = torrent.getInfo().files().stream()
				.filter(file -> !file.path().startsWith(TorrentInfo.PADDING_FILE_PREFIX)) // 去掉填充文件
				.filter(file -> selectFiles.contains(file.path())) // 设置选择下载文件
				.collect(Collectors.summingLong(TorrentFile::getLength));
			taskSession.setSize(size);
			taskSession.setDescription(files);
		} catch (DownloadException | PacketSizeException e) {
			LOGGER.error("设置种子文件选择异常：{}", files, e);
		}
	}
	
}
