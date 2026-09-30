package pt.unl.fct.di.apresentacoes.api.dto

import pt.unl.fct.di.apresentacoes.domain.*
import java.time.LocalDate
import java.time.LocalTime

data class CreateUserDTO(
    val id: Long,
    val name: String,
    val email: String
) {
    constructor(userDAO: UserDAO) : this(
        userDAO.id,
        userDAO.name,
        userDAO.email
    )
}

data class FullUserDTO(
    val id: Long,
    val name: String,
    val email: String,
    val role: UserRoles
) {
    constructor(userDAO: UserDAO) : this(
        userDAO.id,
        userDAO.name,
        userDAO.email,
        userDAO.role
    )
}

data class UserNameDTO(
    val name: String,
) {
    constructor(userDAO: UserDAO) : this(
        userDAO.name,
    )
}

data class CreateIdentityDTO(
    val userId: Long,
    val name: String,
    val password: String,
    val provider: String,
    val identity: String
)

data class GetIdentityDTO(
    val userId: Long,
    val provider: String,
    val identity: String
)

data class SlotDTO(
    val id: Long,
    val date: LocalDate,
    val startingHour: LocalTime
) {
    constructor(slotDAO: SlotDAO) : this(
        slotDAO.id,
        slotDAO.date,
        slotDAO.startingHour
    )
}

data class SlotsDataDTO(
    val date: LocalDate,
    val numberOfSlots: Long
)

data class RoomDTO(
    val id: Long,

    val roomNr:String,
    val building:String
) {
    constructor(roomDAO: RoomDAO) : this(
        roomDAO.id,
        roomDAO.roomNr,
        roomDAO.building
    )
}

data class UnavailabilityDTO(
    val id: Long,
    val unsuitable: Boolean,

    val slotsID: Set<Long>,
    val userID: Long
) {
    constructor(unavailabilityDAO: UnavailabilityDAO) : this(
        unavailabilityDAO.id,
        unavailabilityDAO.unsuitable,
        unavailabilityDAO.slots.map { e -> e.id }.toSet(),
        unavailabilityDAO.user.id
    )
}

data class AdditionalUnavailabilityDTO(
    val id: Long,
    val unsuitable: Boolean,

    val slotsID: Set<Long>,
    val userID: Long
) {
    constructor(additionalUnavailabilityDAO: AdditionalUnavailabilityDAO) : this(
        additionalUnavailabilityDAO.id,
        additionalUnavailabilityDAO.unsuitable,
        additionalUnavailabilityDAO.slots.map { e -> e.id }.toSet(),
        additionalUnavailabilityDAO.user.id
    )
}

data class IgnoredUnsuitableDTO(
    val id: Long,

    val slotsID: Set<Long>,
    val userID: Long
) {
    constructor(ignoredUnsuitableDAO: IgnoredUnsuitableDAO) : this(
        ignoredUnsuitableDAO.id,
        ignoredUnsuitableDAO.slots.map { e -> e.id }.toSet(),
        ignoredUnsuitableDAO.user.id
    )
}

data class AddOrDeleteUnavailabilityDTO(
    val slotID: Long,
    val userID: Long
)

data class AddOrDeleteGroupOfUnavailabilitiesDTO(
    val slotsID: Set<Long>,
    val userID: Long
)

data class IgnoreOrNotIgnoreUnsuitabilityDTO(
    val slotID: Long,
    val userID: Long
)

data class PresentationUnavailabilityDTO(
    val id: Long,
    val unsuitable: Boolean,

    val slotsID: Set<Long>,
    val presentationID: Long,
    val userID: Long
) {
    constructor(presentationUnavailabilityDAO: PresentationUnavailabilityDAO) : this(
        presentationUnavailabilityDAO.id,
        presentationUnavailabilityDAO.unsuitable,
        presentationUnavailabilityDAO.slots.map { e -> e.id }.toSet(),
        presentationUnavailabilityDAO.presentation.id,
        presentationUnavailabilityDAO.user.id
    )
}

data class AdditionalPresentationUnavailabilityDTO(
    val id: Long,
    val unsuitable: Boolean,

    val slotsID: Set<Long>,
    val presentationID: Long,
    val userID: Long
) {
    constructor(additionalPresentationUnavailabilityDAO: AdditionalPresentationUnavailabilityDAO) : this(
        additionalPresentationUnavailabilityDAO.id,
        additionalPresentationUnavailabilityDAO.unsuitable,
        additionalPresentationUnavailabilityDAO.slots.map { e -> e.id }.toSet(),
        additionalPresentationUnavailabilityDAO.presentation.id,
        additionalPresentationUnavailabilityDAO.user.id
    )
}

data class IgnoredPresentationUnsuitableDTO(
    val id: Long,

    val slotsID: Set<Long>,
    val presentationID: Long,
    val userID: Long
) {
    constructor(ignoredPresentationUnsuitableDAO: IgnoredPresentationUnsuitableDAO) : this(
        ignoredPresentationUnsuitableDAO.id,
        ignoredPresentationUnsuitableDAO.slots.map { e -> e.id }.toSet(),
        ignoredPresentationUnsuitableDAO.presentation.id,
        ignoredPresentationUnsuitableDAO.user.id
    )
}

