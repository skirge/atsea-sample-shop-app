package com.docker.atsea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.docker.atsea.configuration.JpaConfiguration;
import com.docker.atsea.controller.LoginController;
import com.docker.atsea.security.JwtFilter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger1.annotations.EnableSwagger;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Import(JpaConfiguration.class)
@SpringBootApplication(scanBasePackages={"com.docker.atsea"})
@EntityScan("com.docker.atsea.model")
@EnableJpaRepositories("com.docker.atsea.repository")
@EnableSwagger2
@EnableSwagger
@Configuration
public class AtSeaApp {

	@Bean
    public FilterRegistrationBean jwtFilter() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new JwtFilter());
        registrationBean.addUrlPatterns("/purchase/*");
        registrationBean.addUrlPatterns("/api/order/*");

        return registrationBean;
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

	public static void main(String[] args) {
		SpringApplication.run(AtSeaApp.class, args);
	}
}
