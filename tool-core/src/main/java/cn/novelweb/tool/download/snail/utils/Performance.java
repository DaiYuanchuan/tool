package cn.novelweb.tool.download.snail.utils;

import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.SystemThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>性能分析工具</p>
 * 
 * @author acgist
 */
public abstract class Performance {

	protected static final Logger LOGGER = LoggerFactory.getLogger(Performance.class);
	
	/**
	 * <p>是否跳过执行方法：{@value}</p>
	 */
	private static final String SKIP_SKIP = "skip";
	/**
	 * <p>是否跳过执行方法：{@value}</p>
	 */
	private static final String SKIP_TRUE = "true";
	/**
	 * <p>是否跳过执行方法</p>
	 * <p>跳过执行方法：性能测试、测试时间过长</p>
	 */
	protected static final boolean SKIP;
	
	static {
		final String skip = System.getProperty(SKIP_SKIP);
		SKIP = SKIP_SKIP.equalsIgnoreCase(skip) || SKIP_TRUE.equalsIgnoreCase(skip);
	}
	
	/**
	 * <p>消耗时间统计</p>
	 */
	protected final AtomicLong costTime = new AtomicLong();
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param message 日志信息
	 */
	protected final void log(Object message) {
		this.log(null, message);
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param message 日志信息
	 * @param args 日志参数
	 */
	protected final void log(String message, Object ... args) {
		if(message == null) {
			message = "{}";
		}
		LOGGER.info(message, args);
	}
	
	/**
	 * <p>统计开始时间</p>
	 */
	protected final void cost() {
		this.costTime.set(System.currentTimeMillis());
	}
	
	/**
	 * <p>结束统计消耗时间</p>
	 * <p>重置消耗时间统计</p>
	 * 
	 * @return 消耗时间
	 */
	protected final long costed() {
		final long time = System.currentTimeMillis();
		final long costed = time - this.costTime.getAndSet(time);
		// TODO：多行文本
		LOGGER.info("消耗时间（毫秒）：{}", costed);
		LOGGER.info("消耗时间（秒）：{}", costed / SystemConfig.ONE_SECOND_MILLIS);
		return costed;
	}
	
	/**
	 * <p>计算执行消耗时间</p>
	 * 
	 * @param count 执行次数
	 * @param coster 消耗任务
	 * 
	 * @return 消耗时间
	 */
	protected final long costed(int count, Coster coster) {
		if(SKIP) {
			this.log("跳过消耗测试");
			return 0L;
		}
		this.cost();
		for (int index = 0; index < count; index++) {
			coster.execute();
		}
		return this.costed();
	}
	
	/**
	 * <p>计算执行消耗时间</p>
	 * 
	 * @param count 任务数量
	 * @param thread 线程数量
	 * @param coster 消耗任务
	 * 
	 * @return 消耗时间
	 */
	protected final long costed(int count, int thread, Coster coster) {
		if(SKIP) {
			this.log("跳过消耗测试");
			return 0L;
		}
		final CountDownLatch latch = new CountDownLatch(count);
		final ExecutorService executor = SystemThreadContext.newExecutor(thread, thread, count, 60L, SystemThreadContext.SNAIL_THREAD_COSTED);
		this.cost();
		for (int index = 0; index < count; index++) {
			executor.submit(() -> {
				try {
					coster.execute();
				} catch (Exception e) {
					LOGGER.error("执行异常", e);
				} finally {
					latch.countDown();
				}
			});
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			LOGGER.error("等待异常", e);
			Thread.currentThread().interrupt();
		}
		final long costed = this.costed();
		SystemThreadContext.shutdownNow(executor);
		return costed;
	}

	/**
	 * <p>线程阻塞</p>
	 */
	public final void pause() {
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				LOGGER.error("等待异常", e);
				Thread.currentThread().interrupt();
			}
		}
	}
	
	/**
	 * <p>消耗任务接口</p>
	 * 
	 * @author acgist
	 */
	public interface Coster {

		/**
		 * <p>执行任务</p>
		 */
		public void execute();
		
	}
	
}
