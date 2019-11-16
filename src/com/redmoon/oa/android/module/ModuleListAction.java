package com.redmoon.oa.android.module;

import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;


import cn.js.fan.util.DateUtil;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.util.RequestUtil;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;


import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;

import cn.js.fan.util.StrUtil;


import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.post.PostDb;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.FuncUtil;
import com.redmoon.oa.visual.ModulePrivDb;
import com.redmoon.oa.visual.ModuleRelateDb;
import com.redmoon.oa.visual.ModuleSetupDb;
import com.redmoon.oa.visual.ModuleUtil;
import com.redmoon.oa.visual.SQLBuilder;

public class ModuleListAction extends BaseAction {
    private String moduleCode = "";
    private String skey = "";
    private int pageSize = 15;
    private int pageNum = 1;

    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    private String op = "";

    private String orderBy = "id";
    private String sort = "desc";

    /**
     * @return the orderBy
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * @param orderBy the orderBy to set
     */
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * @return the sort
     */
    public String getSort() {
        return sort;
    }

    /**
     * @param sort the sort to set
     */
    public void setSort(String sort) {
        this.sort = sort;
    }

    /**
     * @return the moduleCode
     */
    public String getModuleCode() {
        return moduleCode;
    }

    /**
     * @param moduleCode the moduleCode to set
     */
    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    /**
     * @return the op
     */
    public String getOp() {
        return op;
    }

    /**
     * @param op the op to set
     */
    public void setOp(String op) {
        this.op = op;
    }

    public String getSkey() {
        return skey;
    }

    public void setSkey(String skey) {
        this.skey = skey;
    }


    /**
     * @return the pageNum
     */
    public int getPageNum() {
        return pageNum;
    }

