package com.my.chen.fabric.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/1
 * @description
 */
public class DateUtil {

    public static String getDateStr(Date date){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA);
            return sdf.format(date);
        } catch (Exception ex) {
            return "";
        }
    }

    public static String getTimeStr(long time){
        Date date = new Date(time);
        return getDateStr(date);
    }

    public static long getCurrent(){
        return System.currentTimeMillis();
    }

    public static String date2Str(Date date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
            return sdf.format(date);
        } catch (Exception ex) {
            return "";
        }
    }

    public static Date str2Date(String dateStr, String format) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
        return sdf.parse(dateStr);
    }
}