data class AddOrDeletePresentationUnavailabilityDTO(
    val slotID: Long,
    val presentationID: Long
)

data class AddOrDeleteGroupOfPresentationUnavailabilitiesDTO(
    val slotsID: Set<Long>,
    val presentationID: Long
)

data class IgnoreOrNotIgnorePresentationUnsuitabilityDTO(
    val slotID: Long,
    val presentationID: Long
)

data class RoomUnavailabilityDTO(
    val id: Long,
    val unsuitable: Boolean,

    val slotsID: Set<Long>,
    val roomID: Long
) {
    constructor(roomUnavailabilityDAO: RoomUnavailabilityDAO) : this(
        roomUnavailabilityDAO.id,
        roomUnavailabilityDAO.unsuitable,
        roomUnavailabilityDAO.slots.map { e -> e.id }.toSet(),
        roomUnavailabilityDAO.room.id
    )
}

data class AddOrDeleteRoomUnavailabilityDTO(
    val slotID: Long,
    val roomID: Long
)

data class AddOrDeleteGroupOfRoomUnavailabilitiesDTO(
    val slotsID: Set<Long>,
    val roomID: Long
)

data class PresentationDTO(
    val id: Long,

    val studentNumber: String,
    val studentName: String,
    val thesisTitle: String,
    val roomId: Long?,
    val roomNumber: String?,
    val roomBuilding: String?,
    val startingHour: LocalTime?,
    val date: LocalDate?,
    val adviserName: String,
    val adviserId: Long,
    val arguerName: String,
    val arguerId: Long,
    val optionalParticipant1Id: Long?,
    val optionalParticipant1Name: String?,
    val optionalParticipant2Id: Long?,
    val optionalParticipant2Name: String?
) {
    constructor(presentationDAO: PresentationDAO) : this(
        presentationDAO.id,
        presentationDAO.studentNumber,
        presentationDAO.studentName,
        presentationDAO.thesisTitle,
        presentationDAO.room?.id,
        presentationDAO.room?.roomNr,
        presentationDAO.room?.building,
        presentationDAO.slot?.startingHour,
        presentationDAO.slot?.date,
        presentationDAO.adviser.name,
        presentationDAO.adviser.id,
        presentationDAO.arguer.name,
        presentationDAO.arguer.id,
        presentationDAO.optionalParticipant1?.id,
        presentationDAO.optionalParticipant1?.name,
        presentationDAO.optionalParticipant2?.id,
        presentationDAO.optionalParticipant2?.name
    )
}

data class FollowedPresentationsDTO(
    val id: Long,

    val userID: Long,
    val presentationsIDS: Collection<Long>
) {
    constructor(followedPresentationsDAO: FollowedPresentationsDAO) : this(
        followedPresentationsDAO.id,
        followedPresentationsDAO.user.id,
        followedPresentationsDAO.presentations.map { e -> e.id }
    )
}

data class UserUnavailabilitiesDTO (
    val first: UnavailabilityDTO,
    val second: UnavailabilityDTO
)

data class PresentationUnavailabilitiesDTO (
    val first: PresentationUnavailabilityDTO,
    val second: PresentationUnavailabilityDTO
)

data class RoomUnavailabilitiesDTO (
    val first: RoomUnavailabilityDTO,
    val second: RoomUnavailabilityDTO
)

data class AdditionalUserUnavailabilitiesDTO (
    val first: AdditionalUnavailabilityDTO,
    val second: AdditionalUnavailabilityDTO
)

data class AdditionalPresentationUnavailabilitiesDTO (
    val first: AdditionalPresentationUnavailabilityDTO,
    val second: AdditionalPresentationUnavailabilityDTO
)

data class ChangePasswordDTO(
    val oldPassword: String,
    val newPassword: String
)

data class DeleteSelfDTO(
    val uid: Long,
)

data class ChangeRoleDTO(
    val userID: Long,
    val newRole: UserRoles
)

data class AddOptionalDTO(
    val name: String
)

data class LoginDTO(
    val email: String,
    var password: String
)

data class LoginGoogleDTO(
    val verified: Boolean,
    val email: String,
    val name: String,
    val identity: String
)

data class AuthResponseDTO(
    val token: String
)

data class ForgotPasswordDTO(
    val email: String
)

data class ComputationDTO(
    var inProgress: Boolean
)

data class ChangeAdditionalUnavailabilitiesDTO(
    val userAdditionalUnavailabilities: Map<Long, Collection<Long>>,
    val userAdditionalUnsuitableSlots: Map<Long, Collection<Long>>,
    val presentationAdditionalUnavailabilities: Map<Long, Collection<Long>>,
    val presentationAdditionalUnsuitableSlots: Map<Long, Collection<Long>>,
)

data class ChangeAdditionalUnavailabilitiesResponseDTO(
    val userAdditionalUnavailabilities: Map<Long, Collection<Long>>,
    val userAdditionalUnsuitableSlots: Map<Long, Collection<Long>>,
    val presentationAdditionalUnavailabilities: Map<Long, Collection<Long>>,
    val presentationAdditionalUnsuitableSlots: Map<Long, Collection<Long>>
)

