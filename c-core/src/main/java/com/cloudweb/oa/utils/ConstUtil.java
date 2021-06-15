package com.cloudweb.oa.utils;

public class ConstUtil {

    public static final String DEPT_ROOT = "root";

    public static final int TYPE_DEPT = 1;
    public static final int TYPE_UNIT = 0;

    /**
     * 通知所选用户
     */
    public static final int NOTICE_IS_SEL_USER = 0;
    /**
     * 部门通知
     */
    public static final int NOTICE_IS_DEPT = 1;
    /**
     * 全员通知
     */
    public static final int NOTICE_IS_ALL = 2;

    public static final String NOTICE_ATT_BASE_PATH = "upfile/notice";

    /**
     * 角色“全体成员”，对应于所有用户
     */
    public static final String ROLE_MEMBER = "member";

    /**
     * 用户组“每个人”，对应于所有用户
     */
    public static final String GROUP_EVERYONE = "Everyone";
    /**
     * 用户组“管理员组”，拥有超级管理员权限
     */
    public static final String GROUP_ADMINISTRATORS = "Administrators";

    /**
     * 工作中
     */
    public static final int USER_VALID_WORKING = 1;
    /**
     * 已离职
     */
    public static final int USER_VALID_FIRED = 0;

    public static final int GENDER_MAN = 0;
    public static final int GENDER_WOMAN = 1;

    /**
     * 用户admin
     */
    public static final String USER_ADMIN = "admin";

    public static final String USER_SYSTEM = "system";


    /**
     * 系统门户中的桌面项，其system_id字段为0
     */
    public static final boolean USER_DESKTOP_SETUP_SYSTEM_ID_NONE = false;

    public static final String MENU_ROOT = "root";
    public static final String MENU_CODE_BOTTOM = "bottom";

    /**
     * 链接型菜单项
     */
    public static final int MENU_TYPE_LINK = 0;
    /**
     * 预置型菜单项
     */
    public static final int MENU_TYPE_PRESET = 1;
    /**
     * 智能模块型菜单项
     */
    public static final int MENU_TYPE_MODULE = 2;
    /**
     * 流程型菜单项
     */
    public static final int MENU_TYPE_FLOW = 3;
    /**
     * 基础数据型菜单项
     */
    public static final int MENU_TYPE_BASICDATA = 4;

    public static final String MENU_ITEM_SALES = "sales";

    public static final String MENU_SUPERVIS = "supervis";
    public static final String MENU_SUPERVIS_NAME = "部门工作";
    public static final String MENU_SUPERVIS_NAME2 = "督办";
    public static final String MENU_SUPERVIS_PARENT_CODE = "administration";

    /**
     * PC端
     */
    public static final int DEVICE_PC = 0;
    /**
     * 移动端
     */
    public static final int DEVICE_MOBILE = 100;

    /**
     * 配额未设（磁盘空间，消息空间）
     */
    public static final long QUOTA_NOT_SET = -1;


    /**
     * 默认型
     */
    public static final int PRIV_KIND_DEFAULT = 0;
    /**
     * 政府型
     */
    public static final int PRIV_KIND_GOV = 1;

    /**
     * 企业型
     */
    public static final int PRIV_KIND_COM = 2;

    public static final String PRIV_ADMIN = "admin";
    public static final String PRIV_USER = "admin.user";
    public static final String PRIV_READ = "read";

    public static final int LOG_TYPE_LOGIN = 0;
    public static final int LOG_TYPE_LOGOUT = 1;
    public static final int LOG_TYPE_ACTION = 2;
    public static final int LOG_TYPE_WARN = 3;
    public static final int LOG_TYPE_ERROR = 4;
    /**
     * 权限
     */
    public static final int LOG_TYPE_PRIVILEGE = 5;
    /**
     * 攻击
     */
    public static final int LOG_TYPE_HACK = 100;

    /**
     *
     */
    public static final int UI_MODE_NONE = 0;
    /**
     * 经典型简洁菜单
     */
    public static final int UI_MODE_PROFESSION = 1;
    /**
     * 时尚型
     */
    public static final int UI_MODE_FASHION = 2;
    /**
     * 绚丽型
     */
    public static final int UI_MODE_FLOWERINESS = 3;

    /**
     * 经典型传统菜单，即1.3菜单样式
     */
    public static final int UI_MODE_PROFESSION_NORMAL = 4;

    /**
     * 轻简型
     */
    public static final int UI_MODE_LTE = 5;

    /**
     * 2.0菜单样式
     */
    public static final int MENU_MODE_NEW = 1;
    /**
     * 1.3菜单样式
     */
    public static final int MENU_MODE_NORMAL = 0;


    /**
     * session中用于保存用户名的键
     */
    public static final String SESSION_NAME = "OA_NAME";
    /**
     * session中用于保存密码MD5值的键
     */
    public static final String SESSION_PWDMD5 = "OA_PWDMD5";

    public static final String SESSION_UNITCODE = "oa.unitCode";

    /**
     * 消息队列中的消息类型，内部消息
     */
    public static final int MQ_MSG_TYPE_MSG = 0;
    /**
     * 短信消息
     */
    public static final int MQ_MSG_TYPE_SMS = 1;
    /**
     * 邮件消息
     */
    public static final int MQ_MSG_TYPE_EMAIL = 2;
    /**
     * 钉钉消息
     */
    public static final int MQ_MSG_TYPE_DD = 3;
    /**
     * 企业微信消息
     */
    public static final int MQ_MSG_TYPE_WX = 4;

    /**
     * 内部消息，带有actionType、actionSubType
     */
    public static final int MQ_MSG_TYPE_MESSAGE = 5;

    public static final String QUEUE_MESSAGE = "queueMsg";

