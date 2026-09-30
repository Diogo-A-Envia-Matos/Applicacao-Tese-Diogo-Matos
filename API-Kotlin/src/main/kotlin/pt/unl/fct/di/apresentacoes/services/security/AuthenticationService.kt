package pt.unl.fct.di.apresentacoes.services.security

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.AuthResponseDTO
import pt.unl.fct.di.apresentacoes.api.dto.ForgotPasswordDTO
import pt.unl.fct.di.apresentacoes.api.dto.LoginDTO
import pt.unl.fct.di.apresentacoes.api.dto.LoginGoogleDTO
import pt.unl.fct.di.apresentacoes.api.dto.CreateUserDTO
import pt.unl.fct.di.apresentacoes.api.dto.CreateIdentityDTO
import pt.unl.fct.di.apresentacoes.api.dto.GetIdentityDTO
import pt.unl.fct.di.apresentacoes.domain.UserDAO
import pt.unl.fct.di.apresentacoes.domain.UserIdentityDAO
import pt.unl.fct.di.apresentacoes.presistence.UserRepository
import pt.unl.fct.di.apresentacoes.presistence.UserIdentityRepository
import pt.unl.fct.di.apresentacoes.security.JwtProperties
import pt.unl.fct.di.apresentacoes.services.UserService
import pt.unl.fct.di.apresentacoes.services.UserNotFound
import pt.unl.fct.di.apresentacoes.services.UserIdentityNotFound
import pt.unl.fct.di.apresentacoes.services.mail.MailService
import pt.unl.fct.di.apresentacoes.services.passwordGeneration.PasswordGenerationService
import java.util.*

// Providers
private const val GOOGLE_PROVIDER = "Google"

private val FCT_DOMAINS: Set<String> = setOf(
    "campus.fct.unl.pt", "fct.unl.pt"
)

@Service
class AuthenticationService (
    private val autheManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties,
    val userService: UserService,
    // val userIdentities: UserIdentityRepository,
    val users: UserRepository,
    val mailService: MailService,
    val passwordGenerationService: PasswordGenerationService
){

    fun authentication(authRequest: LoginDTO): AuthResponseDTO {
        autheManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authRequest.email,
                authRequest.password
            ))

        val userDet = userDetailsService.loadUserByUsername(authRequest.email)
        
        val accessToken = tokenService.generate(
            userDetails = userDet,
            expriationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration)
        )

        return AuthResponseDTO(accessToken)
    }

    //TODO Test this authentication
    fun googleAuthentication(googleAuthRequest: LoginGoogleDTO): AuthResponseDTO {
        if (!googleAuthRequest.verified) {
            throw GoogleLoginUnverified("This request was not authorized")
        }
        val email = googleAuthRequest.email
        // println("email: " + email)
        // println("name: " + googleAuthRequest.name)
        // var userDet = userDetailsService.loadUserByUsername(email)
        // println("userDet: " + userDet)
        
        val userCheck : Optional<UserDAO> = users.findOneByEmail(email)
        // println("userCheck.isPresent(): " + userCheck.isPresent())
        
        // val user : UserDAO = users.findOneByEmail(email)
        // println("user: " + user)
        
        //TODO Handle exceptions
        // if (userDet == null) {
        if (!userCheck.isPresent()) {
        // if (user == null) {
            // println("userCheck is not present")
            //TODO Testing google login, so add domain check later
            val emailDomain = email.split("@").last()
            // println("emailDomain: " + emailDomain)
            if (FCT_DOMAINS.contains(emailDomain)) {
                val userCreation = CreateUserDTO(0L, googleAuthRequest.name, email)
                userService.addUser(userCreation)
                val user : UserDAO = users.findOneByEmail(email).get()
                // println("user.id: " + user.id)
                // println("user.name: " + user.name)
                // println("user.password: " + user.password)
                // val user : UserDAO = userService.addUser(userCreation)
                // user = userService.addUser(userCreation)
                // println("user: " + user)
                val userGoogleIdentity = CreateIdentityDTO(user.id, user.name, user.password, GOOGLE_PROVIDER, googleAuthRequest.identity)
                // println("userGoogleIdentity: " + userGoogleIdentity)
                val googleIdentity = userService.addIdentity(userGoogleIdentity)
            } else {
                throw UserNotFound()
            }
        } else {
            // println("userCheck is present")
            val user : UserDAO = users.findOneByEmail(email).get()
            // val user : UserDAO = userCheck.get()
            // println("user: " + user)
            // val identities : Iterable<UserIdentityDAO> = userIdentities.findByUserId(user.id)
            // for (iden in identities) {
            //     println("iden.id: " + iden.id)
            //     println("iden.user.id: " + iden.user.id)
            //     println("iden.provider: " + iden.provider)
            //     println("iden.providerId: " + iden.providerId)
            // }

            // //TODO Check User Identity
            // val googleIdentity = identities.find { it.provider == GOOGLE_PROVIDER }
            // // println("googleIdentity: " + googleIdentity)
            val identityGetter = GetIdentityDTO(user.id, googleAuthRequest.name, googleAuthRequest.identity)
            try {
                val googleIdentity = userService.getOneIdentity(identityGetter)
            } catch (e: UserIdentityNotFound) {
                // println("googleIdentity not found")
                val userGoogleIdentity = CreateIdentityDTO(user.id, user.name, user.password, GOOGLE_PROVIDER, googleAuthRequest.identity)
                userService.addIdentity(userGoogleIdentity)
            }
            // val googleIdentity = userService.getOneIdentity(identityGetter)
            // val googleIdentity = userIdentities.findByUserIdAndProviderAndProviderId(findByUserIdAndProviderAndProviderId)
            // if (googleIdentity == null) {
            //     // println("googleIdentity not found")
            //     val userGoogleIdentity = CreateIdentityDTO(user.id, user.name, user.password, GOOGLE_PROVIDER, googleAuthRequest.identity)
            //     userService.addIdentity(userGoogleIdentity)
            // }
        }

        var userDet = userDetailsService.loadUserByUsername(googleAuthRequest.email)
        // println("userDet: " + userDet)

        val accessToken = tokenService.generate(
            userDetails = userDet,
            expriationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration)
        )

        return AuthResponseDTO(accessToken)
    }

    @Transactional
    fun forgotPassword(forgotPasswordDTO: ForgotPasswordDTO): UserDAO {
        val user = users.findOneByEmail(forgotPasswordDTO.email)
            .orElseThrow { ForgotPasswordUnprocessable("There is no account with the given email.") }

        val newPassword = passwordGenerationService.generateRandomPassword()
        user.password = BCryptPasswordEncoder().encode(newPassword)
        mailService.sendForgotPasswordMail(forgotPasswordDTO.email, newPassword)
        return users.save(user)
    }

}

class ForgotPasswordUnprocessable(message: String) : RuntimeException(message)
class GoogleLoginUnverified(message: String) : RuntimeException(message)