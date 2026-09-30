package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.IgnoredPresentationUnsuitableDAO
import pt.unl.fct.di.apresentacoes.domain.PresentationDAO
import pt.unl.fct.di.apresentacoes.domain.UserDAO
import java.time.LocalDate

interface IgnoredPresentationUnsuitableRepository : CrudRepository<IgnoredPresentationUnsuitableDAO,Long>{

    fun findByPresentationId(presentationID: Long): IgnoredPresentationUnsuitableDAO
}