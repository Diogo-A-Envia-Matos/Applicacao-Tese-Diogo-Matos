% determine session participants
% create sessionParticipants for examiners and advisers
sessionParticipants(Examiner,PresentationID):- jury(PresentationID,Examiner,_).
sessionParticipants(Adviser,PresentationID):- jury(PresentationID,_,Adviser).

% precisely one slot per session
1 { assign(PresentationID,SlotID) : slot(SlotID) } 1 :- jury(PresentationID,_,_).

% at most one session per slot per prof
:- 2{assign(PresentationID,SlotID):sessionParticipants(Participant,PresentationID)}, slot(SlotID), sessionParticipants(Participant,_).

% specific room limit
:- N{assign(_,SlotID)},slot(SlotID),N=Rooms+1, numberRooms(Rooms,SlotID).

% output configuration for occupied
slots(SlotID,PresentationID,Examiner,Adviser) :- assign(PresentationID,SlotID), jury(PresentationID,Examiner,Adviser).

% exclude necessary flags
:- participantFlag(U, S).

:- assignFlag(P, S).

% exclude optional flags
:- participantFlagOptional(U, S).

:- assignFlagOptional(P, S).

% exclude additional necessary flags
:- additionalParticipantFlag(U, S).

:- additionalAssignFlag(P, S).

% exclude additional optional flags
:- additionalParticipantFlagOptional(U, S).

:- additionalAssignFlagOptional(P, S).

% ----------Optimization---------

% accept any followedSlots order
followedSlots(SlotID1,SlotID2):- followedSlots(SlotID2,SlotID1).

%create participates predicates
participates(Participant,SlotID) :- assign(PresentationID,SlotID), sessionParticipants(Participant,PresentationID).

% find participant with followed presentations 
conn(Participant,SlotID1) :- participates(Participant,SlotID1), followedSlots(SlotID2,SlotID1), participates(Participant,SlotID2).


% reduce number of single presentations
#minimize{1,Participant,SlotID:not conn(Participant,SlotID),participates(Participant,SlotID)}.

#show slots/4.

% new