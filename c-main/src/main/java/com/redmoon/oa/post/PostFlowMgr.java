package com.redmoon.oa.post;

import java.sql.SQLException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * @Description:
 * @author:
 * @Date: 2016-2-23下午03:13:09
 */

public class PostFlowMgr {
	private int id;
	private int postId;
	private String flowCode;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPostId() {
		return postId;
	}

	public void setPostId(int postId) {
		this.postId = postId;
	}

	public String getFlowCode() {
		return flowCode;
	}

	public void setFlowCode(String flowCode) {
		this.flowCode = flowCode;
	}

	public Vector listByPostId() {
		
		String sql = "select id from post_flow where post_id=? order by is_related desc";
		PostFlowDb pfDb = new PostFlowDb();
		
		
		return pfDb.list(sql, new Object[] { postId });
	}
	
	public boolean addPostFlow(HttpServletRequest request) throws ErrMsgException, ResKeyException {
		if (!com.redmoon.oa.kernel.License.getInstance().canUseSolution(com.redmoon.oa.kernel.License.SOLUTION_PERFORMANCE)) {
			throw new ErrMsgException(com.redmoon.oa.kernel.License.getInstance().getLicenseStr());
		}
		
		int postId = ParamUtil.getInt(request, "post_id", 0);
		String flowCode = ParamUtil.get(request, "flow_code");
		
		setPostId(postId);
		setFlowCode(flowCode);
		if (isExist()) {
			throw new ErrMsgException("操作失败，该流程已存在！");
		}
		PostFlowDb pfd = new PostFlowDb();
		return pfd.create(new JdbcTemplate(), new Object[]{postId, flowCode, 0});		
	}

	public boolean isExist() {
		String sql = "select id from post_flow where post_id=? and flow_code=?";
		PostFlowDb pfDb = new PostFlowDb();
		Vector v = pfDb.list(sql, new Object[] { postId, flowCode });
		Iterator it = v.iterator();
		if (it.hasNext()) {
			PostFlowDb db = (PostFlowDb) it.next();
			id = db.getInt("id");
			return true;
		}
		return false;
	}

	public boolean delBatch(String ids) {
		String[] idAry = StrUtil.split(ids, ",");
		PostFlowDb pfDb = new PostFlowDb();
		boolean re = true;
		for (String idStr : idAry) {
			try {
				int id = StrUtil.toInt(idStr);
				pfDb = pfDb.getPostFlowDb(id);
				if (pfDb.isLoaded()) {
					re &= pfDb.del();
				}
			} catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			}
		}
		return re;
	}
	
	/**
	 * 根据 用户名 流程code 确定用户的岗位
	 * @Description: 
	 * @param userName
	 * @param flowCode
	 * @return
	 */
	public PostDb userPostByNameAndCode(String userName,String flowCode){
		PostDb postDb = null;
		String sql = "SELECT p.post_id FROM post_user p,post_flow f where p.post_id = f.post_id and f.flow_code = ? and p.user_name = ?";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql, new Object[] {flowCode, userName });
			if(ri.size()>0){
				ResultRecord rr = (ResultRecord)ri.next();
				int pos_id = rr.getInt(1);
				postDb = new PostDb();
				postDb = postDb.getPostDb(pos_id);
			}
			
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (jt != null) {
				jt.close();
			}
		}
		return postDb;
		
	}
	public ArrayList<String> listCanUserStartFlow(String userName) {
		String sql = "select flow_code from post_flow f,post_user u where f.post_id=u.post_id and user_name=? and is_related=1";
		JdbcTemplate jt = new JdbcTemplate();
		ArrayList<String> list = new ArrayList<String>();
		try {
			ResultIterator ri = jt.executeQuery(sql, new Object[] { userName });
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				list.add(rr.getString(1));
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (jt != null) {
				jt.close();
			}
		}
		return list;
	}
}
