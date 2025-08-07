package com.example.backend.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class FilterConfig {

    // No need to register JwtAuthenticationFilter here as it's already added in SecurityConfig
    // This class is kept for any additional filters that might be needed in the future
    
    // Example of how to add additional filters if needed:
    /*
    @Bean
    public FilterRegistrationBean<Filter> someOtherFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
                throws IOException, ServletException {
                // Filter logic here
                filterChain.doFilter(request, response);
            }
        });
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registrationBean;
    }
    */
}
