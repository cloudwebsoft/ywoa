package com.cloudweb.oa.module.desktop;

import lombok.Data;

@Data
public class DesktopCard {
    long id;
    String name;
    String title;
    int count = 0;
    double sum = 0.00;
    String moduleCode;
    String cardType;
    String menuItem;
    int startVal = 0;
    boolean link;
    String url;
    int orders = 0;
    String unit;
    String bgColor;
    String icon;
    String roles;
    int style = 0;
    String endValfunc;
    String color;
}
