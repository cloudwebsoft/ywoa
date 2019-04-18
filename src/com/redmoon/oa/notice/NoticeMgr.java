package com.redmoon.oa.notice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.fileark.plugin.sms.SMSDocumentAction;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.UserGroupDb;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;

public class NoticeMgr {
    FileUpload fileUpload;

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

    public boolean create(ServletContext application,
                          HttpServletRequest request) throws ErrMsgException {

        Privilege pvg = new Privilege();
        if (!pvg.isUserLogin(request))
            throw new ErrMsgException("请先登录!");

        doUpload(application, request);

        if (!isNoticeAddable(request))
            return false;

        boolean re = true;
        String userName = pvg.getUser(request);
        String title = StrUtil.getNullStr(fileUpload.getFieldValue("title"));
        //String content = StrUtil.getNullStr(fileUpload.getFieldValue("content"));
        String content = StrUtil.getNullStr(fileUpload.getFieldValue("htmlcode"));
        boolean isToMobile = StrUtil.getNullStr(fileUpload.getFieldValue("isToMobile")).equals("true");
        // boolean isMessageSend = StrUtil.getNullStr(fileUpload.getFieldValue("isUseMsg")).equals("true");
        String strusers = StrUtil.getNullStr(fileUpload.getFieldValue("receiver"));
        int isShow = StrUtil.toInt(fileUpload.getFieldValue("isShow"), 0);
        
        int isDeptNotice = StrUtil.toInt(fileUpload.getFieldValue(
                "isDeptNotice"));
        if (title.equals(""))
            throw new ErrMsgException("标题不能为空！");
        if (content.equals(""))
            throw new ErrMsgException("正文不能为空！");
        
        java.util.Date beginDate = DateUtil.parse(fileUpload.getFieldValue("beginDate"), "yyyy-MM-dd");
        java.util.Date endDate = DateUtil.parse(fileUpload.getFieldValue("endDate"), "yyyy-MM-dd");   
        
        if (beginDate == null) {
        	beginDate = new Date();
        }

        if (endDate != null && DateUtil.compare(beginDate, endDate)==1) {
        	throw new ErrMsgException("有效期的开始日期不能大于结束日期");
        }
        
        String color = StrUtil.getNullStr(fileUpload.getFieldValue("color"));;
        //boolean isBold = StrUtil.getNullStr(fileUpload.getFieldValue("isBold")).equals("true");
        String unitCode = StrUtil.getNullStr(fileUpload.getFieldValue("unitCode"));
        
        int level = StrUtil.toInt(fileUpload.getFieldValue("level"), NoticeDb.LEVEL_NONE);
        boolean isBold = false;
        if(level == 1){
        	isBold = true;
        }
        //add by tbl
       int isall = StrUtil.toInt(fileUpload.getFieldValue("isall"));
        //add by tbl
       //add by lzm 是否回复 强制回复
       int is_reply = StrUtil.toInt(fileUpload.getFieldValue("is_reply"), 0);
       int is_forced_response = StrUtil.toInt(fileUpload.getFieldValue("is_forced_response"),0);
       
        
        NoticeDb nd = new NoticeDb();
        nd.setTitle(title);
        nd.setContent(content);
        nd.setUserName(userName);
        nd.setCreateDate(new java.util.Date());
        nd.setIsDeptNotice(isDeptNotice);
        nd.setIsShow(isShow);
        nd.setBeginDate(beginDate);
        nd.setEndDate(endDate);
        nd.setColor(color);
        nd.setBold(isBold);
        nd.setUnitCode(unitCode);
        nd.setLevel(level);
        //add by tbl
        nd.setIsall(isall);
        nd.setUserList(strusers);
        //add by tbl
        //add by lzm
        nd.setIs_reply(is_reply);
        nd.setIs_forced_response(is_forced_response);
        
        re = nd.create(fileUpload);
        
        if(re) {
        	re = createNoticeReply(nd, isToMobile);
        }
        
/*        if (re) {
        	
            int intToMobile = 0;
            if (isToMobile) {
                intToMobile = 2;
            } else if (isMessageSend) {
                intToMobile = 1;
            }

            UserDb user = new UserDb();
            user = user.getUserDb(userName);

            if (!strusers.trim().equals("")) {
                sendMsg(request, user.getRealName(), title, content, StrUtil.split(strusers, ","),
                        intToMobile, false, nd.getId());
            }
        }*/

        return re;
    }
    
