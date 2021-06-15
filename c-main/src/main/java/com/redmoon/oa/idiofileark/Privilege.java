package com.redmoon.oa.idiofileark;

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
import org.apache.log4j.Logger;
import com.redmoon.oa.person.*;

public class Privilege {
    String connname = Global.getDefaultDB();

    Logger logger = Logger.getLogger(this.getClass().getName());

    public Privilege() {
    }

    /**
     * 修改文件所属目录的根目录是与用户名相等来判断是否有权限删除
     * @param request HttpServletRequest
     * @param ids String[]
     * @return boolean
     */
    public boolean canManage(HttpServletRequest request, String[] ids) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String username = pvg.getUser(request);
        // UserMgr um = new UserMgr();
        // UserDb user = um.getUserDb(username);
        Leaf lf = new Leaf();

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
        String dirCode = null;
        String sql = "select dir_code from oa_idiofileark where id in " + str;
        Conn conn = new Conn(connname);
        try {
            ResultSet rs = conn.executeQuery(sql);
            if (rs != null) {
                while (rs.next()) {
                    dirCode = rs.getString(1);
                    lf = lf.getLeaf(dirCode);
                    // 如果目錄不存在
                    if (lf==null) {
                        isvalid = true;                    	
                    	continue;
                    }
                    if (!lf.getRootCode().equals(username)) {
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
