package com.redmoon.oa.fileark.robot;

import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import com.cloudwebsoft.framework.base.QObjectDb;
import com.redmoon.oa.db.SequenceManager;

import java.io.PrintStream;
import java.sql.SQLException;

public class RobotDb extends QObjectDb
        implements Cloneable {
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
        RobotDb rd = (RobotDb) clone();
        rd.set("name", rd.getString("name") + " copy");
        rd.set("id", new Integer((int) SequenceManager.nextID(76)));
        return rd.create();
    }

    public boolean Import(String robotName, String str) throws ResKeyException, ErrMsgException {
        Properties p = new Properties();
        try {
            p.load(str);
        } catch (Exception e) {
            System.out.println("Import: " + e.getMessage());
        }

        String[] ary = getFieldsFromQueryCreate();
        int len = ary.length;
        RobotDb rd = new RobotDb();
        try {
            rd.initQObject(new Object[]{new Integer(0)});
        } catch (SQLException e) {
            throw new ResKeyException("err_db");
        }
        for (int i = 0; i < len; i++) {
            rd.resultRecord.put(ary[i], RobotUtil.decode(p.getProperty(ary[i])));
        }

        rd.resultRecord.put("name", robotName);
        rd.resultRecord.put("id", new Integer((int) SequenceManager.nextID(76)));
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