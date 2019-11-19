import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * @Description:  日期工具类
 * @Author: 张亚辉 zyh1410@gmail.com
 * @Date: 2019/1/12
 */
public class DateUtil {
	private static Map<String, SimpleDateFormat> sdfmap = new HashMap<String, SimpleDateFormat>();

	public static SimpleDateFormat getSDF(String key) {
		if (sdfmap == null) {
			sdfmap = new HashMap<String, SimpleDateFormat>();
		}
		SimpleDateFormat sdf = sdfmap.get(key);
		if (sdf == null) {
			sdf = new SimpleDateFormat(key);
			sdfmap.put(key, sdf);
		}
		return sdf;
	}

	public DateUtil() {
	}

	public static String getNowDateStr(String format) {
		Date d = new Date();
		if (format == null || format.equals("") || format.toLowerCase().equals("null"))
			return null;
		SimpleDateFormat formatter = getSDF(format);
		return formatter.format(d);
	}

	public static Date praseDate(String dateString) {
		return praseDate(dateString, "yyyy-MM-dd");
	}

	public static Date praseDate(String dateString, String format) {
		SimpleDateFormat formatter = getSDF(format);
		Date d = null;
		if (dateString == null || dateString.equals("") || dateString.toLowerCase().equals("null"))
			return null;
		try {
			d = formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	public static String formatDate2(Date date) {
		if (date == null)
			return "";
		SimpleDateFormat sdf = getSDF("yyyy-MM-dd");
		return sdf.format(date);
	}

	public static Timestamp praseTimestamp(String dateString) {
		String format = "yyyy-MM-dd HH:mm:ss";
		if (dateString.trim().length() >= 17)
			format = "yyyy-MM-dd HH:mm:ss";
		else if (dateString.trim().length() >= 14)
			format = "yyyy-MM-dd HH:mm";
		else if (dateString.trim().length() >= 10)
			format = "yyyy-MM-dd";
		else
			return null;
		SimpleDateFormat formatter = getSDF(format);
		Date d = null;
		try {
			d = formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Timestamp(d.getTime());
	}

	public static String formatDate(Date curTime) {
		if (curTime == null || curTime.equals("")) {
			return "";
		} else {
			SimpleDateFormat formatter = getSDF("yyyy-MM-dd HH:mm:ss");
			return formatter.format(curTime);
		}
	}

	public static String formatDate3(Date curTime) {
		if (curTime == null || curTime.equals("")) {
			return " ";
		} else {
			SimpleDateFormat formatter = getSDF("yyyy-MM-dd");
			return formatter.format(curTime);
		}
	}

	public static String formatDate(Date curTime, String format) {
		if (curTime == null) {
			return " ";
		} else {
			SimpleDateFormat formatter = getSDF(format);
			return formatter.format(curTime);
		}
	}

	public static String formatCalendar(Calendar calendar) {
		String result = (getSDF("yyyy-MM-dd HH:mm:ss")).format(calendar.getTime());
		return result;
	}

	public static long compareDate(String s1, String s2) {
		long DAY = 0x5265c00L;
		Date d1 = praseDate(s1);
		Date d2 = praseDate(s2);
		return (d2.getTime() - d1.getTime()) / DAY;
	}

	public static String addDay(String nDate, int nNumberOfDay) {
		if (nDate.length() > 10)
			nDate = nDate.substring(0, 10);
		String a[] = nDate.split("-");
		SimpleDateFormat formatter = getSDF("yyyy-MM-dd");
		GregorianCalendar gc = new GregorianCalendar(Integer.parseInt(a[0]), Integer.parseInt(a[1]) - 1,
				Integer.parseInt(a[2]));
		gc.add(5, nNumberOfDay);
		return formatter.format(gc.getTime());
	}

	public static String getSystime() {
		Date dt = new Date();
		DateFormat df = getSDF("yyyyMMddHHmmss");
		String nowTime = "";
		nowTime = df.format(dt);
		return nowTime;
	}

	/**
	 * 按整5分钟划分取整 9:02 取整为 9:00 9:07取整为9:05 王万斌
	 * 
	 * @param date
	 * @return 2015-9-11
	 */
	public static Date getTimeBy5Minutes(Date date) {
		String dateStr = formatDate(date);
		String beforStr = dateStr.substring(0, 15);
		String afterStr = "";
		if (Integer.parseInt(dateStr.substring(15, 16)) >= 5) {
			afterStr = "5:00";
		} else {
			afterStr = "0:00";
		}
		return praseDate(beforStr + afterStr, "yyyy-MM-dd HH:mm:ss");
	}

/*	*//**
	 * 时间增加天数 王万斌
	 * 
	 * @param date
	 * @param nNumberOfMinutes
	 * @return 2015-9-11
	 *//*
	public static Date addDays(Date date, int nNumberOfDays) {
		long nextDate = date.getTime() + 1000 * 60 * 60 * 24 * nNumberOfDays;
		return new Date(nextDate);
	}*/

	/**
	 * 时间增加分钟 王万斌
	 * 
	 * @param date
	 * @param nNumberOfMinutes
	 * @return 2015-9-11
	 */
	public static Date addMinutes(Date date, int nNumberOfMinutes) {
		long nextDate = date.getTime() + 60 * 1000 * nNumberOfMinutes;
		return new Date(nextDate);
	}

	/**
	 * 将持续时间转为标准时间格式 万文杰
	 * 
	 * @param time
	 * @return 2015-9-17
	 */
	public static String secToTime(int time) {
		String timeStr = null;
		int hour = 0;
		int minute = 0;
		int second = 0;
		int day = 0;
		if (time <= 0)
			return "00:00";
		else {
			minute = time / 60;
			if (minute < 60) {
				second = time % 60;
				timeStr = unitFormat(minute) + ":" + unitFormat(second);
			} else {
				hour = minute / 60;
				if (hour < 24) {
					minute = minute % 60;
					second = time - hour * 3600 - minute * 60;
					timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
				} else {
					day = hour / 24;
					hour = hour % 24;
					minute = (time - hour * 60 * 60 - day * 24 * 60 * 60) / 60;
					second = time - hour * 60 * 60 - day * 24 * 60 * 60 - minute * 60;
					timeStr = unitFormat(day) + "d" + ":" + unitFormat(hour) + ":" + unitFormat(minute) + ":"
							+ unitFormat(second);

				}
			}
		}
		return timeStr;
	}

	public static String unitFormat(int i) {
		String retStr = null;
		if (i >= 0 && i < 10)
			retStr = "0" + Integer.toString(i);
		else
			retStr = "" + i;
		return retStr;
	}

	/**
	 * 解析日期
	 * @author 吴同
	 * @param date
	 * @return 0：yyyy-mm-dd，1：当天第几个15分钟
	 */
	public static String[] analysisDate(Date date) {
		SimpleDateFormat sdf = getSDF("yyyy-MM-dd HH:mm:ss");
		String s1 = sdf.format(date);
		s1 = s1.substring(0, 10);
		long n = 0;
		try {
			Date d2 = sdf.parse(s1 + " 00:00:00");
			long l = date.getTime() - d2.getTime();
			n = l / 900000l + 1;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String[] { s1, n + "" };
	}
}
