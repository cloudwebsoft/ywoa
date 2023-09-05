package com.redmoon.oa.sys;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.utils.ConfigUtil;
import com.cloudweb.oa.utils.FileUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.UserGroupDb;
import com.redmoon.oa.ui.PortalDb;
import com.redmoon.oa.util.TransmitData;
import com.redmoon.oa.visual.ModuleSetupDb;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.jcs3.access.exception.CacheException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SysUtil {
    /**
     * 清空系统数据
     *
     * @throws ErrMsgException
     */
    public static void initSystem(boolean isClearUser) throws ErrMsgException {
        // 将tableNames中的表与db_dict.jsp中生成的表名对比，即可得到新增的需删除数据的表
        String[] tableNames = {
                "account",
                "address",
                "address_dir",
                "cms_comment",
                "cms_images",
                "cms_site_flash_image",
                "cms_wiki_doc",
                "cms_wiki_doc_update",
                "customer_share",
                "cws_cms_nav",
                // "department",
                // "dept_user",
                "dir_kind",
                // "dir_priv",
                "doc_priv",
                // "directory",
                "doc_content",
                "dir_kind",
                "doc_attachment_log",
                "doc_log",
                "document",
                "document_attach",
                "document_poll",
                "document_poll_option",
                "email",
                "email_attach",
                "email_pop3",
                "email_recently_addr",
                "flow",
                "flow_action",
                "flow_annex",
                "flow_annex_attach",
                "flow_cms_images",
                // "flow_directory",
                "flow_doc_content",
                // "flow_dir_priv",
                "flow_doc_template",
                "flow_doc_template_dept",
                "flow_document",
                "flow_document_attach",
                "flow_document_attach_log",
                "flow_favorite",
                "flow_link",
                "flow_monitor",
                "flow_my_action",
                "flow_paper_distribute",
                "flow_paper_no",
                "flow_paper_no_prefix",
                "flow_paper_no_prefix_dept",
                // "flow_predefined",
                // "flow_sequence",
                //"form_query",
                //"form_query_condition",
                //"form_query_privilege",
                "form_query_report",
                "form_remind",
                // "form",
                // "form_field",
                "form_view",
                "guestbook",
                // "help_document_attach",
                "kaoqin",
                "kaoqin_arrange",
                // "kaoqin_copy",
                "link",
                "log",
                "message",
                // "oa_calendar",
                "oa_document_robot",
                "oa_exchange_rate",
                // "oa_lark_msg",
                "oa_location",
                // "oa_menu",
                "oa_menu_most_recently_used",
                "oa_message",
                "oa_message_attach",
                "oa_notice",
                "oa_notice_attach",
                "oa_notice_dept",
                "oa_notice_reply",
                "oa_online",
                "oa_person_group_type",
                "oa_person_group_user",
                // oa_portal
//                // oa_privilege_center
//                "oa_questionnaire_form",
//                "oa_questionnaire_form_item",
//                "oa_questionnaire_form_subitem",
//                "oa_questionnaire_item",
//                "oa_questionnaire_priv",
//                "oa_questionnaire_subitem",
                // "oa_select",
                // "oa_select_option",
//                "oa_sales_action_setup",
//                "oa_sales_customer_distr",
                // "oa_sales_fahuodan_templ",
//                "oa_sales_stock_product",
                "oa_server_ip",
                "oa_server_ip_priv",
                "oa_stamp",
                "oa_stamp_log",
                // "oa_stamp_priv",
                // "oa_user_level",
                /*"office_equipment",
                "office_equipment_op",
                "office_equipment_type",
                "office_stocktaking",*/
//                "pcinfo",
                "photo",
                // "plugin2_alipay",
//                "plugin_activity",
                // "plugin_auction_bid",
                // "plugin_auction_board",
                // "plugin_auction_catalog",
                // "plugin_auction_catalog_priv",
                // "plugin_auction_order",
                // "plugin_auction_shop",
                // "plugin_auction_shop_dir",
                // "plugin_auction_worth",
                /*"plugin_board",
                "plugin_debate",
                "plugin_debate_viewpoint",
                "plugin_dig",
                "plugin_entrance_vip_card",
                "plugin_entrance_vip_user",
                "plugin_entrance_vip_user_group",
                "plugin_flower",
                "plugin_group",
                "plugin_group_activity",
                "plugin_group_catalog",
                "plugin_group_photo",
                "plugin_group_thread",
                "plugin_group_user",*/
                // "plugin_info",
                // "plugin_info_board",
                // "plugin_info_directory",
                /*"plugin_present",
                "plugin_project",*/
                // "plugin_refer",
                // "plugin_remark",
                /*"plugin_reward",
                "plugin_reward_board",
                "post", // 20190130 fgf 弃用*/
                // "postcode",
                "project_favorite",
                // "report_manage", // 模板列表
                // "privilege",
                // "redmoonid",
                "salary_book_subject",
                "salary_payroll",
                "scheduler",
                "sms_receive_record",
                "sms_send_record",
                "sms_template",
                // "sq_advertisement",
                // "sq_board",
                /* "sq_board_entrance",
                 "sq_board_score",
                 "sq_board_visit_log",
                 "sq_boardmanager",
                 "sq_boardrender",
                 "sq_chat_msg",*/
                // "sq_chatroom",
                // "sq_classmaster",
                // "sq_faction",
//                "sq_forbid_ip",
//                "sq_forbid_ip_range",
                // "sq_forum",
                // "sq_forum_media_dir",
                // "sq_forum_media_file",
                // "sq_forum_menu",
                // "sq_forum_music_dir",
                /*"sq_forum_music_file",
                "sq_forum_music_user",
                "sq_forum_robot",*/
                // "sq_forum_stat",
                /*"sq_friend",*/
                // "sq_id",
                /*"sq_images",
                "sq_ip_address",
                "sq_master",
                "sq_message",
                "sq_message_attach",
                "sq_message_op",
                "sq_message_recommend",
                "sq_message_report",
                "sq_online",
                "sq_poll",
                "sq_poll_option",
                "sq_regist_quiz",
                "sq_roomemcee",
                "sq_scheduler",
                "sq_score_log",
                "sq_score_record",*/
                // "sq_setup_user_level",
                /*"sq_tag",
                "sq_tag_message",
                "sq_thread",
                "sq_thread_type",*/
                // "sq_user",
                // "sq_user_group",
                /*"sq_user_group_priv",
                "sq_user_priv",*/
                // "sq_user_prop",
                /*"sq_user_treasure",
                "sq_visit_board_log",
                "sq_visit_topic_log",*/
//                "update_log",
                "user_admin_dept",
                // "user_archive",
                // "user_desktop_setup",
                "user_favorite",
                // "user_group",
                "user_group_of_role",
                // "user_group_priv",
                "user_key",
                "user_mobile",
                /*"user_of_group",
                "user_of_role",*/
                "user_phrase",
                "user_plan",
                "user_plan_periodicity",
                // "user_priv",
                "user_proxy",
                "user_recently_selected",
                // "user_role",
                // "user_role_dept",
                // "user_role_priv",
                // "user_setup",
                // "users",
                // "verification_code_record",
                "visual_attach",
                "visual_module_export_template",
                "visual_module_import_template",
                // "visual_module_priv", // 清除掉除请假申请单之外的其它模块权限
                // "visual_module_relate",
                "visual_module_reports",
                // "visual_module_setup",
                "visual_module_worklog",
                "work_log",
                "work_log_attach",
                // "work_log_expand",
                "work_plan",
                "work_plan_annex",
                "work_plan_annex_attach",
                "work_plan_attach",
                "work_plan_dept",
                "work_plan_favorite",
                "work_plan_log",
                "work_plan_task",
                "work_plan_task_user",
                // "work_plan_type",
                "work_plan_user",
        };

        String sql;
        JdbcTemplate jt = new JdbcTemplate();
        jt.setAutoClose(false);
        try {
            // map中为保留的表
            Map<String, String> map = new HashMap<>();
            Map<String, String> selector = new HashMap<>();
            /*map.put("ft_data_dict_table", "");    // 数据字典 - 表格
            map.put("ft_data_dict_column", ""); // 数据字字典 - 列
            map.put("ft_formula", "");      // 函数
            map.put("ft_salary_tax", "");   // 税率设置
            map.put("ft_salary_subject", ""); // 工资科目
            map.put("ft_personbasic", "");  // 人事信息，因为桌面图表、卡片项的设置需用到
            map.put("ft_config_card", "");  // 桌面卡片
            map.put("ft_config_chart", ""); // 图表设置
            map.put("ft_ys", ""); // 预算（用于DEMO演示折线图）*/

            ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
            List<String> tableReserved = FileUtil.read(configUtil.getFile("sys_init_table_reserved.txt"));
            for (String tableName : tableReserved) {
                tableName = tableName.trim();
                if (StrUtil.isEmpty(tableName)) {
                    continue;
                }
                if (tableName.startsWith("#")) {
                    continue;
                }
                if (tableName.startsWith("!") || tableName.startsWith("$") || tableName.startsWith("^") || tableName.startsWith("&")) {
                    selector.put(tableName.substring(0, 1), tableName.substring(1));
                    continue;
                }

                int p = tableName.indexOf("(");
                if (p == -1) {
                    p = tableName.indexOf("（"); // 全角左括号，防止配置的时候录错了
                }
                if (p != -1) {
                    tableName = tableName.substring(0, p);
                }
                map.put(tableName, ""); // 图表设置
            }

            for (String tableName : tableNames) {
                if (!map.containsKey(tableName)) {
                    sql = "delete from " + tableName;
                    jt.addBatch(sql);
                }
            }

            Connection connection = new Connection(Global.getDefaultDB());
            try {
                TransmitData td = new TransmitData();
                ResultSet rsTable = td.getTableNames(connection.getCon());
                String tableName = "";
                while (rsTable.next()) {
                    tableName = rsTable.getObject(3).toString();
                    if (tableName.startsWith("ft_")) {
                        if (!map.containsKey(tableName)) {
                            Set<String> keys = selector.keySet();
                            boolean isMatched = false;
                            for (String key : keys) {
                                if ("&".equals(key)) {
                                    if (tableName.contains(selector.get(key))) {
                                        isMatched = true;
                                    }
                                } else if ("$".equals(key)) {
                                    if (tableName.endsWith(selector.get(key))) {
                                        isMatched = true;
                                    }
                                } else if ("!".equals(key)) {
                                    if (!tableName.startsWith(selector.get(key))) {
                                        isMatched = true;
                                    }
                                } else if ("^".equals(key)) {
                                    if (tableName.startsWith(selector.get(key))) {
                                        isMatched = true;
                                    }
                                }
                            }
                            if (isMatched) {
                                LogUtil.getLog(SysUtil.class).info("批处理：清空 " + tableName);
                                sql = "delete from " + tableName;
                                jt.addBatch(sql);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new ErrMsgException("数据库连接错误！");
            } finally {
                connection.close();
            }

            // if (true) return;

            sql = "update redmoonid set id=1";
            jt.addBatch(sql);
            sql = "update sq_id set id=1";
            jt.addBatch(sql);
            sql = "update sq_board set topic_count=0, post_count=0";                //数据是否需要清空
            jt.addBatch(sql);

            // 删除用户自己的个人组
            sql = "delete from oa_person_group_type";
            jt.addBatch(sql);
            sql = "delete from oa_person_group_user";
            jt.addBatch(sql);

            if (isClearUser) {
                sql = "delete from users where name<>'admin' and name<>'system'";
                jt.addBatch(sql);

                sql = "delete from user_setup where user_name<>'admin'";
                jt.addBatch(sql);

                sql = "delete from user_of_group";
                jt.addBatch(sql);

                sql = "delete from user_of_role";
                jt.addBatch(sql);

                sql = "delete from dept_user where user_name<>'admin'";
                jt.addBatch(sql);

                sql = "delete from user_priv where user_name<>'admin'";
                jt.addBatch(sql);

                sql = "delete from user_role_dept where user_name<>'admin'";
                jt.addBatch(sql);

                sql = "delete from user_role_priv where user_name<>'admin'";
                jt.addBatch(sql);

                // 取出请假申请单对应的模块，以免删除其权限
                ModuleSetupDb msd = new ModuleSetupDb();
                sql = msd.getTable().getSql("listForForm") + StrUtil.sqlstr("qjsqd");
                StringBuilder sb = new StringBuilder();
                Vector<ModuleSetupDb> v = msd.list(sql);
                for (ModuleSetupDb moduleSetupDb : v) {
                    String moduleCode = moduleSetupDb.getCode();
                    StrUtil.concat(sb, ",", StrUtil.sqlstr(moduleCode));
                }
                sql = "delete from visual_module_priv where form_code not in (" + sb.toString() + ")";
                jt.addBatch(sql);

                sql = "delete from user_group_priv where groupCode<>" + StrUtil.sqlstr(UserGroupDb.EVERYONE);
                jt.addBatch(sql);

                sql = "delete from oa_stamp_priv where user_name<>'admin'";
                jt.addBatch(sql);

                sql = "delete from dir_priv where user_name<>'admin'";
                jt.addBatch(sql);

                // 序列号不清除
                /*sql = "delete from flow_sequence";
                jt.addBatch(sql);*/
            }

            sql = "update users set diskSpaceUsed=0 where name='admin'";
            jt.addBatch(sql);
            sql = "update users set pwd='96e79218965eb72c92a549dd5a330112',pwdRaw='111111',diskSpaceUsed=0,online_time=0 where name='admin'";
            jt.addBatch(sql);
            jt.addBatch(sql);
            sql = "delete from sq_user where name<>'admin'";
            jt.addBatch(sql);
            sql = "update sq_user set pwd='96e79218965eb72c92a549dd5a330112',rawPwd='111111'";
            jt.addBatch(sql);
            //sql = "delete from user_role where code<>'member'";
            //jt.addBatch(sql);
            sql = "delete from user_group where code<>'Everyone' and isSystem<>1";
            jt.addBatch(sql);
            sql = "delete from sq_faction";
            jt.addBatch(sql);
            sql = "insert into sq_faction (code, name, isHome, description, parent_code, root_code, orders, child_count, add_date, islocked, type, layer, doc_id, template_id) values ('root', '全部门派', 1, '全部门派', -1, 'root', 1, 0, '" + System.currentTimeMillis() + "', 0, 0, 1, -1, -1)";
            jt.addBatch(sql);

            // 置admin所在的部门
            // sql = "update dept_user set dept_code='root' where user_name='admin'";
            // jt.addBatch(sql);

            //	sql = "drop table where name like 'ft_%'";
            //	jt.addBatch(sql);

            sql = "update sq_forum set userCount=1, userNew='', topicCount=0, postCount=0, todayCount=0, yestodayCount=0, maxCount=0, maxOnlineCount=0, notices=''";
            jt.addBatch(sql);

            sql = "delete from directory where code<>'root' and code<>'project'";
            jt.addBatch(sql);
            sql = "update directory set child_count=0 where code='root'";
            jt.addBatch(sql);

            sql = "delete from flow_doc_template";
            jt.addBatch(sql);

            //sql = "delete from department where code <> 'root'";
            //jt.addBatch(sql);

            sql = "update department set childCount=0 where code='root'";
            jt.addBatch(sql);

            sql = "delete from user_key where user_name <> 'admin'";
            jt.addBatch(sql);
            sql = "delete from oa_slide_menu_group where user_name <> 'system' and user_name <> 'admin'";
            jt.addBatch(sql);
            sql = "delete from oa_wallpaper  where user_name <> 'admin'";
            jt.addBatch(sql);
            // sql = "delete from sq_board  where parent_code <> 'root'";
            // jt.addBatch(sql);
            sql = "delete from work_plan_type  where name <> '默认'";
            jt.addBatch(sql);
            sql = "delete from user_favorite  where user_name <> 'system'";
            jt.addBatch(sql);
            sql = "delete from oa_portal where user_name <> 'system' and user_name <> 'admin'";
            jt.addBatch(sql);

            sql = "delete from user_desktop_setup where user_name<>'system' and user_name <> 'admin'";
            jt.addBatch(sql);

            jt.executeBatch();
        } catch (SQLException e) {
            LogUtil.getLog(SysUtil.class).error(e);
        } finally {
            jt.close();
        }

        PortalDb pd = new PortalDb();
        pd.init("admin");
        // UserDesktopSetupDb udsd = new UserDesktopSetupDb();
        // udsd.initDesktopOfUser("admin");

        //cn.js.fan.util.file.FileUtil.del(application.getRealPath("/") + "upfile");
        //cn.js.fan.util.file.FileUtil.del(application.getRealPath("/") + "forum/upfile");

        LogUtil.getLog(SysUtil.class).info("清空文件");

        try {
            cn.js.fan.util.file.FileUtil.del(Global.getRealPath() + "upfile");
            cn.js.fan.util.file.FileUtil.del(Global.getRealPath() + "forum/upfile");
            cn.js.fan.util.file.FileUtil.del(Global.getRealPath() + "public/users"); // 手机端上传的用户头像
        } catch (IOException e) {
            LogUtil.getLog(SysUtil.class).error(e);
        }

        // 初始化相关表自动生成ID的序列
        initSequenceOATables();

        cn.js.fan.cache.jcs.RMCache rmcache = cn.js.fan.cache.jcs.RMCache.getInstance();
        try {
            rmcache.clear();
        } catch (CacheException e) {
            LogUtil.getLog(SysUtil.class).error(e);
        }
    }

    public static String[][] sequenceOATables = new String[95][3];

    static {
        sequenceOATables[0][0] = "document";             //第一列为表名，第二列为SequenceManager.java中的常量
        sequenceOATables[0][1] = "OA_DOCUMENT_CMS";

        sequenceOATables[1][0] = "flow";
        sequenceOATables[1][1] = "OA_WORKFLOW";

        sequenceOATables[2][0] = "flow_action";
        sequenceOATables[2][1] = "OA_WORKFLOW_ACTION";

        sequenceOATables[3][0] = "flow_link";
        sequenceOATables[3][1] = "OA_WORKFLOW_LINK";

        sequenceOATables[4][0] = "email";
        sequenceOATables[4][1] = "OA_EMAIL";

        sequenceOATables[5][0] = "flow_document";
        sequenceOATables[5][1] = "OA_DOCUMENT_FLOW";

        sequenceOATables[6][0] = "";
        sequenceOATables[6][1] = "OA_TASK";

        sequenceOATables[7][0] = "oa_message";
        sequenceOATables[7][1] = "OA_MESSAGE";

        // sequenceOATables[8][0] = "netdisk_document";
        // sequenceOATables[8][1] = "OA_DOCUMENT_NETDISK";

        sequenceOATables[9][0] = "";
        sequenceOATables[9][1] = "OA_VISUAL_DOCUMENT";

        sequenceOATables[10][0] = "log";
        sequenceOATables[10][1] = "OA_LOG";

        sequenceOATables[11][0] = "address";
        sequenceOATables[11][1] = "OA_ADDRESS";

        sequenceOATables[12][0] = "";
        sequenceOATables[12][1] = "OA_ADDRESS_GROUP";

        sequenceOATables[13][0] = "flow_predefined";
        sequenceOATables[13][1] = "OA_WORKFLOW_PREDEFINED";

        sequenceOATables[14][0] = "";
        sequenceOATables[14][1] = "";

        sequenceOATables[15][0] = "";
        sequenceOATables[15][1] = "OA_ARCHIVE_STUDY";

        sequenceOATables[16][0] = "";
        sequenceOATables[16][1] = "OA_ARCHIVE_RESUME";

        sequenceOATables[17][0] = "";
        sequenceOATables[17][1] = "OA_ARCHIVE_FAMILY";

        sequenceOATables[18][0] = "";
        sequenceOATables[18][1] = "OA_ARCHIVE_PROFESSION";

        sequenceOATables[19][0] = "";
        sequenceOATables[19][1] = "OA_ARCHIVE_ASSESS";

        sequenceOATables[20][0] = "";
        sequenceOATables[20][1] = "OA_ARCHIVE_REWARDS";

        sequenceOATables[21][0] = "";
        sequenceOATables[21][1] = "OA_ARCHIVE_DUTY";

        sequenceOATables[22][0] = "";
        sequenceOATables[22][1] = "OA_ARCHIVE_QUERY";

        sequenceOATables[23][0] = "";
        sequenceOATables[23][1] = "OA_ARCHIVE_QUERY_CONDITION";

        sequenceOATables[24][0] = "";
        sequenceOATables[24][1] = "OA_ARCHIVE_USER_HIS";

        sequenceOATables[25][0] = "";
        sequenceOATables[25][1] = "OA_ARCHIVE_STUDY_HIS";

        sequenceOATables[26][0] = "";
        sequenceOATables[26][1] = "OA_ARCHIVE_RESUME_HIS";

        sequenceOATables[27][0] = "";
        sequenceOATables[27][1] = "OA_ARCHIVE_FAMILY_HIS";

        sequenceOATables[28][0] = "";
        sequenceOATables[28][1] = "OA_ARCHIVE_PROFESSION_HIS";

        sequenceOATables[29][0] = "";
        sequenceOATables[29][1] = "OA_ARCHIVE_ASSESS_HIS";

        sequenceOATables[30][0] = "";
        sequenceOATables[30][1] = "OA_ARCHIVE_REWARDS_HIS";

        sequenceOATables[31][0] = "";
        sequenceOATables[31][1] = "OA_ARCHIVE_DUTY_HIS";

        sequenceOATables[32][0] = "";
        sequenceOATables[32][1] = "OA_ARCHIVE_PRIVILEGE";

        sequenceOATables[33][0] = "flow_sequence";
        sequenceOATables[33][1] = "OA_WORKFLOW_SEQUENCE";

        sequenceOATables[34][0] = "flow_my_action";
        sequenceOATables[34][1] = "OA_WORKFLOW_MYACTION";

        sequenceOATables[35][0] = "document_attach";
        sequenceOATables[35][1] = "OA_DOCUMENT_ATTACH_CMS";

        sequenceOATables[36][0] = "dept_user";
        sequenceOATables[36][1] = "OA_DEPT_USER";

        sequenceOATables[37][0] = "user_desktop_setup";
        sequenceOATables[37][1] = "OA_USER_DESKTOP_SETUP";

        sequenceOATables[38][0] = "sms_send_record";
        sequenceOATables[38][1] = "OA_SMS_SEND_RECORD";

        sequenceOATables[39][0] = "scheduler";
        sequenceOATables[39][1] = "OA_SCHEDULER";

        sequenceOATables[40][0] = "oa_notice";
        sequenceOATables[40][1] = "OA_NOTICE";

        sequenceOATables[41][0] = "oa_notice_attach";
        sequenceOATables[41][1] = "OA_NOTICE_ATTACH";

        sequenceOATables[42][0] = "flow_annex";
        sequenceOATables[42][1] = "OA_FLOW_ANNEX";

        sequenceOATables[43][0] = "flow_annex_attach";
        sequenceOATables[43][1] = "OA_FLOW_ANNEX_ATTACHMENT";

        sequenceOATables[44][0] = "department";
        sequenceOATables[44][1] = "OA_DEPT";

        sequenceOATables[45][0] = "users";
        sequenceOATables[45][1] = "OA_USER";

        sequenceOATables[46][0] = "oa_message_attach";
        sequenceOATables[46][1] = "OA_MESSAGE_ATTACHMENT";

        sequenceOATables[47][0] = "";   // 不确定
        sequenceOATables[47][1] = "OA_MESSAGE_IdioAttachment";

        sequenceOATables[48][0] = "";
        sequenceOATables[48][1] = "OA_IDIOMESSAGE";

        sequenceOATables[49][0] = "work_plan_annex";
        sequenceOATables[49][1] = "OA_WORKPLAN_ANNEX";

        sequenceOATables[50][0] = "work_plan_annex_attach";
        sequenceOATables[50][1] = "OA_WORKPLAN_ANNEX_ATTACHMENT";

        sequenceOATables[51][0] = "work_plan";
        sequenceOATables[51][1] = "OA_WORKPLAN";

        sequenceOATables[52][0] = "oa_select_option";
        sequenceOATables[52][1] = "OA_SELECT_OPTION";

        sequenceOATables[53][0] = "oa_questionnaire_form";  //  此表没有ID列
        sequenceOATables[53][1] = "QUESTIONNAIRE_FORM";
        sequenceOATables[53][2] = "form_id";

        sequenceOATables[54][0] = "oa_questionnaire_form_item"; //  此表没有ID列
        sequenceOATables[54][1] = "QUESTIONNAIRE_FORM_ITEM";
        sequenceOATables[54][2] = "item_id";

        sequenceOATables[55][0] = "oa_questionnaire_form_subitem";
        sequenceOATables[55][1] = "QUESTIONNAIRE_FORM_SUBITEM";

        sequenceOATables[56][0] = "";
        sequenceOATables[56][1] = "QUESTIONNAIRE_NUM";

        sequenceOATables[57][0] = "oa_questionnaire_item";
        sequenceOATables[57][1] = "QUESTIONNAIRE_ITEM";

        sequenceOATables[58][0] = "oa_questionnaire_subitem";  //  此表没有ID列
        sequenceOATables[58][1] = "QUESTIONNAIRE_SUBITEM";
        sequenceOATables[58][2] = "subitem_id";

        sequenceOATables[59][0] = "oa_exam_subject";
        sequenceOATables[59][1] = "EXAM_SUBJECT";

        sequenceOATables[60][0] = "oa_exam_paper";
        sequenceOATables[60][1] = "EXAM_PAPER";

        sequenceOATables[61][0] = "";
        sequenceOATables[61][1] = "EXAM_QUESTION";

        sequenceOATables[62][0] = "oa_exam_score";
        sequenceOATables[62][1] = "EXAM_SCORE";

        sequenceOATables[63][0] = "oa_exam_useranswer";
        sequenceOATables[63][1] = "EXAM_USERANSWER";

        sequenceOATables[64][0] = "user_favorite";
        sequenceOATables[64][1] = "USER_FAVORITE";

        sequenceOATables[65][0] = "";
        sequenceOATables[65][1] = "FILEARK_PRIV";

        sequenceOATables[66][0] = "visual_module_priv";
        sequenceOATables[66][1] = "VISUAL_MODULE_PRIV";

        sequenceOATables[67][0] = "";
        sequenceOATables[67][1] = "ASSET_INFO";

        sequenceOATables[68][0] = "";
        sequenceOATables[68][1] = "OA_WATCH_TYPE";

        sequenceOATables[69][0] = "";
        sequenceOATables[69][1] = "OA_WATCH_GROUP";

        sequenceOATables[70][0] = "";
        sequenceOATables[70][1] = "OA_WATCH_PRIVILEGE_OF_USER";

        sequenceOATables[71][0] = "";
        sequenceOATables[71][1] = "OA_WATCH_ROSTER";

        sequenceOATables[72][0] = "";
        sequenceOATables[72][1] = "OA_WATCH_RECORD";

        sequenceOATables[73][0] = "";
        sequenceOATables[73][1] = "OA_WATCH_RECORD_ATTACH";

        sequenceOATables[74][0] = "";
        sequenceOATables[74][1] = "OA_WATCH_BASIC";

        sequenceOATables[75][0] = "user_plan_periodicity";
        sequenceOATables[75][1] = "PLAN_PERIODICITY";

        sequenceOATables[76][0] = "oa_document_robot";
        sequenceOATables[76][1] = "OA_DOCUMENT_ROBOT";

        sequenceOATables[77][0] = "cms_images";
        sequenceOATables[77][1] = "CMS_IMAGES";

        sequenceOATables[78][0] = "email_pop3";
        sequenceOATables[78][1] = "EMAIL_POP3";

        sequenceOATables[79][0] = "oa_stamp";
        sequenceOATables[79][1] = "OA_STAMP";

        sequenceOATables[80][0] = "oa_stamp_log";
        sequenceOATables[80][1] = "OA_STAMP_LOG";

        sequenceOATables[81][0] = "flow_document_attach";
        sequenceOATables[81][1] = "FLOW_DOCUMENT_ATTACH";

        sequenceOATables[82][0] = ""; // 为空表示过滤掉，不处理，该表已被删除
        sequenceOATables[82][1] = "ASS_FORM";

        sequenceOATables[83][0] = "oa_menu_most_recently_used";
        sequenceOATables[83][1] = "OA_MENU_MOST_RECENTLY_USED";

        sequenceOATables[84][0] = "";
        sequenceOATables[84][1] = "OA_SMS_BATCH";

        sequenceOATables[85][0] = "form_query";
        sequenceOATables[85][1] = "OA_FORM_QUERY";

        sequenceOATables[86][0] = "form_query_condition";
        sequenceOATables[86][1] = "OA_FORM_QUERY_CONDITION";

        sequenceOATables[87][0] = "oa_stamp_priv";
        sequenceOATables[87][1] = "OA_STAMP_PRIV";

        sequenceOATables[88][0] = "flow_paper_no_prefix";
        sequenceOATables[88][1] = "FLOW_PAPER_NO_PREFIX";

        sequenceOATables[89][0] = "flow_paper_distribute";
        sequenceOATables[89][1] = "OA_FLOW_PAPER_DISTRIBUTE";

        // sequenceOATables[90][0] = "netdisk_document_attach";
        // sequenceOATables[90][1] = "OA_DOCUMENT_NETDISK_ATTACHMENT";

        sequenceOATables[91][0] = "report_manage";
        sequenceOATables[91][1] = "OA_REPORT_MANAGE";

        // sequenceOATables[92][0] = "netdisk_role_template";
        // sequenceOATables[92][1] = "OA_NETDISK_ROLE_TEMPLATE";

        sequenceOATables[93][0] = "work_log";
        sequenceOATables[93][1] = "OA_WORK_LOG";

        // sequenceOATables[94][0] = "user_group";     表中没有id字段，执行会报错
        // sequenceOATables[94][1] = "OA_USER_GROUP";

        sequenceOATables[94][0] = "work_log_expand";
        sequenceOATables[94][1] = "OA_WORK_LOG_EXPAND";
    }

    public static String[][] getSequenceOATables() {
        return sequenceOATables;
    }

    public static void initSequenceOATables() {
        int cmsId = 0; // 记录常量的值也就是redmoonid表idType的值
        int id = 0;    // 记录redmoonid表ID初始化后的值
        String sql = "";
        for (int i = 0; i < 95; i++) {
            String tableName = sequenceOATables[i][0];
            cmsId = i;
            if (StringUtils.isEmpty(tableName)) {
                continue;
            }

            String tableId = "id";
            if (sequenceOATables[i][2] != null) {
                tableId = sequenceOATables[i][2];
            }
            sql = "select max(" + tableId + ") from " + tableName;
            JdbcTemplate jt = new JdbcTemplate();
            try {
                ResultIterator ri = jt.executeQuery(sql);
                if (ri.hasNext()) {
                    ResultRecord rd = ri.next();
                    id = rd.getInt(1);
                }
                id++;
            } catch (Exception e) {
                LogUtil.getLog(SysUtil.class).error(e);
                return;
            }

            try {
                sql = "update redmoonid set id=" + id + " where idType=" + cmsId;
                int r = jt.executeUpdate(sql);
                if (r == 0) {
                    sql = "insert into redmoonid (id, idType) values (" + id + "," + i + ")";
                    jt.executeUpdate(sql);
                }
            } catch (Exception e) {
                LogUtil.getLog(SysUtil.class).error(e);
                return;
            }
        }
    }
}