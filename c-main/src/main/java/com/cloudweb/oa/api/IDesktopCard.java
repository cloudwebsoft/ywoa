package com.cloudweb.oa.api;

import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.module.desktop.DesktopCard;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

public interface IDesktopCard extends Serializable {
    long getId();

    String getName();

    String getTitle();

    int getStartVal();

    int getEndVal(HttpServletRequest request);

    int getEndValByFunc(DesktopCard desktopCard);

    boolean isLink();

    String getUrl();

    String getUnit();

    String getBgColor();

    String getIcon();

    JSONObject getQuery();
}
