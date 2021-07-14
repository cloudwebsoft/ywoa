package com.cloudweb.oa.controller;

import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.alibaba.fastjson.JSONArray;
import com.cloudweb.oa.api.ISQLCtl;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.*;
import com.redmoon.oa.notice.NoticeAttachmentDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.ModuleSetupDb;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Iterator;

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
        } catch (JSONException e) {
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
            if (rowOrder == null) {
                uniqueIndexes = new String[0];
            } else {
                uniqueIndexes = rowOrder.split(",");
            }
            String formCode = ParamUtil.get(request, "code");
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);
            boolean re = false;
            for (int i = 0; i < uniqueIndexes.length; i++) {
                String name = ParamUtil.get(request, "tblFields_name_" + uniqueIndexes[i]);
                String title = ParamUtil.get(request, "tblFields_title_" + uniqueIndexes[i]);
                String canNull = ParamUtil.get(request, "tblFields_canNull_" + uniqueIndexes[i]);
                String canQuery = ParamUtil.get(request, "tblFields_canQuery_" + uniqueIndexes[i]);
                String canList = ParamUtil.get(request, "tblFields_canList_" + uniqueIndexes[i]);
                String orders = String.valueOf(uniqueIndexes.length - 1 - i); // ParamUtil.get(request, "tblFields_orders_" + uniqueIndexes[i]);
                String width = ParamUtil.get(request, "tblFields_width_" + uniqueIndexes[i]);
                String isMobileDisplay = ParamUtil.get(request, "tblFields_isMobileDisplay_" + uniqueIndexes[i]);
                String isHide = ParamUtil.get(request, "tblFields_isHide_" + uniqueIndexes[i]);
                String moreThan = ParamUtil.get(request, "tblFields_moreThan_" + uniqueIndexes[i]);
                String morethanMode = ParamUtil.get(request, "tblFields_morethanMode_" + uniqueIndexes[i]);
                int isUnique = ParamUtil.getInt(request, "tblFields_isUnique_" + uniqueIndexes[i]);
                int isHelper = ParamUtil.getInt(request, "tblFields_isHelper_" + uniqueIndexes[i], 0);
                re = fd.updateFieldProps(title, canNull, canQuery, canList, orders, width, isMobileDisplay, isHide, moreThan, morethanMode, isUnique, isHelper, formCode, name);
            }
            if (re) {
                FormCache fc = new FormCache(fd);
                fd.getPrimaryKey().setValue(formCode);
                fc.refreshSave(fd.getPrimaryKey());
                json.put("ret", 1);
                json.put("msg", "操作成功");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException | ErrMsgException e) {
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 为计算控件取得嵌套表单域中数值型或SQL宏控件型的字段
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getNestFieldsForCal", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String getNestFieldsForCal(String macroType, String defaultValue, String description) {
        JSONObject json = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            MacroCtlMgr mm = new MacroCtlMgr();
            MacroCtlUnit mu = mm.getMacroCtlUnit(macroType);
            if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
                String nestFormCode = defaultValue;
                try {
                    if (mu.getNestType() == MacroCtlUnit.NEST_DETAIL_LIST) {
                        defaultValue = StrUtil.decodeJSON(description);
                    } else {
                        String desc = defaultValue;
                        if ("".equals(desc)) {
                            desc = description;
                        }
                        defaultValue = StrUtil.decodeJSON(desc);
                    }
                    // 20131123 fgf 添加
                    JSONObject jsonObj = new JSONObject(defaultValue);
                    nestFormCode = jsonObj.getString("destForm");
                } catch (JSONException e) {
                    e.printStackTrace();
                    // LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123.");
                }

                // 20200628 改为支持副模块
                FormDb nestfd = new FormDb();
                nestfd = nestfd.getFormDb(nestFormCode);
                if (!nestfd.isLoaded()) {
                    ModuleSetupDb msd = new ModuleSetupDb();
                    msd = msd.getModuleSetupDb(nestFormCode);
                    nestFormCode = msd.getString("form_code");
                    nestfd = nestfd.getFormDb(nestFormCode);
                }

                Iterator ir2 = nestfd.getFields().iterator();
                while (ir2.hasNext()) {
                    FormField ff2 = (FormField) ir2.next();
                    int fieldType = ff2.getFieldType();
                    boolean isFound = false;
                    if (fieldType == FormField.FIELD_TYPE_INT || fieldType == FormField.FIELD_TYPE_LONG || fieldType == FormField.FIELD_TYPE_FLOAT || fieldType == FormField.FIELD_TYPE_DOUBLE || fieldType == FormField.FIELD_TYPE_PRICE) {
                        isFound = true;
                    }
                    if (FormField.TYPE_MACRO.equals(ff2.getType())) {
                        mu = mm.getMacroCtlUnit(ff2.getMacroType());
                        if (mu.getIFormMacroCtl() instanceof ISQLCtl || mu.getIFormMacroCtl() instanceof FormulaCtl) {
                            isFound = true;
                        }
                    }

                    if (isFound) {
                        JSONObject obj = new JSONObject();
                        obj.put("name", ff2.getName());
                        obj.put("title", ff2.getTitle());
                        obj.put("type", ff2.getType());
                        obj.put("fieldType", ff2.getFieldType());
                        obj.put("macroType", ff2.getMacroType());
                        obj.put("formCode", nestFormCode);
                        obj.put("formName", nestfd.getName());
                        arr.add(obj);
                    }
                }
                json.put("ret", "1");
                json.put("jsonAry", arr);
            }
            else {
                json.put("ret", "0");
                json.put("msg", "非嵌套表宏控件");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/getFieldType", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String getFieldType(String formCode, String fieldName) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        if (!fd.isLoaded()) {
            json.put("ret", 0);
            json.put("msg", "表单：" + formCode + "不存在");
            return json.toString();
        }

        FormField ff = fd.getFormField(fieldName);
        if (ff == null) {
            json.put("ret", 0);
            json.put("msg", "表单：" + formCode + " 中的字段：" + fieldName + " 不存在");
            return json.toString();
        }

        int fieldType;
        if (ff.getType().equals(FormField.TYPE_MACRO)) {
            MacroCtlMgr mm = new MacroCtlMgr();
            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
            fieldType = mu.getIFormMacroCtl().getFieldType(ff);
        }
        else {
            fieldType = ff.getFieldType();
        }

        json.put("ret", 1);
        json.put("fieldType", fieldType);
        return json.toString();
    }

    @RequestMapping("/exportForm")
    public void exportForm(HttpServletResponse response, String formCode) throws IOException, ErrMsgException, JSONException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "read")) {
            throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
        }


        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        if (!fd.isLoaded()) {
            throw new ErrMsgException("表单不存在！");
        }

        String encoding = System.getProperty("file.encoding");
        String content = "<meta http-equiv='Content-Type' content='text/html; charset="+encoding+"'>";
        content = content + fd.getContent();

        response.setContentType(MIMEMap.get("html"));
        response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(fd.getName()) + ".html");

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new ByteArrayInputStream(content.getBytes()));
            bos = new BufferedOutputStream(response.getOutputStream());

            byte[] buff = new byte[2048];
            int bytesRead;

            while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff,0,bytesRead);
            }
        } catch(final IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }
}
