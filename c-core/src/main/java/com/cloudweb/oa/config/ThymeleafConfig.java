package com.cloudweb.oa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;

// import nz.net.ultraq.thymeleaf.LayoutDialect;

@Configuration
public class ThymeleafConfig {

/*    @Bean
    public Java8TimeDialect java8TimeDialect() {
        return new Java8TimeDialect();
    }*/

/*    private SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        engine.addDialect(new SpringSecurityDialect());
        engine.addDialect(new Java8TimeDialect());
        engine.addDialect(new DataAttributeDialect());
        engine.addDialect(new LayoutDialect());
        return engine;
    }*/

    /*@Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }*/
}
