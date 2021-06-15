package com.redmoon.oa.project;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.fileark.Leaf;
import com.redmoon.oa.fileark.LeafPriv;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.IModuleChecker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import java.util.Vector;

public class ProjectChecker implements IModuleChecker {
	public static String CODE_PREFIX = "cws_prj_";

	@Override
    public boolean validateUpdate(HttpServletRequest request, FileUpload fu,
                                  FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {
		return true;
	}

	@Override
	public boolean validateCreate(HttpServletRequest request, FileUpload fu,
								  Vector fields) throws ErrMsgException {
		/*
		 * Iterator ir = fields.iterator(); while (ir.hasNext()) { FormField ff
		 * = (FormField)ir.next(); if (ff.getName().equals("name")) { Leaf lf =
		 * new Leaf();
		 * 
		 * lf = lf.getLeaf("prj_" +
		 * com.cloudwebsoft.framework.util.Cn2Spell.converterToFirstSpell
		 * (ff.getValue())); if (lf!=null && lf.isLoaded()) { throw new
		 * ErrMsgException("已存在同样编码的文件柜目录！"); } break; } }
		 */

		return true;
	}

	@Override
	public boolean validateDel(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		return true;
	}

	@Override
	public boolean onDel(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		// 删除文件柜目录
		String code = CODE_PREFIX + fdao.getId();
		Leaf lf = new Leaf();
		lf = lf.getLeaf(code);
		if (lf != null)
			lf.del(lf);

		return true;
	}

	@Override
	public boolean onCreate(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		// 创建文件柜中的目录
		Leaf plf = new Leaf();
		plf = plf.getLeaf(Leaf.CODE_PROJECT);

		if (plf == null || !plf.isLoaded()) {
			Leaf leaf = new Leaf(Leaf.ROOTCODE);
			plf = new Leaf();
			plf.setCode(Leaf.CODE_PROJECT);
			plf.setName("项目");
			plf.setParentCode(Leaf.ROOTCODE);
			plf.setType(Leaf.TYPE_LIST);
			plf.setIsHome(true);
			plf.setSystem(true);
			plf.setShow(true);
			leaf.AddChild(plf);
		}

		String name = fdao.getFieldValue("name");

		// 创建文件柜目录
		String code = CODE_PREFIX + fdao.getId(); // com.cloudwebsoft.framework.util.Cn2Spell.converterToFirstSpell(name);

		String description = name;

		int type = Leaf.TYPE_LIST;
		String pluginCode = "";
		boolean isSystem = false;
		boolean isHome = true;
		String target = "";

		Leaf lf = new Leaf();
		lf = lf.getLeaf(code);
		if (lf != null && lf.isLoaded()) {
			throw new ErrMsgException("已存在相同编码的节点：" + lf.getName());
		}

		lf = new Leaf();
		lf.setName(name);
		lf.setCode(code);
		lf.setParentCode(plf.getCode());
		lf.setDescription(description);
		lf.setType(type);
		lf.setPluginCode(pluginCode);
		lf.setSystem(isSystem);
		lf.setIsHome(isHome);
		lf.setTarget(target);

		boolean re = plf.AddChild(lf);

		if (re) {
			// 删除默认创建的赋予该目录的所有人可以浏览的权限
			LeafPriv lp = new LeafPriv(code);
			lp.delPrivsOfDir();
		}

		return re;
	}

	@Override
	public boolean onNestTableCtlAdd(HttpServletRequest request,
									 HttpServletResponse response, JspWriter out) {
		return false;
	}

	@Override
	public boolean onUpdate(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		return true;
	}
}
