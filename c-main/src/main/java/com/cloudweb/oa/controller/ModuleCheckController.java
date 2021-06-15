package com.cloudweb.oa.controller;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.IDCardUtil;
import com.redmoon.oa.flow.FormDAOMgr;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.sys.DebugUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

@Controller
@RequestMapping("/module_check")
public class ModuleCheckController {
    @Autowired
    private HttpServletRequest request;

    /**
     * 唯一性检查
     * @param id
     * @param formCode
     * @param fieldName change事件中调用checkFieldIsUnique，值产生变动的字段名
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/checkFieldIsUnique", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String checkFieldIsUnique(@RequestParam(defaultValue="-1") long id, String formCode, String fieldName) {
        JSONObject json = new JSONObject();
        String sql = "select id from form_table_%s where %s";
        StringBuffer sb = new StringBuffer();
        StringBuffer sbFields = new StringBuffer();
        int c = 0;
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        Vector<FormField> fields = fd.getFields();
        for (FormField ff : fields) {
            if (ff.isUnique()) {
                StrUtil.concat(sb, " and ", ff.getName() + "=?");
                StrUtil.concat(sbFields, "+", ff.getTitle());
                c++;
            }
        }
        sql = String.format(sql, formCode, sb.toString());
        if (c>0) {
            String[] ary = new String[c];
            int i = 0;
            for (FormField ff : fields) {
                if (ff.isUnique()) {
                    ary[i] = ParamUtil.get(request, ff.getName());
                    i++;
                }
            }

            int ret = 1;
            JdbcTemplate jt = new JdbcTemplate();
            try {
                ResultIterator ri = jt.executeQuery(sql, ary);
                if (id==-1) {
                    if (ri.size() > 0) {
                        // 当智能模块创建记录时，如果库中记录数大于0，则存在重复记录
                        ret = 0;
                        json.put("msg", StrUtil.format(SkinUtil.LoadString(request, "res.module.flow", "err_is_not_unique"), new Object[]{sbFields.toString()}));
                    }
                }
                else {
                    if (ri.size() > 1) {
                        ret = 0;
                        // 存在重复记录
                        json.put("msg", StrUtil.format(SkinUtil.LoadString(request, "res.module.flow", "err_is_not_unique"), new Object[]{sbFields.toString()}));
                    }
                    else if (ri.size()==1) {
                        if (ri.hasNext()) {
                            ResultRecord rr = (ResultRecord)ri.next();
                            if (rr.getLong(1) != id) {
                                // 如果不是记录本身，则存在重复记录
                                ret = 0;
                                json.put("msg", StrUtil.format(SkinUtil.LoadString(request, "res.module.flow", "err_is_not_unique"), new Object[]{sbFields.toString()}));
                            }
                        }
                    }
                }
                json.put("ret", ret);
            } catch (SQLException e) {
                DebugUtil.log(FormDAOMgr.class, "checkFieldIsUnique", sql);
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                json.put("ret", 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 嵌套表中字段唯一性检查
     * @param id
     * @param formCode
     * @param fieldName change事件中调用checkFieldIsUnique，值产生变动的字段名
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/checkFieldIsUniqueNest", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String checkFieldIsUniqueNest(@RequestParam(defaultValue="-1") long id, String formCode, String fieldName, String cwsId, String parentFormCode) {
        JSONObject json = new JSONObject();
        String sql = "select id from form_table_%s where %s and cws_id=? and cws_parent_form=?";
        StringBuffer sb = new StringBuffer();
        StringBuffer sbFields = new StringBuffer();
        int c = 0;
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        Vector<FormField> fields = fd.getFields();
        for (FormField ff : fields) {
            if (ff.isUniqueNest()) {
                StrUtil.concat(sb, " and ", ff.getName() + "=?");
                StrUtil.concat(sbFields, "+", ff.getTitle());
                c++;
            }
        }
        sql = String.format(sql, formCode, sb.toString());
        if (c>0) {
            String[] ary = new String[c + 2];
            int i = 0;
            for (FormField ff : fields) {
                if (ff.isUniqueNest()) {
                    ary[i] = ParamUtil.get(request, ff.getName());
                    i++;
                }
            }
            ary[i] = cwsId;
            ary[i+1] = parentFormCode;

            int ret = 1;
            JdbcTemplate jt = new JdbcTemplate();
            try {
                ResultIterator ri = jt.executeQuery(sql, ary);
                if (id==-1) {
                    if (ri.size() > 0) {
                        // 当智能模块创建记录时，如果库中记录数大于0，则存在重复记录
                        ret = 0;
                        json.put("msg", StrUtil.format(SkinUtil.LoadString(request, "res.module.flow", "err_is_not_unique"), new Object[]{sbFields.toString()}));
                    }
                }
                else {
                    if (ri.size() > 1) {
                        ret = 0;
                        // 存在重复记录
                        json.put("msg", StrUtil.format(SkinUtil.LoadString(request, "res.module.flow", "err_is_not_unique"), new Object[]{sbFields.toString()}));
                    }
                    else if (ri.size()==1) {
                        if (ri.hasNext()) {
                            ResultRecord rr = (ResultRecord)ri.next();
                            if (rr.getLong(1) != id) {
                                // 如果不是记录本身，则存在重复记录
                                ret = 0;
                                json.put("msg", StrUtil.format(SkinUtil.LoadString(request, "res.module.flow", "err_is_not_unique"), new Object[]{sbFields.toString()}));
                            }
                        }
                    }
                }
                json.put("ret", ret);
            } catch (SQLException e) {
                DebugUtil.log(getClass(), "checkFieldIsUnique", sql);
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                json.put("ret", 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/checkEmail", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String checkEmail(String formCode, String fieldName, String val) {
        JSONObject json = new JSONObject();
        try {
            if (!StrUtil.IsValidEmail(val)) {
                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCode);
                String title = fd.getFieldTitle(fieldName);
                json.put("ret", 0);
                json.put("msg", title + " 格式非法");
            } else {
                json.put("ret", 1);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/checkMobile", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String checkMobile(String formCode, String fieldName, String val) {
        JSONObject json = new JSONObject();
        try {
            if (!com.redmoon.oa.sms.Config.isValidMobile(val)) {
                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCode);
                String title = fd.getFieldTitle(fieldName);
                json.put("ret", 0);
                json.put("msg", title + " 格式非法");
            } else {
                json.put("ret", 1);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/checkIDCard", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String checkIDCard(String val) {
        JSONObject json = new JSONObject();
        try {
            IDCardUtil icUtil = new IDCardUtil();
            String str = icUtil.validate(val);
            if (!"1".equals(str)) {
                json.put("ret", 0);
                json.put("msg", str);
            } else {
                json.put("ret", 1);
                json.put("birthday", DateUtil.format(icUtil.getBirthday(), "yyyy-MM-dd"));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
