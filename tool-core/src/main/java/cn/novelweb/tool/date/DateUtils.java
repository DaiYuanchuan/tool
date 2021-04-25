package cn.novelweb.tool.date;

import cn.hutool.core.date.*;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>时间工具类</p>
 * <p>2019-12-07 22:44</p>
 *
 * @author Dai Yuanchuan
 **/
public class DateUtils {

    /**
     * 传入任意 顺序 的日期 , 对日期进行 开始日期 > 结束日期 的排序
     * 遵循 大的日期在前面[开始日期] 小的日期在后面[结束日期]
     *
     * @param startTime 开始日期
     * @param endTime   结束日期
     * @param format    日期格式化[如:yyyy-MM-dd HH:mm:ss.SSS]
     * @return 返回一个String数组, 数组的第一位为 最终的开始日期,数组的第二位为 最终的结束日期
     */
    public static String[] sortByDate(String startTime, String endTime, String format) {
        // 如果 结束日期 为空 则默认 结束日期 为现在时间
        if (StringUtils.isBlank(endTime)) {
            endTime = DateUtil.today();
        }

        // 如果 起始日期 为空 则默认 起始日期 为现在时间
        if (StringUtils.isBlank(startTime)) {
            startTime = DateUtil.today();
        }

        // 校验:日期格式
        boolean isStartTime = isTime(endTime, format);
        if (!isStartTime) {
            return null;
        }
        boolean isEndTime = isTime(endTime, format);
        if (!isEndTime) {
            return null;
        }

        // 开始日期的毫秒数
        long startTimeMillisecond = DateUtil.parse(startTime, format).getTime();

        // 结束日期的毫秒数
        long endTimeMillisecond = DateUtil.parse(endTime, format).getTime();

        // 调整时间日期 判断 开始日期的毫秒数 是否小于 结束日期的毫秒数
        if (startTimeMillisecond < endTimeMillisecond) {
            // 开始日期小于结束日期 两个 日期调换下位置 保证 开始日期 大于 结束日期
            return new String[]{endTime, startTime};
        }
        return new String[]{startTime, endTime};
    }

    /**
     * 传入任意 顺序 的日期 , 对日期进行 开始日期 > 结束日期 的排序
     * 遵循 大的日期在前面[开始日期] 小的日期在后面[结束日期]
     *
     * @param startTime 开始日期
     * @param endTime   结束日期
     * @return 返回一个Date数组, 数组的第一位为 最终的开始日期,数组的第二位为 最终的结束日期
     */
    public static Date[] sortByDate(Date startTime, Date endTime) {
        // 如果 结束日期 为空 则默认 结束日期 为现在时间
        if (endTime == null) {
            endTime = DateUtil.date();
        }

        // 如果 起始日期 为空 则默认 起始日期 为现在时间
        if (startTime == null) {
            startTime = DateUtil.date();
        }

        // 调整时间日期 判断 开始日期的毫秒数 是否小于 结束日期的毫秒数
        if (startTime.getTime() < endTime.getTime()) {
            // 开始日期小于结束日期 两个 日期调换下位置 保证 开始日期 大于 结束日期
            return new Date[]{endTime, startTime};
        }
        return new Date[]{startTime, endTime};
    }

    /**
     * 验证字符串是否可以转成时间格式
     *
     * @param time       需要转换的时间字符串
     * @param dateFormat yyyy-MM-dd hh:mm:ss
     * @return 返回true:可以转换成时间格式 ，返回false:不可以
     */
    public static boolean isTime(String time, String dateFormat) {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        try {
            // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，
            // 比如2007/02/29会被接受，并转换成2007/03/01
            format.setLenient(false);
            format.parse(time);
            return true;
        } catch (Exception e) {
            // 如果throw ParseException或者NullPointerException
            // 就说明格式不对
            return false;
        }
    }

