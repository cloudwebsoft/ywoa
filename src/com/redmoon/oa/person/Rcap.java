package com.redmoon.oa.person;

import java.util.Calendar;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class Rcap {
  // 初始月份及各月份天数数组
  String[] months = {
      "一　月", "二　月", "三　月", "四　月", "五　月", "六　月", "七　月", "八　月", "九　月", "十　月",
      "十一月", "十二月"};
  int daysInMonth[] = {
      31, 28, 31, 30, 31, 30, 31, 31,
      30, 31, 30, 31};

  int displayMonth;
  int displayYear;

  int todayYear;
  int todayMonth;
  int todayDay;

  public Rcap() {
    Calendar cal = Calendar.getInstance();
    displayYear = cal.get(cal.YEAR);
    displayMonth = cal.get(cal.MONTH);

    todayYear = displayYear;
    todayMonth = displayMonth;
    todayDay = cal.get(cal.DAY_OF_MONTH);

  }
  public int getDays(int month, int year) {
    //测试选择的年份是否是润年？
    if (1 == month)
      return ( (0 == year % 4) && (0 != (year % 100))) ||
          (0 == year % 400) ? 29 : 28;
        else
      return daysInMonth[month];
  }

  public String newCalendar(int displayYear,int displayMonth) {
    this.displayYear = displayYear;
    this.displayMonth = displayMonth;
    Calendar newCal = Calendar.getInstance();
    newCal.set(displayYear, displayMonth, 1);
    int day = -1;
    int startDayOfWeek = newCal.get(newCal.DAY_OF_WEEK );
    if ( (todayYear == newCal.get(newCal.YEAR)) &&
        (todayMonth == newCal.get(newCal.MONTH))) {
      day = todayDay;
    }
    int intDaysInMonth = getDays(newCal.get(newCal.MONTH), newCal.get(newCal.YEAR));
    String daysGrid = makeDaysGrid(startDayOfWeek, day, intDaysInMonth, newCal);
    return daysGrid;
  }

  public String makeDaysGrid(int startDay, int day, int intDaysInMonth, Calendar newCal) {
    String daysGrid;
    int month = newCal.get(newCal.MONTH);
    int year = newCal.get(newCal.YEAR);
    boolean isThisYear = (year == todayYear);
    boolean isThisMonth = (day > -1);
    int forwardyear = displayYear+1;
    int backwordyear = displayYear-1;
    int forwardmonth = displayMonth +1;
    int backwordmonth = displayMonth -1;
    daysGrid = "<table align=center cellSpacing='0' borderColorDark='#ffffff' bgColor='#ebebeb' borderColorLight='#000000' border='1' width=80%>";
    daysGrid += "<tr><td align=center colspan=7 bgcolor=#ffffff nowrap>";
    daysGrid += "<font face='courier new, courier' size=2>";
    daysGrid += "<a href='?displayYear="+displayYear+"&displayMonth="+backwordmonth+"')>&laquo;</a>";
    daysGrid += "<b>";
    if (isThisMonth) {
      daysGrid += "<font color=red>" + months[month] + "</font>";
    }
    else {
      daysGrid += months[month];
    }
    daysGrid += "</b>";
    daysGrid += "<a href='?displayMonth="+forwardmonth+"&displayYear="+displayYear+"')>&raquo;</a>";
    daysGrid += "&nbsp;&nbsp;&nbsp;";
    daysGrid += "<a href='?displayMonth="+displayMonth+"&displayYear="+backwordyear+"')>&laquo;</a>";
    daysGrid += "<b>";
    if (isThisYear) {
      daysGrid += "<font color=red>" +year + "&nbsp;年</font>";
    }
    else {
      daysGrid += year + "&nbsp;年";
    }
    daysGrid += "</b>";
    daysGrid += "<a href='?displayMonth="+displayMonth+"&displayYear="+forwardyear+"')>&raquo;</a></td></tr>";
    daysGrid += "<tr><td align=center>日</td><td align=center>一</td><td align=center>二</td>";
    daysGrid += "<td align=center>三</td><td align=center>四</td><td align=center>五</td><td align=center>六</td></tr>";
    int dayOfMonthOfFirstSunday = 7-startDay+2;//(7 - startDay + 1)//在javascript中因为星期是从0-6计数的,而java中则是从1-7计数;
    daysGrid += "<tr>";
    int count = 0;
    int dayOfMonth = 0;
    for (int intWeek = 0; intWeek < 6; intWeek++) {
      for (int intDay = 0; intDay < 7; intDay++) {
        dayOfMonth = (intWeek * 7) + intDay + dayOfMonthOfFirstSunday - 7;
        if (dayOfMonth <= 0) {
          daysGrid += "<td>&nbsp;</td>";
        }
        else if (dayOfMonth <= intDaysInMonth) {
          count++;
          String color = "blue";
          if (day > 0 && day == dayOfMonth)
            color = "red";
          daysGrid += "<td align=center><a href=\"javascript:setDay('"+dayOfMonth + "')\"";
          daysGrid += " style='color:" + color + "'>";
          String dayString = dayOfMonth + "</a> ";
          if (dayString.length() == 6)
            dayString = "0" + dayString;
          daysGrid += dayString + "</td>";
        }
      }
      int dayspan = dayOfMonth - count;
      if (dayOfMonth < intDaysInMonth)
        daysGrid += "</tr>";
      else {
          if (dayspan < 7 && dayspan > 0) {
            for (int k = 0; k < dayspan; k++) {
              daysGrid += "<td>&nbsp;</td>";
            }
            daysGrid += "</tr>";
         }
        }
    }
    return daysGrid + "</table>";
  }
}
