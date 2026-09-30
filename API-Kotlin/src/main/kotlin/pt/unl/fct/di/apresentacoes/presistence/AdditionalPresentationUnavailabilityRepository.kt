package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.PresentationDAO
import pt.unl.fct.di.apresentacoes.domain.PresentationUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.AdditionalPresentationUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.UserDAO

interface AdditionalPresentationUnavailabilityRepository : CrudRepository<AdditionalPresentationUnavailabilityDAO, Long> {

    fun findByPresentationIdAndUnsuitable(presentationID: Long, unsuitable: Boolean): AdditionalPresentationUnavailabilityDAO
}