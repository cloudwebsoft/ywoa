package com.redmoon.oa.emailpop3.pop3;

import javax.servlet.http.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GetMailStatus {

    public GetMailStatus() {
    }

    public static GetMailStatus getFromSession(HttpServletRequest request, String emailAddress) {
        HttpSession session = request.getSession(true);
        return (GetMailStatus)session.getAttribute("MAIL_STATUS_" + emailAddress);
    }

    public static void storeIntoSession(HttpServletRequest request, String emailAddress, GetMailStatus gms) {
        HttpSession session = request.getSession(true);
        session.setAttribute("MAIL_STATUS_" + emailAddress, gms);
    }

    public static void removeFromSession(HttpServletRequest request, String emailAddress) {
        HttpSession session = request.getSession(true);
        session.removeAttribute("MAIL_STATUS_" + emailAddress);
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setStoredCount(int storedCount) {
        this.storedCount = storedCount;
    }

    public void setOver(boolean over) {
        this.over = over;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getCount() {
        return count;
    }

    public int getStoredCount() {
        return storedCount;
    }

    public boolean isOver() {
        return over;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * 新邮件的数目
     */
    private int count = 0;

    /**
     * 已保存的邮件数目
     */
    private int storedCount = 0;

    /**
     * 接收邮件是否已结束
     */
    private boolean over = false;
    private long startTime;
}
