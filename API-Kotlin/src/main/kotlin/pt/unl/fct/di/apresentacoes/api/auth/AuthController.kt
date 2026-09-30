package pt.unl.fct.di.apresentacoes.api.auth

import org.springframework.web.bind.annotation.RestController
import pt.unl.fct.di.apresentacoes.api.dto.AuthResponseDTO
import pt.unl.fct.di.apresentacoes.api.dto.ForgotPasswordDTO
import pt.unl.fct.di.apresentacoes.api.dto.FullUserDTO
import pt.unl.fct.di.apresentacoes.api.dto.LoginDTO
import pt.unl.fct.di.apresentacoes.api.dto.LoginGoogleDTO
import pt.unl.fct.di.apresentacoes.api.exceptions.APIUnprocessableEntityException
import pt.unl.fct.di.apresentacoes.services.security.AuthenticationService
import pt.unl.fct.di.apresentacoes.services.security.ForgotPasswordUnprocessable
import pt.unl.fct.di.apresentacoes.services.UserService

@RestController
class AuthController(
    private val authenticationService: AuthenticationService,
    val users: UserService
) : AuthAPI{

    override fun login(loginDTO: LoginDTO): AuthResponseDTO =
        authenticationService.authentication(loginDTO)

    override fun loginGoogle(loginGoogleDTO: LoginGoogleDTO): AuthResponseDTO =
        authenticationService.googleAuthentication(loginGoogleDTO)

    override fun forgotMyPassword(forgotPasswordDTO: ForgotPasswordDTO): FullUserDTO {
        try{
            return FullUserDTO(authenticationService.forgotPassword(forgotPasswordDTO))
        } catch (e: ForgotPasswordUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }
}