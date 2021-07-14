package com.redmoon.t;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.SequenceMgr;
import com.redmoon.forum.person.UserDb;
import com.redmoon.forum.person.UserMgr;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.kit.util.FileUploadExt;
import com.redmoon.sns.app.base.IAction;
import com.redmoon.sns.app.t.TAction;
import com.redmoon.sns.app.t.TUnit;
import java.util.regex.*;

public class TMsgMgr {
	
	public static String TAG_DEFAULT_WORDS = "输入话题标题";
	
	public TMsgDb getTMsgDb(long id) {
		TMsgDb tmd = new TMsgDb();
		return tmd.getTMsgDb(id);
	}

	public long create(HttpServletRequest request) throws ErrMsgException {
		if (!Privilege.isUserLogin(request)) {
			throw new ErrMsgException("请先登录！");
		}
		String userName = Privilege.getUser(request);
		long tid = ParamUtil.getLong(request, "tid");
		String content = ParamUtil.get(request, "content");
		String musicUrl = ParamUtil.get(request, "musicUrl");
		String videoUrl = ParamUtil.get(request, "videoUrl");
		long quoteId = ParamUtil.getLong(request, "quoteId", 0);
		if (content.equals("")) {
			if (musicUrl.equals("") && videoUrl.equals("") && quoteId==0) {
				throw new ErrMsgException("请填写内容！");
			}
		}
		long id = com.redmoon.forum.SequenceMgr.nextID(com.redmoon.forum.SequenceMgr.SQ_T_MSG);
		long replyId = 0;
		long replyTId = 0;
		int checkStatus = 1;
		long sourceId = 0;
		TMsgDb tmd = new TMsgDb();
		boolean re = false;
		try {
			re = tmd.create(new JdbcTemplate(), new Object[]{new Long(id), Privilege.getUser(request),content,new java.util.Date(),musicUrl,videoUrl,new Long(tid),new Long(quoteId), new Long(replyId), "", new Integer(checkStatus), new Long(sourceId), new Long(replyTId)});
		} catch (ResKeyException e1) {
			throw new ErrMsgException(e1.getMessage(request));
		}
		// 检查是否有上传的附件，将其关联
		if (re) {
			long attId = ParamUtil.getLong(request, "attId", -1);
			if (attId!=-1) {
				AttachmentDb att = new AttachmentDb();
				att = (AttachmentDb)att.getQObjectDb(new Long(attId));
				att.set("msg_id", new Long(id));
				try {
					att.save();
				} catch (ResKeyException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
			
			// 对@用户进行检查并记录
			parseAt(id, content);			
			
			// 对标签进行检查并记录
			parseTag(id, content, userName);
			
			// 置微博最后一个消息ID
			TDb tdb = new TDb();
			tdb=tdb.getTDb(tid);
			tdb.set("last_msg_id", new Long(id));
			tdb.set("msg_count",new Integer(tdb.getInt("msg_count")+1));
			try {
				tdb.save();
			} catch (ResKeyException e1) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e1));
			}
			
			// 判断SNS是否启用
			com.redmoon.sns.Config cfg = com.redmoon.sns.Config.getInstance();
			if (cfg.isOpen()) {
				TAction ta = new TAction();
				ta.log("" + tid, userName, IAction.ACTION_COMMON, id);
			}
		}
		else
			id = 0;
		return id;
	}

	/**
	 * 引用微博贴子
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean quote(HttpServletRequest request) throws ErrMsgException {
		long tid = ParamUtil.getLong(request, "tid");
		String content = ParamUtil.get(request, "content");
		//long replyId = ParamUtil.getLong(request, "replyId", 0);
		long quoteId = ParamUtil.getLong(request, "quoteId");
		
		long replyId = 0;
		long replyTId = 0;
		boolean isComment = ParamUtil.getInt(request, "isComment", 0)==1;
		if (isComment) {
			replyId = quoteId;
			TMsgDb reMsgDb = getTMsgDb(replyId);
			replyTId = reMsgDb.getLong("t_id");
		}
		
		String musicUrl = "";
		String videoUrl = "";
		int checkStatus = 1;
		long sourceId = 0;
		
		TMsgDb tmd = new TMsgDb();
		tmd = tmd.getTMsgDb(quoteId);
		tmd.set("quote_count", new Integer(tmd.getInt("quote_count")+1));
		// 置被回复贴的回复数
		if (isComment) {
			tmd.set("reply_count", new Integer(tmd.getInt("reply_count")+1));
		}
		try {
			tmd.save();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}
		
		String userName = Privilege.getUser(request);
		String atUserName = tmd.getString("user_name"); // 被引用贴的发布者
		boolean re = false;
		long id = com.redmoon.forum.SequenceMgr.nextID(com.redmoon.forum.SequenceMgr.SQ_T_MSG);		
		try {
			// 如果还用tmd.create,则tmd中的resultRecord将会被重写,tmd将会被冲掉
			TMsgDb newTmd = new TMsgDb();
			re = newTmd.create(new JdbcTemplate(), new Object[]{new Long(id), userName,content,new java.util.Date(),musicUrl,videoUrl,new Long(tid),new Long(quoteId), new Long(replyId), atUserName, new Integer(checkStatus), new Long(sourceId), new Long(replyTId)});
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}
		if (re) {
			// 置微博最后一个消息ID
			TDb tdb = new TDb();
			tdb = tdb.getTDb(tid);
			tdb.set("last_msg_id", new Long(id));
			try {
				tdb.save();
			} catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			}
			
			// 对@用户进行检查并记录
			parseAt(id, content);
			
			// 对标签进行检查并记录
			parseTag(id, content, userName);			
			
			// 判断SNS是否启用
			com.redmoon.sns.Config cfg = com.redmoon.sns.Config.getInstance();
			if (cfg.isOpen()) {
				TAction ta = new TAction();
				ta.log("" + tid, Privilege.getUser(request), IAction.ACTION_COMMON, id);
			}
		}
		return re;
	}

    /**
     * 往文章中插入图片
     * @param application ServletContext
     * @param request HttpServletRequest
     * @return String[] 图片的ID数组
     * @throws ErrMsgException
     */
    public String[] uploadImg(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        if (!Privilege.isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN));

        FileUploadExt fu = new FileUploadExt();
        String[] ext = new String[] {"jpg", "gif", "png", "bmp", "swf"};
        if (ext!=null)
            fu.setValidExtname(ext);

        int ret = 0;
        try {
            ret = fu.doUpload(application, request);
            if (ret!=FileUpload.RET_SUCCESS) {
                throw new ErrMsgException(fu.getErrMessage(request));
            }
            if (fu.getFiles().size()==0)
                throw new ErrMsgException("请上传文件！");
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }

        Calendar cal = Calendar.getInstance();
		String year = "" + (cal.get(Calendar.YEAR));
		String month = "" + (cal.get(Calendar.MONTH) + 1);
		String virtualpath = year + "/" + month;

        Config cfg = Config.getInstance();
        String attPath = cfg.getProperty("t.attachmentPath");

        String filepath = Global.getRealPath() + attPath + "/" +
                          virtualpath + "/";

        File f = new File(filepath);
        if (!f.isDirectory()) {
            f.mkdirs();
        }

        fu.setSavePath(filepath); // 设置保存的目录
        // logger.info(filepath);
        String[] re = null;
        Vector v = fu.getFiles();
        Iterator ir = v.iterator();

        String attachmentBasePath = request.getContextPath() + "/";

        if (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            // 保存至磁盘相应路径
            String fname = FileUpload.getRandName() + "." +
                           fi.getExt();
            // 记录于数据库
            AttachmentDb att = new AttachmentDb();
            boolean r = false;
            try {
            	r = att.create(new JdbcTemplate(), new Object[]{new Long(SequenceMgr.nextID(SequenceMgr.SQ_T_MSG_ATT)), new Long(0),virtualpath + "/" + fname,fi.getExt(),new java.util.Date(),Privilege.getUser(request)});
            }
            catch (ResKeyException e) {
            	LogUtil.getLog(getClass()).error(e.getMessage(request));
            }
            if (r) {
                fi.write(filepath, fname);
                re = new String[3];
                re[0] = "" + att.getLong("id");
                re[1] = attachmentBasePath + att.getString("path");
                re[2] = fi.uploadSerialNo;
            }
        }

        return re;
    }
    
    /**
     * 当删除微博时，删除微博中的所有贴子及回复
     * @param tid
     * @throws ResKeyException
     */
    public void delMsgsOfT(long tid) throws ResKeyException {
    	TMsgDb tmd = new TMsgDb();
    	String sql = "select id from " + tmd.getTable().getName() + " where t_id=?";
    	String sql2 = "select id from " + tmd.getTable().getName() + " where reply_id=? and t_id==0";
    	Iterator ir = tmd.list(sql, new Object[]{new Long(tid)}).iterator();
    	while (ir.hasNext()) {
    		tmd = (TMsgDb)ir.next();
    		tmd.del();
    		
    		// 删除回复
    		Iterator ir2 = tmd.list(sql2, new Object[]{new Long(tmd.getLong("id"))}).iterator();
    		while (ir2.hasNext()) {
    			tmd = (TMsgDb)ir2.next();
    			tmd.del();
    		}
    	}
    }
    
    public synchronized boolean del(HttpServletRequest request, long id) throws ErrMsgException {
    	TMsgDb tmd = new TMsgDb();
    	tmd = tmd.getTMsgDb(id);
    	
    	long tid = tmd.getLong("t_id");
    	long replyId = tmd.getLong("reply_id");
    	TMsgDb rootMsg = null;
    	if (replyId!=0) {
    		// 该消息为回复
    		rootMsg = tmd.getTMsgDb(tmd.getLong("reply_id"));    		
    	}
    	if (tid==0) {
    		if (rootMsg!=null)
    			tid = rootMsg.getLong("t_id");
    		else {
    			// 如果被回复的贴子已经不存在，则直接删除本贴
    			if (TPrivilege.canDel(request, tmd)) {
    				return del(tmd);
    			}
    		}
    	}
    	
    	if (TPrivilege.canDel(request, tmd)) {
    		boolean re = del(tmd);
    		// 如果是回复贴
    		if (replyId!=0) {
    			if (rootMsg!=null) {
	    			rootMsg.set("reply_count", new Integer(rootMsg.getInt("reply_count") -1));
	    			try {
	    				rootMsg.save();
					} catch (ResKeyException e) {
						// TODO Auto-generated catch block
						throw new ErrMsgException(e.getMessage(request));
					}
    			}
    		}
    		
    		if (re) {
    	    	TDb tdb = new TDb();    	
    	    	tdb = tdb.getTDb(tid);    			
    			// 检查是否为最后一条发布的信息
    			if (id==tdb.getLong("last_msg_id")) {
    				long lastMsgId = tmd.getLastMsgIdOfT(tmd.getLong("t_id"));
    				tdb.set("last_msg_id", new Long(lastMsgId));
    				try {
						tdb.save();
					} catch (ResKeyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// 删除微博的统计
					int count = tdb.getInt("msg_count");
					tdb.set("msg_count",new Integer(count-1));
					if(count<0)
					{
					  count =0;
					}
					try {
						tdb.save();
					} catch (ResKeyException e1) {
						LogUtil.getLog(getClass()).error(StrUtil.trace(e1));
					}			
    			}
    		}
    		return re;
    	}
    	else
    		throw new ErrMsgException("权限非法！");    	
    }
    
    public synchronized boolean del(HttpServletRequest request) throws ErrMsgException {
    	long id = ParamUtil.getLong(request, "msgId", -1);
    	if (id==-1)
    		throw new ErrMsgException("缺少标识!");
    	return del(request, id);
    }
    
    public boolean del(TMsgDb tmd) {
    	boolean re = false;
		try {
			re = tmd.del();
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
        if (re) {
			// 判断SNS是否启用
			com.redmoon.sns.Config cfg = com.redmoon.sns.Config.getInstance();
			if (cfg.isOpen()) {
				TAction ba = new TAction();
				ba.del(TUnit.code, IAction.ACTION_COMMON, tmd.getLong("id"));
			}
        }
        return re;
    }
	
	public void delBatch(HttpServletRequest request) throws ErrMsgException {
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids==null)
            return;
        int len = ids.length;
        for (int i=0; i<len; i++) {
			del(request, StrUtil.toLong(ids[i]));
        }
    }
    
    public static String render(HttpServletRequest request, TMsgDb msg) {
    	String content = StrUtil.ubb(request, msg.getString("content"), true, false);
    	
        String patternStr = "";
        Pattern pattern;
        Matcher matcher;

        patternStr = "\\[video=([^\\[]*?)\\](.[^\\[]*?)(\\[\\/video\\])";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        
        matcher = pattern.matcher(content);

        StringBuffer sb = new StringBuffer();
        boolean re = matcher.find();
        while (re) {
        	String site = matcher.group(1);
        	if (site.equals("youku")) {
	            String str = "<span id='showVideo" + msg.getLong("id") + "' class='showVideo' onclick=\"showVideo('" + msg.getLong("id") + "')\">视频分享&nbsp;↓</span><div id='videoBox" + msg.getLong("id") + "' class='videoBox'><embed src=\"$2\" quality=\"high\" width=\"480\" height=\"400\" align=\"middle\" allowScriptAccess=\"sameDomain\" type=\"application/x-shockwave-flash\"></embed></div>";

	            matcher.appendReplacement(sb, str);
        	}
        	else if (site.equals("tudou")) {
        		String str = "<span id='showVideo" + msg.getLong("id") + "' class='showVideo' onclick=\"showVideo('" + msg.getLong("id") + "')\">视频分享&nbsp;↓</span><div id='videoBox" + msg.getLong("id") + "' class='videoBox'><object width=\"480\" height=\"400\"><param name=\"movie\" value=\"$2\"></param><param name=\"allowFullScreen\" value=\"true\"></param><param name=\"allowscriptaccess\" value=\"always\"></param><param name=\"wmode\" value=\"opaque\"></param><embed src=\"$2\" type=\"application/x-shockwave-flash\" allowscriptaccess=\"always\" allowfullscreen=\"true\" wmode=\"opaque\" width=\"480\" height=\"400\"></embed></object></div>";
        		matcher.appendReplacement(sb, str);
        	}
            re = matcher.find();
        }
        matcher.appendTail(sb);
        
        content = sb.toString();
        
        patternStr = "\\[music\\](.[^\\[]*?)(\\[\\/music\\])";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        
        matcher = pattern.matcher(content);
        
		StringBuffer s = new StringBuffer();
		s.append("<object classid=\"clsid:22D6F312-B0F6-11D0-94AB-0080C74C7E95\" id=\"MediaPlayer1\" width=\"80%\" height=\"45\">");
		s.append("<param name=\"AutoStart\" value=\"-1\">");
		s.append("<param name=\"AnimationAtStart\" value=\"-1\">");
		s.append("<param name=\"AutoRewind\" value=\"0\">");
		s.append("<param name=\"DisplayBackColor\" value=\"0\">");
		s.append("<param name=\"DisplayForeColor\" value=\"16777215\">");
		s.append("<param name=\"DisplayMode\" value=\"0\">");
		s.append("<param name=\"DisplaySize\" value=\"2\">");
		s.append("<param name=\"Enabled\" value=\"-1\">");
		s.append("<param name=\"EnableContextMenu\" value=\"-1\">");
		s.append("<param name=\"EnablePositionControls\" value=\"-1\">");
		s.append("<param name=\"EnableFullScreenControls\" value=\"0\">");
		s.append("<param name=\"EnableTracker\" value=\"-1\">");
		s.append("<param name=\"Filename\" value=\"$1\">");
		s.append("<param name=\"Mute\" value=\"0\">");
		s.append("<param name=\"PlayCount\" value=\"1\">");
		s.append("<param name=\"PreviewMode\" value=\"0\">");
		s.append("<param name=\"Rate\" value=\"1\">");
		s.append("<param name=\"SAMILang\" value>");
		s.append("<param name=\"SAMIStyle\" value>");
		s.append("<param name=\"SAMIFileName\" value>");
		s.append("<param name=\"SelectionStart\" value=\"-1\">");
		s.append("<param name=\"SelectionEnd\" value=\"-1\">");
		s.append("<param name=\"SendOpenStateChangeEvents\" value=\"-1\">");
		s.append("<param name=\"SendWarningEvents\" value=\"-1\">");
		s.append("</object>");

        sb = new StringBuffer();
		re = matcher.find();
		while (re) {
			matcher.appendReplacement(sb, s.toString());
			re = matcher.find();
		}
        matcher.appendTail(sb);
        content = sb.toString();
        
        patternStr = "@(.*?)( )+";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);        
        matcher = pattern.matcher(content);
        sb = new StringBuffer();
		re = matcher.find();
		UserMgr um = new UserMgr();
		UserDb user;
		TDb tdb;
		TMgr tm = new TMgr();
		while (re) {
        	String nick = matcher.group(1);
        	user = um.getUserDbByNick(nick);
        	tdb = tm.getTDbOfUser(user.getName());
        	String str = "<a href='" + request.getContextPath() + "/user/t.jsp?tid=" + tdb.getLong("id") + "'>@" + nick + "</a>";
			matcher.appendReplacement(sb, str);
			re = matcher.find();
		}
        matcher.appendTail(sb);
        content = sb.toString();        
        
        
        patternStr = "#(.*?)#";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);        
        matcher = pattern.matcher(content);
        sb = new StringBuffer();
		re = matcher.find();
		while (re) {
        	String tagName = matcher.group(1);
        	String str = "<a href='" + request.getContextPath() + "/user/t_tag_msg.jsp?tid=" + msg.getLong("t_id") + "&tagName=" + StrUtil.UrlEncode(tagName) + "'>" + tagName + "</a>";
			matcher.appendReplacement(sb, str);
			re = matcher.find();
		}
        matcher.appendTail(sb);
        content = sb.toString();         
        
        return content;
    }
    
    public Vector parseAt(long msgId, String content) {
    	Vector v = new Vector();
        Pattern pattern;
        Matcher matcher;

        String patternStr = "@(.*?)( )+";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        
        matcher = pattern.matcher(content);

        boolean re = matcher.find();
        UserDb user = new UserDb();
        while (re) {
        	String nick = matcher.group(1);
        	
        	if (nick.equals(TAG_DEFAULT_WORDS))
        		continue;
        	
        	// 检查用户是否存在
        	user = user.getUserDbByNick(nick);
        	if (!user.isLoaded()) {
        		continue;
        	}
        	
            // 获取当前AT记录
            TAtDb tadb = new TAtDb();
            tadb = tadb.getTAtDb(user.getName());
            tadb.doAt(user.getName(), msgId);
            

        	v.addElement(user);

            re = matcher.find();
        }
        
        return v;
    }
    
    public void parseTag(long msgId, String content, String userName) {
    	Vector v = new Vector();
        Pattern pattern;
        Matcher matcher;
        String patternStr = "#(.*?)#";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        
        matcher = pattern.matcher(content);
        boolean re = matcher.find();
        while (re) {
        	v.addElement(matcher.group(1));
            re = matcher.find();
        }
        TagMsgDb tagmd = new TagMsgDb();
		tagmd.createForMsg(msgId, v, userName);
    }    

}
