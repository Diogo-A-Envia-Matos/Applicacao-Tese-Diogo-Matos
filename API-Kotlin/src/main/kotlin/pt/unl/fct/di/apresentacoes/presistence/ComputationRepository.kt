package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.ComputationDAO

interface ComputationRepository : CrudRepository<ComputationDAO, Long> {

    // fun findId(id: Long): ComputationDAO
}