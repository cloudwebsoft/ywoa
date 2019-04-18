package com.cloudweb.oa.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.LocalUtil;
import com.redmoon.oa.visual.FuncUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.notice.NoticeMgr;
import com.redmoon.oa.notice.NoticeReplyDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

@Controller
@RequestMapping("/public/flow")
public class WorkflowController {
	@Autowired  
	private HttpServletRequest request;
	
	@ResponseBody
	@RequestMapping(value = "/addReply", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})	
	public String addReply(String skey) {
		Privilege pvg = new Privilege();
		boolean re = pvg.auth(request);
		JSONObject json = new JSONObject();
		if (!re) { 
			try {
				json.put("ret", "0");
				json.put("msg", "权限非法！");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return json.toString();
		}

		String myActionId = ParamUtil.get(request, "myActionId");//当前活跃的标志id
		int flowId = ParamUtil.getInt(request, "flowId", -1);//当前流程id
		long actionId = ParamUtil.getLong(request, "actionId", -1);//当前流程action的id
		String content = request.getParameter("content");//“评论”的内容
		int parentId = ParamUtil.getInt(request, "parentId", -1);
		
		WorkflowDb wf = new WorkflowDb();
		wf = wf.getWorkflowDb(flowId);
		
		String replyName = wf.getUserName(); // 被回复的用户
		UserMgr um = new UserMgr();
		UserDb ud = um.getUserDb(pvg.getUserName());
		
		String partakeUsers = "";
		int isSecret = ParamUtil.getInt(request,"isSecret",0);//此“评论”是否隐藏
		// 将数据插入flow_annex附言表中
		long annexId = (long) SequenceManager.nextID(SequenceManager.OA_FLOW_ANNEX);

		int progress = ParamUtil.getInt(request, "progress", 0);
		
		// id,flow_id,content,user_name,reply_name,add_date,action_id,is_secret,parent_id,progress
		WorkflowAnnexDb wad = new WorkflowAnnexDb();
		JdbcTemplate jt = new JdbcTemplate();
		wad.create(jt, new Object[]{annexId, flowId, content, pvg.getUserName(), replyName, new java.util.Date(), actionId, isSecret, parentId, progress});
				
		// 不管来源于“代办流程”还是“我的流程”，跳转之后都进入“我的流程”。如果这条回复是私密的，只给交流双方发送消息提醒，不然就给这条流程的每个人都发送一条消息提醒
		// 写入进度
		Leaf lf = new Leaf();
		lf = lf.getLeaf(wf.getTypeCode());
		String formCode = lf.getFormCode();
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		
		try {
			// 进度为0的时候不更新
			if (fd.isProgress() && progress>0) {
				com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
				fdao = fdao.getFormDAO((int)flowId, fd);
				fdao.setCwsProgress(progress);
				fdao.save();
			}
			
			MessageDb md = new MessageDb();
		    String myAction = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + flowId;
		    MyActionDb mad = new MyActionDb();
		    if (!myActionId.equals("")) {
				mad = mad.getMyActionDb(Long.parseLong(myActionId));
		    }
		    
		    if(isSecret == 1){ // 如果是隐藏“评论”，只提醒发起“意见”的人
		    	if(!replyName.equals(pvg.getUserName())){// 如果发起“意见”的人不是自己，就提醒
		    		if (!myActionId.equals("")) {
		    			md.sendSysMsg(replyName, "请注意查看我的流程："+wf.getTitle(), ud.getRealName()+"对意见："+mad.getResult()+"发表了评论：<p>"+content+"</p>", myAction);
		    		} else {
		    			md.sendSysMsg(replyName, "请注意查看我的流程："+wf.getTitle(), ud.getRealName() + "发表了评论：<p>"+content+"</p>", myAction);
		    		}
		    	}
		    }else{
		    	// 如果不是隐藏“评论”，提醒所有参与流程的人
		    	// 解析得到参与流程的所有人
		       	String allUserListSql = "select distinct user_name from flow_my_action where flow_id="+ flowId + " order by receive_date asc";
		    	ResultIterator ri1 = jt.executeQuery(allUserListSql);
		    	ResultRecord rr1 = null;
		    	while (ri1.hasNext()) {
		    		rr1 = (ResultRecord)ri1.next();
		    		partakeUsers += rr1.getString(1)+",";
		    	}
		    	if(!partakeUsers.equals("")){
		    		partakeUsers = partakeUsers.substring(0,partakeUsers.length()-1);
		    	}
		    	String[] partakeUsersArr = StrUtil.split(partakeUsers, ",");
				for(String user : partakeUsersArr){
					// 如果不是自己就提醒
					if(!user.equals(pvg.getUserName())){
						if (!myActionId.equals("")) {
							md.sendSysMsg(user, "请注意查看我的流程："+wf.getTitle(), ud.getRealName()+"对意见："+mad.getResult()+"发表了评论：<p>"+content+"</p>", myAction);
						} else {
			    			md.sendSysMsg(user, "请注意查看我的流程："+wf.getTitle(), ud.getRealName() + "发表了评论：<p>"+content+"</p>", myAction);
			    		}
					}
				}
		    }
		}
		catch (ErrMsgException e) {
			e.printStackTrace();
			try {
				json.put("ret", "0");
				json.put("msg", e.getMessage());	
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return json.toString();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				json.put("ret", "0");
				json.put("msg", e.getMessage());	
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return json.toString();			
		}

		try {
			if (re) { 
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}		
	
	@ResponseBody
	@RequestMapping(value = "/saveSearchColProps", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})	
	public String saveSearchColProps(String typeCode, String colProps) {
		Privilege pvg = new Privilege();
		boolean re = pvg.auth(request);
		JSONObject json = new JSONObject();
		if (!re) { 
			try {
				json.put("ret", "0");
				json.put("msg", "权限非法！");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return json.toString();
		}
		
		// System.out.println(colProps);
		
		Leaf lf = new Leaf();
		if ("".equals(typeCode)) {
			typeCode = Leaf.CODE_ROOT;
		}
		lf = lf.getLeaf(typeCode);
		lf.setColProps(colProps);
		re = lf.update();
		
		try {
			if (re) { 
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}	
	
	/**
	 * 置待办记录的状态
	 * @param myActionId
	 * @param checkStatus
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/setMyActionStatus", method = RequestMethod.GET, produces={"text/html;charset=UTF-8;","application/json;"})	
	public String setMyActionStatus(long myActionId, int checkStatus) {
		boolean re = false;
		MyActionDb mad = new MyActionDb();
		mad = mad.getMyActionDb(myActionId);
		if (mad.isLoaded()) {
			mad.setCheckStatus(checkStatus);
			re = mad.save();
		}
		JSONObject json = new JSONObject();
		try {
			if (re) { 
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();		
	}
	
	/**
	 * 列出下载日志
	 * @param flowId
	 * @param attId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/listAttLog", method = RequestMethod.POST, produces={"text/html;", "application/json;charset=UTF-8;"})	
	public String listAttLog(long flowId, long attId) {
		AttachmentLogDb ald = new AttachmentLogDb();
		String sql = ald.getQuery(request, flowId, attId);
		int pageSize = ParamUtil.getInt(request, "rp", 20);
		int curPage = ParamUtil.getInt(request, "page", 1);
		ListResult lr = null;
		try {
			lr = ald.listResult(sql, curPage, pageSize);
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		JSONArray rows = new JSONArray();
		JSONObject jobject = new JSONObject();

		try {
			jobject.put("rows", rows);
			jobject.put("page", curPage);
			jobject.put("total", lr.getTotal());
		}catch (JSONException e){
			e.printStackTrace();
		}
		
		UserDb user = new UserDb();
		Iterator ir = lr.getResult().iterator();
		while (ir.hasNext()) {
			ald = (AttachmentLogDb)ir.next();
			JSONObject jo = new JSONObject();
			try {
				jo.put("id", String.valueOf(ald.getLong("id")));
				jo.put("logTime", DateUtil.format(ald.getDate("log_time"), "yyyy-MM-dd HH:mm:ss"));

				user = user.getUserDb(ald.getString("user_name"));
				jo.put("realName", user.getRealName());

				Attachment att = new Attachment((int) ald.getLong("att_id"));
				jo.put("attName", att.getName());

				jo.put("logType", AttachmentLogDb.getTypeDesc(ald.getInt("log_type")));
			}catch (JSONException e){
				e.printStackTrace();
			}
			
			rows.put(jo);
		}
		
		return jobject.toString();		
	}	
	
	/**
	 * 删除附件日志
	 * @param ids
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delLog", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})	
	public String delLog(String ids) {
		JSONObject json = new JSONObject();
		
		String[] ary = StrUtil.split(ids, ",");
		if (ary==null) {
			try {
				json.put("ret", "0");
				json.put("msg", "请选择记录！");
			}catch (JSONException e){
				e.printStackTrace();
			}
			return json.toString();
		}

		try {
			boolean re = false;
			for (String strId : ary) {
				long id = StrUtil.toLong(strId, -1);
				if (id!=-1) {
					AttachmentLogDb ald = new AttachmentLogDb();
					ald = (AttachmentLogDb)ald.getQObjectDb(id);
					
					boolean isValid = false;
					com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
					if (pvg.isUserPrivValid(request, "admin")) {
						isValid = true;
					}			
					else {
						long flowId = ald.getLong("flow_id");
						WorkflowDb wf = new WorkflowDb();
						wf = wf.getWorkflowDb((int)flowId);
						if (wf!=null) {
							LeafPriv lp = new LeafPriv(wf.getTypeCode());
							if (pvg.isUserPrivValid(request, "admin.flow")) {
								if (lp.canUserExamine(pvg.getUser(request))) {
									isValid = true;
								}
							}
						}
						else {
							// 对应的流程如不存在，则允许删除
							isValid = true;
						}			
					}
					
					if (!isValid) {
						json.put("ret", "0");
						json.put("msg", "权限非法！");
						return json.toString();
					}						
					
					if (isValid) {
						re = ald.del();
					}
				}
				else {
					json.put("ret", "0");
					json.put("msg", "标识非法！");					
					return json.toString();
				}
			}
			
			if (re) { 
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();		
	}

	/**
	 * flow_list.jsp中列出查询结果
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/list", method = RequestMethod.POST, produces={"text/html;", "application/json;charset=UTF-8;"})
	public String list() {
	    int displayMode = ParamUtil.getInt(request, "displayMode", WorkflowMgr.DISPLAY_MODE_SEARCH);
		String op = ParamUtil.get(request, "op");
		String typeCode;
		if ("search".equals(op)) {
			typeCode = ParamUtil.get(request, "f.typeCode");
		}
		else {
			typeCode = ParamUtil.get(request, "typeCode");
		}

        String action = ParamUtil.get(request, "action"); // sel 选择我的流程

        MyActionDb mad = new MyActionDb();
        MacroCtlMgr mm = new MacroCtlMgr();
        FormDb fd = new FormDb();
        FormDAO fdao = new FormDAO();
        UserMgr um = new UserMgr();
        UserDb user;

		WorkflowDb wf = new WorkflowDb();
        JSONObject jobject = new JSONObject();
        int pageSize = ParamUtil.getInt(request, "rp", 20);
        int curPage = ParamUtil.getInt(request, "page", 1);
        Leaf leaf = new Leaf();
        if (!"".equals(typeCode)) {
            leaf = leaf.getLeaf(typeCode);
            fd = fd.getFormDb(leaf.getFormCode());
        }

        JSONArray colProps = null;
        if (leaf.isLoaded() && !"".equals(leaf.getColProps())) {
            try {
                colProps = new JSONArray(leaf.getColProps());
            } catch (org.json.JSONException e) {
                System.out.println(getClass() + " colLeaf.getColProps()=" + leaf.getColProps());
                e.printStackTrace();
            }
        }
        if (colProps == null) {
            colProps = com.redmoon.oa.flow.Leaf.getDefaultColProps(request, typeCode, displayMode);
        }
        String userRealName = "";

        // 显示模式，0表示流程查询、1表示待办、2表示我参与的流程、3表示我发起的流程、4表示我关注的流程
        if (displayMode==WorkflowMgr.DISPLAY_MODE_DOING) {
            String sql = wf.getSqlDoing(request);
            ListResult lr = null;
            try {
                lr = mad.listResult(sql, curPage, pageSize);
            } catch (ErrMsgException e) {
                e.printStackTrace();
            }
            JSONArray rows = new JSONArray();
            try {
                jobject.put("rows", rows);
                jobject.put("page", curPage);
                jobject.put("total", lr.getTotal());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            java.util.Iterator ir = lr.getResult().iterator();
            while (ir.hasNext()) {
                mad = (MyActionDb) ir.next();
                WorkflowDb wfd = new WorkflowDb();
                wfd = wfd.getWorkflowDb((int) mad.getFlowId());
                String userName = wfd.getUserName();
                if (userName != null) {
                    user = um.getUserDb(wfd.getUserName());
                    userRealName = user.getRealName();
                }
                fdao = fdao.getFormDAO(wfd.getId(), fd);
                JSONObject jo = getRow(wfd, fdao, colProps, um, userRealName, mad, mm, leaf, displayMode, action);
                rows.put(jo);
            }
        }
        else {
            String sql;
            if (displayMode == WorkflowMgr.DISPLAY_MODE_ATTEND) {
                sql = wf.getSqlAttend(request);
            }
            else if (displayMode==WorkflowMgr.DISPLAY_MODE_MINE) {
                sql = wf.getSqlMine(request);
            }
            else if (displayMode==WorkflowMgr.DISPLAY_MODE_FAVORIATE) {
                sql = wf.getSqlFavorite(request);
            }
            else {
                sql = wf.getSqlSearch(request);
            }

            // DebugUtil.i(getClass(), "list sql=", sql);
            long t = new java.util.Date().getTime();

            ListResult lr = null;
            try {
                lr = wf.listResult(sql, curPage, pageSize);
            } catch (ErrMsgException e) {
                e.printStackTrace();
            }

            // DebugUtil.i(getClass(), "list", "listResult 时长：" + (new Date().getTime() - t) + "毫秒 sql=" + sql);

            JSONArray rows = new JSONArray();
            try {
                jobject.put("rows", rows);
                jobject.put("page", curPage);
                jobject.put("total", lr.getTotal());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Iterator ir = lr.getResult().iterator();
            while (ir.hasNext()) {
                WorkflowDb wfd = (WorkflowDb) ir.next();
                if (!typeCode.equals(wfd.getTypeCode())) { // 流程查询时，点击根节点，会显示所有的流程，此时typeCode可能与wfd.getTypeCode不一致
                    Leaf lf = leaf.getLeaf(wfd.getTypeCode());
                    fd = fd.getFormDb(lf.getFormCode());
                }
                else {
                    fd = fd.getFormDb(leaf.getFormCode());
                }
                fdao = fdao.getFormDAO(wfd.getId(), fd);

                user = um.getUserDb(wfd.getUserName());
                if (user.isLoaded())
                    userRealName = user.getRealName();

                JSONObject jo = getRow(wfd, fdao, colProps, um, userRealName, mad, mm, leaf, displayMode, action);
                rows.put(jo);
            }
        }

		return jobject.toString();
	}

	public JSONObject getRow(WorkflowDb wfd, FormDAO fdao, JSONArray colProps, UserMgr um, String userRealName, MyActionDb mad, MacroCtlMgr mm, Leaf leaf, int displayMode, String action) {
        JSONObject jo = new JSONObject();
        try {
            Leaf lf = leaf.getLeaf(wfd.getTypeCode());
            WorkflowPredefineDb wpd = new WorkflowPredefineDb();
            String rootPath = request.getContextPath();
            String cls = "class=\"readed\"";
            if (!mad.isReaded()) {
                cls = "class=\"unreaded\"";
            }

            for (int i = 0; i < colProps.length(); i++) {
                JSONObject json = (JSONObject) colProps.get(i);
/*					if (json.getBoolean("hide")) {
						continue;
					}*/
                String fieldName = json.getString("name");
                String val = "";
                if (fieldName.startsWith("f.")) {
                    fieldName = fieldName.substring(2);
                    if ("id".equalsIgnoreCase(fieldName)) {
                        val = String.valueOf(wfd.getId());
                    } else if ("flow_level".equalsIgnoreCase(fieldName)) {
                        val = WorkflowMgr.getLevelImg(request, wfd);
                    } else if ("title".equalsIgnoreCase(fieldName)) {
                        val = "<a href=\"javascript:;\" onclick=\"addTab('" + wfd.getTitle() + "', '" + Global.getRootPath(request) + "/flow_modify.jsp?flowId=" + wfd.getId() + "')\" title=\"" + wfd.getTitle() + "\">" + StrUtil.getLeft(wfd.getTitle(), 40) + "</a>";
                    } else if ("type_code".equalsIgnoreCase(fieldName)) {
                        if (lf != null) {
                            val = "<a href=\"flow_list.jsp?op=search&displayMode=" + displayMode + "&typeCode=" + StrUtil.UrlEncode(lf.getCode()) + "\">" + lf.getName(request) + "</a>";
                        } else
                            val = "";
                    } else if ("mydate".equals(fieldName)) {
                        val = DateUtil.format(wfd.getBeginDate(), "yy-MM-dd HH:mm");
                    } else if ("userName".equals(fieldName)) {
                        val = userRealName;
                    } else if ("finallyApply".equals(fieldName)) {
                        // 取得最后一个已办理的人员
                        MyActionDb lastMad = mad.getLastMyActionDbDoneOfFlow(wfd.getId());
                        if (lastMad != null) {
                            val = um.getUserDb(lastMad.getUserName()).getRealName();
                        }
                    } else if ("currentHandle".equals(fieldName)) {
                        Iterator ir2 = mad.getMyActionDbDoingOfFlow(wfd.getId()).iterator();
                        while (ir2.hasNext()) {
                            mad = (MyActionDb) ir2.next();
                            if (!val.equals("")) {
                                val += "、";
                            }
                            val = um.getUserDb(mad.getUserName()).getRealName();
                        }
                    } else if ("status".equals(fieldName)) {
                        if (displayMode!=WorkflowMgr.DISPLAY_MODE_DOING) {
                            val = wfd.getStatusDesc();
                        }
                        else {
                            val = WorkflowActionDb.getStatusName(mad.getActionStatus());
                        }
                    } else if ("remainTime".equals(fieldName)) {
                        String remainDateStr = "";
                        if (mad.getExpireDate() != null && DateUtil.compare(new java.util.Date(), mad.getExpireDate()) == 2) {
                            int[] ary = DateUtil.dateDiffDHMS(mad.getExpireDate(), new java.util.Date());
                            String str_day = LocalUtil.LoadString(request, "res.flow.Flow", "day");
                            String str_hour = LocalUtil.LoadString(request, "res.flow.Flow", "h_hour");
                            String str_minute = LocalUtil.LoadString(request, "res.flow.Flow", "minute");
                            remainDateStr = ary[0] + " " + str_day + ary[1] + " " + str_hour + ary[2] + " " + str_minute;
                            val = remainDateStr;
                        }
                    }
                }
                else if (fieldName.equals("operate")) {
                    if (displayMode==WorkflowMgr.DISPLAY_MODE_SEARCH) {
                        val = "<a href=\"javascript:;\" onclick=\"addTab('" + wfd.getTitle() + "', '" + rootPath + "/flow_modify.jsp?flowId=" + wfd.getId() + "')\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "viewProcess") + "\">" + SkinUtil.LoadString(request, "res.flow.Flow", "show") + "</a>";
                    }
                    else if (displayMode==WorkflowMgr.DISPLAY_MODE_DOING){
                        String suspend = "";
                        if (mad.getCheckStatus() == MyActionDb.CHECK_STATUS_SUSPEND) {
                            suspend = mad.getCheckStatusName();
                        }
                        if (lf.getType() == Leaf.TYPE_LIST) {
                            val = "<a href=\"" + rootPath + "/flow_dispose.jsp?myActionId=" + mad.getId() + "\">" + SkinUtil.LoadString(request, "res.flow.Flow", "chandle") + suspend + "</a>";
                        } else {
                            wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
                            if (wpd.isLight()) {
                                val = "<a href=\"javascript:;\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "processingFlow") + "：" + wfd.getTitle() + "\" " + cls + " link=\"flow_dispose_light.jsp?myActionId=" + mad.getId() + "\"";
                                val += " onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>", "").replace("&#039;","\\&#039;") + "', '" + rootPath + "/flow_dispose_light.jsp?myActionId=" + mad.getId() + "')\">" + SkinUtil.LoadString(request, "res.flow.Flow", "chandle") + "</a>";
                            } else {

                                val = "<a href=\"javascript:;\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "processingFlow") + "：" + wfd.getTitle() + "\" <%=cls%> link=\"flow_dispose_free.jsp?myActionId=" + mad.getId() + "\"";
                                val += " onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "") + "', '" + rootPath + "/flow_dispose_free.jsp?myActionId=" + mad.getId() + "')\">" + SkinUtil.LoadString(request, "res.flow.Flow", "chandle") + "/></a>";

                            }
                        }
                        val += "&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:;\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "focusProcess") + "/>\" onclick=\"favorite(" + wfd.getId() + ")\">" + SkinUtil.LoadString(request, "res.flow.Flow", "attention") + "</a>";
                    }
                    else if (displayMode==WorkflowMgr.DISPLAY_MODE_ATTEND || displayMode==WorkflowMgr.DISPLAY_MODE_MINE) {
						if (wpd.isLight()) {
							val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>", "").replace("&#039;", "\\&#039;") + "', '" + request.getContextPath() + "/flow_dispose_light_show.jsp?flowId=" + wfd.getId() + "')\" title=\"" + wfd.getTitle() + "\">" + SkinUtil.LoadString(request, "res.flow.Flow", "show") + "</a>";
						} else {
							val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "") + "', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + wfd.getId() + "')\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "viewProcess") + "\">" + SkinUtil.LoadString(request, "res.flow.Flow", "show") + "</a>";
						}
						// 当action为sel时，显示选择链接，否则显示关注链接
						if ("sel".equals(action)) {
							val += "&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"selFlow('" + wfd.getId() + "', '" + wfd.getTitle() + "')\">" + SkinUtil.LoadString(request, "res.flow.Flow", "choose") + "</a>";
						} else {
							val += "&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"favorite(" + wfd.getId() + ")\">" + SkinUtil.LoadString(request, "res.flow.Flow", "attention") + "</a>";
						}
                    }
                    else if (displayMode==WorkflowMgr.DISPLAY_MODE_FAVORIATE) {
						if (wpd.isLight()) {
							val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>", "").replace("&#039;", "\\&#039;") + "', '" + request.getContextPath() + "/flow_dispose_light_show.jsp?flowId=" + wfd.getId() + "')\" title=\"" + wfd.getTitle() + "\">" + SkinUtil.LoadString(request, "res.flow.Flow", "show") + "</a>";
						} else {
							val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "") + "', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + wfd.getId() + "')\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "viewProcess") + "\">" + SkinUtil.LoadString(request, "res.flow.Flow", "show") + "</a>";
						}
                    	val += "&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"unfavorite(" + wfd.getId() + ")\">" + SkinUtil.LoadString(request, "res.flow.Flow", "cancelAttention") + "</a>";
					}
                }
                else {
                    FormField ff = fdao.getFormField(fieldName);
                    if (ff != null) {
                        if (ff.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                            if (mu != null) {
                                val = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
                            }
                        } else {
                            val = FuncUtil.renderFieldValue(fdao, ff);
                        }
                    }
                }
                jo.put(json.getString("name"), val);
            }
        } catch(JSONException e){
            e.printStackTrace();
        }
        return jo;
    }
}
