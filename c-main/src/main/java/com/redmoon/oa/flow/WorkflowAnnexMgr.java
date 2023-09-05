package com.redmoon.oa.flow;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
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
public class WorkflowAnnexMgr {
    public WorkflowAnnexMgr() {
    }

    public boolean create(ServletContext application,
                          HttpServletRequest request) throws
            ErrMsgException {
        String contentType = request.getContentType();
        if (contentType.indexOf("multipart/form-data") == -1) {
            throw new IllegalStateException(
                    "The content type of request is not multipart/form-data");
        }

        FileUpload fu = new FileUpload();
        Config cfg = new Config();
        String exts = cfg.get("flowFileExt");
        String[] extAry = StrUtil.split(exts, ",");
        fu.setValidExtname(extAry);
        fu.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K

        int ret = -1;
        try {
            ret = fu.doUpload(application, request);
        } catch (IOException e) {
            throw new ErrMsgException(e.getMessage());
        }
        if (ret!=FileUpload.RET_SUCCESS) {
            throw new ErrMsgException(fu.getErrMessage(request));
        }

        boolean re = false;

        WorkflowAnnexDb wad = new WorkflowAnnexDb();
        String formCode = "flow_annex_create";
        ParamConfig pc = new ParamConfig(wad.getTable().
                                         getFormValidatorFile());
      
        ParamChecker pck = new ParamChecker(request, fu);
        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        try {
            JdbcTemplate jt = new JdbcTemplate();
            re = wad.create(jt, pck);
            if (re) {
                wad.writeAttachment(request, fu, wad.getLong("id"));
            }
            
            // 给流程中的其他啊参与者发送消息
            String myname = new Privilege().getUser(request);
    		UserDb ud = new UserDb(myname);
            int flowId = ParamUtil.getInt(request, "flowId");
            WorkflowDb wf = new WorkflowDb(flowId);
            MessageDb md = new MessageDb();
            String myAction = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + flowId;
            String allUserListSql = "select distinct user_name from flow_my_action where flow_id=" + flowId + " order by receive_date asc";
	    	ResultIterator ri = jt.executeQuery(allUserListSql);
	    	while (ri.hasNext()) {
	    		ResultRecord rr = (ResultRecord) ri.next();
	    		String userName = rr.getString(1);
	    		if (userName.equals(myname)) {
	    			continue;
	    		}
	    		md.sendSysMsg(userName, "请注意查看我的流程：" + wf.getTitle(), ud.getRealName() + "发表了评论：<p>"+pck.getValue("content")+"</p>", myAction);
	    	}
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        } catch (SQLException e) {
            throw new ErrMsgException(e.getMessage());
		}
        return re;
    }

    public boolean save(ServletContext application, HttpServletRequest request
                        ) throws
            ErrMsgException {

        String contentType = request.getContentType();
        if (contentType.indexOf("multipart/form-data") == -1) {
            throw new IllegalStateException(
                    "The content type of request is not multipart/form-data");
        }

        FileUpload fu = new FileUpload();
        Config cfg = new Config();
        String exts = cfg.get("flowFileExt");
        String[] extAry = StrUtil.split(exts, ",");
        fu.setValidExtname(extAry);
        fu.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K

        int ret = -1;
        try {
            ret = fu.doUpload(application, request);
        } catch (IOException e) {
            throw new ErrMsgException(e.getMessage());
        }

        if (ret!=FileUpload.RET_SUCCESS) {
            throw new ErrMsgException(fu.getErrMessage(request));
        }

        WorkflowAnnexDb wad = new WorkflowAnnexDb();
        String formCode = "flow_annex_save";

        ParamConfig pc = new ParamConfig(wad.getTable().
                                         getFormValidatorFile()); // "form_rule.xml");
        ParamChecker pck = new ParamChecker(request, fu);

        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        boolean re = false;

        long id = pck.getLong("id");
        wad = (WorkflowAnnexDb)wad.getQObjectDb(new Long(id));

        try {
            JdbcTemplate jt = new JdbcTemplate();
            re = wad.save(jt, pck);
            if (re) {
                if (fu.getFiles().size()>0) {
                    wad.writeAttachment(request, fu, wad.getLong("id"));
                }
            }
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }
    
    
   /**
    * @流程回复
    * @Description: 
    * @param myActionId
    * @return
    */
	public JSONArray getFlowAnnex(long myActionId,String myname,long flowId) {
		Object[] params = null;
		String sql = "";
		MyActionDb myActionDb = new MyActionDb(myActionId);
		JSONArray jArr = new JSONArray();
		if(flowId == 0){
			sql= "select id from flow_annex where flow_id=? and action_id=? order by add_date asc";
			params = new Object[]{myActionDb.getFlowId(), myActionDb.getActionId()};
		}else{
			sql= "select id from flow_annex where flow_id=? order by add_date asc";
			params = new Object[]{flowId};
		}
		
		WorkflowAnnexDb wfad = new WorkflowAnnexDb();
		Vector list = wfad.list(sql,params);
		Iterator<WorkflowAnnexDb> ir = list.iterator();
		while(ir.hasNext()) {
			WorkflowAnnexDb wad = (WorkflowAnnexDb)ir.next();
			String user_name = wad.getString("user_name");
			String reply_name = wad.getString("reply_name");
			boolean isSecret = wad.getBoolean("is_secret");
			int progress = wad.getInt("progress");
			String add_date =  DateUtil.format(wad.getDate("add_date"),"yyyy-MM-dd HH:mm:ss");
			if(isSecret && (!myname.equals(user_name) && !myname.equals(reply_name))) {
			}else{
				JSONObject jObj = new JSONObject();
				UserDb userDb = new UserDb(user_name);
				try {
					jObj.put("annexUser",userDb.getRealName());
					String content = wad.getString("content");
					jObj.put("content",content);
					jObj.put("add_date", add_date);
					jObj.put("progress", progress);
					jArr.put(jObj);
				} catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e.getMessage());
				}
			}
		}
		return jArr;
	}
	
	public boolean  create(String sql,Object[] params){
		 boolean flag = false;
		 JdbcTemplate jt = new JdbcTemplate();
		 try {
			int i = jt.executeUpdate(sql, params);
			if(i == 1){
				flag = true;
			}
		} catch (SQLException e) {
             LogUtil.getLog(getClass()).error(e.getMessage());
		}return flag;
	}
}
