package com.redmoon.oa.flow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.FormUtil;
import com.redmoon.oa.visual.FuncUtil;

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
public class Render {
    Document doc;
    WorkflowDb wf;
    FormDb fd;
    HttpServletRequest request;
    Leaf lf = new Leaf();
    
    private Vector fieldsOfView;

    public Vector getFieldsOfView() {
		return fieldsOfView;
	}

	public final static String FORM_FLEMENT_ID = "flowForm";

    public Render(HttpServletRequest request, WorkflowDb wf, Document doc) {
        this.request = request;
        this.doc = doc;
        this.wf = wf;

        request.setAttribute("WorkflowDb", wf);

        fd = new FormDb();
        lf = lf.getLeaf(wf.getTypeCode());
        fd = fd.getFormDb(lf.getFormCode());
    }
    
    public String getContentMacroReplaced(FormDAO fdao) {
        String content = doc.getContent(1);
        if (lf.isDebug())
        	content = fd.getContent();
        return getContentMacroReplaced(fdao, content);
    }

    /**
     * 替换宏控件
     * @param fdao 取自数据库
     * @return
     */
    public String getContentMacroReplaced(FormDAO fdao, String content) {
        Vector fieldsDb = fdao.getFields();

        // 替换content中的宏控件
        /*
        FormDAO fdao = new FormDAO(wf.getId(), fd);
        fdao.load();
        Iterator ir = fdao.getFields().iterator();
        */
        Iterator ir = fieldsDb.iterator();
        // 20130308，因为客户端可能会用不同版本浏览器管理后台，如果用原来保存的表单，则因为FormDb中ieVersion变了的关系，会导致解析失败
        // 20130720，恢复从doc中调用，当表单添加或编辑后，在content中加入头部<!--{ieVersion:6}-->
        // 如果是debug模式，则直接调用表单
        // System.out.println("getContentMacroReplaced:" + content);
        MacroCtlMgr mm = new MacroCtlMgr();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (!ff.getMacroType().equals(FormField.MACRO_NOT)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu == null) {
                    LogUtil.getLog(getClass()).error("getContentMacroReplaced: " + ff.getMacroType() + " is not found in xml config file.");
                }
                else {
                    LogUtil.getLog(getClass()).info("getContentMacroReplaced:" + mu.getName());
                    IFormMacroCtl ifc = mu.getIFormMacroCtl();

                    // 传入fdao，考虑到第三方数据宏控件会更改其它字段的数据
                    ifc.setFlowFormDAO(fdao);
                    // 预处理数据（第三方数据宏控件）
                    ifc.preProcessData(request, ff);
                }
            }
        }
        
        ir = fieldsDb.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (!ff.getMacroType().equals(FormField.MACRO_NOT)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu == null) {
                    LogUtil.getLog(getClass()).error("getContentMacroReplaced: " + ff.getMacroType() + " is not found in xml config file.");
                }
                else {
                    LogUtil.getLog(getClass()).info("getContentMacroReplaced:" + mu.getName());
                    IFormMacroCtl ifc = mu.getIFormMacroCtl();
                    // 传入fdao备用
                    ifc.setFlowFormDAO(fdao);                    
                    content = ifc.replaceMacroCtlWithHTMLCtl(request, ff, content);
                }
            }
        }
        
        return content;
    }

    /**
     * 解析表单，根据当前action用户可编辑的表单域，禁止其它表单域
     * @return String
     */
    public String rend(WorkflowActionDb wfa) throws ErrMsgException{
        return rend(wfa, false);
    }

    /**
     * 解析表单，根据当前action用户可编辑的表单域，禁止其它表单域
     * @param wfa WorkflowActionDb
     * @param isForFormEdit boolean 是否用于编辑表单内容（只能由流程管理员编辑）
     * @return String
     */
    public String rend(WorkflowActionDb wfa, boolean isForFormEdit) throws  ErrMsgException{
    	try {
			License.getInstance().checkSolution(fd.getCode());
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			return e.getMessage();
		}    	
    	
        String fieldWrite = StrUtil.getNullString(wfa.getFieldWrite()).trim();
        String fieldHide = StrUtil.getNullString(wfa.getFieldHide()).trim();
        boolean canWriteAll = false;

        if (isForFormEdit)
            canWriteAll = true;

        String[] fds = fieldWrite.split(",");
        int len = fds.length;

        /*
        * 2008.9.17 将下面的两行改为采用fdao，因为用fdm取fields，会重复至数据库中获取，涉及render(WorkflowActionDb wfa)、rend()及rendFree
        * FormDAOMgr fdm = new FormDAOMgr(wf.getId(), fd.getCode());
        * Vector v = fdm.getFields();
        */
        FormDAO fdao = new FormDAO(wf.getId(), fd);
        fdao.load();
        
        Vector v = fdao.getFields();
        
        // 替换宏控件，替换的过程中，有的宏控件（如第三方数据宏控件）可能会修改其它字段的值，因此需要传入fdao
        String content = "";    
        if (wfa.getFormView()!=WorkflowActionDb.VIEW_DEFAULT) {
        	FormViewDb fvd = new FormViewDb();
        	fvd = fvd.getFormViewDb(wfa.getFormView());
        	if (fvd == null){
        		String msg = "表单视图不存在";//LocalUtil.LoadString(request,"res.flow.Flow", "formVeiwNotExist");
        		throw new  ErrMsgException(msg);
        	}
        	content = fvd.getString("form");
        	
            String ieVersion = fvd.getString("ie_version");
        	FormParser fp = new FormParser();
        	Vector fields = fp.parseCtlFromView(content, ieVersion, fd);
        	
        	// 将fields改为已fdao中已取得值的FormField
        	Vector vt = new Vector();
        	Iterator ir = fields.iterator();
        	while (ir.hasNext()) {
        		FormField ff = (FormField)ir.next();
        		vt.addElement(fdao.getFormField(ff.getName()));
        	}
        	v = vt;
        }
        
        // 以便于在flow_dispose.jsp选项卡模式中将找到应显示于tab中的嵌套表
        fieldsOfView = v;
        
        // 将不可写的域筛选出        
        Iterator ir = v.iterator();
        Vector vdisable = new Vector();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();

            boolean finded = false;
            for (int i=0; i<len; i++) {
                if (ff.getName().equals(fds[i])) {
                    finded = true;
                    break;
                }
            }

            if (!finded) {
                vdisable.addElement(ff);
                // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
                ff.setEditable(false);                
            }
        }        
        
        // 将不显示的字段加入fieldHide
        ir = v.iterator();
        while (ir.hasNext()) {
        	FormField ff = (FormField)ir.next();
        	if (ff.getHide()==FormField.HIDE_EDIT || ff.getHide()==FormField.HIDE_ALWAYS) {
        		if ("".equals(fieldHide)) {
        			fieldHide = ff.getName();
        		}
        		else {
        			fieldHide += "," + ff.getName();
        		}
        	}
        }
        
        // System.out.println(getClass() + " fieldHide=" + fieldHide);
        String[] fdsHide = StrUtil.split(fieldHide, ",");
        if (fdsHide!=null) {
        	ir = v.iterator();
        	while (ir.hasNext()) {
        		FormField ffHide = (FormField)ir.next();
	        	for (String hideFieldName : fdsHide) {
	        		if (ffHide.getName().equals(hideFieldName)) {
	        			ffHide.setHidden(true);
	        			break;
	        		}
	        	}
        	}
        }
        
        if (wfa.getFormView()==WorkflowActionDb.VIEW_DEFAULT) {
        	content = getContentMacroReplaced(fdao);
        }
        else {
        	content = getContentMacroReplaced(fdao, content);
        }        

        content = replaceDefaultStr(wfa, content);
        // String formId = "flowForm";

        // 置用户已操作的值
        String str = "";

        MacroCtlMgr mm = new MacroCtlMgr();

        // 为了使控件中的值中有"号或者'号时，JS不出错，采用如下处理方式 20060909
        ir = v.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu != null) {
                    str += mu.getIFormMacroCtl().getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
                }
            }
            else {
                str += FormField.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
            }
        }

        // 显示或隐藏表单中的区域
        str += WorkflowUtil.doGetViewJS(request, wfa, fd, fdao, new Privilege().getUser(request), false);

        str += "\n<script>\n";

        ir = v.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();

            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu != null) {
                    str += mu.getIFormMacroCtl().getSetCtlValueScript(request, fdao, ff, FORM_FLEMENT_ID);
                }
            }
            else {
                str += FormField.getSetCtlValueScript(request, fdao, ff, FORM_FLEMENT_ID);
            }
        }
        // 根据用户不可写的表单域的type，name，插入相应的JavaScript，禁止控件
        if (!canWriteAll) {
            ir = vdisable.iterator();
            while (ir.hasNext()) { 
                FormField ff = (FormField) ir.next();
                LogUtil.getLog(getClass()).info("不可写字段" + ff.getName() + ":" + ff.getTitle());
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        str += mu.getIFormMacroCtl().getDisableCtlScript(ff, FORM_FLEMENT_ID);
                    }
                }
                else {
                    str += FormField.getDisableCtlScript(ff, FORM_FLEMENT_ID);
                }
            }
        }

        // 处理嵌套表的可写单元格
        if (fds!=null) {
            // 取出可写单元格
            for (int k = 0; k < fds.length; k++) {
                if (fds[k].startsWith("nest.")) {
                        str += "try{ setNestTableColWritable('" +
                                fds[k].substring("nest.".length()) +
                                "'); }catch(e) {}\n";
                }
            }
 
            /*
            ir = v.iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField)ir.next();
                if (ff.getType().equals(ff.TYPE_MACRO)) {
                    if (ff.getMacroType().equals("nest_table")) {
                        String nestFormCode = ff.getDefaultValueRaw();
                        FormDb nestForm = new FormDb();
                        nestForm = nestForm.getFormDb(nestFormCode);
                        Iterator ir2 = nestForm.getFields().iterator();
                        while (ir2.hasNext()) {
                            FormField ff2 = (FormField)ir2.next();
                            // 检查ff2是否在可写表单域中出现
                            for (int k=0; k<fds.length; k++) {
                                if (fds[k].startsWith("nest.")) {
                                    if (ff2.getName().equals(fds[k].substring(
                                            "nest.".length()))) {
                                        str += "setNestTableColWritable('" +
                                                fds[k].substring("nest.".length()) +
                                                "');\n";
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
            */
        }

        // 处理隐藏表单域
        if (fdsHide!=null) {
        	// 20161222 fgf 因为嵌套表格2是通过loadNestCtl，ajax方式获得的，所以需在nest_sheet_view.jsp中调用，以隐藏列
        	str += "function hideNestCol() {\n";
        	
            for (int i=0; i<fdsHide.length; i++) {
                if (!fdsHide[i].startsWith("nest.")) {
                    FormField ff = fd.getFormField(fdsHide[i]);
                    if (ff==null) {
                        LogUtil.getLog(getClass()).error(fd.getName() + " Field hidden " + fdsHide[i] + " is not exist.");
                    }
                    else {
                    	str += FormField.getHideCtlScript(ff, FORM_FLEMENT_ID);
                    }
                }
                else {
                    str += "try{ hideNestTableCol('" + fdsHide[i].substring("nest.".length()) +  "'); }catch(e) {}\n";
                    str += "try{ hideNestSheetCol('" + fdsHide[i].substring("nest.".length()) +  "'); }catch(e) {}\n";
                }
            }
        	str += "}\n";
        	
        	str += "hideNestCol();\n";
        	
            // 如果不是管理员，则屏蔽鼠标右键，便于调试
            // Privilege pvg = new Privilege();
            // if (!pvg.isUserPrivValid(request, "admin"))
            //    str += "$(document).bind('contextmenu', function(e){return false;});";
        }

        str += "</script>\n";

        // livevalidation检查
        str += FormUtil.doGetCheckJS(request, v);
        
        // 取得函数的参数中所关联的表单域当值发生改变时，重新获取值的脚本
        str += FuncUtil.doGetFieldsRelatedOnChangeJS(fd);

        // 唯一性检查
        str += FormUtil.doGetCheckJSUnique(fdao.getId(), v);
        
        // 一米OA签名
        if (License.getInstance().isFree()) {
        	content += License.getFormWatermark();
        }
        
        return content + str;
    }
 
    /**
     * 显示嵌套表单
     * @param request HttpServletRequest
     * @param formCode String 嵌套表单的编码
     * @param wfa WorkflowActionDb 当前处理动作
     * @return String
     */
    public String rendForNestCtl(HttpServletRequest request, String formCode, WorkflowActionDb wfa) {
        FormDb formDb = new FormDb(formCode);
        com.redmoon.oa.visual.FormDAO visualfdao = new com.redmoon.oa.visual.FormDAO(formDb);

        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(wfa.getFlowId());
        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());
        FormDb parentFd = new FormDb();
        parentFd = parentFd.getFormDb(lf.getFormCode());

        com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
        fdao = fdao.getFormDAO(wf.getId(), parentFd);
        long cwsId = fdao.getId();
        
        int visualObjId = visualfdao.getIDByCwsId((int)cwsId);

        visualfdao = visualfdao.getFormDAO(visualObjId, formDb);

        LogUtil.getLog(getClass()).info("rendForNestCtl visualObjId=" + visualObjId);

        com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, visualObjId, formDb);
        String content = rd.rend(FORM_FLEMENT_ID);
        // LogUtil.getLog(getClass()).info("rendForNestCtl content=" + content);

        // 检查本节点用户是否有填写嵌套表单的权限
        String fieldWrite = StrUtil.getNullString(wfa.getFieldWrite()).trim();
        boolean canWrite = true;


        MacroCtlMgr mm = new MacroCtlMgr();

        String[] fds = StrUtil.split(fieldWrite, ",");
        int len = 0;
        if (fds!=null)
            len = fds.length;
        if (len==0)
            canWrite = false;
        else {
            Iterator ir = parentFd.getFields().iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
                boolean finded = false;
                for (int i = 0; i < len; i++) {
                    // System.out.println(getClass() + " rendForNestCtl:ff.getName()=" + ff.getName() + " fds[i]=" + fds[i] + " ff.getTitle=" + ff.getTitle());
                    if (ff.getName().equals(fds[i])) {
                        finded = true;
                        break;
                    }
                }
                if (!finded) {
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu.getNestType() == MacroCtlUnit.NEST_TYPE_NORMAIL) {
                            if (ff.getDefaultValueRaw().equals(formCode)) {
                            	// 嵌套表单不可写
                                canWrite = false;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // System.out.println(getClass() + " rendForNestCtl:" + formCode + " canWrite=" + canWrite);

        Vector vdisable = visualfdao.getFields();
        content = replaceDefaultStr(wfa, content);

        // 置用户已操作的值
        String str = "";
        str += "\n<script>\n";
        LogUtil.getLog(getClass()).info("rendForNestCtl canWrite=" + canWrite);

        // 根据用户不可写的表单域的type，name，插入相应的JavaScript，禁止控件
        if (!canWrite) {
            Iterator ir = vdisable.iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        str += mu.getIFormMacroCtl().getDisableCtlScript(ff, FORM_FLEMENT_ID);
                    }
                }
                else {
                    str += FormField.getDisableCtlScript(ff, FORM_FLEMENT_ID);
                }
            }
        }
        str += "</script>\n";
        return content + str;
    }

    /**
     * 解析表单，根据当前action用户可编辑的表单域，禁止其它表单域
     * @return String
     */
    public String rendFree(WorkflowActionDb wfa) {
        // 根据用户所属的角色，取出其能写入的表单域
        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        WorkflowPredefineDb wfpd = new WorkflowPredefineDb();
        wfpd = wfpd.getPredefineFlowOfFree(wf.getTypeCode());

        String[] fds = wfpd.getFieldsWriteOfUser(wf, userName);
        int len = fds.length;

        boolean canWriteAll = false;
        if (fds.length==0)
            canWriteAll = false;
        
        // FormDAOMgr fdm = new FormDAOMgr(wf.getId(), fd.getCode());
        // Vector v = fdm.getFields();

        FormDAO fdao = new FormDAO(wf.getId(), fd);
        fdao.load();

        Vector v = fdao.getFields();
        Iterator ir = v.iterator();
        // 将不可写的域筛选出
        Vector vdisable = new Vector();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            boolean finded = false;
            for (int i=0; i<len; i++) {
                if (ff.getName().equals(fds[i])) {
                    finded = true;
                    break;
                }
            }
            if (!finded) {
                vdisable.addElement(ff);
                // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
                ff.setEditable(false);
            }
        }

        // 替换宏控件，替换的过程中，有的宏控件（如第三方数据宏控件）可能会修改其它字段的值，因此需要传入fdao        
        String content = getContentMacroReplaced(fdao);
        // 再获取一次
        v = fdao.getFields();

        content = replaceDefaultStr(wfa, content);
        // String formId = "flowForm";

        // 置用户已操作的值
        String str = "";

        MacroCtlMgr mm = new MacroCtlMgr();

        // 为了使控件中的值中有"号或者'号时，JS不出错，采用如下处理方式 20060909
        ir = fdao.getFields().iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu != null) {
                    str += mu.getIFormMacroCtl().getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request,
                            ff);
                }
            }
            else {
                str += FormField.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
            }
        }

        // LogUtil.getLog(getClass()).info("pageType:" + request.getAttribute("pageType"));

        str += "\n<script>\n";

        ir = fdao.getFields().iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu != null) {
                    str += mu.getIFormMacroCtl().getSetCtlValueScript(request, fdao, ff, FORM_FLEMENT_ID);
                }
            }
            else {
                str += FormField.getSetCtlValueScript(request, fdao, ff, FORM_FLEMENT_ID);
            }
        }
        // 根据用户不可写的表单域的type，name，插入相应的JavaScript，禁止控件
        if (!canWriteAll) {
            ir = vdisable.iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        str += mu.getIFormMacroCtl().getDisableCtlScript(ff, FORM_FLEMENT_ID);
                    }
                }
                else {
                    str += FormField.getDisableCtlScript(ff, FORM_FLEMENT_ID);
                }
            }
        }

        str += "</script>\n";

        str += FormUtil.doGetCheckJS(request, v);

        str += FormUtil.doGetCheckJSUnique(fdao.getId(), v);

        // 一米OA签名
        if (License.getInstance().isFree()) {
        	content += License.getFormWatermark();
        }
        
        return content + str;
    }

    public String replaceDefaultStr(String content) {
        content = content.replaceFirst("#\\[表单\\]", fd.getName());
        // content = content.replaceFirst("#\\[文号\\]", wf.getTitle());
        content = content.replaceFirst("#\\[标题\\]", wf.getTitle());
        content = content.replaceFirst("#\\[时间\\]", DateUtil.format(wf.getMydate(), FormField.FORMAT_DATE));
        // content = content.replaceFirst("#\\[title\\]", "");

        content = content.replaceFirst("#\\[id\\]", String.valueOf(wf.getId()));

        String pat = "\\{\\$rootPath\\}";
        content = content.replaceAll(pat, request.getContextPath());

        content = replaceResStr(content);

        return content;
    }

    public String replaceDefaultStr(WorkflowActionDb wfa, String content) {
        content = content.replaceFirst("#\\[title\\]", wfa.getTitle());
        content = replaceDefaultStr(content);
        return content;
    }

    /**
     * 替换国际化字符串
     * @param content String
     * @return String
     */
    public String replaceResStr(String content) {
        // {$res.form.code}
        String patternStr = "\\{\\$res\\.(.*?)\\}";
        Pattern pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean result = matcher.find();
        while (result) {
            String key = matcher.group(1);
            int p = key.lastIndexOf(".");
            String key1 = "res." + key.substring(0, p);
            String key2 = key.substring(p+1);
            String str = SkinUtil.LoadString(request, key1, key2);
            if (str==null)
                str = key1 + "." + key2;
            matcher.appendReplacement(sb, str);
            result = matcher.find();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 置表单中的各个输入框的值，显示的是原始的表单，本用于visual模块的显示，后已移植至visual包的Render.java中，
     * @return String
     */
    public String rend() {
        FormDAO fdao = new FormDAO(wf.getId(), fd);
        fdao.load();

        String content = getContentMacroReplaced(fdao);

        // String formId = "flowForm";
        content = replaceDefaultStr(content);
        // 置用户已操作的值
        String str = "";
        str += "\n<script>\n";

        // FormDAOMgr fdm = new FormDAOMgr(wf.getId(), fd.getCode());
        // Iterator ir = fdm.getFields().iterator();

        Iterator ir = fdao.getFields().iterator();

        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                java.util.Date dt = null;
                try {
                	if (ff.getDefaultValueRaw().equals(FormField.DATE_CURRENT)) {
                        dt = new java.util.Date();
                	} else {
                		dt = DateUtil.parse(ff.getDefaultValue(), 
                				FormField.FORMAT_DATE_TIME);
                	}
                }
                catch (Exception e) {
                	LogUtil.getLog(getClass()).error("Render.java rendForAdd:" + e.getMessage());
                }
                String d = DateUtil.format(dt, FormField.FORMAT_DATE);
                String t = DateUtil.format(dt, "HH:mm:ss");
//                str += "setCtlValue('" + ff.getName() + "', '" + ff.getType() +
//                        "', '" +
//                        d + "');\n";
//                str += "setCtlValue('" + ff.getName() + "_time" + "', '" + ff.getType() +
//                        "', '" +
//                        t + "');\n";
				str += "if (o('" + ff.getName()
						+ "_time')!= null){setCtlValue('" + ff.getName()
						+ "', '" + ff.getType() + "', '" + d + "');\n"
						+ "setCtlValue('" + ff.getName() + "_time" + "', '"
						+ ff.getType() + "', '" + t
						+ "');\n}else{setCtlValue('" + ff.getName() + "', '"
						+ ff.getType() + "', '"
						+ DateUtil.format(dt, FormField.FORMAT_DATE_TIME)
						+ "');\n}";
            }
            else {
                str += FormField.getSetCtlValueScript(request, fdao, ff, FORM_FLEMENT_ID);
            }
        }
        str += "</script>\n";

        return content + str;
    }

    /**
     * 为表单存档生成报表
     * @return String
     */
    public String reportForArchive(WorkflowDb wf, FormDb fd) {
        FormDAO fdao = new FormDAO(wf.getId(), fd);
        fdao.load();
        Vector vf = fdao.getFields();
        Iterator fir = vf.iterator();
        ArrayList<String> list = new ArrayList<String>();
        while (fir.hasNext()) {
            FormField ff = (FormField)fir.next();
            // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
            ff.setEditable(false);
            
            if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
            	list.add(ff.getName());
            }
        }

        String content = fd.getContent();
        content = FormParser.replaceTextfieldWithValue(request, content, fdao, fd);
        content = FormParser.replaceTextAreaWithValue(request, content, fdao, fd);
        content = FormParser.replaceSelectWithValue(request, content, fdao, fd);

        content = replaceDefaultStr(content);

        // 清除其它辅助图片按钮等
        String pat = "<img([^>]*?)calendar.gif([^>]*?)>";
        Pattern pattern = Pattern.compile(pat,
                                          Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        content = matcher.replaceAll("");

        pat = "<img([^>]*?)clock.gif([^>]*?)>";
        pattern = Pattern.compile(pat,
                                          Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("");
        //替换存档表单中的日期控件时间输入框 2015-01-29
        pat = "<input([^>]*?)value=12:30:30 name=([^>]*?)_time>";
        pattern = Pattern.compile(pat,
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("");
        
        for (String code : list) {
        	pat = "<input([^>]*?)" + code + "_time([^>]*?)>";
            pattern = Pattern.compile(pat,
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(content);
            content = matcher.replaceAll("");
        }
        
        // 按钮在replaceTextfieldWithValue中已被清除了


        return content;
    }
    
    public String report() {
    	return report(true);
    }

    /**
     * 报表模式显示
     * @return String
     */
    public String report(boolean isHideField) {
        FormDAO fdao = new FormDAO(wf.getId(), fd);
        fdao.load();
        
        Vector vf = fdao.getFields();
        Iterator fir = vf.iterator();
        while (fir.hasNext()) {
            FormField ff = (FormField)fir.next();
            // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
            ff.setEditable(false);
        }

        String content = getContentMacroReplaced(fdao);

        String typeCode = wf.getTypeCode();
        Leaf lf = new Leaf();
        lf = lf.getLeaf(typeCode);
        String formCode = lf.getFormCode();

        // String formId = "flowForm";
        content = replaceDefaultStr(content);
        content = "<div id=formDiv name=formDiv>" + content + "</div>";

        // 置用户已操作的值
        String str = "";

        // FormDAOMgr fdm = new FormDAOMgr(wf.getId(), formCode);
        Vector v = fdao.getFields();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            
        	String val = FuncUtil.renderFieldValue(fdao, ff);
        	ff.setValue(val);   
        	
            /*
            if (ff.getType().equals(ff.TYPE_CHECKBOX)) {
                str += FormField.getOuterHTMLOfElementWithRAWValue(request, ff);
            }
            else
            */
                str += FormField.getOuterHTMLOfElementWithHTMLValue(request, ff);
        }

        MacroCtlMgr mm = new MacroCtlMgr();

        str += "\n<script>\n";
        ir = fdao.getFields().iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu != null) {
                    str += mu.getIFormMacroCtl().getReplaceCtlWithValueScript(fdao, ff);
                }
            }
            else {
                /*
                if (ff.getType().equals(ff.TYPE_CHECKBOX)) {
                    str += FormField.getSetCtlValueScript(request, fdao, ff, FORM_FLEMENT_ID);
                }
                else
                */
          	
                str += FormField.getReplaceCtlWithValueScript(ff);
            }
        }
        
        if (isHideField) {
	        Privilege pvg = new Privilege();
	        MyActionDb mad = new MyActionDb();
	        mad = mad.getMyActionDbOfFlow(wf.getId(), pvg.getUser(request));
	        // 管理员查看时，其本人可能并未参与流程，则mad将为null
	        if (mad!=null) {
	            WorkflowActionDb wad = new WorkflowActionDb();
	            wad = wad.getWorkflowActionDb((int) mad.getActionId());
	
	            String fieldHide = StrUtil.getNullString(wad.getFieldHide()).trim();
	            
	            // 将不显示的字段加入fieldHide	            
	            ir = v.iterator();
	            while (ir.hasNext()) {
	            	FormField ff = (FormField)ir.next();
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
	            if (fdsHide != null) {
                	// 20170731 fgf 增加hideNestCol
	            	str += "function hideNestCol() {\n";
	            	
	                for (int i=0; i<fdsHide.length; i++) {
	                    if (!fdsHide[i].startsWith("nest.")) {
	                        FormField ff = fd.getFormField(fdsHide[i]);
	                        if (ff==null) {
	                            LogUtil.getLog(getClass()).error(fd.getName() + " Field hidden " + fdsHide[i] + " is not exist.");
	                        }
	                        else {
	                        	str += FormField.getHideCtlScript(ff, FORM_FLEMENT_ID);
	                        }
	                    }
	                    else {
	                        str += "try{ hideNestTableCol('" + fdsHide[i].substring("nest.".length()) +  "'); }catch(e) {}\n";
	                        str += "try{ hideNestSheetCol('" + fdsHide[i].substring("nest.".length()) +  "'); }catch(e) {}\n";
	                    }
	                }
	            	str += "}\n";
	            	
	            	str += "hideNestCol();\n";	            	
	            	
	                // 屏蔽鼠标右键
	                // if (!pvg.isUserPrivValid(request, "admin"))
	                //    str += "$(document).bind('contextmenu', function(e){return false;});";
	            }
	        }
        }

        // 清除其它辅助图片按钮等
        str += "ClearAccessory();\n";
        str += "</script>\n";
        
        str += WorkflowUtil.doGetViewJS(request, null, fd, fdao, new Privilege().getUser(request), true);

        // 一米OA签名
        if (License.getInstance().isFree()) {
        	content += License.getFormWatermark();
        }
        
        return content + str;
    }
}
