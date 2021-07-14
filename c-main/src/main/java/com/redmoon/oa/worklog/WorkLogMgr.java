package com.redmoon.oa.worklog;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jdom.Element;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.worklog.domain.WorkLog;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class WorkLogMgr {
	
    Logger logger = Logger.getLogger(WorkLogMgr.class.getName());

    private FileUpload fileUpload;
    private ServletContext application;
    
    public WorkLogMgr() {
    	System.out.println();
    }

    public FileUpload doUpload(ServletContext application,
            HttpServletRequest request) throws
		ErrMsgException {
		fileUpload = new FileUpload();
		fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
		//String[] extnames = {"jpg", "gif", "png"};
		// fileUpload.setValidExtname(extnames);//设置可上传的文件类型
		
		int ret = 0;
		try {
		// fileUpload.setDebug(true);
		ret = fileUpload.doUpload(application, request);
		if (ret != FileUpload.RET_SUCCESS) {
		throw new ErrMsgException("ret=" + ret + " " +
		                       fileUpload.getErrMessage());
		}
		} catch (IOException e) { 
		LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
		}
		return fileUpload;
	}
    
    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request))
            throw new ErrMsgException("请先登录！");
        
        doUpload(application,request);
        
        boolean re = true;
        String errmsg = "";
        String content = "";
        int id = ParamUtil.getInt(request, "id");
        String itemType = "";
        //String content = ParamUtil.get(request, "content");

        String[] ary = fileUpload.getFieldValues("content");
        String[] titleAry = fileUpload.getFieldValues("title");
        String[] canNullAry = fileUpload.getFieldValues("canNull");
        String[] wordCountAry = fileUpload.getFieldValues("wordCount");
        if (ary!=null) {
        	for (int i=0; i<ary.length; i++) {
        		content += ary[i];
        		String title = titleAry[i];
        		String itemContent = ary[i];
        		String canNull = canNullAry[i];
        		String wordCount = wordCountAry[i];
        		if((i+1) == ary.length){
        			if(itemContent.equals("")){
        				itemContent = "NULL";
        			}
        			itemType += title+":#"+itemContent+":#"+canNull+":#"+wordCount;
        		}else{
        			if(itemContent.equals("")){
        				itemContent = "NULL";
        			}
        			itemType += title+":#"+itemContent+":#"+canNull+":#"+wordCount+"a#a";
        		}
        	}
        }
        
        if (content.equals(""))
            errmsg += "内容不能为空！\\n";

        if (!errmsg.equals(""))
            throw new ErrMsgException(errmsg);

        WorkLogDb wld = getWorkLogDb(request, id);
        wld.setContent(content);
        wld.setItemType(itemType);
        re = wld.save(fileUpload);
        return re;
    }

    public WorkLogDb getWorkLogDb(HttpServletRequest request, int id) throws
            ErrMsgException {
        WorkLogDb wld = new WorkLogDb();
        wld = wld.getWorkLogDb(id);
        Privilege pvg = new Privilege();
        if (!wld.getUserName().equals(pvg.getUser(request)))
            if (!pvg.canAdminUser(request, wld.getUserName()))
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        "pvg_invalid"));
        return wld;
    }

    public synchronized boolean create(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request))
            throw new ErrMsgException("请先登录！");
        
        doUpload(application,request);
        
        boolean re = true;

        String errmsg = "";
        String content = "";
        String[] ary = fileUpload.getFieldValues("content");
        Config config = Config.getInstance();
        String itemType = "";
        
        List list = config.getRoot().getChild("items").getChildren();
        
        if(list != null){
        	Iterator ir = list.iterator();
        	int i=0;
        	while(ir.hasNext()){
        		Element element = (Element) ir.next();
        		String title = element.getChildText("title");
        		boolean canNull = element.getChildText("canNull").equals("true");
  			  	int wordCount = StrUtil.toInt(element.getChildText("wordCount"), -1);
        		String itemContent = ary[i];
        		content += ary[i];
        		if(ir.hasNext()){
        			if(itemContent.equals("")){
        				itemContent = "NULL";
        			}
        			itemType += title+":#"+itemContent+":#"+canNull+":#"+wordCount+"a#a";
        		}else{
        			if(itemContent.equals("")){
        				itemContent = "NULL";
        			}
        			itemType += title+":#"+itemContent+":#"+canNull+":#"+wordCount;
        		}
        		i++;
        	}
        }
        
        //String[] ary = ParamUtil.getParameters(request, "content");
        /*if (ary!=null) {
        	for (int i=0; i<ary.length; i++) {
        		content += ary[i];
        	}
        }*/
        
        String name = privilege.getUser(request);

        if (content.equals(""))
            errmsg += "内容不能为空！\\n";

        if (!errmsg.equals(""))
            throw new ErrMsgException(errmsg);
        
        String dateStr = fileUpload.getFieldValue("myDate");
        java.util.Date myDate = DateUtil.parse(dateStr, "yyyy-MM-dd");
        if (myDate==null)
        	myDate = new java.util.Date();
        String logTypeStr = StrUtil.getNullStr(fileUpload.getFieldValue("logType"));
        int logType = logTypeStr.equals("")?WorkLogDb.TYPE_NORMAL:Integer.parseInt(logTypeStr);
        String logItemStr = StrUtil.getNullStr(fileUpload.getFieldValue("logItem"));
        int logItem = logItemStr.equals("")?0:Integer.parseInt(logItemStr);
        String logYearStr = StrUtil.getNullStr(fileUpload.getFieldValue("logYear"));
        int logYear = logYearStr.equals("")?0:Integer.parseInt(logYearStr);
