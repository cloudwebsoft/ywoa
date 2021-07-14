package com.redmoon.forum.plugin.reward;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import com.redmoon.forum.plugin.base.IPluginMsgAction;
import org.apache.log4j.Logger;
import com.redmoon.forum.MsgDb;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.ResKeyException;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.forum.MsgMgr;
import com.redmoon.forum.plugin.ScoreMgr;
import com.redmoon.forum.plugin.ScoreUnit;
import cn.js.fan.web.SkinUtil;

public class RewardMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public RewardMsgAction() {
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
        boolean re = false;
        if (md.getReplyid() == -1) {
            RewardDb rd = new RewardDb();
            rd = rd.getRewardDb(md.getId());
            if (rd.getScoreGiven()>0)
                return true;
            String moneyCode = fu.getFieldValue("moneyCode");
            String strPoint = fu.getFieldValue("sum");
            int point = 0;
            if (StrUtil.isNumeric(strPoint)) {
                point = Integer.parseInt(strPoint);
            } else
                throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.reward","err_score"));//分值必须为整数！
            if (moneyCode.equals(""))
                throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.reward","err_choose_Currency"));//请选择一个币种
            // 检查用户的分值是否足够
            ScoreMgr sm = new ScoreMgr();
            ScoreUnit su = sm.getScoreUnit(moneyCode);
            double syScore = su.getScore().getUserSum(md.getName());
            if (syScore < point) {
                String str = SkinUtil.LoadString(request,"res.forum.plugin.reward","err_score_OutOfBalance");
                str = StrUtil.format(str,new Object[]{""+syScore});
                //throw new ErrMsgException("送的值超出了您的余额" + syScore + "！");//
                throw new ErrMsgException(str);
            }
            rd.setMoneyCode(moneyCode);
            rd.setScore(point);
            try {
                re = rd.save();
            }
            catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
        }
        else
            return true;
        return re;
    }

    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        // logger.info("AddNew:id=" + md.getId());
        String moneyCode = StrUtil.getNullString(fu.getFieldValue("moneyCode"));
        String strPoint = StrUtil.getNullStr(fu.getFieldValue("sum"));
        int point = 0;
        if (StrUtil.isNumeric(strPoint)) {
            point = Integer.parseInt(strPoint);
        }
        else
                throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.reward","err_score"));//分值必须为整数！
        if (moneyCode.equals(""))
                throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.reward","err_choose_Currency"));//请选择一个币种

        // 扣除用户的点数
        ScoreMgr sm = new ScoreMgr();
        ScoreUnit su = sm.getScoreUnit(moneyCode);
        try {
            su.getScore().pay(md.getName(), su.getScore().SELLER_SYSTEM, point);
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }

        RewardDb rd = new RewardDb();
        rd.setMsgId(md.getId());
        rd.setScore(point);
        rd.setMoneyCode(moneyCode);
        boolean re = false;
        try {
            re = rd.create();
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
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
        RewardDb rd = new RewardDb();
        rd = rd.getRewardDb(delId);
        if (rd.isLoaded())
            return rd.del();
        else
            return true;
    }

    public boolean AddReply(ServletContext application,
                            HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        return true;
    }

    /**
     *
     * @param request HttpServletRequest
     * @return int 0 表示失败 1 表示成功 2 表示结贴
     * @throws ErrMsgException
     */
    public int pay(HttpServletRequest request) throws ErrMsgException {
        long msgId = ParamUtil.getLong(request, "msgId");
        MsgMgr mm = new MsgMgr();
        MsgDb md = mm.getMsgDb(msgId);
        if (!md.isLoaded()) {
            throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.reward","err_isNotExsist"));//该贴已不存在！
        }

        // 检查用户是否为楼主
        RewardPrivilege rp = new RewardPrivilege();
        if (!rp.isOwner(request, md.getRootid())) {
            throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.reward","err_isOwner"));//您不是楼主，无权送分！
        }

        int score = ParamUtil.getInt(request, "score");

        if (score<=0) {
            throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.reward","score_invalid"));
        }

        RewardDb rootRd = new RewardDb();
        rootRd = rootRd.getRewardDb(md.getRootid());

        // 如果该贴已送过分了，则不允许再送
        RewardDb rd = rootRd.getRewardDb(msgId);
        if (rd.isLoaded()) {
            String moneyCode = rootRd.getMoneyCode();
            ScoreMgr sm = new ScoreMgr();
            ScoreUnit su = sm.getScoreUnit(moneyCode);
            String moneyName = "";
            if (su!=null)
               moneyName = su.getName(request);
            String str = SkinUtil.LoadString(request, "res.forum.plugin.reward", "err_isSended");
            str = StrUtil.format(str, new Object[] {""+moneyName});
            str = StrUtil.format(str, new Object[] {""+rd.getScore()});
            throw new ErrMsgException(str);
            //throw new ErrMsgException("该贴已经送过 " + moneyName + " " + rd.getScore() + "，不能再送！");
        }

        // 检查是否已结贴
        if (rootRd.isEnd()) {
            throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.reward","err_isEnd"));//该贴已被结贴，不能再送分！！
        }
        int r = 0;
        try {
            r = rootRd.doPay(md, score);
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return r;
    }
}
