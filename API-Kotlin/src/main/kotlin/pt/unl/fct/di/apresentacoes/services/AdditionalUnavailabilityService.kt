package pt.unl.fct.di.apresentacoes.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.UnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.ChangeAdditionalUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.domain.UnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.SlotDAO
import pt.unl.fct.di.apresentacoes.domain.AdditionalUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.AdditionalPresentationUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.presistence.*

private const val ADDITIONAL_UNAVAILABILITY_NOT_FOUND = "There is no additional unavailability with the id received."

//TODO Add function postAdditionalUnavailabilities
@Service
class AdditionalUnavailabilityService (val unavailabilities: UnavailabilityRepository,
                                       val additionalUnavailabilities: AdditionalUnavailabilityRepository,
                                       val presentationUnavailabilities: PresentationUnavailabilityRepository,
                                       val additionalPresentationUnavailabilities: AdditionalPresentationUnavailabilityRepository,
                                       val presentations: PresentationRepository,
                                       val slots: SlotRepository,
                                       val users: UserRepository) {

    // fun addUnsuitableSlot(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UserUnavailabilitiesDTO {
    // @Transactional
    // // fun postAdditionalUnavailabilities(additionalUnavailabilitiesDTO: ChangeAdditionalUnavailabilitiesDTO): Pair<Collection<AdditionalUnavailabilityDAO>, Collection<AdditionalPresentationUnavailabilityDAO>> {
    // fun postAdditionalUnavailabilities(additionalUnavailabilitiesDTO: ChangeAdditionalUnavailabilitiesDTO): Pair<Pair<Collection<AdditionalUnavailabilityDAO>, Collection<AdditionalUnavailabilityDAO>>, Pair<Collection<AdditionalPresentationUnavailabilityDAO>, Collection<AdditionalPresentationUnavailabilityDAO>>> {
    //     // val userChanging = users.findById(additionalUnavailabilitiesDTO.userID)
    //     // if(userChanging.isEmpty){
    //     //     throw UnavailabilityUnprocessable("The user does not exist.")
    //     // }

    //     // Prevent simultaneous changes

    //     var updatedAdditionalUnavailabilities : ArrayDeque<AdditionalUnavailabilityDAO> = ArrayDeque<AdditionalUnavailabilityDAO>()
    //     var updatedAdditionalUnsuitableSlots : ArrayDeque<AdditionalUnavailabilityDAO> = ArrayDeque<AdditionalUnavailabilityDAO>()
    //     var updatedAdditionalPresentationUnavailabilities : ArrayDeque<AdditionalPresentationUnavailabilityDAO> = ArrayDeque<AdditionalPresentationUnavailabilityDAO>()
    //     var updatedAdditionalPresentationUnsuitableSlots : ArrayDeque<AdditionalPresentationUnavailabilityDAO> = ArrayDeque<AdditionalPresentationUnavailabilityDAO>()

    //     // var unavailabilityList : MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
    //     // var unsuitableSlotList : MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
    //     // var presentationUnavailabilityList : MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
    //     // var presentationUnsuitableSlotList : MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()

    //     //Checking if slots exist and initial state matches
    //     for ((userId, slotsId) in additionalUnavailabilitiesDTO.userAdditionalUnavailabilities.entries.iterator()) {
    //         //TODO Check if received prevState matches state on the Database
    //         //TODO Keep updated state until the end
    //         val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(userId, false)
    //         var newSlotList = ArrayDeque<SlotDAO>()
    //         for (slotId in slotsId) {
    //             val newSlot = slots.findById(slotId).orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }
    //             newSlotList.add(newSlot)
    //         }
            
    //         val newAdditionalUnavailability = AdditionalUnavailabilityDAO(
    //             additionalUnavailability.id, true, newSlotList,
    //             additionalUnavailability.user
    //         )
    //         // additionalUnavailability.slots = newSlotList
    //         updatedAdditionalUnavailabilities.add(newAdditionalUnavailability)
    //         // updatedAdditionalUnavailabilities.add(additionalUnavailability)
    //     }
        
    //     for ((userId, slotsId) in additionalUnavailabilitiesDTO.userAdditionalUnsuitableSlots.entries.iterator()) {
    //         //TODO Check if received prevState matches state on the Database
    //         //TODO Keep updated state until the end
    //         val additionalUnsuitableSlots : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(userId, true)
    //         var newSlotList = ArrayDeque<SlotDAO>()
    //         for (slotId in slotsId) {
    //             val newSlot = slots.findById(slotId).orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }
    //             newSlotList.add(newSlot)
    //         }
    //         val newAdditionalUnsuitableSlots = AdditionalUnavailabilityDAO(
    //             additionalUnsuitableSlots.id, true, newSlotList,
    //             additionalUnsuitableSlots.user
    //         )
    //         // additionalUnsuitableSlots.slots = newSlotList
    //         // updatedAdditionalUnavailabilities.add(newAdditionalUnsuitableSlots)
    //         // updatedAdditionalUnavailabilities.add(additionalUnsuitableSlots)
    //         updatedAdditionalUnsuitableSlots.add(newAdditionalUnsuitableSlots)
    //     }
        
    //     for ((presentationId, slotsId) in additionalUnavailabilitiesDTO.presentationAdditionalUnavailabilities.entries.iterator()) {
    //         //TODO Check if received prevState matches state on the Database
    //         //TODO Keep updated state until the end
    //         val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationId, false)
    //         var newSlotList = ArrayDeque<SlotDAO>()
    //         for (slotId in slotsId) {
    //             val newSlot = slots.findById(slotId).orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }
    //             newSlotList.add(newSlot)
    //         }
    //         val newAdditionalPresentationUnavailability = AdditionalPresentationUnavailabilityDAO(
    //             additionalPresentationUnavailability.id, false, newSlotList,
    //             additionalPresentationUnavailability.presentation, additionalPresentationUnavailability.user
    //         )
    //         // additionalPresentationUnavailability.slots = newSlotList
    //         updatedAdditionalPresentationUnavailabilities.add(newAdditionalPresentationUnavailability)
    //         // updatedAdditionalPresentationUnavailabilities.add(additionalPresentationUnavailability)
    //     }
        
    //     for ((presentationId, slotsId) in additionalUnavailabilitiesDTO.presentationAdditionalUnsuitableSlots.entries.iterator()) {
    //         //TODO Check if received prevState matches state on the Database
    //         //TODO Keep updated state until the end
    //         val additionalPresentationUnsuitableSlots : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationId, true)
    //         var newSlotList = ArrayDeque<SlotDAO>()
    //         for (slotId in slotsId) {
    //             val newSlot = slots.findById(slotId).orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }
    //             newSlotList.add(newSlot)
    //         }
    //         val newAdditionalPresentationUnsuitableSlots = AdditionalPresentationUnavailabilityDAO(
    //             additionalPresentationUnsuitableSlots.id, true, newSlotList,
    //             additionalPresentationUnsuitableSlots.presentation, additionalPresentationUnsuitableSlots.user
    //         )
    //         // additionalPresentationUnsuitableSlots.slots = newSlotList
    //         // updatedAdditionalPresentationUnavailabilities.add(newAdditionalPresentationUnsuitableSlots)
    //         // updatedAdditionalPresentationUnavailabilities.add(additionalPresentationUnsuitableSlots)
    //         updatedAdditionalPresentationUnsuitableSlots.add(newAdditionalPresentationUnsuitableSlots)
    //     }

    //     // var updatedAdditionalUnavailabilities : ArrayDeque<AdditionalUnavailabilityDAO> = ArrayDeque<AdditionalUnavailabilityDAO>()
    //     // var updatedAdditionalUnsuitableSlots : ArrayDeque<AdditionalUnavailabilityDAO> = ArrayDeque<AdditionalUnavailabilityDAO>()
    //     // var updatedAdditionalPresentationUnavailabilities : ArrayDeque<AdditionalPresentationUnavailabilityDAO> = ArrayDeque<AdditionalPresentationUnavailabilityDAO>()
    //     // var updatedAdditionalPresentationUnsuitableSlots : ArrayDeque<AdditionalPresentationUnavailabilityDAO> = ArrayDeque<AdditionalPresentationUnavailabilityDAO>()

        
    //     //Update database (Must have confirmed that initial state is consistent and every slot exists)
    //     for (additionalUnavailability in updatedAdditionalUnavailabilities) {
    //         additionalUnavailabilities.save(additionalUnavailability)
    //     }
        
    //     for (additionalUnsuitableSlots in updatedAdditionalUnsuitableSlots) {
    //         additionalUnavailabilities.save(additionalUnsuitableSlots)
    //     }

    //     for (additionalPresentationUnavailability in updatedAdditionalPresentationUnavailabilities) {
    //         additionalPresentationUnavailabilities.save(additionalPresentationUnavailability)
    //     }

    //     for (additionalPresentationUnsuitableSlots in updatedAdditionalPresentationUnsuitableSlots) {
    //         additionalPresentationUnavailabilities.save(additionalPresentationUnsuitableSlots)
    //     }

    //     return Pair(Pair(updatedAdditionalUnavailabilities, updatedAdditionalUnsuitableSlots), Pair(updatedAdditionalPresentationUnavailabilities, updatedAdditionalPresentationUnsuitableSlots))
    // }
}

class AdditionalUnavailabilityNotFound(message: String? = ADDITIONAL_UNAVAILABILITY_NOT_FOUND) : RuntimeException(message)
class AdditionalUnavailabilityUnprocessable(message: String) : RuntimeException(message)
