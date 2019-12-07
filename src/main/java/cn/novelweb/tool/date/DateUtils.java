package cn.novelweb.tool.date;

import cn.hutool.core.date.*;
import org.apache.commons.lang.StringUtils;

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
     *
     * @param startTime 开始日期 yyyy-MM-dd
     * @param endTime   结束日期 yyyy-MM-dd
     * @return 返回一个String数组, 数组的第一位为 最终的开始日期,数组的第二位为 最终的结束日期
     */
    public static String[] sortByDate(String startTime, String endTime) {
        // 如果 结束日期 为空 则默认 结束日期 为现在时间
        if (StringUtils.isBlank(endTime)) {
            endTime = DateUtil.today();
        }

        // 如果 起始日期 为空 则默认 起始日期 为现在时间
        if (StringUtils.isBlank(startTime)) {
            startTime = DateUtil.today();
        }

        // 校验:日期格式
        boolean isStartTime = isTime(endTime, DatePattern.NORM_DATE_PATTERN);
        if (!isStartTime) {
            return null;
        }
        boolean isEndTime = isTime(endTime, DatePattern.NORM_DATE_PATTERN);
        if (!isEndTime) {
            return null;
        }

        // 开始日期的毫秒数
        long startTimeMillisecond = DateUtil.parse(startTime, DatePattern.NORM_DATE_PATTERN).getTime();

        // 结束日期的毫秒数
        long endTimeMillisecond = DateUtil.parse(endTime, DatePattern.NORM_DATE_PATTERN).getTime();

        // 调整时间日期 判断 开始日期的毫秒数 是否小于 结束日期的毫秒数
        if (startTimeMillisecond < endTimeMillisecond) {
            // 开始日期小于结束日期 两个 日期调换下位置 保证 开始日期 大于 结束日期
            return new String[]{endTime, startTime};
        }
        return new String[]{startTime, endTime};
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
                return DateUtil.formatBetween(timeDifference, BetweenFormater.Level.SECOND) + "前";
            }
        }

        // 一小时内
        if (timeDifference > DateUnit.MINUTE.getMillis() && timeDifference < DateUnit.HOUR.getMillis()) {
            return DateUtil.formatBetween(timeDifference, BetweenFormater.Level.MINUTE) + "钟前";
        }

        // 一天内
        if (timeDifference >= DateUnit.HOUR.getMillis() && timeDifference < DateUnit.DAY.getMillis()) {
            return DateUtil.formatBetween(timeDifference, BetweenFormater.Level.HOUR) + "前";
        }

        // 二十天内
        long twentyDays = DateUnit.DAY.getMillis() * 20;
        if (timeDifference >= DateUnit.DAY.getMillis() && timeDifference < twentyDays) {
            return DateUtil.formatBetween(timeDifference, BetweenFormater.Level.DAY) + "前";
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
}
