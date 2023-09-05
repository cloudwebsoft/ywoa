package com.cloudweb.oa.visual;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.DeptUser;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.FormDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

@Slf4j
@Component
public class PersonBasicService {

    @Autowired
    IDeptUserService deptUserService;

    /**
     * 根据用户名取得ID
     * @param userName
     * @return
     */
    public long getIdByUserName(String userName) {
        String sql = "select id from ft_personbasic where user_name=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[]{userName});
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                return rr.getLong(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    public boolean changeDeptOfUser(String userName, String deptCode) {
        boolean re = true;
        Config cfg = Config.getInstance();
        boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");
        if (isArchiveUserSynAccount) {
            String sql = "update ft_personbasic set dept=" + StrUtil.sqlstr(deptCode) + " where user_name=" + StrUtil.sqlstr(userName);
            JdbcTemplate jt = new JdbcTemplate();
            try {
                re = jt.executeUpdate(sql) == 1;
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        return re;
    }

    public int updateUserUnitInDept(String unitCode, String deptCode) {
        String sql = "update ft_personbasic set unit_code="
                + StrUtil.sqlstr(unitCode)
                + " where user_name in (select user_name from dept_user d where d.dept_code="
                + StrUtil.sqlstr(deptCode)
                + ")";

        try {
            JdbcTemplate jt = new JdbcTemplate();
            return jt.executeUpdate(sql);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return 0;
    }

    public void updateInfo(String userName, JSONObject json) {
        // 人员基本信息表存在,则同步至人员基本信息表
        FormDb fd = new FormDb();
        fd = fd.getFormDb("personbasic");
        if (fd != null && fd.isLoaded()) {
            FormDAO fdao = new FormDAO(fd);
            Iterator<FormDAO> it = null;
            try {
                it = fdao.list("personbasic", "select id from ft_personbasic where user_name=" + StrUtil.sqlstr(userName)).iterator();
                if (it.hasNext()) {
                    fdao = it.next();
                    Iterator ir = json.keySet().iterator();
                    while (ir.hasNext()) {
                        String keyName = (String) ir.next();
                        fdao.setFieldValue(keyName, json.getString(keyName));
                    }
                    fdao.save();
                }
                else {
                    DebugUtil.e(getClass(), "updateInfo", "在人事信息中未找到：" + userName);
                }
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
    }

    /**
     * 更新人员信息表
     *
     * @param user
     * @param newDeptCode 仅用户被更换部门时才传递此参数，其它场景下均为null
     * @param operator
     * @param opType      操作类型 0 新增 1 更新 -1 删除
     */
    public void updatePersonbasic(User user, String newDeptCode, String operator, int opType) {
        // 判断配置中是否设置了同步帐户
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");

        if (isArchiveUserSynAccount) {
            // 如果未指定deptCode，则取用户原来的部门编码
            if (newDeptCode == null) {
                DeptUser deptUser = deptUserService.getPrimary(user.getName());
                if (deptUser != null) {
                    newDeptCode = deptUser.getDeptCode();
                }
            }
            // 人员基本信息表存在,则同步至人员基本信息表
            FormDb fd = new FormDb();
            fd = fd.getFormDb("personbasic");
            if (fd != null && fd.isLoaded()) {
                try {
                    FormDAO fdao = new FormDAO(fd);
                    Iterator<FormDAO> it = fdao.list("personbasic", "select id from ft_personbasic where user_name=" + StrUtil.sqlstr(user.getName())).iterator();
                    if (it.hasNext()) {
                        // 如果是删除操作，则删除对应的personbasic记录
                        if (opType == -1) {
                            fdao = it.next();
                            fdao.del();
                        }

                        // 20201016 改为不作更新处理，因为当从personbasic表单中创建时，调用了UserDb.create(...)，create中传参只有几个，而create方法最后又会调用此方法，
                        // 又回写至人事信息中（导致循环写入了），会致有时丢失（有时不丢）部门、出生日期、入职日期，数据出现混乱

                        // 20200325 改为如果是创建操作，而personbasic表单中已存在记录，则说明创建操作来自于智能模块，此处不再处理，否则会导致上述的循环写入
                        // 所以此处只响应更新操作，而在personbaisc表单修改事件中的写法，并不会引起循环写入
                        if (opType == 1) {
                            fdao = it.next();
                            fdao.setFieldValue("realname", user.getRealName());
                            fdao.setFieldValue("mobile", user.getMobile());
                            fdao.setFieldValue("dept", newDeptCode);
                            fdao.setUnitCode(user.getUnitCode());

                            fdao.setCreator(operator);
                            fdao.setFieldValue("user_name", user.getLoginName());
                            String birthday = DateUtil.format(user.getBirthday(), "yyyy-MM-dd");
                            fdao.setFieldValue("csrq", birthday);
                            if (birthday != null && !"".equals(birthday)) {
                                fdao.setFieldValue("age", String.valueOf(DateUtil.getYear(new java.util.Date()) - DateUtil.getYear(user.getBirthday())));
                            }
                            fdao.setFieldValue("sex", !user.getGender() ? "男" : "女");
                            fdao.setFieldValue("idcard", user.getIDCard());
                            fdao.setFieldValue("address", user.getAddress());
                            // 置在职、离职
                            fdao.setFieldValue("zzqk", user.getIsValid() == 1 ? "1" : "0");
                            fdao.setFieldValue("entry_date", DateUtil.format(user.getEntryDate(), "yyyy-MM-dd"));
                            fdao.setFieldValue("person_no", user.getPersonNo());
                            fdao.save();
                        }
                    } else {
                        // 如果不是删除操作，则添加记录
                        if (opType != -1) {
                            fdao.setFieldValue("user_name", user.getLoginName());
                            fdao.setFieldValue("realname", user.getRealName());
                            String birthday = DateUtil.format(user.getBirthday(), "yyyy-MM-dd");
                            fdao.setFieldValue("csrq", birthday);
                            if (!"".equals(birthday)) {
                                fdao.setFieldValue("age", String.valueOf(DateUtil.getYear(new java.util.Date()) - DateUtil.getYear(user.getBirthday())));
                            }
                            fdao.setFieldValue("sex", !user.getGender() ? "男" : "女");
                            fdao.setFieldValue("idcard", user.getIDCard());
                            fdao.setFieldValue("mobile", user.getMobile());
                            fdao.setFieldValue("address", user.getAddress());
                            fdao.setFieldValue("dept", newDeptCode);
                            fdao.setFieldValue("zzqk", "1"); // 在职
                            fdao.setFieldValue("entry_date", DateUtil.format(user.getEntryDate(), "yyyy-MM-dd"));
                            fdao.setFieldValue("person_no", user.getPersonNo());
                            fdao.setUnitCode(user.getUnitCode());
                            fdao.create();
                        }
                    }
                } catch (Exception e) {
                    log.error("updatePersonbasic:" + e.getMessage());
                }
            }
        }
    }
}
