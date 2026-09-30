package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.AdditionalUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.UnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.UserDAO
import java.time.LocalDate

interface AdditionalUnavailabilityRepository : CrudRepository<AdditionalUnavailabilityDAO,Long>{

    fun findByUserIdAndUnsuitable(userID: Long, unsuitable: Boolean): AdditionalUnavailabilityDAO
}