    /**
     * @param pageNum the pageNum to set
     */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    public void executeAction() {
        super.executeAction();

        if ("".equals(moduleCode)) {
            return;
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(moduleCode);

        String formCode = msd.getString("form_code");
        try {
            UserMgr um = new UserMgr();
            Privilege privilege = new Privilege();

            boolean re = privilege.Auth(getSkey());
            String userName = privilege.getUserName(skey);

            jReturn.put(RES, RETURNCODE_SUCCESS); //请求成功
            if (re) {
                jResult.put(RETURNCODE, RESULT_TIME_OUT); //登录超时
            } else {
                HttpServletRequest request = ServletActionContext.getRequest();
                privilege.doLogin(request, skey);

                if (formCode == null || formCode.trim().equals("")) {
                    jResult.put(RETURNCODE, RESULT_FORMCODE_ERROR); //表单为空
                } else {
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(formCode);
                    if (!fd.isLoaded()) { //表单不存在
                        jResult.put(RETURNCODE, RESULT_FORMCODE_ERROR);//表单不存在
                        return;
                    } else {
                        MacroCtlUnit mu;
                        MacroCtlMgr mm = new MacroCtlMgr();

                        FormDAO fdao = new FormDAO();
                        if (msd == null) {
                            jResult.put(RETURNCODE, RESULT_MODULE_ERROR);//表单不存在
                            return;
                        }

/*
						int is_workLog = msd.getInt("is_workLog");
						if(is_workLog == 1){
							jResult.put("isWorkLog", true);
						}else{
							jResult.put("isWorkLog", false);
						}*/

                        String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
                        String[] btnNames = StrUtil.split(btnName, ",");
                        String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
                        String[] btnScripts = StrUtil.split(btnScript, "#");

                        // 取得条件
                        JSONArray conditions = new JSONArray();
                        int len = 0;
                        if (btnNames != null) {
                            len = btnNames.length;
                            for (int i = 0; i < len; i++) {
                                if (btnScripts[i].startsWith("{")) {
                                    // System.out.println(getClass() + " " + btnScripts[i]);
                                    JSONObject jsonBtn = new JSONObject(btnScripts[i]);
                                    if (((String) jsonBtn.get("btnType")).equals("queryFields")) {
                                        String condFields = (String) jsonBtn.get("fields");
                                        String[] fieldAry = StrUtil.split(condFields, ",");
                                        for (int j = 0; j < fieldAry.length; j++) {
                                            String fieldName = fieldAry[j];
                                            FormField ff = fd.getFormField(fieldName);
                                            if (ff == null) {
                                                continue;
                                            }
                                            String condType = (String) jsonBtn.get(fieldName);
                                            String queryValue = ParamUtil.get(request, fieldName);

                                            queryValue = new String(queryValue.getBytes("ISO-8859-1"), "UTF-8");

                                            JSONObject jo = new JSONObject();

                                            jo.put("type", ff.getType());
                                            jo.put("fieldName", ff.getName());
                                            jo.put("fieldTitle", ff.getTitle());
                                            jo.put("fieldType", ff.getFieldType());
                                            jo.put("fieldCond", condType); // 点时间、时间段或模糊、准确查询
                                            jo.put("fieldOptions", "");

                                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                                mu = mm.getMacroCtlUnit(ff.getMacroType());
                                                if (mu != null) {
                                                    IFormMacroCtl ifm = mu.getIFormMacroCtl();
                                                    if (ifm.getControlType().equals(FormField.TYPE_SELECT)) {
                                                        jo.put("controlType", FormField.TYPE_SELECT);
                                                        jo.put("fieldOptions", ifm.getControlOptions(userName, ff));
                                                    }
                                                }
                                            }

                                            jo.put("fieldValue", queryValue);

                                            if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                                                if (condType.equals("0")) {
                                                    String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
                                                    String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
                                                    jo.put("fromDate", fDate);
                                                    jo.put("toDate", tDate);
                                                }
                                            }
                                            conditions.put(jo);
                                        }
                                    }
                                }
                            }
                        }
                        jResult.put("conditions", conditions);

                        String[] ary = null;
                        request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
                        ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
                        String sql = ary[0];

                        // 增加权限控制
                        ModulePrivDb mpd = new ModulePrivDb(moduleCode);
                        boolean canAdd = mpd.canUserAppend(userName);
                        boolean canEdit = mpd.canUserModify(userName);
                        boolean canDel = mpd.canUserManage(userName);
                        jResult.put("canAdd", canAdd);
                        jResult.put("canEdit", canEdit);
                        jResult.put("canDel", canDel);

                        // 列名
                        JSONArray cols = new JSONArray();
                        jResult.put("cols", cols);

                        int i = 0;
                        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
                        String[] formFieldArr = msd.getColAry(false, "list_field");

                        if (formFieldArr != null && formFieldArr.length > 0) {
                            ListResult lr = fdao.listResult(formCode, sql, pageNum, pageSize);
                            int total = lr.getTotal();
                            if (total == 0) {
                                jResult.put("total", total); //总页数
                                jResult.put(RETURNCODE, RESULT_NO_DATA); //没有数据
                            } else {
                                jResult.put("total", total); //总页数
                                jResult.put(RETURNCODE, RESULT_SUCCESS); //请求成功
                                Iterator ir = null;
                                Vector v = lr.getResult();
                                JSONArray dataArr = new JSONArray();
                                if (v != null) {
                                    ir = v.iterator();
                                }

                                // 将不显示的字段加入fieldHide
                                String fieldHide = "";
                                Iterator irField = fd.getFields().iterator();
                                while (irField.hasNext()) {
                                    FormField ff = (FormField)irField.next();
                                    if (ff.getHide()==FormField.HIDE_ALWAYS) {
                                        if ("".equals(fieldHide)) {
                                            fieldHide = ff.getName();
                                        }
                                        else {
                                            fieldHide += "," + ff.getName();
                                        }
                                    }
                                }
                                String[] fdsHide = StrUtil.split(fieldHide, ",");
                                List<String> listHide = null;
                                if (fdsHide!=null) {
                                    listHide = Arrays.asList(ary);
                                }

                                WorkflowDb wf = new WorkflowDb();
                                HashMap<String, FormField> map = getFormFieldsByFromCode(formCode);
                                while (ir != null && ir.hasNext()) {
                                    fdao = (FormDAO) ir.next();

                                    RequestUtil.setFormDAO(request, fdao);

                                    long id = fdao.getId();
                                    JSONObject rowObj = new JSONObject();
                                    rowObj.put("id", id);
                                    rowObj.put("creator", um.getUserDb(fdao.getCreator()).getRealName());

                                    JSONArray fieldArr = new JSONArray();
                                    rowObj.put("fields", fieldArr);
                                    for (String fieldName : formFieldArr) {
                                        // 不需要显示操作列
                                        if (fieldName.equals("colOperate")) {
                                            continue;
                                        }
                                        JSONObject formFieldObj = new JSONObject(); //Form表单对象
                                        // System.out.println(ListAction.class.getName() + " fieldName=" + fieldName);
                                        String title = "";
                                        if (fieldName.startsWith("main:")) {
                                            String[] subFields = StrUtil.split(fieldName, ":");
                                            if (subFields.length == 3) {
                                                FormDb subfd = new FormDb(subFields[1]);
                                                title = subfd.getFieldTitle(subFields[2]);
                                            }
                                        } else if (fieldName.startsWith("other:")) {
                                            String[] otherFields = StrUtil.split(fieldName, ":");
                                            if (otherFields.length == 5) {
                                                FormDb otherFormDb = new FormDb(otherFields[2]);
                                                title = otherFormDb.getFieldTitle(otherFields[4]);
                                            }
                                        } else if (fieldName.equals("cws_creator")) {
                                            title = "创建者";
                                        } else if (fieldName.equals("ID")) {
                                            title = "ID";
                                        } else if (fieldName.equals("cws_progress")) {
                                            title = "进度";
                                        } else if (fieldName.equals("flowId")) {
                                            title = "流程号";
                                        }
                                        else if (fieldName.equals("cws_status")) {
                                            title = "状态";
                                        } else if (fieldName.equals("cws_flag")) {
                                            title = "冲抵状态";
                                        }
                                        else if (fieldName.equals("cws_create_date")) {
                                            title = "创建时间";
                                        }
                                        else if (fieldName.equals("flow_begin_date")) {
                                            title = "流程开始时间";
                                        }
                                        else if (fieldName.equals("flow_end_date")) {
                                            title = "流程结束时间";
                                        }
                                        else {
                                            title = fd.getFieldTitle(fieldName);
                                        }

                                        formFieldObj.put("name", fieldName);
                                        formFieldObj.put("title", title);

                                        if (i == 0) {
                                            JSONObject jo = new JSONObject();
                                            jo.put("name", fieldName);
                                            jo.put("title", title);
                                            cols.put(jo);
                                        }

                                        if (map.containsKey(fieldName)) {
                                            FormField ff = map.get(fieldName);
                                            String value = fdao.getFieldValue(fieldName); //表单值
                                            formFieldObj.put("value", value);
                                            String type = ff.getType();// 类型描述
                                            formFieldObj.put("type", type);
                                        }

                                        String controlText = "";
                                        if (fieldName.startsWith("main:")) {
                                            String[] subFields = fieldName.split(":");
                                            if (subFields.length == 3) {
                                                FormDb subfd = new FormDb(subFields[1]);
                                                com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
                                                FormField subff = subfd.getFormField(subFields[2]);
                                                String subsql = "select id from " + subfdao.getTableName() + " where cws_id=" + id + " order by cws_order";
                                                JdbcTemplate jt = new JdbcTemplate();
                                                StringBuilder sb = new StringBuilder();
                                                try {
                                                    ResultIterator ri = jt.executeQuery(subsql);
                                                    while (ri.hasNext()) {
                                                        ResultRecord rr = (ResultRecord) ri.next();
                                                        int subid = rr.getInt(1);
                                                        subfdao = new com.redmoon.oa.visual.FormDAO(subid, subfd);
                                                        String subFieldValue = subfdao.getFieldValue(subFields[2]);
                                                        if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
                                                            mu = mm.getMacroCtlUnit(subff.getMacroType());
                                                            if (mu != null) {
                                                                subFieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, subFieldValue);
                                                            }
                                                        }
                                                        sb.append("<span>").append(subFieldValue).append("</span>").append(ri.hasNext() ? "</br>" : "");
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                controlText = sb.toString();
                                            }
                                        } else if (fieldName.startsWith("other:")) {
                                            controlText = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName);
                                        } else if (fieldName.equals("ID")) {
                                            controlText = String.valueOf(fdao.getId());
                                        } else if (fieldName.equals("cws_progress")) {
                                            controlText = String.valueOf(fdao.getCwsProgress());
                                        } else if (fieldName.equals("cws_creator")) {
                                            String realName = "";
                                            if (fdao.getCreator() != null) {
                                                UserDb user = um.getUserDb(fdao.getCreator());
                                                if (user != null)
                                                    realName = user.getRealName();
                                            }
                                            controlText = realName;
                                        } else if (fieldName.equals("flowId")) {
                                            controlText = String.valueOf(fdao.getFlowId());
                                        } else if (fieldName.equals("cws_status")) {
                                            controlText = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
                                        }
                                        else if (fieldName.equals("cws_flag")) {
                                            controlText = com.redmoon.oa.flow.FormDAO.getCwsFlagDesc(fdao.getCwsFlag());
                                        }
                                        else if (fieldName.equals("cws_create_date")) {
                                            controlText = DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd HH:mm:ss");
                                        }
                                        else if (fieldName.equals("flow_begin_date")) {
                                            int flowId = fdao.getFlowId();
                                            if (flowId!=-1) {
                                                wf = wf.getWorkflowDb(flowId);
                                                controlText = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss");
                                            }
                                        }
                                        else if (fieldName.equals("flow_end_date")) {
                                            int flowId = fdao.getFlowId();
                                            if (flowId!=-1) {
                                                wf = wf.getWorkflowDb(flowId);
                                                controlText = DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss");
                                            }
                                        }
                                        else {
                                            FormField ff = fdao.getFormField(fieldName);
                                            if (ff == null) {
                                                controlText = fieldName + " 已不存在！";
                                            } else {
                                                // 隐藏
                                                if (listHide.contains(ff.getName())) {
                                                    continue;
                                                }
                                                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                                    mu = mm.getMacroCtlUnit(ff.getMacroType());
                                                    if (mu != null) {
                                                        controlText = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
                                                    }
                                                } else {
                                                    controlText = FuncUtil.renderFieldValue(fdao, ff);
                                                }
                                            }
                                        }

                                        formFieldObj.put("text", controlText);

                                        fieldArr.put(formFieldObj);
                                    }
                                    dataArr.put(rowObj);//组装json数组

                                    i++;
                                }

                                jResult.put(DATAS, dataArr);
                            }
                        }
                    }
                }
            }
            jReturn.put(RESULT, jResult);
        } catch (JSONException e) {
            Logger.getLogger(ModuleListAction.class.getName()).error(e.getMessage());
        } catch (ErrMsgException e) {
            Logger.getLogger(ModuleListAction.class.getName()).error(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获得所有字段信息
     *
     * @return
     */
    public HashMap<String, FormField> getFormFieldsByFromCode(String formCode) {
        HashMap<String, FormField> map = new HashMap<String, FormField>();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormDAO fdao = new FormDAO(fd);//获得所有表单元素
        Iterator fdaoIr = fdao.getFields().iterator();
        while (fdaoIr != null && fdaoIr.hasNext()) {
            FormField ff = (FormField) fdaoIr.next();
            map.put(ff.getName(), ff);
        }
        return map;

    }


}

