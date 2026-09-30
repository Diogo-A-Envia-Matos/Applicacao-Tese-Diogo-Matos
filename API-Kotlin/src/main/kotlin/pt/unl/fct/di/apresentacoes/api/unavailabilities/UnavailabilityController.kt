package pt.unl.fct.di.apresentacoes.api.unavailabilities

import org.springframework.web.bind.annotation.RestController

import pt.unl.fct.di.apresentacoes.api.dto.AdditionalPresentationUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeletePresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.IgnoreOrNotIgnorePresentationUnsuitabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.IgnoreOrNotIgnoreUnsuitabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteGroupOfUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteGroupOfPresentationUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.PresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.PresentationUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.UserUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.UnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AdditionalUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AdditionalUserUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.AdditionalPresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.IgnoredPresentationUnsuitableDTO
import pt.unl.fct.di.apresentacoes.api.dto.IgnoredUnsuitableDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteRoomUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteGroupOfRoomUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.RoomUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.RoomUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.ChangeAdditionalUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.ChangeAdditionalUnavailabilitiesResponseDTO
import pt.unl.fct.di.apresentacoes.api.exceptions.APIConflictException
import pt.unl.fct.di.apresentacoes.api.exceptions.APINotFoundException
import pt.unl.fct.di.apresentacoes.api.exceptions.APIUnprocessableEntityException
import pt.unl.fct.di.apresentacoes.api.exceptions.ComputationInProgress
import pt.unl.fct.di.apresentacoes.api.exceptions.ChangesNotAllowed
import pt.unl.fct.di.apresentacoes.domain.UnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.PresentationUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.AdditionalUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.AdditionalPresentationUnavailabilityDAO
import pt.unl.fct.di.apresentacoes.services.*

private const val COMPUTATION_IN_PROGRESS = "There is a computation in progress."
private const val CHANGES_NOT_ALLOWED = "Currently, changes are not allowed to be performed."

