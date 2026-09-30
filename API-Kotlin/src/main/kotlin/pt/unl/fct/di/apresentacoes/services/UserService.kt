package pt.unl.fct.di.apresentacoes.services

import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.ChangePasswordDTO
import pt.unl.fct.di.apresentacoes.api.dto.ChangeRoleDTO
import pt.unl.fct.di.apresentacoes.api.dto.CreateUserDTO
import pt.unl.fct.di.apresentacoes.api.dto.FullUserDTO
import pt.unl.fct.di.apresentacoes.api.dto.CreateIdentityDTO
import pt.unl.fct.di.apresentacoes.api.dto.GetIdentityDTO
import pt.unl.fct.di.apresentacoes.domain.UnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.AdditionalUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.IgnoredUnsuitableDAO
import pt.unl.fct.di.apresentacoes.domain.UserDAO
import pt.unl.fct.di.apresentacoes.domain.UserIdentityDAO
import pt.unl.fct.di.apresentacoes.domain.UserRoles
import pt.unl.fct.di.apresentacoes.presistence.PresentationRepository
import pt.unl.fct.di.apresentacoes.presistence.UnavailabilityRepository
import pt.unl.fct.di.apresentacoes.presistence.UserIdentityRepository
import pt.unl.fct.di.apresentacoes.presistence.AdditionalUnavailabilityRepository
import pt.unl.fct.di.apresentacoes.presistence.IgnoredUnsuitableRepository
import pt.unl.fct.di.apresentacoes.presistence.UserRepository
import pt.unl.fct.di.apresentacoes.services.mail.MailService
import pt.unl.fct.di.apresentacoes.services.passwordGeneration.PasswordGenerationService

// Exception messages
private const val THERE_ARE_USERS = "There are users in the table. This operation is made to add the initial users."
private const val USER_NOT_FOUND = "This user does not exist."
private const val USER_EXISTS = "This user already exists."
private const val USER_DOES_NOT_MATCH = "The data does not match existing user."
private const val USER_IDENTITY_NOT_FOUND = "This user identity does not exist."

data class DefaultUser(
    val name: String,
    val email: String,
    var role: UserRoles,
    var password: String,
)

data class DefaultUserIdentity(
    val provider: String,
    val providerId: String
)

private val DEFAULT_USERS: List<DefaultUser> = listOf(
    DefaultUser("Admin 1","admin@email.com", UserRoles.ADMIN,"AdminPass1"),
    DefaultUser("Admin 2","admin2@email.com", UserRoles.ADMIN,"AdminPass2"),
    DefaultUser("Organizer 1","organizer@email.com", UserRoles.ORGANIZER,"OrganizerPass1"),
    DefaultUser("Organizer 2","organizer2@email.com", UserRoles.ORGANIZER,"OrganizerPass2"),
    DefaultUser("User 1","user@email.com", UserRoles.USER,"UserPass1"),
    DefaultUser("User 2","user2@email.com", UserRoles.USER,"UserPass2"),
    DefaultUser("User 3","user3@email.com", UserRoles.USER,"UserPass3"),
    DefaultUser("User 4","user4@email.com", UserRoles.USER,"UserPass4"),
    DefaultUser("User 5","user5@email.com", UserRoles.USER,"UserPass5"),
    DefaultUser("Teste","testeaplicacao322@gmail.com", UserRoles.ADMIN,"TestPass1"),
    DefaultUser("TestGoogle Admin","tadmin07@gmail.com", UserRoles.ADMIN,"TestGoogleAdminPass1"),
    // DefaultUser("TestGoogle Admin","tadmin07@gmail.com", UserRoles.USER,"TestPass1"),
)

// private val DEFAULT_USER_IDENTITIES: List<DefaultUserIdentity> = listOf(
//     DefaultUserIdentity("Google", "101934863220450554315"),
// )

