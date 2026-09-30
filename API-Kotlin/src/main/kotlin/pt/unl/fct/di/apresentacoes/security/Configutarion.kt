package pt.unl.fct.di.apresentacoes.security

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import pt.unl.fct.di.apresentacoes.services.UserService
import pt.unl.fct.di.apresentacoes.services.security.CustomUserDetailsService

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class Configutarion {

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun userDetailsService(userService: UserService): UserDetailsService =
        CustomUserDetailsService(userService)

    @Bean
    fun authenticationProvider(userService: UserService): AuthenticationProvider =
        DaoAuthenticationProvider()
            .also {
                it.setUserDetailsService(userDetailsService(userService))
                it.setPasswordEncoder(passwordEncoder())
            }
}