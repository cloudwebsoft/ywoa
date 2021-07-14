package com.redmoon.forum.message;

/**
 * <p>Title: 社区</p>
 * <p>Description: 社区</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: 腾图科技</p>
 * @author bluewind
 * @version 1.0
 */

import java.sql.*;
import javax.crypto.*;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import java.io.*;
import java.net.*;
import cn.js.fan.web.Global;
import org.apache.log4j.Logger;

public class Privilege {
    String connname = Global.getDefaultDB();

    Logger logger = Logger.getLogger(this.getClass().getName());

    public Privilege() {
    }

    public boolean canManage(HttpServletRequest request, String[] ids) {
        com.redmoon.forum.Privilege pvg = new com.redmoon.forum.Privilege();
        String username = pvg.getUser(request);
        //先验证是否为会员
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
        String sql = "select receiver from message where id in " + str;
        Conn conn = new Conn(connname);
        try {
            ResultSet rs = conn.executeQuery(sql);
            if (rs != null) {
                while (rs.next()) {
                    receiver = rs.getString(1);
                    if (!receiver.equals(username)) {
                        isvalid = false;
                        break;
                    }
                    isvalid = true;
                }
            }
        } catch (SQLException e) {
            logger.error("canManage:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        return isvalid;
    }

}
