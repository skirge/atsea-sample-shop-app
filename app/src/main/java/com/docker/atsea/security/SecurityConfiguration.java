package com.docker.atsea.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;



@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
		
	// Admin pages use HTTP Basic authentication against the existing customer table.
	
    @Autowired
    DataSource dataSource;
 
    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                // The dojo's existing customer passwords are stored as plain text.
                .passwordEncoder(NoOpPasswordEncoder.getInstance())
                .usersByUsernameQuery("SELECT username, password, enabled FROM customer WHERE username = ?")
               .authoritiesByUsernameQuery("SELECT username, role FROM customer WHERE username=?");
    }
	
    
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http.authorizeRequests()
			.requestMatchers(new AntPathRequestMatcher("/admin/**", null, false)).hasAuthority("ADMIN")
			.anyRequest().permitAll()
			.and().httpBasic()
			.and().csrf().disable();
	}	

}
