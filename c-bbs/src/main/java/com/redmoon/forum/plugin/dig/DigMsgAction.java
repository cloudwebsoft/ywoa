package com.redmoon.forum.plugin.dig;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.base.IPluginMsgAction;
import com.redmoon.kit.util.FileUpload;
import org.apache.log4j.Logger;
import com.redmoon.forum.Privilege;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.plugin.ScoreMgr;
import com.redmoon.forum.plugin.ScoreUnit;
import com.redmoon.forum.plugin.base.IPluginScore;
import com.redmoon.forum.MsgCache;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.forum.SequenceMgr;

public class DigMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public DigMsgAction() {
    }

    /**
     *
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param md MsgDb 所存储的是ReceiveData后得来的信息
     * @return boolean
     * @throws ErrMsgException
     */
    public synchronized boolean editTopic(ServletContext application,
                                          HttpServletRequest request,
                                          MsgDb md, FileUpload fu) throws
            ErrMsgException {
        return true;
    }

    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {

        return true;
    }

    public boolean AddQuickReply(ServletContext application,
                                 HttpServletRequest request, long replyMsgId) throws
            ErrMsgException {
        return true;
    }

    /**
     * 本方法置于MsgMgr中delTopic真正删除贴子之前，使在删除插件相应内容后，再删除贴子本身
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param md MsgDb
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean delTopic(ServletContext application,
                            HttpServletRequest request, MsgDb md) throws
            ErrMsgException {
        return true;
    }

    /**
     * 此函数置于MsgDb delSingleMsg中真正删除贴子之前，以便于递归删除贴子
     * @param delId int
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean delSingleMsg(long delId) throws
            ResKeyException {

        return true;
    }

    public boolean AddReply(ServletContext application,
                            HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        return true;
    }

    public boolean dig(HttpServletRequest request) throws ErrMsgException,ResKeyException {
        if (!Privilege.isUserLogin(request))
            throw new ErrMsgException("请先登录!");
        long msgId = ParamUtil.getLong(request, "msgId");
        String scoreCode = ParamUtil.get(request, "scoreCode");
        String userName = Privilege.getUser(request);

        ScoreMgr sm = new ScoreMgr();
        ScoreUnit su = sm.getScoreUnit(scoreCode);

        DigConfig dc = DigConfig.getInstance();
        String spay = dc.getProperty("undig", "type", scoreCode, "pay");
        if (spay.equals(""))
            throw new ErrMsgException(su.getName(request) + " 不能参与掘客!");
        double pay = StrUtil.toDouble(spay, 0);
        double reward = StrUtil.toDouble(dc.getProperty("dig", "type", scoreCode, "reward"), 0);

        boolean re = su.getScore().pay(userName, IPluginScore.SELLER_SYSTEM, pay);
        if (re) {
            MsgDb md = new MsgDb();
            md = md.getMsgDb(msgId);
            // System.out.println(getClass() + " reward=" + reward + " md.getScore()=" + md.getScore());
            md.setScore(md.getScore() + reward);
            re = md.updateScore();
            if (re) {
                // 挖掘记录
                DigDb dd = new DigDb();
                dd.create(new JdbcTemplate(), new Object[] {
                    new Long(SequenceMgr.nextID(SequenceMgr.DIG)),
                            new Long(msgId), scoreCode, new Double(pay),
                            new Double(reward), userName, new java.util.Date()
                });
                // 刷新缓存
                dd.refreshList("" + msgId);
            }
        }
        return re;
    }

    public boolean undig(HttpServletRequest request) throws ErrMsgException,ResKeyException {
        if (!Privilege.isUserLogin(request))
            throw new ErrMsgException("请先登录!");
        long msgId = ParamUtil.getLong(request, "msgId");
        String scoreCode = ParamUtil.get(request, "scoreCode");
        String userName = Privilege.getUser(request);

        ScoreMgr sm = new ScoreMgr();
        ScoreUnit su = sm.getScoreUnit(scoreCode);

        DigConfig dc = DigConfig.getInstance();
        String spay = dc.getProperty("undig", "type", scoreCode, "pay");
        if (spay.equals(""))
            throw new ErrMsgException(su.getName(request) + " 不能参与掘客!");
        double pay = StrUtil.toDouble(spay, 0);
        double reward = StrUtil.toDouble(dc.getProperty("undig", "type", scoreCode, "reward"), 0);

        boolean re = su.getScore().pay(userName, IPluginScore.SELLER_SYSTEM, pay);
        if (re) {
            MsgDb md = new MsgDb();
            md = md.getMsgDb(msgId);
            // System.out.println(getClass() + " reward=" + reward + " md.getScore()=" + md.getScore());
            md.setScore(md.getScore() + reward);
            re = md.updateScore();
            if (re) {
                // 挖掘记录
                DigDb dd = new DigDb();
                dd.create(new JdbcTemplate(), new Object[] {
                    new Long(SequenceMgr.nextID(SequenceMgr.DIG)),
                            new Long(msgId), scoreCode, new Double(pay),
                            new Double(reward), userName, new java.util.Date()
                });
                dd.refreshList("" + msgId);
            }
        }
        return re;
    }
}
