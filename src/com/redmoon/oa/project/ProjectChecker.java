package com.redmoon.oa.project;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import cn.js.fan.util.ErrMsgException;

import com.redmoon.forum.BoardEntranceDb;
import com.redmoon.forum.plugin.BoardDb;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.fileark.Leaf;
import com.redmoon.oa.fileark.LeafPriv;
import com.redmoon.oa.project.forum.ProjectUnit;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.IModuleChecker;

public class ProjectChecker implements IModuleChecker {
	public static String CODE_PREFIX = "cws_prj_";

	public boolean validateUpdate(HttpServletRequest request, FileUpload fu,
			FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {
		return true;
	}

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

	public boolean validateDel(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		return true;
	}

	public boolean onDel(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		// 删除文件柜目录
		String code = CODE_PREFIX + fdao.getId();
		Leaf lf = new Leaf();
		lf = lf.getLeaf(code);
		if (lf != null)
			lf.del(lf);

		// 删除版块,版块下对应的entrance会自动被删除
		com.redmoon.forum.Leaf leaf = new com.redmoon.forum.Leaf();
		leaf = leaf.getLeaf(code);
		if (leaf != null)
			leaf.delsingle(leaf);

		return true;
	}

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

		Config cfg = new Config();
		if (cfg.getBooleanProperty("project_with_forum")) {
			// 创建论坛中的版块
			com.redmoon.forum.Leaf leaf = new com.redmoon.forum.Leaf();
			leaf = leaf.getLeaf("project");

			if (leaf == null || !leaf.isLoaded()) {
				com.redmoon.forum.Leaf rootLeaf = new com.redmoon.forum.Leaf(
						"bgtd");
				leaf = new com.redmoon.forum.Leaf();
				leaf.setCode("project");
				leaf.setName("项目讨论");
				leaf.setParentCode("bgtd");
				leaf.setType(com.redmoon.forum.Leaf.TYPE_BOARD);
				leaf.setIsHome(false);
				leaf
						.setDisplayStyle(com.redmoon.forum.Leaf.DISPALY_STYLE_HORIZON);
				rootLeaf.AddChild(leaf);
			}

			com.redmoon.forum.Leaf boardlf = new com.redmoon.forum.Leaf();
			boardlf.setName(name);
			boardlf.setCode(code);
			boardlf.setType(com.redmoon.forum.Leaf.TYPE_BOARD);
			boardlf.setParentCode(leaf.getCode());
			boardlf.setDescription(description);
			boardlf.setLogo("");
			boardlf.setTheme("");
			boardlf.setSkin("");
			boardlf.setLocked(false);
			boardlf.setColor("");
			boardlf.setIsHome(true);
			boardlf
					.setWebeditAllowType(com.redmoon.forum.Leaf.WEBEDIT_ALLOW_TYPE_UBB_NORMAL_REDMOON);
			boardlf.setPlugin2Code("");
			boardlf.setCheckMsg(com.redmoon.forum.Leaf.CHECK_NOT);
			boardlf.setDelMode(com.redmoon.forum.Leaf.DEL_DUSTBIN);
			boardlf
					.setDisplayStyle(com.redmoon.forum.Leaf.DISPALY_STYLE_HORIZON);
			boardlf.setBold(false);

			re = leaf.AddChild(boardlf);
		}

		if (re) {
			// 挂上项目成员准入插件
			BoardEntranceDb be = new BoardEntranceDb();
			be.setBoardCode(code);
			be.setEntranceCode(ProjectUserEntrance.CODE);
			re = be.create();

			// 挂上项目管理插件
			BoardDb sb = new BoardDb();
			re = sb.create(ProjectUnit.code, code, "");
		}

		return re;
	}

	public boolean onNestTableCtlAdd(HttpServletRequest request,
			HttpServletResponse response, JspWriter out) {
		return false;
	}

	public boolean onUpdate(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		return true;
	}
}
