package pt.unl.fct.di.apresentacoes.api.users

import org.springframework.web.bind.annotation.*
import pt.unl.fct.di.apresentacoes.api.dto.*
import java.security.Principal
import java.util.*


@RequestMapping("/api/user")
interface UserAPI {

    @PostMapping("/addInitial")
    fun addInitialUsers()

    @PostMapping("")
    @CanCreateUser
    fun addUser(@RequestBody user: CreateUserDTO): FullUserDTO


    @GetMapping("/{uid}")
    @CanReadOneUser
    fun getUser(@PathVariable uid:Long): FullUserDTO

    @GetMapping("")
    // @CanReadAllUsers
    fun getAllUsers(): Collection<FullUserDTO>

    @GetMapping("/names")
    fun getAllUsersNames(): Collection<UserNameDTO>

    @DeleteMapping("/self/{uid}")
    @CanDeleteSelfUser
    fun deleteSelfUser(@PathVariable uid: Long)

    @DeleteMapping("/{uid}")
    @CanDeleteUser
    fun deleteUser(@PathVariable uid:Long)

    @PutMapping("")
    @CanChangeRole
    fun changeRole(@RequestBody changeRoleDTO: ChangeRoleDTO): FullUserDTO

    @PostMapping("/changePassword")
    fun changePassword(principal: Principal, @RequestBody changePasswordDTO: ChangePasswordDTO): FullUserDTO

    @PostMapping("/mail/sendAssignmentEmail")
    @CanSendAssignmentEmail
    fun sendAssignmentEmail()

}