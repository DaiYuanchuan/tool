package cn.novelweb.tool.download.snail.pojo;

/**
 * <p>获取速度（上传、下载）接口</p>
 * 
 * @author acgist
 */
public interface ISpeedGetter {

	/**
	 * <p>获取上传速度</p>
	 * 
	 * @return 上传速度
	 */
	long uploadSpeed();
	
	/**
	 * <p>获取下载速度</p>
	 * 
	 * @return 下载速度
	 */
	long downloadSpeed();
	
}
