import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * @Description:  时间工具类
 * @Author: 张亚辉 zyh1410@gmail.com
 * @Date: 2019/11/12
 */
public class TimeUtil {
    public static DateTime string2DateTime(String time, String format){
        DateTimeFormatter formats= DateTimeFormat.forPattern(format);
        return DateTime.parse(time,formats);
    }
    public static boolean isValidDate(String time, String format) {
        boolean convertSuccess=true;
        try {
            DateTimeFormatter formats= DateTimeFormat.forPattern(format);
            DateTime.parse(time,formats);
        }catch (Exception e){
            convertSuccess=false;
        }
        return convertSuccess;
    }
    public static String dateTime2String(DateTime time, String format){
        return time.toString(format);
    }

    public static String MillisToTime(Long i){
        long ms=i%1000;
        long allTime=i/1000;
        long ss = allTime%60;
        long h_m = (allTime - ss) / 60;
        long mm = h_m % 60;
        long hh = (h_m - mm) / 60;
        String h="";
        String m="";
        String s="";
        String sss="";
        if(hh>0){
            h=hh+"h";
        }
        if(mm>0){
            m=mm+"m";
        }
        s=ss+"s";
        sss=ms+"ms";
        return h+m+s+sss;
    }

    public static DateTime getNewDate(){
        return DateTime.now();
    }

    public static String getNewDateString(){
        return TimeUtil.dateTime2String(DateTime.now(),"yyyy-MM-dd HH:mm:ss");
    }


    /**
     * string - date
     * @param date
     * @return
     */
    public static Date str2Date(String date, String format){
        Date resdate=  null;
        try{
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            resdate =  sdf.parse(date);
        }catch(Exception e ){
            e.printStackTrace();
        }
        return resdate;
    }

    /**
     *  date - string
     * @param date
     * @param format
     * @return
     */
    public static String date2Str(Date date, String format){
        try{
            DateFormat formats = new SimpleDateFormat(format);
            String str = formats.format(date);
            return str;
        }catch(Exception e ){
            e.printStackTrace();
            return  "";
        }
    }


    /**
     * 获取两个字符串日期的时间差
     * @param date1
     * @param date2
     * @return  date1-date2
     */
    public static String getTimeLen(String date1,String date2, String format){
        return  (str2Date(date1,format).getTime()-str2Date(date2,format).getTime())+"";
    }

}
