package pt.unl.fct.di.apresentacoes.api.python

import org.springframework.web.bind.annotation.RestController

import com.google.gson.Gson
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.gson.reflect.TypeToken
import jakarta.servlet.http.HttpServletRequest
import pt.unl.fct.di.apresentacoes.api.dto.*
import pt.unl.fct.di.apresentacoes.api.exceptions.APIConflictException
import pt.unl.fct.di.apresentacoes.api.exceptions.APINotFoundException
import pt.unl.fct.di.apresentacoes.api.exceptions.APIUnprocessableEntityException
import pt.unl.fct.di.apresentacoes.api.exceptions.ComputationInProgress
import pt.unl.fct.di.apresentacoes.api.exceptions.PresentationUnprocessable
import pt.unl.fct.di.apresentacoes.domain.SlotDAO
import pt.unl.fct.di.apresentacoes.domain.IgnoredUnsuitableDAO
import pt.unl.fct.di.apresentacoes.domain.IgnoredPresentationUnsuitableDAO
import pt.unl.fct.di.apresentacoes.services.*
import pt.unl.fct.di.apresentacoes.services.mail.MailService
import pt.unl.fct.di.apresentacoes.services.security.TokenService
import pt.unl.fct.di.apresentacoes.services.ComputationService
import java.time.Duration

private const val COMPUTATION_IN_PROGRESS = "There is a computation in progress."

private const val DEFAULT_TIME_DIFF_TO_FOLLOWED_SLOTS: Long = 60 // 1 hour
private const val PYTHON_EXECUTE_CLINGO_URL: String = "http://python-backend:5000/executeClingo"
private const val PYTHON_TEST_RESTRICTIONS_URL: String = "http://python-backend:5000/testRestrictions"
private const val DEFAULT_ROOM_ID_POSITION = 0
private const val DEFAULT_SLOT_ID_POSITION = 1
private const val DEFAULT_PRESENTATION_ID_POSITION = 2
private const val DEFAULT_PYTHON_VALUE_FOR_ITERATIONS = 600000

// Response identifiers
private const val NO_VALID_SOLUTION_RESPONSE = "Impossible"
private const val OPTIONAL_FLAG_RESPONSE = "OptionalFlag"
private const val SUCCESS_RESPONSE = "Success"

private const val CONFLICT_DETECTED_RESPONSE = "Conflict"
private const val NO_CONFLICT_RESPONSE = "Success"

// Flag identifiers
private const val PARTICIPANT_FLAG = "participantFlag"
private const val PARTICIPANT_FLAG_OPTIONAL = "participantFlagOptional"
private const val ADDITIONAL_PARTICIPANT_FLAG = "additionalParticipantFlag"
private const val ADDITIONAL_PARTICIPANT_FLAG_OPTIONAL = "additionalParticipantFlagOptional"
private const val ASSIGN_FLAG = "assignFlag"
private const val ASSIGN_FLAG_OPTIONAL = "assignFlagOptional"
private const val ADDITIONAL_ASSIGN_FLAG = "additionalAssignFlag"
private const val ADDITIONAL_ASSIGN_FLAG_OPTIONAL = "additionalAssignFlagOptional"
private const val LOCATION_FLAG = "locationFlag"
private const val LOCATION_FLAG_OPTIONAL = "locationFlagOptional"

data class UnavailabilityCollectionDTO(
    val first: Collection<ReducedUnavailabilityDTO>,
    val second: Collection<ReducedUnavailabilityDTO>,
    val third: Collection<ReducedUnavailabilityDTO>,
    val fourth: Collection<ReducedUnavailabilityDTO>
)

data class PresentationUnavailabilityCollectionDTO(
    val first: Collection<ReducedPresentationUnavailabilityDTO>,
    val second: Collection<ReducedPresentationUnavailabilityDTO>,
    val third: Collection<ReducedPresentationUnavailabilityDTO>,
    val fourth: Collection<ReducedPresentationUnavailabilityDTO>
)

data class RoomUnavailabilityCollectionDTO(
    val first: Collection<ReducedRoomUnavailabilityDTO>,
    val second: Collection<ReducedRoomUnavailabilityDTO>,
)

