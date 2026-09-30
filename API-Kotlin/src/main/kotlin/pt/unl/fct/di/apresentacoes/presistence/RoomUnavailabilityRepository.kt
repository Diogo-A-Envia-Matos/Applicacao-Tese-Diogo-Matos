package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.RoomUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.RoomDAO
import java.time.LocalDate

interface RoomUnavailabilityRepository : CrudRepository<RoomUnavailabilityDAO,Long>{

    fun findByRoomIdAndUnsuitable(roomID: Long, unsuitable: Boolean): RoomUnavailabilityDAO
}