package com.redmoon.oa;
import java.util.Calendar;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CalendarSheet {
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

  public CalendarSheet() {
    Calendar cal = Calendar.getInstance();
    displayYear = cal.get(Calendar.YEAR);
    displayMonth = cal.get(Calendar.MONTH);

    todayYear = displayYear;
    todayMonth = displayMonth;
    todayDay = cal.get(Calendar.DAY_OF_MONTH);
  }

  public int getCurYear() {
    return todayYear;
  }

  public int getCurMonth() {
    return todayMonth;
  }

  public int getCurDay() {
    return todayDay;
  }

  /**
   * 取得月份中的天数
   * @param month 从0开始
   * @param year
   * @return
   */
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
    int startDayOfWeek = newCal.get(Calendar.DAY_OF_WEEK );
    if ( (todayYear == newCal.get(Calendar.YEAR)) &&
        (todayMonth == newCal.get(Calendar.MONTH))) {
      day = todayDay;
    }
    int intDaysInMonth = getDays(newCal.get(Calendar.MONTH), newCal.get(Calendar.YEAR));
    String daysGrid = makeDaysGrid(startDayOfWeek, day, intDaysInMonth, newCal);
    return daysGrid;
  }
/*
  function incMonth(delta, eltName) {
    displayMonth += delta;
    if (displayMonth >= 12) {
      displayMonth = 0;
      incYear(1, eltName);
    }
    else if (displayMonth <= -1) {
      displayMonth = 11;
      incYear( -1, eltName);
    }
    else {
      newCalendar(eltName);
    }
  }

  function incYear(delta, eltName) {
    displayYear = parseInt(displayYear + '') + delta;
    newCalendar(eltName);
  }
*/
  public String changeCld_Script(String virtualpath) {
    String str = "";
    str = "<script language=javascript>\n";
    str += "<!--\n";
    str += "function changeCld(y,m)\n";
    str += "{\n";
    str += "document.location.href=\"" + virtualpath + "?displayMonth=\"+displayMonth.value+\"&displayYear=\"+displayYear.value\n";
    str += "}\n";
    str += "-->\n";
    str += "</script>\n";
    return str;
  }

  public String makeDaysGrid(int startDay, int day, int intDaysInMonth, Calendar newCal) {
    String daysGrid;
    int month = newCal.get(Calendar.MONTH);
    int year = newCal.get(Calendar.YEAR);
    boolean isThisYear = (year == todayYear);
    boolean isThisMonth = (day > -1);
    int forwardyear = displayYear+1;
    int backwordyear = displayYear-1;
    int forwardmonth = displayMonth +1;
    int backwordmonth = displayMonth -1;
    daysGrid = "<table align=center cellSpacing='0' borderColorDark='#ffffff' bgColor='#ebebeb' borderColorLight='#000000' border='1' width=80%>";
    daysGrid += "<tr bgColor='#336699'><td align=center colspan=7 nowrap>";
    daysGrid += "<font style='FONT-SIZE: 9pt' color='#ffffff'>&nbsp;&nbsp;&nbsp;&nbsp;查看&nbsp;<select style='FONT-SIZE: 9pt' onchange='changeCld()' id='displayYear' name='displayYear'>";
    for (int k=-50; k<50; k++)
    {
      daysGrid += "<option value="+(displayYear+k)+">"+(displayYear+k)+"</option>";
    }
    daysGrid += "</select>年";

    daysGrid += "<script language=javascript>\n";
    daysGrid += "<!--\n";
    daysGrid += "displayYear.value=\""+displayYear+"\"\n";
    daysGrid += "-->\n";
    daysGrid += "</script>\n";

    daysGrid += "<select style='FONT-SIZE: 9pt' onchange='changeCld()' name='displayMonth' id='displayMonth'>";
    for (int k=1; k<=12; k++) {
      daysGrid += "<option value="+(k-1)+">"+k+"</option>";
    }
    daysGrid += "</select>";

    daysGrid += "<script language=javascript>\n";
    daysGrid += "<!--\n";
    daysGrid += "displayMonth.value=\""+displayMonth+"\"\n";
    daysGrid += "-->\n";
    daysGrid += "</script>\n";

    daysGrid += "月</font>&nbsp;&nbsp;&nbsp;&nbsp;<font face='courier new, courier' size=2>";
    daysGrid += "<a style='color:white' href='?displayYear="+displayYear+"&displayMonth="+backwordmonth+"')>&laquo;</a>&nbsp;";
    daysGrid += "<b>";
    if (isThisMonth) {
      daysGrid += "<font color=yellow>" + months[month] + "</font>";
    }
    else {
      daysGrid += "<font color=white>" + months[month] + "</font>";
    }
    daysGrid += "</b>";
    daysGrid += "&nbsp;<a style='color:white' href='?displayMonth="+forwardmonth+"&displayYear="+displayYear+"')>&raquo;</a>";
    daysGrid += "&nbsp;&nbsp;&nbsp;";
    daysGrid += "<a style='color:white' href='?displayMonth="+displayMonth+"&displayYear="+backwordyear+"')>&laquo;</a>&nbsp;";
    daysGrid += "<b>";
    if (isThisYear) {
      daysGrid += "<font color=yellow>" +year + "&nbsp;年</font>";
    }
    else {
      daysGrid += "<font color=white>" +year + "&nbsp;年</font>";
    }
    daysGrid += "</b>";
    daysGrid += "&nbsp;<a style='color:white' href='?displayMonth="+displayMonth+"&displayYear="+forwardyear+"')>&raquo;</a></td></tr>";
    daysGrid += "<tr bgColor=#bebebe><td align=center><font color=red>日</font></td><td align=center>一</td><td align=center>二</td>";
    daysGrid += "<td align=center>三</td><td align=center>四</td><td align=center>五</td><td align=center><font color=red>六</font></td></tr>";
    int dayOfMonthOfFirstSunday = 7-startDay+2;//(7 - startDay + 1)//在javascript中因为星期是从0-6计数的,而java中则是从1-7计数;
    int count = 0;
    int dayOfMonth = 0;
    for (int intWeek = 0; intWeek < 6; intWeek++) {
      daysGrid += "<tr height=40>";
      for (int intDay = 0; intDay < 7; intDay++) {
        dayOfMonth = (intWeek * 7) + intDay + dayOfMonthOfFirstSunday - 7;
        if (dayOfMonth <= 0) {
          daysGrid += "<td>&nbsp;</td>";
        }
        else if (dayOfMonth <= intDaysInMonth) {
          count++;
          String color = "black";
          if (intDay==0 || intDay==6)
            color = "red";
          if (day > 0 && day == dayOfMonth)
            color = "blue";
          daysGrid += "<td align=center><a href=\"#\"";
          daysGrid += " style='font-size:15pt;FONT-FAMILY:Arial;color:" + color + "'><b>";
          String dayString = dayOfMonth + "</b></a> ";
          //if (dayString.length() == 13)
          //  dayString = "0" + dayString;
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
