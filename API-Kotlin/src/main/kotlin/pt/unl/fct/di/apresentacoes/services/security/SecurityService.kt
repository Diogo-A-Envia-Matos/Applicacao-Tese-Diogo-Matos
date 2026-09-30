package pt.unl.fct.di.apresentacoes.services.security

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeletePresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.FollowedPresentationsDTO
import pt.unl.fct.di.apresentacoes.api.dto.ChangeAdditionalUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteGroupOfUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteGroupOfPresentationUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.domain.UserRoles
import pt.unl.fct.di.apresentacoes.presistence.*

@Component("SecurityService")
@Service
class SecurityService(
    val userRepository: UserRepository,
    val presentationRepository: PresentationRepository,
    val unavailabilityRepository: UnavailabilityRepository,
    val presentationUnavailabilityRepository: PresentationUnavailabilityRepository,
    val followedPresentationsRepository: FollowedPresentationsRepository,
    ) {

    fun isPrincipal(principal: UserDetails, userID: Long): Boolean {
        val user = userRepository.findById(userID)
        val user2 = userRepository.findOneByEmail(principal.username)
        return user.isPresent && user2.isPresent && user.get().id == user2.get().id
    }

    fun isAdminOrOrganizer(principal: UserDetails): Boolean{
        val user = userRepository.findOneByEmail(principal.username)
        return user.isPresent && (user.get().role == UserRoles.ADMIN || user.get().role == UserRoles.ORGANIZER)
    }

    fun isAdmin(principal: UserDetails): Boolean{
        val user = userRepository.findOneByEmail(principal.username)
        return user.isPresent && user.get().role == UserRoles.ADMIN
    }

    fun isOwnerOfUnavailability(principal: UserDetails, unavailabilityID: Long): Boolean{
        val user = userRepository.findOneByEmail(principal.username)
        val unavailability = unavailabilityRepository.findById(unavailabilityID)
        if(!user.isPresent && !unavailability.isPresent)
            return false
        return user.get().id == unavailability.get().user.id
    }

    fun isOwnerOfPresentationUnavailability(principal: UserDetails, presentationUnavailabilityID: Long): Boolean{
        val user = userRepository.findOneByEmail(principal.username)
        val presentationUnavailability = presentationUnavailabilityRepository.findById(presentationUnavailabilityID)
        if (!user.isPresent && !presentationUnavailability.isPresent)
            return false
        return user.get().id == presentationUnavailability.get().user.id
    }

    fun isAdviserOrArguerOfPresentation(principal: UserDetails, presentationID: Long): Boolean{
        val user = userRepository.findOneByEmail(principal.username)
        val presentation = presentationRepository.findById(presentationID)
        if(!user.isPresent && !presentation.isPresent){
            return false
        }
        return user.get().id == presentation.get().adviser.id || user.get().id == presentation.get().arguer.id
    }

    fun isAdviserOrArguerOfAllPresentations(principal: UserDetails, presentations: FollowedPresentationsDTO): Boolean{
        val user = userRepository.findOneByEmail(principal.username)
        if(!user.isPresent)
            return false
        for (presentationID in presentations.presentationsIDS){
            val presentation = presentationRepository.findById(presentationID)
            if(!presentation.isPresent ||
                (presentation.get().arguer.id != user.get().id &&
                presentation.get().adviser.id != user.get().id)){
                return false
            }
        }
        return true
    }

    fun isOwnerOfFollowedPresentations(principal: UserDetails, followedPresentationID: Long): Boolean{
        val user = userRepository.findOneByEmail(principal.username)
        val followedPresentation = followedPresentationsRepository.findById(followedPresentationID)
        if(!followedPresentation.isPresent || !user.isPresent)
            return false
        return followedPresentation.get().user.id == user.get().id
    }

    fun canAddAndDeletePresentationUnavailability(principal: UserDetails, presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): Boolean{
        val user = userRepository.findOneByEmail(principal.username)
        val presentation = presentationRepository.findById(presentationUnavailabilityDTO.presentationID)
        if(!user.isPresent || !presentation.isPresent)
            return false
        return presentation.get().adviser.id == user.get().id
    }

    fun canAddAndDeleteUnavailability(principal: UserDetails, unavailabilityDTO: AddOrDeleteUnavailabilityDTO): Boolean{
        val principalUser = userRepository.findOneByEmail(principal.username)
        val userInUnavailability = userRepository.findById(unavailabilityDTO.userID)
        return principalUser.isPresent
                &&
                userInUnavailability.isPresent
                &&
                principalUser.get().id == userInUnavailability.get().id
    }

    fun canAddAndDeleteGroupUnavailability(principal: UserDetails, unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): Boolean {
        val principalUser = userRepository.findOneByEmail(principal.username)
        val userInUnavailability = userRepository.findById(unavailabilityGroupDTO.userID)
        return principalUser.isPresent
                &&
                userInUnavailability.isPresent
                &&
                principalUser.get().id == userInUnavailability.get().id
    }

    fun canAddAndDeletePresentationGroupUnavailability(principal: UserDetails, presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): Boolean {
        val principalUser = userRepository.findOneByEmail(principal.username)
        val presentation = presentationRepository.findById(presentationUnavailabilityGroupDTO.presentationID)
        return principalUser.isPresent
                &&
                presentation.isPresent
                &&
                principalUser.get().id == presentation.get().adviser.id
    }

    fun canChangeAdditionalUnavailabilities(principal: UserDetails, additionalUnavailabilityDTO: AddOrDeleteUnavailabilityDTO): Boolean {
        val principalUser = userRepository.findOneByEmail(principal.username)
        // val userChangingUnavailabilities = userRepository.findById(additionalUnavailabilityDTO.userID)
        return principalUser.isPresent
                // &&
                // userChangingUnavailabilities.isPresent
                // &&
                // principalUser.get().id == userChangingUnavailabilities.get().id
    }

    fun canChangeAdditionalUserUnavailabilities(principal: UserDetails, unavailabilityDTO: AddOrDeleteUnavailabilityDTO): Boolean {
        val principalUser = userRepository.findOneByEmail(principal.username)
        // val userChangingUnavailabilities = userRepository.findById(additionalUnavailabilityDTO.userID)
        return principalUser.isPresent
                // &&
                // userChangingUnavailabilities.isPresent
                // &&
                // principalUser.get().id == userChangingUnavailabilities.get().id
    }

    fun canChangeAdditionalPresentationUnavailabilities(principal: UserDetails, unavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): Boolean {
        val principalUser = userRepository.findOneByEmail(principal.username)
        // val userChangingUnavailabilities = userRepository.findById(additionalUnavailabilityDTO.userID)
        return principalUser.isPresent
                // &&
                // userChangingUnavailabilities.isPresent
                // &&
                // principalUser.get().id == userChangingUnavailabilities.get().id
    }

    // fun canChangeAdditionalUserUnavailabilities(principal: UserDetails, additionalUserUnavailabilityDTO: ChangeUserAdditionalUnavailabilitiesDTO): Boolean{
    //     val principalUser = userRepository.findOneByEmail(principal.username)
    //     val userChangingUnavailabilities = userRepository.findById(additionalUserUnavailabilityDTO.userID)
    //     return principalUser.isPresent
    //             &&
    //             userChangingUnavailabilities.isPresent
    //             &&
    //             principalUser.get().id == userChangingUnavailabilities.get().id
    // }

    // fun CanChangeAdditionalPresentationUnavailabilities(principal: UserDetails, additionalPresentationUnavailabilityDTO: ChangePresentationAdditionalUnavailabilitiesDTO): Boolean{
    //     val principalUser = userRepository.findOneByEmail(principal.username)
    //     val userChangingUnavailabilities = userRepository.findById(additionalPresentationUnavailabilityDTO.userID)
    //     return principalUser.isPresent
    //             &&
    //             userChangingUnavailabilities.isPresent
    //             &&
    //             principalUser.get().id == userChangingUnavailabilities.get().id
    // }

    // fun canDeleteUser(principal: UserDetails, userID: Long): Boolean {
    //     val user = userRepository.findById(userID)
    //     val principalUser = userRepository.findOneByEmail(principal.username)
    //     if(!user.isPresent && !principalUser.isPresent)
    //         return false
    //     return principalUser.get().role == UserRoles.ORGANIZER && user.get().role == UserRoles.USER
    // }

}