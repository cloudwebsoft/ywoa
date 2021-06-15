package com.redmoon.forum.plugin.score;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.person.UserDb;
import com.redmoon.forum.plugin.ScoreUnit;
import com.redmoon.forum.plugin.base.IPluginScore;
import org.apache.log4j.Logger;
import com.redmoon.forum.plugin.ScoreMgr;
import com.redmoon.forum.BoardScoreDb;
import cn.js.fan.util.ResKeyException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class RMB implements IPluginScore {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public static final String code = "rmb";
    public ScoreUnit scoreUnit;

    public RMB() {
    }

    /**
     * AddNew
     *
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param md MsgDb
     * @return boolean
     * @throws ErrMsgException
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginScore method
     */
    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md) throws
            ErrMsgException {
        return true;
    }

    public void login(UserDb user) throws ErrMsgException {
    }
    /**
     * AddQuickReply
     *
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param replyMsgId int
     * @return boolean
     * @throws ErrMsgException
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginScore method
     */
    public boolean AddQuickReply(ServletContext application,
                                 HttpServletRequest request, long replyMsgId) throws
            ErrMsgException {
        return true;
    }

    /**
     * AddReply
     *
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param md MsgDb
     * @return boolean
     * @throws ErrMsgException
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginScore method
     */
    public boolean AddReply(ServletContext application,
                            HttpServletRequest request, MsgDb md) throws
            ErrMsgException {
        return true;
    }

    /**
     * delSingleMsg
     *
     * @param md MsgDb
     * @return boolean
     * @throws ErrMsgException
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginScore method
     */
    public boolean delSingleMsg(MsgDb md) throws ResKeyException {
        return true;
    }

    /**
     * getUnit
     *
     * @return ScoreUnit
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginScore method
     */
    public ScoreUnit getUnit() {
        ScoreMgr sm = new ScoreMgr();
        return sm.getScoreUnit(code);
    }

    /**
     * isPluginBoard
     *
     * @param boardCode String
     * @return boolean
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginScore method
     */
    public boolean isPluginBoard(String boardCode) {
        if (scoreUnit.getType().equals(scoreUnit.TYPE_FORUM))
            return true;
        BoardScoreDb be = new BoardScoreDb();
        be = be.getBoardScoreDb(boardCode, code);
        if (be.isLoaded())
            return true;
        else
            return false;
    }

    /**
     * regist
     *
     * @param user UserDb
     * @throws ErrMsgException
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginScore method
     */
    public void regist(UserDb user) throws ErrMsgException {
    }

    /**
     * setElite
     * @param md MsgDb
     * @param isElite int
     * @return boolean
     * @throws ErrMsgException
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginScore method
     */
    public boolean setElite(MsgDb md, int isElite) throws ResKeyException {
        return true;
    }

    public boolean pay(String buyer, String seller, double value) throws ResKeyException {
        return false;
    }

    public double getUserSum(String userName) {
        return 0;
    }

    public boolean changeUserSum(String userName, double valueDlt) {
        return true;
    }

    public boolean onAddAttachment(String userName, int attachmentCount) {
        return true;
    }

    public boolean onDelAttachment(String userName, int attachmentCount) {
        return true;
    }

    public boolean exchange(String userName, String toScore,
                            double value) throws ResKeyException {
        return true;
    }

    public boolean transfer(String fromUserName, String toUserName, double value) throws
        ResKeyException {
    return true;
}


}
