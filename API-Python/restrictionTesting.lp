% generate facts
% extract slot the presentation is assigned to
assign(PresentationID, SlotID) :- presentation(PresentationID, SlotID, _, _, _).

% extract slots the users are assigned to from the presentations
participates(AdviserID, SlotID) :- presentation(_, SlotID, _, AdviserID, _).
participates(ExaminerID, SlotID) :- presentation(_, SlotID, _, _, ExaminerID).

% extract room the presentations is assigned to
location(RoomID, SlotID) :- presentation(_, SlotID, RoomID, _, _).

#show participantFlag/2.
#show participantFlagOptional/2.
#show assignFlag/2.
#show assignFlagOptional/2.
#show locationFlag/2.
#show locationFlagOptional/2.

#show additionalParticipantFlag/2.
#show additionalParticipantFlagOptional/2.
#show additionalAssignFlag/2.
#show additionalAssignFlagOptional/2.
