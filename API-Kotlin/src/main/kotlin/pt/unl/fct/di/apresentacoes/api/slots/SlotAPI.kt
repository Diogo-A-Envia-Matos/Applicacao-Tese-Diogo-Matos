package pt.unl.fct.di.apresentacoes.api.slots

import org.springframework.web.bind.annotation.*
import pt.unl.fct.di.apresentacoes.api.dto.SlotDTO
import pt.unl.fct.di.apresentacoes.api.dto.SlotsDataDTO

@RequestMapping("/api/slot")
interface SlotAPI {

    @PostMapping("/addInitial")
    fun addInitialSlots()

    @PostMapping("/addSeveral")
    @CanCreateSlot
    fun addSlots(@RequestBody slotData: SlotsDataDTO)

    @PostMapping("")
    @CanCreateSlot
    fun addSingleSlot(@RequestBody slotDTO: SlotDTO)

    @GetMapping("/{id}")
    fun getSlot(@PathVariable id:Long): SlotDTO

    @GetMapping("")
    fun getAllSlots(): Collection<SlotDTO>

    @DeleteMapping("/{id}")
    @CanDeleteSlot
    fun deleteSlot(@PathVariable id:Long)

}