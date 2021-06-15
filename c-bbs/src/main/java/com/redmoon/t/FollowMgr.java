package com.redmoon.t;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.Privilege;

public class FollowMgr {
	
	public static boolean isUserFollowT(HttpServletRequest request, long tid) {
		FollowDb fd = new FollowDb();
		return fd.isUserFollowT(tid, Privilege.getUser(request));
	}
	
	/**
	 * 关注/取消某微博
	 * @param request
	 * @param tid
	 * @param isFollow 是否关注
	 * @return
	 */
	public boolean followT(HttpServletRequest request, long tid, boolean isFollow) throws ErrMsgException {
		FollowDb fd = new FollowDb();
		boolean re = false;
		if (isFollow) {
			if (isUserFollowT(request, tid))
				throw new ErrMsgException("您已经关注了该微博！");
			TMgr tm = new TMgr();
			TDb tdb = tm.getTDb(tid);
			try {
				re = fd.create(new JdbcTemplate(), new Object[]{new Long(tid),Privilege.getUser(request),new java.util.Date(), tdb.getString("owner")});
			} catch (ResKeyException e) {
				e.printStackTrace();
			}
			if (re) {
				tdb.set("fans_count", new Integer(tdb.getInt("fans_count") + 1));
				try {
					tdb.save();
				} catch (ResKeyException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
				
				tdb = tm.getTDbOfUser(Privilege.getUser(request));
				tdb.set("follow_count", new Integer(tdb.getInt("follow_count") + 1));
				try {
					tdb.save();
				} catch (ResKeyException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}				
			}
		}
		else {
			re = fd.disFollow(tid, Privilege.getUser(request));
			if (re) {
				TMgr tm = new TMgr();
				TDb tdb = tm.getTDb(tid);
				tdb.set("fans_count", new Integer(tdb.getInt("fans_count") - 1));
				try {
					tdb.save();
				} catch (ResKeyException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
				
				tdb = tm.getTDbOfUser(Privilege.getUser(request));
				tdb.set("follow_count", new Integer(tdb.getInt("follow_count") - 1));
				try {
					tdb.save();
				} catch (ResKeyException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}				
			}
		}
		return re;
	}
}
