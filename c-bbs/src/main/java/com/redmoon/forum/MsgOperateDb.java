package com.redmoon.forum;

import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.person.UserDb;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import java.sql.SQLException;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

/**
 * <p>Title: 贴子加精、置顶、加亮等的记录</p>
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
public class MsgOperateDb extends QObjectDb {
    public static int OP_TYPE_ELITE = 0;
    public static int OP_TYPE_TOP_BOARD = 1;
    public static int OP_TYPE_TOP_FORUM = 2;
    public static int OP_TYPE_COLOR = 3;
    public static int OP_TYPE_BOLD = 4;
    public static int OP_TYPE_MOVE = 5;

    public static int OP_TYPE_ELITE_CANCEL = 6;
    public static int OP_TYPE_TOP_CANCEL = 7;

    public static int OP_TYPE_RISE = 8;
    public static int OP_TYPE_FALL = 9;

    public static int OP_TYPE_MERGE = 10;

    public static int OP_TYPE_DEL = 11;
    
    public static int OP_TYPE_CHECK = 12;

    public static String OPERATOR_MASTER = ""; // 后台管理人员

    public MsgOperateDb() {
    }

    public MsgOperateDb getMsgOperateDb(long id) {
        MsgOperateDb mod = (MsgOperateDb) getQObjectDb(new Long(id));
        return mod;
    }

    public boolean create(JdbcTemplate jt, Object[] params) throws ResKeyException {
        boolean re = super.create(jt, params);
        if (re) {
            long id = ((Long)params[0]).longValue();
            Long msgId = (Long)params[1];
            MsgDb md = new MsgDb();
            md = md.getMsgDb(msgId.longValue());
            md.setLastOperate(id);
            md.save();
        }
        return re;
    }
    
    public MsgOperateDb getLastOperate(long msgId, int opType) {
        String sql = "select id from " + getTable().getName() + " where msg_id=? and op_type=? order by op_date desc";
        try {
        	JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[]{new Long(msgId), new Integer(opType)});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                return getMsgOperateDb(rr.getLong(1));
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getOperateBefore:" + e.getMessage());
        }
        return null;
    }

    public long getOperateBefore(JdbcTemplate jt) {
        String sql = "select id from " + getTable().getName() + " where msg_id=? order by op_date desc";
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[]{new Long(getLong("msg_id"))}, 1, 1);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                return rr.getLong(1);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getOperateBefore:" + e.getMessage());
        }
        return MsgDb.LAST_OPERATE_NONE;
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        boolean re = super.del(jt);
        if (re) {
            long msgId = getLong("msg_id");
            long id = getLong("id");
            MsgDb md = new MsgDb();
            md = md.getMsgDb(msgId);
            // 判断是否已被删除
            if (md.isLoaded() && md.getLastOperate()==id) {
                // 取得上一个操作
                long beforeId = getOperateBefore(jt);
                md.setLastOperate(beforeId);
                md.save();
            }
        }
        return re;
    }

    public String getOperate(HttpServletRequest request) {
        return getOperate(request, getInt("op_type"));
    }

    public String getOperate(HttpServletRequest request, int opType) {
        String op = "";
        if (opType == OP_TYPE_ELITE)
            op = SkinUtil.LoadString(request, "res.forum.MsgDb", "OP_TYPE_ELITE");
        else if (opType == OP_TYPE_TOP_BOARD) {
            op = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                     "OP_TYPE_TOP_BOARD");
        } else if (opType == OP_TYPE_TOP_FORUM) {
            op = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                     "OP_TYPE_TOP_FORUM");
        } else if (opType == OP_TYPE_COLOR) {
            op = SkinUtil.LoadString(request, "res.forum.MsgDb", "OP_TYPE_COLOR");
        } else if (opType == OP_TYPE_BOLD) {
            op = SkinUtil.LoadString(request, "res.forum.MsgDb", "OP_TYPE_BOLD");
        } else if (opType == OP_TYPE_MOVE) {
            op = SkinUtil.LoadString(request, "res.forum.MsgDb", "OP_TYPE_MOVE");
        } else if (opType == OP_TYPE_ELITE_CANCEL) {
            op = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                     "OP_TYPE_ELITE_CANCEL");
        } else if (opType == OP_TYPE_TOP_CANCEL) {
            op = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                     "OP_TYPE_TOP_CANCEL");
        }
        else if (opType == OP_TYPE_RISE) {
            op = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                     "OP_TYPE_RISE");
        } else if (opType == OP_TYPE_FALL) {
            op = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                     "OP_TYPE_FALL");
        } else if (opType == OP_TYPE_MERGE) {
            op = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                     "OP_TYPE_MERGE");
        } else if (opType == OP_TYPE_DEL) {
            op = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                     "OP_TYPE_DEL");
        }
        else if (opType == OP_TYPE_CHECK) {
        	op = SkinUtil.LoadString(request, "res.forum.MsgDb", "OP_TYPE_CHECk");
        }
        return op;
    }

    public String getOperateDesc(HttpServletRequest request) {
        String desc = SkinUtil.LoadString(request, "res.forum.MsgDb", "op_desc");

        String userName = StrUtil.getNullStr(getString("user_name"));
        if (!userName.equals(OPERATOR_MASTER)) {
            UserDb ud = new UserDb();
            ud = ud.getUser(userName);
            userName = ud.getNick();
        }
        else
            userName = "Administrator";
        String op = getOperate(request);
        return StrUtil.format(desc, new Object[] {userName, ForumSkin.formatDateTime(request, getDate("op_date")), op});
    }
}
