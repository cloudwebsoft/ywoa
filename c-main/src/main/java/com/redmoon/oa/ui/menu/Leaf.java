package com.redmoon.oa.ui.menu;

import cn.js.fan.base.ITagSupport;
import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.entity.Menu;
import com.cloudweb.oa.permission.MenuPermission;
import com.cloudweb.oa.service.IMenuService;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.ui.LocalUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Leaf implements Serializable, ITagSupport {
    public static String CODE_ROOT = "root";
    public static String CODE_BOTTOM = "bottom";

    transient RMCache rmCache = RMCache.getInstance();

    public static final int TYPE_PRESET = 1;
    public static final int TYPE_LINK = 0;
    public static final int TYPE_MODULE = 2;
    public static final int TYPE_FLOW = 3;
    /**
     * 基础数据
     */
    public static final int TYPE_BASICDATA = 4;
    
    /**
     * 默认型
     */
    public static final int KIND_DEFAULT = 0;
    /**
     * 政府型
     */
    public static final int KIND_GOV = 1;
    
    /**
     * 企业型
     */
    public static final int KIND_COM = 2;    

    private String target;
    private boolean widget = false;

    private String code = "", name = "", link = "", parent_code = "-1", root_code = "";
    
    /**
     * 存储模块编码或流程类型
     */
    private String formCode;
    
    private java.util.Date addDate;
    private int orders = 1, layer = 1, child_count = 0, islocked = 0;
    boolean isHome = false;
    final String dirCache = "OA_MENU";
    private String bigIcon;
    private int widgetHeight;
    private int widgetWidth;
    private boolean isSystem;     //判断是否是系统菜单

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    @Override
    public String get(String field) {
        switch (field) {
            case "code":
                return getCode();
            case "name":
                return getName();
            case "desc":
                return getLink();
            case "parent_code":
                return getParentCode();
            case "root_code":
                return getRootCode();
            case "layer":
                return "" + getLayer();
            default:
                return "";
        }
    }

    public Leaf() {
    }

    public Leaf(String code) {
        this.code = code;
        load();
    }
    
    public void renew() {
        if (rmCache == null) {
            rmCache = RMCache.getInstance();
        }
    }

    public Leaf getFromMenu(Menu menu, Leaf lf) {
        if (menu == null) {
            return lf;
        }
        lf.setCode(menu.getCode());
        lf.setName(menu.getName());
        lf.setLink(menu.getLink());
        lf.setParentCode(menu.getParentCode());
        lf.setRootCode(menu.getRootCode());
        lf.setOrders(menu.getOrders());
        lf.setLayer(menu.getLayer());
        lf.setChildCount(menu.getChildCount());
        lf.setAddDate(DateUtil.parse(menu.getAddDate()));
        lf.setIsLocked((menu.getIslocked() != null && menu.getIslocked()) ? 1 : 0);
        lf.setType(menu.getType());
        lf.setIsHome(menu.getIsHome());
        lf.setPreCode(menu.getPreCode());
        lf.setWidth(menu.getWidth());
        lf.setHasPath(menu.getIsHasPath()==1);
        lf.setResource(menu.getIsResource()==1);
        lf.setTarget(menu.getTarget());
        lf.setPvg(menu.getPvg());
        lf.setIcon(menu.getIcon());
        lf.setUse(menu.getIsUse()==1);
        lf.setNav(menu.getIsNav()==1);
        lf.setFormCode(menu.getFormCode());
        lf.setCanRepeat(menu.getCanRepeat()==1);
        lf.setBigIcon(menu.getBigIcon());
        lf.setWidget(menu.getIsWidget()==1);
        lf.setWidgetHeight(menu.getWidgetHeight());
        lf.setWidgetWidth(menu.getWidgetWidth());
        lf.setKind(menu.getKind());
        lf.setSystem(menu.getIsSystem());
        lf.setFontIcon(menu.getFontIcon());
        lf.setDescription(menu.getDescription());
        lf.setLoaded(true);

        return lf;
    }

    public void load() {
        com.cloudweb.oa.cache.MenuCache menuCache = SpringUtil.getBean(com.cloudweb.oa.cache.MenuCache.class);
        Menu menu = menuCache.getMenu(code);
        if (menu != null) {
            getFromMenu(menu, this);
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setRootCode(String c) {
        this.root_code = c;
    }

    public void setType(int t) {
        this.type = t;
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getOrders() {
        return orders;
    }

    public boolean getIsHome() {
        return isHome;
    }

    public void setParentCode(String p) {
        this.parent_code = p;
    }

    public String getParentCode() {
        return this.parent_code;
    }

    public void setIsHome(boolean b) {
        this.isHome = b;
    }

    public String getRootCode() {
        return root_code;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public String getLink() {
        return link;
    }

    public int getType() {
        return type;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getPreCode() {
        return preCode;
    }

    public int getWidth() {
        return width;
    }

    public boolean isHasPath() {
        return hasPath;
    }

    public boolean isResource() {
        return resource;
    }

    public String getTarget() {
        return target;
    }

    public String getPvg() {
        return pvg;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isUse() {
        return use;
    }

    public boolean isNav() {
        return nav;
    }

    public String getFormCode() {
        return formCode;
    }

    public boolean isCanRepeat() {
        return canRepeat;
    }

    public String getBigIcon() {
        return bigIcon;
    }

    public boolean isWidget() {
        return widget;
    }

    public int getWidgetWidth() {
        return widgetWidth;
    }

    public int getWidgetHeight() {
        return widgetHeight;
    }

    public int getChildCount() {
        return child_count;
    }

    public Vector getChildren() {
        IMenuService menuService = SpringUtil.getBean(IMenuService.class);
        List<Menu> list = menuService.getChildren(code);
        Vector<Leaf> v = new Vector<>();
        for (Menu menu : list) {
            v.addElement(getFromMenu(menu, new Leaf()));
        }
        return v;
    }

    /**
     * 取出code结点的所有孩子结点
     * @return ResultIterator
     * @throws ErrMsgException
     */
    public Vector getAllChild(Vector vt, Leaf leaf) throws ErrMsgException {
        Vector children = leaf.getChildren();
        if (children.isEmpty())
            return children;
        vt.addAll(children);
        Iterator ir = children.iterator();
        while (ir.hasNext()) {
            Leaf lf = (Leaf) ir.next();
            getAllChild(vt, lf);
        }
        // return children;
        return vt;
    }

    @Override
    public String toString() {
        return "menu is " + code;
    }

    private int type;

    public Leaf getLeaf(String code) {
        com.cloudweb.oa.cache.MenuCache departmentCache = SpringUtil.getBean(com.cloudweb.oa.cache.MenuCache.class);
        Menu menu = departmentCache.getMenu(code);
        if (menu != null) {
            return getFromMenu(menu, new Leaf());
        }
        else {
            return null;
        }
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setPreCode(String preCode) {
        this.preCode = preCode;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHasPath(boolean hasPath) {
        this.hasPath = hasPath;
    }

    public void setResource(boolean resource) {
        this.resource = resource;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setPvg(String pvg) {
        this.pvg = pvg;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setUse(boolean use) {
        this.use = use;
    }

    public void setNav(boolean nav) {
        this.nav = nav;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public void setCanRepeat(boolean canRepeat) {
        this.canRepeat = canRepeat;
    }

    public void setBigIcon(String bigIcon) {
        this.bigIcon = bigIcon;
    }

    public void setWidget(boolean widget) {
        this.widget = widget;
    }

    public void setWidgetWidth(int widgetWidth) {
        this.widgetWidth = widgetWidth;
    }

    public void setWidgetHeight(int widgetHeight) {
        this.widgetHeight = widgetHeight;
    }

    public void setChildCount(int childCount) {
        this.child_count = childCount;
    }

    public String getName(HttpServletRequest request) {
    	if(name.startsWith("#")){
			return LocalUtil.LoadString(request, "res.ui.menu",code);
		}else{
			return name;
		}
    }

    public String getLink(HttpServletRequest request) {
        IMenuService menuService = SpringUtil.getBean(IMenuService.class);
        return menuService.getRealLink(getFromLeaf(this, new Menu()));
    }

    public Menu getFromLeaf(Leaf lf, Menu menu) {
        if (lf == null) {
            return menu;
        }
        menu.setCode(lf.getCode());
        menu.setName(lf.getName());
        menu.setLink(lf.getLink());
        menu.setParentCode(lf.getParentCode());
        menu.setRootCode(lf.getRootCode());
        menu.setOrders(lf.getOrders());
        menu.setLayer(lf.getLayer());
        menu.setChildCount(lf.getChildCount());
        menu.setAddDate(lf.getAddDate() != null ? String.valueOf(lf.getAddDate().getTime()) : "");
        menu.setIslocked(lf.getIslocked()==1);
        menu.setType(lf.getType());
        menu.setIsHome(lf.getIsHome());
        menu.setPreCode(lf.getPreCode());
        menu.setWidth(lf.getWidth());
        menu.setIsHasPath(lf.isHasPath()?1:0);
        menu.setIsResource(lf.isResource()?1:0);
        menu.setTarget(lf.getTarget());
        menu.setPvg(lf.getPvg());
        menu.setIcon(lf.getIcon());
        menu.setIsUse(lf.isUse()?1:0);
        menu.setIsNav(lf.isNav()?1:0);
        menu.setFormCode(lf.getFormCode());
        menu.setCanRepeat(lf.isCanRepeat()?1:0);
        menu.setBigIcon(lf.getBigIcon());
        menu.setIsWidget(lf.isWidget()?1:0);
        menu.setWidgetHeight(lf.getWidgetHeight());
        menu.setWidgetWidth(lf.getWidgetWidth());
        menu.setKind(lf.getKind());
        menu.setIsSystem(lf.getSystem());
        menu.setFontIcon(lf.getFontIcon());
        menu.setDescription(lf.getDescription());
        return menu;
    }

    /**
     * 判断用户能否看到菜单项
     * @param request HttpServletRequest
     * @return boolean
     */
    public boolean canUserSee(HttpServletRequest request) {
        // long t = System.currentTimeMillis();
        Menu menu = getFromLeaf(this, new Menu());
        MenuPermission menuPermission = SpringUtil.getBean(MenuPermission.class);
        return menuPermission.canUserSee(request, menu);
        // DebugUtil.i(getClass(), "canUserSee", getName() + "(" + getDescription() + ")" + (System.currentTimeMillis() - t) + " ms");
    }

    /**
     * 取得菜单对应记录的条数
     * @return int
     */
    public int getCount(String userName) {
        if (code.equals("message")) {
            // 取得未读短消息的条数
            MessageDb md = new MessageDb();
            return md.getNewMsgCount(userName);
        }
        else if (code.equals("flow_wait")) {
            // 取得待办流程的条数
            return WorkflowDb.getWaitCount(userName);
        }
        else {
            return 0;
        }
    }

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getKind() {
		return kind;
	}

	public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
	
	private boolean loaded = false;
    private String preCode;
    private int width = 60;
    private boolean hasPath = false;
    private boolean resource = true;
    private String pvg;
    private String icon;
    private boolean use = true;
    private boolean nav = false;
    private boolean canRepeat = false;
    
    /**
     * 字体图标
     */
    private String fontIcon;
    
    public String getFontIcon() {
		return fontIcon;
	}

	public void setFontIcon(String fontIcon) {
		this.fontIcon = fontIcon;
	}

	/**
     * 20140228暂未启用，因为在前台及管理菜单、滑动菜单的时候都需控制显示，工作量较大
     */
    private int kind = KIND_DEFAULT;

	public boolean getSystem() {
		return isSystem;
	}

	public void setSystem(boolean isSystem) {
		this.isSystem = isSystem;
	}

	public void setAddDate(Date addDate) {
	    this.addDate = addDate;
    }

    public void setIsLocked(int isLocked) {
	    this.islocked = isLocked;
    }

    public int getIslocked() {
        return islocked;
    }

    public Date getAddDate() {
        return addDate;
    }
}
