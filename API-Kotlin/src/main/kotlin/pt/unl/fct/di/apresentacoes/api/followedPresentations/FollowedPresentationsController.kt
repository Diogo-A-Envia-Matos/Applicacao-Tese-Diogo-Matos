package pt.unl.fct.di.apresentacoes.api.followedPresentations

import org.springframework.web.bind.annotation.RestController
import pt.unl.fct.di.apresentacoes.api.dto.FollowedPresentationsDTO
import pt.unl.fct.di.apresentacoes.api.exceptions.APINotFoundException
import pt.unl.fct.di.apresentacoes.api.exceptions.APIUnprocessableEntityException
import pt.unl.fct.di.apresentacoes.api.exceptions.ComputationInProgress
import pt.unl.fct.di.apresentacoes.api.exceptions.ChangesNotAllowed
import pt.unl.fct.di.apresentacoes.services.FollowedPesentationsNotFound
import pt.unl.fct.di.apresentacoes.services.FollowedPresentationsService
import pt.unl.fct.di.apresentacoes.services.FollowedPresentationsUnprocessable
import pt.unl.fct.di.apresentacoes.services.ComputationService
import pt.unl.fct.di.apresentacoes.services.StopChangesService
import pt.unl.fct.di.apresentacoes.services.PresentationNotFound
import pt.unl.fct.di.apresentacoes.services.StopChangesNotFound

private const val COMPUTATION_IN_PROGRESS = "There is a computation in progress."
private const val CHANGES_NOT_ALLOWED = "Currently, changes are not allowed to be performed."

@RestController
class FollowedPresentationsController(val followedPresentations: FollowedPresentationsService,
                                      val computation: ComputationService,
                                      val stopChanges: StopChangesService
                                      ): FollowedPresentationsAPI {

    override fun addFollowedPresentations(presentations: FollowedPresentationsDTO): FollowedPresentationsDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            return FollowedPresentationsDTO(followedPresentations.add(presentations))
        } catch (e: FollowedPresentationsUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun getAllFollowedPresentations(): Collection<FollowedPresentationsDTO> =
        followedPresentations.getAllFollowedPresentations().map { e -> FollowedPresentationsDTO(e) }

    override fun getUserFollowedPresentations(uid: Long): Collection<FollowedPresentationsDTO> {
        return followedPresentations.getUserFollowedPresentations(uid).map { e -> FollowedPresentationsDTO(e) }
    }

    override fun deleteFollowedPresentations(fpid: Long) {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            followedPresentations.deleteOneFollowedPresentations(fpid)
        } catch (e: FollowedPesentationsNotFound){
            throw APINotFoundException(e.message)
        }
    }

    private fun privateCheckComputation() {
        try {
            if (computation.getComputationStatus()) {
                throw ComputationInProgress(COMPUTATION_IN_PROGRESS)
            }
        } catch (e:PresentationNotFound) {
            throw APINotFoundException(e.message)
        }
    }

    private fun privateCheckStopChanges() {
        try {
            if (stopChanges.getStateOfChangesOfRestrictions()) {
                throw ChangesNotAllowed(CHANGES_NOT_ALLOWED)
            }
        } catch (e:StopChangesNotFound) {
            throw APINotFoundException(e.message)
        }
    }
}