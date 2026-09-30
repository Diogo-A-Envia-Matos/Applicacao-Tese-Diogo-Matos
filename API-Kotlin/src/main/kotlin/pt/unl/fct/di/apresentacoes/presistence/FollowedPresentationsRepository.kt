package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.FollowedPresentationsDAO

interface FollowedPresentationsRepository : CrudRepository<FollowedPresentationsDAO, Long> {

    fun findByUserId(userID: Long): Iterable<FollowedPresentationsDAO>
}