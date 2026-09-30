from flask import Flask, request, jsonify
import json
import time

from clorm import monkey

monkey.patch()  # must call this before importing clingo

from clorm import IntegerField, Predicate, ConstantField, FactBase
from clorm.clingo import Control

app = Flask(__name__)

# -------------------------------------------------------------------
# Constants
# -------------------------------------------------------------------

# Json objects
PRESENTATIONS_KEY = 'presentations'
PRESENTATION_JURY_KEY = 'presentationJury'
FOLLOWED_SLOTS_KEY = 'followedSlots'
ROOMS_KEY = 'rooms'
USERS_UNAVAILABILITIES_KEY = 'usersUnavailabilities'
USERS_UNSUITABLE_SLOTS_KEY = 'usersUnsuitableSlots'
ADDITIONAL_USERS_UNAVAILABILITIES_KEY = 'additionalUsersUnavailabilities'
ADDITIONAL_USERS_UNSUITABLE_SLOTS_KEY = 'additionalUsersUnsuitableSlots'
PRESENTATIONS_UNAVAILABILITIES_KEY = 'presentationsUnavailabilities'
PRESENTATIONS_UNSUITABLE_SLOTS_KEY = 'presentationsUnsuitableSlots'
ADDITIONAL_PRESENTATIONS_UNAVAILABILITIES_KEY = 'additionalPresentationsUnavailabilities'
ADDITIONAL_PRESENTATIONS_UNSUITABLE_SLOTS_KEY = 'additionalPresentationsUnsuitableSlots'
ROOMS_UNAVAILABILITIES_KEY = 'roomsUnavailabilities'
ROOMS_UNSUITABLE_SLOTS_KEY = 'roomsUnsuitableSlots'
OPTIONALS_KEY = 'optionals'
ALL_PRESENTATIONS_JURIES_KEY = 'allPresentationsJuries'
FOLLOWED_PRESENTATIONS_KEY = 'followedPresentations'

# Attribute keys
SLOT_ID_KEY = 'slotID'
SLOTS_ID_KEY = 'slotsID'
ROOM_ID_KEY = 'roomID'
PRESENTATION_ID_KEY = 'presentationID'
ADVISER_ID_KEY = 'adviserID'
ARGUER_ID_KEY = 'arguerID'
USER_ID_KEY = 'userID'
FOLLOWED_PRESENTATIONS_IDS_KEY = 'followedPresentationsIDS'

# File names
SLOTS_ASSIGNMENT_PROGRAM = "slotsAssignment.lp"
TEST_RESTRICTION_FLAGS_PROGRAM = "testFlags.lp"
TEST_OPTIONAL_FLAGS_PROGRAM = "testFlagsOptional.lp"
ROOMS_ASSIGNMENT_PROGRAM = "roomsAssignment.lp"
FOLLOWED_PRESENTATIONS_PROGRAM = "followedPresentationsFacts.lp"
RESTRICTION_TESTING_PROGRAM = "restrictionTesting.lp"
RESTRICTION_FLAGS_PROGRAM = "assignmentFlags.lp"
UNSUITABLE_FLAGS_PROGRAM = "assignmentFlagsOptional.lp"

# Response identifiers
NO_VALID_SOLUTION_RESPONSE = ["Impossible"]
OPTIONAL_FLAG_RESPONSE = ["OptionalFlag"]
SUCCESS_RESPONSE = ["Success"]

# NO_VALID_SOLUTION_RESPONSE = [["Impossible"]]
# OPTIONAL_FLAG_RESPONSE = [["OptionalFlag"]]
# SUCCESS_RESPONSE = [["Success"]]

CONFLICT_DETECTED_RESPONSE = [["Conflict"]]
NO_CONFLICT_RESPONSE = [["Success"]]

TIME_LIMIT = 600 # = 10 min

# -------------------------------------------------------------------
# Classes creation
# -------------------------------------------------------------------

# Input Classes
class NumberRooms(Predicate):
    number = IntegerField
    slotID = IntegerField

class Location(Predicate):
    roomID = IntegerField
    slotID = IntegerField

class Room(Predicate):
    roomID = IntegerField

class Slot(Predicate):
    slotID = IntegerField

class FollowedSlots(Predicate):
    slotID1 = IntegerField
    slotID2 = IntegerField

class Jury(Predicate):
    presentationID = IntegerField
    adviserID = IntegerField
    arguerID = IntegerField

class SessionParticipants(Predicate):
    userID = IntegerField
    presentationID = IntegerField

class Participates(Predicate):
    userID = IntegerField
    slotID = IntegerField

class Assign(Predicate):
    presentationID = IntegerField
    slotID = IntegerField
    
class Presentation(Predicate):
    presentationID = IntegerField
    slotID = IntegerField
    roomID = IntegerField
    adviserID = IntegerField
    arguerID = IntegerField

class ParticipantFlag(Predicate):
    userID = IntegerField
    slotID = IntegerField

class AssignFlag(Predicate):
    presentationID = IntegerField
    slotID = IntegerField

