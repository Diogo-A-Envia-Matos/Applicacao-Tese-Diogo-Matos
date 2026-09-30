package pt.unl.fct.di.apresentacoes.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.FollowedPresentationsDTO
import pt.unl.fct.di.apresentacoes.domain.FollowedPresentationsDAO
import pt.unl.fct.di.apresentacoes.presistence.FollowedPresentationsRepository
import pt.unl.fct.di.apresentacoes.presistence.PresentationRepository
import pt.unl.fct.di.apresentacoes.presistence.UserRepository

private const val FOLLOWED_PRESENTATIONS_NOT_FOUND = "There is no followedPresentations with the id received."
private const val DEFAULT_NR_FOLLOWED_PRESENTATIONS = 2

@Service
class FollowedPresentationsService(val followedPresentations: FollowedPresentationsRepository,
                                   val users: UserRepository,
                                   val presentations: PresentationRepository) {

    @Transactional
    fun add(followedPresentationsDTO: FollowedPresentationsDTO): FollowedPresentationsDAO {
        if (followedPresentationsDTO.presentationsIDS.size != DEFAULT_NR_FOLLOWED_PRESENTATIONS)
            throw FollowedPresentationsUnprocessable("There can only be $DEFAULT_NR_FOLLOWED_PRESENTATIONS followed presentations.")

        val existingFollowedPresentations = followedPresentations.findAll()
        val existingPresentationIDS = existingFollowedPresentations.flatMap { it.presentations.map { e -> e.id } }
        val newPresentationIDS = followedPresentationsDTO.presentationsIDS.filterNot { existingPresentationIDS.contains(it) }

        if (newPresentationIDS.isEmpty()) {
            throw FollowedPresentationsUnprocessable("All presentations are already being followed.")
        }

        val existingPresentationCounts = mutableMapOf<Long, Int>()

        for (followedPresentation in existingFollowedPresentations) {
            followedPresentation.presentations.forEach {
                existingPresentationCounts[it.id] = existingPresentationCounts.getOrDefault(it.id, 0) + 1
            }
        }

        followedPresentationsDTO.presentationsIDS.forEach { presentationID ->
            val count = existingPresentationCounts.getOrDefault(presentationID, 0)
            if (count + 1 > 2) {
                throw FollowedPresentationsUnprocessable("Presentation $presentationID is already followed in two other slots.")
            }
        }

        return followedPresentations.save(buildDAO(followedPresentationsDTO))
    }

    fun getAllFollowedPresentations():Iterable<FollowedPresentationsDAO> = followedPresentations.findAll()

    fun getUserFollowedPresentations(uid: Long) : Iterable<FollowedPresentationsDAO>{
        return followedPresentations.findByUserId(uid)
    }

    fun deleteOneFollowedPresentations(fpid:Long){
        followedPresentations.findById(fpid)
            .orElseThrow { FollowedPesentationsNotFound() }
        followedPresentations.deleteById(fpid)
    }
    private fun buildDAO(followedPresentationsDTO: FollowedPresentationsDTO): FollowedPresentationsDAO{
        val presentationsIDS = presentations.findAllById(followedPresentationsDTO.presentationsIDS).toMutableList()
        if(presentationsIDS.size != followedPresentationsDTO.presentationsIDS.size){
            throw FollowedPresentationsUnprocessable("The presentations ids entered do not exist.")
        }
        val user = users.findById(followedPresentationsDTO.userID).orElseThrow { FollowedPresentationsUnprocessable("The user does not exist.") }
        return FollowedPresentationsDAO(0, user,presentationsIDS)
    }

}

class FollowedPesentationsNotFound(message: String? = FOLLOWED_PRESENTATIONS_NOT_FOUND) : RuntimeException(message)
class FollowedPresentationsUnprocessable(message: String) : RuntimeException(message)