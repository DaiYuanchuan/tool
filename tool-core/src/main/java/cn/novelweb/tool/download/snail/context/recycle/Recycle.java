package cn.novelweb.tool.download.snail.context.recycle;

import cn.novelweb.tool.download.snail.utils.StringUtils;

import java.io.File;

/**
 * <p>回收站</p>
 * 
 * @author acgist
 */
public abstract class Recycle {

	/**
	 * <p>删除文件路径</p>
	 * <p>必须是完整路径（不能填写相对路径）</p>
	 */
	protected final String path;
	/**
	 * <p>删除文件</p>
	 */
	protected final File file;
	
	/**
	 * @param path 文件路径
	 */
	protected Recycle(String path) {
		if(StringUtils.isEmpty(path)) {
			throw new IllegalArgumentException("删除文件路径错误：" + path);
		}
		this.path = path;
		this.file = new File(path);
	}
	
	/**
	 * <p>删除文件</p>
	 * 
	 * @return 是否删除成功
	 */
	public abstract boolean delete();

}
