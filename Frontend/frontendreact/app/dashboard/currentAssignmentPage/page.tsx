"use client";

import InfoBox from '@/app/components/infoBox/component';
import React, { useState, useEffect } from 'react';
import axios, { AxiosError } from 'axios';
import { Container, Typography, TextField, Button, Box, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { FaCheckCircle, FaExclamationCircle, FaInfoCircle } from 'react-icons/fa';
import './Page.css';

interface ErrorResponse {
  message: string;
}

interface RoomDTO {
  id: number;
  roomNr: string;
  building: string;
}

interface SlotDTO {
  id: string;
  date: string;
  startingHour: string; 
}

interface Presentation {
  id: number;
  studentNumber: string;
  studentName: string;
  thesisTitle: string;
  roomId: number | null;
  roomNumber: string | null;
  roomBuilding: string | null;
  startingHour: string | null;
  date: string | null;
  adviserName: string;
  adviserId: number;
  arguerName: string;
  arguerId: number;
  optionalParticipant1Id: number | null;
  optionalParticipant1Name: string | null;
  optionalParticipant2Id: number | null;
  optionalParticipant2Name: string | null;
}

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
}

interface Unavailability {
  id: number;
  slotId: number;
}

interface UserUnavailabilityDTO {
  id: number;
  unsuitable: boolean;

  slotsID: number[];
  userID: number;
}

interface PresentationUnavailabilityDTO {
  id: number;
  unsuitable: boolean;

  slotsID: number[];
  presentationID: number;
  userID: number;
}

