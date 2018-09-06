package com.docker.atsea.configuration;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.*;

@Component
@EnableWebMvc
public class WebConfiguration extends WebMvcConfigurerAdapter {
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**").addResourceLocations("file:/static/");
		registry.addResourceHandler("swagger-ui.html")
				.addResourceLocations("classpath:/META-INF/resources/");

		registry.addResourceHandler("/webjars/**")
				.addResourceLocations("classpath:/META-INF/resources/webjars/");
	}
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("redirect:/index.html");
	}

        @Override
        public void configurePathMatch(PathMatchConfigurer configurer) {
        	AntPathMatcher matcher = new AntPathMatcher();
        	matcher.setCaseSensitive(false);
       		configurer.setPathMatcher(matcher);
    	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorPathExtension(true).
				favorParameter(true).
				defaultContentType(MediaType.APPLICATION_JSON).
				mediaType("xml", MediaType.APPLICATION_XML);
	}
}
