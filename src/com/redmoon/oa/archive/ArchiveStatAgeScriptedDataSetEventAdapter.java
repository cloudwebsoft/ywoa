package com.redmoon.oa.archive;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import cn.js.fan.util.ParamUtil;

public class ArchiveStatAgeScriptedDataSetEventAdapter extends
		ScriptedDataSetEventAdapter {
	private int num = 0;
	private int total = 0;
	private ArchiveStatAge[] dataList;
	private String depts = "";

	public void beforeOpen(IDataSetInstance dataSet,
			IReportContext reportContext) throws ScriptException {
		HttpServletRequest request = (HttpServletRequest)reportContext.getHttpServletRequest();
		
		// depts = ParamUtil.get(request, "depts");
		
		depts = (String)reportContext.getParameterValue("depts");
		
		// System.out.println(getClass() + " depts=" + depts);
	}
	
	public void open(IDataSetInstance dataSet) throws ScriptException {
		// TODO Auto-generated method stub
		super.open(dataSet);
		
		ArchiveStatMgr asm = new ArchiveStatMgr();
		dataList = asm.createStatAgeList(depts);
		this.total = dataList.length;
	}

	public boolean fetch(IDataSetInstance dataSet, IUpdatableDataSetRow row) {
		// TODO Auto-generated method stub
		try {
			if (num >= total) {
				return false;
			}
			ArchiveStatAge asa = (ArchiveStatAge) dataList[num];
			row.setColumnValue(1, asa.getAge());
			row.setColumnValue(2, asa.getCount());
			num++;
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
		// return super.fetch(dataSet, row);
	}

}