class LocationFlag(Predicate):
    roomID = IntegerField
    slotID = IntegerField

class ParticipantFlagOptional(Predicate):
    userID = IntegerField
    slotID = IntegerField

class AssignFlagOptional(Predicate):
    presentationID = IntegerField
    slotID = IntegerField

class LocationFlagOptional(Predicate):
    roomID = IntegerField
    slotID = IntegerField

class AdditionalParticipantFlag(Predicate):
    userID = IntegerField
    slotID = IntegerField

class AdditionalAssignFlag(Predicate):
    presentationID = IntegerField
    slotID = IntegerField

class AdditionalParticipantFlagOptional(Predicate):
    userID = IntegerField
    slotID = IntegerField

class AdditionalAssignFlagOptional(Predicate):
    presentationID = IntegerField
    slotID = IntegerField

# Output Class
class Slots(Predicate):
    slotID = IntegerField
    presentationID = IntegerField
    adviserID = IntegerField
    arguerID = IntegerField

class FinalSlots(Predicate):
    roomID = IntegerField
    slotID = IntegerField
    presentationID = IntegerField


# -------------------------------------------------------------------
# Execute Clingo Endpoint
# -------------------------------------------------------------------

@app.route("/executeClingo", methods=["POST"])
def execute_clingo():

    # print("Sleeping for 10 seconds")

    # #TODO Test Computation state
    # time.sleep(10)
    
    # print("Awake")

    # ---------------------Data Process-----------------------------
    data_or_error = process_data(request)
    
    if isinstance(data_or_error, tuple):  # Check if it's an error response
        return data_or_error  # Return the error response
    
    data = data_or_error  # Data successfully parsed
    
    #TODO Add new presentations
    data_keys = {
        PRESENTATION_JURY_KEY: 'allPresentationsJuries',
        # PRESENTATION_JURY_KEY: 'presentationJury',
        FOLLOWED_SLOTS_KEY: 'followedSlots',
        ROOMS_KEY: 'rooms',
        USERS_UNAVAILABILITIES_KEY: 'usersUnavailabilities',
        USERS_UNSUITABLE_SLOTS_KEY: 'usersUnsuitableSlots',
        ADDITIONAL_USERS_UNAVAILABILITIES_KEY: 'additionalUsersUnavailabilities',
        ADDITIONAL_USERS_UNSUITABLE_SLOTS_KEY: 'additionalUsersUnsuitableSlots',
        PRESENTATIONS_UNAVAILABILITIES_KEY: 'presentationsUnavailabilities',
        PRESENTATIONS_UNSUITABLE_SLOTS_KEY: 'presentationsUnsuitableSlots',
        ADDITIONAL_PRESENTATIONS_UNAVAILABILITIES_KEY: 'additionalPresentationsUnavailabilities',
        ADDITIONAL_PRESENTATIONS_UNSUITABLE_SLOTS_KEY: 'additionalPresentationsUnsuitableSlots',
        ROOMS_UNAVAILABILITIES_KEY: 'roomsUnavailabilities',
        ROOMS_UNSUITABLE_SLOTS_KEY: 'roomsUnsuitableSlots',
        OPTIONALS_KEY: 'optionals',
        FOLLOWED_PRESENTATIONS_KEY:'followedPresentations'
    }
    
    # Extract data using keys
    extracted_data = {value: data[key] for key, value in data_keys.items() if key in data}
    
    print("extracted_data:")
    print(extracted_data)

    #TODO Check flags before trying to generate program (include followed presentations ?)

    write_followed_presentations_program(extracted_data, FOLLOWED_PRESENTATIONS_PROGRAM)
    write_restriction_flags_program(extracted_data, RESTRICTION_FLAGS_PROGRAM)
    write_unsuitable_flags_program(extracted_data, UNSUITABLE_FLAGS_PROGRAM)

    # -----------------Geração Factos------------------------

    #TODO Create new array
    room_assignment_standard_facts = []
    slot_assignment_facts = []
    slot_testing_facts = []

    # Generate facts
    generate_facts(extracted_data, slot_assignment_facts, room_assignment_standard_facts, slot_testing_facts)
    
    # print("slot_assignment_facts:")
    # print(slot_assignment_facts)
        
    # print("slot_testing_facts:")
    # print(slot_testing_facts)
    
    # print("room_assignment_standard_facts:")
    # print(room_assignment_standard_facts)

    # ---------------------Clorm-----------------------------

    #TODO New Clorm program: Test flags

    # ---------------------------------
    #     Restriction flag testing
    # ---------------------------------
    
    # Create a Control object that will unify models against the appropriate predicates. 
    #TODO ADD RESTRICTION FLAG PROGRAM
    ctrl = Control(unifier=[Slot, NumberRooms, FollowedSlots, Jury, SessionParticipants, Participates, Assign, Location,
                            ParticipantFlag, AssignFlag, LocationFlag, AdditionalParticipantFlag, AdditionalAssignFlag])
    # Load the asp file that encodes the problem domain.
    ctrl.load(FOLLOWED_PRESENTATIONS_PROGRAM)
    ctrl.load(RESTRICTION_FLAGS_PROGRAM)
    ctrl.load(TEST_RESTRICTION_FLAGS_PROGRAM)
    
    # Add the instance data and ground the ASP program
    ctrl.add_facts(FactBase(slot_testing_facts))
    ctrl.ground([("base", [])])

    resultFlags = None
    
    def on_model_result_flags(model):
        nonlocal resultFlags
        resultFlags = [(str(model)), model.cost]

    start_time = time.time()
    
    with ctrl.solve(on_model=on_model_result_flags, async_=True) as handle:
        handle.wait(TIME_LIMIT)
        handle.cancel()

    end_time = time.time()
    
    flags_list = resultFlags[0].split(" ")

    print("Result Flags:")
    print(resultFlags)
    print("Time on slot assignment flag testing:")
    print(end_time-start_time)
    print("Results:")
    print(resultFlags[0])
    print(len(resultFlags[0]))
    print("Slots Optimization:")
    print(resultFlags[1])
    print(len(resultFlags[1]))

    # if (len(resultFlags[0]) != 0) or (len(resultFlags[1]) != 0) and (resultFlags[1][0] != 0):
    if (len(resultFlags[1]) != 0) and (resultFlags[1][0] != 0):
        print("No possible solution found")

        no_solution_response = []

        #TODO Send response to the Server that there is no valid solution
        # No solution response
        no_solution_response.append(NO_VALID_SOLUTION_RESPONSE)
        no_solution_response.append(flags_list)

        # no_solution_response.extend(NO_VALID_SOLUTION_RESPONSE)
        # no_solution_response.extend(flags_list)

        print(no_solution_response)

        return jsonify(no_solution_response)




    # ---------------------------------
    #      Optional flag testing
    # ---------------------------------
    #TODO ADD OPTIONAL FLAG PROGRAM
    
    ctrl = Control(unifier=[Slot, NumberRooms, FollowedSlots, Jury, SessionParticipants, Participates, Assign, Location,
                            ParticipantFlag, AssignFlag, LocationFlag, ParticipantFlagOptional, AssignFlagOptional, LocationFlagOptional,
                            AdditionalParticipantFlag, AdditionalAssignFlag,
                            AdditionalParticipantFlagOptional, AssignFlagOptional])
    # Load the asp file that encodes the problem domain.
    ctrl.load(FOLLOWED_PRESENTATIONS_PROGRAM)
    ctrl.load(RESTRICTION_FLAGS_PROGRAM)
    ctrl.load(UNSUITABLE_FLAGS_PROGRAM)
    ctrl.load(TEST_OPTIONAL_FLAGS_PROGRAM)
    
    # Add the instance data and ground the ASP program
    ctrl.add_facts(FactBase(slot_testing_facts))
    ctrl.ground([("base", [])])

    resultFlags = None
    
    def on_model(model):
        nonlocal resultFlags
        resultFlags = [(str(model)), model.cost]

    start_time = time.time()
    
    with ctrl.solve(on_model=on_model_result_flags, async_=True) as handle:
        handle.wait(TIME_LIMIT)
        handle.cancel()

    end_time = time.time()
    
    optional_flags_list = resultFlags[0].split(" ")

    print("Result Flags:")
    print(resultFlags)
    print("Time on slot assignment optional flag testing:")
    print(end_time-start_time)
    print("Results:")
    print(resultFlags[0])
    print(len(resultFlags[0]))
    print("Slots Optimization:")
    print(resultFlags[1])
    print(len(resultFlags[1]))

    if (len(resultFlags[1]) != 0) and ((resultFlags[1][0] != 0) and ((len(resultFlags[1]) == 1) or (resultFlags[1][1] != 0))):
        print("No solution found that satisfies every obligatory and optional restriction")
        #TODO Return flags that were triggered, and do not perform slot assignment. The user will be able to perfom changes
        
        optional_flag_response = []

        #TODO Send response to the Server that there is no valid solution
        # optional flag response
        optional_flag_response.append(OPTIONAL_FLAG_RESPONSE)
        optional_flag_response.append(optional_flags_list)

        # optional_flag_response.extend(OPTIONAL_FLAG_RESPONSE)
        # optional_flag_response.extend(optional_flags_list)

        print(optional_flag_response)

        return jsonify(optional_flag_response)

    # ---------------------------------
    #         Slot assignemnt
    # ---------------------------------

    #TODO Slot attribution based on flags
    # Create a Control object that will unify models against the appropriate predicates. 
    ctrl = Control(unifier=[Slot, NumberRooms, FollowedSlots, Jury, SessionParticipants, Slots, Participates, Assign])
    # Load the asp file that encodes the problem domain.
    ctrl.load(FOLLOWED_PRESENTATIONS_PROGRAM)
    ctrl.load(SLOTS_ASSIGNMENT_PROGRAM)
    
    # Add the instance data and ground the ASP program
    ctrl.add_facts(FactBase(slot_assignment_facts))
    ctrl.ground([("base", [])])

    result = None
    
    def on_model(model):
        nonlocal result
        result = [(str(model)), model.cost]

    start_time = time.time()
    
    with ctrl.solve(on_model=on_model, async_=True) as handle:
        handle.wait(TIME_LIMIT)
        handle.cancel()

    end_time = time.time()

    print("Result model:")
    print(result)
    
    slots_list = result[0].split(" ")
    # final_slots_list = result[0].split(" ")

    print("Time on slot assignment:")
    print(end_time-start_time)
    print("Results:")
    print(result[0])
    print("Slots Optimization:")
    print(result[1])

    #writeProgram("slots.lp",slots_list) 

    # Get followed slots to divide by sets
    followed_slots = extracted_data.get(FOLLOWED_SLOTS_KEY, [])
    
    final_rooms_list = []

    print(SUCCESS_RESPONSE)
    # Successful response
    final_rooms_list.append(SUCCESS_RESPONSE)
    # final_rooms_list.extend(SUCCESS_RESPONSE)

    # print(final_rooms_list)
    # return jsonify(final_rooms_list)

    # Loop through each set in followed_slots
    for sublist in followed_slots:
        #add standard facts for room assignment
        # print("sublist ", sublist)
        room_assignment_facts = list(room_assignment_standard_facts)
        # print("room_assignment_facts ", room_assignment_facts)
        
        for item in sublist:
            slot_id = item[SLOT_ID_KEY]
            
            # Find slots that match the slotID and add them into the facts list
            for slots_str in slots_list:
                if int(slots_str.split('(')[1].split(',')[0]) == slot_id:
                    numbers = [int(num) for num in slots_str[6:-1].split(",")]
                    room_assignment_facts.append(Slots(slotID=numbers[0], presentationID=numbers[1], adviserID=numbers[2], arguerID=numbers[3]))
        # -------------------------------
        #  Clorm program Room assignment
        # -------------------------------
        # print("(slots added) room_assignment_facts ", room_assignment_facts)
        print("room_assignment_facts ", room_assignment_facts)
        ctrl = Control(unifier=[NumberRooms, Room, FollowedSlots, Slots, FinalSlots])
        ctrl.add_facts(FactBase(room_assignment_facts))
        ctrl.load(ROOMS_ASSIGNMENT_PROGRAM)
        ctrl.ground([("base", [])])
        
        start_time = time.time()
        with ctrl.solve(on_model=on_model, async_=True) as handle:
            handle.wait(TIME_LIMIT)
            handle.cancel()
        print("Result model:")
        print(result)
        final_rooms_list.append(result[0].split(" "))
        end_time = time.time()
        print("Time on room assignment:")
        print(end_time-start_time)
        print("Results:")
        print(result[0])
        print("Room Optimization:")
        print(result[1])

    writeProgram("finalSlots.lp",final_rooms_list) 

    # for element in final_rooms_list:
    #    print(element)
    print(final_rooms_list)
    return jsonify(final_rooms_list)

