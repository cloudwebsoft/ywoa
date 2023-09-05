package com.cloudweb.oa.module.desktop;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.IDesktopCard;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.flow.WorkflowDb;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

public class DesktopCardFlowDoing extends AbstractDesktopCard {

    public DesktopCardFlowDoing(DesktopCard desktopCard) {
        this.desktopCard = desktopCard;
    }

    @Override
    public int getEndVal(HttpServletRequest request) {
        if (!StrUtil.isEmpty(desktopCard.getEndValfunc())) {
            return getEndValByFunc(desktopCard);
        }

        WorkflowDb wf = new WorkflowDb();
        String sql = wf.getSqlDoing(request);
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(SQLFilter.getCountSql(sql));
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                return rr.getInt(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }
}