/** PYTHON DTOs **/

data class PythonDataDTO(
    val presentationJury: Collection<PresentationJuryDTO>,
    val followedSlots :Collection<Collection<SlotIDDTO>>,
    val rooms: Collection<RoomIDDTO>,
    val usersUnavailabilities: Collection<ReducedUnavailabilityDTO>,
    val usersUnsuitableSlots: Collection<ReducedUnavailabilityDTO>,
    val presentationsUnavailabilities: Collection<ReducedPresentationUnavailabilityDTO>,
    val presentationsUnsuitableSlots: Collection<ReducedPresentationUnavailabilityDTO>,
    val roomsUnavailabilities: Collection<ReducedRoomUnavailabilityDTO>,
    val roomsUnsuitableSlots: Collection<ReducedRoomUnavailabilityDTO>,
    val additionalUsersUnavailabilities: Collection<ReducedUnavailabilityDTO>,
    val additionalUsersUnsuitableSlots: Collection<ReducedUnavailabilityDTO>,
    val additionalPresentationsUnavailabilities: Collection<ReducedPresentationUnavailabilityDTO>,
    val additionalPresentationsUnsuitableSlots: Collection<ReducedPresentationUnavailabilityDTO>,
    val optionals: Collection<OptionalViewersDTO>,
    val followedPresentations: Collection<ReducedFollowedPresentationsDTO>
)

data class RestrictionTestingDTO(
    val presentations: Collection<ReducedPresentationDTO>,
    val usersUnavailabilities: Collection<ReducedUnavailabilityDTO>,
    val usersUnsuitableSlots: Collection<ReducedUnavailabilityDTO>,
    val presentationsUnavailabilities: Collection<ReducedPresentationUnavailabilityDTO>,
    val presentationsUnsuitableSlots: Collection<ReducedPresentationUnavailabilityDTO>,
    val roomsUnavailabilities: Collection<ReducedRoomUnavailabilityDTO>,
    val roomsUnsuitableSlots: Collection<ReducedRoomUnavailabilityDTO>,
    val additionalUsersUnavailabilities: Collection<ReducedUnavailabilityDTO>,
    val additionalUsersUnsuitableSlots: Collection<ReducedUnavailabilityDTO>,
    val additionalPresentationsUnavailabilities: Collection<ReducedPresentationUnavailabilityDTO>,
    val additionalPresentationsUnsuitableSlots: Collection<ReducedPresentationUnavailabilityDTO>,
    val optionals: Collection<OptionalViewersTestingDTO>
)

data class TestRestrictionDTO(
    val noConflicts: Boolean,
    val usersUnavailabilitiesList: Collection<Pair<Long, Long>>,
    val usersUnsuitableSlotsList: Collection<Pair<Long, Long>>,
    val presentationsUnavailabilitiesList: Collection<Pair<Long, Long>>,
    val presentationsUnsuitableSlotsList: Collection<Pair<Long, Long>>,
    val roomsUnavailabilitiesList: Collection<Pair<Long, Long>>,
    val roomsUnsuitableSlotsList: Collection<Pair<Long, Long>>,
    val additionalUsersUnavailabilitiesList: Collection<Pair<Long, Long>>,
    val additionalUsersUnsuitableSlotsList: Collection<Pair<Long, Long>>,
    val additionalPresentationsUnavailabilitiesList: Collection<Pair<Long, Long>>,
    val additionalPresentationsUnsuitableSlotsList: Collection<Pair<Long, Long>>,
)

data class PresentationJuryDTO(
    val presentationID: Long,
    val adviserID: Long,
    val arguerID: Long
)

data class SlotIDDTO(
    val slotID: Long
)

data class RoomIDDTO(
    val roomID: Long
)

data class ReducedUnavailabilityDTO(
    val userID: Long,
    val slotsID: Set<Long>
)

data class ReducedFollowedPresentationsDTO(
    val followedPresentationsIDS: Collection<Long>
)

data class ReducedPresentationUnavailabilityDTO(
    val presentationID: Long,
    val slotsID: Set<Long>
)

data class ReducedRoomUnavailabilityDTO(
    val roomID: Long,
    val slotsID: Set<Long>
)

data class OptionalViewersDTO(
    val userID: Long,
    val presentationID: Long
)

data class OptionalViewersTestingDTO(
    val userID: Long,
    val slotID: Long
)

data class ReducedPresentationDTO(
    val presentationID: Long,
    val roomID: Long,
    val slotID: Long,
    val adviserID: Long,
    val arguerID: Long,
)

/** MAIL DTOs **/

data class AssignmentSuccessDTO(
    val presentationID: Long,
    val presentationName: String,
    val slotDate: LocalDate,
    val slotTime: LocalTime,
    val roomBuilding: String,
    val roomNr: String,
)

data class RestrictionFlagDTO(
    val itemID: Long,
    val itemName: String,
    val slotDate: LocalDate,
    val slotTime: LocalTime,
)