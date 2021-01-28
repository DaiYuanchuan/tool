package cn.novelweb.tool.download.snail.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.event.Level;

import cn.novelweb.tool.download.snail.logger.adapter.ConsoleLoggerAdapter;
import cn.novelweb.tool.download.snail.logger.adapter.FileLoggerAdapter;

/**
 * <p>日志上下文</p>
 * 
 * @author acgist
 */
public final class LoggerContext implements ILoggerFactory {

	private static final LoggerContext INSTANCE = new LoggerContext();

	public static LoggerContext getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>日志对象</p>
	 */
	private final Map<String, Logger> loggers;
	/**
	 * <p>日志适配器</p>
	 */
	private final List<LoggerAdapter> adapters;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private LoggerContext() {
		this.loggers = new ConcurrentHashMap<>();
		final String adapter = LoggerConfig.getAdapter();
		final List<LoggerAdapter> list = new ArrayList<>();
		if(adapter != null && !adapter.isEmpty()) {
			final String[] adapters = adapter.split(",");
			for (String value : adapters) {
				value = value.trim();
				if(FileLoggerAdapter.ADAPTER.equalsIgnoreCase(value)) {
					list.add(new FileLoggerAdapter());
				} else if(ConsoleLoggerAdapter.ADAPTER.equalsIgnoreCase(value)) {
					list.add(new ConsoleLoggerAdapter());
				}
			}
		}
		this.adapters = list;
	}
	
	/**
	 * <p>获取日志上下文名称</p>
	 * 
	 * @return 日志上下文名称
	 */
	public String getName() {
		return this.getClass().getName();
	}
	
	@Override
	public org.slf4j.Logger getLogger(String name) {
		return this.loggers.computeIfAbsent(name, Logger::new);
	}

	/**
	 * <p>输出日志</p>
	 * 
	 * @param level 级别
	 * @param message 日志
	 */
	public void output(Level level, String message) {
		final boolean error = level.toInt() >= Level.ERROR.toInt();
		for (LoggerAdapter adapter : this.adapters) {
			if(error) {
				adapter.errorOutput(message);
			} else {
				adapter.output(message);
			}
		}
	}
	
	/**
	 * <p>系统异常记录</p>
	 * 
	 * @param t 异常
	 */
	public static void error(Throwable t) {
		try(FileOutputStream outputStream = new FileOutputStream(new File("logs/snail.logger.log"), true)) {
			final PrintWriter printWriter = new PrintWriter(outputStream);
			t.printStackTrace(printWriter);
			printWriter.flush();
		} catch (Exception e) {
			error(t);
		}
	}
	
	/**
	 * <p>关闭日志</p>
	 */
	public static void shutdown() {
		INSTANCE.adapters.forEach(LoggerAdapter::release);
	}

}
