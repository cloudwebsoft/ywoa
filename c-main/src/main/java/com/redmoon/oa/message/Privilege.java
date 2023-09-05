package com.redmoon.oa.message;

/**
 * <p>Title: 社区</p>
 * <p>Description: 社区</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: 镇江云网软件技术有限公司</p>
 * @author bluewind
 * @version 1.0
 */

import java.sql.*;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.db.*;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;

public class Privilege {
    String connname = Global.getDefaultDB();

    public Privilege() {
    }


    public boolean canSendDraft(HttpServletRequest request, int id) {
        MessageDb md = new MessageDb();
        md = (MessageDb)md.getMessageDb(id);
        if (md.getBox() == MessageDb.DRAFT)
            return false;
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (md.getSender().equals(pvg.getUser(request)))
            return true;
        else
            return false;
    }

    public boolean canManage(HttpServletRequest request, String[] ids) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String username = pvg.getUser(request);
        // 先验证是否为会员
        boolean isvalid = false;
        int len = ids.length;
        String str = "";
        for (int i = 0; i < len; i++)
            if (str.equals(""))
                str += ids[i];
            else
                str += "," + ids[i];
        str = "(" + str + ")";
        String receiver = null;
        String sql = "select receiver,box,sender from oa_message where id in " + str;
        Conn conn = new Conn(connname);
        try {
            ResultSet rs = conn.executeQuery(sql);
            if (rs != null) {
                while (rs.next()) {
                    receiver = rs.getString(1);
                    boolean isDraft =false;
                    if (rs.getInt(2)==MessageDb.DRAFT || rs.getInt(2)==MessageDb.OUTBOX){
                    	isDraft = true;
                    }
                    String sender = rs.getString(3);
                    if (isDraft) {
                        if (!sender.equals(username)) {
                            isvalid = false;
                            break;
                        }
                    }
                    else {
                        if (!receiver.equals(username)) {
                            isvalid = false;
                            break;
                        }
                    }
                    isvalid = true;
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("canManage:" + e.getMessage());
        } finally {
            conn.close();
        }

        return isvalid;
    }

}