//        int logType = ParamUtil.getInt(request, "logType", WorkLogDb.TYPE_NORMAL);
//        int logItem = ParamUtil.getInt(request, "logItem", 0);
//        int logYear = ParamUtil.getInt(request, "logYear", 0);

        WorkLogDb wld = new WorkLogDb();
        if (logType==WorkLogDb.TYPE_NORMAL) {
	        if (wld.isWorkLogWritten(name, myDate))
	            throw new ErrMsgException(dateStr + "的记录已存在，请不要重复提交！");
        }
        
        // 检查是否在可补写几个工作日内的日记
        if (myDate!=null && logType==WorkLogDb.TYPE_NORMAL) {
	        Config cfg = Config.getInstance();
	        int dayLimit = cfg.getIntProperty("dayLimit");
	        if (dayLimit>0) {
	        	OACalendarDb oacdb = new OACalendarDb();
	        	int c = oacdb.getWorkDayCount(myDate, new java.util.Date());
	        	if (c>dayLimit) {
	        		throw new ErrMsgException("您仅能补写" + dayLimit + "天内的日报");
	        	}
	        }
        }

        wld.setContent(content);
        wld.setUserName(name);
        wld.setMyDate(myDate);
        wld.setLogType(logType);
        wld.setLogItem(logItem);
        wld.setLogYear(logYear);
        wld.setItemType(itemType);
        re = wld.create(fileUpload);
        return re;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        WorkLogDb wld = getWorkLogDb(request, id);
        if (wld == null || !wld.isLoaded())
            throw new ErrMsgException("该项已不存在！");

        return wld.del();
    }
    /**
     * 删除上传附件
     * @param request
     * @return
     * @throws ErrMsgException 
     */
    public boolean delAttachment(HttpServletRequest request) throws ErrMsgException{
    	int id = ParamUtil.getInt(request, "id");
    	int attachId = ParamUtil.getInt(request, "attachId");
    	WorkLogDb workLogDb = getWorkLogDb(request, id);
    	return workLogDb.delAttachment(attachId);
    }

    public boolean saveAppraise(HttpServletRequest request) throws
            ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request))
            throw new ErrMsgException("请先登录！");
        boolean re = true;
        String errmsg = "";

        int id = ParamUtil.getInt(request, "id");
        String appraise = ParamUtil.get(request, "appraise");

        if (appraise.equals(""))
            errmsg += "内容不能为空！\\n";

        if (!errmsg.equals(""))
            throw new ErrMsgException(errmsg);

        WorkLogDb wld = getWorkLogDb(request, id);
        UserMgr um = new UserMgr();
        appraise = um.getUserDb(privilege.getUser(request)).getRealName()+ "&nbsp;&nbsp;&nbsp;&nbsp;" +
                   cn.
                   js.fan.util.DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss") +
                   "<br />" + appraise;
        appraise = appraise + "<br />" + wld.getAppraise();
        wld.setAppraise(appraise);
        re = wld.save();
        return re;
    }
	
  
}
