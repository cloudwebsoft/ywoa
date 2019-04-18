package com.redmoon.oa.flow;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkflowHelper {
    // 不能通过线程局部变量在线程中传递参数
    // 因为当request处理后，可能仍会保持该线程keepalive，所以当在前台未选择部门，点击“同意”按钮后
    // 仍为同一个线程处理，这时就会混乱
    // tomcat 6使用nio技术，一个线程可能会处理多个request，提高处理访问能力的情况下，ThreadLocal的使用将会出现问题，对象不再服务于一个 request，所以ThreadLocal的长期使用需要注意

/*
    private static ThreadLocal<WorkflowParams> params = new ThreadLocal<
            WorkflowParams>() {
        public WorkflowParams initialValue() {
            return null;
        }
    };

    public static WorkflowParams get() {
        return params.get();
    }

    public static void set(WorkflowParams wfparams) {
        params.set(wfparams);
    }

    public WorkflowHelper() {
    }
*/
}
