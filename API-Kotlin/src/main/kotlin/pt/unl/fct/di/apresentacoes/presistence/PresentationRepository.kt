package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.PresentationDAO

interface PresentationRepository : CrudRepository<PresentationDAO, Long> {

    fun findByAdviserId(adviserId: Long): Iterable<PresentationDAO>
    fun findByArguerId(arguerId: Long): Iterable<PresentationDAO>
    fun findByOptionalParticipant1Id(optionalParticipant1Id:Long):Iterable<PresentationDAO>
    fun findByOptionalParticipant2Id(optionalParticipant2Id:Long):Iterable<PresentationDAO>
    fun findByOptionalParticipant1NotNullOrOptionalParticipant2NotNull(): Iterable<PresentationDAO>
    fun existsByStudentName(studentName: String): Boolean
    fun existsByStudentNumber(studentNumber: String): Boolean
}