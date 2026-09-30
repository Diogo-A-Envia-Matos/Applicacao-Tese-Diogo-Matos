package pt.unl.fct.di.apresentacoes.security

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import pt.unl.fct.di.apresentacoes.services.UserService
import pt.unl.fct.di.apresentacoes.services.security.CustomUserDetailsService
import pt.unl.fct.di.apresentacoes.services.security.TokenService

@Component
class JwtAuthenticationFilter(
    private val userDetailsService: CustomUserDetailsService,
    private val tokenService: TokenService,
    private val users: UserService
) : OncePerRequestFilter(){

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader: String? = request.getHeader("Authorization")

        if(authHeader.doesNotContainBearerToken()){
            filterChain.doFilter(request, response)
            return
        }
        val jwtToken = authHeader!!.extractTokenValue()

        try {
            // Check if the token is expired
            if (tokenService.isExpired(jwtToken)) {
                // Handle token expiration (e.g., return 401 Unauthorized)
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired")
                return
            }
        } catch (ex: ExpiredJwtException) {
            // Handle expired token exception
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired")
            return
        } catch (ex: NullPointerException) {
            // Handle case where token doesn't exist
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token missing")
            return
        } catch (ex: Exception) {
            // Handle other exceptions (e.g., token parsing error)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token parsing error")
            return
        }

        val email = tokenService.extractID(jwtToken)?.let { users.getOneById(it.toLong())?.email }
        if(email!= null && SecurityContextHolder.getContext().authentication == null){
            val foundUser = userDetailsService.loadUserByUsername(email)
            if(tokenService.isValid(jwtToken, foundUser)){
                updateContext(foundUser, request)
            }

            filterChain.doFilter(request, response)
        }
    }

    private fun updateContext(foundUser: UserDetails, request: HttpServletRequest){
        val authToken = UsernamePasswordAuthenticationToken(foundUser, null, foundUser.authorities)
        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)

        SecurityContextHolder.getContext().authentication = authToken
    }

    private fun String?.doesNotContainBearerToken(): Boolean =
        this == null || !this.startsWith("Bearer ")

    private fun String.extractTokenValue() : String =
        this.substringAfter("Bearer ")
}