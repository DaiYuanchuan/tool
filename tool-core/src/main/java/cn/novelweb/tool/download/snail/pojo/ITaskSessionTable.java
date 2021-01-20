package cn.novelweb.tool.download.snail.pojo;

/**
 * <p>任务信息面板数据绑定接口</p>
 * 
 * @author acgist
 */
public interface ITaskSessionTable {

	/**
	 * @return 任务名称
	 */
	String getNameValue();

	/**
	 * @return 任务状态
	 */
	String getStatusValue();
	
	/**
	 * @return 任务进度
	 */
	String getProgressValue();

	/**
	 * @return 创建时间
	 */
	String getCreateDateValue();
	
	/**
	 * @return 完成时间
	 */
	String getEndDateValue();
	
}
