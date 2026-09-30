package pt.unl.fct.di.apresentacoes.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteGroupOfUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.UnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.IgnoreOrNotIgnoreUnsuitabilityDTO
import pt.unl.fct.di.apresentacoes.domain.UnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.AdditionalUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.IgnoredUnsuitableDAO
import pt.unl.fct.di.apresentacoes.presistence.*

private const val UNAVAILABILITY_NOT_FOUND = "There is no unavailability with the id received."
private const val COMPUTATION_IN_PROGRESS = "There is a computation in progress."


@Service
class UnavailabilityService (val unavailabilities: UnavailabilityRepository,
                             val additionalUnavailabilities: AdditionalUnavailabilityRepository,
                             val ignoredUnsuitable: IgnoredUnsuitableRepository,
                             val slots: SlotRepository,
                             val users: UserRepository
                             //  val computation: ComputationService
                             ) {

    @Transactional
    fun addUnsuitableSlot(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): Pair<UnavailabilityDAO, UnavailabilityDAO> {
        val user = users.findById(unavailabilityDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }

        val unavailability : UnavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, false)
        val unavailabilityUnsuitable : UnavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, true)
        val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, false)
        val additionalUnavailabilityUnsuitable : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, true)
        
        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityDTO.userID)

        val slotToAdd = slots.findById(unavailabilityDTO.slotID)
            .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        unavailability.slots.remove(slotToAdd)
        unavailabilityUnsuitable.slots.add(slotToAdd)

        additionalUnavailability.slots.remove(slotToAdd)
        additionalUnavailabilityUnsuitable.slots.remove(slotToAdd)

        unsuitableToNotIgnore.slots.remove(slotToAdd)

        additionalUnavailabilities.save(additionalUnavailability)
        additionalUnavailabilities.save(additionalUnavailabilityUnsuitable)

        ignoredUnsuitable.save(unsuitableToNotIgnore)

        return Pair(unavailabilities.save(unavailability), unavailabilities.save(unavailabilityUnsuitable))
    }

    @Transactional
    fun changeUnsuitableToUnavailability(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): Pair<UnavailabilityDAO, UnavailabilityDAO> {
        val user = users.findById(unavailabilityDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }

        val unavailability : UnavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, false)
        val unavailabilityUnsuitable : UnavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, true)
        val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, false)
        val additionalUnavailabilityUnsuitable : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, true)

        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityDTO.userID)

        val slotToChange = slots.findById(unavailabilityDTO.slotID)
            .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        unavailability.slots.add(slotToChange)
        unavailabilityUnsuitable.slots.remove(slotToChange)
        
        additionalUnavailability.slots.remove(slotToChange)
        additionalUnavailabilityUnsuitable.slots.remove(slotToChange)
        
        unsuitableToNotIgnore.slots.remove(slotToChange)

        additionalUnavailabilities.save(additionalUnavailability)
        additionalUnavailabilities.save(additionalUnavailabilityUnsuitable)

        ignoredUnsuitable.save(unsuitableToNotIgnore)

        return Pair(unavailabilities.save(unavailability), unavailabilities.save(unavailabilityUnsuitable))
    }

    @Transactional
    fun deleteUnavailability(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): Pair<UnavailabilityDAO, UnavailabilityDAO> {
        val user = users.findById(unavailabilityDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }
        
        val unavailability : UnavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, false)
        val unavailabilityUnsuitable : UnavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, true)
        val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, false)
        val additionalUnavailabilityUnsuitable : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, true)
        
        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityDTO.userID)

        val slotToRemove = slots.findById(unavailabilityDTO.slotID)
            .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        unavailability.slots.remove(slotToRemove)
        unavailabilityUnsuitable.slots.remove(slotToRemove)
        
        additionalUnavailability.slots.remove(slotToRemove)
        additionalUnavailabilityUnsuitable.slots.remove(slotToRemove)
        
        unsuitableToNotIgnore.slots.remove(slotToRemove)

        additionalUnavailabilities.save(additionalUnavailability)
        additionalUnavailabilities.save(additionalUnavailabilityUnsuitable)

        ignoredUnsuitable.save(unsuitableToNotIgnore)
        
        return Pair(unavailabilities.save(unavailability), unavailabilities.save(unavailabilityUnsuitable))
    }

    //TODO Change multiple slots
    @Transactional
    fun addUnsuitableSlotGroup(unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): Pair<UnavailabilityDAO, UnavailabilityDAO> {
        val user = users.findById(unavailabilityGroupDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }

        val unavailability : UnavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, false)
        val unavailabilityUnsuitable : UnavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, true)
        val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, false)
        val additionalUnavailabilityUnsuitable : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, true)

        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityGroupDTO.userID)

        // val slotToAdd = slots.findById(unavailabilityGroupDTO.slotID)
        //     .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        //Check if all slots exist
        for (slotID in unavailabilityGroupDTO.slotsID) {
            val slotToAdd = slots.findById(slotID)
                .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }
            unavailability.slots.remove(slotToAdd)
            unavailabilityUnsuitable.slots.add(slotToAdd)

            additionalUnavailability.slots.remove(slotToAdd)
            additionalUnavailabilityUnsuitable.slots.remove(slotToAdd)

            unsuitableToNotIgnore.slots.remove(slotToAdd)
        }

        //TODO Testing if not null
        // if (unavailability != null) {
        //     unavailability.slots.add(slotToAdd)
        // }
        // if (unavailabilityUnsuitable != null) {
        //     unavailabilityUnsuitable.slots.remove(slotToAdd)
        // }
        // unavailability.slots.remove(slotToAdd)
        // unavailabilityUnsuitable.slots.add(slotToAdd)

        // additionalUnavailability.slots.remove(slotToAdd)
        // additionalUnavailabilityUnsuitable.slots.remove(slotToAdd)

        additionalUnavailabilities.save(additionalUnavailability)
        additionalUnavailabilities.save(additionalUnavailabilityUnsuitable)
        
        ignoredUnsuitable.save(unsuitableToNotIgnore)
        
        return Pair(unavailabilities.save(unavailability), unavailabilities.save(unavailabilityUnsuitable))
    }

    //TODO Change multiple slots
    @Transactional
    fun addUnavailabilityGroup(unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): Pair<UnavailabilityDAO, UnavailabilityDAO> {
        val user = users.findById(unavailabilityGroupDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }

        val unavailability : UnavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, false)
        val unavailabilityUnsuitable : UnavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, true)
        val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, false)
        val additionalUnavailabilityUnsuitable : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, true)

        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityGroupDTO.userID)

        // val slotToChange = slots.findById(unavailabilityGroupDTO.slotID)
        //     .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }
            
        for (slotID in unavailabilityGroupDTO.slotsID) {
            val slotToChange = slots.findById(slotID)
                .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }
            unavailability.slots.add(slotToChange)
            unavailabilityUnsuitable.slots.remove(slotToChange)

            additionalUnavailability.slots.remove(slotToChange)
            additionalUnavailabilityUnsuitable.slots.remove(slotToChange)

            unsuitableToNotIgnore.slots.remove(slotToChange)
        }

        // //TODO Testing if not null
        // if (unavailability != null) {
        //     unavailability.slots.remove(slotToChange)
        // }
        // if (unavailabilityUnsuitable != null) {
        //     unavailabilityUnsuitable.slots.add(slotToChange)
        // }
        // unavailability.slots.add(slotToChange)
        // unavailabilityUnsuitable.slots.remove(slotToChange)
        
        // additionalUnavailability.slots.remove(slotToChange)
        // additionalUnavailabilityUnsuitable.slots.remove(slotToChange)

        additionalUnavailabilities.save(additionalUnavailability)
        additionalUnavailabilities.save(additionalUnavailabilityUnsuitable)
        
        ignoredUnsuitable.save(unsuitableToNotIgnore)

        return Pair(unavailabilities.save(unavailability), unavailabilities.save(unavailabilityUnsuitable))
    }

    //TODO Change multiple slots
    @Transactional
    fun deleteUnavailabilityGroup(unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): Pair<UnavailabilityDAO, UnavailabilityDAO> {
        val user = users.findById(unavailabilityGroupDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }
        
        val unavailability : UnavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, false)
        val unavailabilityUnsuitable : UnavailabilityDAO = unavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, true)
        val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, false)
        val additionalUnavailabilityUnsuitable : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, true)

        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityGroupDTO.userID)

        // val slotToRemove = slots.findById(unavailabilityGroupDTO.slotID)
        //     .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        for (slotID in unavailabilityGroupDTO.slotsID) {
            val slotToRemove = slots.findById(slotID)
                .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }
            unavailability.slots.remove(slotToRemove)
            unavailabilityUnsuitable.slots.remove(slotToRemove)

            additionalUnavailability.slots.remove(slotToRemove)
            additionalUnavailabilityUnsuitable.slots.remove(slotToRemove)

            unsuitableToNotIgnore.slots.remove(slotToRemove)
        }


        // //TODO Testing if not null
        // if (unavailability != null) {
        //     unavailability.slots.remove(slotToRemove)
        // }
        // if (unavailabilityUnsuitable != null) {
        //     unavailabilityUnsuitable.slots.remove(slotToRemove)
        // }
        // unavailability.slots.remove(slotToRemove)
        // unavailabilityUnsuitable.slots.remove(slotToRemove)
        
        // additionalUnavailability.slots.remove(slotToRemove)
        // additionalUnavailabilityUnsuitable.slots.remove(slotToRemove)

        additionalUnavailabilities.save(additionalUnavailability)
        additionalUnavailabilities.save(additionalUnavailabilityUnsuitable)
        
        ignoredUnsuitable.save(unsuitableToNotIgnore)

        return Pair(unavailabilities.save(unavailability), unavailabilities.save(unavailabilityUnsuitable))
    }

    @Transactional
    fun addAdditionalUnsuitableSlot(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): Pair<AdditionalUnavailabilityDAO, AdditionalUnavailabilityDAO> {
        val user = users.findById(unavailabilityDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }

        val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, false)
        val additionalUnavailabilityUnsuitable : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, true)

        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityDTO.userID)
        
        val slotToAdd = slots.findById(unavailabilityDTO.slotID)
            .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        //TODO Testing if not null
        // if (unavailability != null) {
        //     unavailability.slots.add(slotToAdd)
        // }
        // if (unavailabilityUnsuitable != null) {
        //     unavailabilityUnsuitable.slots.remove(slotToAdd)
        // }

        additionalUnavailability.slots.remove(slotToAdd)
        additionalUnavailabilityUnsuitable.slots.add(slotToAdd)

        unsuitableToNotIgnore.slots.remove(slotToAdd)

        ignoredUnsuitable.save(unsuitableToNotIgnore)

        return Pair(additionalUnavailabilities.save(additionalUnavailability), additionalUnavailabilities.save(additionalUnavailabilityUnsuitable))
    }

    @Transactional
    fun changeAdditionalUnsuitableToUnavailability(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): Pair<AdditionalUnavailabilityDAO, AdditionalUnavailabilityDAO> {
        val user = users.findById(unavailabilityDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }

        val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, false)
        val additionalUnavailabilityUnsuitable : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, true)

        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityDTO.userID)

        val slotToChange = slots.findById(unavailabilityDTO.slotID)
            .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        // //TODO Testing if not null
        // if (unavailability != null) {
        //     unavailability.slots.remove(slotToChange)
        // }
        // if (unavailabilityUnsuitable != null) {
        //     unavailabilityUnsuitable.slots.add(slotToChange)
        // }
        
        additionalUnavailability.slots.add(slotToChange)
        additionalUnavailabilityUnsuitable.slots.remove(slotToChange)

        unsuitableToNotIgnore.slots.remove(slotToChange)

        ignoredUnsuitable.save(unsuitableToNotIgnore)

        return Pair(additionalUnavailabilities.save(additionalUnavailability), additionalUnavailabilities.save(additionalUnavailabilityUnsuitable))
    }
    
    @Transactional
    fun deleteAdditionalUnavailability(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): Pair<AdditionalUnavailabilityDAO, AdditionalUnavailabilityDAO> {
        val user = users.findById(unavailabilityDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }
        
        val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, false)
        val additionalUnavailabilityUnsuitable : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityDTO.userID, true)

        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityDTO.userID)

        val slotToRemove = slots.findById(unavailabilityDTO.slotID)
            .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        // //TODO Testing if not null
        // if (unavailability != null) {
        //     unavailability.slots.remove(slotToRemove)
        // }
        // if (unavailabilityUnsuitable != null) {
        //     unavailabilityUnsuitable.slots.remove(slotToRemove)
        // }
        additionalUnavailability.slots.remove(slotToRemove)
        additionalUnavailabilityUnsuitable.slots.remove(slotToRemove)

        unsuitableToNotIgnore.slots.remove(slotToRemove)

        ignoredUnsuitable.save(unsuitableToNotIgnore)

        return Pair(additionalUnavailabilities.save(additionalUnavailability), additionalUnavailabilities.save(additionalUnavailabilityUnsuitable))
    }

    
    //TODO Change multiple slots
    @Transactional
    fun addAdditionalUnsuitableSlotGroup(unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): Pair<AdditionalUnavailabilityDAO, AdditionalUnavailabilityDAO> {
        val user = users.findById(unavailabilityGroupDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }

        val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, false)
        val additionalUnavailabilityUnsuitable : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, true)

        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityGroupDTO.userID)

        // val slotToAdd = slots.findById(unavailabilityDTO.slotID)
        //     .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        //Check if all slots exist
        for (slotID in unavailabilityGroupDTO.slotsID) {
            val slotToAdd = slots.findById(slotID)
                .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

            additionalUnavailability.slots.remove(slotToAdd)
            additionalUnavailabilityUnsuitable.slots.add(slotToAdd)
            
            unsuitableToNotIgnore.slots.remove(slotToAdd)
        }

        //TODO Testing if not null
        // if (unavailability != null) {
        //     unavailability.slots.add(slotToAdd)
        // }
        // if (unavailabilityUnsuitable != null) {
        //     unavailabilityUnsuitable.slots.remove(slotToAdd)
        // }
        // unavailability.slots.remove(slotToAdd)
        // unavailabilityUnsuitable.slots.add(slotToAdd)

        // additionalUnavailability.slots.remove(slotToAdd)
        // additionalUnavailabilityUnsuitable.slots.remove(slotToAdd)
        
        ignoredUnsuitable.save(unsuitableToNotIgnore)

        return Pair(additionalUnavailabilities.save(additionalUnavailability), additionalUnavailabilities.save(additionalUnavailabilityUnsuitable))
    }

    //TODO Change multiple slots
    @Transactional
    fun addAdditionalUnavailabilityGroup(unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): Pair<AdditionalUnavailabilityDAO, AdditionalUnavailabilityDAO> {
        val user = users.findById(unavailabilityGroupDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }

        val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, false)
        val additionalUnavailabilityUnsuitable : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, true)

        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityGroupDTO.userID)
        
        // val slotToAdd = slots.findById(unavailabilityDTO.slotID)
        //     .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        //Check if all slots exist
        for (slotID in unavailabilityGroupDTO.slotsID) {
            val slotToChange = slots.findById(slotID)
                .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

            additionalUnavailability.slots.add(slotToChange)
            additionalUnavailabilityUnsuitable.slots.remove(slotToChange)
            
            unsuitableToNotIgnore.slots.remove(slotToChange)
        }

        //TODO Testing if not null
        // if (unavailability != null) {
        //     unavailability.slots.add(slotToAdd)
        // }
        // if (unavailabilityUnsuitable != null) {
        //     unavailabilityUnsuitable.slots.remove(slotToAdd)
        // }
        // unavailability.slots.remove(slotToAdd)
        // unavailabilityUnsuitable.slots.add(slotToAdd)

        // additionalUnavailability.slots.remove(slotToAdd)
        // additionalUnavailabilityUnsuitable.slots.remove(slotToAdd)
        
        ignoredUnsuitable.save(unsuitableToNotIgnore)

        return Pair(additionalUnavailabilities.save(additionalUnavailability), additionalUnavailabilities.save(additionalUnavailabilityUnsuitable))
    }

    //TODO Change multiple slots
    @Transactional
    fun deleteAdditionalUnavailabilityGroup(unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): Pair<AdditionalUnavailabilityDAO, AdditionalUnavailabilityDAO> {
        val user = users.findById(unavailabilityGroupDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }
        
        val additionalUnavailability : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, false)
        val additionalUnavailabilityUnsuitable : AdditionalUnavailabilityDAO = additionalUnavailabilities.findByUserIdAndUnsuitable(unavailabilityGroupDTO.userID, true)

        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityGroupDTO.userID)
        
        // val slotToAdd = slots.findById(unavailabilityDTO.slotID)
        //     .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        //Check if all slots exist
        for (slotID in unavailabilityGroupDTO.slotsID) {
            val slotToRemove = slots.findById(slotID)
                .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

            additionalUnavailability.slots.remove(slotToRemove)
            additionalUnavailabilityUnsuitable.slots.remove(slotToRemove)
            
            unsuitableToNotIgnore.slots.remove(slotToRemove)
        }

        //TODO Testing if not null
        // if (unavailability != null) {
        //     unavailability.slots.add(slotToAdd)
        // }
        // if (unavailabilityUnsuitable != null) {
        //     unavailabilityUnsuitable.slots.remove(slotToAdd)
        // }
        // unavailability.slots.remove(slotToAdd)
        // unavailabilityUnsuitable.slots.add(slotToAdd)

        // additionalUnavailability.slots.remove(slotToAdd)
        // additionalUnavailabilityUnsuitable.slots.remove(slotToAdd)
        
        ignoredUnsuitable.save(unsuitableToNotIgnore)

        return Pair(additionalUnavailabilities.save(additionalUnavailability), additionalUnavailabilities.save(additionalUnavailabilityUnsuitable))
    }

    fun getOneUnavailability(id:Long): UnavailabilityDAO {
        return unavailabilities.findById(id)
            .orElseThrow { UnavailabilityNotFound() }
    }

    fun getAllUnavailabilities(): Iterable<UnavailabilityDAO> = unavailabilities.findAll()
    
    fun getUserUnavailabilities(uid: Long): Pair<UnavailabilityDAO, UnavailabilityDAO> {
        return Pair(unavailabilities.findByUserIdAndUnsuitable(uid, false), unavailabilities.findByUserIdAndUnsuitable(uid, true))
    }
    
    fun getOneAdditionalUnavailability(id:Long): AdditionalUnavailabilityDAO {
        return additionalUnavailabilities.findById(id)
            .orElseThrow { UnavailabilityNotFound() }
    }

    fun getAllAdditionalUnavailabilities(): Pair<Iterable<UnavailabilityDAO>, Iterable<AdditionalUnavailabilityDAO>> {
        return Pair(unavailabilities.findAll(), additionalUnavailabilities.findAll())
    }
    
    fun getAdditionalUserUnavailabilities(uid: Long): Pair<AdditionalUnavailabilityDAO, AdditionalUnavailabilityDAO> {
        return Pair(additionalUnavailabilities.findByUserIdAndUnsuitable(uid, false), additionalUnavailabilities.findByUserIdAndUnsuitable(uid, true))
    }
    
    @Transactional
    fun addIgnoreUnsuitable(unavailabilityDTO: IgnoreOrNotIgnoreUnsuitabilityDTO): IgnoredUnsuitableDAO {
        val user = users.findById(unavailabilityDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }

        val unsuitableToIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityDTO.userID)
        
        val slotToAdd = slots.findById(unavailabilityDTO.slotID)
            .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        unsuitableToIgnore.slots.add(slotToAdd)
        return ignoredUnsuitable.save(unsuitableToIgnore)
    }
    
    @Transactional
    fun deleteIgnoreUnsuitable(unavailabilityDTO: IgnoreOrNotIgnoreUnsuitabilityDTO): IgnoredUnsuitableDAO {
        val user = users.findById(unavailabilityDTO.userID)
        if(user.isEmpty){
            throw UnavailabilityUnprocessable("The user does not exist.")
        }
        
        val unsuitableToNotIgnore : IgnoredUnsuitableDAO = ignoredUnsuitable.findByUserId(unavailabilityDTO.userID)

        val slotToRemove = slots.findById(unavailabilityDTO.slotID)
            .orElseThrow { UnavailabilityUnprocessable("The slot does not exist.") }

        unsuitableToNotIgnore.slots.remove(slotToRemove)
        return ignoredUnsuitable.save(unsuitableToNotIgnore)
    }

    fun getOneIgnoredUnsuitable(id:Long): IgnoredUnsuitableDAO {
        return ignoredUnsuitable.findById(id)
            .orElseThrow { UnavailabilityNotFound() }
    }

    fun getAllIgnoredUnsuitables(): Iterable<IgnoredUnsuitableDAO> = ignoredUnsuitable.findAll()
    
    fun getUserIgnoredUnsuitables(uid: Long): IgnoredUnsuitableDAO {
        return ignoredUnsuitable.findByUserId(uid)
    }
    
}

class UnavailabilityNotFound(message: String? = UNAVAILABILITY_NOT_FOUND) : RuntimeException(message)
class UnavailabilityUnprocessable(message: String) : RuntimeException(message)
// class ComputationInProgress(message: String? = COMPUTATION_IN_PROGRESS) : RuntimeException(message)
