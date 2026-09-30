package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.PresentationDAO
import pt.unl.fct.di.apresentacoes.domain.PresentationUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.UserDAO

interface PresentationUnavailabilityRepository : CrudRepository<PresentationUnavailabilityDAO, Long> {

    // fun findByPresentationId(presentationID: Long): PresentationUnavailabilityDAO
    fun findByPresentationIdAndUnsuitable(presentationID: Long, unsuitable: Boolean): PresentationUnavailabilityDAO
}