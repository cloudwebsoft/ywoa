package com.redmoon.clouddisk.db;

import java.sql.SQLException;
import java.util.ArrayList;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.bean.SideBean;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.netdisk.SideBarMgr;

/**
 * @Description:
 * @author: 郝炜
 * @Date: 2015-4-2上午09:49:16
 */
public class SideDb {
	public final static byte MOD_NONE = 0x00;
	public final static byte MOD_PIC = 0x01;
	public final static byte MOD_COUNT = 0x02;
	public final static byte MOD_OTHER = 0x03;

	public final static byte IS_CHECK = 0x01;

	private SideBean sideBean;

	/**
	 * @param SideBean
	 */
	public SideDb(SideBean sideBean) {
		this.sideBean = sideBean;
	}

	public void checkSidebar() {
		SideBarMgr sbMgr = new SideBarMgr();
		try {
			// 检测初始化
			sbMgr.initialization(sideBean.getUserName());
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}

		sideBean.setModFlag(MOD_NONE);

		// 取得待办流程的条数
		int flowWaitCount = WorkflowDb.getWaitCount(sideBean.getUserName());

		// 客户端保存的待办流程条数与服务端不一致,则客户端需更新
		if (flowWaitCount != sideBean.getFlowCount()) {
			sideBean.setFlowCount(flowWaitCount);
			sideBean.setModFlag(MOD_COUNT);
		}

		// 内部邮件的数目
		MessageDb md = new MessageDb();
		int msgNewCount = md.getNewInnerMsgCount(sideBean.getUserName());

		// 客户端保存的内部邮件条数与服务端不一致,则客户端需更新
		if (msgNewCount != sideBean.getMsgCount()) {
			sideBean.setMsgCount(msgNewCount);
			sideBean.setModFlag(MOD_COUNT);
		}

		if (sideBean.getIsCheck() == IS_CHECK) {
			// 获取侧边栏自定义的修改信息
			int modFlag = sbMgr.getSideBarEdit(sideBean.getUserName());

			if (modFlag == MOD_PIC) {
				// 获取需要更新的图片列表
				ArrayList<String> list = sbMgr.getPicture(sideBean
						.getUserName(), false);
				if (list.size() > 0) {
					sideBean.setList(list);
					sideBean.setModFlag(MOD_PIC);
				} else {
					sideBean.setModFlag(MOD_COUNT);
				}
			} else if (sideBean.getModFlag() != MOD_COUNT) {
				if (modFlag == MOD_COUNT) {
					sideBean.setModFlag(MOD_OTHER);
				}
			}

			// 重置所有的mod_flag为0
			sbMgr.returnFlag(sideBean.getUserName());
		} else {
			// 获取需要更新的图片列表
			ArrayList<String> list = sbMgr.getPicture(sideBean.getUserName(),
					true);
			if (list.size() > 0) {
				sideBean.setList(list);
				sideBean.setModFlag(MOD_PIC);
			} else {
				sideBean.setModFlag(MOD_COUNT);
			}
		}
	}
}
