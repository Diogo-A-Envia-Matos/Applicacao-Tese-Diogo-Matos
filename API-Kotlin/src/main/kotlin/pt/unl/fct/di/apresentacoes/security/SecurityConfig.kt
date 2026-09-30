package pt.unl.fct.di.apresentacoes.security

import jakarta.servlet.DispatcherType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.DispatcherTypeRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import pt.unl.fct.di.apresentacoes.presistence.UserRepository
import pt.unl.fct.di.apresentacoes.services.UserService

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig (
    private val authenticationProvider: AuthenticationProvider
){

    @Bean
    fun filterChain(http: HttpSecurity,
                    jwtAuthenticationFilter: JwtAuthenticationFilter
    ): DefaultSecurityFilterChain =
        http
            .cors()
            .and()
            .csrf() { it.disable() }
            .authorizeHttpRequests{
                it
                    // .anyRequest()
                    // .permitAll()
                    .requestMatchers("/api/auth/login","/api/auth/login/google","/api/auth/forgotMyPassword","/error","/api/user/addInitial","/api/slot/addInitial","/api/room/addInitial","/api/presentation/addInitial","/api/python/addInitial","/api/stopChanges/addInitial")
                    .permitAll()
                    //.requestMatchers(HttpMethod.POST,"/api/user/add")
                    //.permitAll()
                    //.requestMatchers("/api/user**")
                    //.hasRole("ADMIN")
                    .anyRequest()
                    .fullyAuthenticated()
            }
            .sessionManagement{
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()


    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration().applyPermitDefaultValues()
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }

}