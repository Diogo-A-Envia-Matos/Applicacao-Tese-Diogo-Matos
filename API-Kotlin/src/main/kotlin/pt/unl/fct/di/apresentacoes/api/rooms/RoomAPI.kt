package pt.unl.fct.di.apresentacoes.api.rooms

import org.springframework.web.bind.annotation.*
import pt.unl.fct.di.apresentacoes.api.dto.RoomDTO
import pt.unl.fct.di.apresentacoes.api.dto.SlotDTO
import pt.unl.fct.di.apresentacoes.api.slots.CanDeleteSlot

@RequestMapping("/api/room")
interface RoomAPI {

    @PostMapping("/addInitial")
    fun addInitialRooms()

    @PostMapping("")
    @CanAddRoom
    fun addRoom(@RequestBody room: RoomDTO): RoomDTO

    @GetMapping("/{id}")
    @CanReadOneRoom
    fun getRoom(@PathVariable id:Long): RoomDTO

    @GetMapping("")
    // @CanReadAllRooms
    fun getAllRooms(): Collection<RoomDTO>

    @DeleteMapping("/{id}")
    @CanDeleteRoom
    fun deleteRoom(@PathVariable id: Long)


}