package com.cloudweb.oa.config;

import com.cloudweb.oa.filter.JwtFilter;
import com.cloudweb.oa.security.LoginFailureAuthenticationHandler;
import com.cloudweb.oa.security.LoginSuccessAuthenticationHandler;
import com.cloudweb.oa.security.MyLogoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import javax.annotation.Resource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    @Qualifier(value = "userDetailServiceImpl")
    private UserDetailsService userDetailsService;

    @Autowired
    @Qualifier(value = "authenticationProvider")
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private MyLogoutHandler logoutHandler;

    /**
     * 登录成功handler
     */
    @Resource
    private LoginSuccessAuthenticationHandler loginSuccessHandler;

    /**
     * 登录失败handler
     */
    @Resource
    private LoginFailureAuthenticationHandler loginFailureHandler;

    @Resource
    private LogoutSuccessHandler logoutSuccessHandler;

    @Resource
    private AccessDeniedHandler accessDeniedHandler;

    @Autowired
    private JwtFilter jwtFilter;

    //@Autowired
    //private AsyncSupportFilter asyncSupportFilter;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        //校验用户
//        auth.userDetailsService( userDetailsService ).passwordEncoder( new PasswordEncoder() {
//            //对密码进行加密
//            @Override
//            public String encode(CharSequence charSequence) {
//                return DigestUtils.md5DigestAsHex(charSequence.toString().getBytes());
//            }
//            //对密码进行判断匹配
//            @Override
//            public boolean matches(CharSequence charSequence, String s) {
//                String encode = DigestUtils.md5DigestAsHex(charSequence.toString().getBytes());
//                boolean res = s.equals( encode );
//                return res;
//            }
//        } );

