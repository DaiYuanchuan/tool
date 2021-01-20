package cn.novelweb.tool.download.snail.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import cn.novelweb.tool.download.snail.config.SystemConfig;

/**
 * <p>时间工具</p>
 * 
 * @author acgist
 */
public final class DateUtils {

	/**
	 * <p>工具类禁止实例化</p>
	 */
	private DateUtils() {
	}
	
	/**
	 * <p>默认时间格式：{@value}</p>
	 */
	public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
	/**
	 * <p>时间格式工具</p>
	 */
	private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);
	/**
	 * <p>Unix和Java时间戳倍数：{@value}</p>
	 */
	private static final int UNIX_JAVA_TIMESTAMP_SCALE = 1000;
	/**
	 * <p>Windows系统时间和Java系统时间相差毫秒数：{@value}</p>
	 * <p>Java时间戳转Windows时间戳：{@value} + System.currentTimeMillis()</p>
	 */
	private static final long WINDOWS_JAVA_DIFF_TIMEMILLIS = 11644473600000L;
	/**
	 * <p>Windows开始时间（北京时间）</p>
	 * <p>开始时间：（1601年1月1日）北京时间（东八区）</p>
	 * 
	 * TODO：通过时区优化
	 */
	private static final LocalDateTime WINDOWS_BEIJIN_BEGIN_TIME = LocalDateTime.of(1601, 01, 01, 8, 00, 00);
	/**
	 * <p>Java和Windows时间戳倍数：{@value}</p>
	 */
	private static final int JAVA_WINDOWS_TIMESTAMP_SCALE = 10_000;
	
	/**
	 * <p>时间格式化：保留两个时间单位</p>
	 * 
	 * @param value 时间（单位：秒）
	 * 
	 * @return XX天XX小时、XX小时XX分钟、XX分钟XX秒
	 */
	public static final String format(long value) {
		final StringBuilder builder = new StringBuilder();
		final long day = value / SystemConfig.ONE_DAY;
		if(day != 0) {
			builder.append(day).append("天");
			value = value % SystemConfig.ONE_DAY;
		}
		final long hour = value / SystemConfig.ONE_HOUR;
		if(hour != 0) {
			builder.append(hour).append("小时");
			value = value % SystemConfig.ONE_HOUR;
			if(day != 0) {
				return builder.toString();
			}
		}
		final long minute = value / SystemConfig.ONE_MINUTE;
		if(minute != 0) {
			builder.append(minute).append("分钟");
			value = value % SystemConfig.ONE_MINUTE;
			if(hour != 0) {
				return builder.toString();
			}
		}
		builder.append(value).append("秒");
		return builder.toString();
	}
	
	/**
	 * <p>时间格式化</p>
	 * <p>默认格式：{@value #DEFAULT_PATTERN}</p>
	 * 
	 * @param date 时间
	 * 
	 * @return 格式化字符串
	 * 
	 * @see #dateFormat(Date, String)
	 */
	public static final String dateFormat(Date date) {
		return dateFormat(date, DEFAULT_PATTERN);
	}
	
	/**
	 * <p>时间格式化</p>
	 * 
	 * @param date 时间
	 * @param pattern 格式（为空默认：{@value #DEFAULT_PATTERN}）
	 * 
	 * @return 格式化字符串
	 */
	public static final String dateFormat(Date date, String pattern) {
		if(date == null) {
			return null;
		}
		return localDateTimeFormat(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()), pattern);
	}
	
	/**
	 * <p>时间格式化</p>
	 * <p>默认格式：{@value #DEFAULT_PATTERN}</p>
	 * 
	 * @param localDateTime 时间
	 * 
	 * @return 格式化字符串
	 * 
	 * @see #localDateTimeFormat(LocalDateTime, String)
	 */
	public static final String localDateTimeFormat(LocalDateTime localDateTime) {
		return localDateTimeFormat(localDateTime, DEFAULT_PATTERN);
	}
	
	/**
	 * <p>时间格式化</p>
	 * 
	 * @param localDateTime 时间
	 * @param pattern 格式（为空默认：{@value #DEFAULT_PATTERN}）
	 * 
	 * @return 格式化字符串
	 */
	public static final String localDateTimeFormat(LocalDateTime localDateTime, String pattern) {
		if(localDateTime == null) {
			return null;
		}
		DateTimeFormatter formatter;
		if(DEFAULT_PATTERN.equals(pattern) || pattern == null) {
			formatter = DEFAULT_FORMATTER;
		} else {
			formatter = DateTimeFormatter.ofPattern(pattern);
		}
		return formatter.format(localDateTime);
	}
	
	/**
	 * <p>Java时间戳</p>
	 * 
	 * @return Java时间戳
	 */
	public static final long javaTimestamp() {
		return System.currentTimeMillis();
	}

	/**
	 * <p>Java时间戳转Unix时间戳</p>
	 * 
	 * @param javaTimestamp Java时间戳
	 * 
	 * @return Unix时间戳
	 */
	public static final long javaToUnixTimestamp(long javaTimestamp) {
		return javaTimestamp / UNIX_JAVA_TIMESTAMP_SCALE;
	}
	
	/**
	 * <p>Unix时间戳</p>
	 * 
	 * @return Unix时间戳
	 */
	public static final long unixTimestamp() {
		return javaToUnixTimestamp(javaTimestamp());
	}

	/**
	 * <p>Unix时间戳转Java时间戳</p>
	 * 
	 * @param unixTimestamp Unix时间戳
	 * 
	 * @return Java时间戳
	 */
	public static final long unixToJavaTimestamp(long unixTimestamp) {
		return unixTimestamp * UNIX_JAVA_TIMESTAMP_SCALE;
	}
	
	/**
	 * <p>Unix时间戳转Java时间</p>
	 * 
	 * @param unixTimestamp Unix时间戳
	 * 
	 * @return Java时间
	 */
	public static final Date unixToJavaDate(long unixTimestamp) {
		return new Date(unixToJavaTimestamp(unixTimestamp));
	}
	
	/**
	 * <p>获取时间戳（微秒）</p>
	 * 
	 * @return 时间戳（微秒）
	 */
	public static final int timestampUs() {
		return (int) (System.nanoTime() / 1000);
	}

	/**
	 * <p>获取Windows时间戳</p>
	 * 
	 * @return Windows时间戳
	 */
	public static final long windowsTimestamp() {
		return (WINDOWS_JAVA_DIFF_TIMEMILLIS + System.currentTimeMillis()) * JAVA_WINDOWS_TIMESTAMP_SCALE;
	}
	
	/**
	 * <p>获取Windows时间戳（扩展）</p>
	 * 
	 * @return Windows时间戳
	 */
	public static final long windowsTimestampEx() {
		return DateUtils.diff(WINDOWS_BEIJIN_BEGIN_TIME, LocalDateTime.now()).toMillis() * JAVA_WINDOWS_TIMESTAMP_SCALE;
	}
	
	/**
	 * <p>计算时间差</p>
	 * 
	 * @param begin 开始时间
	 * @param end 结束时间
	 * 
	 * @return 时间差
	 */
	public static final Duration diff(LocalDateTime begin, LocalDateTime end) {
		return Duration.between(begin, end);
	}
	
}