@RestController
class PythonController(val request: HttpServletRequest,
                       val computation: ComputationService,
                       val usersService: UserService,
                       val presentationsService: PresentationService,
                       val slotsService: SlotService,
                       val roomsService: RoomService,
                       val unavailabilitiesService: UnavailabilityService,
                       val presentationUnavailabilitiesService: PresentationUnavailabilityService,
                       val roomUnavailabilitiesService: RoomUnavailabilityService,
                       val followedPresentationsService: FollowedPresentationsService,
                       val mailService: MailService,
                       val tokenService: TokenService) : PythonAPI {
    
    override fun addInitialComputation() {
        try{
            computation.addInitialComputation()
        } catch (e:ComputationNotFound){
            throw APINotFoundException(e.message)
        }
    }

    // Get computation status
    override fun checkComputation() : Boolean {
        try{
            return computation.getComputationStatus()
        } catch (e:ComputationNotFound){
            throw APINotFoundException(e.message)
        }
    }

    // Change computation status
    override fun changeState(newState: ComputationDTO) : Boolean {
        try{
            println("(PythonController changeState) newState: " + newState)
            val comp = computation.changeComputationStatus(newState.inProgress)
            return comp.inProgress
        } catch (e:ComputationNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun executeClingo() : String {
        // If computation in progress, return early
        privateCheckComputation()
        println("Start \"executeClingo\"")
        val jury = getJury()
        val followedSlots = getFollowedSlots()
        val rooms = getRooms()
        val res1 = getUnavailabilities()
        val unavailabilities = res1.first
        val unsuitableSlots = res1.second
        val additionalUnavailabilities = res1.third
        val additionalUnsuitableSlots = res1.fourth
        val res2 = getPresentationsUnavailabilities()
        val presentationUnavailabilities = res2.first
        val presentationUnsuitableSlots = res2.second
        val additionalPresentationUnavailabilities = res2.third
        val additionalPresentationUnsuitableSlots = res2.fourth
        val res3 = getRoomsUnavailabilities()
        val roomUnavailabilities = res3.first
        val roomUnsuitableSlots = res3.second
        val optionals = getOptionals()
        val followedPresentations = getFollowedPresentations()
        println("Data extracted")


        val numberSlots = followedSlots.sumOf { it.size }

        if(numberSlots == 0 || rooms.isEmpty())
            throw APIUnprocessableEntityException("Please insert rooms and/or slots.")

        if(jury.size > rooms.size * numberSlots )
            throw APIUnprocessableEntityException("The number of presentations exceeds the available slots and rooms. Please add more slots or more rooms.")

        var started = true
        val timeoutRead = DEFAULT_PYTHON_VALUE_FOR_ITERATIONS + DEFAULT_PYTHON_VALUE_FOR_ITERATIONS*followedSlots.size

        val pythonData = PythonDataDTO(jury, followedSlots, rooms, unavailabilities, unsuitableSlots,
            presentationUnavailabilities, presentationUnsuitableSlots,
            roomUnavailabilities, roomUnsuitableSlots,
            additionalUnavailabilities, additionalUnsuitableSlots,
            additionalPresentationUnavailabilities, additionalPresentationUnsuitableSlots,
            optionals, followedPresentations)
        val gson = Gson()
        val jsonData = gson.toJson(pythonData)
        println(jsonData)

        //Using val request: HttpServletRequest to extract token
        val authHeader: String = request.getHeader("Authorization")

        val jwtToken = tokenService.extractTokenValue(authHeader)

        val email = tokenService.extractEmail(jwtToken)

        //TODO Wait for response
        // Fuel.post(PYTHON_EXECUTE_CLINGO_URL)
        val requestAsync = Fuel.post(PYTHON_EXECUTE_CLINGO_URL)
            .header("Content-Type" to "application/json")
            .body(jsonData)
            .timeoutRead(timeoutRead)
            .response { result ->
                val (bytes, error) = result
                //TODO Test this
                println("Changing state")
                computation.changeComputationStatus(false)
                if (bytes != null) {
                    val responseString = String(bytes)
                    println("[Response]: $responseString")

                    val gson = Gson()
                    // Exclude initial element (String)
                    val finalSlotsObject: List<List<String>> = gson.fromJson(responseString, object : TypeToken<List<List<String>>>() {}.type)
                    println("finalSlotsObject created")
                    println("finalSlotsObject: " + finalSlotsObject)
                    val finalSlotsList = finalSlotsObject.drop(1)
                    println("finalSlotsList created")
                    println("finalSlotsList: " + finalSlotsList)
                    val flatList = finalSlotsList.flatten()
                    println("flatList created")
                    println("flatList: " + flatList)

                    val identifier = finalSlotsObject[0][0]
                    println("identifier: " + identifier)
                    if (identifier == NO_VALID_SOLUTION_RESPONSE) {
                        println("Python Response: $NO_VALID_SOLUTION_RESPONSE")
                        val participantFlags: MutableList<Pair<Long, Long>> = mutableListOf()
                        val assignFlags: MutableList<Pair<Long, Long>> = mutableListOf()
                        val locationFlags: MutableList<Pair<Long, Long>> = mutableListOf()

                        val additionalParticipantFlags: MutableList<Pair<Long, Long>> = mutableListOf()
                        val additionalAssignFlags: MutableList<Pair<Long, Long>> = mutableListOf()

                        val participantFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
                        val assignFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
                        val locationFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()

                        val additionalParticipantFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
                        val additionalAssignFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()

                        for (item in flatList) {
                            val flagType = item.split('(')[0]
                            val values = item.substringAfter('(').substringBeforeLast(')').split(',')
                            val itemID = values[0].toLong()
                            val slotID = values[1].toLong()
                            when (flagType) {
                                PARTICIPANT_FLAG -> {
                                    val user = usersService.getOneUser(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val userId = user.id
                                    val userName = user.name
                                    participantFlags.add(Pair(itemID, slotID))
                                    participantFlagsResponse.add(RestrictionFlagDTO(userId, userName, slot.date, slot.startingHour))    
                                }
                                ASSIGN_FLAG -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    assignFlags.add(Pair(itemID, slotID))
                                    assignFlagsResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                }
                                LOCATION_FLAG -> {
                                    val room = roomsService.getOneRoom(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val roomId = room.id
                                    val roomNumber = room.roomNr
                                    val roomBuilding = room.building
                                    val roomName = formatRoomName(roomNumber, roomBuilding)
                                    locationFlags.add(Pair(itemID, slotID))
                                    locationFlagsResponse.add(RestrictionFlagDTO(roomId, roomName, slot.date, slot.startingHour))
                                }
                                ADDITIONAL_PARTICIPANT_FLAG -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    additionalParticipantFlags.add(Pair(itemID, slotID))
                                    additionalParticipantFlagsResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                }
                                ADDITIONAL_ASSIGN_FLAG -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    additionalAssignFlags.add(Pair(itemID, slotID))
                                    additionalAssignFlagsResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                }
                            }
                        }
                        mailService.sendNoSolutionMail(email!!, participantFlagsResponse, assignFlagsResponse, locationFlagsResponse,
                            additionalParticipantFlagsResponse, additionalAssignFlagsResponse)
                    } else if (identifier == OPTIONAL_FLAG_RESPONSE) {
                        println("Python Response: $OPTIONAL_FLAG_RESPONSE")
                        val participantFlags: MutableList<Pair<Long, Long>> = mutableListOf()
                        val participantFlagsOptional: MutableList<Pair<Long, Long>> = mutableListOf()
                        val assignFlags: MutableList<Pair<Long, Long>> = mutableListOf()
                        val assignFlagsOptional: MutableList<Pair<Long, Long>> = mutableListOf()
                        val locationFlags: MutableList<Pair<Long, Long>> = mutableListOf()
                        val locationFlagsOptional: MutableList<Pair<Long, Long>> = mutableListOf()
                        val additionalParticipantFlags: MutableList<Pair<Long, Long>> = mutableListOf()
                        val additionalParticipantFlagsOptional: MutableList<Pair<Long, Long>> = mutableListOf()
                        val additionalAssignFlags: MutableList<Pair<Long, Long>> = mutableListOf()
                        val additionalAssignFlagsOptional: MutableList<Pair<Long, Long>> = mutableListOf()
                        
                        val participantFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
                        val participantFlagsOptionalResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
                        val assignFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
                        val assignFlagsOptionalResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
                        val locationFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
                        val locationFlagsOptionalResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
                        val additionalParticipantFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
                        val additionalParticipantFlagsOptionalResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
                        val additionalAssignFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
                        val additionalAssignFlagsOptionalResponse: MutableList<RestrictionFlagDTO> = mutableListOf()

                        for (item in flatList) {
                            val flagType = item.split('(')[0]
                            val values = item.substringAfter('(').substringBeforeLast(')').split(',')
                            val itemID = values[0].toLong()
                            val slotID = values[1].toLong()

                            
                            when (flagType) {
                                PARTICIPANT_FLAG -> {
                                    val user = usersService.getOneUser(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val userId = user.id
                                    val userName = user.name
                                    participantFlags.add(Pair(itemID, slotID))
                                    participantFlagsResponse.add(RestrictionFlagDTO(userId, userName, slot.date, slot.startingHour))  
                                }
                                PARTICIPANT_FLAG_OPTIONAL -> {
                                    val user = usersService.getOneUser(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val userId = user.id
                                    val userName = user.name
                                    participantFlagsOptional.add(Pair(itemID, slotID))
                                    participantFlagsOptionalResponse.add(RestrictionFlagDTO(userId, userName, slot.date, slot.startingHour))
                                }
                                ASSIGN_FLAG -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    assignFlags.add(Pair(itemID, slotID))
                                    assignFlagsResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                }
                                ASSIGN_FLAG_OPTIONAL -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    assignFlagsOptional.add(Pair(itemID, slotID))
                                    assignFlagsOptionalResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                } 
                                LOCATION_FLAG -> {
                                    val room = roomsService.getOneRoom(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val roomId = room.id
                                    val roomNumber = room.roomNr
                                    val roomBuilding = room.building
                                    val roomName = formatRoomName(roomNumber, roomBuilding)
                                    locationFlags.add(Pair(itemID, slotID))
                                    locationFlagsResponse.add(RestrictionFlagDTO(roomId, roomName, slot.date, slot.startingHour))
                                }
                                LOCATION_FLAG_OPTIONAL -> {
                                    val room = roomsService.getOneRoom(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val roomId = room.id
                                    val roomNumber = room.roomNr
                                    val roomBuilding = room.building
                                    val roomName = formatRoomName(roomNumber, roomBuilding)
                                    locationFlagsOptional.add(Pair(itemID, slotID))
                                    locationFlagsOptionalResponse.add(RestrictionFlagDTO(roomId, roomName, slot.date, slot.startingHour))
                                } 
                                ADDITIONAL_PARTICIPANT_FLAG -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    additionalParticipantFlags.add(Pair(itemID, slotID))
                                    additionalParticipantFlagsResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                }
                                ADDITIONAL_PARTICIPANT_FLAG_OPTIONAL -> {
                                    val user = usersService.getOneUser(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val userId = user.id
                                    val userName = user.name
                                    additionalParticipantFlagsOptional.add(Pair(itemID, slotID))
                                    additionalParticipantFlagsOptionalResponse.add(RestrictionFlagDTO(userId, userName, slot.date, slot.startingHour))
                                }
                                ADDITIONAL_ASSIGN_FLAG -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    additionalAssignFlags.add(Pair(itemID, slotID))
                                    additionalAssignFlagsResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                }
                                ADDITIONAL_ASSIGN_FLAG_OPTIONAL -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    additionalAssignFlagsOptional.add(Pair(itemID, slotID))
                                    additionalAssignFlagsOptionalResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                } 
                            }
                        }
                        mailService.sendOptionalFlagMail(email!!, participantFlagsOptionalResponse, assignFlagsOptionalResponse, locationFlagsOptionalResponse,
                            additionalParticipantFlagsOptionalResponse, additionalAssignFlagsOptionalResponse)
                    } else if (identifier == SUCCESS_RESPONSE) {
                        println("Python Response: $SUCCESS_RESPONSE")
                        val finalSlots: MutableList<Triple<Long, Long, Long>> = mutableListOf()

                        val finalSlotsResponse: MutableList<AssignmentSuccessDTO> = mutableListOf()

                        for (item in flatList) {
                            val values = item.substringAfter('(').substringBeforeLast(')').split(',')
                            if(values.all { it.isBlank() }) {
                                continue
                            }
                            val roomID = values[DEFAULT_ROOM_ID_POSITION].toLong()
                            val slotID = values[DEFAULT_SLOT_ID_POSITION].toLong()
                            val presentationID = values[DEFAULT_PRESENTATION_ID_POSITION].toLong()
                            
                            try {
                                val presentation = presentationsService.getOnePresentation(presentationID)
                                val slot = slotsService.getOneSlot(slotID)
                                val room = roomsService.getOneRoom(roomID)
                                val presentationId = presentation.id
                                val presentationName = presentation.thesisTitle
                                val roomBuilding = room.building
                                val roomNr = room.roomNr
                                presentationsService.addRoomAndSlot(presentationID, roomID, slotID)
                                finalSlots.add(Triple(roomID, slotID, presentationID))
                                finalSlotsResponse.add(AssignmentSuccessDTO(presentationId, presentationName, slot.date, slot.startingHour, roomBuilding, roomNr))
                            } catch (e:PresentationNotFound){
                                throw APINotFoundException(e.message)
                            } catch (e:PresentationUnprocessable){
                                throw APIUnprocessableEntityException(e.message)
                            }
                        }
                        mailService.sendComputedAssignmentMail(email!!, finalSlotsResponse)
                    }
                } else {
                    if (error != null) {
                        println("[Error]: ${error.exception.message}")
                        started = false
                        throw APIUnprocessableEntityException("There was an error on the server side, please try again later.")
                    } else {
                        println("[Error]: Unknown error occurred")
                        started = false
                        throw APIUnprocessableEntityException("There was an error on the server side, please try again later.")
                    }
                }
            }
        
        if (started)
            return "Python started"
        else
            return "Not started"
    }

    override fun testRestrictions() : TestRestrictionDTO {
        println("Start \"testRestrictions\"")
        val presentations = getAssignedPresentationsForTesting()
        val res1 = getUnavailabilitiesForTesting()
        val unavailabilities = res1.first
        val unsuitableSlots = res1.second
        val additionalUnavailabilities = res1.third
        val additionalUnsuitableSlots = res1.fourth
        val res2 = getPresentationsUnavailabilitiesForTesting()
        val presentationUnavailabilities = res2.first
        val presentationUnsuitableSlots = res2.second
        val additionalPresentationUnavailabilities = res2.third
        val additionalPresentationUnsuitableSlots = res2.fourth
        val res3 = getRoomsUnavailabilities()
        val roomUnavailabilities = res3.first
        val roomUnsuitableSlots = res3.second
        val optionals = getOptionalsForTesting()
        println("Test Data extracted")

        if(presentations.isEmpty())
            throw APIUnprocessableEntityException("There is currently no assignment. Please compute an assignment first.")

        var success = false

        // one value for slots assignment and another one for each roomAssignment in each set of followed slots
        val timeoutRead = DEFAULT_PYTHON_VALUE_FOR_ITERATIONS

        val pythonData = RestrictionTestingDTO(presentations, unavailabilities, unsuitableSlots,
            presentationUnavailabilities, presentationUnsuitableSlots,
            roomUnavailabilities, roomUnsuitableSlots,
            additionalUnavailabilities, additionalUnsuitableSlots,
            additionalPresentationUnavailabilities, additionalPresentationUnsuitableSlots, optionals)
        val gson = Gson()
        val jsonData = gson.toJson(pythonData)
        //println(pythonData)
        println(jsonData)

        val usersUnavailabilitiesList: MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
        val usersUnsuitableSlotsList: MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
        val presentationsUnavailabilitiesList: MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
        val presentationsUnsuitableSlotsList: MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
        val roomsUnavailabilitiesList: MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
        val roomsUnsuitableSlotsList: MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
        val additionalUsersUnavailabilitiesList: MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
        val additionalUsersUnsuitableSlotsList: MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
        val additionalPresentationsUnavailabilitiesList: MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
        val additionalPresentationsUnsuitableSlotsList: MutableList<Pair<Long, Long>> = mutableListOf<Pair<Long, Long>>()
        
        val participantFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
        val participantFlagsOptionalResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
        val assignFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
        val assignFlagsOptionalResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
        val locationFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
        val locationFlagsOptionalResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
        val additionalParticipantFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
        val additionalParticipantFlagsOptionalResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
        val additionalAssignFlagsResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
        val additionalAssignFlagsOptionalResponse: MutableList<RestrictionFlagDTO> = mutableListOf()
        
        val authHeader: String = request.getHeader("Authorization")

        val jwtToken = tokenService.extractTokenValue(authHeader)

        val email = tokenService.extractEmail(jwtToken)

        val requestAsync = Fuel.post(PYTHON_TEST_RESTRICTIONS_URL)
            .header("Content-Type" to "application/json")
            .body(jsonData)
            .timeoutRead(timeoutRead)
            .response { result ->
                val (bytes, error) = result

                println("Checking restrictions")
                if (bytes != null) {
                    val responseString = String(bytes)

                    val gson = Gson()
                    val flagsObject: List<List<String>> = gson.fromJson(responseString, object : TypeToken<List<List<String>>>() {}.type)
                    val flagsList = flagsObject.drop(1)
                    val flatList = flagsList.flatten()

                    val identifier = flagsObject[0][0]
                    println("identifier: " + identifier)
                    if (identifier == CONFLICT_DETECTED_RESPONSE) {
                        println("Python Response: $CONFLICT_DETECTED_RESPONSE")
                        for (item in flatList) {
                            val flagType = item.split('(')[0]
                            val values = item.substringAfter('(').substringBeforeLast(')').split(',')
                            val itemID = values[0].toLong()
                            val slotID = values[1].toLong()

                            when (flagType) {
                                PARTICIPANT_FLAG -> {
                                    val user = usersService.getOneUser(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val userId = user.id
                                    val userName = user.name
                                    usersUnavailabilitiesList.add(Pair(itemID, slotID))
                                    participantFlagsResponse.add(RestrictionFlagDTO(userId, userName, slot.date, slot.startingHour))
                                }
                                PARTICIPANT_FLAG_OPTIONAL -> {
                                    val user = usersService.getOneUser(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val userId = user.id
                                    val userName = user.name
                                    usersUnsuitableSlotsList.add(Pair(itemID, slotID))
                                    participantFlagsOptionalResponse.add(RestrictionFlagDTO(userId, userName, slot.date, slot.startingHour))
                                }
                                ASSIGN_FLAG -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    presentationsUnavailabilitiesList.add(Pair(itemID, slotID))
                                    assignFlagsResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                }
                                ASSIGN_FLAG_OPTIONAL -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    presentationsUnsuitableSlotsList.add(Pair(itemID, slotID))
                                    assignFlagsOptionalResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                } 
                                LOCATION_FLAG -> {
                                    val room = roomsService.getOneRoom(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val roomId = room.id
                                    val roomNumber = room.roomNr
                                    val roomBuilding = room.building
                                    val roomName = formatRoomName(roomNumber, roomBuilding)
                                    roomsUnavailabilitiesList.add(Pair(itemID, slotID))
                                    locationFlagsResponse.add(RestrictionFlagDTO(roomId, roomName, slot.date, slot.startingHour))
                                }
                                LOCATION_FLAG_OPTIONAL -> {
                                    val room = roomsService.getOneRoom(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val roomId = room.id
                                    val roomNumber = room.roomNr
                                    val roomBuilding = room.building
                                    val roomName = formatRoomName(roomNumber, roomBuilding)
                                    roomsUnsuitableSlotsList.add(Pair(itemID, slotID))
                                    locationFlagsOptionalResponse.add(RestrictionFlagDTO(roomId, roomName, slot.date, slot.startingHour))
                                } 
                                ADDITIONAL_PARTICIPANT_FLAG -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    additionalUsersUnavailabilitiesList.add(Pair(itemID, slotID))
                                    additionalParticipantFlagsResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                }
                                ADDITIONAL_PARTICIPANT_FLAG_OPTIONAL -> {
                                    val user = usersService.getOneUser(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val userId = user.id
                                    val userName = user.name
                                    additionalUsersUnsuitableSlotsList.add(Pair(itemID, slotID))
                                    additionalParticipantFlagsOptionalResponse.add(RestrictionFlagDTO(userId, userName, slot.date, slot.startingHour))
                                }
                                ADDITIONAL_ASSIGN_FLAG -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    additionalPresentationsUnavailabilitiesList.add(Pair(itemID, slotID))
                                    additionalAssignFlagsResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                }
                                ADDITIONAL_ASSIGN_FLAG_OPTIONAL -> {
                                    val presentation = presentationsService.getOnePresentation(itemID)
                                    val slot = slotsService.getOneSlot(slotID)
                                    val presentationId = presentation.id
                                    val presentationName = presentation.thesisTitle
                                    additionalPresentationsUnsuitableSlotsList.add(Pair(itemID, slotID))
                                    additionalAssignFlagsOptionalResponse.add(RestrictionFlagDTO(presentationId, presentationName, slot.date, slot.startingHour))
                                } 
                            }
                        }
                        mailService.sendTestRestrictionsFoundMail(email!!, participantFlagsResponse, assignFlagsResponse,
                            locationFlagsResponse, additionalParticipantFlagsResponse, additionalAssignFlagsResponse,
                            participantFlagsOptionalResponse, assignFlagsOptionalResponse, locationFlagsOptionalResponse, additionalParticipantFlagsOptionalResponse,
                            additionalAssignFlagsOptionalResponse)
                    } else if (identifier == NO_CONFLICT_RESPONSE) {
                        println("Python Response: $NO_CONFLICT_RESPONSE")
                        success = true
                        mailService.sendTestSuccessMail(email!!)
                    }
                } else {
                    if (error != null) {
                        println("[Error]: ${error.exception.message}")
                        success = false
                        throw APIUnprocessableEntityException("There was an error on the server side, please try again later.")
                    } else {
                        println("[Error]: Unknown error occurred")
                        success = false
                        throw APIUnprocessableEntityException("There was an error on the server side, please try again later.")
                    }
                }
            }

        requestAsync.join()

        println("After Fuel (testing sync)")
        println("After Fuel (testing not sync)")

        println("usersUnavailabilitiesList: " + usersUnavailabilitiesList)
        println("usersUnsuitableSlotsList: " + usersUnsuitableSlotsList)
        println("presentationsUnavailabilitiesList: " + presentationsUnavailabilitiesList)
        println("presentationsUnsuitableSlotsList: " + presentationsUnsuitableSlotsList)
        println("roomsUnavailabilitiesList: " + roomsUnavailabilitiesList)
        println("roomsUnsuitableSlotsList: " + roomsUnsuitableSlotsList)
        println("additionalUsersUnavailabilitiesList: " + additionalUsersUnavailabilitiesList)
        println("additionalUsersUnsuitableSlotsList: " + additionalUsersUnsuitableSlotsList)
        println("additionalPresentationsUnavailabilitiesList: " + additionalPresentationsUnavailabilitiesList)
        println("additionalPresentationsUnsuitableSlotsList: " + additionalPresentationsUnsuitableSlotsList)
        
        if (success) {
            println("Python: Success")
            // return "No conflict"
            return TestRestrictionDTO(
                true,
                // listOf<String>()
                listOf<Pair<Long, Long>>(),
                listOf<Pair<Long, Long>>(),
                listOf<Pair<Long, Long>>(),
                listOf<Pair<Long, Long>>(),
                listOf<Pair<Long, Long>>(),
                listOf<Pair<Long, Long>>(),
                listOf<Pair<Long, Long>>(),
                listOf<Pair<Long, Long>>(),
                listOf<Pair<Long, Long>>(),
                listOf<Pair<Long, Long>>()
            )
        }
        else {
            println("Python: Conflicts")
            // return "Conflicts found"
            return TestRestrictionDTO(
                false,
                // flagList
                usersUnavailabilitiesList,
                usersUnsuitableSlotsList,
                presentationsUnavailabilitiesList,
                presentationsUnsuitableSlotsList,
                roomsUnavailabilitiesList,
                roomsUnsuitableSlotsList,
                additionalUsersUnavailabilitiesList,
                additionalUsersUnsuitableSlotsList,
                additionalPresentationsUnavailabilitiesList,
                additionalPresentationsUnsuitableSlotsList
            )
        }
    }

    private fun getJury(): Collection<PresentationJuryDTO> {
        val allPresentations = presentationsService.getAllPresentations().iterator()
        val res = mutableListOf<PresentationJuryDTO>()

        while (allPresentations.hasNext()){
            val onePresentation = allPresentations.next()
            res.add(PresentationJuryDTO(
                onePresentation.id,
                onePresentation.adviser.id,
                onePresentation.arguer.id))
        }

        return res
    }

    private fun getFollowedSlots(): Collection<Collection<SlotIDDTO>> {
        val allSlots = slotsService.getAllSlots().toList().listIterator() //toList so the iterator can be bidirectional
        val followedSlots = mutableListOf<MutableList<SlotIDDTO>>() //list of lists so there is a list of theh slots that are right after each other

        while (allSlots.hasNext()){
            val setOfFollowedSlots = mutableListOf<SlotIDDTO>()

            var previousSlot = allSlots.next()

            //add first slot to the list
            setOfFollowedSlots.add(SlotIDDTO(previousSlot.id))

            while(allSlots.hasNext()){
                val nextSlot = allSlots.next()

                val duration = Duration.between(nextSlot.startingHour, previousSlot.startingHour).abs()

                if(duration.toMinutes() > DEFAULT_TIME_DIFF_TO_FOLLOWED_SLOTS || previousSlot.date != nextSlot.date)
                    break

                previousSlot = nextSlot
                setOfFollowedSlots.add(SlotIDDTO(nextSlot.id))
            }
            if(allSlots.hasNext()) //just so there is no slot ignored, since in the loop before i already do the .next to verify the time and day
                allSlots.previous()

            followedSlots.add(setOfFollowedSlots)
        }
        return followedSlots
    }

    private fun getRooms(): Collection<RoomIDDTO> {
        val res = mutableListOf<RoomIDDTO>()

        val rooms = roomsService.getAllRooms().iterator()
        while(rooms.hasNext()){
            res.add(RoomIDDTO(rooms.next().id))
        }
        return res
    }
    
    private fun getUnavailabilities(): UnavailabilityCollectionDTO {
        val res1 = mutableListOf<ReducedUnavailabilityDTO>()
        val res2 = mutableListOf<ReducedUnavailabilityDTO>()
        val res3 = mutableListOf<ReducedUnavailabilityDTO>()
        val res4 = mutableListOf<ReducedUnavailabilityDTO>()

        val allUsersUnavailabilities = unavailabilitiesService.getAllAdditionalUnavailabilities()
        val allUsersIgnored : Iterable<IgnoredUnsuitableDAO> = unavailabilitiesService.getAllIgnoredUnsuitables()
        
        val ignoreMap : MutableMap<Long, MutableSet<Long>> = HashMap()
        val usersIgnoredUnsuitableIterator = allUsersIgnored.iterator()
        while(usersIgnoredUnsuitableIterator.hasNext()) {
            val userIgnoredUnsuitable = usersIgnoredUnsuitableIterator.next()
            val slotList : Collection<SlotDAO> = userIgnoredUnsuitable.slots
            val slotIdList : List<Long> = slotList.map { e -> e.id }
            ignoreMap.put(userIgnoredUnsuitable.user.id, HashSet(slotIdList))
        }

        val usersUnavailabilitiesIterator = allUsersUnavailabilities.first.iterator()
        val additionalUsersUnavailabilitiesIterator = allUsersUnavailabilities.second.iterator()
        while(usersUnavailabilitiesIterator.hasNext()){
            val unavailability = usersUnavailabilitiesIterator.next()
            val slotList : MutableCollection<SlotDAO> = unavailability.slots
            val slotIdList : List<Long> = slotList.map { e -> e.id }
            if (unavailability.unsuitable) {
                val notIgnored = slotIdList.filter { it -> !(ignoreMap.get(unavailability.user.id)!!.contains(it)) }.toSet()
                res2.add(ReducedUnavailabilityDTO(unavailability.user.id, notIgnored))
            } else {
                res1.add(ReducedUnavailabilityDTO(unavailability.user.id, slotIdList.toSet()))
            }
        }
        while(additionalUsersUnavailabilitiesIterator.hasNext()) {
            val additionalUnavailability = additionalUsersUnavailabilitiesIterator.next()
            val slotList : MutableCollection<SlotDAO> = additionalUnavailability.slots
            val slotIdList : List<Long> = slotList.map { e -> e.id }
            if (additionalUnavailability.unsuitable) {
                val notIgnored = slotIdList.filter { it -> !(ignoreMap.get(additionalUnavailability.user.id)!!.contains(it)) }.toSet()
                res4.add(ReducedUnavailabilityDTO(additionalUnavailability.user.id, notIgnored))
            } else {
                res3.add(ReducedUnavailabilityDTO(additionalUnavailability.user.id, slotIdList.toSet()))
            }
        }
        return UnavailabilityCollectionDTO(res1, res2, res3, res4)
    }

    private fun getUnavailabilitiesForTesting(): UnavailabilityCollectionDTO {
        val res1 = mutableListOf<ReducedUnavailabilityDTO>()
        val res2 = mutableListOf<ReducedUnavailabilityDTO>()
        val res3 = mutableListOf<ReducedUnavailabilityDTO>()
        val res4 = mutableListOf<ReducedUnavailabilityDTO>()

        val allUsersUnavailabilities = unavailabilitiesService.getAllAdditionalUnavailabilities()
        
        val usersUnavailabilitiesIterator = allUsersUnavailabilities.first.iterator()
        val additionalUsersUnavailabilitiesIterator = allUsersUnavailabilities.second.iterator()
        while(usersUnavailabilitiesIterator.hasNext()){
            val unavailability = usersUnavailabilitiesIterator.next()
            val slotList : MutableCollection<SlotDAO> = unavailability.slots
            val slotIdList : List<Long> = slotList.map { e -> e.id }
            if (unavailability.unsuitable) {
                res2.add(ReducedUnavailabilityDTO(unavailability.user.id, slotIdList.toSet()))
            } else {
                res1.add(ReducedUnavailabilityDTO(unavailability.user.id, slotIdList.toSet()))
            }
        }
        while(additionalUsersUnavailabilitiesIterator.hasNext()) {
            val additionalUnavailability = additionalUsersUnavailabilitiesIterator.next()
            val slotList : MutableCollection<SlotDAO> = additionalUnavailability.slots
            val slotIdList : List<Long> = slotList.map { e -> e.id }
            if (additionalUnavailability.unsuitable) {
                res4.add(ReducedUnavailabilityDTO(additionalUnavailability.user.id, slotIdList.toSet()))
            } else {
                res3.add(ReducedUnavailabilityDTO(additionalUnavailability.user.id, slotIdList.toSet()))
            }
        }
        return UnavailabilityCollectionDTO(res1, res2, res3, res4)
    }

    private fun getPresentationsUnavailabilities(): PresentationUnavailabilityCollectionDTO {
        val res1 = mutableListOf<ReducedPresentationUnavailabilityDTO>()
        val res2 = mutableListOf<ReducedPresentationUnavailabilityDTO>()
        val res3 = mutableListOf<ReducedPresentationUnavailabilityDTO>()
        val res4 = mutableListOf<ReducedPresentationUnavailabilityDTO>()

        val allPresentationsUnavailabilities = presentationUnavailabilitiesService.getAllAdditionalPresentationUnavailabilities()
        val allPresentationsIgnored : Iterable<IgnoredPresentationUnsuitableDAO> = presentationUnavailabilitiesService.getAllIgnoredPresentationUnsuitable()

        val ignoreMap : MutableMap<Long, HashSet<Long>> = HashMap()
        val presentationsIgnoredUnsuitableIterator = allPresentationsIgnored.iterator()
        while(presentationsIgnoredUnsuitableIterator.hasNext()) {
            val presentationIgnoredUnsuitable = presentationsIgnoredUnsuitableIterator.next()
            val slotList : Collection<SlotDAO> = presentationIgnoredUnsuitable.slots
            val slotIdList : List<Long> = slotList.map { e -> e.id }
            ignoreMap.put(presentationIgnoredUnsuitable.presentation.id, HashSet(slotIdList))
        }

        val presentationsUnavailabilitiesIterator = allPresentationsUnavailabilities.first.iterator()
        val additionalPresentationsUnavailabilitiesIterator = allPresentationsUnavailabilities.second.iterator()
        while(presentationsUnavailabilitiesIterator.hasNext()) {
            val presentationUnavailability = presentationsUnavailabilitiesIterator.next()
            val slotList : MutableCollection<SlotDAO> = presentationUnavailability.slots
            val slotIdList : List<Long> = slotList.map { e -> e.id }
            if (presentationUnavailability.unsuitable) {
                val notIgnored = slotIdList.filter { it -> !(ignoreMap.get(presentationUnavailability.presentation.id)!!.contains(it)) }.toSet()
                res2.add(ReducedPresentationUnavailabilityDTO(presentationUnavailability.presentation.id, notIgnored))
            } else {
                res1.add(ReducedPresentationUnavailabilityDTO(presentationUnavailability.presentation.id, slotIdList.toSet()))
            }
        }
        while(additionalPresentationsUnavailabilitiesIterator.hasNext()) {
            val additionalPresentationUnavailability = additionalPresentationsUnavailabilitiesIterator.next()
            val slotList : MutableCollection<SlotDAO> = additionalPresentationUnavailability.slots
            val slotIdList : List<Long> = slotList.map { e -> e.id }
            if (additionalPresentationUnavailability.unsuitable) {
                val notIgnored = slotIdList.filter { it -> !(ignoreMap.get(additionalPresentationUnavailability.presentation.id)!!.contains(it)) }.toSet()
                res4.add(ReducedPresentationUnavailabilityDTO(additionalPresentationUnavailability.presentation.id, notIgnored))
            } else {
                res3.add(ReducedPresentationUnavailabilityDTO(additionalPresentationUnavailability.presentation.id, slotIdList.toSet()))
            }
        }
        return PresentationUnavailabilityCollectionDTO(res1, res2, res3, res4)
    }

    private fun getPresentationsUnavailabilitiesForTesting(): PresentationUnavailabilityCollectionDTO {
        val res1 = mutableListOf<ReducedPresentationUnavailabilityDTO>()
        val res2 = mutableListOf<ReducedPresentationUnavailabilityDTO>()
        val res3 = mutableListOf<ReducedPresentationUnavailabilityDTO>()
        val res4 = mutableListOf<ReducedPresentationUnavailabilityDTO>()

        val allPresentationsUnavailabilities = presentationUnavailabilitiesService.getAllAdditionalPresentationUnavailabilities()
        
        val presentationsUnavailabilitiesIterator = allPresentationsUnavailabilities.first.iterator()
        val additionalPresentationsUnavailabilitiesIterator = allPresentationsUnavailabilities.second.iterator()
        while(presentationsUnavailabilitiesIterator.hasNext()) {
            val presentationUnavailability = presentationsUnavailabilitiesIterator.next()
            val slotList : MutableCollection<SlotDAO> = presentationUnavailability.slots
            val slotIdList : List<Long> = slotList.map { e -> e.id }
            if (presentationUnavailability.unsuitable) {
                res2.add(ReducedPresentationUnavailabilityDTO(presentationUnavailability.presentation.id, slotIdList.toSet()))
            } else {
                res1.add(ReducedPresentationUnavailabilityDTO(presentationUnavailability.presentation.id, slotIdList.toSet()))
            }
        }
        while(additionalPresentationsUnavailabilitiesIterator.hasNext()) {
            val additionalPresentationUnavailability = additionalPresentationsUnavailabilitiesIterator.next()
            val slotList : MutableCollection<SlotDAO> = additionalPresentationUnavailability.slots
            val slotIdList : List<Long> = slotList.map { e -> e.id }
            if (additionalPresentationUnavailability.unsuitable) {
                res4.add(ReducedPresentationUnavailabilityDTO(additionalPresentationUnavailability.presentation.id, slotIdList.toSet()))
            } else {
                res3.add(ReducedPresentationUnavailabilityDTO(additionalPresentationUnavailability.presentation.id, slotIdList.toSet()))
            }
        }
        return PresentationUnavailabilityCollectionDTO(res1, res2, res3, res4)
    }

    private fun getRoomsUnavailabilities(): RoomUnavailabilityCollectionDTO {
        val res1 = mutableListOf<ReducedRoomUnavailabilityDTO>()
        val res2 = mutableListOf<ReducedRoomUnavailabilityDTO>()

        val roomsUnavailabilities = roomUnavailabilitiesService.getAllRoomUnavailabilities()
        
        val roomsUnavailabilitiesIterator = roomsUnavailabilities.iterator()
        while(roomsUnavailabilitiesIterator.hasNext()) {
            val roomUnavailability = roomsUnavailabilitiesIterator.next()
            val slotList : MutableCollection<SlotDAO> = roomUnavailability.slots
            val slotIdList : List<Long> = slotList.map { e -> e.id }
            val slotIdSet : Set<Long> = slotIdList.toSet()
            if (roomUnavailability.unsuitable) {
                res2.add(ReducedRoomUnavailabilityDTO(roomUnavailability.room.id, slotIdSet))
            } else {
                res1.add(ReducedRoomUnavailabilityDTO(roomUnavailability.room.id, slotIdSet))
            }
        }
        return RoomUnavailabilityCollectionDTO(res1, res2)
    }

    private fun getOptionals(): Collection<OptionalViewersDTO>{
        val res = mutableListOf<OptionalViewersDTO>()
        val presentationsWithOptionalsIterator = presentationsService.getPresentationsWithOptionals().iterator()
        while (presentationsWithOptionalsIterator.hasNext()){
            val presentationUnavailabilityWithOptionals = presentationsWithOptionalsIterator.next()
            if(presentationUnavailabilityWithOptionals.optionalParticipant1 != null)
                res.add(OptionalViewersDTO(presentationUnavailabilityWithOptionals.optionalParticipant1!!.id, presentationUnavailabilityWithOptionals.id))
            if(presentationUnavailabilityWithOptionals.optionalParticipant2 != null)
                res.add(OptionalViewersDTO(presentationUnavailabilityWithOptionals.optionalParticipant2!!.id, presentationUnavailabilityWithOptionals.id))
        }
        return res
    }

    private fun getOptionalsForTesting(): Collection<OptionalViewersTestingDTO>{
        val res = mutableListOf<OptionalViewersTestingDTO>()
        val presentationsWithOptionalsIterator = presentationsService.getPresentationsWithOptionals().iterator()
        while (presentationsWithOptionalsIterator.hasNext()){
            val presentationUnavailabilityWithOptionals = presentationsWithOptionalsIterator.next()
            val roomID = if (presentationUnavailabilityWithOptionals.room != null) presentationUnavailabilityWithOptionals.room!!.id else -1L
            val slotID = if (presentationUnavailabilityWithOptionals.slot != null) presentationUnavailabilityWithOptionals.slot!!.id else -1L
            if (roomID == -1L || slotID == -1L) {
                continue
            }
            if(presentationUnavailabilityWithOptionals.optionalParticipant1 != null)
                res.add(OptionalViewersTestingDTO(presentationUnavailabilityWithOptionals.optionalParticipant1!!.id, slotID))
            if(presentationUnavailabilityWithOptionals.optionalParticipant2 != null)
                res.add(OptionalViewersTestingDTO(presentationUnavailabilityWithOptionals.optionalParticipant2!!.id, slotID))
        }
        return res
    }

    private fun getFollowedPresentations():  Collection<ReducedFollowedPresentationsDTO>{
        val res = mutableListOf<ReducedFollowedPresentationsDTO>()

        val followed = followedPresentationsService.getAllFollowedPresentations().iterator()
        while(followed.hasNext()){
            res.add(ReducedFollowedPresentationsDTO(followed.next().presentations.map { e -> e.id }))
        }
        return res
    }

    private fun getAssignedPresentationsForTesting(): Collection<ReducedPresentationDTO> {
        val allPresentations = presentationsService.getAllPresentations().iterator()
        val res = mutableListOf<ReducedPresentationDTO>()
        
        while (allPresentations.hasNext()){
            val onePresentation = allPresentations.next()
            val roomID = if (onePresentation.room != null) onePresentation.room!!.id else -1L
            val slotID = if (onePresentation.slot != null) onePresentation.slot!!.id else -1L
            if (roomID == -1L || slotID == -1L) {
                continue
            }
            res.add(ReducedPresentationDTO(
                onePresentation.id,
                roomID,
                slotID,
                onePresentation.adviser.id,
                onePresentation.arguer.id
            ))
        }
        return res
    }

    private fun formatRoomName(roomNr: String, building: String): String {
        return "${roomNr} - Building: ${building}"
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
}
