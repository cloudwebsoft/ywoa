package com.redmoon.oa.ui.menu;

import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.redmoon.oa.basic.SelectKindPriv;
import com.redmoon.oa.flow.LeafPriv;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.UserSet;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.ui.Skin;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.visual.ModulePrivDb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.util.Iterator;
import java.util.Vector;

public class DirectoryView {
    Leaf rootLeaf;
    Vector UprightLineNodes = new Vector(); //用于显示竖线
    HttpServletRequest request;
    String skinPath;

    public DirectoryView(HttpServletRequest request, Leaf rootLeaf) {
        this.rootLeaf = rootLeaf;
        this.request = request;

        String skincode = UserSet.getSkin(request);
        if (skincode == null || skincode.equals("")) {
            skincode = UserSet.defaultSkin;
        }
        SkinMgr skm = new SkinMgr();
        Skin skin = skm.getSkin(skincode);
        skinPath = skin.getPath();
    }

    void ShowLeafAsOption(JspWriter out, Leaf leaf, int rootlayer)
                  throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        if (leaf.getChildCount()>0) {
            out.print("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "╋ " + name + "</option>");
        }
        else {
            out.print("<option value=\"" + code + "\" style='COLOR: #0005ff'>" + blank + "├『" + name +
                      "』</option>");
        }
    }

    public void ShowLeafAsOptionToString(StringBuffer sb, Leaf leaf, int rootlayer) {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        if (leaf.getChildCount()>0) {
            sb.append("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "╋ " + name + "</option>");
        }
        else {
            sb.append("<option value=\"" + code + "\" style='COLOR: #0005ff'>" + blank + "├『" + name +
                      "』</option>");
        }
    }

    // 显示根结点为leaf的树
    public void ShowDirectoryAsOptions(JspWriter out, Leaf leaf, int rootlayer) throws Exception {
        ShowLeafAsOption(out, leaf, rootlayer);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            ShowDirectoryAsOptions(out, childlf, rootlayer);
        }
    }