@Service
class UserService(val users: UserRepository,
                  val userIdentities: UserIdentityRepository,
                  val presentations: PresentationRepository,
                  val unavailabilities: UnavailabilityRepository,
                  val additionalUnavailabilities: AdditionalUnavailabilityRepository,
                  val ignoredUnsuitable: IgnoredUnsuitableRepository,
                  val mailService: MailService,
                  val passwordGenerationService: PasswordGenerationService
) {
    
    @Transactional
    fun addInitialUsers(){
        if(users.count().toInt() != 0)
            throw ThereAreUsers()

        // var testIdentityUser: UserDAO? = null

        for(defaultUser in DEFAULT_USERS) {
            var encodedPassword = BCryptPasswordEncoder().encode(defaultUser.password)
            var newUser = UserDAO(0, defaultUser.name, defaultUser.email, defaultUser.role, encodedPassword)
            
            val unavailabilityDAO = UnavailabilityDAO(0, false, mutableSetOf(),newUser)
            val unsuitableSlotsDAO = UnavailabilityDAO(0, true, mutableSetOf(),newUser)
            val additionalUnavailabilityDAO = AdditionalUnavailabilityDAO(0, false, mutableSetOf(),newUser)
            val additionalUnsuitableSlotsDAO = AdditionalUnavailabilityDAO(0, true, mutableSetOf(),newUser)
            val ignoredUnsuitableSlotsDAO = IgnoredUnsuitableDAO(0, mutableSetOf(),newUser)
            unavailabilities.save(unavailabilityDAO)
            unavailabilities.save(unsuitableSlotsDAO)
            additionalUnavailabilities.save(additionalUnavailabilityDAO)
            additionalUnavailabilities.save(additionalUnsuitableSlotsDAO)
            ignoredUnsuitable.save(ignoredUnsuitableSlotsDAO)
            newUser.unavailability = mutableSetOf(unavailabilityDAO, unsuitableSlotsDAO)

            users.save(newUser)

            // testIdentityUser = newUser
        }
        
        // if (testIdentityUser != null) {
        //     for(defaultUserIdentity in DEFAULT_USER_IDENTITIES) {
        //         val userIdentityDAO = UserIdentityDAO(0, testIdentityUser, defaultUserIdentity.provider, defaultUserIdentity.providerId)
        //         userIdentities.save(userIdentityDAO)
        //     }
        // }
    }

    @Transactional
    fun addUser(user: CreateUserDTO): UserDAO {
        if(users.existsByEmail(user.email) || users.existsByName(user.name))
            throw UserAlreadyExists()

        val generatedPassword = passwordGenerationService.generateRandomPassword()
        val encodedPassword = BCryptPasswordEncoder().encode(generatedPassword)

        val userDAO = UserDAO(user.id,user.name,user.email, UserRoles.USER,encodedPassword)
        // val unavailabilityDAO = UnavailabilityDAO(0,mutableSetOf(),userDAO)
        val unavailabilityDAO = UnavailabilityDAO(0, false, mutableSetOf(),userDAO)
        val unsuitableSlotsDAO = UnavailabilityDAO(0, true, mutableSetOf(),userDAO)
        val additionalUnavailabilityDAO = AdditionalUnavailabilityDAO(0, false, mutableSetOf(),userDAO)
        val additionalUnsuitableSlotsDAO = AdditionalUnavailabilityDAO(0, true, mutableSetOf(),userDAO)
        val ignoredUnsuitableSlotsDAO = IgnoredUnsuitableDAO(0, mutableSetOf(),userDAO)
        unavailabilities.save(unavailabilityDAO)
        unavailabilities.save(unsuitableSlotsDAO)
        additionalUnavailabilities.save(additionalUnavailabilityDAO)
        additionalUnavailabilities.save(additionalUnsuitableSlotsDAO)
        ignoredUnsuitable.save(ignoredUnsuitableSlotsDAO)
        userDAO.unavailability = mutableSetOf(unavailabilityDAO, unsuitableSlotsDAO)
        mailService.sendRegisteredMail(user.email, generatedPassword)
        return users.save(userDAO)
    }

    fun getOneUser(uid:Long): UserDAO {
        return users.findById(uid)
            .orElseThrow { UserNotFound() }
    }

    fun getAllUsers(): Iterable<UserDAO> = users.findAll()

    // fun deleteSelfUser(email: String) {
    @Transactional
    fun deleteSelfUser(uid: Long) {
        val user = users.findById(uid)
            .orElseThrow { UserNotFound() }
        // val user = users
        //     .findOneByEmail(email)
        //     .orElseThrow { UserNotFound() }

        // val uid = user.id
        // if(user.role == UserRoles.ADMIN)
        //     throw UserUnprocessable("You can not delete an admin.")

        //TODO DELETE UNAVAILABILITIES BEFORE DELETING USER
        // val unavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(uid, false)
        // val unsuitableSlotsDAO = unavailabilities.findByUserIdAndUnsuitable(uid, true)
        // val additionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(uid, false)
        // val additionalUnsuitableSlotsDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(uid, true)
        // val ignoredUnsuitableSlotsDAO = ignoredUnsuitable.findByUserId(uid)
        // val userIdentitiesDAO = userIdentities.findByUserId(uid)

        // unavailabilities.deleteById(unavailabilityDAO.id)
        // unavailabilities.deleteById(unsuitableSlotsDAO.id)
        // additionalUnavailabilities.deleteById(additionalUnavailabilityDAO.id)
        // additionalUnavailabilities.deleteById(additionalUnsuitableSlotsDAO.id)
        // ignoredUnsuitable.deleteById(ignoredUnsuitableSlotsDAO.id)

        // for(identity in userIdentitiesDAO){
        //     userIdentities.deleteById(identity.id)
        // }
        for(presentation in user.optional1Presentations){
            presentation.optionalParticipant1 = null
            presentations.save(presentation)
        }
        for(presentation in user.optional2Presentations){
            presentation.optionalParticipant2 = null
            presentations.save(presentation)
        }
        users.deleteById(uid)
    }

    @Transactional
    fun deleteOneUser(uid:Long) {
        val user = users.findById(uid)
            .orElseThrow { UserNotFound() }

        if(user.role == UserRoles.ADMIN)
            throw UserUnprocessable("You can not delete an admin.")

        //TODO DELETE UNAVAILABILITIES BEFORE DELETING USER
        // val unavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(uid, false)
        // val unsuitableSlotsDAO = unavailabilities.findByUserIdAndUnsuitable(uid, true)
        // val additionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(uid, false)
        // val additionalUnsuitableSlotsDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(uid, true)
        // val ignoredUnsuitableSlotsDAO = ignoredUnsuitable.findByUserId(uid)
        // val userIdentitiesDAO = userIdentities.findByUserId(uid)

        // unavailabilities.deleteById(unavailabilityDAO.id)
        // unavailabilities.deleteById(unsuitableSlotsDAO.id)
        // additionalUnavailabilities.deleteById(additionalUnavailabilityDAO.id)
        // additionalUnavailabilities.deleteById(additionalUnsuitableSlotsDAO.id)
        // ignoredUnsuitable.deleteById(ignoredUnsuitableSlotsDAO.id)

        // for(identity in userIdentitiesDAO){
        //     userIdentities.deleteById(identity.id)
        // }
        for(presentation in user.optional1Presentations){
            presentation.optionalParticipant1 = null
            presentations.save(presentation)
        }
        for(presentation in user.optional2Presentations){
            presentation.optionalParticipant2 = null
            presentations.save(presentation)
        }
        users.deleteById(uid)
    }

    @Transactional
    fun changeRole(changeRoleDTO: ChangeRoleDTO): UserDAO{
        val user = users.findById(changeRoleDTO.userID)
            .orElseThrow { UserNotFound() }
        if(user.role == UserRoles.ADMIN){
            throw UserUnprocessable("You can not change the role of an admin.")
        }
        user.role = changeRoleDTO.newRole
        return users.save(user)
    }

    fun getOneByEmail(email: String): UserDAO =
        users
            .findOneByEmail(email).orElse(null)//.orElseThrow { UserNotFound() }

    fun getOneById(uid: Long): UserDAO =
        users
            .findById(uid).orElse(null)//.orElseThrow { UserNotFound() }

    @Transactional
    fun changePassword(email: String, changePasswordDTO: ChangePasswordDTO): UserDAO {
        val user = users
            .findOneByEmail(email)
            .orElseThrow { UserNotFound() }

        if (BCryptPasswordEncoder().matches(changePasswordDTO.oldPassword, user.password)) {
            user.password = BCryptPasswordEncoder().encode(changePasswordDTO.newPassword)
            return users.save(user)
        } else {
            throw UserConflict()
        }
    }
    
    fun sendAssignmentEmail() {
        val userList: Iterable<UserDAO> = users.findAll()

        val emailList: List<String> = userList.map { e -> e.email }

        println("emailList: " + emailList)

        mailService.sendAssignmentMail(emailList)
    }


    /** USER IDENTITIES - FUNCTIONS TO MANAGE USER IDENTITIES **/


    @Transactional
    fun addIdentity(userIdentity: CreateIdentityDTO): UserIdentityDAO {
        val user = users.findById(userIdentity.userId)
            .orElseThrow { UserNotFound() }

        // println("Found user")
        // println("user.name: " + user.name)
        // println("user.password: " + user.password)
        // println("userIdentity.name: " + userIdentity.name)
        // println("userIdentity.password: " + userIdentity.password)
        // if (BCryptPasswordEncoder().matches(changePasswordDTO.oldPassword, user.password)) {
            
        //     return users.save(user)
        // } else {
        //     throw UserConflict()
        // }

        if (user.name != userIdentity.name || user.password != userIdentity.password) {
            throw UserDoesNotMatch()
        }

        val userIdentityDAO = UserIdentityDAO(0, user, userIdentity.provider, userIdentity.identity)

        // val encPass = BCryptPasswordEncoder().encode(userIdentity.identity)
        // val userIdentityDAO = UserIdentityDAO(0, user, userIdentity.provider, encPass)

        // println("userIdentityDAO.id: " + userIdentityDAO.id)
        // println("userIdentityDAO.provider: " + userIdentityDAO.provider)
        // println("userIdentityDAO.providerId: " + userIdentityDAO.providerId)
        
        return userIdentities.save(userIdentityDAO)
    }

    fun getOneIdentity(userIdentity: GetIdentityDTO): UserIdentityDAO {
        return userIdentities.findByUserIdAndProviderAndProviderId(userIdentity.userId, userIdentity.provider, BCryptPasswordEncoder().encode(userIdentity.identity))
            .orElseThrow { UserIdentityNotFound() }
    }
    
}

class ThereAreUsers(message: String? = THERE_ARE_USERS) : Exception(message)
class UserAlreadyExists(message: String? = USER_EXISTS) : Exception(message)
class UserNotFound(message: String? = USER_NOT_FOUND) : RuntimeException(message)
class UserConflict(message: String? = USER_EXISTS) : RuntimeException(message)
class UserUnprocessable(message: String) : RuntimeException(message)
class UserDoesNotMatch(message: String? = USER_DOES_NOT_MATCH) : Exception(message)
class UserIdentityNotFound(message: String? = USER_IDENTITY_NOT_FOUND) : Exception(message)

