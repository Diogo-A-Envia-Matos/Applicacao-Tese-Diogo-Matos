package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.SlotDAO
import java.time.LocalDate
import java.time.LocalTime

interface SlotRepository : CrudRepository<SlotDAO, Long> {

    fun existsByDate(date: LocalDate): Boolean
    fun existsByDateAndStartingHour(date: LocalDate, startingHour: LocalTime): Boolean

}