package pt.unl.fct.di.apresentacoes.api.unavailabilities

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeletePresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.IgnoreOrNotIgnorePresentationUnsuitabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteRoomUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.IgnoreOrNotIgnoreUnsuitabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteGroupOfPresentationUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteGroupOfUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.AddOrDeleteGroupOfRoomUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.PresentationUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.AdditionalPresentationUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.PresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AdditionalPresentationUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.IgnoredPresentationUnsuitableDTO
import pt.unl.fct.di.apresentacoes.api.dto.UserUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.RoomUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.AdditionalUserUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.UnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.RoomUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.AdditionalUnavailabilityDTO
import pt.unl.fct.di.apresentacoes.api.dto.IgnoredUnsuitableDTO
import pt.unl.fct.di.apresentacoes.api.dto.ChangeAdditionalUnavailabilitiesDTO
import pt.unl.fct.di.apresentacoes.api.dto.ChangeAdditionalUnavailabilitiesResponseDTO

@RequestMapping("/api/unavailability")
interface UnavailabilityAPI {
    
    @PostMapping("")
    @CanAddAndDeleteUnavailability
    fun addUnsuitableSlot(@RequestBody unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UserUnavailabilitiesDTO

    @PutMapping("")
    @CanAddAndDeleteUnavailability
    fun changeUnsuitableToUnavailability(@RequestBody unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UserUnavailabilitiesDTO

    @DeleteMapping("")
    @CanAddAndDeleteUnavailability
    fun deleteUnavailability(@RequestBody unavailabilityDTO: AddOrDeleteUnavailabilityDTO): UserUnavailabilitiesDTO
    
    @PostMapping("/groupSlot")
    @CanAddAndDeleteGroupUnavailability
    fun addUnsuitableSlotGroup(@RequestBody unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): UserUnavailabilitiesDTO

    @PutMapping("/groupSlot")
    @CanAddAndDeleteGroupUnavailability
    fun addUnavailabilityGroup(@RequestBody unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): UserUnavailabilitiesDTO

    @DeleteMapping("/groupSlot")
    @CanAddAndDeleteGroupUnavailability
    fun deleteUnavailabilityGroup(@RequestBody unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): UserUnavailabilitiesDTO

    @GetMapping("/{id}")
    @CanReadOneUnavailability
    fun getOneUnavailability(@PathVariable id: Long): UnavailabilityDTO

    @GetMapping("")
    @CanReadAllUnavailabilities
    fun getAllUnavailabilities(): Collection<UnavailabilityDTO>

    @GetMapping("/user/{uid}")
    @CanReadUserUnavailabilities
    fun getUserUnavailabilities(@PathVariable uid: Long): UserUnavailabilitiesDTO

    @PostMapping("/additional")
    @CanChangeAdditionalUserUnavailabilities
    fun addAdditionalUnsuitableSlot(@RequestBody unavailabilityDTO: AddOrDeleteUnavailabilityDTO): AdditionalUserUnavailabilitiesDTO

    @PutMapping("/additional")
    @CanChangeAdditionalUserUnavailabilities
    fun changeAdditionalUnsuitableToUnavailability(@RequestBody unavailabilityDTO: AddOrDeleteUnavailabilityDTO): AdditionalUserUnavailabilitiesDTO

    @DeleteMapping("/additional")
    @CanChangeAdditionalUserUnavailabilities
    fun deleteAdditionalUnavailability(@RequestBody unavailabilityDTO: AddOrDeleteUnavailabilityDTO): AdditionalUserUnavailabilitiesDTO
    
    @PostMapping("/additional/groupSlot")
    @CanAddAndDeleteAdditionalGroupUnavailability
    fun addAdditionalUnsuitableSlotGroup(@RequestBody unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): AdditionalUserUnavailabilitiesDTO

    @PutMapping("/additional/groupSlot")
    @CanAddAndDeleteAdditionalGroupUnavailability
    fun addAdditionalUnavailabilityGroup(@RequestBody unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): AdditionalUserUnavailabilitiesDTO

    @DeleteMapping("/additional/groupSlot")
    @CanAddAndDeleteAdditionalGroupUnavailability
    fun deleteAdditionalUnavailabilityGroup(@RequestBody unavailabilityGroupDTO: AddOrDeleteGroupOfUnavailabilitiesDTO): AdditionalUserUnavailabilitiesDTO

    @GetMapping("/additional/{id}")
    @CanReadOneAdditionalUnavailability
    fun getOneAdditionalUnavailability(@PathVariable id: Long): AdditionalUnavailabilityDTO

    @GetMapping("/additional")
    @CanReadAllAdditionalUnavailabilities
    fun getAllAdditionalUnavailabilities(): Pair<Collection<UnavailabilityDTO>, Collection<AdditionalUnavailabilityDTO>>

    @GetMapping("/additional/user/{uid}")
    @CanReadAdditionalUserUnavailabilities
    fun getAdditionalUserUnavailabilities(@PathVariable uid: Long): AdditionalUserUnavailabilitiesDTO
    
    @PostMapping("/ignored")
    @CanIgnoreUnavailabilities
    fun addIgnoreUnsuitable(@RequestBody unavailabilityDTO: IgnoreOrNotIgnoreUnsuitabilityDTO): IgnoredUnsuitableDTO

    @DeleteMapping("/ignored")
    @CanIgnoreUnavailabilities
    fun deleteIgnoreUnsuitable(@RequestBody unavailabilityDTO: IgnoreOrNotIgnoreUnsuitabilityDTO): IgnoredUnsuitableDTO
    
    @GetMapping("/ignored/{id}")
    @CanIgnoreUnavailabilities
    fun getOneIgnoredUnsuitable(@PathVariable id:Long): IgnoredUnsuitableDTO

    @GetMapping("/ignored")
    @CanIgnoreUnavailabilities
    fun getAllIgnoredUnsuitables(): Collection<IgnoredUnsuitableDTO>

    @GetMapping("/ignored/user/{uid}")
    @CanIgnoreUnavailabilities
    fun getUserIgnoredUnsuitables(@PathVariable uid: Long): IgnoredUnsuitableDTO

    /** PRESENTATION UNAVAILABILITIES - RESTRICTIONS FOR ONLY ONE PRESENTATION **/

    @PostMapping("/presentation")
    @CanAddAndDeletePresentationUnavailability
    fun addPresentationUnsuitable(@RequestBody presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilitiesDTO
    
    @PutMapping("/presentation")
    @CanAddAndDeletePresentationUnavailability
    fun changePresentationUnsuitableToUnavailability(@RequestBody presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilitiesDTO

    @DeleteMapping("/presentation")
    @CanAddAndDeletePresentationUnavailability
    fun deletePresentationUnavailability(@RequestBody presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): PresentationUnavailabilitiesDTO

    @PostMapping("/presentation/groupSlot")
    @CanAddAndDeleteGroupPresentationUnavailability
    fun addPresentationUnsuitableGroup(@RequestBody presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): PresentationUnavailabilitiesDTO
    
    @PutMapping("/presentation/groupSlot")
    @CanAddAndDeleteGroupPresentationUnavailability
    fun addPresentationUnavailabilityGroup(@RequestBody presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): PresentationUnavailabilitiesDTO

    @DeleteMapping("/presentation/groupSlot")
    @CanAddAndDeleteGroupPresentationUnavailability
    fun deletePresentationUnavailabilityGroup(@RequestBody presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): PresentationUnavailabilitiesDTO

    @GetMapping("/presentationUnavailability/{puid}")
    @CanReadOnePresentationUnavailability
    fun getOnePresentationUnavailability(@PathVariable puid: Long): PresentationUnavailabilityDTO

    @GetMapping("/presentation")
    @CanReadAllPresentationUnavailabilities
    fun getAllPresentationUnavailabilities(): Collection<PresentationUnavailabilityDTO>

    @GetMapping("/presentation/{pid}")
    @CanReadPresentationUnavailabilities
    fun getPresentationUnavailabilities(@PathVariable pid: Long): PresentationUnavailabilitiesDTO
    
    @PostMapping("/additional/presentation")
    @CanChangeAdditionalPresentationUnavailabilities
    fun addAdditionalPresentationUnsuitable(@RequestBody presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): AdditionalPresentationUnavailabilitiesDTO
    
    @PutMapping("/additional/presentation")
    @CanChangeAdditionalPresentationUnavailabilities
    fun changeAdditionalPresentationUnsuitableToUnavailability(@RequestBody presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): AdditionalPresentationUnavailabilitiesDTO

    @DeleteMapping("/additional/presentation")
    @CanChangeAdditionalPresentationUnavailabilities
    fun deleteAdditionalPresentationUnavailability(@RequestBody presentationUnavailabilityDTO: AddOrDeletePresentationUnavailabilityDTO): AdditionalPresentationUnavailabilitiesDTO

    @PostMapping("/additional/presentation/groupSlot")
    @CanAddAndDeleteAdditionalGroupPresentationUnavailability
    fun addAdditionalPresentationUnsuitableGroup(@RequestBody presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): AdditionalPresentationUnavailabilitiesDTO
    
    @PutMapping("/additional/presentation/groupSlot")
    @CanAddAndDeleteAdditionalGroupPresentationUnavailability
    fun addAdditionalPresentationUnavailabilityGroup(@RequestBody presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): AdditionalPresentationUnavailabilitiesDTO

    @DeleteMapping("/additional/presentation/groupSlot")
    @CanAddAndDeleteAdditionalGroupPresentationUnavailability
    fun deleteAdditionalPresentationUnavailabilityGroup(@RequestBody presentationUnavailabilityGroupDTO: AddOrDeleteGroupOfPresentationUnavailabilitiesDTO): AdditionalPresentationUnavailabilitiesDTO

    @GetMapping("/additional/presentationUnavailability/{puid}")
    @CanReadOneAdditionalPresentationUnavailability
    fun getOneAdditionalPresentationUnavailability(@PathVariable puid: Long): AdditionalPresentationUnavailabilityDTO

    @GetMapping("/additional/presentation")
    @CanReadAllAdditionalPresentationUnavailabilities
    fun getAllAdditionalPresentationUnavailabilities(): Pair<Collection<PresentationUnavailabilityDTO>, Collection<AdditionalPresentationUnavailabilityDTO>>
    // fun getAllAdditionalPresentationUnavailabilities(): Collection<PresentationUnavailabilityDTO>

    @GetMapping("/additional/presentation/{pid}")
    @CanReadAdditionalPresentationUnavailabilities
    fun getAdditionalPresentationUnavailabilities(@PathVariable pid: Long): AdditionalPresentationUnavailabilitiesDTO
    
    @PostMapping("/ignored/presentation")
    @CanIgnorePresentationUnavailabilities
    fun addIgnorePresentationUnsuitable(@RequestBody unavailabilityDTO: IgnoreOrNotIgnorePresentationUnsuitabilityDTO): IgnoredPresentationUnsuitableDTO

    @DeleteMapping("/ignored/presentation")
    @CanIgnorePresentationUnavailabilities
    fun deleteIgnorePresentationUnsuitable(@RequestBody unavailabilityDTO: IgnoreOrNotIgnorePresentationUnsuitabilityDTO): IgnoredPresentationUnsuitableDTO
    
    @GetMapping("/ignored/presentationUnavailability/{id}")
    @CanIgnorePresentationUnavailabilities
    fun getOneIgnoredPresentationUnsuitable(@PathVariable id:Long): IgnoredPresentationUnsuitableDTO

    @GetMapping("/ignored/presentation")
    @CanIgnorePresentationUnavailabilities
    fun getAllIgnoredPresentationUnsuitable(): Collection<IgnoredPresentationUnsuitableDTO>

    @GetMapping("/ignored/presentation/{pid}")
    @CanIgnorePresentationUnavailabilities
    fun getPresentationIgnoredPresentationUnsuitables(@PathVariable pid: Long): IgnoredPresentationUnsuitableDTO

    /** ROOM UNAVAILABILITIES - RESTRICTIONS FOR ONLY ONE ROOM **/
    
    @PostMapping("/room")
    @CanAddAndDeleteRoomUnavailability
    fun addRoomUnsuitableSlot(@RequestBody roomUnavailabilityDTO: AddOrDeleteRoomUnavailabilityDTO): RoomUnavailabilitiesDTO

    @PutMapping("/room")
    @CanAddAndDeleteRoomUnavailability
    fun changeRoomUnsuitableToUnavailability(@RequestBody roomUnavailabilityDTO: AddOrDeleteRoomUnavailabilityDTO): RoomUnavailabilitiesDTO

    @DeleteMapping("/room")
    @CanAddAndDeleteRoomUnavailability
    fun deleteRoomUnavailability(@RequestBody roomUnavailabilityDTO: AddOrDeleteRoomUnavailabilityDTO): RoomUnavailabilitiesDTO
    
    @PostMapping("/room/groupSlot")
    @CanAddAndDeleteGroupRoomUnavailability
    fun addRoomUnsuitableSlotGroup(@RequestBody roomUnavailabilityGroupDTO: AddOrDeleteGroupOfRoomUnavailabilitiesDTO): RoomUnavailabilitiesDTO

    @PutMapping("/room/groupSlot")
    @CanAddAndDeleteGroupRoomUnavailability
    fun addRoomUnavailabilityGroup(@RequestBody roomUnavailabilityGroupDTO: AddOrDeleteGroupOfRoomUnavailabilitiesDTO): RoomUnavailabilitiesDTO

    @DeleteMapping("/room/groupSlot")
    @CanAddAndDeleteGroupRoomUnavailability
    fun deleteRoomUnavailabilityGroup(@RequestBody roomUnavailabilityGroupDTO: AddOrDeleteGroupOfRoomUnavailabilitiesDTO): RoomUnavailabilitiesDTO

    @GetMapping("/roomUnavailability/{id}")
    @CanReadOneRoomUnavailability
    fun getOneRoomUnavailability(@PathVariable id: Long): RoomUnavailabilityDTO

    @GetMapping("/room")
    @CanReadAllRoomUnavailabilities
    fun getAllRoomUnavailabilities(): Collection<RoomUnavailabilityDTO>

    @GetMapping("/room/{rid}")
    @CanReadRoomUnavailabilities
    fun getRoomUnavailabilities(@PathVariable rid: Long): RoomUnavailabilitiesDTO

}