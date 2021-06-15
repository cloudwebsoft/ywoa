package com.redmoon.forum.test;
import java.text.*;
import java.util.*;

import com.redmoon.t.TMsgMgr;

public class DateExample {

   public static void main(String args[]) {
	   //TMsgMgr.parseAt("ɭ  dfs@cloudweb()  xxx");
	   
	   if (true) return;

     // Get the Date
     Date now = new Date();

     // Get date formatters for default, German, and French locales
     DateFormat theDate = DateFormat.getDateInstance(DateFormat.MEDIUM);
     DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.MEDIUM);
     DateFormat germanDate = DateFormat.getDateInstance(DateFormat.LONG, Locale.GERMANY);
     DateFormat frenchDate = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);

     // Format and print the dates
     System.out.println("Date in the default locale: " + theDate.format(now));
     System.out.println("Date time in the default locale: " + dateTimeFormat.format(now));
     System.out.println("Date in the German locale : " + germanDate.format(now));
     System.out.println("Date in the French locale : " + frenchDate.format(now));

     for (int i=32; i<150; i++) {
         System.out.print((char)i);
     }

     System.out.println("--------------");

     for (int i=160; i<384; i++) {
         System.out.println((char)i);
     }

     int[] ary2 = new int[1];
     System.out.println("ary2[0]=" + ary2[0]);

     String[] ary = "ddd=".split("=");
     System.out.println("ary len=" + ary.length);
   }
}
