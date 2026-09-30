% determine session participants
% create sessionParticipants for examiners and advisers
sessionParticipants(Examiner,PresentationID):- jury(PresentationID,Examiner,_).
sessionParticipants(Adviser,PresentationID):- jury(PresentationID,_,Adviser).

% precisely one slot per session
1 { assign(PresentationID,SlotID) : slot(SlotID) } 1 :- jury(PresentationID,_,_).

% at most one session per slot per prof
:- 2{assign(PresentationID,SlotID):sessionParticipants(Participant,PresentationID)}, slot(SlotID), sessionParticipants(Participant,_).

% output configuration for occupied
slots(SlotID,PresentationID,Examiner,Adviser) :- assign(PresentationID,SlotID), jury(PresentationID,Examiner,Adviser).

% exactly one room per presentation
1{room(PresentationID,RoomId):room(RoomId)}1:- slots(_,PresentationID,_,_).

% max one presentation per room
:- 2{room(PresentationID,RoomId):slots(SlotID,PresentationID,_,_)},slots(SlotID,_,_,_),room(_,RoomId).

% max total rooms per slot
:- N{room(PresentationID,RoomId):slots(SlotID,PresentationID,_,_)},slots(SlotID,_,_,_),N=Rooms+1, numberRooms(Rooms,SlotID).

% specific room limit
:- N{assign(PresentationID,SlotID)},slot(SlotID),N=Rooms+1, numberRooms(Rooms,SlotID).

%create participates predicates
participates(Participant,SlotID) :- assign(PresentationID,SlotID), sessionParticipants(Participant,PresentationID).

% create location predicates
location(RoomId, SlotID) :- room(PresentationID,RoomId), assign(PresentationID,SlotID).

#minimize{1@1,F:participantFlag(F, _)}.
#minimize{1@1,F:assignFlag(F, _)}.
#minimize{1@1,F:locationFlag(F, _)}.
#minimize{1@1,F:additionalParticipantFlag(F, _)}.
#minimize{1@1,F:additionalAssignFlag(F, _)}.

#show participantFlag/2.
#show assignFlag/2.
#show locationFlag/2.
#show additionalParticipantFlag/2.
#show additionalAssignFlag/2.
