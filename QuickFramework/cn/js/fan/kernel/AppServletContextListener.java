package cn.js.fan.kernel;

/**
 * 当reload的时候，contextDestroyed会被调用
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class AppServletContextListener implements javax.servlet.ServletContextListener {
    private java.util.Timer timer;

    public AppServletContextListener() {
        // System.out.println( "startup init" );
        // timer = new java.util.Timer( true );
    }

    public void contextDestroyed( javax.servlet.ServletContextEvent event ) {
        System.out.println("Cloud Web Soft Framework has been destroyed." );
        // timer.cancel();
        // 关闭调度程序，如果关闭，会使得线程dead，调度器便不再调度了
        // Scheduler.getInstance().doExit();
    }

    public void contextInitialized( javax.servlet.ServletContextEvent event ) {
        // 注意在这里调用Global的属性或方法，会使得调度被初始化，而调度中的任务会调用proxool连接
        // 而此时proxool的连接还未初始化好，这时就会使得出错，Tomcat启动不下去
        // System.out.println( Global.AppName + " has started." );
        // System.out.println( "The real path of " + Global.AppName + " is " + event.getServletContext().getRealPath( "/" ) );
        System.out.println("Cloud Web Soft Framework has been started." );
        /*
        timer.schedule( new java.util.TimerTask() {
            public void run() {
                System.out.println( "TimerTask run..." );
            }
        } , 0 , 1000 );
        */
        // Global.init();
    }

}
