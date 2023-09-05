package com.cloudweb.oa.module.desktop;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.IDesktopCard;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.ThreadContext;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

public class DesktopCardModule extends AbstractDesktopCard {

    public DesktopCardModule(DesktopCard desktopCard) {
        this.desktopCard = desktopCard;
    }

    @Override
    public int getEndVal(HttpServletRequest request) {
        if (!StrUtil.isEmpty(desktopCard.getEndValfunc())) {
            return getEndValByFunc(desktopCard);
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(desktopCard.getModuleCode());
        if (msd == null) {
            LogUtil.getLog(getClass()).warn("桌面模块 " + desktopCard.getModuleCode() + " 不存在");
            return 0;
        }
        FormDb fd = new FormDb();
        fd = fd.getFormDb(msd.getString("form_code"));
        try {
            request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
            String sql = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, "", "", "")[0];
            sql = SQLFilter.getCountSql(sql);
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(SQLFilter.getCountSql(sql));
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                return rr.getInt(1);
            }
        } catch (ErrMsgException | SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return 0;
    }

    @Override
    public String getUrl() {
        return "/smartModulePage";
    }

    @Override
    public JSONObject getQuery() {
        JSONObject query = new JSONObject();
        query.put("moduleCode", desktopCard.getModuleCode());
        return query;
    }
}
