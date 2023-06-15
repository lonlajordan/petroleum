package com.petroleum.security;

import com.petroleum.enums.Role;
import com.petroleum.models.User;
import com.petroleum.repositories.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
    public static String ADMIN_EMAIL;
    public static String ADMIN_PASSWORD;


    @Value("${admin.email}")
    public void setAdminEmail(String adminEmail) {
        ADMIN_EMAIL = adminEmail;
    }

    @Value("${admin.password}")
    public void setAdminPassword(String adminPassword) {
        ADMIN_PASSWORD = adminPassword;
    }

    @Bean
    protected WebSecurityCustomizer ignoringCustomizer(){
        return (web) -> web.ignoring().antMatchers("/css/**", "/js/**", "/images/**", "/fonts/**");
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf().disable()
            .exceptionHandling()
                .accessDeniedPage("/error/403")
                .and()
            .formLogin()
                .usernameParameter("email")
                .loginPage("/")
                .loginProcessingUrl("/")
                .defaultSuccessUrl("/home", true)
                .failureHandler(new AuthenticationFailureHandler())
                .and()
            .logout()
                .deleteCookies("JSESSIONID")
                .logoutUrl("/logout")
                .logoutSuccessHandler(new AuthenticationLogoutSuccessHandler())
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
            .and()
            .authorizeRequests()
                .antMatchers("/", "/validate", "/fuels/view/**").permitAll()
                .antMatchers("/users/**").hasAuthority("ROLE_ADMIN")
                .antMatchers("/supplies/**", "/products/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DIRECTOR")
                .anyRequest().authenticated();
        return http.build();
    }

    @Component
    public static class CustomAuthenticationProvider implements AuthenticationProvider {
        private final UserRepository userRepository;

        public CustomAuthenticationProvider(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            String email = StringUtils.lowerCase(authentication.getName());
            String password = (String) authentication.getCredentials();
            User user = new User();
            if(SecurityConfig.ADMIN_EMAIL.equals(email)){
                if(!new BCryptPasswordEncoder().matches(password, SecurityConfig.ADMIN_PASSWORD)) throw new BadCredentialsException("incorrect.password");
                user.setRole(Role.ROLE_ADMIN);
                user.setEmail(email);
                user.setId(0L);
            }else{
                user = userRepository.findByEmail(email);
                if(user == null || !user.getEmail().equals(email)) throw new BadCredentialsException("incorrect.username");
                if(!new BCryptPasswordEncoder().matches(password, user.getPassword())) throw new BadCredentialsException("incorrect.password");
                if(!user.isEnabled()) throw new BadCredentialsException("account.disabled");
                user.setLastLogin(LocalDateTime.now());
                user = userRepository.save(user);
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attributes.getRequest().getSession(true);
            session.setAttribute("user", user);
            Collection<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
            return new UsernamePasswordAuthenticationToken(email, password, authorities);
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return authentication.equals(UsernamePasswordAuthenticationToken.class);
        }

    }

    private static class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
            String url = "/";
            if(exception != null) {
                String message = exception.getMessage();
                if("incorrect.username".equals(message)){
                    url = "/?error=1";
                }else if("incorrect.password".equals(message)){
                    url = "/?error=2";
                }else if("account.disabled".equals(message)){
                    url = "/?error=3";
                }
            }
            RequestDispatcher dispatcher = request.getRequestDispatcher(url);
            dispatcher.forward(request, response);
        }
    }

    private static class AuthenticationLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
        @Override
        public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

}
