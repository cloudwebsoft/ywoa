package com.redmoon.oa.flow;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.api.IFlowRender;
import com.cloudweb.oa.utils.SpringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

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

    public Render(HttpServletRequest request, FormDb fd) {
        this.request = request;
        this.fd = fd;
    }

    /**
     * 解析表单，根据当前action用户可编辑的表单域，禁止其它表单域
     * @return String
     */
    public String rend(WorkflowActionDb wfa) throws ErrMsgException{
        return rend(wfa, false);
    }

    public static List<FormField>[] getDisabledAndHidedFieldOfNestTable(WorkflowActionDb wfa, Vector<FormField> fields) {
        List<FormField>[] arr = new ArrayList[2];
        String fieldWrite = StrUtil.getNullString(wfa.getFieldWrite()).trim();
        String fieldHide = StrUtil.getNullString(wfa.getFieldHide()).trim();

        List<FormField> disableList = new ArrayList<>();

        String[] fds = StrUtil.split(fieldWrite, ",");
        if (fds!=null) {
            // 将不可写的域筛选出
            for (FormField ff : fields) {
                boolean finded = false;
                for (String s : fds) {
                    if (s.startsWith("nest.")) {
                        String nestName = s.substring(5);
                        if (ff.getName().equals(nestName)) {
                            finded = true;
                            break;
                        }
                    }
                }

                if (!finded) {
                    disableList.add(ff);
                }
            }
            arr[0] = disableList;
        }
        else {
            List<FormField> list = Collections.list(fields.elements());
            arr[0] = list;
        }

        // 将不显示的字段加入fieldHide
        for (FormField ff : fields) {
            if (ff.getHide() == FormField.HIDE_EDIT || ff.getHide() == FormField.HIDE_ALWAYS) {
                if ("".equals(fieldHide)) {
                    fieldHide = ff.getName();
                } else {
                    fieldHide += "," + ff.getName();
                }
            }
        }

        String[] arrHide = StrUtil.split(fieldHide, ",");
        List<FormField> hideList = new ArrayList<>();
        if (arrHide!=null) {
            for (FormField ff : fields) {
                for (String s : arrHide) {
                    if (s.startsWith("nest.")) {
                        String nestName = s.substring(5);
                        if (ff.getName().equals(nestName)) {
                            hideList.add(ff);
                            break;
                        }
                    }
                }
            }
        }
        arr[1] = hideList;
        return arr;
    }

    /**
     * 解析表单，根据当前action用户可编辑的表单域，禁止其它表单域
     * @param wfa WorkflowActionDb
     * @param isForFormEdit boolean 是否用于编辑表单内容（只能由流程管理员编辑）
     * @return String
     */
    public String rend(WorkflowActionDb wfa, boolean isForFormEdit) throws  ErrMsgException{
        IFlowRender flowRender = (IFlowRender)SpringUtil.getBean("flowRender");
        return flowRender.rend(wf, fd, wfa, isForFormEdit);
    }
 
    /**
     * 显示嵌套表单
     * @param request HttpServletRequest
     * @param formCode String 嵌套表单的编码
     * @param wfa WorkflowActionDb 当前处理动作
     * @return String
     */
    public String rendForNestCtl(HttpServletRequest request, String formCode, WorkflowActionDb wfa) {
        IFlowRender flowRender = (IFlowRender)SpringUtil.getBean("flowRender");
        return flowRender.rendForNestCtl(request, formCode, wfa);
    }

    /**
     * 解析表单，根据当前action用户可编辑的表单域，禁止其它表单域
     * @return String
     */
    public String rendFree(WorkflowActionDb wfa) {
        IFlowRender flowRender = (IFlowRender)SpringUtil.getBean("flowRender");
        return flowRender.rendFree(wf, fd, wfa);
    }

    /**
     * 为表单存档生成报表
     * @return String
     */
    public String reportForArchive(WorkflowDb wf, FormDb fd) {
        FormDAO fdao = new FormDAO();
        fdao = fdao.getFormDAO(wf.getId(), fd);
        IFlowRender flowRender = (IFlowRender)SpringUtil.getBean("flowRender");
        return flowRender.reportForArchive(wf, fd, fdao);
    }
    
    public String report() {
    	return report(true);
    }

    /**
     * 报表模式显示
     * @return String
     */
    public String report(boolean isHideField) {
        IFlowRender flowRender = (IFlowRender)SpringUtil.getBean("flowRender");
        return flowRender.report(wf, fd, isHideField);
    }

    /**
     * 视图报表模式显示
     * @return String
     */
    public String reportForView(int formViewId, boolean isHideField) {
        IFlowRender flowRender = (IFlowRender)SpringUtil.getBean("flowRender");
        return flowRender.reportForView(wf, fd, formViewId, isHideField);
    }
}
