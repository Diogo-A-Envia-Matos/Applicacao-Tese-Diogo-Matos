package pt.unl.fct.di.apresentacoes.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeletePresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteGroupOfPresentationUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.PresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.IgnoredPresentationUnsuitableDTO
import pt.unl.fct.di.apresentacoes.api.dto.IgnoreOrNotIgnorePresentationUnsuitabilityDTO
import pt.unl.fct.di.apresentacoes.domain.PresentationUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.AdditionalPresentationUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.IgnoredPresentationUnsuitableDAO
import pt.unl.fct.di.apresentacoes.presistence.PresentationRepository
import pt.unl.fct.di.apresentacoes.presistence.PresentationUnavailabilityRepository
import pt.unl.fct.di.apresentacoes.presistence.AdditionalPresentationUnavailabilityRepository
import pt.unl.fct.di.apresentacoes.presistence.IgnoredPresentationUnsuitableRepository
import pt.unl.fct.di.apresentacoes.presistence.SlotRepository
import pt.unl.fct.di.apresentacoes.presistence.UserRepository

private const val PRESENTATION_UNAVAILABILITY_NOT_FOUND = "There is no presentationUnavailability with the id received."
private const val COMPUTATION_IN_PROGRESS = "There is a computation in progress."

//TODO Apply changes in response to Pairs
@Service
class PresentationUnavailabilityService (val presentationUnavailabilities: PresentationUnavailabilityRepository,
                                         val additionalPresentationUnavailabilities: AdditionalPresentationUnavailabilityRepository,
                                         val ignoredPresentationUnsuitable: IgnoredPresentationUnsuitableRepository,
                                         val presentations: PresentationRepository,
                                         val slots: SlotRepository,
                                         val users: UserRepository
                                         //  val computation: ComputationService
                                         ) {

    @Transactional
    fun addPresentationUnsuitable(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): Pair<PresentationUnavailabilityDAO, PresentationUnavailabilityDAO> {
        val presentation = presentations.findById(presentationUnavailabilityDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }

        val presentationUnavailability : PresentationUnavailabilityDAO = presentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, false)
        val presentationUnavailabilityUnsuitable : PresentationUnavailabilityDAO = presentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, true)
        val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, false)
        val additionalPresentationUnavailabilityUnsuitable : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, true)
        
        val presentationUnsuitableToNotIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityDTO.presentationID)

        val slotToAdd = slots.findById(presentationUnavailabilityDTO.slotID)
            .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        // //TODO Testing if not null
        // if (presentationUnavailability != null) {
        //     presentationUnavailability.slots.add(slotToAdd)
        // }
        // if (presentationUnavailabilityUnsuitable != null) {
        //     presentationUnavailabilityUnsuitable.slots.remove(slotToAdd)
        // }
        presentationUnavailability.slots.remove(slotToAdd)
        presentationUnavailabilityUnsuitable.slots.add(slotToAdd)

        additionalPresentationUnavailability.slots.remove(slotToAdd)
        additionalPresentationUnavailabilityUnsuitable.slots.remove(slotToAdd)

        presentationUnsuitableToNotIgnore.slots.remove(slotToAdd)
        
        additionalPresentationUnavailabilities.save(additionalPresentationUnavailability)
        additionalPresentationUnavailabilities.save(additionalPresentationUnavailabilityUnsuitable)
        
        ignoredPresentationUnsuitable.save(presentationUnsuitableToNotIgnore)
        return Pair(presentationUnavailabilities.save(presentationUnavailability), presentationUnavailabilities.save(presentationUnavailabilityUnsuitable))
    }
    
    //TODO New function fun changeUnsuitableToUnavailability(@RequestBody presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilityDTO
    @Transactional
    fun changePresentationUnsuitableToUnavailability(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): Pair<PresentationUnavailabilityDAO, PresentationUnavailabilityDAO> {
        val presentation = presentations.findById(presentationUnavailabilityDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }
        
        val presentationUnavailability : PresentationUnavailabilityDAO = presentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, false)
        val presentationUnavailabilityUnsuitable : PresentationUnavailabilityDAO = presentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, true)
        val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, false)
        val additionalPresentationUnavailabilityUnsuitable : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, true)

        val presentationUnsuitableToNotIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityDTO.presentationID)

        val slotToChange = slots.findById(presentationUnavailabilityDTO.slotID)
            .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        // //TODO Testing if not null
        // if (presentationUnavailability != null) {
        //     presentationUnavailability.slots.remove(slotToChange)
        // }
        // if (presentationUnavailabilityUnsuitable != null) {
        //     presentationUnavailabilityUnsuitable.slots.add(slotToChange)
        // }
        presentationUnavailability.slots.add(slotToChange)
        presentationUnavailabilityUnsuitable.slots.remove(slotToChange)

        additionalPresentationUnavailability.slots.remove(slotToChange)
        additionalPresentationUnavailabilityUnsuitable.slots.remove(slotToChange)
        
        presentationUnsuitableToNotIgnore.slots.remove(slotToChange)

        additionalPresentationUnavailabilities.save(additionalPresentationUnavailability)
        additionalPresentationUnavailabilities.save(additionalPresentationUnavailabilityUnsuitable)

        ignoredPresentationUnsuitable.save(presentationUnsuitableToNotIgnore)
        return Pair(presentationUnavailabilities.save(presentationUnavailability), presentationUnavailabilities.save(presentationUnavailabilityUnsuitable))
    }

    @Transactional
    fun deletePresentationUnavailability(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): Pair<PresentationUnavailabilityDAO, PresentationUnavailabilityDAO> {
        val presentation = presentations.findById(presentationUnavailabilityDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }

        val presentationUnavailability : PresentationUnavailabilityDAO = presentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, false)
        val presentationUnavailabilityUnsuitable : PresentationUnavailabilityDAO = presentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, true)
        val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, false)
        val additionalPresentationUnavailabilityUnsuitable : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, true)

        val presentationUnsuitableToNotIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityDTO.presentationID)

        val slotToDelete = slots.findById(presentationUnavailabilityDTO.slotID)
            .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        // //TODO Testing if not null
        // if (presentationUnavailability != null) {
        //     presentationUnavailability.slots.remove(slotToDelete)
        // }
        // if (presentationUnavailabilityUnsuitable != null) {
        //     presentationUnavailabilityUnsuitable.slots.remove(slotToDelete)
        // }
        presentationUnavailability.slots.remove(slotToDelete)
        presentationUnavailabilityUnsuitable.slots.remove(slotToDelete)

        additionalPresentationUnavailability.slots.remove(slotToDelete)
        additionalPresentationUnavailabilityUnsuitable.slots.remove(slotToDelete)
        
        presentationUnsuitableToNotIgnore.slots.remove(slotToDelete)

        additionalPresentationUnavailabilities.save(additionalPresentationUnavailability)
        additionalPresentationUnavailabilities.save(additionalPresentationUnavailabilityUnsuitable)

        ignoredPresentationUnsuitable.save(presentationUnsuitableToNotIgnore)
        return Pair(presentationUnavailabilities.save(presentationUnavailability), presentationUnavailabilities.save(presentationUnavailabilityUnsuitable))
    }

    @Transactional
    fun addPresentationUnsuitableGroup(presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): Pair<PresentationUnavailabilityDAO, PresentationUnavailabilityDAO> {
        val presentation = presentations.findById(presentationUnavailabilityGroupDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }

        val presentationUnavailability : PresentationUnavailabilityDAO = presentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, false)
        val presentationUnavailabilityUnsuitable : PresentationUnavailabilityDAO = presentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, true)
        val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, false)
        val additionalPresentationUnavailabilityUnsuitable : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, true)

        val presentationUnsuitableToNotIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityGroupDTO.presentationID)

        // val slotToAdd = slots.findById(presentationUnavailabilityGroupDTO.slotID)
        //     .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        for (slotID in presentationUnavailabilityGroupDTO.slotsID) {
            val slotToAdd = slots.findById(slotID)
                .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }
            presentationUnavailability.slots.remove(slotToAdd)
            presentationUnavailabilityUnsuitable.slots.add(slotToAdd)

            additionalPresentationUnavailability.slots.remove(slotToAdd)
            additionalPresentationUnavailabilityUnsuitable.slots.remove(slotToAdd)
                
            presentationUnsuitableToNotIgnore.slots.remove(slotToAdd)
        }

        // //TODO Testing if not null
        // if (presentationUnavailability != null) {
        //     presentationUnavailability.slots.add(slotToAdd)
        // }
        // if (presentationUnavailabilityUnsuitable != null) {
        //     presentationUnavailabilityUnsuitable.slots.remove(slotToAdd)
        // }
        // presentationUnavailability.slots.remove(slotToAdd)
        // presentationUnavailabilityUnsuitable.slots.add(slotToAdd)

        // additionalPresentationUnavailability.slots.remove(slotToAdd)
        // additionalPresentationUnavailabilityUnsuitable.slots.remove(slotToAdd)

        additionalPresentationUnavailabilities.save(additionalPresentationUnavailability)
        additionalPresentationUnavailabilities.save(additionalPresentationUnavailabilityUnsuitable)

        ignoredPresentationUnsuitable.save(presentationUnsuitableToNotIgnore)
        return Pair(presentationUnavailabilities.save(presentationUnavailability), presentationUnavailabilities.save(presentationUnavailabilityUnsuitable))
    }
    
    //TODO New function fun changeUnsuitableToUnavailability(@RequestBody presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilityDTO
    @Transactional
    fun addPresentationUnavailabilityGroup(presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): Pair<PresentationUnavailabilityDAO, PresentationUnavailabilityDAO> {
        val presentation = presentations.findById(presentationUnavailabilityGroupDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }
        
        val presentationUnavailability : PresentationUnavailabilityDAO = presentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, false)
        val presentationUnavailabilityUnsuitable : PresentationUnavailabilityDAO = presentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, true)
        val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, false)
        val additionalPresentationUnavailabilityUnsuitable : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, true)

        val presentationUnsuitableToNotIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityGroupDTO.presentationID)

        // val slotToChange = slots.findById(presentationUnavailabilityGroupDTO.slotID)
        //     .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        for (slotID in presentationUnavailabilityGroupDTO.slotsID) {
            val slotToChange = slots.findById(slotID)
                .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }
            presentationUnavailability.slots.add(slotToChange)
            presentationUnavailabilityUnsuitable.slots.remove(slotToChange)

            additionalPresentationUnavailability.slots.remove(slotToChange)
            additionalPresentationUnavailabilityUnsuitable.slots.remove(slotToChange)
            
            presentationUnsuitableToNotIgnore.slots.remove(slotToChange)
        }

        additionalPresentationUnavailabilities.save(additionalPresentationUnavailability)
        additionalPresentationUnavailabilities.save(additionalPresentationUnavailabilityUnsuitable)
        ignoredPresentationUnsuitable.save(presentationUnsuitableToNotIgnore)
        return Pair(presentationUnavailabilities.save(presentationUnavailability), presentationUnavailabilities.save(presentationUnavailabilityUnsuitable))
    }

    @Transactional
    fun deletePresentationUnavailabilityGroup(presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): Pair<PresentationUnavailabilityDAO, PresentationUnavailabilityDAO> {
        val presentation = presentations.findById(presentationUnavailabilityGroupDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }
        val presentationUnavailability : PresentationUnavailabilityDAO = presentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, false)
        val presentationUnavailabilityUnsuitable : PresentationUnavailabilityDAO = presentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, true)
        val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, false)
        val additionalPresentationUnavailabilityUnsuitable : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, true)

        val presentationUnsuitableToNotIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityGroupDTO.presentationID)

        // val slotToDelete = slots.findById(presentationUnavailabilityGroupDTO.slotID)
        //     .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        for (slotID in presentationUnavailabilityGroupDTO.slotsID) {
            val slotToDelete = slots.findById(slotID)
                .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }
            presentationUnavailability.slots.remove(slotToDelete)
            presentationUnavailabilityUnsuitable.slots.remove(slotToDelete)

            additionalPresentationUnavailability.slots.remove(slotToDelete)
            additionalPresentationUnavailabilityUnsuitable.slots.remove(slotToDelete)
            
            presentationUnsuitableToNotIgnore.slots.remove(slotToDelete)
        }

        additionalPresentationUnavailabilities.save(additionalPresentationUnavailability)
        additionalPresentationUnavailabilities.save(additionalPresentationUnavailabilityUnsuitable)

        ignoredPresentationUnsuitable.save(presentationUnsuitableToNotIgnore)
        return Pair(presentationUnavailabilities.save(presentationUnavailability), presentationUnavailabilities.save(presentationUnavailabilityUnsuitable))
    }

    @Transactional
    fun addAdditionalPresentationUnsuitable(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): Pair<AdditionalPresentationUnavailabilityDAO, AdditionalPresentationUnavailabilityDAO> {
        val presentation = presentations.findById(presentationUnavailabilityDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }

        val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, false)
        val additionalPresentationUnavailabilityUnsuitable : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, true)
        
        val presentationUnsuitableToNotIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityDTO.presentationID)

        val slotToAdd = slots.findById(presentationUnavailabilityDTO.slotID)
            .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        // //TODO Testing if not null
        // if (presentationUnavailability != null) {
        //     presentationUnavailability.slots.add(slotToAdd)
        // }
        // if (presentationUnavailabilityUnsuitable != null) {
        //     presentationUnavailabilityUnsuitable.slots.remove(slotToAdd)
        // }
        additionalPresentationUnavailability.slots.remove(slotToAdd)
        additionalPresentationUnavailabilityUnsuitable.slots.add(slotToAdd)

        presentationUnsuitableToNotIgnore.slots.remove(slotToAdd)

        ignoredPresentationUnsuitable.save(presentationUnsuitableToNotIgnore)
        return Pair(additionalPresentationUnavailabilities.save(additionalPresentationUnavailability), additionalPresentationUnavailabilities.save(additionalPresentationUnavailabilityUnsuitable))
    }
    
    //TODO New function fun changeUnsuitableToUnavailability(@RequestBody presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilityDTO
    @Transactional
    fun changeAdditionalPresentationUnsuitableToUnavailability(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): Pair<AdditionalPresentationUnavailabilityDAO, AdditionalPresentationUnavailabilityDAO> {
        val presentation = presentations.findById(presentationUnavailabilityDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }
        
        val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, false)
        val additionalPresentationUnavailabilityUnsuitable : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, true)

        val presentationUnsuitableToNotIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityDTO.presentationID)

        val slotToChange = slots.findById(presentationUnavailabilityDTO.slotID)
            .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        // //TODO Testing if not null
        // if (presentationUnavailability != null) {
        //     presentationUnavailability.slots.remove(slotToChange)
        // }
        // if (presentationUnavailabilityUnsuitable != null) {
        //     presentationUnavailabilityUnsuitable.slots.add(slotToChange)
        // }
        additionalPresentationUnavailability.slots.add(slotToChange)
        additionalPresentationUnavailabilityUnsuitable.slots.remove(slotToChange)
        
        presentationUnsuitableToNotIgnore.slots.remove(slotToChange)

        ignoredPresentationUnsuitable.save(presentationUnsuitableToNotIgnore)
        return Pair(additionalPresentationUnavailabilities.save(additionalPresentationUnavailability), additionalPresentationUnavailabilities.save(additionalPresentationUnavailabilityUnsuitable))
    }
    
    @Transactional
    fun deleteAdditionalPresentationUnavailability(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): Pair<AdditionalPresentationUnavailabilityDAO, AdditionalPresentationUnavailabilityDAO> {
        val presentation = presentations.findById(presentationUnavailabilityDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }

        val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, false)
        val additionalPresentationUnavailabilityUnsuitable : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityDTO.presentationID, true)

        val presentationUnsuitableToNotIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityDTO.presentationID)

        val slotToDelete = slots.findById(presentationUnavailabilityDTO.slotID)
            .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        // //TODO Testing if not null
        // if (presentationUnavailability != null) {
        //     presentationUnavailability.slots.remove(slotToDelete)
        // }
        // if (presentationUnavailabilityUnsuitable != null) {
        //     presentationUnavailabilityUnsuitable.slots.remove(slotToDelete)
        // }
        additionalPresentationUnavailability.slots.remove(slotToDelete)
        additionalPresentationUnavailabilityUnsuitable.slots.remove(slotToDelete)

        presentationUnsuitableToNotIgnore.slots.remove(slotToDelete)

        ignoredPresentationUnsuitable.save(presentationUnsuitableToNotIgnore)
        return Pair(additionalPresentationUnavailabilities.save(additionalPresentationUnavailability), additionalPresentationUnavailabilities.save(additionalPresentationUnavailabilityUnsuitable))
    }

    @Transactional
    fun addAdditionalPresentationUnsuitableGroup(presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): Pair<AdditionalPresentationUnavailabilityDAO, AdditionalPresentationUnavailabilityDAO> {
        val presentation = presentations.findById(presentationUnavailabilityGroupDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }

        val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, false)
        val additionalPresentationUnavailabilityUnsuitable : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, true)

        val presentationUnsuitableToNotIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityGroupDTO.presentationID)

        // val slotToAdd = slots.findById(presentationUnavailabilityGroupDTO.slotID)
        //     .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        for (slotID in presentationUnavailabilityGroupDTO.slotsID) {
            val slotToAdd = slots.findById(slotID)
                .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

            additionalPresentationUnavailability.slots.remove(slotToAdd)
            additionalPresentationUnavailabilityUnsuitable.slots.add(slotToAdd)
            
            presentationUnsuitableToNotIgnore.slots.remove(slotToAdd)
        }

        ignoredPresentationUnsuitable.save(presentationUnsuitableToNotIgnore)
        return Pair(additionalPresentationUnavailabilities.save(additionalPresentationUnavailability), additionalPresentationUnavailabilities.save(additionalPresentationUnavailabilityUnsuitable))
    }
    
    @Transactional
    fun addAdditionalPresentationUnavailabilityGroup(presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): Pair<AdditionalPresentationUnavailabilityDAO, AdditionalPresentationUnavailabilityDAO> {
        val presentation = presentations.findById(presentationUnavailabilityGroupDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }
        
        val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, false)
        val additionalPresentationUnavailabilityUnsuitable : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, true)

        val presentationUnsuitableToNotIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityGroupDTO.presentationID)

        // val slotToChange = slots.findById(presentationUnavailabilityGroupDTO.slotID)
        //     .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        for (slotID in presentationUnavailabilityGroupDTO.slotsID) {
            val slotToChange = slots.findById(slotID)
                .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

            additionalPresentationUnavailability.slots.add(slotToChange)
            additionalPresentationUnavailabilityUnsuitable.slots.remove(slotToChange)

            presentationUnsuitableToNotIgnore.slots.remove(slotToChange)
        }

        ignoredPresentationUnsuitable.save(presentationUnsuitableToNotIgnore)
        return Pair(additionalPresentationUnavailabilities.save(additionalPresentationUnavailability), additionalPresentationUnavailabilities.save(additionalPresentationUnavailabilityUnsuitable))
    }

    @Transactional
    fun deleteAdditionalPresentationUnavailabilityGroup(presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): Pair<AdditionalPresentationUnavailabilityDAO, AdditionalPresentationUnavailabilityDAO> {
        val presentation = presentations.findById(presentationUnavailabilityGroupDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityUnprocessable("The presentation does not exist.")
        }
        
        val additionalPresentationUnavailability : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, false)
        val additionalPresentationUnavailabilityUnsuitable : AdditionalPresentationUnavailabilityDAO = additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(presentationUnavailabilityGroupDTO.presentationID, true)

        val presentationUnsuitableToNotIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityGroupDTO.presentationID)

        // val slotToDelete = slots.findById(presentationUnavailabilityGroupDTO.slotID)
        //     .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        for (slotID in presentationUnavailabilityGroupDTO.slotsID) {
            val slotToDelete = slots.findById(slotID)
                .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

            additionalPresentationUnavailability.slots.remove(slotToDelete)
            additionalPresentationUnavailabilityUnsuitable.slots.remove(slotToDelete)

            presentationUnsuitableToNotIgnore.slots.remove(slotToDelete)
        }

        ignoredPresentationUnsuitable.save(presentationUnsuitableToNotIgnore)
        return Pair(additionalPresentationUnavailabilities.save(additionalPresentationUnavailability), additionalPresentationUnavailabilities.save(additionalPresentationUnavailabilityUnsuitable))
    }


    fun getOnePresentationUnavailability(puid:Long): PresentationUnavailabilityDAO {
        return presentationUnavailabilities.findById(puid)
            .orElseThrow { PresentationUnavailabilityNotFound() }
    }

    fun getAllPresentationUnavailabilities(): Iterable<PresentationUnavailabilityDAO> = presentationUnavailabilities.findAll()

    // fun getPresentationUnavailabilities(pid: Long): PresentationUnavailabilityDAO{
    //     return presentationUnavailabilities.findByPresentationId(pid)
    // }

    fun getPresentationUnavailabilities(pid: Long): Pair<PresentationUnavailabilityDAO, PresentationUnavailabilityDAO> {
        return Pair(presentationUnavailabilities.findByPresentationIdAndUnsuitable(pid, false), presentationUnavailabilities.findByPresentationIdAndUnsuitable(pid, true))
    }

    fun getOneAdditionalPresentationUnavailability(puid:Long): AdditionalPresentationUnavailabilityDAO {
        return additionalPresentationUnavailabilities.findById(puid)
            .orElseThrow { PresentationUnavailabilityNotFound() }
    }

    fun getAllAdditionalPresentationUnavailabilities(): Pair<Iterable<PresentationUnavailabilityDAO>, Iterable<AdditionalPresentationUnavailabilityDAO>> {
        return Pair(presentationUnavailabilities.findAll(), additionalPresentationUnavailabilities.findAll())
    }

    // fun getAdditionalPresentationUnavailabilities(pid: Long): PresentationUnavailabilityDAO{
    //     return additionalPresentationUnavailabilities.findByPresentationId(pid)
    // }

    fun getAdditionalPresentationUnavailabilities(pid: Long): Pair<AdditionalPresentationUnavailabilityDAO, AdditionalPresentationUnavailabilityDAO> {
        return Pair(additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(pid, false), additionalPresentationUnavailabilities.findByPresentationIdAndUnsuitable(pid, true))
    }
    
    @Transactional
    fun addIgnorePresentationUnsuitable(presentationUnavailabilityDTO: IgnoreOrNotIgnorePresentationUnsuitabilityDTO): IgnoredPresentationUnsuitableDAO {
        val presentation = presentations.findById(presentationUnavailabilityDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityNotFound("The presentation does not exist.")
        }

        val presentationUnsuitableToIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityDTO.presentationID)
        
        val slotToAdd = slots.findById(presentationUnavailabilityDTO.slotID)
            .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        // //TODO Testing if not null
        // if (presentationUnavailability != null) {
        //     presentationUnavailability.slots.remove(slotToChange)
        // }
        // if (presentationUnavailabilityUnsuitable != null) {
        //     presentationUnavailabilityUnsuitable.slots.add(slotToChange)
        // }
        presentationUnsuitableToIgnore.slots.add(slotToAdd)
        return ignoredPresentationUnsuitable.save(presentationUnsuitableToIgnore)
    }
    
    @Transactional
    fun deleteIgnorePresentationUnsuitable(presentationUnavailabilityDTO: IgnoreOrNotIgnorePresentationUnsuitabilityDTO): IgnoredPresentationUnsuitableDAO {
        val presentation = presentations.findById(presentationUnavailabilityDTO.presentationID)
        if (presentation.isEmpty) {
            throw PresentationUnavailabilityNotFound("The presentation does not exist.")
        }

        val presentationUnsuitableToIgnore : IgnoredPresentationUnsuitableDAO = ignoredPresentationUnsuitable.findByPresentationId(presentationUnavailabilityDTO.presentationID)
        
        val slotToRemove = slots.findById(presentationUnavailabilityDTO.slotID)
            .orElseThrow { PresentationUnavailabilityUnprocessable("The slot does not exist.") }

        // //TODO Testing if not null
        // if (presentationUnavailability != null) {
        //     presentationUnavailability.slots.remove(slotToChange)
        // }
        // if (presentationUnavailabilityUnsuitable != null) {
        //     presentationUnavailabilityUnsuitable.slots.add(slotToChange)
        // }
        presentationUnsuitableToIgnore.slots.remove(slotToRemove)
        return ignoredPresentationUnsuitable.save(presentationUnsuitableToIgnore)
    }
    
    fun getOneIgnoredPresentationUnsuitable(pid:Long): IgnoredPresentationUnsuitableDAO {
        return ignoredPresentationUnsuitable.findById(pid)
            .orElseThrow { PresentationUnavailabilityNotFound() }
    }

    fun getAllIgnoredPresentationUnsuitable(): Iterable<IgnoredPresentationUnsuitableDAO> = ignoredPresentationUnsuitable.findAll()

    // fun getPresentationUnavailabilities(pid: Long): PresentationUnavailabilityDAO{
    //     return presentationUnavailabilities.findByPresentationId(pid)
    // }

    fun getPresentationIgnoredPresentationUnsuitables(pid: Long): IgnoredPresentationUnsuitableDAO {
        return ignoredPresentationUnsuitable.findByPresentationId(pid)
    }

}

class PresentationUnavailabilityNotFound(message: String? = PRESENTATION_UNAVAILABILITY_NOT_FOUND) : RuntimeException(message)
class PresentationUnavailabilityUnprocessable(message: String) : RuntimeException(message)
// class ComputationInProgress(message: String? = COMPUTATION_IN_PROGRESS) : RuntimeException(message)
