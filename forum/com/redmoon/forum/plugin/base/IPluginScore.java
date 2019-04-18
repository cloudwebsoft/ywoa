package com.redmoon.forum.plugin.base;

import com.redmoon.forum.plugin.ScoreUnit;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.MsgDb;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import com.redmoon.forum.person.UserDb;
import cn.js.fan.util.ResKeyException;

public interface IPluginScore {
    public static String SELLER_SYSTEM = "forum";

    public boolean isPluginBoard(String boardCode);
    public ScoreUnit getUnit();

    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md) throws
            ErrMsgException ;

    public boolean AddQuickReply(ServletContext application,
                                 HttpServletRequest request, long replyMsgId) throws
            ErrMsgException;

    public boolean delSingleMsg(MsgDb md) throws
            ResKeyException;

    public boolean AddReply(ServletContext application,
                          HttpServletRequest request, MsgDb md) throws
            ErrMsgException;

    public boolean setElite(MsgDb md, int isElite) throws ResKeyException;

    public void regist(UserDb user) throws ErrMsgException;

    public void login(UserDb user) throws ErrMsgException;

    public boolean pay(String buyer, String seller, double value) throws ResKeyException;

    public double getUserSum(String userName);

    public boolean changeUserSum(String userName, double valueDlt);

    public boolean onAddAttachment(String userName, int attachmentCount);

    public boolean onDelAttachment(String userName, int attachmentCount);

    public boolean exchange(String userName, String toScore, double value) throws ResKeyException;

    public boolean transfer(String userName, String toUserName, double value) throws ResKeyException;
}
