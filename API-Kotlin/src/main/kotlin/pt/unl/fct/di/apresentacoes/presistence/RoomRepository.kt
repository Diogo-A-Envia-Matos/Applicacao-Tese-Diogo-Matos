package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.RoomDAO

interface RoomRepository : CrudRepository<RoomDAO,Long> {

    fun existsByRoomNrAndBuilding(roomNr:String, building:String):Boolean
}