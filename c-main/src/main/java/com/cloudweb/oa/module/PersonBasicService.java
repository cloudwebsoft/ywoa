package com.cloudweb.oa.module;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.entity.DeptUser;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.Config;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Iterator;

@Slf4j
@Component
public class PersonBasicService {

    @Autowired
    IDeptUserService deptUserService;

    public boolean changeDeptOfUser(String userName, String deptCode) {
        boolean re = true;
        Config cfg = Config.getInstance();
        boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");
        if (isArchiveUserSynAccount) {
            String sql = "update form_table_personbasic set dept=" + StrUtil.sqlstr(deptCode) + " where user_name=" + StrUtil.sqlstr(userName);
            JdbcTemplate jt = new JdbcTemplate();
            try {
                re = jt.executeUpdate(sql) == 1;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return re;
    }

    public int updateUserUnitInDept(String unitCode, String deptCode) {
        String sql = "update form_table_personbasic set unit_code="
                + StrUtil.sqlstr(unitCode)
                + " where user_name in (select user_name from dept_user d where d.dept_code="
                + StrUtil.sqlstr(deptCode)
                + ")";

        try {
            JdbcTemplate jt = new JdbcTemplate();
            return jt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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
            FormDb fd = new FormDb("personbasic");
            if (fd != null && fd.isLoaded()) {
                try {
                    FormDAO fdao = new FormDAO(fd);
                    Iterator it = fdao.list("personbasic", "select id from form_table_personbasic where user_name=" + StrUtil.sqlstr(user.getName())).iterator();
                    if (it.hasNext()) {
                        // 20201016 不作处理，因为当从人事信息中创建时，调用了UserDb.create(...)，create中传参只有几个，如果此处再保存，会致有时丢失（有时不丢）部门、出生日期、入职日期，数据出现混乱
                        /*fdao = (FormDAO) it.next();
                        fdao.setCreator(operator);
                        fdao.setUnitCode(user.getUnitCode());
                        fdao.setFieldValue("user_name", user.getName());
                        fdao.setFieldValue("realname", user.getRealName());
                        String birthday = DateUtil.format(user.getBirthday(), "yyyy-MM-dd");
                        fdao.setFieldValue("csrq", birthday);
                        if (birthday != null && !birthday.equals("")) {
                            fdao.setFieldValue("age", String.valueOf(DateUtil.getYear(new java.util.Date()) - DateUtil.getYear(user.getBirthday())));
                        }
                        fdao.setFieldValue("sex", !user.getGender() ? "男" : "女");
                        fdao.setFieldValue("idcard", user.getIDCard());
                        fdao.setFieldValue("mobile", user.getMobile());
                        fdao.setFieldValue("address", user.getAddress());
                        fdao.setFieldValue("dept", newDeptCode);
                        // 置在职、离职
                        fdao.setFieldValue("zzqk", user.getIsValid() == 1 ? "1" : "0");
                        fdao.setFieldValue("entry_date", DateUtil.format(user.getEntryDate(), "yyyy-MM-dd"));
                        fdao.setFieldValue("person_no", user.getPersonNo());
                        fdao.setUnitCode(user.getUnitCode());
                        fdao.save();*/
                    } else {
                        fdao.setFieldValue("user_name", user.getName());
                        fdao.setFieldValue("realname", user.getRealName());
                        String birthday = DateUtil.format(user.getBirthday(), "yyyy-MM-dd");
                        fdao.setFieldValue("csrq", birthday);
                        if (birthday != null && !birthday.equals("")) {
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
                } catch (Exception e) {
                    log.error("updatePersonbasic:" + e.getMessage());
                }
            }
        }
    }
}
