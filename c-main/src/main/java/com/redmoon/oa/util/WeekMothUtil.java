package com.redmoon.oa.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class WeekMothUtil {
    private int weeks = 0;


    public static String getWeek(String sdate) {
        Date date = strToDate(sdate);


        Calendar c = Calendar.getInstance();


        c.setTime(date);


        return new SimpleDateFormat("EEEE").format(c.getTime());
    }


    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");


        ParsePosition pos = new ParsePosition(0);


        Date strtodate = formatter.parse(strDate, pos);


        return strtodate;
    }


    public static long getDays(String date1, String date2) {
        if ((date1 == null) || (date1.equals(""))) {
            return 0L;
        }
        if ((date2 == null) || (date2.equals(""))) {
            return 0L;
        }


        SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");


        Date date = null;


        Date mydate = null;
        try {
            date = myFormatter.parse(date1);


            mydate = myFormatter.parse(date2);
        } catch (Exception localException) {
        }


        long day = (date.getTime() - mydate.getTime()) / 86400000L;


        return day;
    }


    public String getyd() {
        Calendar cal = Calendar.getInstance();
        cal.add(5, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd ").format(cal.getTime());
        return yesterday;
    }


    public String getDefaultDay() {
        String str = "";


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


        Calendar lastDate = Calendar.getInstance();


        lastDate.set(5, 1);


        lastDate.add(2, 1);


        lastDate.add(5, -1);


        str = sdf.format(lastDate.getTime());


        return str;
    }


    public String getPreviousMonthFirst() {
        String str = "";


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


        Calendar lastDate = Calendar.getInstance();


        lastDate.set(5, 1);


        lastDate.add(2, -1);


        str = sdf.format(lastDate.getTime());


        return str;
    }


    public String getFirstDayOfMonth() {
        String str = "";


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


        Calendar lastDate = Calendar.getInstance();


        lastDate.set(5, 1);


        str = sdf.format(lastDate.getTime());


        return str;
    }


    public String getCurrentWeekday() {
        this.weeks = 0;


        int mondayPlus = getMondayPlus();


        GregorianCalendar currentDate = new GregorianCalendar();


        currentDate.add(5, mondayPlus + 6);


        Date monday = currentDate.getTime();


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


        String preMonday = sdf.format(monday);


        return preMonday;
    }


    public String getNowTime(String dateformat) {
        Date now = new Date();


        SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);


        String hehe = dateFormat.format(now);


        return hehe;
    }


    private int getMondayPlus() {
        Calendar cd = Calendar.getInstance();


        int dayOfWeek = cd.get(7) - 1;


        if (dayOfWeek == 1) {
            return 0;
        }


        return (1 - dayOfWeek);
    }


    public String getMondayOFWeek() {
        this.weeks = 0;


        int mondayPlus = getMondayPlus();


        GregorianCalendar currentDate = new GregorianCalendar();


        currentDate.add(5, mondayPlus);


        Date monday = currentDate.getTime();


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


        String preMonday = sdf.format(monday);


        return preMonday;
    }


    public String getPreviousWeekSunday() {
        this.weeks = 0;


        this.weeks -= 1;


        int mondayPlus = getMondayPlus();


        GregorianCalendar currentDate = new GregorianCalendar();


        currentDate.add(5, mondayPlus + this.weeks);


        Date monday = currentDate.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String preMonday = sdf.format(monday);


        return preMonday;
    }


    public String getPreviousWeekday() {
        this.weeks -= 1;


        int mondayPlus = getMondayPlus();


        GregorianCalendar currentDate = new GregorianCalendar();


        currentDate.add(5, mondayPlus + 7 * this.weeks);


        Date monday = currentDate.getTime();


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


        String preMonday = sdf.format(monday);


        return preMonday;
    }


    public String getPreviousMonthEnd() {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar lastDate = Calendar.getInstance();
        lastDate.add(2, -1);
        lastDate.set(5, 1);
        lastDate.roll(5, -1);
        str = sdf.format(lastDate.getTime());
        return str;
    }
}
