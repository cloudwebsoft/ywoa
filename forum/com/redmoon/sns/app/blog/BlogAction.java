package com.redmoon.sns.app.blog;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.blog.MusicDb;
import com.redmoon.blog.VideoDb;
import com.redmoon.blog.photo.PhotoDb;
import com.redmoon.forum.BoardRenderDb;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.SequenceMgr;
import com.redmoon.forum.person.UserPropDb;
import com.redmoon.forum.plugin.base.IPluginRender;
import com.redmoon.sns.ActionDb;
import com.redmoon.sns.app.base.*;
import com.redmoon.sns.ui.SkinMgr;

public class BlogAction extends AppAction {
	public static final int ACTION_BLOG = 1;
	public static final int ACTION_PHOTO = 2;
	public static final int ACTION_MUSIC = 3;
	public static final int ACTION_VIDEO = 4;
	
	public boolean log(String ownerId, String userName, int action, long actionId) {
		ActionDb ad = new ActionDb();
		long id = SequenceMgr.nextID(SequenceMgr.SNS_APP_ACTION);
		boolean re = false;
		try {
			re = ad.create(new JdbcTemplate(), new Object[]{new Long(id), ownerId, userName,BlogUnit.code,new Integer(action),new Long(actionId),new java.util.Date()});
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		// 更新sq_user_prop中的用户最后发布的action，以便于在用户中心中对action进行排序
		UserPropDb upd = new UserPropDb();
		upd = upd.getUserPropDb(userName);
		upd.set("app_action_id", new Long(id));
		try {
			upd.save();
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		
		return re;
	}
	
	public String getIcon(HttpServletRequest request, ActionDb ad) {
		return request.getContextPath() + "/user/" + SkinMgr.getSkinPath(request) + "/images/app_blog.gif";
	}
	
	public String getTitle(ActionDb ad) {
		int action = ad.getInt("app_action");
		if (action==ACTION_BLOG) {
			long id = ad.getLong("action_id");
			MsgDb md = new MsgDb();
			md = md.getMsgDb(id);
			return "更新了日志：" + md.getTitle();
		}
		else if (action==ACTION_PHOTO) {
			// long id = ad.getLong("action_id");
			return "发布了照片";
		}
		else if (action==ACTION_MUSIC) {
			return "发布了歌曲";
		}
		else if (action==ACTION_VIDEO) {
			return "发布了视频";
		}
		else {
			return "";
		}
	}
	
	public String getAbstract(HttpServletRequest request, ActionDb ad) {
		int action = ad.getInt("app_action");
		if (action==ACTION_BLOG) {
			long id = ad.getLong("action_id");
			MsgDb md = new MsgDb();
			md = md.getMsgDb(id);
			
			BoardRenderDb boardRender = new BoardRenderDb();
			boardRender = boardRender.getBoardRenderDb(md.getboardcode());
			IPluginRender render = boardRender.getRender();
			return StrUtil.getAbstract(request, render.RenderContent(request, md), 200);
		}
		else if (action==ACTION_PHOTO) {
			long id = ad.getLong("action_id");
			PhotoDb pd = new PhotoDb();
			pd = pd.getPhotoDb(id);
	        String attachmentBasePath = request.getContextPath() + "/upfile/" +
	                                    PhotoDb.photoBasePath + "/";
	        String block = "<a href=\"" + attachmentBasePath + pd.getImage() +
	                       "\" target=_blank title=\"点击在新窗口中打开\"><img border=0 src=\"" +
	                       attachmentBasePath + pd.getImage() + "\" onload=\"javascript:if(this.width>screen.width*0.4) this.width=screen.width*0.4\"></a>";
	        
			return block;
		}
		else if (action==ACTION_MUSIC) {
			long id = ad.getLong("action_id");
			MusicDb mud = new MusicDb();
			mud = (MusicDb)mud.getQObjectDb(new Long(id));
	        if (mud==null) {
	            return SkinUtil.LoadString(request, "err_id");
	        }			
			String link = StrUtil.getNullStr(mud.getString("link"));
			
	        StringBuffer block = new StringBuffer(200);
	        block.append("<object classid='clsid:22D6F312-B0F6-11D0-94AB-0080C74C7E95' id='MediaPlayer1' width='80%' height='68'>");
	        block.append("<param name='AudioStream' value='-1'>");
	        block.append("<param name='AutoSize' value='0'>");
	        block.append("<param name='AutoStart' value='0'>");
	        block.append("<param name='AnimationAtStart' value='-1'>");
	        block.append("<param name='AllowScan' value='-1'>");
	        block.append("<param name='AllowChangeDisplaySize' value='-1'>");
	        block.append("<param name='AutoRewind' value='0'>");
	        block.append("<param name='BufferingTime' value='5'>");
	        block.append("<param name='ClickToPlay' value='-1'>");
	        block.append("<param name='CursorType' value='0'>");
	        block.append("<param name='CurrentPosition' value='-1'>");
	        block.append("<param name='CurrentMarker' value='0'>");
	        block.append("<param name='DisplayBackColor' value='0'>");
	        block.append("<param name='DisplayForeColor' value='16777215'>");
	        block.append("<param name='DisplayMode' value='0'>");
	        block.append("<param name='DisplaySize' value='2'>");
	        block.append("<param name='Enabled' value='-1'>");
	        block.append("<param name='EnableContextMenu' value='-1'>");
	        block.append("<param name='EnablePositionControls' value='-1'>");
	        block.append("<param name='EnableFullScreenControls' value='0'>");
	        block.append("<param name='EnableTracker' value='-1'>");
	        block.append("<param name='Filename' value='" +
	                     (link.equals("") ? mud.getMusicUrl(request) : link) + "'>");
	        block.append("<param name='InvokeURLs' value='-1'>");
	        block.append("<param name='Language' value='-1'>");
	        block.append("<param name='PlayCount' value='1'>");
	        block.append("<param name='PreviewMode' value='0'>");
	        block.append("<param name='Rate' value='1'>");
	        block.append("<param name='SelectionStart' value='-1'>");
	        block.append("<param name='SelectionEnd' value='-1'>");
	        block.append("<param name='SendOpenStateChangeEvents' value='-1'>");
	        block.append("<param name='SendWarningEvents' value='-1'>");
	        block.append("<param name='SendErrorEvents' value='-1'>");
	        block.append("<param name='SendKeyboardEvents' value='0'>");
	        block.append("<param name='SendMouseClickEvents' value='0'>");
	        block.append("<param name='SendMouseMoveEvents' value='0'>");
	        block.append("<param name='SendPlayStateChangeEvents' value='-1'>");
	        block.append("<param name='ShowCaptioning' value='0'>");
	        block.append("<param name='ShowControls' value='-1'>");
	        block.append("<param name='ShowAudioControls' value='-1'>");
	        block.append("<param name='ShowDisplay' value='0'>");
	        block.append("<param name='ShowGotoBar' value='0'>");
	        block.append("<param name='ShowPositionControls' value='-1'>");
	        block.append("<param name='ShowStatusBar' value='-1'>");
	        block.append("<param name='ShowTracker' value='-1'>");
	        block.append("<param name='TransparentAtStart' value='0'>");
	        block.append("<param name='VideoBorderWidth' value='0'>");
	        block.append("<param name='VideoBorderColor' value='0'>");
	        block.append("<param name='VideoBorder3D' value='0'>");
	        block.append("<param name='Volume' value='-40'>");
	        block.append("<param name='WindowlessVideo' value='0'>");
	        block.append("</object>");

			return block.toString();
		}
		else if (action==ACTION_VIDEO) {
	        VideoDb mud = new VideoDb();
	        mud = (VideoDb) mud.getQObjectDb(new Long(ad.getLong("action_id")));
	        if (mud==null) {
	            return SkinUtil.LoadString(request, "err_id");
	        }
	        String link = StrUtil.getNullStr(mud.getString("link"));

	        StringBuffer block = new StringBuffer(200);
	        block.append("<object classid='clsid:22D6F312-B0F6-11D0-94AB-0080C74C7E95' id='MediaPlayer1' width='90%' height='320'>");
	        block.append("<param name='AudioStream' value='-1'>");
	        block.append("<param name='AutoSize' value='0'>");
	        block.append("<param name='AutoStart' value='0'>");
	        block.append("<param name='AnimationAtStart' value='-1'>");
	        block.append("<param name='AllowScan' value='-1'>");
	        block.append("<param name='AllowChangeDisplaySize' value='-1'>");
	        block.append("<param name='AutoRewind' value='0'>");
	        block.append("<param name='BufferingTime' value='5'>");
	        block.append("<param name='ClickToPlay' value='-1'>");
	        block.append("<param name='CursorType' value='0'>");
	        block.append("<param name='CurrentPosition' value='-1'>");
	        block.append("<param name='CurrentMarker' value='0'>");
	        block.append("<param name='DisplayBackColor' value='0'>");
	        block.append("<param name='DisplayForeColor' value='16777215'>");
	        block.append("<param name='DisplayMode' value='0'>");
	        block.append("<param name='DisplaySize' value='2'>");
	        block.append("<param name='Enabled' value='-1'>");
	        block.append("<param name='EnableContextMenu' value='-1'>");
	        block.append("<param name='EnablePositionControls' value='-1'>");
	        block.append("<param name='EnableFullScreenControls' value='0'>");
	        block.append("<param name='EnableTracker' value='-1'>");
	        block.append("<param name='Filename' value='" +
	                     (link.equals("") ? mud.getVideoUrl(request) : link) + "'>");
	        block.append("<param name='InvokeURLs' value='-1'>");
	        block.append("<param name='Language' value='-1'>");
	        block.append("<param name='PlayCount' value='1'>");
	        block.append("<param name='PreviewMode' value='0'>");
	        block.append("<param name='Rate' value='1'>");
	        block.append("<param name='SelectionStart' value='-1'>");
	        block.append("<param name='SelectionEnd' value='-1'>");
	        block.append("<param name='SendOpenStateChangeEvents' value='-1'>");
	        block.append("<param name='SendWarningEvents' value='-1'>");
	        block.append("<param name='SendErrorEvents' value='-1'>");
	        block.append("<param name='SendKeyboardEvents' value='0'>");
	        block.append("<param name='SendMouseClickEvents' value='0'>");
	        block.append("<param name='SendMouseMoveEvents' value='0'>");
	        block.append("<param name='SendPlayStateChangeEvents' value='-1'>");
	        block.append("<param name='ShowCaptioning' value='0'>");
	        block.append("<param name='ShowControls' value='-1'>");
	        block.append("<param name='ShowAudioControls' value='-1'>");
	        block.append("<param name='ShowDisplay' value='0'>");
	        block.append("<param name='ShowGotoBar' value='0'>");
	        block.append("<param name='ShowPositionControls' value='-1'>");
	        block.append("<param name='ShowStatusBar' value='-1'>");
	        block.append("<param name='ShowTracker' value='-1'>");
	        block.append("<param name='TransparentAtStart' value='0'>");
	        block.append("<param name='VideoBorderWidth' value='0'>");
	        block.append("<param name='VideoBorderColor' value='0'>");
	        block.append("<param name='VideoBorder3D' value='0'>");
	        block.append("<param name='Volume' value='-40'>");
	        block.append("<param name='WindowlessVideo' value='0'>");
	        block.append("</object>");			
			return block.toString();
		}		
		else {
			return "";
		}
	}
	
	public String getOperate(HttpServletRequest request, ActionDb ad) {
		// 回复、评论、投票、分享给好友、赞一个
		// 我也来测测，查看测试详情
		// 转载
		int action = ad.getInt("app_action");
		if (action==ACTION_BLOG) {
			StringBuffer buf = new StringBuffer();
			String rootPath = Global.getRootPath();
			String isJsWrited = (String)request.getAttribute("isJsWrited_" + BlogUnit.code);
			// System.out.println(getClass() + " " + isJsWrited);
			if (isJsWrited==null) {
				buf.append("<script src='" + rootPath + "/user/blog_action.js'></script>");
				request.setAttribute("isJsWrited_" + BlogUnit.code, "true");
			}
			// buf.append("<a href='javascript:;' onclick=\"blogReply('" + ad.getLong("id") + "','" + ad.getLong("action_id") + "')\">回复</a>";
			buf.append("&nbsp;&nbsp;<a href='" + rootPath + "/blog/showblog.jsp?rootid=" + ad.getLong("action_id")  + "' target='_blank'>查看全文</a>");
			return buf.toString();
		}
		else if (action==ACTION_PHOTO) {
			String str = "<a href='../blog/showphoto.jsp?photoId=" + ad.getLong("action_id") + "' target='_blank'>回复</a>";
			return str;
		}
		else if (action==ACTION_MUSIC) {
			String str = "<a href='../blog/showmusic.jsp?musicId=" + ad.getLong("action_id") + "' target='_blank'>回复</a>";
			return str;
		}
		else if (action==ACTION_VIDEO) {
			String str = "<a href='../blog/showvideo.jsp?videoId=" + ad.getLong("action_id") + "' target='_blank'>回复</a>";
			return str;
		}			
		else
			return "";
	}
}
