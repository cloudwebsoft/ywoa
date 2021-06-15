package com.redmoon.forum.robot;

import cn.js.fan.db.ResultRecord;
import cn.js.fan.module.cms.robot.Properties;
import cn.js.fan.module.cms.robot.RobotUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.base.QObjectDb;
import com.redmoon.forum.SequenceMgr;
import com.redmoon.forum.person.UserDb;
import com.redmoon.forum.person.UserMgr;
import java.io.PrintStream;
import java.sql.SQLException;

public class RobotDb extends QObjectDb
        implements Cloneable
{
    public boolean create(ParamChecker paramChecker)
            throws ResKeyException, ErrMsgException
    {
        String topic_user_name = paramChecker.getString("topic_user_name");
        topic_user_name = topic_user_name.replaceAll("，", ",");
        String[] ary = StrUtil.split(topic_user_name, ",");
        int len = ary.length;
        UserMgr um = new UserMgr();
        for (int i = 0; i < len; i++) {
            UserDb ud = um.getUserDbByNick(ary[i]);
            if ((ud != null) && (!ud.isLoaded())) {
                throw new ErrMsgException("用户" + ary[i] + "不存在！");
            }

        }

        return super.create(paramChecker);
    }

    public boolean save(ParamChecker paramChecker) throws ResKeyException, ErrMsgException
    {
        String topic_user_name = paramChecker.getString("topic_user_name");
        topic_user_name = topic_user_name.replaceAll("，", ",");
        String[] ary = StrUtil.split(topic_user_name, ",");
        int len = ary.length;
        UserMgr um = new UserMgr();
        for (int i = 0; i < len; i++) {
            UserDb ud = um.getUserDbByNick(ary[i]);
            if ((ud != null) && (!ud.isLoaded())) {
                throw new ErrMsgException("用户" + ary[i] + "不存在！");
            }

        }

        return super.save(paramChecker);
    }

    public Object clone() {
        Object o = null;
        try {
            o = super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return o;
    }

    public boolean copy() throws ResKeyException, ErrMsgException {
        RobotDb rd = (RobotDb)clone();
        rd.set("name", rd.getString("name") + " copy");
        rd.set("id", new Integer((int)SequenceMgr.nextID(26)));
        return rd.create();
    }

    public boolean Import(String robotName, String str) throws ResKeyException, ErrMsgException {
        Properties p = new Properties();
        try {
            p.load(str);
        }
        catch (Exception e) {
            System.out.println("PropertiesUtil: getValue " + e.getMessage());
        }

        String[] ary = getFieldsFromQueryCreate();
        int len = ary.length;
        RobotDb rd = new RobotDb();
        try {
            rd.initQObject(new Object[] { new Integer(0) });
        }
        catch (SQLException e) {
            throw new ResKeyException("err_db");
        }
        for (int i = 0; i < len; i++) {
            rd.resultRecord.put(ary[i], RobotUtil.decode(p.getProperty(ary[i])));
        }

        rd.resultRecord.put("name", robotName);
        rd.resultRecord.put("id", new Integer((int)SequenceMgr.nextID(26)));
        return rd.create();
    }

    public String Export() {
        StringBuffer sb = new StringBuffer();
        String[] ary = getFieldsFromQueryCreate();
        int len = ary.length;
        for (int i = 0; i < len; i++) {
            if (!ary[i].equals("id"))
                sb.append(ary[i] + "=" + RobotUtil.encode(this.resultRecord.getString(ary[i])) + "\n");
        }
        return sb.toString();
    }
}