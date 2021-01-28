package cn.novelweb.tool.download.snail.context;

import cn.novelweb.tool.download.snail.IContext;
import cn.novelweb.tool.download.snail.pojo.ISpeedGetter;
import cn.novelweb.tool.download.snail.pojo.IStatisticsSession;
import cn.novelweb.tool.download.snail.pojo.IStatisticsSessionGetter;
import cn.novelweb.tool.download.snail.pojo.session.StatisticsSession;

/**
 * <p>系统统计上下文</p>
 * <p>系统统计：累计下载、累计上传、速度采样</p>
 * <p>系统只限制单个任务的速度，如果需要限制整个系统的速度，可以打开{@link #statistics}限速。</p>
 * 
 * @author acgist
 */
public final class StatisticsContext implements IContext, ISpeedGetter, IStatisticsSessionGetter {
	
	private static final StatisticsContext INSTANCE = new StatisticsContext();
	
	public static StatisticsContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>系统全局统计</p>
	 */
	private IStatisticsSession statistics;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private StatisticsContext() {
		this.statistics = new StatisticsSession();
	}
	
	@Override
	public IStatisticsSession statistics() {
		return this.statistics;
	}
	
	@Override
	public long uploadSpeed() {
		return this.statistics.uploadSpeed();
	}
	
	@Override
	public long downloadSpeed() {
		return this.statistics.downloadSpeed();
	}

}
