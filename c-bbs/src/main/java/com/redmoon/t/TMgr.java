package com.redmoon.t;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.Privilege;

public class TMgr {

	/**
	 * 获取用户的微博实例，如果用户的微博尚未初始化，则自动初始化
	 * @param userName
	 * @return
	 */
	public TDb getTDbOfUser(String userName) {
		TDb tdb = new TDb();
		return tdb.getTDb(userName, TDb.KIND_USER);
	}

	/**
	 * 初始化微博
	 * @param userName
	 * @return
	 */
	public boolean init(String userName) {
		boolean re = false;
		TDb tdb = new TDb();
		tdb = tdb.getTDb(userName, TDb.KIND_USER);
		// 如果用户的微博尚未初始化，则自动初始化
		if (tdb==null) {
			tdb = new TDb();
			try {
				re = tdb.create(new JdbcTemplate(), new Object[]{userName,new java.util.Date(),new Integer(TDb.KIND_USER)});
			}
			catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			}
		}
		return re;
	}


	/**
	 * 管理员批量操作
	 * @param request
	 * @throws ErrMsgException
	 */
	public void operateBatch(HttpServletRequest request, int status) throws ErrMsgException {
        if (!Privilege.isMasterLogin(request)) {
        	throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        }
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids==null)
            return;
        int len = ids.length;
        TMgr tm = new TMgr();
		for (int i = 0; i < len; i++) {
			try {
				TDb tdb = tm.getTDb(StrUtil.toLong(ids[i]));
				tdb.set("is_open", new Integer(status));
				tdb.save();
			} catch (ResKeyException e) {
				throw new ErrMsgException(e.getMessage(request));
			}
		}
    }

	/**
	 * 关闭本人微博
	 * @param userName
	 * @return
	 */
	public boolean close(HttpServletRequest request) throws ResKeyException {
		TMgr tm = new TMgr();
		TDb tdb = tm.getTDbOfUser(Privilege.getUser(request));
		tdb.set("is_open", new Integer(TDb.STATUS_CLOSE));
		return tdb.save();
	}

	/**
	 * 开通本人微博
	 * @param userName
	 * @return
	 */
	public boolean open(HttpServletRequest request) throws ErrMsgException, ResKeyException {
		TMgr tm = new TMgr();
		TDb tdb = tm.getTDbOfUser(Privilege.getUser(request));
		int isOpen = tdb.getInt("is_open");
		if (isOpen == TDb.STATUS_FORCE_CLOSE) {
			if (!Privilege.isMasterLogin(request))
				throw new ErrMsgException("您的微博已被管理员关闭，如需开通，请联系管理员！");
		}
		tdb.set("is_open", new Integer(TDb.STATUS_OPEN));
		return tdb.save();
	}

	public TDb getTDb(long id) {
		TDb tdb = new TDb();
		return tdb.getTDb(id);
	}

}