    /**
     * 根据传入的时间与现在的时间差
     * 将时间差转成可读字符串 如:刚刚、3秒前、一小时前等等
     *
     * @param time 需要进行比较的时间对象
     * @return 返回可读字符串, 大于20天的 将会直接显示日期
     */
    public static String timeDifference(Date time) {
        if (time == null) {
            return "";
        }

        // 获取两个日期之间相差的毫秒数
        long timeDifference = DateUtil.between(DateUtil.date(), time, DateUnit.MS);

        // 1分钟之内
        if (timeDifference < DateUnit.MINUTE.getMillis()) {
            // 如果是5秒内
            int just = 5;
            if (timeDifference < DateUnit.SECOND.getMillis() * just) {
                return "刚刚";
            } else {
                return DateUtil.formatBetween(timeDifference, BetweenFormatter.Level.SECOND) + "前";
            }
        }

        // 一小时内
        if (timeDifference > DateUnit.MINUTE.getMillis() && timeDifference < DateUnit.HOUR.getMillis()) {
            return DateUtil.formatBetween(timeDifference, BetweenFormatter.Level.MINUTE) + "钟前";
        }

        // 一天内
        if (timeDifference >= DateUnit.HOUR.getMillis() && timeDifference < DateUnit.DAY.getMillis()) {
            return DateUtil.formatBetween(timeDifference, BetweenFormatter.Level.HOUR) + "前";
        }

        // 二十天内
        long twentyDays = DateUnit.DAY.getMillis() * 20;
        if (timeDifference >= DateUnit.DAY.getMillis() && timeDifference < twentyDays) {
            return DateUtil.formatBetween(timeDifference, BetweenFormatter.Level.DAY) + "前";
        }
        return DateUtil.format(time, "yyyy-MM-dd");
    }

    /**
     * 获取过去某一天的日期
     * 参数为正整数时 获取过去的某一天
     * 参数为负整数时 获取未来的某一天
     *
     * @param past 指定的日期 单位 天
     * @return 返回yyyy-MM-dd格式的日期
     */
    public static String getPastDate(int past) {
        DateTime dateTime = DateUtil.offsetDay(DateUtil.date(), past * -1);
        return DateUtil.formatDate(dateTime);
    }

    /**
     * 获取过去的任意时间内的日期数组
     * 参数为正整数时 获取过去的某一天
     * 参数为负整数时 获取未来的某一天
     *
     * @param intervals 指定的日期 单位 天
     * @return 返回yyyy-MM-dd格式的日期ArrayList数组
     */
    public static List<String> getPastDateList(int intervals) {
        List<String> pastDaysList = new ArrayList<>();
        for (int i = intervals; i > 0; i--) {
            pastDaysList.add(getPastDate(i));
        }
        return pastDaysList;
    }

    /**
     * 时间单位换算,将以下格式的时间转换为毫秒
     * 时:分:秒.毫秒
     * 如:参数为 01:44:41.42 应该换算为 6281042
     *
     * @param time 指定格式的时间字符串
     * @return 返回对应时间的毫秒数[返回-1时,转换出错]
     */
    public static long getTimeConversion(String time) {
        boolean isTime = isTime(time, DatePattern.NORM_TIME_PATTERN + ".SSS");
        if (!isTime) {
            return -1;
        }
        // 定义当前的日期
        String string = new SimpleDateFormat(DatePattern.NORM_DATE_PATTERN).format(DateUtil.date());

        // 起始日期
        Date beginDate = DateUtil.parse(string + " 00:00:00.000", DatePattern.NORM_DATETIME_MS_PATTERN);
        // 结束日期
        Date endDate = DateUtil.parse(string + " " + time, DatePattern.NORM_DATETIME_MS_PATTERN);
        // 相差的毫秒数
        return DateUtil.between(beginDate, endDate, DateUnit.MS);
    }