# -------------------------------------------------------------------
# Test Restrictions Endpoint
# -------------------------------------------------------------------

@app.route("/testRestrictions", methods=["POST"])
def test_restrictions():

    # print("Sleeping for 10 seconds")

    # time.sleep(10)
    
    # print("Awake")

    # ---------------------Data Process-----------------------------
    data_or_error = process_data(request)
    
    if isinstance(data_or_error, tuple):  # Check if it's an error response
        return data_or_error  # Return the error response
    
    data = data_or_error  # Data successfully parsed
    
    data_keys = {
        PRESENTATIONS_KEY: 'presentations',
        USERS_UNAVAILABILITIES_KEY: 'usersUnavailabilities',
        USERS_UNSUITABLE_SLOTS_KEY: 'usersUnsuitableSlots',
        ADDITIONAL_USERS_UNAVAILABILITIES_KEY: 'additionalUsersUnavailabilities',
        ADDITIONAL_USERS_UNSUITABLE_SLOTS_KEY: 'additionalUsersUnsuitableSlots',
        PRESENTATIONS_UNAVAILABILITIES_KEY: 'presentationsUnavailabilities',
        PRESENTATIONS_UNSUITABLE_SLOTS_KEY: 'presentationsUnsuitableSlots',
        ADDITIONAL_PRESENTATIONS_UNAVAILABILITIES_KEY: 'additionalPresentationsUnavailabilities',
        ADDITIONAL_PRESENTATIONS_UNSUITABLE_SLOTS_KEY: 'additionalPresentationsUnsuitableSlots',
        ROOMS_UNAVAILABILITIES_KEY: 'roomsUnavailabilities',
        ROOMS_UNSUITABLE_SLOTS_KEY: 'roomsUnsuitableSlots',
        OPTIONALS_KEY: 'optionals',
    }
    
    # Extract data using keys
    extracted_data = {value: data[key] for key, value in data_keys.items() if key in data}

    print("extracted_data:")
    print(extracted_data)

    write_test_restriction_flags_program(extracted_data, RESTRICTION_FLAGS_PROGRAM)
    write_test_unsuitable_flags_program(extracted_data, UNSUITABLE_FLAGS_PROGRAM)

    # -----------------Geração Factos------------------------

    testing_facts = []

    # Generate facts
    generate_testing_facts(extracted_data, testing_facts)
    
    print("testing_facts:")
    print(testing_facts)
    
    # ---------------------Clorm-----------------------------

    # ---------------------------------
    #     Restriction testing
    # ---------------------------------
    
    # Create a Control object that will unify models against the appropriate predicates. 
    ctrl = Control(unifier=[Presentation, SessionParticipants, Room, Slot, Participates, Assign, ParticipantFlag, AssignFlag, ParticipantFlagOptional, AssignFlagOptional])
    # Load the asp file that encodes the problem domain.
    ctrl.load(RESTRICTION_FLAGS_PROGRAM)
    ctrl.load(UNSUITABLE_FLAGS_PROGRAM)
    ctrl.load(RESTRICTION_TESTING_PROGRAM)
    
    # Add the instance data and ground the ASP program
    ctrl.add_facts(FactBase(testing_facts))
    ctrl.ground([("base", [])])

    result = None
    
    def on_model_result(model):
        nonlocal result
        result = [(str(model))]

    start_time = time.time()
    
    with ctrl.solve(on_model=on_model_result, async_=True) as handle:
        handle.wait(TIME_LIMIT)
        handle.cancel()

    end_time = time.time()
    
    print("Result:")
    print(result)
    print("Time on restriction testing:")
    print(end_time-start_time)

    response = []

    if ((result is not None) and (len(result[0]) != 0)):
        result_list = result[0].split(" ")
        print("Some restriction conflicts detected")
        
        response.extend(CONFLICT_DETECTED_RESPONSE)
        response.extend([result_list])

        print("Response:")
        print(response)

        return jsonify(response)

    response.extend(NO_CONFLICT_RESPONSE)

    print("Response:")
    print(response)
    return jsonify(response)

