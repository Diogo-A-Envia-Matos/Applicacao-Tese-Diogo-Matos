package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.UnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.UserDAO
import java.time.LocalDate

interface UnavailabilityRepository : CrudRepository<UnavailabilityDAO,Long>{

    // fun findByUserId(userID: Long): UnavailabilityDAO
    fun findByUserIdAndUnsuitable(userID: Long, unsuitable: Boolean): UnavailabilityDAO
}