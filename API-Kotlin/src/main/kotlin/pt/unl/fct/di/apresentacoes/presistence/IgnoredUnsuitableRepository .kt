package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.IgnoredUnsuitableDAO
import pt.unl.fct.di.apresentacoes.domain.UserDAO
import java.time.LocalDate

interface IgnoredUnsuitableRepository : CrudRepository<IgnoredUnsuitableDAO,Long>{

    fun findByUserId(userID: Long): IgnoredUnsuitableDAO
}