# -------------------------------------------------------------------
# Functions
# -------------------------------------------------------------------

def process_data(request):
    if request.headers['Content-Type'] != 'application/json':
        return jsonify({'error': 'Unsupported Media Type'}), 400
    
    try:
        data = json.loads(request.data)
        return data
    except Exception as e:
        return jsonify({'error': 'Failed to parse JSON data'}), 400

def write_followed_presentations_program(data, program_name):
    followedPresentations = data.get(FOLLOWED_PRESENTATIONS_KEY)

    i = 0
    file = open(program_name,"w")
    for followed in followedPresentations:
        ids = followed[FOLLOWED_PRESENTATIONS_IDS_KEY]
        file.write(f"ok{i}:- assign({ids[0]}, H1), assign({ids[1]}, H2), followedSlots(H1, H2).\n")
        file.write(f"ok{i}:- assign({ids[0]}, H1), assign({ids[1]}, H2), followedSlots(H2, H1).\n")
        file.write(f":- not ok{i}.\n")

def write_restriction_flags_program(data, program_name):
    userUnavailabilities = data.get(USERS_UNAVAILABILITIES_KEY)
    presentationUnavailabilities = data.get(PRESENTATIONS_UNAVAILABILITIES_KEY)
    roomUnavailabilities = data.get(ROOMS_UNAVAILABILITIES_KEY)
    additionalUserUnavailabilities = data.get(ADDITIONAL_USERS_UNAVAILABILITIES_KEY)
    additionalPresentationUnavailabilities = data.get(ADDITIONAL_PRESENTATIONS_UNAVAILABILITIES_KEY)

    file = open(program_name,"w")
    for unavailability in userUnavailabilities:
        userId = unavailability[USER_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        for slotId in slotList:
            file.write(f"participantFlag({userId}, {slotId}):- participates({userId}, {slotId}).\n")

    for unavailability in additionalUserUnavailabilities:
        userId = unavailability[USER_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        for slotId in slotList:
            file.write(f"additionalParticipantFlag({userId}, {slotId}):- participates({userId}, {slotId}).\n")

    for unavailability in presentationUnavailabilities:
        presentationId = unavailability[PRESENTATION_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        for slotId in slotList:
            file.write(f"assignFlag({presentationId}, {slotId}):- assign({presentationId}, {slotId}).\n")

    for unavailability in additionalPresentationUnavailabilities:
        presentationId = unavailability[PRESENTATION_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        for slotId in slotList:
            file.write(f"additionalAssignFlag({presentationId}, {slotId}):- assign({presentationId}, {slotId}).\n")

    for unavailability in roomUnavailabilities:
        roomId = unavailability[ROOM_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        for slotId in slotList:
            file.write(f"locationFlag({roomId}, {slotId}):- location({roomId}, {slotId}).\n")

def write_unsuitable_flags_program(data, program_name):
    userUnsuitableSlots = data.get(USERS_UNSUITABLE_SLOTS_KEY)
    presentationUnsuitableSlots = data.get(PRESENTATIONS_UNSUITABLE_SLOTS_KEY)
    roomUnsuitableSlots = data.get(ROOMS_UNSUITABLE_SLOTS_KEY)
    additionalUserUnsuitableSlots = data.get(ADDITIONAL_USERS_UNSUITABLE_SLOTS_KEY)
    additionalPresentationUnsuitableSlots = data.get(ADDITIONAL_PRESENTATIONS_UNSUITABLE_SLOTS_KEY)

    i = 0
    file = open(program_name,"w")

    for unavailability in userUnsuitableSlots:
        userId = unavailability[USER_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        for slotId in slotList:
            file.write(f"participantFlagOptional({userId}, {slotId}):- participates({userId}, {slotId}).\n")

    for unavailability in additionalUserUnsuitableSlots:
        userId = unavailability[USER_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        for slotId in slotList:
            file.write(f"additionalParticipantFlagOptional({userId}, {slotId}):- participates({userId}, {slotId}).\n")

    for unavailability in presentationUnsuitableSlots:
        presentationId = unavailability[PRESENTATION_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        for slotId in slotList:
            file.write(f"assignFlagOptional({presentationId}, {slotId}):- assign({presentationId}, {slotId}).\n")

    for unavailability in additionalPresentationUnsuitableSlots:
        presentationId = unavailability[PRESENTATION_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        for slotId in slotList:
            file.write(f"additionalAssignFlagOptional({presentationId}, {slotId}):- assign({presentationId}, {slotId}).\n")

    for unavailability in roomUnsuitableSlots:
        roomId = unavailability[ROOM_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        for slotId in slotList:
            file.write(f"locationFlagOptional({roomId}, {slotId}):- location({roomId}, {slotId}).\n")

def write_test_restriction_flags_program(data, program_name):
    userUnavailabilities = data.get(USERS_UNAVAILABILITIES_KEY)
    presentationUnavailabilities = data.get(PRESENTATIONS_UNAVAILABILITIES_KEY)
    roomUnavailabilities = data.get(ROOMS_UNAVAILABILITIES_KEY)
    additionalUserUnavailabilities = data.get(ADDITIONAL_USERS_UNAVAILABILITIES_KEY)
    additionalPresentationUnavailabilities = data.get(ADDITIONAL_PRESENTATIONS_UNAVAILABILITIES_KEY)

    i = 0
    file = open(program_name,"w")

    for unavailability in userUnavailabilities:
        userId = unavailability[USER_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        print(f"unavailability {i}")
        print(unavailability)

        i += 1

        for slotId in slotList:
            print(f"flag written: {slotId}")
            file.write(f"participantFlag({userId}, {slotId}):- participates({userId}, {slotId}).\n")

    i = 0
    for unavailability in additionalUserUnavailabilities:
        userId = unavailability[USER_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        print(f"unavailability {i}")
        print(unavailability)

        i += 1

        for slotId in slotList:
            print(f"flag written: {slotId}")
            file.write(f"additionalParticipantFlag({userId}, {slotId}):- participates({userId}, {slotId}).\n")

    i = 0
    for unavailability in presentationUnavailabilities:
        presentationId = unavailability[PRESENTATION_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]
        
        print(f"unavailability {i}")
        print(unavailability)

        i += 1

        for slotId in slotList:
            print(f"flag written: {slotId}")
            file.write(f"assignFlag({presentationId}, {slotId}):- assign({presentationId}, {slotId}).\n")
            
    i = 0
    for unavailability in additionalPresentationUnavailabilities:
        presentationId = unavailability[PRESENTATION_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]
        
        print(f"unavailability {i}")
        print(unavailability)

        i += 1

        for slotId in slotList:
            print(f"flag written: {slotId}")
            file.write(f"additionalAssignFlag({presentationId}, {slotId}):- assign({presentationId}, {slotId}).\n")

    # print("roomUnavailabilities")
    for unavailability in roomUnavailabilities:
        roomId = unavailability[ROOM_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        for slotId in slotList:
            file.write(f"locationFlag({roomId}, {slotId}):- location({roomId}, {slotId}).\n")

def write_test_unsuitable_flags_program(data, program_name):
    userUnsuitableSlots = data.get(USERS_UNSUITABLE_SLOTS_KEY)
    presentationUnsuitableSlots = data.get(PRESENTATIONS_UNSUITABLE_SLOTS_KEY)
    roomUnsuitableSlots = data.get(ROOMS_UNSUITABLE_SLOTS_KEY)
    additionalUserUnsuitableSlots = data.get(ADDITIONAL_USERS_UNSUITABLE_SLOTS_KEY)
    additionalPresentationUnsuitableSlots = data.get(ADDITIONAL_PRESENTATIONS_UNSUITABLE_SLOTS_KEY)

    file = open(program_name,"w")

    i = 0
    for unavailability in userUnsuitableSlots:
        userId = unavailability[USER_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        print(f"unavailability {i}")
        print(unavailability)

        i += 1

        for slotId in slotList:
            print(f"flag written: {slotId}")
            file.write(f"participantFlagOptional({userId}, {slotId}):- participates({userId}, {slotId}).\n")

    i = 0
    for unavailability in additionalUserUnsuitableSlots:
        userId = unavailability[USER_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        print(f"unavailability {i}")
        print(unavailability)

        i += 1

        for slotId in slotList:
            print(f"flag written: {slotId}")
            file.write(f"additionalParticipantFlagOptional({userId}, {slotId}):- participates({userId}, {slotId}).\n")
    
    i = 0
    for unavailability in presentationUnsuitableSlots:
        presentationId = unavailability[PRESENTATION_ID_KEY]
        slotId = unavailability[SLOTS_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        print(f"unavailability {i}")
        print(unavailability)

        i += 1

        for slotId in slotList:
            print(f"flag written: {slotId}")
            file.write(f"assignFlagOptional({presentationId}, {slotId}):- assign({presentationId}, {slotId}).\n")

    i = 0
    for unavailability in additionalPresentationUnsuitableSlots:
        presentationId = unavailability[PRESENTATION_ID_KEY]
        slotId = unavailability[SLOTS_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        print(f"unavailability {i}")
        print(unavailability)

        i += 1

        for slotId in slotList:
            print(f"flag written: {slotId}")
            file.write(f"additionalAssignFlagOptional({presentationId}, {slotId}):- assign({presentationId}, {slotId}).\n")

    for unavailability in roomUnsuitableSlots:
        roomId = unavailability[ROOM_ID_KEY]
        slotList = unavailability[SLOTS_ID_KEY]

        for slotId in slotList:
            file.write(f"locationFlagOptional({roomId}, {slotId}):- location({roomId}, {slotId}).\n")

def generate_facts(data, slot_assignment_facts, room_assignment_standard_facts, slot_testing_facts):
    # Access extracted data
    rooms = data.get(ROOMS_KEY)
    followed_slots = data.get(FOLLOWED_SLOTS_KEY, [])
    all_presentations_juries = data.get(ALL_PRESENTATIONS_JURIES_KEY, [])
    optionals = data.get(OPTIONALS_KEY, [])
    user_unavailabilities = data.get(USERS_UNAVAILABILITIES_KEY)
    presentation_unavailabilities = data.get(PRESENTATIONS_UNAVAILABILITIES_KEY)
    room_unavailabilities = data.get(ROOMS_UNAVAILABILITIES_KEY)
    room_unsuitable_slots = data.get(ROOMS_UNSUITABLE_SLOTS_KEY)
    additional_user_unavailabilities = data.get(ADDITIONAL_USERS_UNAVAILABILITIES_KEY)
    additional_presentation_unavailabilities = data.get(ADDITIONAL_PRESENTATIONS_UNAVAILABILITIES_KEY)

    # print("rooms")
    # print(rooms)
    # print("room_unavailabilities")
    # print(room_unavailabilities)
    # print("room_unsuitable_slots")
    # print(room_unsuitable_slots)

    # print("(generate_facts) Rooms:")
    # print(rooms)
    # roomNumber = len(rooms)
    # Create Rooms predicate     
    # slot_assignment_facts.append(NumberRooms(number=roomNumber))
    # slot_testing_facts.append(NumberRooms(number=roomNumber))

    # slot_assignment_facts.append(NumberRooms(number=len(rooms)))
    # # room_assignment_standard_facts.append(NumberRooms(number=len(rooms)))
    # slot_testing_facts.append(NumberRooms(number=len(rooms)))
    # # slot_assignment_facts.append(NumberRooms(number=str(len(rooms))))
    # # room_assignment_standard_facts.append(NumberRooms(number=str(len(rooms))))
    # # slot_testing_facts.append(NumberRooms(number=str(len(rooms))))
    
    # print("slot_assignment_facts:")
    # print(slot_assignment_facts)

    for room in rooms:
        slot_testing_facts.append(Room(roomID=room.get(ROOM_ID_KEY)))
        room_assignment_standard_facts.append(Room(roomID=room.get(ROOM_ID_KEY)))
    #     slot_assignment_facts.append(Room(roomID=room.get(ROOM_ID_KEY)))
    #     slot_testing_facts.append(Room(roomID=room.get(ROOM_ID_KEY)))

    # Create Slot and FollowedSlots predicates 
    for set_of_slots in followed_slots:
        for i, slot in enumerate(set_of_slots):
            slot_id = slot.get(SLOT_ID_KEY)
            slot_assignment_facts.append(Slot(slotID=slot_id))
            slot_testing_facts.append(Slot(slotID=slot_id))

            slotValidRoom = rooms
            # slotValidRoom = json.loads(rooms)
            #TODO Get number of available rooms
            for room in room_unavailabilities:
                roomID = room.get(ROOM_ID_KEY)
                roomSlotIDList = room.get(SLOTS_ID_KEY)
                if slot_id in roomSlotIDList:
                    slotValidRoom = [item for item in slotValidRoom if item['roomID'] != roomID]

            for room in room_unsuitable_slots:
                roomID = room.get(ROOM_ID_KEY)
                roomSlotIDList = room.get(SLOTS_ID_KEY)
                if slot_id in roomSlotIDList:
                    slotValidRoom = [item for item in slotValidRoom if item['roomID'] != roomID]

            print("slotValidRoom")
            print(slotValidRoom)
            slot_assignment_facts.append(NumberRooms(number=len(list(slotValidRoom)),slotID=slot_id))
            slot_testing_facts.append(NumberRooms(number=len(list(slotValidRoom)),slotID=slot_id))
            room_assignment_standard_facts.append(NumberRooms(number=len(list(slotValidRoom)),slotID=slot_id))

            if i < len(set_of_slots) - 1:
                next_slot_id = set_of_slots[i + 1].get(SLOT_ID_KEY)
                slot_assignment_facts.append(FollowedSlots(slotID1=slot_id, slotID2=next_slot_id))
                slot_testing_facts.append(FollowedSlots(slotID1=slot_id, slotID2=next_slot_id))
                room_assignment_standard_facts.append(FollowedSlots(slotID1=slot_id, slotID2=next_slot_id))

    # Create Jury predicate 
    for presentation_jury in all_presentations_juries:
        slot_assignment_facts.append(Jury(presentationID=presentation_jury[PRESENTATION_ID_KEY],
                          adviserID=presentation_jury[ADVISER_ID_KEY],
                          arguerID=presentation_jury[ARGUER_ID_KEY]))
        slot_testing_facts.append(Jury(presentationID=presentation_jury[PRESENTATION_ID_KEY],
                          adviserID=presentation_jury[ADVISER_ID_KEY],
                          arguerID=presentation_jury[ARGUER_ID_KEY]))

    # Create OptionalParticipants predicate 
    for item in optionals:
        slot_assignment_facts.append(SessionParticipants(userID=item[USER_ID_KEY],
                                           presentationID=item[PRESENTATION_ID_KEY]))
        slot_testing_facts.append(SessionParticipants(userID=item[USER_ID_KEY],
                                           presentationID=item[PRESENTATION_ID_KEY]))

    # Create Participates negative predicate 
    for item in user_unavailabilities:
        for slot in item[SLOTS_ID_KEY]:
            slot_assignment_facts.append(Participates(userID=item[USER_ID_KEY],
                                      slotID=slot,
                                      sign=False))
    
    # Create Participates negative predicate 
    for item in additional_user_unavailabilities:
        for slot in item[SLOTS_ID_KEY]:
            slot_assignment_facts.append(Participates(userID=item[USER_ID_KEY],
                                      slotID=slot,
                                      sign=False))
    
    # Create Assign negative predicate 
    for item in presentation_unavailabilities:
        for slot in item[SLOTS_ID_KEY]:
            slot_assignment_facts.append(Assign(presentationID=item[PRESENTATION_ID_KEY],
                                slotID=slot,
                                sign=False))
    
    # Create Assign negative predicate 
    for item in additional_presentation_unavailabilities:
        for slot in item[SLOTS_ID_KEY]:
            slot_assignment_facts.append(Assign(presentationID=item[PRESENTATION_ID_KEY],
                                slotID=slot,
                                sign=False))
    
    # Create Location negative predicate 
    for item in room_unavailabilities:
        for slot in item[SLOTS_ID_KEY]:
            room_assignment_standard_facts.append(Location(roomID=item[ROOM_ID_KEY],
                                slotID=slot,
                                sign=False))
            
    # Create Location negative predicate 
    for room in room_unavailabilities:
        for slot in item[SLOTS_ID_KEY]:
            room_assignment_standard_facts.append(Location(roomID=item[ROOM_ID_KEY],
                                slotID=slot,
                                sign=False))

    # Test the facts created
    # writeProgram("scheduleAssignmentFacts.lp",slot_assignment_facts)
    # writeProgram("roomAssignmentFacts.lp",room_assignment_standard_facts)

def generate_testing_facts(data, testing_facts):
    # Access extracted data
    presentationsData = data.get(PRESENTATIONS_KEY)
    optionals = data.get(OPTIONALS_KEY, [])

    for presentation in presentationsData:
        presentation_id = presentation[PRESENTATION_ID_KEY]
        slot_id = presentation[SLOT_ID_KEY]
        room_id = presentation[ROOM_ID_KEY]
        adviser_id = presentation[ADVISER_ID_KEY]
        arguer_id = presentation[ARGUER_ID_KEY]

        testing_facts.append(Presentation(presentationID=presentation_id,
                                        slotID=slot_id,
                                        roomID=room_id,
                                        adviserID=adviser_id,
                                        arguerID=arguer_id))
    
    # Create OptionalParticipants predicate 
    for item in optionals:
        testing_facts.append(Participates(userID=item[USER_ID_KEY],
                                          slotID=item[SLOT_ID_KEY]))
    # Test the facts created
    #writeProgram("scheduleAssignmentFacts.lp",slot_assignment_facts)
    #writeProgram("roomAssignmentFacts.lp",room_assignment_standard_facts)


def writeProgram(name,program):
    
    file = open(name,"w")
    
    for i in range(len(program)):
        file.write(str(program[i])+".\n")




if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000, debug=True)

