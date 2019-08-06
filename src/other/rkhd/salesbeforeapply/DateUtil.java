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
    static String getAfterDay(String startDay, int count) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(startDay);
            Calendar cl = Calendar.getInstance();
            cl.setTime(date);
            cl.add(Calendar.DATE, count);
            return sdf.format(cl.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 获取当前系统日期
     * @return  当前系统日期  yyyy-MM-dd
     */
    static String getSysDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cl = Calendar.getInstance();
        return sdf.format(cl.getTime());
    }


 /*   public static void main(String[] args) {
        System.out.println(getSysDate());
    }
*/

}