@RestController
class UnavailabilityController(val unavailabilities: UnavailabilityService,
                               val presentationUnavailabilities: PresentationUnavailabilityService,
                               val roomUnavailabilities: RoomUnavailabilityService,
                               val computation: ComputationService,
                               val stopChanges: StopChangesService
                               ) : UnavailabilityAPI {

    override fun addUnsuitableSlot(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UserUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            val response = unavailabilities.addUnsuitableSlot(unavailabilityDTO)
            return UserUnavailabilitiesDTO(UnavailabilityDTO(response.first), UnavailabilityDTO(response.second))
        } catch (e: UnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun changeUnsuitableToUnavailability(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UserUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            val response = unavailabilities.changeUnsuitableToUnavailability(unavailabilityDTO)
            return UserUnavailabilitiesDTO(UnavailabilityDTO(response.first), UnavailabilityDTO(response.second))
        } catch (e: UnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteUnavailability(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UserUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            val response = unavailabilities.deleteUnavailability(unavailabilityDTO)
            return UserUnavailabilitiesDTO(UnavailabilityDTO(response.first), UnavailabilityDTO(response.second))
        } catch (e: UnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun addUnsuitableSlotGroup(unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): UserUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            val response = unavailabilities.addUnsuitableSlotGroup(unavailabilityGroupDTO)
            return UserUnavailabilitiesDTO(UnavailabilityDTO(response.first), UnavailabilityDTO(response.second))
        } catch (e: UnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun addUnavailabilityGroup(unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): UserUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            val response = unavailabilities.addUnavailabilityGroup(unavailabilityGroupDTO)
            return UserUnavailabilitiesDTO(UnavailabilityDTO(response.first), UnavailabilityDTO(response.second))
        } catch (e: UnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteUnavailabilityGroup(unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): UserUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            val response = unavailabilities.deleteUnavailabilityGroup(unavailabilityGroupDTO)
            return UserUnavailabilitiesDTO(UnavailabilityDTO(response.first), UnavailabilityDTO(response.second))
        } catch (e: UnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun getOneUnavailability(id: Long): UnavailabilityDTO {
        try {
            return UnavailabilityDTO(unavailabilities.getOneUnavailability(id))
        } catch (e : UnavailabilityNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun getAllUnavailabilities(): Collection<UnavailabilityDTO> =
        unavailabilities.getAllUnavailabilities().map { e -> UnavailabilityDTO(e) }

    override fun getUserUnavailabilities(uid: Long): UserUnavailabilitiesDTO {
        val response = unavailabilities.getUserUnavailabilities(uid)
        return UserUnavailabilitiesDTO(UnavailabilityDTO(response.first), UnavailabilityDTO(response.second))
    }
    
    override fun addAdditionalUnsuitableSlot(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): AdditionalUserUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = unavailabilities.addAdditionalUnsuitableSlot(unavailabilityDTO)
            return AdditionalUserUnavailabilitiesDTO(AdditionalUnavailabilityDTO(response.first), AdditionalUnavailabilityDTO(response.second))
        } catch (e: UnavailabilityUnprocessable) {
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun changeAdditionalUnsuitableToUnavailability(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): AdditionalUserUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = unavailabilities.changeAdditionalUnsuitableToUnavailability(unavailabilityDTO)
            return AdditionalUserUnavailabilitiesDTO(AdditionalUnavailabilityDTO(response.first), AdditionalUnavailabilityDTO(response.second))
        } catch (e: UnavailabilityUnprocessable) {
            throw APIUnprocessableEntityException(e.message)
        }
    }
    
    override fun deleteAdditionalUnavailability(unavailabilityDTO: AddOrDeleteUnavailabilityDTO): AdditionalUserUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = unavailabilities.deleteAdditionalUnavailability(unavailabilityDTO)
            return AdditionalUserUnavailabilitiesDTO(AdditionalUnavailabilityDTO(response.first), AdditionalUnavailabilityDTO(response.second))
        } catch (e: UnavailabilityUnprocessable) {
            throw APIUnprocessableEntityException(e.message)
        }
    }
    
    override fun addAdditionalUnsuitableSlotGroup(unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): AdditionalUserUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = unavailabilities.addAdditionalUnsuitableSlotGroup(unavailabilityGroupDTO)
            return AdditionalUserUnavailabilitiesDTO(AdditionalUnavailabilityDTO(response.first), AdditionalUnavailabilityDTO(response.second))
        } catch (e: UnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun addAdditionalUnavailabilityGroup(unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): AdditionalUserUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = unavailabilities.addAdditionalUnavailabilityGroup(unavailabilityGroupDTO)
            return AdditionalUserUnavailabilitiesDTO(AdditionalUnavailabilityDTO(response.first), AdditionalUnavailabilityDTO(response.second))
        } catch (e: UnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteAdditionalUnavailabilityGroup(unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): AdditionalUserUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = unavailabilities.deleteAdditionalUnavailabilityGroup(unavailabilityGroupDTO)
            return AdditionalUserUnavailabilitiesDTO(AdditionalUnavailabilityDTO(response.first), AdditionalUnavailabilityDTO(response.second))
        } catch (e: UnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    
    override fun getOneAdditionalUnavailability(id: Long): AdditionalUnavailabilityDTO {
        try {
            return AdditionalUnavailabilityDTO(unavailabilities.getOneAdditionalUnavailability(id))
        } catch (e : UnavailabilityNotFound) {
            throw APINotFoundException(e.message)
        }
    }
    
    override fun getAllAdditionalUnavailabilities(): Pair<Collection<UnavailabilityDTO>, Collection<AdditionalUnavailabilityDTO>> {
        val response = unavailabilities.getAllAdditionalUnavailabilities()
        // val firstList : Iterable<UnavailabilityDAO> = response.first
        // val secondList : Iterable<AdditionalUnavailabilityDAO> = response.second
        val baseUnavailabilities : Collection<UnavailabilityDTO> = response.first.map { e -> UnavailabilityDTO(e) }
        val additionalUnavailabilities : Collection<AdditionalUnavailabilityDTO> = response.second.map { e -> AdditionalUnavailabilityDTO(e) }
        // val baseUnavailabilities = firstList.map { e -> UnavailabilityDTO(e) }
        // val additionalUnavailabilities = secondList.map { e -> UnavailabilityDTO(e) }
        return Pair(baseUnavailabilities, additionalUnavailabilities)
    }

    override fun getAdditionalUserUnavailabilities(uid: Long): AdditionalUserUnavailabilitiesDTO {
        val response = unavailabilities.getAdditionalUserUnavailabilities(uid)
        return AdditionalUserUnavailabilitiesDTO(AdditionalUnavailabilityDTO(response.first), AdditionalUnavailabilityDTO(response.second))
    }
    
    override fun addIgnoreUnsuitable(unavailabilityDTO: IgnoreOrNotIgnoreUnsuitabilityDTO): IgnoredUnsuitableDTO {
        try {
            privateCheckComputation()
            // val response = unavailabilities.addIgnoreUnsuitable(unavailabilityDTO)
            return IgnoredUnsuitableDTO(unavailabilities.addIgnoreUnsuitable(unavailabilityDTO))
        } catch (e: UnavailabilityUnprocessable) {
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteIgnoreUnsuitable(unavailabilityDTO: IgnoreOrNotIgnoreUnsuitabilityDTO): IgnoredUnsuitableDTO {
        try {
            privateCheckComputation()
            // val response = unavailabilities.deleteIgnoreUnsuitable(unavailabilityDTO)
            return IgnoredUnsuitableDTO(unavailabilities.deleteIgnoreUnsuitable(unavailabilityDTO))
        } catch (e: UnavailabilityUnprocessable) {
            throw APIUnprocessableEntityException(e.message)
        }
    }
    
    // override fun getOrganizerIgnoreUnsuitable(oid: Long): List<IgnoredUnsuitableDTO> {
    //     return IgnoredUnsuitableDTO(unavailabilities.getOrganizerIgnoreUnsuitable(oid))
    // }

    override fun getOneIgnoredUnsuitable(id:Long): IgnoredUnsuitableDTO {
        try {
            return IgnoredUnsuitableDTO(unavailabilities.getOneIgnoredUnsuitable(id))
        } catch (e : UnavailabilityNotFound) {
            throw APINotFoundException(e.message)
        }
    }

    override fun getAllIgnoredUnsuitables(): Collection<IgnoredUnsuitableDTO> =
        unavailabilities.getAllIgnoredUnsuitables().map { e -> IgnoredUnsuitableDTO(e) }

    // fun getUserUnavailabilities(uid: Long): UnavailabilityDAO{
    //     return unavailabilities.findByUserId(uid)
    // }
    
    override fun getUserIgnoredUnsuitables(uid: Long): IgnoredUnsuitableDTO =
        IgnoredUnsuitableDTO(unavailabilities.getUserIgnoredUnsuitables(uid))
    

    /** PRESENTATION UNAVAILABILITIES - RESTRICTIONS FOR ONLY ONE PRESENTATION **/


    override fun addPresentationUnsuitable(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            val response = presentationUnavailabilities.addPresentationUnsuitable(presentationUnavailabilityDTO)
            return PresentationUnavailabilitiesDTO(PresentationUnavailabilityDTO(response.first), PresentationUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun changePresentationUnsuitableToUnavailability(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            val response = presentationUnavailabilities.changePresentationUnsuitableToUnavailability(presentationUnavailabilityDTO)
            return PresentationUnavailabilitiesDTO(PresentationUnavailabilityDTO(response.first), PresentationUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deletePresentationUnavailability(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            val response = presentationUnavailabilities.deletePresentationUnavailability(presentationUnavailabilityDTO)
            return PresentationUnavailabilitiesDTO(PresentationUnavailabilityDTO(response.first), PresentationUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun addPresentationUnsuitableGroup(presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): PresentationUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            val response = presentationUnavailabilities.addPresentationUnsuitableGroup(presentationUnavailabilityGroupDTO)
            return PresentationUnavailabilitiesDTO(PresentationUnavailabilityDTO(response.first), PresentationUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun addPresentationUnavailabilityGroup(presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): PresentationUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            val response = presentationUnavailabilities.addPresentationUnavailabilityGroup(presentationUnavailabilityGroupDTO)
            return PresentationUnavailabilitiesDTO(PresentationUnavailabilityDTO(response.first), PresentationUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deletePresentationUnavailabilityGroup(presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): PresentationUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            privateCheckStopChanges()
            val response = presentationUnavailabilities.deletePresentationUnavailabilityGroup(presentationUnavailabilityGroupDTO)
            return PresentationUnavailabilitiesDTO(PresentationUnavailabilityDTO(response.first), PresentationUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun getOnePresentationUnavailability(puid: Long): PresentationUnavailabilityDTO {
        try {
            return PresentationUnavailabilityDTO(presentationUnavailabilities.getOnePresentationUnavailability(puid))
        } catch (e : PresentationUnavailabilityNotFound){
            throw APINotFoundException(e.message)
        }
    }
    
    override fun getAllPresentationUnavailabilities(): Collection<PresentationUnavailabilityDTO> =
        presentationUnavailabilities.getAllPresentationUnavailabilities().map { e -> PresentationUnavailabilityDTO(e) }
    
    override fun getPresentationUnavailabilities(pid: Long): PresentationUnavailabilitiesDTO {
        val response = presentationUnavailabilities.getPresentationUnavailabilities(pid)
        return PresentationUnavailabilitiesDTO(PresentationUnavailabilityDTO(response.first), PresentationUnavailabilityDTO(response.second))
    }

    override fun addAdditionalPresentationUnsuitableGroup(presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): AdditionalPresentationUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = presentationUnavailabilities.addAdditionalPresentationUnsuitableGroup(presentationUnavailabilityGroupDTO)
            return AdditionalPresentationUnavailabilitiesDTO(AdditionalPresentationUnavailabilityDTO(response.first), AdditionalPresentationUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun addAdditionalPresentationUnavailabilityGroup(presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): AdditionalPresentationUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = presentationUnavailabilities.addAdditionalPresentationUnavailabilityGroup(presentationUnavailabilityGroupDTO)
            return AdditionalPresentationUnavailabilitiesDTO(AdditionalPresentationUnavailabilityDTO(response.first), AdditionalPresentationUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteAdditionalPresentationUnavailabilityGroup(presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): AdditionalPresentationUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = presentationUnavailabilities.deleteAdditionalPresentationUnavailabilityGroup(presentationUnavailabilityGroupDTO)
            return AdditionalPresentationUnavailabilitiesDTO(AdditionalPresentationUnavailabilityDTO(response.first), AdditionalPresentationUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun addAdditionalPresentationUnsuitable(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): AdditionalPresentationUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = presentationUnavailabilities.addAdditionalPresentationUnsuitable(presentationUnavailabilityDTO)
            return AdditionalPresentationUnavailabilitiesDTO(AdditionalPresentationUnavailabilityDTO(response.first), AdditionalPresentationUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun changeAdditionalPresentationUnsuitableToUnavailability(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): AdditionalPresentationUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = presentationUnavailabilities.changeAdditionalPresentationUnsuitableToUnavailability(presentationUnavailabilityDTO)
            return AdditionalPresentationUnavailabilitiesDTO(AdditionalPresentationUnavailabilityDTO(response.first), AdditionalPresentationUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteAdditionalPresentationUnavailability(presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): AdditionalPresentationUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = presentationUnavailabilities.deleteAdditionalPresentationUnavailability(presentationUnavailabilityDTO)
            return AdditionalPresentationUnavailabilitiesDTO(AdditionalPresentationUnavailabilityDTO(response.first), AdditionalPresentationUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun getOneAdditionalPresentationUnavailability(puid: Long): AdditionalPresentationUnavailabilityDTO {
        try {
            return AdditionalPresentationUnavailabilityDTO(presentationUnavailabilities.getOneAdditionalPresentationUnavailability(puid))
        } catch (e : PresentationUnavailabilityNotFound){
            throw APINotFoundException(e.message)
        }
    }
    
    override fun getAllAdditionalPresentationUnavailabilities(): Pair<Collection<PresentationUnavailabilityDTO>, Collection<AdditionalPresentationUnavailabilityDTO>> {
        val response = presentationUnavailabilities.getAllAdditionalPresentationUnavailabilities()
        // val firstList = response.first
        // val secondList = response.second
        // val firstList : Iterable<PresentationUnavailabilityDAO> = response.first
        // val secondList : Iterable<PresentationUnavailabilityDAO> = response.second
        // val secondList : Iterable<AdditionalPresentationUnavailabilityDAO> = response.second
        val basePresentationUnavailabilities : Collection<PresentationUnavailabilityDTO> = response.first.map { e -> PresentationUnavailabilityDTO(e) }
        val additionalPresentationUnavailabilities : Collection<AdditionalPresentationUnavailabilityDTO> = response.second.map { e -> AdditionalPresentationUnavailabilityDTO(e) }
        // val basePresentationUnavailabilities : Collection<PresentationUnavailabilityDTO> = firstList.map { e -> PresentationUnavailabilityDTO(e as PresentationUnavailabilityDAO) }
        // val additionalPresentationUnavailabilities : Collection<PresentationUnavailabilityDTO> = secondList.map { e -> PresentationUnavailabilityDTO(e as AdditionalPresentationUnavailabilityDAO) }
        return Pair(basePresentationUnavailabilities, additionalPresentationUnavailabilities)
    }
    
    override fun getAdditionalPresentationUnavailabilities(pid: Long): AdditionalPresentationUnavailabilitiesDTO {
        val response = presentationUnavailabilities.getAdditionalPresentationUnavailabilities(pid)
        // AdditionalPresentationUnavailabilityDAO
        // for (obj in response) {
        //     //TODO Convert to AdditionalPresentationUnavailabilityDAO to AdditionalPresentationUnavailabilityDTO
        // }
        return AdditionalPresentationUnavailabilitiesDTO(AdditionalPresentationUnavailabilityDTO(response.first), AdditionalPresentationUnavailabilityDTO(response.second))
    }

    override fun addIgnorePresentationUnsuitable(presentationUnavailabilityDTO: IgnoreOrNotIgnorePresentationUnsuitabilityDTO): IgnoredPresentationUnsuitableDTO {
        try {
            privateCheckComputation()
            // val response = presentationUnavailabilities.addIgnorePresentationUnsuitable(presentationUnavailabilityDTO)
            return IgnoredPresentationUnsuitableDTO(presentationUnavailabilities.addIgnorePresentationUnsuitable(presentationUnavailabilityDTO))
        } catch (e: PresentationUnavailabilityUnprocessable) {
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteIgnorePresentationUnsuitable(presentationUnavailabilityDTO: IgnoreOrNotIgnorePresentationUnsuitabilityDTO): IgnoredPresentationUnsuitableDTO {
        try {
            privateCheckComputation()
            // val response = presentationUnavailabilities.deleteIgnorePresentationUnsuitable(presentationUnavailabilityDTO)
            return IgnoredPresentationUnsuitableDTO(presentationUnavailabilities.deleteIgnorePresentationUnsuitable(presentationUnavailabilityDTO))
        } catch (e: PresentationUnavailabilityUnprocessable) {
            throw APIUnprocessableEntityException(e.message)
        }
    }
    
    override fun getOneIgnoredPresentationUnsuitable(id: Long): IgnoredPresentationUnsuitableDTO {
        try {
            return IgnoredPresentationUnsuitableDTO(presentationUnavailabilities.getOneIgnoredPresentationUnsuitable(id))
        } catch (e : PresentationUnavailabilityNotFound) {
            throw APINotFoundException(e.message)
        }
    }

    override fun getAllIgnoredPresentationUnsuitable(): Collection<IgnoredPresentationUnsuitableDTO> =
        presentationUnavailabilities.getAllIgnoredPresentationUnsuitable().map { e -> IgnoredPresentationUnsuitableDTO(e) }

    // fun getUserUnavailabilities(uid: Long): UnavailabilityDAO{
    //     return unavailabilities.findByUserId(uid)
    // }
    
    override fun getPresentationIgnoredPresentationUnsuitables(pid: Long): IgnoredPresentationUnsuitableDTO = 
        IgnoredPresentationUnsuitableDTO(presentationUnavailabilities.getPresentationIgnoredPresentationUnsuitables(pid))


    /** ROOM UNAVAILABILITIES - RESTRICTIONS FOR ONLY ONE ROOM **/
    
    override fun addRoomUnsuitableSlot(roomUnavailabilityDTO: AddOrDeleteRoomUnavailabilityDTO): RoomUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = roomUnavailabilities.addRoomUnsuitableSlot(roomUnavailabilityDTO)
            return RoomUnavailabilitiesDTO(RoomUnavailabilityDTO(response.first), RoomUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun changeRoomUnsuitableToUnavailability(roomUnavailabilityDTO: AddOrDeleteRoomUnavailabilityDTO): RoomUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = roomUnavailabilities.changeRoomUnsuitableToUnavailability(roomUnavailabilityDTO)
            return RoomUnavailabilitiesDTO(RoomUnavailabilityDTO(response.first), RoomUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteRoomUnavailability(roomUnavailabilityDTO: AddOrDeleteRoomUnavailabilityDTO): RoomUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = roomUnavailabilities.deleteRoomUnavailability(roomUnavailabilityDTO)
            return RoomUnavailabilitiesDTO(RoomUnavailabilityDTO(response.first), RoomUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun addRoomUnsuitableSlotGroup(roomUnavailabilityGroupDTO: AddOrDeleteGroupOfRoomUnavailabilitiesDTO): RoomUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = roomUnavailabilities.addRoomUnsuitableSlotGroup(roomUnavailabilityGroupDTO)
            return RoomUnavailabilitiesDTO(RoomUnavailabilityDTO(response.first), RoomUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun addRoomUnavailabilityGroup(roomUnavailabilityGroupDTO: AddOrDeleteGroupOfRoomUnavailabilitiesDTO): RoomUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = roomUnavailabilities.addRoomUnavailabilityGroup(roomUnavailabilityGroupDTO)
            return RoomUnavailabilitiesDTO(RoomUnavailabilityDTO(response.first), RoomUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteRoomUnavailabilityGroup(roomUnavailabilityGroupDTO: AddOrDeleteGroupOfRoomUnavailabilitiesDTO): RoomUnavailabilitiesDTO {
        try {
            privateCheckComputation()
            val response = roomUnavailabilities.deleteRoomUnavailabilityGroup(roomUnavailabilityGroupDTO)
            return RoomUnavailabilitiesDTO(RoomUnavailabilityDTO(response.first), RoomUnavailabilityDTO(response.second))
        } catch (e: PresentationUnavailabilityUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun getOneRoomUnavailability(id: Long): RoomUnavailabilityDTO {
        try {
            return RoomUnavailabilityDTO(roomUnavailabilities.getOneRoomUnavailability(id))
        } catch (e : PresentationUnavailabilityNotFound){
            throw APINotFoundException(e.message)
        }
    }
    
    override fun getAllRoomUnavailabilities(): Collection<RoomUnavailabilityDTO> =
        roomUnavailabilities.getAllRoomUnavailabilities().map { e -> RoomUnavailabilityDTO(e) }
    
    override fun getRoomUnavailabilities(rid: Long): RoomUnavailabilitiesDTO {
        val response = roomUnavailabilities.getRoomUnavailabilities(rid)
        return RoomUnavailabilitiesDTO(RoomUnavailabilityDTO(response.first), RoomUnavailabilityDTO(response.second))
    }

    private fun privateCheckComputation() {
        try{
            if (computation.getComputationStatus()) {
                throw ComputationInProgress(COMPUTATION_IN_PROGRESS)
            }
        } catch (e:PresentationNotFound) {
            throw APINotFoundException(e.message)
        }
    }

    private fun privateCheckStopChanges() {
        try{
            if (stopChanges.getStateOfChangesOfRestrictions()) {
                throw ChangesNotAllowed(CHANGES_NOT_ALLOWED)
            }
        } catch (e:StopChangesNotFound) {
            throw APINotFoundException(e.message)
        }
    }
}
