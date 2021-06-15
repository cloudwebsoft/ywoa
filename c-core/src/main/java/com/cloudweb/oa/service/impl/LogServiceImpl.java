package com.cloudweb.oa.service.impl;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.entity.Log;
import com.cloudweb.oa.mapper.LogMapper;
import com.cloudweb.oa.service.ILogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-02-15
 */
@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements ILogService {
    @Autowired
    LogMapper logMapper;

    @Autowired
    I18nUtil i18nUtil;

    @Override
    public List<Log> list(String userName, String op, String logType, String userAction, int device, Date beginDate, Date endDate, String deptCode) {
        String sql;
        sql = "select l.* from log l where 1=1";

        if ("search".equals(op)) {
            if (!"".equals(deptCode) && !"".equals(userName)) {
                sql = "select l.* from log l, dept_user du, users u where l.USER_NAME=du.user_name and l.user_name=u.name";
            }
            else if (!"".equals(deptCode)) {
                sql = "select l.* from log l, dept_user du where l.USER_NAME=du.user_name";
            }
            else if (!"".equals(userName)) {
                sql = "select l.* from log l, users u where l.USER_NAME=u.name";
            }

            String cond = "";
            if (!"".equals(userName)) {
                cond += " and u.realname like " + StrUtil.sqlstr("%" + userName + "%");
            }
            if (beginDate!=null) {
                cond += " and log_date>="
                        + SQLFilter.getDateStr(DateUtil.format(beginDate, "yyyy-MM-dd"), "yyyy-MM-dd");
            }
            if (endDate!=null) {
                cond += " and log_date<="
                        + SQLFilter.getDateStr(DateUtil.format(endDate, "yyyy-MM-dd"), "yyyy-MM-dd");
            }
            if (!"".equals(userAction)) {
                cond += " and l.action like " + StrUtil.sqlstr("%" + userAction + "%");
            }

            if (!"".equals(logType)) {
                cond += " and log_type=" + StrUtil.sqlstr(logType);
            }
            if (!"".equals(deptCode)) {
                cond += " and du.dept_code=" + StrUtil.sqlstr(deptCode);
            }
            if (device!=-1) {
                cond += " and device=" + device;
            }

            if (!"".equals(cond)) {
                sql += " " + cond;
            }
        }

        sql += " order by log_date desc";

        return logMapper.listBySql(sql);
    }

    @Override
    public String getTypeDesc(int type) {
        HttpServletRequest request = SpringUtil.getRequest();
        String desc = "";
        switch (type) {
            case ConstUtil.LOG_TYPE_LOGIN:
                desc = i18nUtil.get("LOG_TYPE_LOGIN");
                break;
            case ConstUtil.LOG_TYPE_LOGOUT:
                desc = i18nUtil.get("LOG_TYPE_LOGOUT");
                break;
            case ConstUtil.LOG_TYPE_ACTION:
                desc = i18nUtil.get("LOG_TYPE_ACTION");
                break;
            case ConstUtil.LOG_TYPE_PRIVILEGE:
                desc = i18nUtil.get("LOG_TYPE_PRIVILEGE");
                break;
            case ConstUtil.LOG_TYPE_WARN:
                desc = i18nUtil.get("LOG_TYPE_WARN");
                break;
            case ConstUtil.LOG_TYPE_ERROR:
                desc = i18nUtil.get("LOG_TYPE_ERROR");
                break;
            case ConstUtil.LOG_TYPE_HACK:
                desc = i18nUtil.get("LOG_TYPE_HACK");
                break;
            default:;
        }
        return desc;
    }
}
