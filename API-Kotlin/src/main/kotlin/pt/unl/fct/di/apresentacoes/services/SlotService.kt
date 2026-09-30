package pt.unl.fct.di.apresentacoes.services


import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.SlotDTO
import pt.unl.fct.di.apresentacoes.api.dto.SlotsDataDTO
import pt.unl.fct.di.apresentacoes.domain.SlotDAO
import pt.unl.fct.di.apresentacoes.presistence.*
import java.time.LocalDate
import java.time.LocalTime

private const val THERE_ARE_SLOTS = "There are slots in the table. This operation is made to add the initial slots."
private const val SLOT_DAY_EXISTS = "There are already slots for this day."
private const val SLOT_ALREADY_EXISTS = "There is already a slots with this day and starting hour."
private const val MAX_SLOTS = "Max slots per day is 8."
private const val SLOT_NOT_FOUND = "This slot does not exist."

// private val STARTER_DATE: LocalDate = LocalDate.parse("2026-02-10")
private val DEFAULT_STARTER_DATES: List<LocalDate> = listOf(
    LocalDate.parse("2026-11-09"),
    LocalDate.parse("2026-11-10"),
    LocalDate.parse("2026-11-11"),
    LocalDate.parse("2026-11-12"),
)

private val DEFAULT_SLOT_TIMES: List<LocalTime> = listOf(
    LocalTime.of(9, 0),
    LocalTime.of(10, 0),
    LocalTime.of(11, 0),
    LocalTime.of(12, 0),
    LocalTime.of(14, 0),
    LocalTime.of(15, 0),
    LocalTime.of(16, 0),
    LocalTime.of(17, 0)
)

@Service
class SlotService(val slots: SlotRepository,
                  val presentations: PresentationRepository,
                  val unavailabilities: UnavailabilityRepository,
                  val presentationUnavailabilities: PresentationUnavailabilityRepository) {

    @Transactional
    fun addInitialSlots() {
        if(slots.count().toInt() != 0)
            throw ThereAreSlots()

        for (date in DEFAULT_STARTER_DATES) {
            for (time in DEFAULT_SLOT_TIMES) {
                slots.save(SlotDAO(0, date, time, mutableListOf(), mutableListOf(),
                    mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()
                ))
            }
        }
    }

    @Transactional
    fun addSlots(slotsDataDTO: SlotsDataDTO) {
        if(slots.existsByDate(slotsDataDTO.date)) {
            throw SlotDayAlreadyExists()
        }
        if(slotsDataDTO.numberOfSlots > 8 || slotsDataDTO.numberOfSlots < 1) {
            throw MaxSlots()
        }
        for (i in 0 until slotsDataDTO.numberOfSlots) {
            slots.save(SlotDAO(0,slotsDataDTO.date, DEFAULT_SLOT_TIMES[i.toInt()], mutableListOf(), mutableListOf(),
                mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()
            ))
        }
    }

    @Transactional
    fun addSingleSlot(slotDTO: SlotDTO) {
        if(slots.existsByDateAndStartingHour(slotDTO.date, slotDTO.startingHour)) {
            throw SlotAlreadyExists()
        }
        slots.save(SlotDAO(0,slotDTO.date,slotDTO.startingHour, mutableListOf(), mutableListOf(), mutableListOf(),
                mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()))
    }

    fun getOneSlot(id:Long): SlotDAO {
        return slots.findById(id)
            .orElseThrow { SlotNotFound() }
    }

    fun getAllSlots(): Iterable<SlotDAO> = slots.findAll()

    //TODO Check if this function also deletes unsuitable slots
    @Transactional
    fun deleteOne(id:Long) {
        val slot = slots.findById(id)
            .orElseThrow { SlotNotFound() }
        for (unavailability in slot.unavailabilities) {
            unavailability.slots.remove(slot)
            unavailabilities.save(unavailability)
        }
        for (presentationUnavailability in slot.presentationUnavailabilities) {
            presentationUnavailability.slots.remove(slot)
            presentationUnavailabilities.save(presentationUnavailability)
        }
        for (presentation in slot.presentations) {
            presentation.slot = null
            presentation.room = null
            presentations.save(presentation)
        }
        slots.delete(slot)
    }

}

class ThereAreSlots(message: String? = THERE_ARE_SLOTS) : Exception(message)
class SlotDayAlreadyExists(message: String? = SLOT_DAY_EXISTS) : Exception(message)
class SlotAlreadyExists(message: String? = SLOT_ALREADY_EXISTS) : Exception(message)
class MaxSlots(message: String? = MAX_SLOTS) : Exception(message)
class SlotNotFound(message: String? = SLOT_NOT_FOUND) : RuntimeException(message)