//TODO Add ability to test previous solutions
const Page: React.FC = () => {
  const [isTesting, setIsTesting] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [hasTested, setHasTested] = useState(false);
  const [emailSent, setEmailSent] = useState(false);
  const [isComputing, setIsComputing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [onlyOptionalRestrictions, setOnlyOptionalRestrictions] = useState(false);

  const [loggedInUserRole, setLoggedInUserRole] = useState<String>('');
  const [rooms, setRooms] = useState<RoomDTO[]>([]);
  const [slots, setSlots] = useState<SlotDTO[]>([]);
  const [dates, setDates] = useState<string[]>([]);
  const [hours, setHours] = useState<string[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [presentations, setPresentations] = useState<Presentation[]>([]);
  const [userPresentations, setUserPresentations] = useState<Presentation[]>([]);
  const [currentUser, setCurrentUser] = useState<number | null>(null);
  const [currentDate, setCurrentDate] = useState<string>('');

  const [userUnavailable, setUserUnavailable] = useState<Unavailability[]>([]);
  const [userUnsuitable, setUserUnsuitable] = useState<Unavailability[]>([]);
  const [presentationUnavailable, setPresentationUnavailable] = useState<Unavailability[]>([]);
  const [presentationUnsuitable, setPresentationUnsuitable] = useState<Unavailability[]>([]);
  const [roomUnavailable, setRoomUnavailable] = useState<Unavailability[]>([]);
  const [roomUnsuitable, setRoomUnsuitable] = useState<Unavailability[]>([]);
  
  const [userAdditionalUnavailable, setUserAdditionalUnavailable] = useState<Unavailability[]>([]);
  const [userAdditionalUnsuitable, setUserAdditionalUnsuitable] = useState<Unavailability[]>([]);
  const [presentationAdditionalUnavailable, setPresentationAdditionalUnavailable] = useState<Unavailability[]>([]);
  const [presentationAdditionalUnsuitable, setPresentationAdditionalUnsuitable] = useState<Unavailability[]>([]);
  
  const [presentationDialogOpen, setPresentationDialogOpen] = useState(false);
  const [currentPresentation, setCurrentPresentation] = useState<Presentation | undefined>(undefined);
  
  useEffect(() => {
    const checkComputation = async () => {
      try{
        const token = sessionStorage.getItem('token');
        const response = await axios.get(`http://localhost:8080/api/python/checkComputation`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        if (response.data === true) {
          setIsComputing(true);
          console.log("There is already an assignment being computed, try again later.")
        }
      } catch(error) {
        if (axios.isAxiosError(error)) {
          const axiosError = error as AxiosError<ErrorResponse>;
          if (axiosError.response?.status === 401) {
              // Unauthorized, remove token and id, and redirect to login page
              sessionStorage.removeItem('token');
              sessionStorage.removeItem('id');
              if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
                sessionStorage.setItem('sessionExpiredAlertShown', 'true');
                alert("Your session has expired. Please log in again if you want to continue.");
              }
              window.location.href = 'http://localhost:3000';
          }  else if(axiosError.response?.status == 422){
            setError(axiosError.response.data.message);
          } else {
            setError('There was an error on the server side, please try again later.');
          }
        } else {
          setError('There was an error on the server side, please try again later.');
        }
      } 
    }

    checkComputation()
  }, []);

  const fetchUserRole = async () => {
      try {
          const token = sessionStorage.getItem('token');
          const userId = sessionStorage.getItem('id');

          const response = await axios.get<User>(`http://localhost:8080/api/user/${userId}`, {
              headers: {
                  Authorization: `Bearer ${token}`,
              },
          });
          setLoggedInUserRole(response.data.role);
          console.log("loggedInUserRole: ", loggedInUserRole)
      } catch (error) {
          console.error('Error fetching user role:', error);
          if (axios.isAxiosError(error)) {
              const axiosError = error as AxiosError;
              if (axiosError.response?.status === 401) {
                  sessionStorage.removeItem('token');
                  sessionStorage.removeItem('id');
                  alert("Your session has expired. Please log in again.");
                  window.location.href = 'http://localhost:3000';
              }
          }
      }
  };

  const fetchUsersData = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const response = await axios.get<User[]>('http://localhost:8080/api/user', {
        headers: {
          Authorization: `Bearer ${token}`
        }
    });
      setUsers(response.data);
    } catch (error) {
      console.error('Error fetching users:', error);
    }
  };

  const fetchRoomsData = async () => {
    try{
      const token = sessionStorage.getItem('token');
      const response = await axios.get<RoomDTO[]>('http://localhost:8080/api/room', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      setRooms(response.data)
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError;
        if (axiosError.response?.status === 401) {
            // Unauthorized, remove token and id, and redirect to login page
            sessionStorage.removeItem('token');
            sessionStorage.removeItem('id');
            if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
              sessionStorage.setItem('sessionExpiredAlertShown', 'true');
              alert("Your session has expired. Please log in again if you want to continue.");
          }
            window.location.href = 'http://localhost:3000';
        } else {
          setError('There was an error on the server side, please try again later.');
        }
      } else {
        setError('There was an error on the server side, please try again later.');
      }
    }
  };

  const fetchSlotsData = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const response = await axios.get<SlotDTO[]>('http://localhost:8080/api/slot', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const data = response.data;
      setSlots(data);
      const uniqueDates = Array.from(new Set(data.map(slot => slot.date)));
      const uniqueHours = Array.from(new Set(data.map(slot => slot.startingHour)));
      setDates(uniqueDates);
      setHours(uniqueHours.sort());
    } catch (error) {
      console.error('Error fetching slots:', error);
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError;
        if (axiosError.response?.status === 401) {
            // Unauthorized, remove token and id, and redirect to login page
            sessionStorage.removeItem('token');
            sessionStorage.removeItem('id');
            if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
              sessionStorage.setItem('sessionExpiredAlertShown', 'true');
              alert("Your session has expired. Please log in again if you want to continue.");
          }
            window.location.href = 'http://localhost:3000';
        } else {
          setError('There was an error on the server side, please try again later.');
        }
      } else {
        setError('There was an error on the server side, please try again later.');
      }
    }
  };

  const fetchPresentationsData = async () => {
    const token = sessionStorage.getItem('token');
    try{
      const response = await axios.get<Presentation[]>('http://localhost:8080/api/presentation', {
          headers: {
              Authorization: `Bearer ${token}`
          }
      });
      setPresentations(response.data);
    } catch (error){
      console.error('Error fetching presentations:', error);
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError;
        if (axiosError.response?.status === 401) {
            // Unauthorized, remove token and id, and redirect to login page
            sessionStorage.removeItem('token');
            sessionStorage.removeItem('id');
            if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
              sessionStorage.setItem('sessionExpiredAlertShown', 'true');
              alert("Your session has expired. Please log in again if you want to continue.");
          }
            window.location.href = 'http://localhost:3000';
        }
      }
    }
  };

  const formatRoom = (room: RoomDTO): string => {
    return `Building ${room.building}  Room ${room.roomNr}`;
  };

  const formatDate = (date: string): string => {
    const [year, month, day] = date.split('-');
    return `${day}-${month}-${year}`;
  };

  const formatStartingHour = (startingHour: string | null | undefined): string => {
    if (startingHour === null || startingHour === undefined) return 'Not assigned yet';
    
    // Parse the time string to get the hour and minute components
    const [hour, minute] = startingHour.split(':').map(Number);

    // Ensure hour and minute are padded with leading zeros if necessary
    const formattedHour = hour.toString().padStart(2, '0');
    const formattedMinute = minute.toString().padStart(2, '0');

    return `${formattedHour}:${formattedMinute}`;
  };

  const compareRooms = (a: RoomDTO , b: RoomDTO) => {
    if (a.building !== b.building) {
      return parseInt(a.building) - parseInt(b.building)
    } else {
      return parseInt(a.roomNr) - parseInt(b.roomNr)
    }
  };

  const calculateFinishHour = (startingHour: string): string => {
    const [hour, minute] = startingHour.split(':').map(parseFloat);
    const finishHour = hour + (minute + 45) / 60;
    const finishHourHour = Math.floor(finishHour);
    const finishHourMinute = Math.round((finishHour - finishHourHour) * 60);
    return `${finishHourHour.toString().padStart(2, '0')}:${finishHourMinute.toString().padStart(2, '0')}`;
  };

  // const updateCurrentPresentations = () => {
  //   if (currentDate === '') {
  //     setDatePresentations(presentations)
  //   } else {
  //     setDatePresentations(presentations.find((value) => value.date === currentDate))
  //   }
  // };
  
  const changeUserPresentations = async (userId: number | null) => {
    if (userId === null) {
      setUserPresentations([]);
      return;
    }
    console.info('changeUserPresentations: ', userId);
    const token = sessionStorage.getItem('token');
    try{
      const response = await axios.get<Presentation[]>(`http://localhost:8080/api/presentation/user/${userId}`, {
        headers: {
            Authorization: `Bearer ${token}`
        }
      });
      setUserPresentations(response.data);
    } catch (error){
      console.error('Error fetching presentations:', error);
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError;
        if (axiosError.response?.status === 401) {
          // Unauthorized, remove token and id, and redirect to login page
          sessionStorage.removeItem('token');
          sessionStorage.removeItem('id');
          if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
            sessionStorage.setItem('sessionExpiredAlertShown', 'true');
            alert("Your session has expired. Please log in again if you want to continue.");
          }
          window.location.href = 'http://localhost:3000';
        }
      }
    }
  };

  const testRestrictions = async () => {
    //TODO Send response to user
    setHasTested(false);
    setOnlyOptionalRestrictions(false);
    setIsTesting(true);
    setSuccess(false);
    const token = sessionStorage.getItem('token');
    axios.post(`http://localhost:8080/api/python/test`, {}, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    .then((response) => {
      console.log("response: ", response)
      const success = response.data.noConflicts
      if (success) {
        //TODO Write on Site
        console.log("No conflicts found")
        setSuccess(true);
      } else {
        //TODO Get additional restriction
        const userUnavailabilitiesflags = response.data.usersUnavailabilitiesList
        const usersUnsuitableSlotsFlags = response.data.usersUnsuitableSlotsList
        const presentationsUnavailabilitiesFlags = response.data.presentationsUnavailabilitiesList
        const presentationsUnsuitableSlotsFlags = response.data.presentationsUnsuitableSlotsList
        const roomsUnavailabilitiesFlags = response.data.roomsUnavailabilitiesList
        const roomsUnsuitableSlotsFlags = response.data.roomsUnsuitableSlotsList
        const additionalUserUnavailabilitiesflags = response.data.additionalUsersUnavailabilitiesList
        const additionalUsersUnsuitableSlotsFlags = response.data.additionalUsersUnsuitableSlotsList
        const additionalPresentationsUnavailabilitiesFlags = response.data.additionalPresentationsUnavailabilitiesList
        const additionalPresentationsUnsuitableSlotsFlags = response.data.additionalPresentationsUnsuitableSlotsList
        const userUnavailableList : Unavailability[] = []
        const userUnsuitableList : Unavailability[] = []
        const presentationUnavailableList : Unavailability[] = []
        const presentationUnsuitableList : Unavailability[] = []
        const roomUnavailableList : Unavailability[] = []
        const roomUnsuitableList : Unavailability[] = []
        const additionalUserUnavailableList : Unavailability[] = []
        const additionalUserUnsuitableList : Unavailability[] = []
        const additionalPresentationUnavailableList : Unavailability[] = []
        const additionalPresentationUnsuitableList : Unavailability[] = []

        var onlyOptional = true

        for (const userUnavailability of userUnavailabilitiesflags) {
          userUnavailableList.push({id: userUnavailability.first, slotId: userUnavailability.second})
          onlyOptional = false
        }
        for (const userUnsuitable of usersUnsuitableSlotsFlags) {
          userUnsuitableList.push({id: userUnsuitable.first, slotId: userUnsuitable.second})
        }
        for (const presentationUnavailability of presentationsUnavailabilitiesFlags) {
          presentationUnavailableList.push({id: presentationUnavailability.first, slotId: presentationUnavailability.second})
          onlyOptional = false
        }
        for (const presentationUnsuitable of presentationsUnsuitableSlotsFlags) {
          presentationUnsuitableList.push({id: presentationUnsuitable.first, slotId: presentationUnsuitable.second})
        }
        for (const roomUnavailability of roomsUnavailabilitiesFlags) {
          roomUnavailableList.push({id: roomUnavailability.first, slotId: roomUnavailability.second})
          onlyOptional = false
        }
        for (const roomUnsuitable of roomsUnsuitableSlotsFlags) {
          roomUnsuitableList.push({id: roomUnsuitable.first, slotId: roomUnsuitable.second})
        }

        for (const additionalUserUnavailability of additionalUserUnavailabilitiesflags) {
          additionalUserUnavailableList.push({id: additionalUserUnavailability.first, slotId: additionalUserUnavailability.second})
          onlyOptional = false
        }
        for (const additionalUserUnsuitable of additionalUsersUnsuitableSlotsFlags) {
          additionalUserUnsuitableList.push({id: additionalUserUnsuitable.first, slotId: additionalUserUnsuitable.second})
        }
        for (const additionalPresentationUnavailability of additionalPresentationsUnavailabilitiesFlags) {
          additionalPresentationUnavailableList.push({id: additionalPresentationUnavailability.first, slotId: additionalPresentationUnavailability.second})
          onlyOptional = false
        }
        for (const additionalPresentationUnsuitable of additionalPresentationsUnsuitableSlotsFlags) {
          additionalPresentationUnsuitableList.push({id: additionalPresentationUnsuitable.first, slotId: additionalPresentationUnsuitable.second})
        }

        setUserUnavailable(userUnavailableList)
        setUserUnsuitable(userUnsuitableList)
        setPresentationUnavailable(presentationUnavailableList)
        setPresentationUnsuitable(presentationUnsuitableList)
        setRoomUnavailable(roomUnavailableList)
        setRoomUnsuitable(roomUnsuitableList)
        setUserAdditionalUnavailable(additionalUserUnavailableList)
        setUserAdditionalUnsuitable(additionalUserUnsuitableList)
        setPresentationAdditionalUnavailable(additionalPresentationUnavailableList)
        setPresentationAdditionalUnsuitable(additionalPresentationUnsuitableList)
        setOnlyOptionalRestrictions(onlyOptional)
        console.log("Some conflicts were found")
        // console.log(userUnavailabilitiesflags)
        // console.log(usersUnsuitableSlotsFlags)
        // console.log(presentationsUnavailabilitiesFlags)
        // console.log(presentationsUnsuitableSlotsFlags)
      }
    })
    .catch((error) => {
      if (error.response?.status === 401) {
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('id');
        if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
          sessionStorage.setItem('sessionExpiredAlertShown', 'true');
          alert("Your session has expired. Please log in again if you want to continue.");
        }
        window.location.href = 'http://localhost:3000';
      } else if(error.response?.status == 422) {
        setError(error.response.data.message);
      } else {
        setError('There was an error on the server side, please try again later.');
        console.log(error.response)
      }
      setSuccess(false);
    })
    .finally(() => {
      setIsTesting(false);
      setHasTested(true);
    });
    // setSuccess(true);
    // setError(null);
  };

  const handleUserChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedValue = parseInt(event.target.value);
    setCurrentUser(selectedValue === 0 ? null : selectedValue);
  };

  const handlePresentationClick = (presentationId: number) => {
    const presentation = presentations.find((value) => value.id === presentationId)
    setCurrentPresentation(presentation)
    setPresentationDialogOpen(true)
  };
  
  const handleDateChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedValue = event.target.value;
    if (selectedValue === '') {
      setCurrentDate('');
    } else {
      setCurrentDate(selectedValue);
    }
  };
  
  const handleTestButtonClick = async () => {
    testRestrictions()
    // setRestrictionDialogOpen(true)
  };

  //TODO Send email to all users
  const handleSendEmailButtonClick = async () => {
    setEmailSent(false);
    setIsSending(true);
    // console.log("userUnavailable: ", userUnavailable)
    // console.log("userUnsuitable: ", userUnsuitable)
    // console.log("presentationUnavailable: ", presentationUnavailable)
    // console.log("presentationUnsuitable: ", presentationUnsuitable)
    const token = sessionStorage.getItem('token');
    //TODO Check alternative
    axios.post(`http://localhost:8080/api/user/mail/sendAssignmentEmail`, {}, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    .then(() => {
      setEmailSent(true)
    })
    .catch((error) => {
      if (error.response?.status === 401) {
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('id');
        if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
          sessionStorage.setItem('sessionExpiredAlertShown', 'true');
          alert("Your session has expired. Please log in again if you want to continue.");
        }
        window.location.href = 'http://localhost:3000';
      } else if(error.response?.status == 422) {
        setError(error.response.data.message);
      } else {
        setError('There was an error on the server side, please try again later.');
        console.log(error.response)
      }
      setSuccess(false);
    })
    .finally(() => {
      setIsSending(false);
    });
  };

  const handleClosePresentationDialog = () => {
    setCurrentPresentation(undefined)
    setPresentationDialogOpen(false)
  };

  //TODO Test additional restrictions
  const checkUnavailability = (presentation: Presentation, slot: SlotDTO): boolean => {
    let presentationId = presentation?.id ?? -1;
    let adviserId = presentation?.adviserId ?? -1;
    let arguerId = presentation?.arguerId ?? -1;
    let optionalParticipant1Id = presentation?.optionalParticipant1Id ?? -1;
    let optionalParticipant2Id = presentation?.optionalParticipant2Id ?? -1;
    let roomId = presentation?.roomId ?? -1;
    let unavailable: Unavailability | undefined = undefined;
    unavailable = userUnavailable.find(((value) => value.slotId === Number(slot.id) && (value.id === adviserId || value.id === arguerId
      || value.id === optionalParticipant1Id || value.id === optionalParticipant2Id)))
    if (unavailable === undefined) {
      unavailable = presentationUnavailable.find(((value) => value.slotId === Number(slot.id) && value.id === presentationId))
      
      if (unavailable === undefined) {
        unavailable = roomUnavailable.find(((value) => value.slotId === Number(slot.id) && value.id === roomId));
      }
    }
    return unavailable !== undefined
  }

  //TODO Test additional restrictions
  const checkUnsuitability = (presentation: Presentation, slot: SlotDTO): boolean => {
    let presentationId = presentation?.id ?? -1;
    let adviserId = presentation?.adviserId ?? -1;
    let arguerId = presentation?.arguerId ?? -1;
    let optionalParticipant1Id = presentation?.optionalParticipant1Id ?? -1;
    let optionalParticipant2Id = presentation?.optionalParticipant2Id ?? -1;
    let roomId = presentation?.roomId ?? -1;
    let unsuitable: Unavailability | undefined = undefined;
    unsuitable = userUnsuitable.find(((value) => value.slotId === Number(slot.id) && (value.id === adviserId || value.id === arguerId
      || value.id === optionalParticipant1Id || value.id === optionalParticipant2Id)));
    if (unsuitable === undefined) {
      unsuitable = presentationUnsuitable.find(((value) => value.slotId === Number(slot.id) && value.id === presentationId));
      
      if (unsuitable === undefined) {
        unsuitable = roomUnsuitable.find(((value) => value.slotId === Number(slot.id) && value.id === roomId));
      }
    }
    return unsuitable !== undefined
  }

  //TODO Test additional restrictions
  const checkAdditionalUnavailability = (presentation: Presentation, slot: SlotDTO): boolean => {
    let presentationId = presentation?.id ?? -1;
    let adviserId = presentation?.adviserId ?? -1;
    let arguerId = presentation?.arguerId ?? -1;
    let optionalParticipant1Id = presentation?.optionalParticipant1Id ?? -1;
    let optionalParticipant2Id = presentation?.optionalParticipant2Id ?? -1;
    let roomId = presentation?.roomId ?? -1;
    let unavailable: Unavailability | undefined = undefined;
    unavailable = userAdditionalUnavailable.find(((value) => value.slotId === Number(slot.id) && (value.id === adviserId || value.id === arguerId
      || value.id === optionalParticipant1Id || value.id === optionalParticipant2Id)));
    if (unavailable === undefined) {
      unavailable = presentationAdditionalUnavailable.find(((value) => value.slotId === Number(slot.id) && value.id === presentationId))
      
      if (unavailable === undefined) {
        unavailable = roomUnavailable.find(((value) => value.slotId === Number(slot.id) && value.id === roomId));
      }
    }
    return unavailable !== undefined
  }

  //TODO Test additional restrictions
  const checkAdditionalUnsuitability = (presentation: Presentation, slot: SlotDTO): boolean => {
    let presentationId = presentation?.id ?? -1;
    let adviserId = presentation?.adviserId ?? -1;
    let arguerId = presentation?.arguerId ?? -1;
    let optionalParticipant1Id = presentation?.optionalParticipant1Id ?? -1;
    let optionalParticipant2Id = presentation?.optionalParticipant2Id ?? -1;
    let roomId = presentation?.roomId ?? -1;
    let unsuitable: Unavailability | undefined = undefined;
    unsuitable = userAdditionalUnsuitable.find(((value) => value.slotId === Number(slot.id) && (value.id === adviserId || value.id === arguerId
      || value.id === optionalParticipant1Id || value.id === optionalParticipant2Id)));
    if (unsuitable === undefined) {
      unsuitable = presentationAdditionalUnsuitable.find(((value) => value.slotId === Number(slot.id) && value.id === presentationId));
      
      if (unsuitable === undefined) {
        unsuitable = roomUnsuitable.find(((value) => value.slotId === Number(slot.id) && value.id === roomId));
      }
    }
    return unsuitable !== undefined
  }

  useEffect(() => {
    fetchUserRole();
    fetchUsersData();
    fetchRoomsData();
    fetchSlotsData();
    fetchPresentationsData();
    // fetchUnavailabilitiesData();
  }, []);
  
  useEffect(() => {
    changeUserPresentations(currentUser);
  }, [currentUser]);
  

  // useEffect(() => {
  //   updateCurrentPresentations();
  // }, [currentDate])

  //TODO Allow user to check certain solution
  return (
    <>
      <div className="container">
        <div className="header">
          <h2 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Current Assignment</h2>
          {(isComputing && (loggedInUserRole === 'ADMIN' || loggedInUserRole === 'ORGANIZER')) && (<InfoBox />)}
          <p>
            The tables below display the latest computed schedule, displaying the distribution of the presentations across the
            rooms and existing time slots.
            The presentations are represented by their ids, however you can click on these presentations in order to get more details.
          </p>
          <p>
            The tables are displayed in chronological order, however you can choose to see a specific day with the dropdown menu on the left.
          </p>
          <p>
            It is possible to choose a specific user on the dropdown menu on the right.
            This will highlight every presentation where the chosen user is an adviser, examiner or an extra participant.
          </p>
          {((loggedInUserRole === 'ADMIN' || loggedInUserRole === 'ORGANIZER')) && (
            <>
              <p>
                {"The \"Test restrictions\" button will test this schedule with the current restrictions, changing the color of the slots with presentations to the color of the corresponding restriction."}
              </p>
              <p>
                {"The \"Send assignment email\" button will send an email to every user stating that the computation process has finished."}
              </p>
            </>
          )}
        </div>

        {((loggedInUserRole === 'ADMIN' || loggedInUserRole === 'ORGANIZER')) && (
          <div className="flag-testing">
            <div className="buttons-container">
              <button className="test-button" onClick={handleTestButtonClick} disabled={isTesting}>
                {isTesting ? 'Testing...' : 'Test restrictions'}
              </button>
              <button className="send-email-button" onClick={handleSendEmailButtonClick} disabled={isSending}>
                {isSending ? 'Sending...' : 'Send assignment email'}
              </button>
            </div>
            {(hasTested && success) && (
              <div className="success-message">
                <FaCheckCircle className="icon" />
                All restrictions are satisfied by this assignment.
              </div>
            )}
            {(hasTested && onlyOptionalRestrictions && !success) && (
              <div className="optional-failure-message">
                <FaExclamationCircle className="icon" />
                Only optional restrictions are not satisfied by this assignment.
              </div>
            )}
            {(hasTested && !onlyOptionalRestrictions && !success) && (
              <div className="failure-message">
                <FaExclamationCircle className="icon" />
                Some restrictions are not satisfied by this assignment.
              </div>
            )}
            {error && (
              <div className="error-message">
                <FaExclamationCircle className="icon" />
                {error}
              </div>
            )}
          </div>
        )}

        <div className="input-division">
          <select id="date-select" className="input-field" value={currentDate || ''} onChange={handleDateChange}>
            <option value=''>All dates</option>
            {dates.sort((a, b) => formatDate(a).localeCompare(formatDate(b))).map(date => (
              <option key={date} value={date}>
                {date}
              </option>
            ))}
          </select>

          <select id="user-select" className="input-field" value={currentUser || 0} onChange={handleUserChange}>
            <option value="0">All users</option>
            {users.sort((a, b) => a.name.localeCompare(b.name)).map(user => (
              <option key={user.id} value={user.id}>
                {user.name}
              </option>
            ))}
          </select>
        </div>

        {dates.filter((date) => currentDate === '' ? true : date === currentDate).sort().map(date => {
          const tableDate = formatDate(date);
          return (
          <div key={tableDate} className="table-container" style={{ overflowX: 'auto' }}>
            {slots.length === 0 ? (
                <>
                  <div className="message" style={{ whiteSpace: 'pre-line'}}>
                      There are no slots.
                  </div>
                </>
              ) : (
                <>
                  <h3 className="day-header">Date: {tableDate}</h3>
                  <table className="slots-table">
                    <thead>
                      <tr>
                        <th className="hour-cell"></th>
                        {rooms.sort(compareRooms).map(room => {
                          return (
                          <th key={room.roomNr} className="room-cell">{formatRoom(room)}</th>
                        )})}
                      </tr>
                    </thead>
                    <tbody>
                      {hours.map(hour => {
                        const startHourParts = hour.split(':');
                        const startHour = `${startHourParts[0]}:${startHourParts[1]}`;
                        const finishHour = calculateFinishHour(hour);
                        const slot = slots.find(s => s.date === date && s.startingHour === hour);
                        return (
                          <tr key={`${tableDate}-${hour}`}>
                            <td className="hour-cell">{startHour} - {finishHour}</td>
                            {rooms.map(room => {
                              let presentation: Presentation | undefined;
                              let slotClass = '';
                              if (slot === undefined) {
                                presentation = undefined;
                                slotClass = 'slot-not-exists';
                              } else {
                                presentation = presentations.find(p => p.date === slot.date && p.startingHour === slot.startingHour
                                  && p.roomNumber === room.roomNr && p.roomBuilding === room.building);
                                slotClass = presentation ? 'slot-has-presentation' : 'slot-unassigned';
                              }
                              let presentationId = presentation?.id ?? -1
                              if (currentUser !== null) {
                                let filtered = userPresentations.find((value) => value.id === presentationId) ? true : false
                                slotClass = filtered ? slotClass + ' slot-filtered' : slotClass;
                              }
                              let unavailability = false;
                              let unsuitable = false;
                              let additionalUnavailability = false;
                              let additionalUnsuitable = false;
                              if (slot !== undefined && presentationId != -1) {
                                console.log("Check Unavailability");
                                unavailability = checkUnavailability(presentation!!, slot!!)
                                slotClass = unavailability ? slotClass + ' slot-current-unavailable' : slotClass
                                if (!unavailability) {
                                  console.log("Check Unsuitability");
                                  additionalUnavailability = checkAdditionalUnavailability(presentation!!, slot!!)
                                  slotClass = additionalUnavailability ? slotClass + ' slot-current-additional-unavailable' : slotClass
                                }
                                if (!(unavailability || additionalUnavailability)) {
                                  console.log("Check Unsuitability");
                                  unsuitable = checkUnsuitability(presentation!!, slot!!)
                                  slotClass = unsuitable ? slotClass + ' slot-current-unsuitable' : slotClass
                                  if (!unsuitable) {
                                    console.log("Check Unsuitability");
                                    additionalUnsuitable = checkAdditionalUnsuitability(presentation!!, slot!!)
                                    slotClass = additionalUnsuitable ? slotClass + ' slot-current-additional-unsuitable' : slotClass
                                  }
                                }
                              }
                              return (
                                <td
                                  key={`${tableDate}-${hour}-${room.building}-${room.roomNr}`}
                                  className={slotClass}
                                  onClick={(slot && presentation) ? () => handlePresentationClick(presentationId) : undefined}
                                >
                                  {(slot && presentation) ? (
                                    <div className="slot-content">
                                      <span>{presentationId}</span>
                                    </div>
                                  ) : '-'}
                                </td>
                              );
                            })}
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </>
              )
            }
          </div>
        )})}

        {/* {success && (
          <div className="">
            
          </div>
        )}
        {error && (
          <div className="">

          </div>
        )} */}
      </div>
      
      {/* Presentation details */}
      <Dialog
        open={presentationDialogOpen}
        onClose={handleClosePresentationDialog}
        maxWidth="xl"
        fullWidth={true}
      >
        <DialogTitle>Presentation information: {currentPresentation?.id}</DialogTitle>
        <DialogContent>
          <div className="presentation-table" style={{ overflowX: 'auto' }}>
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Student Number</th>
                    <th>Student</th>
                    <th>Thesis Title</th>
                    <th>Room</th>
                    <th>Building</th>
                    <th>Date</th>
                    <th>Starting Hour</th>
                    <th>Adviser</th>
                    <th>Examiner</th>
                    <th>Extra Participant</th>
                    <th>Extra Participant</th>
                  </tr>
                </thead>
                <tbody>
                  <tr >
                    <td>{currentPresentation?.id}</td>
                    <td>{currentPresentation?.studentNumber}</td>
                    <td>{currentPresentation?.studentName}</td>
                    <td>{currentPresentation?.thesisTitle}</td>
                    <td>{currentPresentation?.roomNumber ?? 'Not assigned yet'}</td>
                    <td>{currentPresentation?.roomBuilding ?? 'Not assigned yet'}</td>
                    <td>{currentPresentation?.date ?? 'Not assigned yet'}</td>
                    <td>{formatStartingHour(currentPresentation?.startingHour)}</td>
                    <td>{currentPresentation?.adviserName}</td>
                    <td>{currentPresentation?.arguerName}</td>
                    <td>{currentPresentation?.optionalParticipant1Name ?? ''}</td>
                    <td>{currentPresentation?.optionalParticipant2Name ?? ''}</td>
                  </tr>
                </tbody>
              </table>
          </div>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClosePresentationDialog} color="primary">
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </>

  );
};

export default Page;
