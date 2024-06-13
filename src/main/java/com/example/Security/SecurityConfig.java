package com.example.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    //Configure Security Filter chain - chain (цепочка) of methods , that will control how your security works
    UserDetailsService userDetailsService;
    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService) {this.userDetailsService = userDetailsService;}

    @Bean
    public static PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // отключает защиту от межсайтовой подделки запросов (CSRF). В реальном приложении это может быть небезопасно, и вы должны включить защиту CSRF, если ваше приложение подвержено этому типу атак.
                .authorizeRequests(authorize -> authorize
                        .requestMatchers(new AntPathRequestMatcher("/home")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/home/find")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/assets/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/login")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/register/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/static/css/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/static/js/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/static/assets/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                        // own security logic in controller
                        .requestMatchers(new AntPathRequestMatcher("/news/actions/{newsId}")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/news/actions/{newsId}/comments/{commentId}")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/comments/{newsId}/save")).permitAll()

                        .requestMatchers(new AntPathRequestMatcher("/news/delete/{newsId}")).hasAuthority("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/users/delete/{userId}")).permitAll()
                        // nobody can delete ADMIN user (even ADMIN himself)
                        .requestMatchers(new AntPathRequestMatcher("/users/delete/1")).denyAll()
                        .anyRequest().authenticated()) //other URLs are only allowed authenticated users.

                // Это указывает, что запросы к определенным URL-адресам должны быть разрешены для всех пользователей, включая анонимных.
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home?successLogin",true)
                        .loginProcessingUrl("/login")
                        .failureUrl("/login?error=true")// = ?fail
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .permitAll()
                );
        return http.build();}
    //Authentication Manager
    public void configure(AuthenticationManagerBuilder builder) throws Exception
    {
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
}