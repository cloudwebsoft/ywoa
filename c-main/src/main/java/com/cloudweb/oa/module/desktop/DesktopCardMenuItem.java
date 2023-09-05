package com.cloudweb.oa.module.desktop;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.IDesktopCard;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.ui.menu.Leaf;
import com.redmoon.oa.visual.ModuleSetupDb;
import com.redmoon.oa.visual.ModuleUtil;
import com.redmoon.oa.visual.SQLBuilder;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

public class DesktopCardMenuItem extends AbstractDesktopCard {

    public DesktopCardMenuItem(DesktopCard desktopCard) {
        this.desktopCard = desktopCard;
    }

    @Override
    public int getEndVal(HttpServletRequest request) {
        if (!StrUtil.isEmpty(desktopCard.getEndValfunc())) {
            return getEndValByFunc(desktopCard);
        }

        Leaf lf = new Leaf();
        lf = lf.getLeaf(desktopCard.getMenuItem());
        if (lf.getType() == Leaf.TYPE_MODULE) {
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(lf.getFormCode());
            FormDb fd = new FormDb();
            fd = fd.getFormDb(msd.getFormCode());
            String sql = "", sql2 = "";
            try {
                request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
                // 注意须带有search，否则当存在union时结果中的order by 部分可能不正确：
                // 如过滤条件：select distinct tmp.id from (select t1.id from ft_xmxxgl_qx t1, ft_zs_imp_tjk t100 where t1.id=t100.xm_id union select t1.id from ft_xmxxgl_qx t1 where mygx<>'项目终止' and is_pass_qysh='是' and tzze_rmb>=1 and zsqysj>='2021-01-01') as tmp
                // 生成后的sql: select distinct tmp.id from (select t1.id from ft_xmxxgl_qx t1, ft_zs_imp_tjk t100 where t1.id=t100.xm_id union select t1.id from ft_xmxxgl_qx t1 where mygx<>'项目终止' and is_pass_qysh='是' and tzze_rmb>=1 and zsqysj>='2021-01-01') as tmp order by t1.id desc
                // order by t1.id desc部分会报错
                sql = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, "search", "id", "desc")[0];
                sql2 = SQLFilter.getCountSql(sql);
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql2);
                if (ri.hasNext()) {
                    ResultRecord rr = ri.next();
                    return rr.getInt(1);
                }
            } catch (ErrMsgException | SQLException e) {
                LogUtil.getLog(getClass()).error("sql: " + sql);
                LogUtil.getLog(getClass()).error("sql2: " + sql2);
                LogUtil.getLog(getClass()).error(e);
            }
        }

        return 0;
    }

    @Override
    public String getUrl() {
        String menuItem = desktopCard.getMenuItem();
        Leaf lf = new Leaf();
        lf = lf.getLeaf(menuItem);
        return lf.getLink(SpringUtil.getRequest());
    }
}
