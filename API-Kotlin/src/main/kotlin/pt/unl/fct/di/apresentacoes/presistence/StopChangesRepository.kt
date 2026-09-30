package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.StopChangesDAO

interface StopChangesRepository : CrudRepository<StopChangesDAO, Long> {

    // fun findId(id: Long): ComputationDAO
}