    // 显示根结点为leaf的树
    public void ShowDirectoryAsOptionsToString(StringBuffer sb, Leaf leaf, int rootlayer) {
        ShowLeafAsOptionToString(sb, leaf, rootlayer);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            ShowDirectoryAsOptionsToString(sb, childlf, rootlayer);
        }
    }

    /**
     * 把列表或无内容显示为蓝色
     * @param out JspWriter
     * @param leaf Leaf
     * @param rootlayer int
     * @throws Exception
     */
    void ShowLeafAsOptionWithCode(JspWriter out, Leaf leaf, int rootlayer)
                  throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        out.print("<option value='" + code + "'>" + blank + "╋ " + name + "</option>");
    }

    // 显示根结点为leaf的树，value中全为code
    public void ShowDirectoryAsOptionsWithCode(JspWriter out, Leaf leaf, int rootlayer) throws Exception {
        ShowLeafAsOptionWithCode(out, leaf, rootlayer);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            ShowDirectoryAsOptionsWithCode(out, childlf, rootlayer);
        }
    }

	/**
	 * 递归获得jsTree的json字符串
	 * 
	 * @param parentCode
	 *            父节点parentCode
	 * @return str
	 */
	private String getJson(Directory dir, String parentCode, String str)
			throws Exception {
		int i = 0;
		int j = 0;
		// 把顶层的查出来
		Vector children = dir.getChildren(parentCode);
        License lic = License.getInstance();
		int size = children.size();
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			i++;
			Leaf childlf = (Leaf) ri.next();
			String desc = childlf.getDescription();
			if (!"".equals(desc)) {
			    desc = "(" + desc + ")";
            }
			
			if ("-1".equals(parentCode)) {
				if (str.indexOf("{id:\"" + childlf.getCode()) == -1) {
					str += "{id:\"" + childlf.getCode() + "\",parent:\"#\",text:\""
					+ childlf.getName() + desc + "\",state:{opened:true}} ,";
				}
			} else {
				str += "{id:\"" + childlf.getCode() + "\",parent:\""
						+ childlf.getParentCode() + "\",text:\""
						+ childlf.getName() + desc + "\", isUse:\"" + childlf.isUse() + "\" },";
			}
			Vector childs = dir.getChildren(childlf.getCode());
			// 如果有子节点
			if (!childs.isEmpty()) {
				// 遍历它的子节点
				int size2 = childs.size();
				Iterator childri = childs.iterator();
				while (childri.hasNext()) {
					j++;
					Leaf child = (Leaf) childri.next();
                    desc = child.getDescription();
                    if (!"".equals(desc)) {
                        desc = "(" + desc + ")";
                    }

					if (child.getCode().equals(ConstUtil.MENU_ITEM_SALES)) {
				        if (!lic.isPlatformSrc()) {
                            continue;
                        }
				        // 平台版才可以用CRM模块，如果许可证中的解决方案中未勾选CRM模块
					    if (lic.isSolutionVer() && !lic.canUseSolution(License.SOLUTION_CRM)) {
					       	continue;
					    }
					}						
					
					str += "{id:\"" + child.getCode() + "\",parent:\""
							+ child.getParentCode() + "\",text:\""
							+ child.getName() + desc + "\" },";
					// 还有子节点(递归调用)
					Vector ch = dir.getChildren(child.getCode());
					if (!ch.isEmpty()) {
						str = getJson(dir, child.getCode(), str);
					}
				}
			}
		}
		return str;
	}
	
	public String getJsonString() throws Exception {
    	Directory dir = new Directory();
    	String str = "[";  
    	// 从根开始  
    	str = getJson(dir,"-1",str);  
    	str = str.substring(0,str.length()-1);
    	str += "]";  
    	return str;
    }

	public boolean canRoleSee(String roleCode, Leaf childlf, String[] rolePrivs) {
		if (childlf.getType()==Leaf.TYPE_MODULE) {
			String moduleCode = childlf.getFormCode();		
			ModulePrivDb mpd = new ModulePrivDb();
			Vector v = mpd.getModulePrivsOfModule(moduleCode);
	        Iterator ir = v.iterator();
	        while (ir.hasNext()) {
	            // 遍历每个权限项
	            ModulePrivDb lp = (ModulePrivDb) ir.next();
	            if (lp.getType()==ModulePrivDb.TYPE_ROLE) {
	            	if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
	            		if (lp.getSee() == 1 || lp.getManage()==1) {
	            			return true;
	            		}	            		
	            	}
	            	else if (roleCode.equals(lp.getName())) {
	            		if (lp.getSee() == 1 || lp.getManage()==1) {
	            			return true;
	            		}
	            	}
	            }
	        }
		}
		else if (childlf.getType()==Leaf.TYPE_FLOW) {
            LeafPriv leafPriv = new LeafPriv(childlf.getFormCode());
            // list该节点的所有拥有权限的用户
            Vector v = leafPriv.listPriv(LeafPriv.PRIV_SEE);
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
                LeafPriv lp = (LeafPriv) ir.next();
                if (lp.getType()==LeafPriv.TYPE_ROLE) {
		        	if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
		        		if (lp.getSee()==1) {
		        			return true;
		        		}
		        	}
		        	else if (roleCode.equals(lp.getName())) {
                        if (lp.getSee() == 1) {
                            return true;
                        }
                	}
                }
            }
		}
		else if (childlf.getType()==Leaf.TYPE_LINK) {
			if (childlf.getPvg().equals("")) {
				return true;
			}
			if (rolePrivs!=null) {
				for (String pv : rolePrivs) {
                    String[] ary = StrUtil.split(childlf.getPvg(), ",");
                    if (ary!=null) {
                        for (int k = 0; k < ary.length; k++) {
                            if (pv.equals(ary[k])) {
                                return true;
                            }
                        }
                    }
				}
			}
		}
		else if (childlf.getType()==Leaf.TYPE_BASICDATA) { // 如果是基礎數據管理
        	int kindId = StrUtil.toInt(childlf.getFormCode(), -1);        	
            SelectKindPriv skp = new SelectKindPriv();
            skp.setKindId(kindId);
            // list该节点的所有拥有权限的用户?
            Vector r = skp.listPriv(SelectKindPriv.PRIV_APPEND);
            Iterator ir = r.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
            	SelectKindPriv lp = (SelectKindPriv) ir.next();
            	if (lp.getType()==SelectKindPriv.TYPE_ROLE) {
		        	if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
		                return true;
		            } 
		            else {
	                    if (roleCode.equals(lp.getName())) {
	                        return true;
	                    }
		            }
            	}
            }
            r = skp.listPriv(SelectKindPriv.PRIV_MODIFY);
            ir = r.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
            	SelectKindPriv lp = (SelectKindPriv) ir.next();
            	if (lp.getType()==SelectKindPriv.TYPE_ROLE) {
		        	if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
		                return true;
		            } 
		            else {
	                    if (roleCode.equals(lp.getName())) {
	                        return true;
	                    }
		            }
            	}
            }      
            r = skp.listPriv(SelectKindPriv.PRIV_DEL);
            ir = r.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
            	SelectKindPriv lp = (SelectKindPriv) ir.next();
            	if (lp.getType()==SelectKindPriv.TYPE_ROLE) {
		        	if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
		                return true;
		            } 
		            else {
	                    if (roleCode.equals(lp.getName())) {
	                        return true;
	                    }
		            }
            	}
            }             
		}
        	
        return false;
	}
}
