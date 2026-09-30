package pt.unl.fct.di.apresentacoes.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.RoomDTO
import pt.unl.fct.di.apresentacoes.domain.RoomDAO
import pt.unl.fct.di.apresentacoes.domain.SlotDAO
import pt.unl.fct.di.apresentacoes.domain.RoomUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.presistence.PresentationRepository
import pt.unl.fct.di.apresentacoes.presistence.RoomRepository
import pt.unl.fct.di.apresentacoes.presistence.SlotRepository
import pt.unl.fct.di.apresentacoes.presistence.RoomUnavailabilityRepository

private const val THERE_ARE_ROOMS = "There are rooms in the table. This operation is made to add the initial rooms."
private const val ROOM_EXISTS = "This room already exists."
private const val ROOM_NOT_FOUND = "This room does not exist."


private const val STARTER_BUILDING = 2
// private val STARTER_ROOMS: List<Long> = listOf(110, 112, 114, 116)
private val STARTER_ROOMS: List<Long> = listOf(110, 112, 114, 116, 119, 120, 121, 122, 123, 124)

@Service
class RoomService(val rooms: RoomRepository,
                  val roomUnavailabilities: RoomUnavailabilityRepository,
                  val presentations: PresentationRepository,
                  val slots: SlotRepository) {

    @Transactional
    fun addInitialRooms(){
        if(rooms.count().toInt() != 0)
            throw ThereAreRooms()

        for(i in STARTER_ROOMS.indices) {
            // val newRoom = RoomDAO(0, STARTER_ROOMS[i].toString(), STARTER_BUILDING.toString(), mutableListOf())
            val newRoom = RoomDAO(0, STARTER_ROOMS[i].toString(), STARTER_BUILDING.toString())
            val roomUnavailabilityDAO = RoomUnavailabilityDAO(0, false, mutableSetOf(),newRoom)
            val roomUnsuitableSlotsDAO = RoomUnavailabilityDAO(0, true, mutableSetOf(),newRoom)
            roomUnavailabilities.save(roomUnavailabilityDAO)
            roomUnavailabilities.save(roomUnsuitableSlotsDAO)
            rooms.save(newRoom)
        }
    }

    @Transactional
    fun addRoom(roomDTO: RoomDTO): RoomDAO{
        if(rooms.existsByRoomNrAndBuilding(roomDTO.roomNr,roomDTO.building))
            throw RoomAlreadyExists()

        // val newRoom = RoomDAO(0, roomDTO.roomNr, roomDTO.building, mutableListOf())
        val newRoom = RoomDAO(0, roomDTO.roomNr, roomDTO.building)
        val roomUnavailabilityDAO = RoomUnavailabilityDAO(0, false, mutableSetOf(),newRoom)
        val roomUnsuitableSlotsDAO = RoomUnavailabilityDAO(0, true, mutableSetOf(),newRoom)
        roomUnavailabilities.save(roomUnavailabilityDAO)
        roomUnavailabilities.save(roomUnsuitableSlotsDAO)
        return rooms.save(newRoom)
    }

    fun getOneRoom(id:Long): RoomDAO{
        return rooms.findById(id)
            .orElseThrow{RoomNotFound()}
    }

    fun getAllRooms(): Iterable<RoomDAO> = rooms.findAll()


    fun deleteOneRoom(id:Long) {
        val room = rooms.findById(id)
            .orElseThrow { RoomNotFound() }
        val roomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(id, false)
        val roomUnsuitableSlotsDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(id, true)
        
        roomUnavailabilities.deleteById(roomUnavailabilityDAO.id)
        roomUnavailabilities.deleteById(roomUnsuitableSlotsDAO.id)

        for(presentation in room.presentations){
            presentation.room = null
            presentation.slot = null
            presentations.save(presentation)
        }
        rooms.deleteById(id)
    }
}

class ThereAreRooms(message: String? = THERE_ARE_ROOMS) : Exception(message)
class RoomAlreadyExists(message: String? = ROOM_EXISTS) : Exception(message)
class RoomNotFound(message: String? = ROOM_NOT_FOUND) : RuntimeException(message)
