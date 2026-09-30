package pt.unl.fct.di.apresentacoes.api.users

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import pt.unl.fct.di.apresentacoes.api.dto.*
import pt.unl.fct.di.apresentacoes.api.exceptions.APIConflictException
import pt.unl.fct.di.apresentacoes.api.exceptions.APINotFoundException
import pt.unl.fct.di.apresentacoes.api.exceptions.APIUnprocessableEntityException
import pt.unl.fct.di.apresentacoes.domain.UserRoles
import pt.unl.fct.di.apresentacoes.services.*
import java.security.Principal

@RestController
class UserController(val users: UserService) : UserAPI {

    override fun addInitialUsers() {
        try{
            users.addInitialUsers()
        } catch (e: ThereAreUsers){
            throw APIConflictException(e.message)
        }
    }

    override fun addUser(user: CreateUserDTO): FullUserDTO {
        try{
            return FullUserDTO(users.addUser(user))
        } catch (e:UserAlreadyExists){
            throw APIConflictException(e.message)
        }
    }

    override fun getUser(uid: Long): FullUserDTO {
        try {
            return FullUserDTO(users.getOneUser(uid))
        } catch (e: UserNotFound) {
            throw APINotFoundException(e.message)
        }
    }

    override fun getAllUsers(): Collection<FullUserDTO> =
        users.getAllUsers().map { e -> FullUserDTO(e) }

    override fun getAllUsersNames(): Collection<UserNameDTO> =
        users.getAllUsers().map { e -> UserNameDTO(e) }

    // override fun deleteSelfUser(principal: Principal, deleteSelfDTO: DeleteSelfDTO) {
    override fun deleteSelfUser(uid: Long) {
        try {
            // print("deleteSelfDTO: " + deleteSelfDTO)
            // users.deleteSelfUser(principal.name)
            users.deleteSelfUser(uid)
        } catch (e: UserNotFound) {
            throw APINotFoundException(e.message)
        } catch (e: UserUnprocessable) {
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteUser(uid: Long) {
        try {
            users.deleteOneUser(uid)
        } catch (e: UserNotFound) {
            throw APINotFoundException(e.message)
        } catch (e: UserUnprocessable) {
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun changeRole(changeRoleDTO: ChangeRoleDTO): FullUserDTO {
        try{
            return FullUserDTO(users.changeRole(changeRoleDTO))
        } catch (e: UserNotFound) {
            throw APINotFoundException(e.message)
        } catch (e: UserUnprocessable) {
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun changePassword(principal: Principal, changePasswordDTO: ChangePasswordDTO): FullUserDTO {
        try {
            return FullUserDTO(users.changePassword(principal.name, changePasswordDTO))
        } catch (e: UserNotFound) {
            throw APINotFoundException(e.message)
        } catch (e: UserConflict) {
            throw APIConflictException(e.message)
        }
    }

    override fun sendAssignmentEmail() {
        try {
            users.sendAssignmentEmail()
        } catch (e: UserNotFound) {
            throw APINotFoundException(e.message)
        }
    }

}