    /**
     * 获取当前的网络时间(调用百度时间)
     * 获取百度的时间
     * 采取默认时间格式[yyyy-MM-dd HH:mm:ss]
     *
     * @return 当前百度的时间
     */
    public static String getBaiDuTime() {
        return getBaiDuTime(DatePattern.NORM_DATETIME_PATTERN);
    }

    /**
     * 获取当前的网络时间(调用百度时间)
     * 获取百度的时间
     *
     * @param format 传入时间格式  如(yyyy-MM-dd HH:mm:ss)
     * @return 当前百度的时间
     */
    public static String getBaiDuTime(String format) {
        try {
            // 取得资源对象
            URL url = new URL("http://www.baidu.com");
            // 生成连接对象
            URLConnection urlConnection = url.openConnection();
            // 发出连接
            urlConnection.connect();
            // 读取网站日期时间
            long connectionDate = urlConnection.getDate();
            // 转换为标准时间对象
            Date date = new Date(connectionDate);
            // 输出北京时间
            return format(date, format);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 根据 时间戳 格式化 日期
     * 默认的日期格式为:yyyy-MM-dd HH:mm:ss
     *
     * @param date 需要格式化的时间戳
     * @return 格式化后的字符串
     */
    public static String format(long date) {
        return DateUtil.format(DateUtil.date(date), DatePattern.NORM_DATETIME_PATTERN);
    }

    /**
     * 根据 时间戳 格式化 日期
     *
     * @param date   需要格式化的时间戳
     * @param format 日期格式[如:yyyy-MM-dd]
     * @return 格式化后的字符串
     */
    public static String format(long date, String format) {
        return DateUtil.format(DateUtil.date(date), format);
    }

    /**
     * 格式化日期
     * 默认的日期格式为:yyyy-MM-dd HH:mm:ss
     *
     * @param date 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String format(Date date) {
        return DateUtil.format(date, DatePattern.NORM_DATETIME_PATTERN);
    }

    /**
     * 格式化日期
     *
     * @param date   被格式化的日期
     * @param format 日期格式[如:yyyy-MM-dd]
     * @return 格式化后的字符串
     */
    public static String format(Date date, String format) {
        return DateUtil.format(date, format);
    }

    /**
     * 将时间戳转成Date
     *
     * @param date 时间戳
     * @return Date日期
     */
    public static Date parse(long date) {
        return DateUtil.date(date);
    }

    /**
     * 将字符串形式的日期转为Date
     *
     * @param date 字符串格式的日期时间
     * @return Date日期
     */
    public static Date parse(String date) {
        return DateUtil.parse(date);
    }

    /**
     * 将字符串形式的日期转为Date
     * 指定转换的格式[yyyy-MM-dd HH:mm:ss]
     *
     * @param date   字符串格式的日期时间
     * @param format 需要转换的格式
     * @return Date日期
     */
    public static Date parse(String date, String format) {
        return DateUtil.parse(date, format);
    }

    /**
     * 判断一个时间段 是否在 另一个时间段内
     * 判断是否存在重叠
     * 例:13:30 ~ 14:30 这个区间段
     * 13:29 ~ 14:29 为 true
     * 13:31 ~ 14:31 为 true
     * 13:29 ~ 14:31 为 true
     * 13:31 ~ 14:29 为 true
     * 14:31 ~ 15:30 为 false
     *
     * @param start         需要进行比较的开始时间
     * @param end           需要进行比较的结束时间
     * @param startInterval 区间段开始时间[可以是数据库中查找出的开始区间段]
     * @param endInterval   区间段结束时间[可以是数据库中查找出的结束区间段]
     * @return 返回Boolean值 [true:存在重叠的时间段 false:时间段不重叠]
     */
    public static Boolean timeOverlap(Date start, Date end, Date startInterval, Date endInterval) {
        // 区间段1
        Date[] interval1 = sortByDate(start, end);
        // 区间段2
        Date[] interval2 = sortByDate(startInterval, endInterval);
        return interval2[1].before(interval1[0]) && interval2[0].after(interval1[1]);
    }
}
