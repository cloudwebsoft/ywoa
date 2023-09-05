package com.redmoon.oa.visual;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.api.ISQLCtl;
import com.cloudweb.oa.permission.ModuleTreePermission;
import com.cloudweb.oa.security.FormSecurityUtil;
import com.cloudweb.oa.service.IVisualModuleTreePrivService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.security.ProtectSQLInjectException;
import com.cloudwebsoft.framework.security.ProtectXSSException;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.shell.BSHShell;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.LocalUtil;
import com.redmoon.oa.util.BeanShellUtil;
import com.redmoon.oa.util.RequestUtil;
import nl.bitwalker.useragentutils.DeviceType;
import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FormDAOMgr {
    String formCode;
    FormDb fd;
    FileUpload fu;
    long visualObjId;

    private ArrayList<FormDAO> prjUserDao;

    FormDAO fdao;

    public long getVisualObjId() {
        return visualObjId;
    }

    public void setVisualObjId(int visualObjId) {
        this.visualObjId = visualObjId;
    }

    public FormDAO getFormDAO() {
        return fdao;
    }

    public ArrayList<FormDAO> getPrjUserDao() {
        return prjUserDao;
    }

    public void setPrjUserDao(ArrayList<FormDAO> prjUserDao) {
        this.prjUserDao = prjUserDao;
    }

    public FormDAOMgr(FormDb fd) {
        this.formCode = fd.getCode();
        this.fd = fd;
        fu = new FileUpload();
    }

    public FormDAOMgr(String formCode) {
        this.formCode = formCode;
        fd = new FormDb();
        fd = fd.getFormDb(formCode);
        fu = new FileUpload();
    }

    public boolean doUpload(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        // String[] extnames = {"jpg", "gif", "xls", "rar", "doc", "rm", "avi",
        //                    "bmp", "swf"};
        // fu.setValidExtname(extnames); // 设置可上传的文件类型
        // if (formCode.equals("project"))
        // 	fu.setDebug(true);

        fu.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K
        int ret = 0;
        try {
            // fu.setDebug(true);
            ret = fu.doUpload(application, request);
            if (ret != FileUpload.RET_SUCCESS) {
                throw new ErrMsgException(fu.getErrMessage());
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException(e.getMessage());
        }

        String param = "", value = "";
        // 防攻击
        try {
            FormSecurityUtil.filter(formCode, fu);
        } catch (ProtectXSSException e) {
            LogUtil.getLog(getClass()).error(e);
            Privilege pvg = new Privilege();
            param = e.getParam();
            value = e.getValue();
            com.redmoon.oa.LogUtil.log(pvg.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "XSS " + getClass().getName() + " " + param + "=" + value);
            throw new ErrMsgException("XSS攻击: " + param + "=" + value + "，已记录！");
        } catch (ProtectSQLInjectException e) {
            Privilege pvg = new Privilege();
            param = e.getParam();
            value = e.getValue();
            com.redmoon.oa.LogUtil.log(pvg.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ " + getClass().getName() + " " + param + "=" + value);
            throw new ErrMsgException("SQL注入: " + param + "=" + value + "，已记录！");
        }
        return true;
    }

    public String getFieldValue(String fieldName) {
        return getFieldValue(fieldName, fu);
    }

    public static String getFieldValue(String fieldName, FileUpload fu) {
        String val = "";
        try {
            val = StrUtil.getNullStr(fu.getFieldValue(fieldName, true));
        } catch (ClassCastException e) {
            // 使表单域选择宏控件支持多选的时候，这里得到的应是数组，将其拼装成,号分隔的字符串
            String[] ary = fu.getFieldValues(fieldName);
            for (String str : ary) {
                if ("".equals(val)) {
                    val = str;
                } else {
                    val += "," + str;
                }
            }
        }
        return val;
    }

    /**
     * 根据fields获取request中所有域的值
     *
     * @param request HttpServletRequest
     */
    public static Vector<FormField> getFieldsByForm(HttpServletRequest request, FormDb fd, FileUpload fu, Vector<FormField> fields) throws ErrMsgException {
        for (FormField ff : fields) {
            if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                UserAgent ua = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
                OperatingSystem os = ua.getOperatingSystem();
                boolean isMobile = false;
                if (DeviceType.MOBILE.equals(os.getDeviceType())) {
                    isMobile = true;
                }
                if (!isMobile) {
                    String d = getFieldValue(ff.getName(), fu) + " " + getFieldValue(ff.getName() + "_time", fu);
                    // logger.info("getFieldsByForm:" + d);
                    ff.setValue(d);
                } else {
                    ff.setValue(getFieldValue(ff.getName(), fu));
                }
            } else if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                String val = getFieldValue(ff.getName(), fu);
                if ("".equals(val)) {
                    val = "0";
                }
                ff.setValue(val);
            } else {
                // 判断如果是文件框，则不需要取，因为getFiledValue中用fu.getFieldValue本身也取不到
                // 在其setValueForSave中会设其值，如果上传了文件或者被映射了值的话
                if ("macro_attachment".equals(ff.getMacroType())) {
                    String val = StrUtil.getNullStr(fu.getFieldValue(ff.getName()));
                    // 如果不为空，则说明是表单选择域宏控件映射的值，在此处赋值，可以便于验证时判断是否为空
                    if (!"".equals(val)) {
                        ff.setValue(val);
                    }
                } else {
                    if (FormField.FORMAT_THOUSANDTH.equals(ff.getFormat())) {
                        ff.setValue(getFieldValue(ff.getName(), fu).replaceAll(",", ""));
                    }
                    else {
                        ff.setValue(getFieldValue(ff.getName(), fu));
                    }
                }
            }
        }
        return fields;
    }

    /**
     * 根据fields获取request中所有域的值
     *
     * @param request HttpServletRequest
     */
    public Vector<FormField> getFieldsByForm(ServletContext application, HttpServletRequest request, Vector fields) throws ErrMsgException {
        doUpload(application, request);
        return getFieldsByForm(request, fd, fu, fields);
    }

    public boolean create(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        return create(application, request, fd.getFields(), fd.getCode());
    }

    public boolean create(ServletContext application, HttpServletRequest request, String moduleCode) throws ErrMsgException {
        return create(application, request, fd.getFields(), moduleCode);
    }

    public boolean createPrjMember(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        return createPrjMember(application, request, fd.getFields(), fd.getCode());
    }

    public boolean create(ServletContext application, HttpServletRequest request, ModuleSetupDb msd) throws ErrMsgException {
        String code = msd.getString("code");
        String formCode = msd.getString("form_code");
        // 如果是主表单
        // if (code.equals(formCode)) {
            // return create(application, request);
        // }

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        int viewEdit = msd.getInt("view_edit");
        if (viewEdit == ModuleSetupDb.VIEW_DEFAULT || viewEdit == ModuleSetupDb.VIEW_EDIT_CUSTOM) {
            return create(application, request, code);
        }

        FormViewDb fvd = new FormViewDb();
        fvd = fvd.getFormViewDb(viewEdit);
        if (fvd == null) {
            throw new ErrMsgException("视图ID=" + viewEdit + "不存在");
        }

        String content = fvd.getString("content");
        String ieVersion = fvd.getString("ie_version");

        FormParser fp = new FormParser();
        Vector<FormField> fields = fp.parseCtlFromView(content, ieVersion, fd);

        return create(application, request, fields, code);
    }

    public static String getParentFormCode(HttpServletRequest request) {
        // 当嵌套表格2添加时，formCode为父表单的编码，当前嵌套表的编码则为formCodeRelated
        String parentFormCode = "";
        String pageType = (String) request.getAttribute("pageType");
        if (pageType == null) {
            pageType = ParamUtil.get(request, "pageType");
        }

        // 仅module_add_relate.jsp中pageType为add，而nest_sheet_add_relate.jsp中为add_relate
        // flow表示来自于手机端，此时的flowId为url后的参数
        if ("add_relate".equals(pageType) || "add".equals(pageType) || "flow".equals(pageType)) {
            parentFormCode = ParamUtil.get(request, "formCode");
        }

        return parentFormCode;
    }

    /**
     * 创建
     *
     * @param request HttpServletRequest
     * @return boolean
     */
    public boolean create(ServletContext application, HttpServletRequest request, Vector<FormField> fields, String moduleCode) throws ErrMsgException {
        long actionId = StrUtil.toInt(StrUtil.getNullStr((String) request.getAttribute("actionId")), -1);
        if (actionId == -1) {
            actionId = ParamUtil.getLong(request, "actionId", -1);
        }
        String userName = new Privilege().getUser(request);

        String pageType = (String) request.getAttribute("pageType");
        if (pageType == null) {
            pageType = ParamUtil.get(request, "pageType");
        }
        String parentPageType = ParamUtil.get(request, "parentPageType");

        String parentFormCode = getParentFormCode(request);
        boolean isFlow = false;

        ModulePrivDb mpd = new ModulePrivDb(moduleCode);
        if (actionId == -1 || actionId == 0) {
            String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
            if (fieldWrite != null) {
                if (!"".equals(fieldWrite)) {
                    String[] fieldAry = StrUtil.split(fieldWrite, ",");
                    if (fieldAry != null) {
                        fields = new Vector<>();
                        for (String s : fieldAry) {
                            try {
                                // 需clone，否则会导致fields中字段被更新后，影响fd.getFormField(...)取得的原始值
                                // 致嵌套表格2在智能模块添加后，立刻向流程中添加出现缓存问题，这时会看到表单中出现了上一次添加的值
                                FormField ff = fd.getFormField(s);
                                if (ff == null) {
                                    continue;
                                }
                                fields.addElement((FormField)ff.clone());
                            } catch (CloneNotSupportedException e) {
                                LogUtil.getLog(getClass()).error(e);
                            }
                        }
                    }
                }
            }
            // 不去掉验证，因为二开的时候有些字段需隐藏
/*	       	// 去掉隐藏字段的验证
	       	String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
	        String[] fdsHide = StrUtil.split(fieldHide, ",");        	
	       	if (fdsHide!=null) {
	       		for (int i=0; i<fdsHide.length; i++) {
	       			Iterator ir = fields.iterator();
	       			while (ir.hasNext()) {
	       				FormField ff = (FormField)ir.next();
	       				if (fdsHide[i].equals(ff.getName())) {
	       					ir.remove();
	       				}
	       			}
	       		}
	       	}	 */
        } else {
            WorkflowActionDb wfa = new WorkflowActionDb();
            wfa = wfa.getWorkflowActionDb((int) actionId);
            String fieldWrite = StrUtil.getNullString(wfa.getFieldWrite()).trim();

            isFlow = true;
            String[] fds = fieldWrite.split(",");

            MacroCtlMgr mm = new MacroCtlMgr();
            int nestLen = "nest.".length();
            // 将嵌套表中可写的域筛选出
            Iterator<FormField> ir = fields.iterator();
            while (ir.hasNext()) {
                FormField ff = ir.next();
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    // 跳过SQL控件，因为当SQL控件不可写时，仍需记录其值
                    if (mu.getIFormMacroCtl() instanceof ISQLCtl) {
                        continue;
                    }
                }

                boolean isFound = false;
                for (String s : fds) {
                    // 如果不是嵌套表格2的可写表单域
                    if (!s.startsWith("nest.")) {
                        continue;
                    }
                    String fName = s.substring(nestLen);
                    if (ff.getName().equals(fName)) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    ir.remove();
                }
            }
        }

        fields = getFieldsByForm(application, request, fields);

        FormDAO fdao = new FormDAO(fd);
        fdao.setFields(fields);

        Privilege privilege = new Privilege();
        // 取主模块
        ModuleSetupDb vsd = new ModuleSetupDb();
        vsd = vsd.getModuleSetupDbOrInit(formCode);
		boolean re = true;
        // 通过检查接口对fields中的值进行有效性判断
        Config cfg = new Config();
        IModuleChecker ifv = cfg.getIModuleChecker(formCode);

        boolean canUserData = mpd.canUserData(userName);
        if (!canUserData) {
            // 对表单进行有效性验证
            com.redmoon.oa.flow.FormDAOMgr.validateFields(request, fu, fields, null, false);
            // logger.info("ifv=" + ifv + " formCode=" + formCode);

            if (ifv != null) {
                re = ifv.validateCreate(request, fu, fields);
            }
            if (!re) {
                throw new ErrMsgException("表单验证非法！");
            }

            /*20190922 去掉验证，因为已被“验证规则”取代
            // 模块验证
            String validateProp = StrUtil.getNullStr(vsd.getString("validate_prop"));
            if (!"".equals(validateProp)) {
                String cond = ModuleUtil.parseValidate(request, fu, fdao, validateProp);
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByName("javascript");
                try {
                    Boolean ret = (Boolean) engine.eval(cond);
                    if (!ret.booleanValue()) {
                        String msg = vsd.getString("validate_msg");
                        if ("".equals(msg)) {
                            msg = LocalUtil.LoadString(request, "res.flow.Flow", "validError");
                        }
                        throw new ErrMsgException(StrUtil.getNullStr(msg));
                    }
                } catch (ScriptException ex) {
                    LogUtil.getLog(getClass()).error(ex);
                }
            }*/

            // 校验验证规则
            ModuleUtil.doCheckSetup(request, userName, fdao, fu);

            // 执行验证脚本
            String script = vsd.getScript("validate");
            if (script != null && !"".equals(script)) {
                BSHShell bs = new BSHShell();
                Privilege pvg = new Privilege();
                bs.set("userName", pvg.getUser(request));
                bs.set("action", "create");
                bs.set("fdao", fdao);
                bs.set("request", request);
                bs.set("fileUpload", fu);
                bs.eval(script);

                Object obj = bs.get("ret");
                if (obj != null) {
                    boolean ret = (Boolean) obj;
                    if (!ret) {
                        String errMsg = (String) bs.get("errMsg");
                        if (errMsg != null) {
                            throw new ErrMsgException("提示：" + errMsg);
                        } else {
                            throw new ErrMsgException("验证非法，但errMsg为空！");
                        }
                    }
                } else {
                    // 需要判断action进行检测，因为delete事件中可能要验证，而当创建验证时，该情况下脚本中未写ret值
                    // throw new ErrMsgException("该节点脚本中未配置ret=...");
                }
            }
        }
		
		// 对函数型的表单域赋值
        for (FormField ff : fields) {
            if (ff.isFunc()) {
                ff.setValue(String.valueOf(FuncUtil.render(ff, ff.getDefaultValueRaw(), fdao)));
            }
        }

        fdao.setCreator(userName);
        // 置单位
        Privilege pvg = new Privilege();
        fdao.setUnitCode(pvg.getUserUnitCode(request));

        // 置用于关联模块的cws_id的值
        String cwsId = StrUtil.getNullStr(fu.getFieldValue("cws_id"));
        cwsId = StrUtil.emptyTo(cwsId, FormDAO.TEMP_CWS_ID);
        fdao.setCwsId(cwsId);

        // 置父模块编码，仅当前表单为嵌套表格或关联的子表时
        fdao.setCwsParentForm(parentFormCode);

        int cwsStatus = com.redmoon.oa.flow.FormDAO.STATUS_DONE;
        if (isFlow) {
            cwsStatus = com.redmoon.oa.flow.FormDAO.STATUS_NOT;
        }/* else {
            // 20221012 注释，因为只要在流程中，通过isFlow判断即可确定cwsStatus
            // 流程中添加嵌套表格2记录，nest_sheet_add_relate.jsp，表单中上传了cwsStatus，且为STATUS_NOT状态
            cwsStatus = StrUtil.toInt(fu.getFieldValue("cwsStatus"), com.redmoon.oa.flow.FormDAO.STATUS_DONE);
        }*/
        fdao.setCwsStatus(cwsStatus);

        int flowId = StrUtil.toInt(fu.getFieldValue("flowId"), com.redmoon.oa.visual.FormDAO.NONEFLOWID);
        // 如果是来自于手机端流程中的嵌套表，20220901检查pageType，在nest_sheet_add_edit.jsp的mui.nest_sheet.js中当添加时为add（而非flowId），当修改时为空，所以需通过request直接取flowId
        // if ("flow".equals(pageType)) {
        if (flowId == FormDAO.NONEFLOWID) {
            flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);
        }
        if (flowId == com.redmoon.oa.visual.FormDAO.NONEFLOWID) {
            // 如果是嵌套表格添加页，且不在流程中添加，则取其父记录的flowId，以使得在模块中添加嵌套表格记录后仍能够与流程相关联
            if (ConstUtil.PAGE_TYPE_ADD_RELATE.equals(pageType)) {
                long parentId = ParamUtil.getLong(request, "parentId", -1);
                if (parentId != -1) {
                    FormDb fdParent = new FormDb();
                    fdParent = fdParent.getFormDb(parentFormCode);
                    FormDAO fdaoParent = new FormDAO();
                    fdaoParent = fdaoParent.getFormDAO(parentId, fdParent);
                    if (fdaoParent.isLoaded()) {
                        flowId = fdaoParent.getFlowId();
                    }
                }
            }
        }
        fdao.setFlowId(flowId);

        // 如果手机端父页面为添加，则说明是来自于nest_sheet_add_edit.jsp中，此处置flowTypeCode为-1，表示临时数据
        if ("add".equals(parentPageType)) {
            fdao.setFlowTypeCode("-1");
        }

        re = fdao.create(request, fu);
        if (re) {
            this.fdao = fdao;

            visualObjId = fdao.getId();

            if (ifv != null) {
                try {
                    re = ifv.onCreate(request, fdao);
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }

            // 如果需要记录历史
            if (fd.isLog()) {
                FormDAO.log(userName, FormDAOLog.LOG_TYPE_CREATE, fdao);
            }

            // 为关联模块中的表单型（单条记录）生成一条记录
            ModuleRelateDb mrd = new ModuleRelateDb();
            Vector v = mrd.getModulesRelated(formCode);
            for (Object o : v) {
                mrd = (ModuleRelateDb) o;
                if (mrd.getInt("relate_type") == ModuleRelateDb.TYPE_SINGLE) {
                    FormDb fdRelate = new FormDb();
                    fdRelate = fdRelate.getFormDb(mrd.getString("relate_code"));
                    FormDAO fdaoRelate = new FormDAO(fdRelate);
                    fdaoRelate.setCwsId(StrUtil.emptyTo(getRelateFieldValue(fdao.getId(), mrd.getString("relate_code")), com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID));
                    fdaoRelate.createEmptyForm();
                } else {
                    FormDb fdRelate = new FormDb();
                    fdRelate = fdRelate.getFormDb(mrd.getString("relate_code"));
                    // 有可能是副模块
                    if (!fdRelate.isLoaded()) {
                        ModuleSetupDb msd = new ModuleSetupDb();
                        msd = msd.getModuleSetupDb(mrd.getString("relate_code"));
                        fdRelate = fdRelate.getFormDb(msd.getString("form_code"));
                    }
                    FormDAO fdaoRelate = new FormDAO();
                    ifv = cfg.getIModuleChecker(fdRelate.getCode());

                    // 检查从模块是否有加入表单的临时temp_cws_ids_formCode
                    String fName = FormDAO.NAME_TEMP_CWS_IDS + "_" + fdRelate.getCode();
                    String[] ary = fu.getFieldValues(fName);
                    if (ary != null) {
                        for (String s : ary) {
                            fdaoRelate = fdaoRelate.getFormDAO(StrUtil.toLong(s), fdRelate);
                            if (fdaoRelate == null || !fdaoRelate.isLoaded()) {
                                continue;
                            }
                            fdaoRelate.setCwsId(StrUtil.emptyTo(getRelateFieldValue(fdao.getId(), mrd.getString("relate_code")), com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID));
                            fdaoRelate.save();

                            if (ifv != null) {
                                try {
                                    re = ifv.onCreate(request, fdaoRelate);
                                } catch (Exception e) {
                                    LogUtil.getLog(getClass()).error(e);
                                }
                            }
                        }
                    }
                }
            }

            ModuleSetupDb msd = new ModuleSetupDb();
            // 手机端中module_add_edit.jsp中置id有值，以便于添加嵌套表记录作关联，而PC端request中不会带有id
            // 手机端嵌套表中置flowTypeCode为-1表示临时记录，详见story
            long idTemp = ParamUtil.getLong(request, "id", -1);
            DebugUtil.i(getClass(), "create", "idTemp=" + idTemp);
            if (idTemp!=-1) {
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    mrd = (ModuleRelateDb) ir.next();
                    // 手机端不支持单表关联
                    if (mrd.getInt("relate_type") == ModuleRelateDb.TYPE_SINGLE) {
                        ;
                    } else {
                        msd = msd.getModuleSetupDb(mrd.getString("relate_code"));
                        if (msd==null) {
                            LogUtil.getLog(getClass()).error("模块：" + mrd.getString("relate_code") + " 不存在");
                            msd = new ModuleSetupDb();
                            continue;
                        }

                        String relateFormCode = msd.getString("form_code");

                        FormDAO fdaoRelate = new FormDAO();
                        ifv = cfg.getIModuleChecker(relateFormCode);

                        // 当主表单保存时，如果id存在，则说明是通过手机端保存
                        // 根据在module_add_edit.jsp中生成的id=RandomSecquenceCreator.getId(20)，更新嵌套表中的cws_id为主表单记录的实际ID，并置其flowTypeCode为主表单的flowTypeCode
                        String sql = "select id from ft_" + relateFormCode + " where cws_id='" + idTemp + "'";
                        for (FormDAO formDAO : fdaoRelate.list(relateFormCode, sql)) {
                            formDAO.setCwsId(String.valueOf(visualObjId));
                            formDAO.setFlowTypeCode(fdao.getFlowTypeCode());
                            formDAO.save();
                            if (ifv != null) {
                                try {
                                    re = ifv.onCreate(request, formDAO);
                                } catch (Exception e) {
                                    LogUtil.getLog(getClass()).error(e);
                                }
                            }
                        }
                    }
                }
            }

            // 如果表单中含有“表单域选择宏控件”，且当前模块是该宏控件对应的表单的从模块，则自动关联
            // 例如：单独添加合同付款，而不是先打开合同，再添加付款记录，此时则自动关联合同

            // 宏控件为智能模块表单型，如其formCode不为空，则表单编码为其formCode，如为空，则由控件自行去解析
            // 相应地需要在宏控件中增加虚方法getFormCode()

            // 如果cwsId不为空，则表示在添加时，是从主模块入口进入的
            if ("".equals(cwsId)) {
                MacroCtlMgr mm = new MacroCtlMgr();
                for (FormField ff : fields) {
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        if (ff.getValue() == null || "".equals(ff.getValue())) {
                            continue;
                        }

                        // 如果不是long类型的，则说明字段中存储的不是ID，字段对应的不可能是主模块
                        long mainFormId = StrUtil.toLong(ff.getValue(), -1);
                        if (mainFormId == -1) {
                            continue;
                        }

                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        // 如果是表单域选择窗体
                        if (mu.isForm()) {
                            String mainFormCode = mu.getFormCode();
                            if ("".equals(mainFormCode)) {
                                mainFormCode = StrUtil.getNullStr(mu.getIFormMacroCtl().getFormCode(request, ff));
                            }
                            if ("".equals(mainFormCode)) {
                                continue;
                            }

                            boolean isRelateToMainForm = false;
                            // 检查该mainFormCode是否为当前表单的主表单
                            for (Object o : mrd.getModulesRelated(mainFormCode)) {
                                mrd = (ModuleRelateDb) o;
                                if (mrd.getString("relate_code").equals(formCode)) {
                                    isRelateToMainForm = true;
                                    break;
                                }
                            }
                            if (isRelateToMainForm) {
                                FormDAOMgr fdmMain = new FormDAOMgr(mainFormCode);
                                String relateId = fdmMain.getRelateFieldValue(mainFormId, mrd.getString("relate_code"));
                                fdao.setCwsId(StrUtil.emptyTo(relateId, com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID));
                                fdao.save();
                            }
                        }
                    }
                }
            }
            // 发送消息
            sendRemindMsg(request, vsd, fdao, "create");

            // 添加事件脚本处理
            String script = vsd.getScript("create");
            if (script != null) {
                BSHShell bs = new BSHShell();
                StringBuffer sb = new StringBuffer();
                BeanShellUtil.setFieldsValue(fdao, sb);
                // 赋值当前用户
                sb.append("userName=\"" + privilege.getUser(request) + "\";");
                // 这样的方法，会使得fdao被解释为字符串，即使强制类型转换都不行
                // sb.append("fdao=\"" + fdao + "\";");
                bs.set("fdao", fdao);
                bs.set("request", request);
                bs.set("fileUpload", fu);
                bs.eval(BeanShellUtil.escape(sb.toString()));
                bs.eval(script);
            }
        }
        return re;
    }

    /**
     * 运行查看事件脚本
     * @param request
     * @param privilege
     * @param msd
     * @param fdao
     */
    public void runScriptOnSee(HttpServletRequest request, Privilege privilege, ModuleSetupDb msd, FormDAO fdao) {
        String script = msd.getScript("see");
        if (script != null) {
            Interpreter bsh = new Interpreter();
            try {
                StringBuffer sb = new StringBuffer();

                BeanShellUtil.setFieldsValue(fdao, sb);

                // 赋值当前用户
                sb.append("userName=\"" + privilege.getUser(request) + "\";");
                // 这样的方法，会使得fdao被解释为字符串，即使强制类型转换都不行
                // sb.append("fdao=\"" + fdao + "\";");

                bsh.set("fdao", fdao);
                bsh.set("request", request);

                bsh.eval(BeanShellUtil.escape(sb.toString()));

                bsh.eval(script);
                Object obj = bsh.get("ret");
                if (obj != null) {
                    boolean ret = (Boolean) obj;
                    if (!ret) {
                        String errMsg = (String) bsh.get("errMsg");
                        LogUtil.getLog(getClass()).error("see bsh errMsg=" + errMsg);
                    }
                }
            } catch (EvalError e) {
                DebugUtil.i(getClass(), "see 查看事件出错", fdao.getFormDb().getName() + " " + fdao.getFormCode());
                DebugUtil.i(getClass(), "see 查看事件出错", StrUtil.trace(e));
            }
        }
    }

    public void sendRemindMsg(HttpServletRequest request, ModuleSetupDb vsd, FormDAO fdao, String eventType) {
        String msgProp = vsd.getString("msg_prop");
        Element action = null;
        boolean isFound = false;
        if (msgProp != null && !"".equals(msgProp)) {
            try {
                SAXBuilder parser = new SAXBuilder();
                org.jdom.Document docu = parser.build(new InputSource(new StringReader(msgProp)));
                Element root = docu.getRootElement();
                List<Element> actions = root.getChildren("action");
                Iterator<Element> irAction = null;
                if (actions != null) {
                    irAction = actions.iterator();
                }
                while (irAction != null && irAction.hasNext()) {
                    action = irAction.next();
                    String attEventType = action.getAttributeValue("eventType");
                    if (eventType.equals(attEventType)) {
                        isFound = true;
                        break;
                    }
                }
            } catch (JDOMException e) {
                LogUtil.getLog(getClass()).error("parse msgProp field jdom error");
                LogUtil.getLog(getClass()).error(e);
            } catch (IOException e) {
                LogUtil.getLog(getClass()).error("parse msgProp field io error");
                LogUtil.getLog(getClass()).error(e);
            }
        }

        if (!isFound) {
            return;
        }

        String deptFields = action.getChildText("deptFields");
        String userFields = action.getChildText("userFields");
        String users = action.getChildText("users");
        String roles = action.getChildText("roles");

        String title = action.getChildText("title");
        String content = action.getChildText("content");

        Privilege pvg = new Privilege();
        UserDb user = new UserDb();
        user = user.getUserDb(pvg.getUser(request));

        title = title.replaceFirst("\\$fromUser", user.getRealName());
        content = content.replaceFirst("\\$fromUser", user.getRealName());

        MacroCtlMgr mm = new MacroCtlMgr();

        RequestUtil.setFormDAO(request, fdao);
        // 处理表单域
        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(title);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            FormField ff = fd.getFormField(fieldName);
            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu != null) {
                    m.appendReplacement(sb, mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
                } else {
                    m.appendReplacement(sb, "控件不存在");
                }
            } else {
                m.appendReplacement(sb, fdao.getFieldValue(fieldName));
            }
        }
        m.appendTail(sb);
        title = sb.toString();

        m = p.matcher(content);
        sb = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            FormField ff = fd.getFormField(fieldName);
            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu != null) {
                    m.appendReplacement(sb, mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
                } else {
                    m.appendReplacement(sb, "控件不存在");
                }
            } else {
                m.appendReplacement(sb, fdao.getFieldValue(fieldName));
            }
        }
        m.appendTail(sb);
        content = sb.toString();

        boolean isMsg = "true".equals(action.getChildText("isMsg"));
        boolean isMail = "true".equals(action.getChildText("isMail"));
        boolean isSms = "true".equals(action.getChildText("isSms"));

        UserMgr um = new UserMgr();

        Vector<UserDb> v = new Vector<>();
        Map<String, String> map = new HashMap<>();

        // 取出部门
        DeptUserDb dud = new DeptUserDb();
        String[] dNames = StrUtil.split(deptFields, ",");
        if (dNames != null) {
            for (int i = 0; i < dNames.length; i++) {
                Vector<DeptUserDb> vt = dud.list(dNames[i]);
                for (DeptUserDb deptUserDb : vt) {
                    dud = deptUserDb;
                    if (!map.containsKey(dud.getUserName())) {
                        v.addElement(um.getUserDb(dud.getUserName()));
                        map.put(dud.getUserName(), "");
                    }
                }
            }
        }

        // 取出用户域
        String[] uNames = StrUtil.split(userFields, ",");
        if (uNames != null) {
            for (String uName : uNames) {
                if (!map.containsKey(uName)) {
                    v.addElement(um.getUserDb(uName));
                    map.put(uName, "");
                }
            }
        }

        // 取出角色
        String[] roleAry = StrUtil.split(roles, ",");
        if (roleAry != null) {
            RoleDb rd = new RoleDb();
            for (String s : roleAry) {
                rd = rd.getRoleDb(s);
                if (rd == null) {
                    rd = new RoleDb();
                    continue;
                }
                for (Object o : rd.getAllUserOfRole()) {
                    UserDb ud = (UserDb) o;
                    if (!map.containsKey(ud.getName())) {
                        v.addElement(ud);
                        map.put(ud.getName(), "");
                    }
                }
            }
        }

        // 取出用户
        uNames = StrUtil.split(users, ",");
        if (uNames != null) {
            for (String uName : uNames) {
                if (!map.containsKey(uName)) {
                    v.addElement(um.getUserDb(uName));
                    map.put(uName, "");
                }
            }
        }

        String charset = Global.getSmtpCharset();
        cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
        String senderName = StrUtil.GBToUnicode(Global.AppName);
        senderName += "<" + Global.getEmail() + ">";
        String mailserver = Global.getSmtpServer();
        int smtp_port = Global.getSmtpPort();
        String name = Global.getSmtpUser();
        String pwd_raw = Global.getSmtpPwd();
        boolean isSsl = Global.isSmtpSSL();
        try {
            sendmail.initSession(mailserver, smtp_port, name,
                    pwd_raw, "", isSsl);
        } catch (Exception ex) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
        }

        // String tail = getFormAbstractTable(wf);
        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        MessageDb md = new MessageDb();

        for (UserDb ud : v) {
            if (isSms && SMSFactory.isUseSMS()) {
                IMsgUtil imu = SMSFactory.getMsgUtil();
                if (imu != null) {
                    try {
                        imu.send(ud, title, MessageDb.SENDER_SYSTEM);
                    } catch (ErrMsgException e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }
            }

            // 发送信息
            if (isMsg) {
                try {
                    md.sendSysMsg(ud.getName(), title, content, "");
                } catch (ErrMsgException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }

            if (isMail && ud.getEmail() != null && !"".equals(ud.getEmail())) {
                sendmail.initMsg(ud.getEmail(), senderName, title, content, true);
                sendmail.send();
                sendmail.clear();
            }
        }
    }

    /**
     * 创建
     *
     * @param request HttpServletRequest
     * @return boolean
     */
    public boolean createPrjMember(ServletContext application, HttpServletRequest request, Vector<FormField> fields, String moduleCode) throws ErrMsgException {
        if (!"project_members".equals(formCode)) {
            return create(application, request, fields, fd.getCode());
        }

        fields = getFieldsByForm(application, request, fields);

        String prjUser = "";
        for (FormField ff : fields) {
            if ("prj_user".equals(ff.getName())) {
                prjUser = ff.getValue();
                break;
            }
        }

        String[] prjUsers = prjUser.split(",");

        boolean re = true;
        prjUserDao = new ArrayList<FormDAO>();

        for (String user : prjUsers) {
            Vector prjVec = new Vector();
            fu.setFieldValue("prj_user", user);
            Iterator it = fields.iterator();
            while (it.hasNext()) {
                FormField ff = (FormField) it.next();
                if (ff.getName().equals("prj_user")) {
                    ff.setValue(user);
                }
                try {
                    prjVec.add(ff.clone());
                } catch (CloneNotSupportedException e) {
                    throw new ErrMsgException("创建失败！");
                }
            }
            // 对表单进行有效性验证
            com.redmoon.oa.flow.FormDAOMgr.validateFields(request, fu, prjVec, null, false);

            // 通过检查接口对fields中的值进行有效性判断
            Config cfg = new Config();
            IModuleChecker ifv = cfg.getIModuleChecker(formCode);
            // logger.info("ifv=" + ifv + " formCode=" + formCode);
            if (ifv != null) {
                re = ifv.validateCreate(request, fu, prjVec);
            }
            if (!re) {
                throw new ErrMsgException("表单验证非法！");
            }

            FormDAO fdao = new FormDAO(fd);
            fdao.setFields(prjVec);

            Privilege privilege = new Privilege();

            ModuleSetupDb vsd = new ModuleSetupDb();
            vsd = vsd.getModuleSetupDbOrInit(formCode);

            // 执行验证脚本
            String script = vsd.getScript("validate");
            if (script != null && !script.equals("")) {
                Interpreter bsh = new Interpreter();
                try {
                    StringBuffer sb = new StringBuffer();

                    BeanShellUtil.setFieldsValue(fdao, sb);

                    // 赋值用户
                    sb.append("userName=\"" + privilege.getUser(request) + "\";");
                    bsh.eval(BeanShellUtil.escape(sb.toString()));

                    bsh.set("action", "create");
                    bsh.set("fdao", fdao);
                    bsh.set("request", request);
                    bsh.set("fileUpload", fu);

                    bsh.eval(script);
                    Object obj = bsh.get("ret");
                    if (obj != null) {
                        boolean ret = ((Boolean) obj).booleanValue();
                        if (!ret) {
                            String errMsg = (String) bsh.get("errMsg");
                            if (errMsg != null)
                                throw new ErrMsgException("提示：" + errMsg);
                            else {
                                throw new ErrMsgException("验证非法，但errMsg为空！");
                            }
                        }
                    } else {
                        // 需要判断action进行检测，因为delete事件中可能要验证，而当创建验证时，该情况下脚本中未写ret值
                        // throw new ErrMsgException("该节点脚本中未配置ret=...");
                    }
                } catch (EvalError e) {
                    // TODO Auto-generated catch block
                    LogUtil.getLog(getClass()).error(e);
                }
            }

            String userName = privilege.getUser(request);
            fdao.setCreator(userName);
            // 置单位
            Privilege pvg = new Privilege();
            fdao.setUnitCode(pvg.getUserUnitCode(request));

            // 置用于关联模块的cws_id的值
            String cwsId = StrUtil.getNullStr(fu.getFieldValue("cws_id"));
            fdao.setCwsId(StrUtil.emptyTo(cwsId, FormDAO.TEMP_CWS_ID));

            // 流程中添加嵌套表格2记录，nest_sheet_add_relate.jsp，表单中上传了cwsStatus，且为STATUS_NOT状态
            int cwsStatus = StrUtil.toInt(fu.getFieldValue("cwsStatus"), com.redmoon.oa.flow.FormDAO.STATUS_DONE);
            fdao.setCwsStatus(cwsStatus);

            int flowId = StrUtil.toInt(fu.getFieldValue("flowId"), com.redmoon.oa.visual.FormDAO.NONEFLOWID);
            fdao.setFlowId(flowId);

            re = fdao.create(request, fu);
            if (re) {
                prjUserDao.add(fdao);

                this.fdao = fdao;

                visualObjId = fdao.getId();

                if (ifv != null) {
                    try {
                        re = ifv.onCreate(request, fdao);
                    } catch (Exception e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }

                // 如果需要记录历史
                if (fd.isLog()) {
                    FormDAO.log(userName, FormDAOLog.LOG_TYPE_CREATE, fdao);
                }

                // 为关联模块中的表单型（单条记录）生成一条记录
                ModuleRelateDb mrd = new ModuleRelateDb();
                for (Object o : mrd.getModulesRelated(formCode)) {
                    mrd = (ModuleRelateDb) o;
                    if (mrd.getInt("relate_type") == ModuleRelateDb.TYPE_SINGLE) {
                        FormDb fdRelate = new FormDb();
                        fdRelate = fdRelate.getFormDb(mrd.getString("relate_code"));
                        FormDAO fdaoRelate = new FormDAO(fdRelate);
                        fdaoRelate.setCwsId(StrUtil.emptyTo(getRelateFieldValue(fdao.getId(), mrd.getString("relate_code")), FormDAO.CWS_ID_NONE));
                        fdaoRelate.createEmptyForm();
                    } else {
                        FormDb fdRelate = new FormDb();
                        fdRelate = fdRelate.getFormDb(mrd.getString("relate_code"));
                        FormDAO fdaoRelate = new FormDAO();
                        ifv = cfg.getIModuleChecker(fdRelate.getCode());

                        // 检查从模块是否有加入表单的临时temp_cws_ids_formCode
                        String fName = FormDAO.NAME_TEMP_CWS_IDS + "_" + mrd.getString("relate_code");
                        String[] ary = fu.getFieldValues(fName);
                        if (ary != null) {
                            for (String s : ary) {
                                fdaoRelate = fdaoRelate.getFormDAO(StrUtil.toLong(s), fdRelate);
                                if (fdaoRelate == null || !fdaoRelate.isLoaded()) {
                                    continue;
                                }
                                fdaoRelate.setCwsId(StrUtil.emptyTo(getRelateFieldValue(fdao.getId(), mrd.getString("relate_code")), com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID));
                                fdaoRelate.save();

                                if (ifv != null) {
                                    try {
                                        re = ifv.onCreate(request, fdaoRelate);
                                    } catch (Exception e) {
                                        LogUtil.getLog(getClass()).error(e);
                                    }
                                }
                            }
                        }
                    }
                }

                script = vsd.getScript("create");
                if (script != null) {
                    Interpreter bsh = new Interpreter();
                    try {
                        StringBuffer sb = new StringBuffer();

                        BeanShellUtil.setFieldsValue(fdao, sb);

                        // 赋值当前用户
                        sb.append("userName=\"" + privilege.getUser(request)
                                + "\";");
                        sb.append("fdao=\"" + fdao + "\";");

                        bsh.set("request", request);
                        bsh.set("fileUpload", fu);

                        bsh.eval(BeanShellUtil.escape(sb.toString()));

                        bsh.eval(script);
                        Object obj = bsh.get("ret");
                        if (obj != null) {
                            boolean ret = (Boolean) obj;
                            if (!ret) {
                                String errMsg = (String) bsh.get("errMsg");
                                LogUtil.getLog(getClass()).error(
                                        "create bsh errMsg=" + errMsg);
                            }
                        }
                    } catch (EvalError e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }

            }
        }

        return re;
    }

    public FormDAO getFormDAO(long visualObjId) {
        FormDAO fdao = new FormDAO();
        return fdao.getFormDAOByCache(visualObjId, fd);
    }

    public boolean update(ServletContext application, HttpServletRequest request, ModuleSetupDb msd) throws ErrMsgException {
        String code = msd.getString("code");
        String formCode = msd.getString("form_code");
        // 如果是主表单
        if (code.equals(formCode)) {
            // return update(application, request);
        }

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        int viewEdit = msd.getInt("view_edit");
        if (viewEdit == ModuleSetupDb.VIEW_DEFAULT || viewEdit == ModuleSetupDb.VIEW_EDIT_CUSTOM) {
            return update(application, request, code);
        }

        FormViewDb fvd = new FormViewDb();
        fvd = fvd.getFormViewDb(viewEdit);
        if (fvd == null) {
            throw new ErrMsgException("视图ID=" + viewEdit + "不存在");
        }

        // String form = fvd.getString("form");
        String ieVersion = fvd.getString("ie_version");

        FormParser fp = new FormParser();
        Vector<FormField> fields = fp.parseCtlFromView(fvd.getString("content"), ieVersion, fd);
        return update(application, request, fields, code);
    }

    public boolean update(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        return update(application, request, fd.getFields(), fd.getCode());
    }

    public boolean update(ServletContext application, HttpServletRequest request, String moduleCode) throws ErrMsgException {
        return update(application, request, fd.getFields(), moduleCode);
    }

    public boolean update(ServletContext application, HttpServletRequest request, Vector<FormField> fields, String moduleCode) throws ErrMsgException {
        Privilege privilege = new Privilege();

        // 从request中获取表单中域的值
        fields = getFieldsByForm(application, request, fields);

        String userName = privilege.getUser(request);
        long actionId = StrUtil.toInt(StrUtil.getNullStr((String) request.getAttribute("actionId")), -1);

        visualObjId = StrUtil.toLong(fu.getFieldValue("id"), -1);
        if (visualObjId == -1) {
            // 20170227 手机端的明细表中编辑时可能未传过来
            visualObjId = ParamUtil.getLong(request, "id", -1);
            if (visualObjId == -1) {
                throw new ErrMsgException("缺少ID！");
            }
        }

        FormDAO fdaoOld = getFormDAO(visualObjId);
        FormDAO fdao = getFormDAO(visualObjId);
        Config cfg = new Config();
        IModuleChecker ifv = cfg.getIModuleChecker(formCode);
        // 取得主模块
        ModuleSetupDb vsd = new ModuleSetupDb();
        vsd = vsd.getModuleSetupDb(formCode);
        boolean re = true;
        ModulePrivDb mpd = new ModulePrivDb(moduleCode);
        if (!mpd.canUserData(userName)) {
            // 复制一份用于校验
            Vector<FormField> fieldsForCheck = new Vector<FormField>();
            for (FormField field : fields) {
                try {
                    FormField ff = (FormField) field.clone();
                    fieldsForCheck.addElement(ff);
                } catch (CloneNotSupportedException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }

            if (actionId == -1) {
                String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
                if (fieldWrite != null) {
                    if (!"".equals(fieldWrite)) {
                        String[] fieldAry = StrUtil.split(fieldWrite, ",");
                        if (fieldAry != null) {
                            Iterator<FormField> ir = fieldsForCheck.iterator();
                            while (ir.hasNext()) {
                                FormField ff = ir.next();
                                boolean isWrite = false;
                                for (String s : fieldAry) {
                                    if (ff.getName().equals(s)) {
                                        isWrite = true;
                                        break;
                                    }
                                }
                                if (!isWrite) {
                                    ir.remove();
                                }
                            }
                        }
                    }
                }
                // 20171209 二开的时候可能需要用到隐藏字段，所以不隐藏
    /*	       	// 去掉隐藏字段的验证
                String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
                String[] fdsHide = StrUtil.split(fieldHide, ",");
                if (fdsHide!=null) {
                    for (int i=0; i<fdsHide.length; i++) {
                        Iterator<FormField> ir = fieldsForCheck.iterator();
                        while (ir.hasNext()) {
                            FormField ff = (FormField)ir.next();
                            if (fdsHide[i].equals(ff.getName())) {
                                ir.remove();
                            }
                        }
                    }
                }	*/
            } else {
                WorkflowActionDb wfa = new WorkflowActionDb();
                wfa = wfa.getWorkflowActionDb((int) actionId);
                String fieldWrite = StrUtil.getNullString(wfa.getFieldWrite()).trim();

                String[] fds = fieldWrite.split(",");
                int len = fds.length;

                MacroCtlMgr mm = new MacroCtlMgr();
                int nestLen = "nest.".length();
                // 将嵌套表中可写的域筛选出
                Iterator<FormField> ir = fieldsForCheck.iterator();
                while (ir.hasNext()) {
                    FormField ff = (FormField) ir.next();
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        // 跳过SQL控件，因为当SQL控件不可写时，仍需记录其值
                        if (mu.getIFormMacroCtl() instanceof ISQLCtl) {
                            continue;
                        }
                    }
                    boolean isFound = false;
                    for (String s : fds) {
                        // 如果不是嵌套表格2的可写表单域
                        if (!s.startsWith("nest.")) {
                            continue;
                        }
                        String fName = s.substring(nestLen);
                        if (ff.getName().equals(fName)) {
                            isFound = true;
                            break;
                        }
                    }
                    if (!isFound) {
                        ir.remove();
                    }
                }
            }

            // 对表单中的可写表单域（且不隐藏的）进行有效性验证，必填、大小范围的验证
            com.redmoon.oa.flow.FormDAOMgr.validateFields(request, fu, fieldsForCheck, fdao, false);

            // 20220130本想把getFieldsByForm从方法的头部移至此处，因为在flow.FormDAOMgr.validateFields中，嵌套表格2的有效性检查中时，
            // 如果存在记录，则会赋予该嵌套表格字段值cws，但是不成功，因为validateFields可能会通不过，故此处重新通过fu取嵌套表格2的值
            // fields = getFieldsByForm(application, request, fields);
            for (FormField ff : fields) {
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    if ("nest_sheet".equals(ff.getMacroType())) {
                        ff.setValue(fu.getFieldValue(ff.getName()));
                    }
                }
            }

            // 通过检查接口对fields中的值进行有效性判断
            if (ifv != null) {
                re = ifv.validateUpdate(request, fu, fdao, fields);
            }
            if (!re) {
                throw new ErrMsgException("表单验证非法！");
            }

            /*20190922 去掉验证，因为已被“验证规则”取代
            // 模块验证
            String validateProp = StrUtil.getNullStr(vsd.getString("validate_prop"));
            if (!"".equals(validateProp)) {
                String cond = ModuleUtil.parseValidate(request, fu, fdao, validateProp);
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByName("javascript");
                try {
                    Boolean ret = (Boolean) engine.eval(cond);
                    if (!ret.booleanValue()) {
                        String msg = vsd.getString("validate_msg");
                        if ("".equals(msg)) {
                            msg = LocalUtil.LoadString(request, "res.flow.Flow", "validError");
                        }
                        throw new ErrMsgException(StrUtil.getNullStr(msg));
                    }
                } catch (ScriptException ex) {
                    LogUtil.getLog(getClass()).error(ex);
                }
            }*/

            // 校验验证规则
            ModuleUtil.doCheckSetup(request, userName, fdao, fu);

            // 执行验证脚本
            String script = vsd.getScript("validate");
            if (script != null && !"".equals(script)) {
                Interpreter bsh = new Interpreter();
                try {
                    StringBuffer sb = new StringBuffer();

                    BeanShellUtil.setFieldsValue(fdao, sb);

                    // 赋值用户
                    sb.append("userName=\"" + privilege.getUser(request) + "\";");
                    bsh.eval(BeanShellUtil.escape(sb.toString()));

                    bsh.set("action", "save");
                    bsh.set("fdao", fdao);
                    bsh.set("request", request);
                    bsh.set("fileUpload", fu);

                    bsh.eval(script);
                    Object obj = bsh.get("ret");
                    if (obj != null) {
                        boolean ret = (Boolean) obj;
                        if (!ret) {
                            String errMsg = (String) bsh.get("errMsg");
                            if (errMsg != null) {
                                throw new ErrMsgException("提示：" + errMsg);
                            } else {
                                throw new ErrMsgException("验证非法，但errMsg为空！");
                            }
                        }
                    } else {
                        // throw new ErrMsgException("该节点脚本中未配置ret=...");
                    }
                } catch (EvalError e) {
                    LogUtil.getLog(getClass()).error(e);
                    throw new ErrMsgException(e.getMessage());
                }
            }
        }

        // boolean isMobile = WorkflowUtil.isMobile(request);
        // 置fdao中的辅助字段为原值，以免在事件中处理，再次被保存时值会发生变化（如：亮灯字段为只读状态时，传值至服务器为空，相当于disabled）
        for (FormField ff : fields) {
            if (ff.isHelper()) {
                ff.setValue(fdao.getFieldValue(ff.getName()));
            }
            // 手机端需置隐藏字段的值，以免在提交后，变为空，而PC端字段仅仅是隐藏了，提交后仍然可以获取到值
            // 因在PC端的chrome测试手机端时,isMobile为false，故注释掉，这样也可以使PC端与手机端隐藏字段的处理统一起来
            // if (isMobile && ff.getHide() != FormField.HIDE_NONE) {
            if (ff.getHide() != FormField.HIDE_NONE) {
                ff.setValue(fdao.getFieldValue(ff.getName()));
            }
        }

        fdao.setFields(fields);

        // 对函数型的表单域赋值
        for (FormField ff : fields) {
            if (ff.isFunc()) {
                ff.setValue(String.valueOf(FuncUtil.render(ff, ff.getDefaultValueRaw(), fdao)));
            }
        }

        re = fdao.save(request, fu);
        if (re) {
            if (ifv != null) {
                re = ifv.onUpdate(request, fdao);
            }

            // 如果需要记录历史
            if (fd.isLog()) {
                FormDAO.log(userName, FormDAOLog.LOG_TYPE_EDIT, fdao);
            }

            // 如果表单中含有“表单域选择宏控件”，且当前模块是该宏控件对应的表单的从模块，则自动关联
            if (true) {
                ModuleRelateDb mrd = new ModuleRelateDb();
                MacroCtlMgr mm = new MacroCtlMgr();
                for (FormField ff : fields) {
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        if (ff.getValue() == null || "".equals(ff.getValue())) {
                            continue;
                        }

                        // 如果不是long类型的，则说明字段中存储的不是ID，字段对应的不可能是主模块
                        long mainFormId = StrUtil.toLong(ff.getValue(), -1);
                        if (mainFormId == -1) {
                            continue;
                        }

                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu.isForm()) {
                            String mainFormCode = mu.getFormCode();
                            if ("".equals(mainFormCode)) {
                                mainFormCode = StrUtil.getNullStr(mu.getIFormMacroCtl().getFormCode(request, ff));
                            }
                            if ("".equals(mainFormCode)) {
                                continue;
                            }

                            boolean isRelateToMainForm = false;
                            // 检查该mainFormCode是否为当前表单的主表单
                            for (Object o : mrd.getModulesRelated(mainFormCode)) {
                                mrd = (ModuleRelateDb) o;
                                if (mrd.getString("relate_code").equals(formCode)) {
                                    isRelateToMainForm = true;
                                    break;
                                }
                            }
                            if (isRelateToMainForm) {
                                FormDAOMgr fdmMain = new FormDAOMgr(mainFormCode);
                                String relateId = fdmMain.getRelateFieldValue(mainFormId, mrd.getString("relate_code"));
                                if (relateId != null && !relateId.equals(fdao.getCwsId())) {
                                    fdao.setCwsId(relateId);
                                    fdao.save();
                                }
                            }
                        }
                    }
                }
            }

            // 发送消息
            sendRemindMsg(request, vsd, fdao, "save");

            String script = vsd.getScript("save");
            LogUtil.getLog(getClass()).info("update bsh formCode=" + formCode + " script=" + script);
            if (script != null) {
                BSHShell bs = new BSHShell();
                StringBuffer sb = new StringBuffer();
                BeanShellUtil.setFieldsValue(fdao, sb);
                // 赋值当前用户
                sb.append("userName=\"" + privilege.getUser(request) + "\";");
                bs.set(ConstUtil.SCENE, ConstUtil.SCENE_MODULE_SAVE);
                bs.set("fdaoOld", fdaoOld);
                bs.set("fdao", fdao);
                bs.set("request", request);
                bs.set("fileUpload", fu);

                bs.eval(BeanShellUtil.escape(sb.toString()));
                bs.eval(script);
            }
        }
        return re;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        return del(request, false, formCode);
    }

    public boolean del(HttpServletRequest request, boolean isNestSheet, String moduleCode) throws ErrMsgException {
        Privilege privilege = new Privilege();
        ModulePrivDb mpd = new ModulePrivDb(moduleCode);
        String userName = privilege.getUser(request);
        // 不检查，以免未配置权限时，嵌套表在流程中被删除时判断为权限非法，降低使用难度 2015-1-5
        // 如果是嵌套表，则根据配置是否需要检查权限来处理2015-1-16
        if (isNestSheet) {
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");
            if (isNestSheetCheckPrivilege) {
                if (!mpd.canUserDel(userName) && !mpd.canUserManage(userName)) {
                    throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
                }
            }
        } else {
            boolean isTreeView = ParamUtil.getBoolean(request, "isTreeView", false);
            if (isTreeView) {
                String nodeCode = ParamUtil.get(request, "treeNodeCode");
                ModuleTreePermission moduleTreePermission = SpringUtil.getBean(ModuleTreePermission.class);
                if (!moduleTreePermission.canSee(SpringUtil.getUserName(), nodeCode)) {
                    throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
                }
            }
            else if (!mpd.canUserDel(userName) && !mpd.canUserManage(userName)) {
                throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            }
        }

        String ids = ParamUtil.get(request, "ids");
        if ("".equals(ids)) {
            // 手机端删除时，传过来的是id
            ids = ParamUtil.get(request, "id");
        }
        String[] aryIds = StrUtil.split(ids, ",");
        if (aryIds == null) {
            throw new ErrMsgException("缺少标识！");
        }

        ModuleSetupDb moduleSetupDb = new ModuleSetupDb();
        moduleSetupDb = moduleSetupDb.getModuleSetupDb(moduleCode);
        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);

        // vsd用于取出验证脚本
        ModuleSetupDb vsd = new ModuleSetupDb();
        vsd = vsd.getModuleSetupDbOrInit(formCode);

        Config cfg = new Config();
        IModuleChecker imc = cfg.getIModuleChecker(formCode);

        boolean re = true;
        for (String aryId : aryIds) {
            visualObjId = StrUtil.toLong(aryId);

            // 检查数据权限
            if (!ModulePrivMgr.canAccessData(request, moduleSetupDb, visualObjId)) {
                throw new ErrMsgException(i18nUtil.get("info_access_data_fail"));
            }
            // 删除校验
            validateDel(request, visualObjId, vsd, imc, userName);
        }

        for (String aryId : aryIds) {
            visualObjId = StrUtil.toLong(aryId);
            re = del(request, vsd, imc, userName);
        }
        return re;
    }

    public boolean delRelate(HttpServletRequest request, ModuleSetupDb msdRelated) throws ErrMsgException {
        String moduleCodeRelated = msdRelated.getString("code");
        Privilege privilege = new Privilege();
        ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
        String userName = privilege.getUser(request);
        // 不检查，以免未配置权限时，嵌套表在流程中被删除时判断为权限非法，降低使用难度 2015-1-5
        // 如果是嵌套表，则根本配置是否需要检查权限来处理2015-1-16
        // isNestSheetCheckPrivilege已经不知道什么时候在config_oa.xml被删除了，所以下面的权限判断的代码已经无用了
        if (!mpd.canUserDel(userName) && !mpd.canUserManage(userName)) {
            throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
        }

        String ids = ParamUtil.get(request, "ids");
        String[] aryIds = StrUtil.split(ids, ",");
        if (aryIds == null) {
            throw new ErrMsgException("缺少标识！");
        }

        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);

        // vsd用于取出验证脚本
        ModuleSetupDb vsd = new ModuleSetupDb();
        vsd = vsd.getModuleSetupDbOrInit(msdRelated.getString("form_code"));

        Config cfg = new Config();
        IModuleChecker imc = cfg.getIModuleChecker(msdRelated.getString("form_code"));

        // 检查数据权限，判断用户是否可以存取此条数据
        String parentModuleCode = ParamUtil.get(request, "moduleCode");
        if ("".equals(parentModuleCode)) {
            throw new ErrMsgException("缺少父模块编码！");
        }
        ModuleSetupDb parentMsd = new ModuleSetupDb();
        parentMsd = parentMsd.getModuleSetupDb(parentModuleCode);
        if (parentMsd==null) {
            throw new ErrMsgException("父模块不存在！");
        }
        String parentFormCode = parentMsd.getString("form_code");

        String mode = ParamUtil.get(request, "mode");
        // 通过选项卡标签关联
        boolean isSubTagRelated = "subTagRelated".equals(mode);
        String relateFieldValue = "";
        long parentId = ParamUtil.getLong(request, "parentId", -1);
        if (parentId==-1) {
            throw new ErrMsgException("缺少父模块记录的ID！");
        }
        else {
            if (!isSubTagRelated) {
                com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
                relateFieldValue = fdm.getRelateFieldValue(parentId, moduleCodeRelated);
                if (relateFieldValue==null) {
                    // 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
                    relateFieldValue = SQLBuilder.IS_NOT_RELATED;
                }
            }
        }

        boolean re = true;
        for (String aryId : aryIds) {
            visualObjId = StrUtil.toLong(aryId);

            // 检查数据权限
            if (!ModulePrivMgr.canAccessDataRelated(request, msdRelated, relateFieldValue, visualObjId)) {
                throw new ErrMsgException(i18nUtil.get("info_access_data_fail"));
            }

            validateDel(request, visualObjId, vsd, imc, userName);
        }

        for (String aryId : aryIds) {
            visualObjId = StrUtil.toLong(aryId);
            re = del(request, vsd, imc, userName);
        }
        return re;
    }

    public boolean validateDel(HttpServletRequest request, long id, ModuleSetupDb vsd, IModuleChecker imc, String userName) throws ErrMsgException {
        boolean re = true;
        FormDAO fdao = getFormDAO(visualObjId);
        if (fdao==null || !fdao.isLoaded()) {
            throw new ErrMsgException("该记录已不存在！");
        }
        if (imc != null) {
            re = imc.validateDel(request, fdao);
        }
        if (!re) {
            throw new ErrMsgException("验证非法！");
        }

        // 执行验证脚本
        String script = vsd.getScript("validate");
        if (script != null && !"".equals(script)) {
            Interpreter bsh = new Interpreter();
            try {
                StringBuffer sb = new StringBuffer();

                BeanShellUtil.setFieldsValue(fdao, sb);

                // 赋值用户
                sb.append("userName=\"" + userName + "\";");
                bsh.eval(BeanShellUtil.escape(sb.toString()));

                bsh.set("action", "delete");
                bsh.set("fdao", fdao);
                bsh.set("request", request);
                bsh.set("fileUpload", fu);

                bsh.eval(script);
                Object obj = bsh.get("ret");
                if (obj != null) {
                    boolean ret = (Boolean) obj;
                    if (!ret) {
                        String errMsg = (String) bsh.get("errMsg");
                        if (errMsg != null) {
                            throw new ErrMsgException("提示：" + errMsg);
                        } else {
                            throw new ErrMsgException("验证非法，但errMsg为空！");
                        }
                    }
                } else {
                    // throw new ErrMsgException("该节点脚本中未配置ret=...");
                }
            } catch (EvalError e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        return re;
    }

    public boolean del(HttpServletRequest request, ModuleSetupDb vsd, IModuleChecker imc, String userName) throws ErrMsgException {
        FormDAO fdao = getFormDAO(visualObjId);
        if (fdao == null) {
            LogUtil.getLog(getClass()).error("记录: " + visualObjId + " 已不存在");
            return false;
        }
        boolean re = fdao.del();
        if (re) {
            // 如果需要记录历史
            if (fd.isLog()) {
                FormDAO.log(userName, FormDAOLog.LOG_TYPE_DEL, fdao);
            }

            if (imc != null) {
                re = imc.onDel(request, fdao);
            }

            // 发送消息
            sendRemindMsg(request, vsd, fdao, "del");

            String script = vsd.getScript("del");
            if (script != null) {
                Interpreter bsh = new Interpreter();
                try {
                    StringBuffer sb = new StringBuffer();
                    BeanShellUtil.setFieldsValue(fdao, sb);

                    // 赋值当前用户
                    sb.append("userName=\"" + userName + "\";");

                    bsh.set("fdao", fdao);
                    bsh.set("request", request);
                    bsh.set("fileUpload", fu);

                    bsh.eval(BeanShellUtil.escape(sb.toString()));

                    bsh.eval(script);
                    Object obj = bsh.get("ret");
                    if (obj != null) {
                        boolean ret = (Boolean) obj;
                        if (!ret) {
                            String errMsg = (String) bsh.get("errMsg");
                            LogUtil.getLog(getClass()).error(
                                    "del bsh errMsg=" + errMsg);
                        }
                    }
                } catch (EvalError e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }
        return re;
    }

    public boolean batchOperate(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);
//        ModulePrivDb mpd = new ModulePrivDb(formCode);
        // 需根据模块中配置的自定义按钮的权限判断
        // if (!mpd.canUserManage(userName)) {
        //    throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
        // }

        String ids = ParamUtil.get(request, "ids");
        String[] aryIds = StrUtil.split(ids, ",");
        if (aryIds == null) {
            throw new ErrMsgException("缺少标识！");
        }

//        ModuleSetupDb vsd = new ModuleSetupDb();
//        vsd = vsd.getModuleSetupDbOrInit(formCode);

        String batchField = ParamUtil.get(request, "batchField");
        String batchValue = ParamUtil.get(request, "batchValue");

        int len = aryIds.length;
        boolean re = true;
        for (String aryId : aryIds) {
            visualObjId = StrUtil.toInt(aryId);
            FormDAO fdao = getFormDAO(visualObjId);
            if (!fdao.isLoaded()) {
                throw new ErrMsgException("该项已不存在！");
            }

            fdao.setFieldValue(batchField, batchValue);
            re = fdao.save();
            if (re) {
                // 如果需要记录历史
                if (fd.isLog()) {
                    FormDAO.log(userName, FormDAOLog.LOG_TYPE_EDIT, fdao);
                }
            }
        }
        return re;
    }

    /**
     * 取得与子模块相关的父模块的字段的值 @task:暂时无用
     *
     * @param parentId  int
     * @param mainField String
     * @return String
     */
    public String getFieldValueOfMain(long parentId, String mainField) {
        com.redmoon.oa.visual.FormDAO fdaoParent = getFormDAO(parentId);
        return fdaoParent.getFieldValue(mainField);
    }

    /**
     * 根据描述字符串，获得映射字段的值
     *
     * @param fdao           FormDAO
     * @param otherFieldDesc String
     * @return String
     */
    public static String getFieldValueOfOther(HttpServletRequest request, FormDAO fdao, String otherFieldDesc) {
        String[] ary = otherFieldDesc.split(":");
        String fieldName = ary[1];
        String fieldValue;
        if ("cws_id".equals(fieldName)) {
            fieldValue = fdao.getCwsId();
        } else if ("id".equals(fieldName)) {
            fieldValue = String.valueOf(fdao.getId());
        } else {
            fieldValue = fdao.getFieldValue(fieldName);
        }

        // 如果没有多重映射，即只有单重映射，则允许显示多条记录，以逗号分隔
        if (ary.length == 5 && !"cws_log_id".equals(ary[3])) {
            String formCode = "";
            String showFieldName = "";
            for (int i = 2; i < ary.length; i += 3) {
                formCode = ary[i];
                fieldName = ary[i + 1];
                showFieldName = ary[i + 2];
            }
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);
            return getFieldValueOfOtherMulti(request, fd, fieldValue, fieldName, showFieldName);
        } else {
            String formCode = "";
            String showFieldName = "";
            for (int i = 2; i < ary.length; i += 3) {
                formCode = ary[i];
                fieldName = ary[i + 1];
                showFieldName = ary[i + 2];
                // cws_id=null:nr fieldName=cws_id fieldValue=null showFieldName=nr

                FormDAOMgr fdaom = new FormDAOMgr(formCode);
                fieldValue = fdaom.getFieldValueOfOther(fieldValue, fieldName, showFieldName);
            }

            LogUtil.getLog(FormDAOMgr.class).info("getFieldValueOfOther:formCode=" +
                    formCode + " fieldName=" + fieldName +
                    " ary.length=" + ary.length);
            if (!"id".equalsIgnoreCase(showFieldName)) {
                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCode);

                FormField ff = fd.getFormField(showFieldName);
                if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
                    String macroType = ff.getMacroType();
                    MacroCtlMgr mcm = new MacroCtlMgr();
                    MacroCtlUnit mcu = mcm.getMacroCtlUnit(macroType);
                    RequestUtil.setFormDAO(request, fdao);
                    fieldValue = mcu.getIFormMacroCtl().converToHtml(request, ff, fieldValue);
                }
            }
            return StrUtil.getNullStr(fieldValue);
        }
    }

    public static String getFieldValueOfOtherMulti(HttpServletRequest request, FormDb fd, String fieldValue, String fieldName, String showFieldName) {
        boolean isForExport = "true".equals(request.getAttribute(ConstUtil.IS_FOR_EXPORT));

        Object[] objs = new Object[1];
        boolean isGetIdsFromDb = true;

        ModuleSetupDb msd = (ModuleSetupDb)request.getAttribute(ModuleUtil.MODULE_SETUP);

        StringBuffer ids = new StringBuffer();
        long id = -1;
        try {
            FormField ff = null;
            if (!"id".equals(fieldName) && !"cws_id".equals(fieldName)) {
                ff = fd.getFormField(fieldName);
                if (ff == null) {
                    return "字段" + fieldName + "不存在！";
                }
            }

            // if (isForExport) {
                // 当导出时，如果有相同的键值对，如来源于活动的新增项目，多个项目可能对应同一个活动，如果导出时前一记录已取出活动名，则后一记录不用再读取数据库，以加快速度
                String strIds = (String) request.getAttribute(fd.getCode() + "_ids_" + fieldName + "_" + fieldValue);
                if (strIds != null) {
                    ids.append(strIds);
                    isGetIdsFromDb = false;
                }
            // }

            if (isGetIdsFromDb) {
                String orderBy = "asc";
                if (msd!=null) {
                    orderBy = msd.getInt("other_multi_order")==1 ? "asc" : "desc";
                }
                String sql = "select id from " + fd.getTableNameByForm() + " where " + fieldName + "=? order by id " + orderBy; // 20200506 由desc改为asc
                DebugUtil.i(FormDAOMgr.class, "getFieldValueOfOtherMulti", sql + " fieldName=" + fieldName);
                if ("id".equals(fieldName)) {
                    long myId = StrUtil.toLong(fieldValue, -65536);
                    if (myId == -65536) {
                        return null;
                    }
                    objs[0] = myId;
                } else if ("cws_id".equals(fieldName)) {
                    objs[0] = fieldValue;
                } else if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR ||
                        ff.getFieldType() == FormField.FIELD_TYPE_TEXT) {
                    objs[0] = fieldValue;
                } else if (ff.getFieldType() == FormField.FIELD_TYPE_DATE) {
                    objs[0] = DateUtil.parse(fieldValue, "yyyy-MM-dd");
                } else if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
                    objs[0] = DateUtil.parse(fieldValue, "yyyy-MM-dd HH:mm:ss");
                } else {
                    // sql = "select id from " + fd.getTableNameByForm() + " where " + fieldName + "=" + fieldValue;
                    objs[0] = fieldValue;
                }
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                while (ri.hasNext()) {
                    ResultRecord rr = ri.next();
                    id = rr.getLong(1);
                    StrUtil.concat(ids, ",", String.valueOf(id));
                }

                if (ids.length()>0) {
                    request.setAttribute(fd.getCode() + "_ids_" + fieldName + "_" + fieldValue, ids.toString());
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(FormDAOMgr.class).error(StrUtil.trace(e));
        }
        if (ids.length()==0) {
            return null;
        }

        // 分隔符
        String otherMultiWs = ",";
        if (msd!=null) {
            otherMultiWs = StrUtil.getNullStr(msd.getString("other_multi_ws"));
            if ("".equals(otherMultiWs)) {
                otherMultiWs = ",";
            }
        }

        StringBuffer sb = new StringBuffer();
        String[] ary = StrUtil.split(ids.toString(), ",");
        FormDAO fdao = new FormDAO();
        for (String strId : ary) {
            id = StrUtil.toLong(strId);

            // if (isForExport) {
            // 不通过getFormDAOByCache从redis获取，加快速度
                fdao = (FormDAO) request.getAttribute("FormDAO_" + id + "_" + fd.getCode());
                if (fdao == null) {
                    fdao = new FormDAO();
                    fdao = fdao.getFormDAOByCache(id, fd);
                    request.setAttribute("FormDAO_" + id + "_" + fd.getCode(), fdao);
                }
            // } else {
            //     fdao = fdao.getFormDAOByCache(id, fd);
            // }
            String val;
            if (!"id".equalsIgnoreCase(showFieldName)) {
                FormField ff = fdao.getFormField(showFieldName);
                if (ff == null) {
                    return "无";
                }
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    String macroType = ff.getMacroType();
                    MacroCtlMgr mcm = new MacroCtlMgr();
                    MacroCtlUnit mcu = mcm.getMacroCtlUnit(macroType);
                    // 取得request中原来的fdao
                    IFormDAO ifdao = RequestUtil.getFormDAO(request);
                    RequestUtil.setFormDAO(request, fdao);
                    val = mcu.getIFormMacroCtl().converToHtml(request, ff, ff.getValue());
                    if (ifdao != null) {
                        // 恢复request中原来的fdao，以免ModuleController中setFormDAO的值被修改为本方法中的fdao
                        RequestUtil.setFormDAO(request, ifdao);
                    }
                }
                else {
                    val = ff.convertToHtml();
                }
            }
            else {
                val = String.valueOf(fdao.getId());
            }
            StrUtil.concat(sb, otherMultiWs, val);
        }
        return sb.toString();
    }

    /**
     * 取得表中字段为指定值的其它字段的值，用于在模块列表中显示与其相关的其它表的字段的值
     *
     * @param fieldValue    String 指定字段的值
     * @param fieldName     String 指定字段的名称
     * @param showFieldName String 将要显示的字段的名称
     * @return String
     */
    public String getFieldValueOfOther(String fieldValue, String fieldName, String showFieldName) {
        LogUtil.getLog(getClass()).info(fieldName + "=" + fieldValue + ":" + showFieldName);

        Object[] objs = new Object[1];

        long id = -1;
        try {
            FormField ff = null;
            if (!"id".equals(fieldName) && !"cws_id".equals(fieldName) && !"cws_log_id".equals(fieldName)) {
                ff = fd.getFormField(fieldName);
                if (ff == null) {
                    LogUtil.getLog(getClass()).error("getFieldValueOfOther:表单：" + fd.getCode() + "中的字段" + fieldName + " 不存在！");
                    return "字段" + fieldName + "不存在！";
                }
            }

            String sql = "select id from " + fd.getTableNameByForm() +
                    " where " + fieldName + "=? order by id desc";
            if ("id".equals(fieldName) || "cws_log_id".equals(fieldName)) {
                long myId = StrUtil.toLong(fieldValue, -65536);
                if (myId == -65536) {
                    return null;
                }
                objs[0] = myId;
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                if (ri.hasNext()) {
                    ResultRecord rr = ri.next();
                    id = rr.getLong(1);
                }
            } else if ("cws_id".equals(fieldName)) {
                objs[0] = fieldValue;
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                if (ri.hasNext()) {
                    ResultRecord rr = ri.next();
                    id = rr.getLong(1);
                }
            } else if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR ||
                    ff.getFieldType() == FormField.FIELD_TYPE_TEXT) {
                objs[0] = fieldValue;
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                if (ri.hasNext()) {
                    ResultRecord rr = ri.next();
                    id = rr.getInt(1);
                }
            } else if (ff.getFieldType() == FormField.FIELD_TYPE_DATE) {
                objs[0] = DateUtil.parse(fieldValue, "yyyy-MM-dd");
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                if (ri.hasNext()) {
                    ResultRecord rr = ri.next();
                    id = rr.getInt(1);
                }
            } else if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
                objs[0] = DateUtil.parse(fieldValue, "yyyy-MM-dd HH:mm:ss");
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                if (ri.hasNext()) {
                    ResultRecord rr = ri.next();
                    id = rr.getInt(1);
                }
            } else {
                sql = "select id from " + fd.getTableNameByForm() + " where " +
                        fieldName + "=" + fieldValue;
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql);
                if (ri.hasNext()) {
                    ResultRecord rr = ri.next();
                    id = rr.getInt(1);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        if (id == -1) {
            return null;
        }

        visualObjId = id;

        String formCode = fd.getCode();
        boolean isLog = false;
        if (!fd.isLoaded()) {
            if (formCode.endsWith("_log")) {
                formCode = formCode.substring(0, formCode.length() - 4);
                fd = fd.getFormDb(formCode);
                isLog = true;
            }
        }

        FormDAOLog fdaoLog = new FormDAOLog(fd);
        IFormDAO fdao;
        if (!isLog) {
            FormDAO formDAO = new FormDAO();
            fdao = formDAO.getFormDAO(id, fd);
        } else {
            // 当搜索被删除的记录时，此时从ft_***_log中查找记录
            fdao = fdaoLog.getFormDAOLog(id);
        }

        return fdao.getFieldValue(showFieldName);
    }

    /**
     * 取得父模块中被关联字段的值
     *
     * @param parentId          int 父模块记录的ID
     * @param moduleCodeRelated String 关联模块的编码
     * @return String
     */
    public String getRelateFieldValue(long parentId, String moduleCodeRelated) {
        String relateFieldValue = null;

        com.redmoon.oa.visual.FormDAO fdaoParent = getFormDAO(parentId);
        ModuleRelateDb mrd = new ModuleRelateDb();
        for (Object o : mrd.getModulesRelated(formCode)) {
            mrd = (ModuleRelateDb) o;
            // 201807 fgf 改为了可以关联副模块
            if (mrd.getString("relate_code").equals(moduleCodeRelated)) {
                String field = mrd.getString("relate_field");
                if ("id".equals(field)) {
                    relateFieldValue = String.valueOf(fdaoParent.getId());
                } else if ("cws_id".equals(field)) {
                    relateFieldValue = fdaoParent.getCwsId();
                } else {
                    relateFieldValue = fdaoParent.getFieldValue(field);
                }
                break;
            }
        }

        return relateFieldValue;
    }

}
