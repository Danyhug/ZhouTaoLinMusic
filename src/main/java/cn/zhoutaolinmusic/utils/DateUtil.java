package cn.zhoutaolinmusic.utils;

import org.joda.time.DateTime;

import java.util.Date;

public class DateUtil {
    /**
     * 对日期的【分钟】进行加/减
     *
     * @param date    日期
     * @param minute 分钟数，负数为减
     * @return 加/减几分钟后的日期
     */
    public static Date addDateMinute(Date date, int minute) {
        return new DateTime(date).plusMinutes(minute).toDate();
    }

    // 对天加减
    public static Date addDateDays(Date date, int days) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusDays(days).toDate();
    }
}
