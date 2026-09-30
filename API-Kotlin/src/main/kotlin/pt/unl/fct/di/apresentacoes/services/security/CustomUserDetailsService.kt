package pt.unl.fct.di.apresentacoes.services.security

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import pt.unl.fct.di.apresentacoes.services.UserService

typealias  ApplicationUser = pt.unl.fct.di.apresentacoes.domain.UserDAO


@Service
class CustomUserDetailsService(private val users: UserService) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails =
        users.getOneByEmail(email)
            ?.mapToUserDetails()
            ?: throw UsernameNotFoundException("Not found!")

    private fun ApplicationUser.mapToUserDetails(): UserDetails =
        User.builder()
            .username(this.email)
            .password(this.password)
            .roles(this.role.name)
            .build()
}