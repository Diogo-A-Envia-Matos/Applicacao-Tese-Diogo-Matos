% Rooms assignment

1{room(PresentationID,RoomId):room(RoomId)}1:- slots(_,PresentationID,_,_).

% max one presentation per room
:- 2{room(PresentationID,RoomId):slots(SlotID,PresentationID,_,_)},slots(SlotID,_,_,_),room(_,RoomId).

% max total rooms per slot
:- N{room(PresentationID,RoomId):slots(SlotID,PresentationID,_,_)},slots(SlotID,_,_,_),N=Rooms+1, numberRooms(Rooms,SlotID).

% create location predicates
location(RoomId, SlotID) :- room(PresentationID,RoomId), slots(SlotID,PresentationID,_,_).

% exclude necessary flags
:- locationFlag(R, S).

% exclude optional flags
:- locationFlagOptional(R, S).

%symmetry-breaking
roomSlot(RoomId,SlotID):- room(PresentationID,RoomId),slots(SlotID,PresentationID,_,_).

:- not followedSlots(SlotID,_), room(Presentation1,Room1),room(Presentation2,Room2),slots(SlotID,Presentation1,_,_), slots(SlotID,Presentation2,_,_), Presentation1>Presentation2,Room1<Room2.
:- not followedSlots(SlotID,_), roomSlot(Room1,SlotID), not roomSlot(Room2,SlotID), room(Room2),Room1>Room2.

room(Presentation2,RoomId) :- room(Presentation1,RoomId), followedSlots(Slot1,Slot2),slots(Slot1,Presentation1,Examiner,Adviser),slots(Slot2,Presentation2,Examiner,Adviser).

1{room(Presentation2,Room); room(Presentation3,Room)}1 :- room(Presentation1,Room), followedSlots(Slot1,Slot2), slots(Slot1,Presentation1,Examiner,Adviser), slots(Slot2,Presentation2,_,_), juryMember(Examiner,Presentation2), slots(Slot2,Presentation3,_,_), juryMember(Adviser,Presentation3), Presentation2 > Presentation3.

room(Presentation2,Room) :- room(Presentation1,Room), followedSlots(Slot2,Slot1), slots(Slot1,Presentation1,_,_), juryMember(P1,Presentation1), slots(Slot2,Presentation2,_,_),juryMember(P1,Presentation2), juryMember(P2,Presentation1), not participates(P2,Slot2), juryMember(P3,Presentation2), not participates(P3,Slot1).

juryMember(Participant,PresentationID):- slots(SlotID,PresentationID,Participant,_).
juryMember(Participant,PresentationID):- slots(SlotID,PresentationID,_,Participant).

participates(Participant,SlotID) :- slots(SlotID,_,Participant,_).
participates(Participant,SlotID) :- slots(SlotID,_,_,Participant).


% improved
#minimize{1@1,RoomId,Slot1:roomSlot(RoomId,Slot1),roomSlot(RoomId,Slot2),followedSlots(Slot2,Slot1)}.

% find room switches and minimize them

#minimize { 1@2,Participant,Presentation1:juryMember(Participant,Presentation1),juryMember(Participant,Presentation2),slots(Slot1,Presentation1,_,_),slots(Slot2,Presentation2,_,_), followedSlots(Slot1,Slot2),Presentation1!=Presentation2,room(Presentation1,Room1),room(Presentation2,Room2),Room1!=Room2 }.

% add room to slots
finalSlots(RoomID,SlotID,PresentationID):- slots(SlotID,PresentationID,_,_),room(PresentationID,RoomID).

#show finalSlots/3.