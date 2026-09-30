package pt.unl.fct.di.apresentacoes.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteRoomUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteGroupOfRoomUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.UnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.IgnoreOrNotIgnoreUnsuitabilityDTO
import pt.unl.fct.di.apresentacoes.domain.RoomUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.AdditionalUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.IgnoredUnsuitableDAO
import pt.unl.fct.di.apresentacoes.presistence.*

private const val ROOM_UNAVAILABILITY_NOT_FOUND = "There is no room unavailability with the id received."
private const val COMPUTATION_IN_PROGRESS = "There is a computation in progress."


@Service
class RoomUnavailabilityService (val roomUnavailabilities: RoomUnavailabilityRepository,
                                 val slots: SlotRepository,
                                 val rooms: RoomRepository
                                 //  val computation: ComputationService
                                 ) {

    @Transactional
    fun addRoomUnsuitableSlot(roomUnavailabilityDTO: AddOrDeleteRoomUnavailabilityDTO): Pair<RoomUnavailabilityDAO, RoomUnavailabilityDAO> {
        val room = rooms.findById(roomUnavailabilityDTO.roomID)
        if(room.isEmpty){
            throw RoomUnavailabilityUnprocessable("The room does not exist.")
        }

        val roomUnavailability : RoomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(roomUnavailabilityDTO.roomID, false)
        val roomUnavailabilityUnsuitable : RoomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(roomUnavailabilityDTO.roomID, true)
        
        val slotToAdd = slots.findById(roomUnavailabilityDTO.slotID)
            .orElseThrow { RoomUnavailabilityUnprocessable("The slot does not exist.") }

        //TODO Testing if not null
        // if (roomUnavailability != null) {
        //     roomUnavailability.slots.add(slotToAdd)
        // }
        // if (roomUnavailabilityUnsuitable != null) {
        //     roomUnavailabilityUnsuitable.slots.remove(slotToAdd)
        // }
        roomUnavailability.slots.remove(slotToAdd)
        roomUnavailabilityUnsuitable.slots.add(slotToAdd)

        return Pair(roomUnavailabilities.save(roomUnavailability), roomUnavailabilities.save(roomUnavailabilityUnsuitable))
    }

    @Transactional
    fun changeRoomUnsuitableToUnavailability(roomUnavailabilityDTO: AddOrDeleteRoomUnavailabilityDTO): Pair<RoomUnavailabilityDAO, RoomUnavailabilityDAO> {
        val room = rooms.findById(roomUnavailabilityDTO.roomID)
        if(room.isEmpty){
            throw RoomUnavailabilityUnprocessable("The room does not exist.")
        }

        val roomUnavailability : RoomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(roomUnavailabilityDTO.roomID, false)
        val roomUnavailabilityUnsuitable : RoomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(roomUnavailabilityDTO.roomID, true)

        val slotToChange = slots.findById(roomUnavailabilityDTO.slotID)
            .orElseThrow { RoomUnavailabilityUnprocessable("The slot does not exist.") }

        // //TODO Testing if not null
        // if (roomUnavailability != null) {
        //     roomUnavailability.slots.remove(slotToChange)
        // }
        // if (roomUnavailabilityUnsuitable != null) {
        //     roomUnavailabilityUnsuitable.slots.add(slotToChange)
        // }
        roomUnavailability.slots.add(slotToChange)
        roomUnavailabilityUnsuitable.slots.remove(slotToChange)
        
        return Pair(roomUnavailabilities.save(roomUnavailability), roomUnavailabilities.save(roomUnavailabilityUnsuitable))
    }

    @Transactional
    fun deleteRoomUnavailability(roomUnavailabilityDTO: AddOrDeleteRoomUnavailabilityDTO): Pair<RoomUnavailabilityDAO, RoomUnavailabilityDAO> {
        val room = rooms.findById(roomUnavailabilityDTO.roomID)
        if(room.isEmpty){
            throw RoomUnavailabilityUnprocessable("The room does not exist.")
        }
        
        val roomUnavailability : RoomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(roomUnavailabilityDTO.roomID, false)
        val roomUnavailabilityUnsuitable : RoomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(roomUnavailabilityDTO.roomID, true)
        
        val slotToRemove = slots.findById(roomUnavailabilityDTO.slotID)
            .orElseThrow { RoomUnavailabilityUnprocessable("The slot does not exist.") }

        // //TODO Testing if not null
        // if (roomUnavailability != null) {
        //     roomUnavailability.slots.remove(slotToRemove)
        // }
        // if (roomUnavailabilityUnsuitable != null) {
        //     roomUnavailabilityUnsuitable.slots.remove(slotToRemove)
        // }
        roomUnavailability.slots.remove(slotToRemove)
        roomUnavailabilityUnsuitable.slots.remove(slotToRemove)
        
        return Pair(roomUnavailabilities.save(roomUnavailability), roomUnavailabilities.save(roomUnavailabilityUnsuitable))
    }

    //TODO Change multiple slots
    @Transactional
    fun addRoomUnsuitableSlotGroup(roomUnavailabilityGroupDTO: AddOrDeleteGroupOfRoomUnavailabilitiesDTO): Pair<RoomUnavailabilityDAO, RoomUnavailabilityDAO> {
        val room = rooms.findById(roomUnavailabilityGroupDTO.roomID)
        if(room.isEmpty){
            throw RoomUnavailabilityUnprocessable("The room does not exist.")
        }
        val roomUnavailability : RoomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(roomUnavailabilityGroupDTO.roomID, false)
        val roomUnavailabilityUnsuitable : RoomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(roomUnavailabilityGroupDTO.roomID, true)

        for (slotID in roomUnavailabilityGroupDTO.slotsID) {
            val slotToAdd = slots.findById(slotID)
                .orElseThrow { RoomUnavailabilityUnprocessable("The slot does not exist.") }
            roomUnavailability.slots.remove(slotToAdd)
            roomUnavailabilityUnsuitable.slots.add(slotToAdd)
        }
        
        return Pair(roomUnavailabilities.save(roomUnavailability), roomUnavailabilities.save(roomUnavailabilityUnsuitable))
    }

    //TODO Change multiple slots
    @Transactional
    fun addRoomUnavailabilityGroup(roomUnavailabilityGroupDTO: AddOrDeleteGroupOfRoomUnavailabilitiesDTO): Pair<RoomUnavailabilityDAO, RoomUnavailabilityDAO> {
        val room = rooms.findById(roomUnavailabilityGroupDTO.roomID)
        if(room.isEmpty){
            throw RoomUnavailabilityUnprocessable("The room does not exist.")
        }

        val roomUnavailability : RoomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(roomUnavailabilityGroupDTO.roomID, false)
        val roomUnavailabilityUnsuitable : RoomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(roomUnavailabilityGroupDTO.roomID, true)

        for (slotID in roomUnavailabilityGroupDTO.slotsID) {
            val slotToChange = slots.findById(slotID)
                .orElseThrow { RoomUnavailabilityUnprocessable("The slot does not exist.") }
            roomUnavailability.slots.add(slotToChange)
            roomUnavailabilityUnsuitable.slots.remove(slotToChange)
        }

        return Pair(roomUnavailabilities.save(roomUnavailability), roomUnavailabilities.save(roomUnavailabilityUnsuitable))
    }

    //TODO Change multiple slots
    @Transactional
    fun deleteRoomUnavailabilityGroup(roomUnavailabilityGroupDTO: AddOrDeleteGroupOfRoomUnavailabilitiesDTO): Pair<RoomUnavailabilityDAO, RoomUnavailabilityDAO> {
        val room = rooms.findById(roomUnavailabilityGroupDTO.roomID)
        if(room.isEmpty){
            throw RoomUnavailabilityUnprocessable("The room does not exist.")
        }
        
        val roomUnavailability : RoomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(roomUnavailabilityGroupDTO.roomID, false)
        val roomUnavailabilityUnsuitable : RoomUnavailabilityDAO = roomUnavailabilities.findByRoomIdAndUnsuitable(roomUnavailabilityGroupDTO.roomID, true)
        
        for (slotID in roomUnavailabilityGroupDTO.slotsID) {
            val slotToRemove = slots.findById(slotID)
                .orElseThrow { RoomUnavailabilityUnprocessable("The slot does not exist.") }
            roomUnavailability.slots.remove(slotToRemove)
            roomUnavailabilityUnsuitable.slots.remove(slotToRemove)
        }

        return Pair(roomUnavailabilities.save(roomUnavailability), roomUnavailabilities.save(roomUnavailabilityUnsuitable))
    }

    fun getOneRoomUnavailability(id:Long): RoomUnavailabilityDAO {
        return roomUnavailabilities.findById(id)
            .orElseThrow { UnavailabilityNotFound() }
    }

    fun getAllRoomUnavailabilities(): Iterable<RoomUnavailabilityDAO> = roomUnavailabilities.findAll()

    fun getRoomUnavailabilities(rid: Long): Pair<RoomUnavailabilityDAO, RoomUnavailabilityDAO> {
        return Pair(roomUnavailabilities.findByRoomIdAndUnsuitable(rid, false), roomUnavailabilities.findByRoomIdAndUnsuitable(rid, true))
    }

    
}

class RoomUnavailabilityNotFound(message: String? = ROOM_UNAVAILABILITY_NOT_FOUND) : RuntimeException(message)
class RoomUnavailabilityUnprocessable(message: String) : RuntimeException(message)
// class ComputationInProgress(message: String? = COMPUTATION_IN_PROGRESS) : RuntimeException(message)
