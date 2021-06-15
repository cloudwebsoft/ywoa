package com.redmoon.oa.project;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.fileark.Leaf;
import com.redmoon.oa.fileark.LeafPriv;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormMgr;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.IModuleChecker;

public class ProjectMemberChecker implements IModuleChecker {

	@Override
	public boolean onCreate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
		// TODO Auto-generated method stub
		
		long prjId = StrUtil.toLong(fdao.getCwsId());
		// 如果是创建时的临时ID，当作为嵌套表2，与主表一起创建时
		if (prjId==FormDAO.TEMP_CWS_ID) {
			return true;
		}
		
        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb("project");
        if (fd==null || !fd.isLoaded()) {
        	throw new ErrMsgException("项目管理表单不存在！");
        }
		
        // com.redmoon.oa.visual.FormDAO fdaoPrj = new com.redmoon.oa.visual.FormDAO(fd);
        // fdaoPrj = fdaoPrj.getFormDAO(prjId, fd);
		
        String prjCode = ProjectChecker.CODE_PREFIX + prjId; // com.cloudwebsoft.framework.util.Cn2Spell.converterToFirstSpell(fdaoPrj.getFieldValue("name"));
        
        // 加入文件柜目录管理权限
    	LeafPriv lp = new LeafPriv(prjCode);
		String role = fdao.getFieldValue("prj_role");
		if (role.equals("manager")) {
        	lp.setAppend(1);
        	lp.setModify(1);
        	lp.setDel(1);
        	lp.setSee(1);
        	lp.setExamine(1);			
		}
		else {
        	lp.setAppend(1);
        	lp.setModify(1);
        	lp.setDel(0);
        	lp.setSee(1);
        	lp.setExamine(1);			
		}

       	lp.add(fdao.getFieldValue("prj_user"), LeafPriv.TYPE_USER);
		
		return true;
	}

	@Override
	public boolean onDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
		// TODO Auto-generated method stub
		long prjId = StrUtil.toLong(fdao.getCwsId());

		String prjCode = ProjectChecker.CODE_PREFIX + prjId;
		// 删除文件柜上的相应权限
		LeafPriv lp = new LeafPriv();
		lp = lp.getLeafPriv(prjCode, fdao.getFieldValue("prj_user"), LeafPriv.TYPE_USER);
		if (lp!=null) {
			lp.del();
		}
		
		return true;
	}
	
	public static boolean isUserExist(long projectId, String userName) {
		String sql = "select id from form_table_project_members where cws_id=? and prj_user=?";
    	JdbcTemplate jt = new JdbcTemplate();
    	try {
			ResultIterator ri = jt.executeQuery(sql, new Object[]{"" + projectId, userName});
			if (ri.hasNext()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean isUserManager(long projectId, String userName) {
		String sql = "select id from form_table_project_members where cws_id=? and prj_user=? and prj_role='manager'";
    	JdbcTemplate jt = new JdbcTemplate();
    	try {
			ResultIterator ri = jt.executeQuery(sql, new Object[]{"" + projectId, userName});
			if (ri.hasNext()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}	

	@Override
	public boolean validateCreate(HttpServletRequest request, FileUpload fu, Vector fields)
			throws ErrMsgException {
		// 检查是否已存在
		String projectId = fu.getFieldValue("cws_id");
		long prjId = StrUtil.toLong(projectId);

		if (prjId==FormDAO.TEMP_CWS_ID) {
			return true;
		}
		
		Iterator ir = fields.iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField) ir.next();
			if (ff.getName().equals("prj_user")) {
				String userName = ff.getValue();
				if (isUserExist(prjId, userName))
					throw new ErrMsgException("用户已存在！");

				break;
			}
		}
		return true;
	}

	@Override
	public boolean validateDel(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
    public boolean validateUpdate(HttpServletRequest request, FileUpload fu,
                                  FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {
		// TODO Auto-generated method stub
		return true;
	}

    @Override
	public boolean onNestTableCtlAdd(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
    	return false;
    }     
    
    @Override
	public boolean onUpdate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }       
}