    public boolean creatNoticeForFlow(NoticeDb noticeDb)
    {
    	boolean re = true;
        NoticeDb nd = new NoticeDb();
        re = nd.createNoticeForFlow();
        
        boolean isToMobile = com.redmoon.oa.sms.SMSFactory.isUseSMS();
        
        if(re)
        	re = createNoticeReply(nd, isToMobile);

        if (re) {

            UserDb user = new UserDb();
            user = user.getUserDb(nd.getUserName());

//            if (!nd.getUserList().trim().equals("")) {
//                sendMsg(request, user.getRealName(), title, content, StrUtil.split(strusers, ","),
//                        intToMobile, false, nd.getId());
//            }
        }
    	return re;
    }
    
    public boolean createNoticeReply(NoticeDb noticeDb, boolean isToMobile){
    	
    	String [] usernames = null;
    	
    	if (NoticeDb.IS_ALL_SEL_USER==noticeDb.getIsall()) // 选择特定用户的情况 取出userlist中的用户
    	{
    		usernames = StrUtil.split(noticeDb.getUserList(), ",");
    	}
    	else if(NoticeDb.IS_ALL_DEPT==noticeDb.getIsall()) // 部门管理员选择全部人员 取出部门下全部用户,含子部门用户
    	{
    		String userName = noticeDb.getUserName();
			DeptUserDb dud = new DeptUserDb();
			Vector v1 = dud.getDeptsOfUser(userName); // 取得用户所在部门的deptcode
			//ArrayList<String> v2 = new ArrayList<String>();
			//v2.add(userName);
			if (v1.size() > 0)
			{
				for(int j=0;j<v1.size();j++)
				{
					DeptDb deptDb = (DeptDb)v1.get(j);
					Vector va = new Vector();
					try {
						va = deptDb.getAllChild(va, deptDb);
					} catch (ErrMsgException e) {
						e.printStackTrace();
					}
					Vector va2 = new Vector();
					va2.add(deptDb);
					va2.addAll(va);
					Iterator it = va2.iterator();
					String sql = "";
					while (it.hasNext()) {
						DeptDb dept = (DeptDb) it.next();
						String deptCode = dept.getCode();
						sql += (sql.equals("") ? "" : ",") + StrUtil.sqlstr(deptCode);
						/*
						Vector v = dud.list(deptCode);// 根据deptcode取得部门下用户
						if (v.size() > 0)
						{
							//usernames = new String[v.size()];
							for (int i=0;i<v.size();i++)
							{
								DeptUserDb ud = (DeptUserDb)v.get(i);
							    v2.add(ud.getUserName());
							}
						}*/
					}
					if (!sql.equals("")) {
						sql = "select distinct(u.name) from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in (" + sql + ") order by orders desc";
						JdbcTemplate jt = new JdbcTemplate();
						int i = 0;
						try {
							ResultIterator ri = jt.executeQuery(sql);
							usernames = new String[ri.getRows()];
							while (ri.hasNext()) {
								ResultRecord rr = (ResultRecord) ri.next();
								//v2.add(rr.getString(1));
								usernames[i++] = rr.getString(1);
							}
						} catch (SQLException e) {
							LogUtil.getLog(getClass()).error("createNoticeReply:" + StrUtil.trace(e));
							e.printStackTrace();
						} finally {
							jt.close();
						}
					}
				}
			}
			//removeDuplicateWithOrder(v2);
			//usernames = v2.toArray(new String[]{});

    	}
    	else if (NoticeDb.IS_ALL_WHOLE==noticeDb.getIsall()) // 系统管理员选择全部人员 取出当前单位及其子单位的全部用户
    	{
    		DeptDb dd = new DeptDb(noticeDb.getUnitCode());
    		Vector v = new Vector();
    		try {
				v = dd.getAllChild(v, dd);
			} catch (ErrMsgException e) {
				e.printStackTrace();
			}
			Vector va2 = new Vector();
			va2.add(dd);
			va2.addAll(v);
			Iterator it = va2.iterator();
    		//Vector uva = new Vector();
    		String sql = "";
    		while (it.hasNext()) {
    			DeptDb dept = (DeptDb) it.next();
    			if (dept.getType() == DeptDb.TYPE_UNIT) {
    				String deptCode = dept.getCode();
    				sql += (sql.equals("") ? "" : ",") + StrUtil.sqlstr(deptCode);
    			}
	    		//UserDb ud = new UserDb();
	    		//Vector uv = ud.listUserOfUnit(dept.getCode());
	    		//uva.addAll(uv);
    		}
    		if (!sql.equals("")) {
				sql = "select distinct(name) from users where isValid=1 and unit_code in (" + sql + ") order by orders desc";
				JdbcTemplate jt = new JdbcTemplate();
				int i = 0;
				try {
					ResultIterator ri = jt.executeQuery(sql);
					usernames = new String[ri.getRows()];
					while (ri.hasNext()) {
						ResultRecord rr = (ResultRecord) ri.next();
						//v2.add(rr.getString(1));
						usernames[i++] = rr.getString(1);
					}
				} catch (SQLException e) {
					LogUtil.getLog(getClass()).error("createNoticeReply:" + StrUtil.trace(e));
					e.printStackTrace();
				} finally {
					jt.close();
				}
			}
    		/*
    		if (uva.size() > 0) {
				usernames = new String[uva.size()];
	    		Iterator uit = uva.iterator();
	    		int i = 0;
				while (uit.hasNext()) {
					UserDb ud = (UserDb) uit.next();
					usernames[i++] = ud.getName();
				}
    		}*/
    	}
    	else
    	{
    		return false;
    	}
    	
    	if (usernames == null || usernames.length == 0) {
    		return false;
    	}
    	
    	boolean re = true;
    	NoticeReplyDb brd = new NoticeReplyDb();
    	int count = brd.createBatch(noticeDb.getId(), usernames);
    	if (count>0) {
    		re = true;
			MessageDb mdb = new MessageDb();
        	String txt = StrUtil.getAbstract(null, noticeDb.getContent(), 380, "", false) + "......";
			try {				
				mdb.sendSysMsgNotice(noticeDb.getId(), usernames, "请注意查看：通知公告 "+noticeDb.getTitle(), txt);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				re = false;
			}
			
			if (isToMobile) {
				if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
	                IMsgUtil imu = SMSFactory.getMsgUtil();
	                try {
						imu.sendBatch(usernames, txt, noticeDb.getUserName());
					} catch (ErrMsgException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
    	}
    	return re;
    }
    
	public void removeDuplicateWithOrder(ArrayList list) {
		Set set = new HashSet();
		ArrayList newList = new ArrayList();
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (set.add(element))
				newList.add(element);
		}
		list.clear();
		list.addAll(newList);
		//System.out.println(" remove duplicate " + list);
	}

    /**
     *
     * @param application ServletContext
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean edit(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isUserLogin(request))
            throw new ErrMsgException("请先登录!");
        doUpload(application, request);

        String id = StrUtil.getNullStr(fileUpload.getFieldValue("id"));
        long idLong = Long.valueOf(id).longValue();
        if (!isNoticeManageable(request, idLong)){
            return false;
        }
        if (!isNoticeAddable(request)){
            return false;
        }

        boolean re = true;

        String userName = pvg.getUser(request);
        String title = StrUtil.getNullStr(fileUpload.getFieldValue("title"));
        String content = StrUtil.getNullStr(fileUpload.getFieldValue("content"));
        int isDeptNotice = StrUtil.toInt(fileUpload.getFieldValue(
                "isDeptNotice"));
        if (title.equals(""))
            throw new ErrMsgException("标题不能为空！");
        if (content.equals(""))
            throw new ErrMsgException("正文不能为空！");
        int isShow = StrUtil.toInt(fileUpload.getFieldValue("isShow"),0);
        boolean isToMobile = StrUtil.getNullStr(fileUpload.getFieldValue("isToMobile")).equals("true");
        boolean isMessageSend = StrUtil.getNullStr(fileUpload.getFieldValue("isUseMsg")).equals("true");
        String strusers = StrUtil.getNullStr(fileUpload.getFieldValue("receiver"));
        java.util.Date beginDate = DateUtil.parse(fileUpload.getFieldValue("beginDate"), "yyyy-MM-dd");
        java.util.Date endDate = DateUtil.parse(fileUpload.getFieldValue("endDate"), "yyyy-MM-dd");        
        if (DateUtil.compare(beginDate, endDate)==1) {
        	throw new ErrMsgException("有效期的开始日期不能大于结束日期");
        }
        
        String color = StrUtil.getNullStr(fileUpload.getFieldValue("color"));
        boolean isBold = StrUtil.getNullStr(fileUpload.getFieldValue("isBold")).equals("true");

        String unitCode = StrUtil.getNullStr(fileUpload.getFieldValue("unitCode"));
        int level = StrUtil.toInt(fileUpload.getFieldValue("level"), NoticeDb.LEVEL_NONE);

        NoticeDb nd = new NoticeDb();
        nd = nd.getNoticeDb(idLong);
        nd.setTitle(title);
        nd.setContent(content);
        nd.setUserName(userName);
        nd.setCreateDate(new java.util.Date());
        nd.setIsDeptNotice(isDeptNotice);
        nd.setIsShow(isShow);
        nd.setBeginDate(beginDate);
        nd.setEndDate(endDate);
        
        nd.setColor(color);
        nd.setBold(isBold);
        nd.setUnitCode(unitCode);
        
        nd.setLevel(level);
        
        re = nd.save(fileUpload);

        /**
        if (re) {
            int intToMobile = 0;
            if (isToMobile) {
                intToMobile = 2;
            } else if (isMessageSend) {
                intToMobile = 1;
            }

            UserDb user = new UserDb();
            user = user.getUserDb(userName);
            
            if (!strusers.trim().equals(""))
                sendMsg(request, user.getRealName(), title, content, StrUtil.split(strusers, ","),
                        intToMobile, true, nd.getId());
        }*/

        return re;
    }

