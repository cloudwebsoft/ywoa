package com.redmoon.oa.fileark.robot;

import java.util.Vector;

public class RobotInfo {
    public static String SESSION_VAR_GATHER_INFO = "cms_gather_info";
    public String[] listPageUrls;
    public int curListPageUrlsIndex = 0;
    public Vector docPageUrls;
    public int curDocPageUrlsIndex = 0;

    public int count = 0;

    public RobotInfo(String[] listPageUrls) {
        this.listPageUrls = listPageUrls;
    }
}