//        auth.jdbcAuthentication().dataSource(dataSource)
//                .usersByUsernameQuery("select u_name,u_password,a_id from `admin` where u_name = ?")
//                //此处必须3个参数，前2个用户名和密码，第三个可以随便找一个
//                .authoritiesByUsernameQuery("select u_name, a_id from `admin` where u_name = ?");
//        //此处必须为2个参数，第一个为用户名，第二个为用户权限（可以随意找一个）
    }

    /**
     * 禁止内置Tomcat不安全的http方法，已移至TomcatConfig的postProcessContext方法中，否则会报：
     * org.springframework.context.ApplicationContextException:
     * Unable to start ServletWebServerApplicationContext due to multiple ServletWebServerFactory beans : embeddedServletContainerFactory,configurableServletWebServerFactory
     * @return
     */
    /*@Bean
    public ConfigurableServletWebServerFactory configurableServletWebServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addContextCustomizers(context -> {
            SecurityConstraint securityConstraint = new SecurityConstraint();
            securityConstraint.setUserConstraint("CONFIDENTIAL");
            SecurityCollection collection = new SecurityCollection();
            collection.addPattern("/*");
            collection.addMethod("HEAD");
            collection.addMethod("PUT");
            collection.addMethod("DELETE");
            collection.addMethod("OPTIONS");
            collection.addMethod("TRACE");
            collection.addMethod("COPY");
            collection.addMethod("SEARCH");
            collection.addMethod("PROPFIND");
            securityConstraint.addCollection(collection);
            context.addConstraint(securityConstraint);
        });
        return factory;
    }*/

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        auth.authenticationProvider(authenticationProvider);
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        // 允许两个斜杠 //，ios手机端中调用九宫格图标时里面有双斜杠
        firewall.setAllowUrlEncodedDoubleSlash(true);
        // 请求地址中存在 . 编码之后的字符 %2e、%2E，则请求将被允许
        // firewall.setAllowUrlEncodedPeriod(true);
        return firewall;
    }

    @Override
    public void configure(WebSecurity webSecurity) throws Exception {
        // 因为spring security在响应标头中自动加上了X-Content-Type-Options: nosniff，使浏览器不自动判别，致打开desktop.jsp时出现以下报错：
        // Refused to execute script from 'http://localhost:8099/inc/ajax_getpage.jsp'
        // because its MIME type ('text/html') is not executable, and strict MIME type checking is enabled.
        // formdesigner.jsp引入时也会报错：*****的脚本因 Mime 类型不匹配而被阻止
        // 放开资源，ignoring的请求均不受保护，因此也没有SecurityContext，所以取不到用户名，网站前台登录页面不能置于public这些目录下，故如需在public下放登录页的话得注释掉

        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
        boolean isAccessUpfileNeedLogin = scfg.getBooleanProperty("isAccessUpfileNeedLogin");
        if (!isAccessUpfileNeedLogin) {
            // @task
            // 企业微信域名归属验证，如：WW_verify_***.txt，放在根目录下
            webSecurity
                    .ignoring()
                    .antMatchers("/**/*.css", "/**/*.js", "/**/*.html", "/**/*.ttf", "/**/*.woff", "/**/*.woff2",
                            "/**/*.png", "/**/*.jpg", "/**/*.gif",
                            "/inc/**", "/images/**",
                            // 20201220 public/robot/robotreply.jsp中创建用户时，需要通过SpringUtil取request
                            // "/public/**",
                            "/skin/**", "/css/**", "/js/**", "/dwr/**", "/activex/**",
                            "/ueditor/js/**", "/**/*.txt", "/editor_full/**", "/upfile/**", "/reportServlet");
        }
        else {
            webSecurity
                    .ignoring()
                    .antMatchers("/**/*.css", "/**/*.js", "/**/*.html", "/**/*.ttf", "/**/*.woff", "/**/*.woff2",
                            "/**/*.png", "/**/*.jpg", "/**/*.gif",
                            "/inc/**", "/images/**",
                            // 20201220 public/robot/robotreply.jsp中创建用户时，需要通过SpringUtil取request
                            // "/public/**",
                            "/skin/**", "/css/**", "/js/**", "/dwr/**", "/activex/**",
                            "/ueditor/js/**", "/**/*.txt", "/editor_full/**", "/reportServlet", "/druid/**");
        }
        webSecurity.httpFirewall(allowUrlEncodedSlashHttpFirewall());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        com.redmoon.oa.security.Config myconfig = com.redmoon.oa.security.Config.getInstance();
        String pwdName = myconfig.getProperty("pwdName");

        /*http.httpBasic()//配置HTTP基本身份验证
                    .and();*/
        // 企业微信消息回调接口 http://***.com/servlet/WXCallBack
        // 导出至word时，图片宏控件需用到img_show.jsp
        http.authorizeRequests()
                    .antMatchers("/","/index", "/login","/doLogin.do","/setup/**", "/login-error","/error.jsp", "/401",
                            "/inc/**", "/skin/**", "/images/**", "/activex/**", "/public/**", "/weixin/**", "/dingding/**",
                            "/document/**", "/static/**", "/servlet/WXCallBack", "/actuator/**", "/showImg", "/showImg.do", "/flow/macro/**", "/flow/form_js/**", "/wap/**",
                            "/mobile/**").permitAll()
                    // OPTIONS请求不验证
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 浏览器对复杂跨域请求的一种处理方式,在真正发送请求之前,会先进行一次预请求
                    .anyRequest().authenticated() // 其它所有请求都需要认证
                .and()
                    .formLogin()
                    .loginPage("/") // 如果未登录，则重定向至此路径
                    .usernameParameter("name")
                    .passwordParameter(pwdName)
                    // .defaultSuccessUrl("/index")//成功登录跳转
                    .loginProcessingUrl("/doLogin.do")
                    .failureUrl( "/login-error" )
                    .permitAll() // 登录成功后有权限访问所有页面
                    .successHandler(loginSuccessHandler)    // 成功登录处理器
                    .failureHandler(loginFailureHandler)    // 失败登录处理器
                .and()
                    .sessionManagement()
                    // 配置中如果配了如下的invalid-session-url，配置了permitAll链接首次链接系统时会跳转到登录页，将该配置删除即可解决此问题。
                    // .invalidSessionUrl("/session/invalid")    // session过期后跳转的URL
                    // 不创建session 使用jwt token不需要session
                    // .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .exceptionHandling()
                    .accessDeniedPage( "/403" )
                    .accessDeniedHandler(accessDeniedHandler); // 无权限处理，会覆盖accessDeniedPage

        // 关闭csrf跨站攻击防御
        http.csrf().disable();

        http.logout()
                .logoutUrl("/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler(logoutSuccessHandler)
                .logoutSuccessUrl( "/index" ); // 注销成功回到首页
        // 使支持iframe
        http.headers().frameOptions().disable();
        // 相当于设置X-Frame-Options，表示该页面可以在相同域名页面的 frame 中展示
        // http.headers().frameOptions().sameOrigin();

        // 禁用缓存
        http.headers().cacheControl();

        // 添加JWT filter，在UsernamePasswordAuthenticationFilter之前运行
        // http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        // LogoutFilter在jwtFilter之前拦截了请求，所以会导致LogoutHandler中获取到的authentication为null，因而需addFilterBefore
        // 否则在MyLogoutHandler及MyLogoutSuccessHandler中authentication将为null
        http.addFilterBefore(jwtFilter, LogoutFilter.class);
    }
}
