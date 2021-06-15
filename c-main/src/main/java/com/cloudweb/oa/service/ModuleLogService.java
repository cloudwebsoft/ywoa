package com.cloudweb.oa.service;

import cn.js.fan.util.DateUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.FormDAOLog;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Date;

@Component
public class ModuleLogService {

    public boolean logRead(String formCode, String moduleCode, long id, String userName, String unitCode) {
        FormDb fdModule = new FormDb(formCode);
        FormDb fd = new FormDb("module_log_read");
        FormDAO fdao = new FormDAO(fd);
        fdao.setFieldValue("read_type", String.valueOf(FormDAOLog.READ_TYPE_MODULE));
        fdao.setFieldValue("log_date", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        fdao.setFieldValue("module_code", moduleCode);
        fdao.setFieldValue("form_code", formCode);
        fdao.setFieldValue("module_id", String.valueOf(id));
        fdao.setFieldValue("form_name", fdModule.getName());
        fdao.setFieldValue("user_name", userName);
        fdao.setCreator(userName); // 参数为用户名（创建记录者）
        fdao.setUnitCode(unitCode); // 置单位编码
        fdao.setFlowTypeCode(String.valueOf(System.currentTimeMillis())); // 置冗余字段“流程编码”，可用于取出刚插入的记录，也可以为空
        try {
            return fdao.create();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }
}