    /**
     *
     * @param request HttpServletRequest
     * @param id long
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean del(  HttpServletRequest request, long id) throws  ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isUserLogin(request))
            throw new ErrMsgException("请先登录!");

        if (!isNoticeManageable(request, id)) {
            return false;
        }
        boolean re = false;
        NoticeDb nd = new NoticeDb(id);
	//nd = nd.getNoticeDb(id);
        re = nd.del();
        return re;
    }

    /**
     *
     * @param attachId long
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean delAttachment(long attachId) throws ErrMsgException {
        boolean re = false;
        NoticeAttachmentDb nad = new NoticeAttachmentDb(attachId);
        re = nad.del();
        return re;
    }

    /**
     * 只判断用户所属部门，不包含子部门(保留方法)
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean isNoticeAddable2(
            HttpServletRequest request) throws
            ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isUserLogin(request))
            throw new ErrMsgException("请先登录!");

        String userName = pvg.getUser(request);
        String deptCode = "";

        if (pvg.isUserPrivValid(request, "notice") ||
            pvg.isUserPrivValid(request, "notice.all")) {
            return true;
        }
        if (pvg.isUserPrivValid(request, "notice.dept")) {
            Vector ud = new Vector();
            DeptUserDb deptUserDb = new DeptUserDb();
            ud = deptUserDb.getDeptsOfUser(userName);
            DeptDb deptDb = new DeptDb();
            Iterator ir;
            String depts = StrUtil.getNullStr(fileUpload.getFieldValue("depts"));
            String[] ary = StrUtil.split(depts, ",");
            boolean isValid = false;
            if (ary != null) {
                int len = ary.length;
                for (int i = 0; i < len; i++) {
                    isValid = false;
                    ir = ud.iterator();
                    while (ir.hasNext()) {
                        deptDb = (DeptDb) ir.next();
                        deptCode = deptDb.getCode();
                        if (ary[i].equals(deptCode)) {
                            isValid = true;
                            break;
                        } else {
                            continue;
                        }
                    }
                    if (!isValid) {
                        deptDb = new DeptDb(ary[i]);
                        throw new ErrMsgException("您不隶属于部门:" + deptDb.getName() +
                                                  "，无法发布该部门通知!");
                    }
                }
            }
            return isValid;
        }
        return false;
    }

    /**
     * 判断用户所属部门，包含子部门也可发
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean isNoticeAddable(
            HttpServletRequest request) throws
            ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isUserLogin(request))
            throw new ErrMsgException("请先登录!");

        String userName = pvg.getUser(request);
        String deptCode = "";

        if (pvg.isUserPrivValid(request, "notice") ||
            pvg.isUserPrivValid(request, "notice.all")) {
            return true;
        }
        if (pvg.isUserPrivValid(request, "notice.dept")) {
            Vector ud = new Vector();
            Vector allChild = new Vector();
            DeptUserDb deptUserDb = new DeptUserDb();
            ud = deptUserDb.getDeptsOfUser(userName);
            DeptDb deptDb = new DeptDb();
            Iterator ir;
            ir = ud.iterator();
            while (ir.hasNext()) {
                deptDb = (DeptDb) ir.next();
                allChild.add(deptDb);
                Vector vt = new Vector();
                deptDb.getAllChild(vt,deptDb);
                allChild.addAll(vt);
            }

            String depts = StrUtil.getNullStr(fileUpload.getFieldValue("depts"));
            String[] ary = StrUtil.split(depts, ",");
            boolean isValid = false;
            if (ary != null) {
                int len = ary.length;
                for (int i = 0; i < len; i++) {
                    isValid = false;
                    ir = allChild.iterator();
                    while (ir.hasNext()) {
                        deptDb = (DeptDb) ir.next();
                        deptCode = deptDb.getCode();
                        if (ary[i].equals(deptCode)) {
                            isValid = true;
                            break;
                        } else {
                            continue;
                        }
                    }
                    if (!isValid) {
                        deptDb = new DeptDb(ary[i]);
                        throw new ErrMsgException("您不隶属于部门:" + deptDb.getName() +
                                                  "，无法发布该部门通知!");
                    }
                }
            }
            return isValid;
        }
        return false;
    }

    /**
     *
     * @param request HttpServletRequest
     * @param StrNoticeId String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean isNoticeManageable(HttpServletRequest request,
                                          String StrNoticeId) throws
                ErrMsgException {
            long noticeId = StrUtil.toLong(StrNoticeId);
            return isNoticeManageable(request, noticeId);
}

        /**
         *只判断用户所属部门，不包含子部门(保留方法)
         * @param request HttpServletRequest
         * @param noticeId long
         * @return boolean
         * @throws ErrMsgException
         */
        public boolean isNoticeManageable2(HttpServletRequest request,
                                      long noticeId) throws
            ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isUserLogin(request))
            throw new ErrMsgException("请先登录!");

        String userName = pvg.getUser(request);
        String deptCode = "";
        String deptCodeNotice ="";

        if (pvg.isUserPrivValid(request, "notice")) {
            return true;
        }
        if (pvg.isUserPrivValid(request, "notice.dept")) {

            Vector ud = new Vector();
            DeptUserDb deptUserDb = new DeptUserDb();
            Vector nd = new Vector();
            NoticeDeptDb ndd = new NoticeDeptDb();
            NoticeDb noticeDb = new NoticeDb(noticeId);

            if (!noticeDb.isDeptNotice()){
                throw new ErrMsgException("您没有管理公共通知的权限!");
            }

            ud = deptUserDb.getDeptsOfUser(userName);
            DeptDb deptDb = new DeptDb();
            Iterator ir;

            nd = ndd.getDeptOfNotice(noticeId);
            Iterator ird = nd.iterator();
            DeptDb deptDbNotice = new DeptDb();
            boolean isValid = false;
            while (ird.hasNext()) {
                isValid = false;
                deptDbNotice = (DeptDb) ird.next();
                deptCodeNotice = deptDbNotice.getCode();
                ir = ud.iterator();
                while (ir.hasNext()) {
                    deptDb = (DeptDb) ir.next();
                    deptCode = deptDb.getCode();
                    if (deptCodeNotice.equals(deptCode)) {
                        isValid = true;
                        break;
                    } else {
                        continue;
                    }
                }
                if (!isValid) {
                    throw new ErrMsgException("您不隶属于部门:" + deptDbNotice.getName() +
                                              "，无法管理该部门通知!");
                }
            }
            return isValid;
        }
        return false;
    }

    /**
       *判断用户所属部门，包含子部门也可发
       * @param request HttpServletRequest
       * @param noticeId long
       * @return boolean
       * @throws ErrMsgException
       */
      public boolean isNoticeManageable(HttpServletRequest request,
                                    long noticeId) throws
          ErrMsgException {
      Privilege pvg = new Privilege();
      if (!pvg.isUserLogin(request))
          throw new ErrMsgException("请先登录!");

      String userName = pvg.getUser(request);
      String deptCode = "";
      String deptCodeNotice ="";

      if (pvg.isUserPrivValid(request, "notice")) {
          return true;
      }
      if (pvg.isUserPrivValid(request, "notice.dept")) {

          Vector ud = new Vector();
          Vector allChild = new Vector();
          DeptUserDb deptUserDb = new DeptUserDb();
          Vector nd = new Vector();
          NoticeDeptDb ndd = new NoticeDeptDb();
          NoticeDb noticeDb = new NoticeDb();
          noticeDb = noticeDb.getNoticeDb(noticeId);
          
          if (!noticeDb.isDeptNotice()){
              throw new ErrMsgException("您没有管理公共通知的权限!");
          }

          ud = deptUserDb.getDeptsOfUser(userName);
          DeptDb deptDb = new DeptDb();
          Iterator ir;

          ir = ud.iterator();
          while (ir.hasNext()) {
              deptDb = (DeptDb) ir.next();
              allChild.add(deptDb);
              Vector vt = new Vector();
              deptDb.getAllChild(vt, deptDb);
              allChild.addAll(vt);
          }

          nd = ndd.getDeptOfNotice(noticeId);
          Iterator ird = nd.iterator();
          DeptDb deptDbNotice = new DeptDb();
          boolean isValid = false;
          // 遍历通知所发布的部门，当用户所在部门及其下属部门包含了通知所发布的部门，才能管理该通知
          while (ird.hasNext()) {
              deptDbNotice = (DeptDb) ird.next();
              isValid = false;
              deptCodeNotice = deptDbNotice.getCode();
              ir = allChild.iterator();
              // 遍历用户所在的部门及下属部门
              while (ir.hasNext()) {
                  deptDb = (DeptDb) ir.next();
                  deptCode = deptDb.getCode();
                  if (deptCodeNotice.equals(deptCode)) {
                      isValid = true;
                      break;
                  } else {
                      continue;
                  }
              }
              if (!isValid) {
                  throw new ErrMsgException("您不隶属于部门：" + deptDbNotice.getName() +
                                            "，无法管理该通知!");
              }
          }
          return isValid;
      }
      return false;
      }
      
      public void sendMsg(HttpServletRequest request, String realName, String noticeTitle, String noticeContent, String[] users, int intToMobile, boolean isEdit, long noticeId) {
    	  
          String t = SkinUtil.LoadString(request,
                                         "res.module.notice",
                                         "notice");
          if (isEdit) {
        	  t = SkinUtil.LoadStr(request, "res.module.notice", "notice_modified");
          }
          t = StrUtil.format(t, new Object[]{realName, noticeTitle});
          if (intToMobile==2) {
              IMessage imsg = null;
              ProxyFactory proxyFactory = new ProxyFactory(
                      "com.redmoon.oa.message.MessageDb");
              /*
              Advisor adv = new Advisor();
              MobileAfterAdvice mba = new MobileAfterAdvice();
              adv.setAdvice(mba);
              adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
              proxyFactory.addAdvisor(adv);
              */
              imsg = (IMessage) proxyFactory.getProxy();
              int len = users.length;
              try {
            	  // String sms = realName + "请您及时查阅一则新通知：" + noticeTitle;
                  IMsgUtil imu = SMSFactory.getMsgUtil();
                  UserMgr um = new UserMgr();
                  for (int i = 0; i < len; i++) {
                      imsg.sendSysMsg(users[i], t, getTextFromHTML(noticeContent), MessageDb.ACTION_NOTICE, "", String.valueOf(noticeId));
                      if (imu != null) {
                          UserDb ud = um.getUserDb(users[i]);
                          imu.send(ud, t, MessageDb.SENDER_SYSTEM);
                      }
                  }
              }
              catch (ErrMsgException e) {
                  LogUtil.getLog(getClass()).error("sendMsg:" + StrUtil.trace(e));
              }
          } else if (intToMobile == 1) {
              // 发送短消息
              MessageDb md = new MessageDb();
              int len = users.length;
              try {
                  for (int i = 0; i < len; i++) {
                      md.sendSysMsg(users[i], t, noticeTitle, MessageDb.ACTION_NOTICE, "", String.valueOf(noticeId));
                  }
              }
              catch (ErrMsgException e) {
                  LogUtil.getLog(getClass()).error("sendMsg:" + StrUtil.trace(e));
              }
          }
      }

  public static String getTextFromHTML(String content) {
      String str = "";
      try {
          Parser myParser;
          NodeList nodeList = null;
          myParser = Parser.createParser(content, "utf-8");
          NodeFilter textFilter = new NodeClassFilter(TextNode.class);
          NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
          NodeFilter imgFilter = new NodeClassFilter(ImageTag.class);
          // 暂时不处理 meta
          // NodeFilter metaFilter = new NodeClassFilter(MetaTag.class);
          OrFilter lastFilter = new OrFilter();
          lastFilter.setPredicates(new NodeFilter[] {textFilter, linkFilter,
                                   imgFilter});
          nodeList = myParser.parse(lastFilter);
          Node[] nodes = nodeList.toNodeArray();
          for (int i = 0; i < nodes.length; i++) {
              Node anode = (Node) nodes[i];
              String line = "";
              if (anode instanceof TextNode) {
                  TextNode textnode = (TextNode) anode;
                  // line = textnode.toPlainTextString().trim();
                  line = textnode.getText();
              }
              str += line;
          }
      }
      catch (ParserException e) {
          Logger.getLogger(SMSDocumentAction.class).error("getAbstract:" + e.getMessage());
      }
      return str;
    }
  
	public void creatNoticeReplayForFlow(NoticeDb noticeDb, int flowId)
			throws ErrMsgException {
		String[] usernames = null;
		if (0 == noticeDb.getIsall()) {// 选择特定用户的情况 取出userlist中的用户
			usernames = StrUtil.split(getUserList(flowId), ",");
//			if (usernames.length == 1 && usernames[0].equals("全部用户")) {
//				noticeDb.setIsall(1);
//			}
		} else if (1 == noticeDb.getIsall()) {// 单位管理员选择全部人员 取出单位下全部用户
			String userName = noticeDb.getUserName();
			DeptUserDb dud = new DeptUserDb();
			Vector v1 = dud.getDeptsOfUser(userName); // 取得用户所在部门的deptcode
			// ArrayList<String> v2 = new ArrayList<String>();
			// v2.add(userName);
			if (v1.size() > 0) {
				for (int j = 0; j < v1.size(); j++) {

					DeptDb deptDb = (DeptDb) v1.get(j);
					Vector va = new Vector();
					try {
						va = deptDb.getAllChild(va, deptDb);
					} catch (ErrMsgException e) {
						e.printStackTrace();
					}
					Vector va2 = new Vector();
					va2.add(deptDb);
					va2.addAll(va);
					Iterator it = va2.iterator();
					String sql = "";
					while (it.hasNext()) {
						DeptDb dept = (DeptDb) it.next();
						String deptCode = dept.getCode();
						sql += (sql.equals("") ? "" : ",")
								+ StrUtil.sqlstr(deptCode);
						/*
						 * Vector v = dud.list(deptCode);// 根据deptcode取得部门下用户 if
						 * (v.size() > 0) { //usernames = new String[v.size()];
						 * for (int i=0;i<v.size();i++) { DeptUserDb ud =
						 * (DeptUserDb)v.get(i); v2.add(ud.getUserName()); } }
						 */
					}
					if (!sql.equals("")) {
						sql = "select distinct(u.name) from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in ("
								+ sql
								+ ") order by u.orders desc";
						JdbcTemplate jt = new JdbcTemplate();
						int i = 0;
						try {
							ResultIterator ri = jt.executeQuery(sql);
							usernames = new String[ri.getRows()];
							while (ri.hasNext()) {
								ResultRecord rr = (ResultRecord) ri.next();
								// v2.add(rr.getString(1));
								usernames[i++] = rr.getString(1);
							}
						} catch (SQLException e) {
							LogUtil.getLog(getClass()).error(
									"createNoticeReply:" + StrUtil.trace(e));
							e.printStackTrace();
						} finally {
							jt.close();
						}
					}
				}
			}
			// removeDuplicateWithOrder(v2);
			// usernames = v2.toArray(new String[]{});

		} else if (2 == noticeDb.getIsall()) // 系统管理员选择全部人员 取出当前单位及其子单位的全部用户
		{
			/*
			 * UserGroupDb ugd = new UserGroupDb(); ugd =
			 * ugd.getUserGroupDb(UserGroupDb.EVERYONE); Vector v =
			 * ugd.getAllUserOfGroup(); if (v.size() > 0) { usernames = new
			 * String[v.size()]; for (int i=0;i<v.size();i++) { UserDb ud =
			 * (UserDb)v.get(i); usernames[i] = ud.getName(); } }
			 */
			DeptDb dd = new DeptDb(noticeDb.getUnitCode());
			Vector v = new Vector();
			try {
				v = dd.getAllChild(v, dd);
			} catch (ErrMsgException e) {
				e.printStackTrace();
			}
			Vector va2 = new Vector();
			va2.add(dd);
			va2.addAll(v);
			Iterator it = va2.iterator();
			// Vector uva = new Vector();
			String sql = "";
			while (it.hasNext()) {
				DeptDb dept = (DeptDb) it.next();
				if (dept.getType() == DeptDb.TYPE_UNIT) {
					String deptCode = dept.getCode();
					sql += (sql.equals("") ? "" : ",")
							+ StrUtil.sqlstr(deptCode);
				}
				// UserDb ud = new UserDb();
				// Vector uv = ud.listUserOfUnit(dept.getCode());
				// uva.addAll(uv);
			}
			if (!sql.equals("")) {
				sql = "select distinct(name) from users where isValid=1 and unit_code in ("
						+ sql + ") order by orders desc";
				JdbcTemplate jt = new JdbcTemplate();
				int i = 0;
				try {
					ResultIterator ri = jt.executeQuery(sql);
					usernames = new String[ri.getRows()];
					while (ri.hasNext()) {
						ResultRecord rr = (ResultRecord) ri.next();
						// v2.add(rr.getString(1));
						usernames[i++] = rr.getString(1);
					}
				} catch (SQLException e) {
					LogUtil.getLog(getClass()).error(
							"createNoticeReply:" + StrUtil.trace(e));
					e.printStackTrace();
				} finally {
					jt.close();
				}
			}
			/*
			 * if (uva.size() > 0) { usernames = new String[uva.size()];
			 * Iterator uit = uva.iterator(); int i = 0; while (uit.hasNext()) {
			 * UserDb ud = (UserDb) uit.next(); usernames[i++] = ud.getName(); }
			 * }
			 */
		} else {
			return;
		}

		if (usernames == null || usernames.length == 0) {
			return;
		}

		// String[] usernames = StrUtil.split(strUsers, ",");
		boolean re = true;
    	NoticeReplyDb brd = new NoticeReplyDb();
    	int count = brd.createBatch(noticeDb.getId(), usernames);
    	if (count>0) {
    		re = true;
			MessageDb mdb = new MessageDb();
			try {
				mdb.sendSysMsgNotice(noticeDb.getId(), usernames, "请注意查看：通知公告 "+noticeDb.getTitle(), noticeDb.getContent());
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				re = false;
			}
			
			// if (isToMobile) {
				if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
	                IMsgUtil imu = SMSFactory.getMsgUtil();
	                try {
	                	String txt = StrUtil.getAbstract(null, noticeDb.getContent(), 380, "", false);
						imu.sendBatch(usernames, txt, noticeDb.getUserName());
					} catch (ErrMsgException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			// }			
    	}
	}
	
	public String getUserList(int flowId){
		String userList = "";
		String sql = "select deptNames from form_table_tzgg where flowId="+flowId;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		try {
			ri = jt.executeQuery(sql);
			if(ri.hasNext()){
				rd = (ResultRecord)ri.next();
				userList = rd.getString(1);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return userList;
	}
	
	public boolean saveNotice(HttpServletRequest request) throws ErrMsgException{
		String id = ParamUtil.get(request,"id");
        long idLong = Long.valueOf(id).longValue();
        String title = ParamUtil.get(request,"title");
        String content = ParamUtil.get(request,"content");
        int isall = ParamUtil.getInt(request, "isall",-1);
        String unitCode = ParamUtil.get(request,"unitCode");
        String deptNames = ParamUtil.get(request,"deptNames");
        String endDate = ParamUtil.get(request, "endDate");
        
        String color = ParamUtil.get(request, "color");
        
        NoticeDb ndb = new NoticeDb();
        //ndb.setId(idLong);
        ndb = ndb.getNoticeDb(idLong);
        ndb.setTitle(title);
        ndb.setContent(content);
        ndb.setColor(color);
        if (!endDate.equals("")) {
        	ndb.setEndDate(DateUtil.parse(endDate, "yyyy-MM-dd"));
        }
        boolean res = ndb.save();
        if(res){
        	changeNoticeStatus(idLong);
        	/**
        	String[] usernames = null;
    		if (isall == 0 ){// 选择特定用户的情况 取出userlist中的用户
    			usernames = StrUtil.split(getNoticeUsers(idLong), ",");
    			
    		}
    		if (isall == 1 ){// 单位管理员选择全部人员 取出单位下全部用户
    			String userName = ndb.getUserName();
    			DeptUserDb dud = new DeptUserDb();
    			Vector v1 = dud.getDeptsOfUser(userName); // 取得用户所在部门的deptcode
    			ArrayList<String> v2 = new ArrayList<String>();
    			//v2.add(userName);
    			if (v1.size() > 0)
    			{
    				for(int j=0;j<v1.size();j++)
    				{
    					
    					DeptDb deptDb = (DeptDb)v1.get(j);
    					String deptCode = deptDb.getCode();
    					Vector v = dud.list(deptCode);// 根据deptcode取得部门下用户
    					if (v.size() > 0)
    					{
    						//usernames = new String[v.size()];
    						for (int i=0;i<v.size();i++)
    						{
    							DeptUserDb ud = (DeptUserDb)v.get(i);
    						    v2.add(ud.getUserName());
    						}
    					}
    				}
    			}
    			removeDuplicateWithOrder(v2);
    			usernames = v2.toArray(new String[]{});
    		}
    		if (isall == 2 ){// 系统管理员选择全部人员 取出全部用户
    			UserGroupDb ugd = new UserGroupDb();
    			ugd = ugd.getUserGroupDb(UserGroupDb.EVERYONE);
    			Vector v = ugd.getAllUserOfGroup();
    			if (v.size() > 0) {
    				usernames = new String[v.size()];
    				for (int i = 0; i < v.size(); i++) {
    					UserDb ud = (UserDb) v.get(i);
    					usernames[i] = ud.getName();
    				}
    			}
    		} 

    		for (int i = 0; i < usernames.length; i++) {
				MessageDb mdb = new MessageDb();
				mdb.sendSysMsg(usernames[i], "请注意查看：通知公告 "+title, content);
    		}
    		return true;*/
        }
		return true;
	}
	public boolean canEditNotice(long id,String uName){
		String sql = "select id from oa_notice where id="+id+" and user_name="+StrUtil.sqlstr(uName);
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql);
			if(ri.hasNext()){
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean changeNoticeStatus(long id) {
		boolean res = false;
		String sql = "update oa_notice_reply set is_readed=0 where notice_id=?";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			res = jt.executeUpdate(sql, new Object[]{id})>0?true:false;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return res;
	}
	
	public String getNoticeUsers(long idss) {
		String usersStr = "";
		String sql = "select user_name from oa_notice_reply where id="+idss;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		try {
			ri = jt.executeQuery(sql);
			if(ri.hasNext()){
				rd = (ResultRecord)ri.next();
				String name = rd.getString(1);
				if("".equals(usersStr)){
					usersStr = name;
				}else{
					usersStr += ","+name;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return usersStr;
	}
}

