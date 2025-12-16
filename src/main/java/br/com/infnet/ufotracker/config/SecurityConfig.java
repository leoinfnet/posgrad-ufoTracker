package br.com.infnet.ufotracker.config;

import br.com.infnet.ufotracker.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration conf) {
        return conf.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userComum = User.withUsername("user")
                .password(passwordEncoder().encode("123456"))
                .roles("USER")
                .build();
        UserDetails userAdm = User.withUsername("admin")
                .password(passwordEncoder().encode("123456"))
                .roles("ADMIN", "USER")
                .build();

        return new InMemoryUserDetailsManager(userComum,userAdm);
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        //Liberado para todos
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/search").permitAll()

                        .requestMatchers(HttpMethod.GET,"/avistamentos/**").hasAnyRole("ADMIN", "USER")

                        .requestMatchers(HttpMethod.POST,"/avistamentos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/avistamentos/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
