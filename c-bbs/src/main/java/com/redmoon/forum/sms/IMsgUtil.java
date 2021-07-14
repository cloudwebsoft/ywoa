package com.redmoon.forum.sms;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.person.UserDb;

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
public interface IMsgUtil {
    public boolean send(UserDb user, String content, String sender) throws ErrMsgException ;
    public boolean send(String mobile, String content, String sender) throws ErrMsgException ;
    public void increaseTailAddr();
    public String getOrgAddr();
}
