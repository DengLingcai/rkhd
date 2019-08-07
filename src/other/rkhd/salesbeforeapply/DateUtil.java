package other.rkhd.salesbeforeapply;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @Author: 邓令才
 * @Date:2019-08-01
 * @Description:  日期工具类
 *
 **/
public class DateUtil {

    /**
     * 指定日期后几天
     * @param startDay 指定日期
     * @param count    天数
     * @return  计算后的日期 yyyy-MM-dd
     */
    static long getAfterDay(long startDay, int count) {
        Calendar cl = Calendar.getInstance();
        cl.setTime(new Date(startDay));
        cl.add(Calendar.DATE, count);
        return cl.getTime().getTime();
    }

    /**
     * 获取当前系统日期
     * @return  当前系统日期  yyyy-MM-dd
     */
    static long getSysDate() {
        Calendar cl = Calendar.getInstance();
        return cl.getTime().getTime();
    }

    /**
     * 日期字符串转换为时间戳
     * @param strDate 日期字符串
     * @return 日期时间戳
     */
    static long strDateToLong(String strDate){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd hh:mm");
        try {
            Date date = format.parse(strDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.getTime().getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date().getTime();
    }


   public static void main(String[] args) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd hh:mm");
       try {
           Date date = format.parse("2019-09-06 00:00");
           Calendar calendar = Calendar.getInstance();
           calendar.setTime(date);
           System.out.println(calendar.getTime());
       } catch (ParseException e) {
           e.printStackTrace();
       }

    }


}
