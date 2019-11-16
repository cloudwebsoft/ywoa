package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import com.redmoon.oa.flow.FormCache;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormMgr;
import com.redmoon.oa.flow.FormParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/form")
public class FormController {
    @Autowired
    private HttpServletRequest request;

    @ResponseBody
    @RequestMapping(value = "/parseForm", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String parseForm() {
        String content = ParamUtil.get(request, "content");
        JSONObject json = new JSONObject();
        try {
            json.put("ret", 0);
            json.put("msg", "操作失败");
            try {
                return FormParser.doParse(content);
            } catch (ResKeyException e) {
                json.put("ret", 0);
                json.put("msg", e.getMessage(request));
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/updateFormField", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String updateFormField() {
        JSONObject json = new JSONObject();
        try {
            String rowOrder = ParamUtil.get(request, "tblFields_rowOrder");
            String[] uniqueIndexes = null;
            if (rowOrder==null) {
                uniqueIndexes = new String[0];
            }
            else {
                uniqueIndexes = rowOrder.split(",");
            }
            String formCode = ParamUtil.get(request, "code");
            FormDb fd = new FormDb();
            boolean re = false;
            for (int i = 0; i < uniqueIndexes.length; i++) {
                String name = ParamUtil.get(request, "tblFields_name_" + uniqueIndexes[i]);
                String title = ParamUtil.get(request, "tblFields_title_" + uniqueIndexes[i]);
                String canNull = ParamUtil.get(request, "tblFields_canNull_" + uniqueIndexes[i]);
                String canQuery = ParamUtil.get(request, "tblFields_canQuery_" + uniqueIndexes[i]);
                String canList = ParamUtil.get(request, "tblFields_canList_" + uniqueIndexes[i]);
                String orders = String.valueOf(uniqueIndexes.length -1 - i); // ParamUtil.get(request, "tblFields_orders_" + uniqueIndexes[i]);
                String width = ParamUtil.get(request, "tblFields_width_" + uniqueIndexes[i]);
                String isMobileDisplay = ParamUtil.get(request, "tblFields_isMobileDisplay_" + uniqueIndexes[i]);
                String isHide = ParamUtil.get(request, "tblFields_isHide_" + uniqueIndexes[i]);
                String moreThan = ParamUtil.get(request, "tblFields_moreThan_" + uniqueIndexes[i]);
                String morethanMode = ParamUtil.get(request, "tblFields_morethanMode_" + uniqueIndexes[i]);
                int isUnique = ParamUtil.getInt(request, "tblFields_isUnique_" + uniqueIndexes[i]);

                re = fd.updateFieldProps(title, canNull, canQuery, canList, orders, width, isMobileDisplay, isHide, moreThan, morethanMode, isUnique, formCode, name);
            }
            if (re) {
                FormCache fc = new FormCache(fd);
                fd.getPrimaryKey().setValue(formCode);
                fc.refreshSave(fd.getPrimaryKey());
                json.put("ret", 1);
                json.put("msg", "操作成功");
            }
            else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 删除
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/del", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String del(String codes) {
        JSONObject json = new JSONObject();
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        String priv = "admin";
        if (!privilege.isUserPrivValid(request, priv)) {
            try {
                json.put("res", "0");
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            try {
                FormMgr fm = new FormMgr();
                String[] ary = StrUtil.split(codes, ",");
                for (String code : ary) {
                    fm.del(code);
                }
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } catch (ErrMsgException e) {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
