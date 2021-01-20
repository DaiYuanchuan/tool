package cn.novelweb.tool.download.snail.pojo;

import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.pojo.ITaskSessionStatus.Status;

/**
 * <p>任务信息操作接口</p>
 * 
 * @author acgist
 */
public interface ITaskSessionHandler {

	/**
	 * <p>重置状态</p>
	 * <p>如果软件没有正常关闭，重置任务状态。</p>
	 */
	void reset();
	
	/**
	 * <p>开始下载任务</p>
	 * <p>添加下载任务并开始下载</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	void start() throws DownloadException;

	/**
	 * <p>重新添加下载</p>
	 * <p>删除旧下载器，重新开始下载。</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	void restart() throws DownloadException;
	
	/**
	 * <p>等待下载</p>
	 */
	void await();
	
	/**
	 * <p>暂停任务</p>
	 */
	void pause();
	
	/**
	 * <p>重新暂停任务</p>
	 * <p>任务已经完成校验失败：暂停任务</p>
	 */
	void repause();
	
	/**
	 * <p>删除任务</p>
	 */
	void delete();
	
	/**
	 * <p>刷新任务</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	void refresh() throws DownloadException;

	/**
	 * <p>校验下载文件</p>
	 * 
	 * @return 校验结果
	 * 
	 * @throws DownloadException 下载异常
	 */
	boolean verify() throws DownloadException;
	
	/**
	 * <p>释放下载锁</p>
	 */
	void unlockDownload();
	
	/**
	 * <p>更新实体</p>
	 */
	void update();
	
	/**
	 * <p>更新状态</p>
	 * <p>如果任务完成不会更新</p>
	 * 
	 * @param status 任务状态
	 */
	void updateStatus(Status status);
	
}