    /**
     * 当从缓存获取不到对象时，为防穿透，赋予的临时对象，其主键为CACHE_NONE
     */
    public static final String CACHE_NONE = "&&";

    /**
     * 临时对象的缓存到期时间，300秒
     */
    public static final int CACHE_NONE_EXPIRE = 300;

    // 日报
    public static final String TYPE_DAY = "0";
    // 周报
    public static final String TYPE_WEEK = "1";
    // 月报
    public static final String TYPE_MONTH = "2";

    /**
     * 用户在某部门中不拥有角色
     */
    public static final int ROLE_OF_DEPT_NO = 0;
    /**
     * 用户在某部门中拥有角色
     */
    public static final int ROLE_OF_DEPT_YES = 1;
    /**
     * 默认用户拥有的角色属于所在的部门
     */
    public static final int ROLE_OF_DEPT_DEFAULT = 2;

    public static final int ADDRESS_TYPE_PUBLIC = 1;
    public static final int ADDRESS_TYPE_USER = 0;

    /**
     * 列表页中统计字段所在行的id
     */
    public static final int MODULE_ID_STAT = -10;

    /**
     * 模块生成word时，自选视图
     */
    public static final int MODULE_EXPORT_WORD_VIEW_SELECT = -100;
    /**
     * 模块生成word时，使用表单
     */
    public static final int MODULE_EXPORT_WORD_VIEW_FORM = -1;

    /**
     * 为向下兼容，为0表示老流程中没有用form_archive
     */
    public static final int FORM_ARCHIVE_NONE = 0;

    public static final String IS_FOR_EXPORT = "isForExport";

    /**
     * 流程存档表的编码
     */
    public static final String FLOW_ARCHIVE = "flow_archive";

    public static final String PAGE_TYPE_WORD = "word";
    public static final String PAGE_TYPE_LIST = "moduleList";
    public static final String PAGE_TYPE_LIST_RELATE = "moduleListRelate";
    public static final String PAGE_TYPE_ADD = "add";
    public static final String PAGE_TYPE_EDIT = "edit";
    public static final String PAGE_TYPE_SHOW = "show";
    public static final String PAGE_TYPE_FLOW = "flow";
    public static final String PAGE_TYPE_FLOW_SHOW = "flowShow";

    public static final int CLIENT_NONE = 0;     		//没有使用手机app
    public static final int CLIENT_ANDROID = 1;     		//安卓
    public static final int CLIENT_IOS = 2;         		//苹果

    public static final int RES_SUCCESS = 0;                      //成功
    public static final int RES_FAIL = -1;                        //失败

    public static final int RETURNCODE_SUCCESS = 0;
    public static final int RETURNCODE_SUCCESS_NULL = -1;       // 获取成功，但无数据

    public static final String BASIC_TREE_NODE = "basic_tree_node"; // 树形结构节点描述表单的编码
    /**
     * 链接型节点
     */
    public static final int BASIC_TREE_NODE_TYPE_LINK = 0;
    /**
     * 模块型节点
     */
    public static final int BASIC_TREE_NODE_TYPE_MODULE = 1;
    /**
     * 模块型节点列表页
     */
    public static final String BASIC_TREE_NODE_PAGE_TYPE_LIST = "list";
    /**
     * 模块型节点编辑页
     */
    public static final String BASIC_TREE_NODE_PAGE_TYPE_EDIT = "edit";
    /**
     * 模块型节点详情页
     */
    public static final String BASIC_TREE_NODE_PAGE_TYPE_show = "show";

    /**
     * 页面风格，默认
     */
    public static final int PAGE_STYLE_DEFAULT = 0;
    /**
     * 页面风格，轻量
     */
    public static final int PAGE_STYLE_LIGHT = 1;

    /**
     * 日志模块（记录增删改）的编码
     */
    public static final String MODULE_CODE_LOG = "module_log";
    /**
     * 浏览日志模块的编码
     */
    public static final String MODULE_CODE_LOG_READ = "module_log_read";

    /**
     * 表单存档表单的编码
     */
    public static final String FORM_ARCHIVE = "form_archive";

    /**
     * 函数表单的编码
     */
    public static final String FORM_FORMULA = "formula";
    /**
     * 详情页打印按钮
     */
    public static final String BTN_PRINT = "btnPrint";
    /**
     * 详情页编辑按钮
     */
    public static final String BTN_EDIT = "btnEdit";
    /**
     * 编辑页确定按钮
     */
    public static final String BTN_OK = "btnOK";

    /**
     * 脚本场景
     */
    public static final String SCENE = "scene";

    /**
     * 脚本场景：流程结束
     */
    public static final String SCENE_FLOW_ON_FINISH = "flow.onFinish";

    /**
     * 脚本场景：节点提交验证
     */
    public static final String SCENE_FLOW_VALIDATE = "flow.validate";

    /**
     * 脚本场景：节点流转
     */
    public static final String SCENE_FLOW_ACTION_FINISH = "flow.actionFinish";

    /**
     * 脚本场景：返回事件
     */
    public static final String SCENE_FLOW_ACTION_RETURN = "flow.actionReturn";

    /**
     * 脚本场景：放弃事件
     */
    public static final String SCENE_FLOW_DISCARD = "flow.discard";

    /**
     * 嵌套表格
     */
    public static final String NEST_TABLE = "nest_table";

    /**
     * 嵌套表格2
     */
    public static final String NEST_SHEET = "nest_sheet";
    /**
     * 明细表
     */
    public static final String NEST_DETAIL_LIST = "detaillist";

    /**
     * 嵌套表格字段名的前缀，以免与主表中的字段重名
     */
    public static final String NEST_TABLE_FIELD_PREFIX = "nest_field_";
}
