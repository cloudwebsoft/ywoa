package com.redmoon.forum.plugin.score;

import com.redmoon.forum.plugin.base.IPluginScore;
import com.redmoon.forum.MsgDb;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import com.redmoon.forum.plugin.ScoreUnit;
import com.redmoon.forum.plugin.ScoreMgr;
import org.apache.log4j.Logger;
import com.redmoon.forum.BoardScoreDb;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.person.UserDb;
import cn.js.fan.util.ResKeyException;
import com.redmoon.forum.plugin.ScoreExchangeConfig;
import com.redmoon.forum.plugin.ScoreRecordDb;

public class Gold implements IPluginScore {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public static final String code = "gold";
    public ScoreUnit scoreUnit;

    public Gold() {
        scoreUnit = getUnit();
    }

    public ScoreUnit getUnit() {
        ScoreMgr sm = new ScoreMgr();
        return sm.getScoreUnit(code);
    }

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

    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md) throws
            ErrMsgException {
        if (!isPluginBoard(md.getboardcode()))
            return true;

        Privilege pvg = new Privilege();
        UserDb user = new UserDb();
        user = user.getUser(pvg.getUser(request));
        user.setGold(user.getGold() + scoreUnit.getAddValue());
        boolean re = user.save();
        if (re) {
            ScoreRecordDb.recordSysOperate(user.getName(), md.getId(), code, scoreUnit.getAddValue(), ScoreRecordDb.OPERATION_PAY, ScoreRecordDb.OP_TYPE_ADD_NEW);
        }
        return re;
    }

    public boolean AddQuickReply(ServletContext application,
                                 HttpServletRequest request, long replyMsgId) throws
            ErrMsgException {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(replyMsgId);

        if (!isPluginBoard(md.getboardcode()))
            return true;

        Privilege pvg = new Privilege();
        UserDb user = new UserDb();
        user = user.getUser(pvg.getUser(request));
        user.setGold(user.getGold() + scoreUnit.getReplyValue());
        boolean re = user.save();
        if (re) {
            ScoreRecordDb.recordSysOperate(user.getName(), md.getId(), code, scoreUnit.getReplyValue(), ScoreRecordDb.OPERATION_PAY, ScoreRecordDb.OP_TYPE_ADD_REPLY);
        }
        return re;
    }

    public boolean delSingleMsg(MsgDb md) throws
            ResKeyException {
        if (!isPluginBoard(md.getboardcode()))
            return true;

        UserDb user = new UserDb();
        user = user.getUser(md.getName());
        user.setGold(user.getGold() + scoreUnit.getDelValue());
        boolean re = user.save();
        if (re) {
            ScoreRecordDb.recordSysOperate(user.getName(), md.getId(), code, scoreUnit.getDelValue(), ScoreRecordDb.OPERATION_PAY, ScoreRecordDb.OP_TYPE_DEL_MSG);
        }
        return re;
    }

    public boolean AddReply(ServletContext application,
                          HttpServletRequest request, MsgDb md) throws
            ErrMsgException {

        if (!isPluginBoard(md.getboardcode()))
            return true;

        Privilege pvg = new Privilege();
        UserDb user = new UserDb();
        user = user.getUser(pvg.getUser(request));
        user.setGold(user.getGold() + scoreUnit.getReplyValue());
        boolean re = user.save();
        if (re) {
            ScoreRecordDb.recordSysOperate(user.getName(), md.getId(), code, scoreUnit.getReplyValue(), ScoreRecordDb.OPERATION_PAY, ScoreRecordDb.OP_TYPE_ADD_REPLY);
        }
        return re;
    }

    public boolean setElite(MsgDb md, int isElite) throws ResKeyException {
        UserDb user = new UserDb();
        user = user.getUser(md.getName());
        int d = scoreUnit.getEliteValue();
        if (isElite==0)
            d = -d;
        user.setGold(user.getGold() + d);
        boolean re = user.save();
        if (re) {
            ScoreRecordDb.recordSysOperate(user.getName(), md.getId(), code, d, ScoreRecordDb.OPERATION_PAY, ScoreRecordDb.OP_TYPE_SET_ELITE);
        }
        return re;
    }

    public void regist(UserDb user) throws ErrMsgException {
        user.setGold(scoreUnit.getRegistValue());
        ScoreRecordDb.recordSysOperate(user.getName(), ScoreRecordDb.MSG_ID_NONE, code, scoreUnit.getRegistValue(), ScoreRecordDb.OPERATION_PAY, ScoreRecordDb.OP_TYPE_REGIST);
    }

    public void login(UserDb user) throws ErrMsgException {
        user.setGold(user.getGold() + scoreUnit.getLoginValue());
        user.save();

        ScoreRecordDb.recordSysOperate(user.getName(), ScoreRecordDb.MSG_ID_NONE, code, scoreUnit.getLoginValue(), ScoreRecordDb.OPERATION_PAY, ScoreRecordDb.OP_TYPE_LOGIN);
    }

    public boolean pay(String buyer, String seller, double value) throws ResKeyException {
        int sum = 0;
        if (!buyer.equals(SELLER_SYSTEM)) {
            sum = (int) getUserSum(buyer);
            if (sum < value) {
                UserDb ud = new UserDb();
                ud = ud.getUser(buyer);
                throw new ResKeyException("res.forum.plugin.score.Gold",
                                          "err_inadequate", new Object[] {ud.getNick(),
                                          "" + sum, "" + (int)value});
            }
            // throw new ErrMsgException("金币不足！ " + buyer + "的金币总额为：" + sum);
        }

        boolean re = false;
        if (seller.equals(SELLER_SYSTEM)) {
            UserDb ud = new UserDb();
            UserDb buser = ud.getUser(buyer);
            buser.setGold((int)(buser.getGold() - value));
            re =  buser.save();
        }
        else {
            if (buyer.equals(SELLER_SYSTEM)) {
                UserDb ud = new UserDb();
                UserDb suser = ud.getUser(seller);
                suser.setGold(suser.getGold() + (int) value);
                re = suser.save();
            }
            else {
                UserDb ud = new UserDb();
                UserDb buser = ud.getUser(buyer);
                UserDb suser = ud.getUser(seller);
                buser.setGold(buser.getGold() - (int) value);
                if (buser.save()) {
                    suser.setGold(suser.getGold() + (int) value);
                    re = suser.save();
                }
            }
        }

        if (re) {
            ScoreRecordDb srd = new ScoreRecordDb();
            srd.setBuyer(buyer);
            srd.setSeller(seller);
            srd.setFromScore(code);
            srd.setToScore(code);
            srd.setFromValue(value);
            srd.setToValue(value);
            srd.setOperation(ScoreRecordDb.OPERATION_PAY);
            srd.create();
        }

        return re;
    }

    public double getUserSum(String userName) {
        UserDb ud = new UserDb();
        ud = ud.getUser(userName);
        return ud.getGold();
    }

    public boolean changeUserSum(String userName, double valueDlt) {
        UserDb ud = new UserDb();
        ud = ud.getUser(userName);
        ud.setGold(ud.getGold() + (int)valueDlt);
        return ud.save();
    }

    public boolean onAddAttachment(String userName, int attachmentCount) {
        UserDb user = new UserDb();
        user = user.getUser(userName);
        user.setGold(user.getGold() + scoreUnit.getAddAttachmentValue() * attachmentCount);
        boolean re = user.save();
        if (re)
            ScoreRecordDb.recordSysOperate(user.getName(), ScoreRecordDb.MSG_ID_NONE, code, scoreUnit.getAddAttachmentValue() * attachmentCount, ScoreRecordDb.OPERATION_PAY, ScoreRecordDb.OP_TYPE_ADD_ATTACHMENT);
        return re;
    }

    public boolean onDelAttachment(String userName, int attachmentCount) {
        UserDb user = new UserDb();
        user = user.getUser(userName);
        user.setGold(user.getGold() + scoreUnit.getDelAttachmentValue() * attachmentCount);
        boolean re = user.save();
        if (re) {
            ScoreRecordDb.recordSysOperate(user.getName(), ScoreRecordDb.MSG_ID_NONE, code, scoreUnit.getDelAttachmentValue() * attachmentCount, ScoreRecordDb.OPERATION_PAY, ScoreRecordDb.OP_TYPE_DEL_ATTACHMENT);
        }
        return re;
    }

    public boolean exchange(String userName, String toScore,
                            double value) throws ResKeyException {

        if(toScore.equals(code)){
            throw new ResKeyException("res.forum.plugin.score.Gold","err_code");
        }

        int sum = 0, exchangeMin = 0;
        sum = (int) getUserSum(userName);
        ScoreExchangeConfig secfg = new ScoreExchangeConfig();
        String tax = secfg.getProperty("tax");
        exchangeMin = secfg.getIntProperty("exchangemin");

        UserDb ud = new UserDb();
        ud = ud.getUser(userName);
        if (sum < value || sum < exchangeMin) {
            throw new ResKeyException("res.forum.plugin.score.Gold","err_inadequate", new Object[] {ud.getNick(),"" + sum, "" + (int) value});
        }

        ud.setGold(ud.getGold() - (int) value);
        if (ud.save()) {
            ScoreMgr sm = new ScoreMgr();
            ScoreUnit fromSu = sm.getScoreUnit(code);
            int fromRatio = fromSu.getRatio();
            ScoreUnit toSu = sm.getScoreUnit(toScore);
            int toRatio = toSu.getRatio();

            toSu.getScore().changeUserSum(userName, (int)(value * fromRatio * ((double)1 - Double.parseDouble(tax)) / toRatio));

            ScoreRecordDb srd = new ScoreRecordDb();
            srd.setBuyer(userName);
            srd.setSeller(userName);
            srd.setFromScore(code);
            srd.setToScore(toScore);
            srd.setFromValue(value);
            srd.setToValue(value * fromRatio *
                                  ((double) 1 - Double.parseDouble(tax)) /
                                  toRatio);
            srd.setOperation(ScoreRecordDb.OPERATION_EXCHANGE);
            return srd.create();

        }
        return true;
    }

    public boolean transfer(String fromUserName, String toUserName, double value) throws
            ResKeyException {
        if (fromUserName.equals(toUserName)) {
            throw new ResKeyException("res.forum.plugin.score.Gold", "err_name");
        }

        int sum = 0, transferMin = 0;
        sum = (int) getUserSum(fromUserName);
        ScoreExchangeConfig secfg = new ScoreExchangeConfig();
        String tax = secfg.getProperty("tax");
        transferMin = secfg.getIntProperty("transfermin");

        UserDb ud = new UserDb();
        UserDb fuser = ud.getUser(fromUserName);
        if (sum < value || sum < transferMin) {
            throw new ResKeyException("res.forum.plugin.score.Gold","err_inadequate", new Object[] {fuser.getNick(),"" + sum, "" + (int) value});
        }

        UserDb tuser = ud.getUser(toUserName);
        fuser.setGold(fuser.getGold() - (int) value);
        if (fuser.save()) {
            tuser.setGold(tuser.getGold() + (int) (value * ((double) 1 - Double.parseDouble(tax))));
            tuser.save();
            ScoreRecordDb srd = new ScoreRecordDb();
            srd.setBuyer(fromUserName);
            srd.setSeller(toUserName);
            srd.setFromScore(code);
            srd.setFromValue(value);
            srd.setToValue(value *
                                  ((double) 1 - Double.parseDouble(tax)));
            srd.setOperation(ScoreRecordDb.OPERATION_TRANSFER);
            return srd.create();
        }
        return true;
    }
}
