package com.redmoon.oa.flow.macroctl;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import cn.js.fan.db.ResultIterator;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ParamUtil;
import com.redmoon.oa.workplan.WorkPlanTaskDb;

/**
 * <p>Title: 汇报流程使用</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkPlanTaskCtl extends AbstractMacroCtl {
    public WorkPlanTaskCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        long taskId = ParamUtil.getLong(request, "taskId", -1);
        if (taskId==-1) {
            taskId = StrUtil.toLong(ff.getValue(), -1);
            if (taskId==-1)
                return "";
        }
        WorkPlanTaskDb wptd = new WorkPlanTaskDb();
        wptd = (WorkPlanTaskDb)wptd.getQObjectDb(taskId);
        if (wptd==null) {
            return "未找到任务项";
        }
        String str = "<a href='javascript:;' onclick=\"addTab('计划项', 'workplan/workplan_task.jsp?id=" + wptd.getLong("work_plan_id") + "')\">" + wptd.get("name") + "</a>";
        str += "<input name='" + ff.getName() + "' value='" + taskId + "' type='hidden' />";
        return str;
    }

    public String getDisableCtlScript(FormField ff, String formElementId) {
        return "o('" + ff.getName() + "').style.display='none';";
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "', '');\n";
     }

    /**
     * 必须重载此方法，否则setCtlValue('task', 'macro', flowForm.cws_textarea_task.value)会将控件值置为空，当为必填项时通不过
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     */
    @Override
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request, FormField ff) {
        FormField ffNew = new FormField();
        ffNew.setName(ff.getName());
        ffNew.setValue(ff.getValue());
        ffNew.setType(ff.getType());
        ffNew.setFieldType(ff.getFieldType());
        ffNew.setValue("");

        long taskId = ParamUtil.getLong(request, "taskId", -1);
        if (taskId!=-1) {
            ffNew.setValue("" + taskId);
        }
        else {
            taskId = StrUtil.toLong(ff.getValue(), -1);
            if (taskId!=-1)
                ffNew.setValue("" + taskId);
        }

        // System.out.println(getClass() + " getOuterHTMLOfElementsWithRAWValueAndHTMLValue ffNew=" + ffNew.getValue());
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request,
                ffNew);
    }

	@Override
	public String getControlOptions(String userName, FormField ff) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getControlText(String userName, FormField ff) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getControlType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getControlValue(String userName, FormField ff) {
		// TODO Auto-generated method stub
		return null;
	}
}
