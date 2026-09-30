"use client";

import ComputationWarning from '@/app/components/computationWarning/component';
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
  optionalParticipant1Name: string | null;
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
  // itemId: number;
  slotId: number;
  // slotsId: number[];
}

// interface Unavailabilities {
//   id: number;
//   itemId: number;
//   slotsId: number[];
// }

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

interface UserUnavailabilitiesDTO {
  first: UserUnavailabilityDTO;
  second: UserUnavailabilityDTO;
}

interface PresentationUnavailabilitiesDTO {
  first: PresentationUnavailabilityDTO;
  second: PresentationUnavailabilityDTO;
}

interface ChangeAdditionalUnavailabilitiesDTO {
    // userID: number;
    userAdditionalUnavailabilities: Map<number, number[]>;
    userAdditionalUnsuitableSlots: Map<number, number[]>;
    presentationAdditionalUnavailabilities: Map<number, number[]>;
    presentationAdditionalUnsuitableSlots: Map<number, number[]>;
}

const COMPUTATION_IN_PROGRESS = "There is a computation in progress."

const Page: React.FC = () => {
  const [isComputing, setIsComputing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [selectedRestrictionUserId, setSelectedRestrictionUserId] = useState<number | null>(null);
  const [selectedRestrictionPresentationId, setSelectedRestrictionPresentationId] = useState<number | null>(null);
  
  const [isUserSlotLoading, setIsUserSlotLoading] = useState<number[]>([]);
  const [isPresentationSlotLoading, setIsPresentationSlotLoading] = useState<number[]>([]);

  const [userDateUnavailability, setUserDateUnavailability] = useState<string[]>([]);
  const [userDateUnsuitable, setUserDateUnsuitable] = useState<string[]>([]);
  const [userHourUnavailability, setUserHourUnavailability] = useState<string[]>([]);
  const [userHourUnsuitable, setUserHourUnsuitable] = useState<string[]>([]);

  const [presentationDateUnavailability, setPresentationDateUnavailability] = useState<string[]>([]);
  const [presentationDateUnsuitable, setPresentationDateUnsuitable] = useState<string[]>([]);
  const [presentationHourUnavailability, setPresentationHourUnavailability] = useState<string[]>([]);
  const [presentationHourUnsuitable, setPresentationHourUnsuitable] = useState<string[]>([]);

  const [loggedInUserRole, setLoggedInUserRole] = useState<String>('');
  const [slots, setSlots] = useState<SlotDTO[]>([]);
  const [dates, setDates] = useState<string[]>([]);
  const [hours, setHours] = useState<string[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [presentations, setPresentations] = useState<Presentation[]>([]);

  const [userUnavailable, setUserUnavailable] = useState<Unavailability[]>([]);
  const [userUnsuitable, setUserUnsuitable] = useState<Unavailability[]>([]);
  const [presentationUnavailable, setPresentationUnavailable] = useState<Unavailability[]>([]);
  const [presentationUnsuitable, setPresentationUnsuitable] = useState<Unavailability[]>([]);

  const [userAdditionalUnavailable, setUserAdditionalUnavailable] = useState<number[]>([]);
  const [userAdditionalUnsuitable, setUserAdditionalUnsuitable] = useState<number[]>([]);
  const [presentationAdditionalUnavailable, setPresentationAdditionalUnavailable] = useState<number[]>([]);
  const [presentationAdditionalUnsuitable, setPresentationAdditionalUnsuitable] = useState<number[]>([]);

  useEffect(() => {
    const checkComputation = async () => {
      try {
        const token = sessionStorage.getItem('token');
        //TODO New HTTP request to check computationstate
        // const response = await axios.get(`http://localhost:8080/api/python/checkComputation`, {},{
        const response = await axios.get(`http://localhost:8080/api/python/checkComputation`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        //TODO Obtain data from response
        // const state = response.data
        if (response.data === true) {
          //TODO Computation in progress
          // setIstesting(true);
          setIsComputing(true);
          console.log("There is already an assignment being computed, try again later.")
        }
      } catch (error) {
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
          } else if (axiosError.response?.status == 422) {
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
      // console.log("response.data.role: ", response.data.role)
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

  //TODO Show all unavailabilities
  const fetchUnavailabilitiesData = async () => {
    const token = sessionStorage.getItem('token');
    try{
      const response1 = await axios.get<UserUnavailabilityDTO[]>('http://localhost:8080/api/unavailability', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      
      const response2 = await axios.get<PresentationUnavailabilityDTO[]>('http://localhost:8080/api/unavailability/presentation', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      // setPresentations(response.data);
      //TODO Set unavailabilities

      const userUnavailabilities = response1.data;
      const presentationUnavailabilties = response2.data;

      console.log("userUnavailabilities: ", userUnavailabilities)
      console.log("presentationUnavailabilties: ", presentationUnavailabilties)

      const userUnavailableList : Unavailability[] = []
      const userUnsuitableList : Unavailability[] = []
      for (const userUnavailability of userUnavailabilities) {
        const id = userUnavailability["id"];
        const userId = userUnavailability["userID"];
        const slots = userUnavailability["slotsID"];
        if (userUnavailability["unsuitable"] === false) {
          for (const slotId of slots) {
            userUnavailableList.push({id: userId, slotId: slotId})
          }
        } else  {
          for (const slotId of slots) {
            userUnsuitableList.push({id: userId, slotId: slotId})
          }
        }
      }
      
      const presentationUnavailableList : Unavailability[] = []
      const presentationUnsuitableList : Unavailability[] = []
      for (const presentationUnavailability of presentationUnavailabilties) {
        const id = presentationUnavailability["id"];
        const presentationId = presentationUnavailability["presentationID"];
        const slots = presentationUnavailability["slotsID"];
        if (presentationUnavailability["unsuitable"] === false) {
          for (const slotId of slots) {
            presentationUnavailableList.push({id: presentationId, slotId: slotId})
          }
        } else {
          for (const slotId of slots) {
            presentationUnsuitableList.push({id: presentationId, slotId: slotId})
          }
        }
      }

      //TODO Add additional restrictions
      setUserUnavailable(userUnavailableList)
      setUserUnsuitable(userUnsuitableList)
      setPresentationUnavailable(presentationUnavailableList)
      setPresentationUnsuitable(presentationUnsuitableList)
    } catch(error) {
      console.error('Error fetching presentations: ', error);
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

  //TODO Check functionality
  const fetchAdditionalUserUnavailabilitySlots = async (userId: number | null) => {
    if (userId === null) {
      setUserAdditionalUnavailable([])
      setUserAdditionalUnsuitable([])
      return
    }

    try {
      const token = sessionStorage.getItem('token');
      // const userId = sessionStorage.getItem('id');
      const response = await axios.get<UserUnavailabilitiesDTO>(`http://localhost:8080/api/unavailability/additional/user/${userId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const data = response.data;
      const unavailabilityData = data.first;
      const unsuitableData = data.second;
      setUserAdditionalUnavailable(unavailabilityData.slotsID)
      setUserAdditionalUnsuitable(unsuitableData.slotsID)
      // if (selectedRestrictionUserId === userId) {
      //   setUserAdditionalUnavailable(unavailabilityData.slotsID)
      //   setUserAdditionalUnsuitable(unsuitableData.slotsID)
      // }
      // const [selectedRestrictionUserId, setSelectedRestrictionUserId] = useState<number | null>(null);

      // setUserUnavailabilitySlots(unavailabilityData.slotsID);
      // setUserUnsuitableSlots(unsuitableData.slotsID);
    } catch(error) {
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

  //TODO Check functionality
  const fetchAdditionalPresentationUnavailabilitySlots = async (presentationId: number | null) => {
    if (presentationId === null) {
      setPresentationAdditionalUnavailable([])
      setPresentationAdditionalUnsuitable([])
      return
    }

    try {
      const token = sessionStorage.getItem('token');
      // const userId = sessionStorage.getItem('id');
      //TODO Check if "axios.get<PresentationUnavailabilitiesDTO>" is correct
      const response = await axios.get<PresentationUnavailabilitiesDTO>(`http://localhost:8080/api/unavailability/additional/presentation/${presentationId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const data = response.data;
      const presentationUnavailabilityData = data.first;
      const presentationUnsuitableData = data.second;
      setPresentationAdditionalUnavailable(presentationUnavailabilityData.slotsID)
      setPresentationAdditionalUnsuitable(presentationUnsuitableData.slotsID)
      // if (selectedRestrictionPresentationId === presentationId) {
      //   setPresentationAdditionalUnavailable(presentationUnavailabilityData.slotsID)
      //   setPresentationAdditionalUnsuitable(presentationUnsuitableData.slotsID)
      // }
      // setPresentationAdditionalUnavailable(
      //   presentationAdditionalUnavailable.filter((value) =>
      //     value.id !== parseInt(slotID)
      //   )
      // );
      // setPresentationAdditionalUnsuitable(
      //   presentationAdditionalUnsuitable.filter((value) =>
      //     value.id !== parseInt(slotID)
      //   )
      // );
      //TODO Change specific slots
      // setUserUnavailabilitySlots(unavailabilityData.slotsID);
      // setUserUnsuitableSlots(unsuitableData.slotsID);
    } catch(error) {
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

  const formatDate = (date: string): string => {
    const [year, month, day] = date.split('-');
    return `${day}-${month}-${year}`;
  };

  const calculateFinishHour = (startingHour: string): string => {
    const [hour, minute] = startingHour.split(':').map(parseFloat);
    const finishHour = hour + (minute + 45) / 60;
    const finishHourHour = Math.floor(finishHour);
    const finishHourMinute = Math.round((finishHour - finishHourHour) * 60);
    return `${finishHourHour.toString().padStart(2, '0')}:${finishHourMinute.toString().padStart(2, '0')}`;
  };

  const mapAdditionalRestrictionState = (restrictionState: Unavailability[]) : Map<number, number[]> => {
    const newMap = new Map<number, number[]>();
    for (const restriction of restrictionState) {
      const key = restriction.id
      const value = restriction.slotId
      if (newMap.has(key) === false) {
        newMap.set(key, [])
      }
      const slotArray = newMap.get(restriction.id)
      slotArray?.push(value)
      newMap.set(key, slotArray!!)
    }
    return newMap;
  }

  //TODO Send message to user when computation starts
  const handleRestrictionPresentationChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedValue = parseInt(event.target.value);
    setSelectedRestrictionPresentationId(selectedValue === 0 ? null : selectedValue);
    fetchAdditionalPresentationUnavailabilitySlots(selectedValue === 0 ? null : selectedValue)
    setIsPresentationSlotLoading([])
    setPresentationDateUnavailability([])
    setPresentationDateUnsuitable([])
    setPresentationHourUnavailability([])
    setPresentationHourUnsuitable([])
  };
  
  const handleRestrictionUserChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedValue = parseInt(event.target.value);
    setSelectedRestrictionUserId(selectedValue === 0 ? null : selectedValue);
    fetchAdditionalUserUnavailabilitySlots(selectedValue === 0 ? null : selectedValue)
    setIsUserSlotLoading([])
    setUserDateUnavailability([])
    setUserDateUnsuitable([])
    setUserHourUnavailability([])
    setUserHourUnsuitable([])
  };
  
  const handleRestrictionPresentationSlotClick = async (presentationId: number | null, slotId: number) => {
    if (presentationId === null) {
      return
    }
    console.log("presentationId", presentationId);
    console.log("slotId", slotId);
    try {
      const token = sessionStorage.getItem('token');
      // const userId = sessionStorage.getItem('id');
      setIsPresentationSlotLoading([...isPresentationSlotLoading, slotId])
      if (isPresentationSlotAdditionalUnavailable(slotId)) {
        //TODO Test this feature
        console.log("isPresentationSlotAdditionalUnavailable");
        await axios.delete('http://localhost:8080/api/unavailability/additional/presentation', {
          data: { slotID: slotId, presentationID: presentationId } ,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
      } else if (isPresentationSlotAdditionalUnsuitable(slotId)) {
        //TODO Test this feature
        console.log("isPresentationSlotAdditionalUnsuitable");
        // setIsPresentationSlotLoading([...isPresentationSlotLoading, {id: presentationId, slotId: slotId}])
        await axios.put('http://localhost:8080/api/unavailability/additional/presentation', {
          slotID: slotId,
          presentationID: presentationId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
      } else {
        //TODO Test this feature
        console.log("!isPresentationSlotAdditionalUnsuitable");
        // setIsPresentationSlotLoading([...isPresentationSlotLoading, {id: presentationId, slotId: slotId}])
        await axios.post('http://localhost:8080/api/unavailability/additional/presentation', {
          slotID: slotId,
          presentationID: presentationId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
      }
      await fetchAdditionalPresentationUnavailabilitySlots(presentationId); // Refresh user unavailability slots after toggle
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
        } else if (axiosError.response?.status === 409) {
          // const errorMessage = axiosError.response?.data.message
          const errorMessage = axiosError.response?.data
          console.log("errorMessage: ", errorMessage)
          if (errorMessage == COMPUTATION_IN_PROGRESS) {
            setIsComputing(true);
            setError(COMPUTATION_IN_PROGRESS);
          } else {
            setError('There was an error on the server side, please try again later.');
          }
        } else {
          setError('There was an error on the server side, please try again later.');
        }
      } else {
        setError('There was an error on the server side, please try again later.');
      }
    } finally {
      setIsPresentationSlotLoading(
        isPresentationSlotLoading.filter((value) =>
          value !== slotId
        )
      );
    }
  };

  const handleRestrictionUserSlotClick = async (userId: number | null, slotId: number) => {
    if (userId === null) {
      return
    }
    console.log("userId", userId);
    console.log("slotId", slotId);
    try {
      const token = sessionStorage.getItem('token');
      setIsUserSlotLoading([...isUserSlotLoading, slotId])
      if (isUserSlotAdditionalUnavailable(slotId)) {
        console.log("isPresentationSlotAdditionalUnavailable");
        await axios.delete('http://localhost:8080/api/unavailability/additional', {
          data: { slotID: slotId, userID: userId } ,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
      } else if (isUserSlotAdditionalUnsuitable(slotId)) {
        console.log("isPresentationSlotAdditionalUnsuitable");
        await axios.put('http://localhost:8080/api/unavailability/additional', {
          slotID: slotId,
          userID: userId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
      } else {
        //TODO Test this feature
        console.log("!isPresentationSlotAdditionalUnsuitable");
        // setIsPresentationSlotLoading([...isPresentationSlotLoading, {id: presentationId, slotId: slotId}])
        await axios.post('http://localhost:8080/api/unavailability/additional', {
          slotID: slotId,
          userID: userId
          },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
      }
      await fetchAdditionalUserUnavailabilitySlots(userId); // Refresh user unavailability slots after toggle
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
        } else if (axiosError.response?.status === 409) {
          // const errorMessage = axiosError.response?.data.message
          const errorMessage = axiosError.response?.data
          console.log("errorMessage: ", errorMessage)
          if (errorMessage == COMPUTATION_IN_PROGRESS) {
            setIsComputing(true);
            setError(COMPUTATION_IN_PROGRESS);
          } else {
            setError('There was an error on the server side, please try again later.');
          }
        } else {
          setError('There was an error on the server side, please try again later.');
        }
      } else {
        setError('There was an error on the server side, please try again later.');
      }
    } finally {
      setIsUserSlotLoading(
        isUserSlotLoading.filter((value) =>
          value !== slotId
        )
      );
    }
  };

  const togglePresentationDateAvailability = async (presentationId: number | null, date: string) => {
    //TODO Set loading for the group
    const slotGroup = slots.filter((value) => value.date === date)
    const slotGroupIDs = slotGroup.map((value) => parseInt(value.id))
    // console.log("dateUnsuitable: ", dateUnsuitable)
    // console.log("dateUnavailability: ", dateUnavailability)
    // console.log("dateUnsuitable.includes(date): ", dateUnsuitable.includes(date))
    // console.log("dateUnavailability.includes(date): ", dateUnavailability.includes(date))
    try {
      //TODO Check if slot loading before changes
      const token = sessionStorage.getItem('token');
      console.log("slotGroup: ", slotGroup)
      console.log("presentationDateUnsuitable: ", presentationDateUnsuitable)
      console.log("presentationDateUnavailability: ", presentationDateUnavailability)
      setIsPresentationSlotLoading(isPresentationSlotLoading.concat(slotGroupIDs))
      if (presentationDateUnsuitable.includes(date)) {
        // console.log("presentationDateUnsuitable")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.put('http://localhost:8080/api/unavailability/additional/presentation/groupSlot', {
          slotsID: slotGroupIDs,
          presentationID: presentationId,
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
        setPresentationDateUnsuitable(presentationDateUnsuitable.filter(value => value !== date))
        setPresentationDateUnavailability([...presentationDateUnavailability, date])
      } else if (presentationDateUnavailability.includes(date)) {
        // console.log("presentationDateUnavailability")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.delete('http://localhost:8080/api/unavailability/additional/presentation/groupSlot', {
          data: { slotsID: slotGroupIDs, presentationID: presentationId } ,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
        // setPresentationDateUnsuitable(presentationDateUnsuitable.filter(value => value === date))
        setPresentationDateUnavailability(presentationDateUnavailability.filter(value => value !== date))
      } else {
        // console.log("!presentationDateUnsuitable")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.post('http://localhost:8080/api/unavailability/additional/presentation/groupSlot', {
          slotsID: slotGroupIDs,
          presentationID: presentationId,
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
        setPresentationDateUnsuitable([...presentationDateUnsuitable, date])
        // setPresentationDateUnavailability([...presentationDateUnavailability, date])
      }
      await fetchAdditionalPresentationUnavailabilitySlots(presentationId); // Refresh user unavailability slots after toggle
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
        } else if (axiosError.response?.status === 409) {
          // const errorMessage = axiosError.response?.data.message
          const errorMessage = axiosError.response?.data
          console.log("errorMessage: ", errorMessage)
          if (errorMessage == COMPUTATION_IN_PROGRESS) {
            setIsComputing(true);
            setError(COMPUTATION_IN_PROGRESS);
          } else {
            setError('There was an error on the server side, please try again later.');
          }
        } else {
          setError('There was an error on the server side, please try again later.');
        }
      } else {
        setError('There was an error on the server side, please try again later.');
      }
    } finally {
      setIsPresentationSlotLoading(
        isPresentationSlotLoading.filter(value =>
          !slotGroupIDs.includes(value)
        )
      );
    }
  };
  
  const togglePresentationHourAvailability = async (presentationId: number | null, startingHour: string) => {
    //TODO Set loading for the group
    const slotGroup = slots.filter((value) => value.startingHour === startingHour)
    const slotGroupIDs = slotGroup.map((value) => parseInt(value.id))
    // console.log("dateUnsuitable: ", dateUnsuitable)
    // console.log("dateUnavailability: ", dateUnavailability)
    // console.log("dateUnsuitable.includes(date): ", dateUnsuitable.includes(date))
    // console.log("dateUnavailability.includes(date): ", dateUnavailability.includes(date))
    try {
      const token = sessionStorage.getItem('token');
      console.log("slotGroup: ", slotGroup)
      console.log("presentationHourUnsuitable: ", presentationHourUnsuitable)
      console.log("presentationHourUnavailability: ", presentationHourUnavailability)
      setIsPresentationSlotLoading(isPresentationSlotLoading.concat(slotGroupIDs))
      if (presentationHourUnsuitable.includes(startingHour)) {
        // console.log("presentationHourUnsuitable")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.put('http://localhost:8080/api/unavailability/additional/presentation/groupSlot', {
          slotsID: slotGroupIDs,
          presentationID: presentationId,
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
        setPresentationHourUnsuitable(presentationHourUnsuitable.filter(value => value !== startingHour))
        setPresentationHourUnavailability([...presentationHourUnavailability, startingHour])
      } else if (presentationHourUnavailability.includes(startingHour)) {
        // console.log("presentationHourUnavailability")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.delete('http://localhost:8080/api/unavailability/additional/presentation/groupSlot', {
          data: { slotsID: slotGroupIDs, presentationID: presentationId } ,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
        // setPresentationHourUnsuitable(presentationHourUnsuitable.filter(value => value !== startingHour))
        setPresentationHourUnavailability(presentationHourUnavailability.filter(value => value !== startingHour))
      } else {
        // console.log("!presentationHourUnsuitable")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.post('http://localhost:8080/api/unavailability/additional/presentation/groupSlot', {
          slotsID: slotGroupIDs,
          presentationID: presentationId,
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
        setPresentationHourUnsuitable([...presentationHourUnsuitable, startingHour])
        // setPresentationHourUnavailability([...presentationHourUnavailability, startingHour])
      }
      await fetchAdditionalPresentationUnavailabilitySlots(presentationId); // Refresh user unavailability slots after toggle
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
        } else if (axiosError.response?.status === 409) {
          // const errorMessage = axiosError.response?.data.message
          const errorMessage = axiosError.response?.data
          console.log("errorMessage: ", errorMessage)
          if (errorMessage == COMPUTATION_IN_PROGRESS) {
            setIsComputing(true);
            setError(COMPUTATION_IN_PROGRESS);
          } else {
            setError('There was an error on the server side, please try again later.');
          }
        } else {
          //TODO Add error
          setError('There was an error on the server side, please try again later.');
        }
      } else {
        setError('There was an error on the server side, please try again later.');
      }
    } finally {
      setIsPresentationSlotLoading(
        isPresentationSlotLoading.filter(value =>
          !slotGroupIDs.includes(value)
        )
      );
    }
  };

  const toggleUserDateAvailability = async (userId: number | null, date: string) => {
    //TODO Set loading for the group
    const slotGroup = slots.filter((value) => value.date === date)
    const slotGroupIDs = slotGroup.map((value) => parseInt(value.id))
    // console.log("dateUnsuitable: ", dateUnsuitable)
    // console.log("dateUnavailability: ", dateUnavailability)
    // console.log("dateUnsuitable.includes(date): ", dateUnsuitable.includes(date))
    // console.log("dateUnavailability.includes(date): ", dateUnavailability.includes(date))
    try {
      //TODO Check if slot loading before changes
      const token = sessionStorage.getItem('token');
      // console.log("slotGroup: ", slotGroup)
      // setIsUserSlotLoading([...setIsUserSlotLoading, ])
      console.log("slotGroup: ", slotGroup)
      console.log("userDateUnsuitable: ", userDateUnsuitable)
      console.log("userDateUnavailability: ", userDateUnavailability)
      setIsUserSlotLoading(isUserSlotLoading.concat(slotGroupIDs))
      if (userDateUnsuitable.includes(date)) {
        // console.log("dateUnsuitable")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.put('http://localhost:8080/api/unavailability/additional/groupSlot', {
          slotsID: slotGroupIDs,
          userID: userId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
        setUserDateUnsuitable(userDateUnsuitable.filter(value => value !== date))
        setUserDateUnavailability([...userDateUnavailability, date])
      } else if (userDateUnavailability.includes(date)) {
        // console.log("userDateUnavailability")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.delete('http://localhost:8080/api/unavailability/additional/groupSlot', {
          data: { slotsID: slotGroupIDs, userID: userId } ,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
        // setUserDateUnsuitable(userDateUnsuitable.filter(value => value === date))
        setUserDateUnavailability(userDateUnavailability.filter(value => value !== date))
      } else {
        // console.log("!dateUserUnsuitable")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.post('http://localhost:8080/api/unavailability/additional/groupSlot', {
          slotsID: slotGroupIDs,
          userID: userId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
        setUserDateUnsuitable([...userDateUnsuitable, date])
        // setUserDateUnavailability([...userDateUnavailability, date])
      }
      await fetchAdditionalUserUnavailabilitySlots(userId); // Refresh user unavailability slots after toggle
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
        } else if (axiosError.response?.status === 409) {
          // const errorMessage = axiosError.response?.data.message
          const errorMessage = axiosError.response?.data
          console.log("errorMessage: ", errorMessage)
          if (errorMessage == COMPUTATION_IN_PROGRESS) {
            setIsComputing(true);
            setError(COMPUTATION_IN_PROGRESS);
          } else {
            setError('There was an error on the server side, please try again later.');
          }
        } else {
          //TODO Add error
          setError('There was an error on the server side, please try again later.');
        }
      } else {
        setError('There was an error on the server side, please try again later.');
      }
    } finally {
      setIsUserSlotLoading(
        isUserSlotLoading.filter(value =>
          !slotGroupIDs.includes(value)
        )
      );
    }
  };

  const toggleUserHourAvailability = async (userId: number | null, startingHour: string) => {
    //TODO Set loading for the group
    const slotGroup = slots.filter((value) => value.startingHour === startingHour)
    const slotGroupIDs = slotGroup.map((value) => parseInt(value.id))
    // console.log("dateUnsuitable: ", dateUnsuitable)
    // console.log("dateUnavailability: ", dateUnavailability)
    // console.log("dateUnsuitable.includes(date): ", dateUnsuitable.includes(date))
    // console.log("dateUnavailability.includes(date): ", dateUnavailability.includes(date))
    try {
      //TODO Check if slot loading before changes
      const token = sessionStorage.getItem('token');
      // console.log("slotGroup: ", slotGroup)
      // setIsUserSlotLoading([...setIsUserSlotLoading, ])
      console.log("slotGroup: ", slotGroup)
      console.log("userHourUnsuitable: ", userHourUnsuitable)
      console.log("userHourUnavailability: ", userHourUnavailability)
      setIsUserSlotLoading(isUserSlotLoading.concat(slotGroupIDs))
      if (userHourUnsuitable.includes(startingHour)) {
        // console.log("userHourUnsuitable")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.put('http://localhost:8080/api/unavailability/additional/groupSlot', {
          slotsID: slotGroupIDs,
          userID: userId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
        setUserHourUnsuitable(userHourUnsuitable.filter(value => value !== startingHour))
        setUserHourUnavailability([...userHourUnavailability, startingHour])
      } else if (userHourUnavailability.includes(startingHour)) {
        // console.log("userHourUnavailability")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.delete('http://localhost:8080/api/unavailability/additional/groupSlot', {
          data: { slotsID: slotGroupIDs, userID: userId } ,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
        // setUserHourUnsuitable(userHourUnsuitable.filter(value => value !== startingHour))
        setUserHourUnavailability(userHourUnavailability.filter(value => value !== startingHour))
      } else {
        // console.log("!userHourUnsuitable")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.post('http://localhost:8080/api/unavailability/additional/groupSlot', {
          slotsID: slotGroupIDs,
          userID: userId,
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // })
        // .then(() => {
        //   //TODO Test this feature
        //   setIsSlotLoading(
        //     isSlotLoading.filter(value =>
        //       value !== parseInt(slotID)
        //     )
        //   );
        // });
        setUserHourUnsuitable([...userHourUnsuitable, startingHour])
        // setUserHourUnavailability([...userHourUnavailability, startingHour])
      }
      await fetchAdditionalUserUnavailabilitySlots(userId); // Refresh user unavailability slots after toggle
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
        } else if (axiosError.response?.status === 409) {
          // const errorMessage = axiosError.response?.data.message
          const errorMessage = axiosError.response?.data
          console.log("errorMessage: ", errorMessage)
          if (errorMessage == COMPUTATION_IN_PROGRESS) {
            setIsComputing(true);
            setError(COMPUTATION_IN_PROGRESS);
          } else {
            setError('There was an error on the server side, please try again later.');
          }
        } else {
          setError('There was an error on the server side, please try again later.');
        }
      } else {
        setError('There was an error on the server side, please try again later.');
      }
    } finally {
      setIsUserSlotLoading(
        isUserSlotLoading.filter(value =>
          !slotGroupIDs.includes(value)
        )
      );
    }
  };
  
  const isPresentationSlotUnavailable = (presentationId: number | null, slotId: number): boolean => {
    if (presentationId === null) {
      return false
    }
    // console.log("isPresentationSlotUnavailable?.includes({id: presentationId, slotId: slotId}) ?? false: ", presentationUnavailable?.includes({id: presentationId, slotId: slotId}) ?? false)
    const res = presentationUnavailable?.find(value => value.id === presentationId && value.slotId === slotId)
    return res !== undefined;
  };

  const isPresentationSlotUnsuitable = (presentationId: number | null, slotId: number): boolean => {
    if (presentationId === null) {
      return false
    }
    // console.log("presentationUnsuitable?.includes({id: presentationId, slotId: slotId}) ?? false: ", presentationUnsuitable?.includes({id: presentationId, slotId: slotId}) ?? false)
    const res = presentationUnsuitable?.find(value => value.id === presentationId && value.slotId === slotId)
    return res !== undefined;
  };

  const isPresentationSlotAdditionalUnavailable = (slotId: number): boolean => {
    const res = presentationAdditionalUnavailable?.find(value => value === slotId)
    return res !== undefined;
  };

  const isPresentationSlotAdditionalUnsuitable = (slotId: number): boolean => {
    const res = presentationAdditionalUnsuitable?.find(value => value === slotId)
    return res !== undefined;
  };

  const isUserSlotUnavailable = (userId: number | null, slotId: number): boolean => {
    if (userId === null) {
      return false
    }
    const res = userUnavailable?.find(value => value.id === userId && value.slotId === slotId)
    return res !== undefined;
  };

  const isUserSlotUnsuitable = (userId: number | null, slotId: number): boolean => {
    if (userId === null) {
      return false
    }
    const res = userUnsuitable?.find(value => value.id === userId && value.slotId === slotId)
    return res !== undefined;
  };
  
  const isUserSlotAdditionalUnavailable = (slotId: number): boolean => {
    const res = userAdditionalUnavailable?.find(value => value === slotId)
    return res !== undefined;
  };

  const isUserSlotAdditionalUnsuitable = (slotId: number): boolean => {
    const res = userAdditionalUnsuitable?.find(value => value === slotId)
    return res !== undefined;
  };

  useEffect(() => {
    fetchUserRole();
    fetchUsersData();
    fetchSlotsData();
    fetchPresentationsData();
    fetchUnavailabilitiesData();
  }, []);

  return (
    <>
      <div className="container">
        <div className="header">
          <h2 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Additional Restrictions</h2>
          {(isComputing && (loggedInUserRole === 'ADMIN' || loggedInUserRole === 'ORGANIZER')) && (<ComputationWarning />)}
          <p>
            Here you can set additional restrictions on users and presentations by clicking on the slots, after choosing the desired user or presentation.
            You can also click the date and hour cells at the headers of the columns and rows of the tables, respectively, to change additional restrictions across the column/row.
          </p>
          <p>
            Alongside the additional restrictions, the tables below will display the restrictions added by the users, if no additional restrictions have been set on that slot.
          </p>
          <p>
            The additional restrictions can have the following colors:
          </p>
          <ol>
            <li>{"Dark Yellow Slot -> The slot has an additional optional restriction."}</li>
            <li>{"Dark Red Slot -> The slot has an additional obligatory restriction."}</li>
          </ol>
        </div>

        <div className="user-unavailability-table" style={{ overflowX: 'auto' }}>
          <div className="user-input-division">
            <select id="user-select" className="user-input-field" value={selectedRestrictionUserId || 0} onChange={handleRestrictionUserChange}>
              <option value="0">Select one user</option>
              {users.sort((a, b) => a.name.localeCompare(b.name)).map(user => (
                <option key={user.id} value={user.id}>
                  {user.name}
                </option>
              ))}
            </select>
          </div>
          <table className="user-unavailability-slots-table">
            <thead>
              <tr>
                <th className="hour-cell"></th>
                {dates.sort().map(date => (
                  <th
                    key={`user-${date}`}
                    className="date-cell"
                    onClick={() => {
                      if (!isComputing) {
                        toggleUserDateAvailability(selectedRestrictionUserId, date);
                      }
                    }}
                  >
                    {formatDate(date)}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {hours.map(hour => {
                const startHourParts = hour.split(':');
                const startHour = `${startHourParts[0]}:${startHourParts[1]}`;
                const finishHour = calculateFinishHour(hour);
                return (
                  <tr key={hour}>
                    <td 
                      className="hour-cell"
                      onClick={() => {
                        if (!isComputing) {
                          toggleUserHourAvailability(selectedRestrictionUserId, hour);
                        }
                      }}
                    >
                      {startHour} - {finishHour}
                    </td>
                    {dates.sort().map(date => {
                      const slot = slots.find(s => s.date === date && s.startingHour === hour);
                      const slotId = slot?.id ?? '';
                      let slotClassName = 'slot-not-exists'; // Default to slot-not-exists
                      if (slot) {
                        // Slot exists, apply user unavailability logic if a user is selected
                        if (selectedRestrictionUserId && isUserSlotUnavailable(selectedRestrictionUserId, Number(slotId))) {
                          slotClassName = 'slot-unavailable';
                        } else if (selectedRestrictionUserId && isUserSlotUnsuitable(selectedRestrictionUserId, Number(slotId))) {
                          slotClassName = 'slot-unsuitable';
                        } else {
                          slotClassName = 'slot-exists';
                        }
                        if (selectedRestrictionUserId && isUserSlotAdditionalUnavailable(Number(slotId))) {
                          slotClassName = slotClassName + ' slot-additional-unavailable';
                        } else if (selectedRestrictionUserId && isUserSlotAdditionalUnsuitable(Number(slotId))) {
                          slotClassName = slotClassName + ' slot-additional-unsuitable';
                        }
                      }
                      console.log("slotClassName: ", slotClassName);
                      return (
                        <td
                          key={`user-${date}-${hour}`}
                          className={slotClassName}
                          onClick={(slot && !isComputing) ? () => handleRestrictionUserSlotClick(selectedRestrictionUserId, Number(slotId)) : undefined} // Handle slot click
                        >
                          {slot && <div className="slot-content"></div>}
                        </td>
                      );
                    })}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        <div className="presentation-unavailability-table" style={{ overflowX: 'auto' }}>
          <div className="presentation-input-division">
            <select id="presentation-select" className="presentation-input-field" value={selectedRestrictionPresentationId || 0} onChange={handleRestrictionPresentationChange}>
              <option value="0">Select one presentation</option>
              {presentations.sort((a, b) => a.id - b.id).map(presentation => (
                <option key={presentation.id} value={presentation.id}>
                    {presentation.thesisTitle}
                </option>
              ))}
            </select>
          </div>
          <table className="presentation-unavailability-slots-table">
            <thead>
              <tr>
                <th className="hour-cell"></th>
                {dates.sort().map(date => (
                  <th
                    key={`presentation-${date}`}
                    className="date-cell"
                    onClick={() => {
                      if (!isComputing) {
                        togglePresentationDateAvailability(selectedRestrictionPresentationId, date);
                      }
                    }}
                  >
                    {formatDate(date)}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {hours.map(hour => {
                const startHourParts = hour.split(':');
                const startHour = `${startHourParts[0]}:${startHourParts[1]}`;
                const finishHour = calculateFinishHour(hour);
                return (
                  <tr key={hour}>
                    <td
                      className="hour-cell"
                      onClick={() => {
                        if (!isComputing) {
                          togglePresentationHourAvailability(selectedRestrictionPresentationId, hour);
                        }
                      }}
                    >
                      {startHour} - {finishHour}
                    </td>
                    {dates.sort().map(date => {
                      const slot = slots.find(s => s.date === date && s.startingHour === hour);
                      const slotId = slot?.id ?? '';
                      let slotClassName = '';
                      if (slot) {
                        if (selectedRestrictionPresentationId && isPresentationSlotUnavailable(selectedRestrictionPresentationId, Number(slotId))) {
                          slotClassName = 'slot-unavailable';
                        } else if (selectedRestrictionPresentationId && isPresentationSlotUnsuitable(selectedRestrictionPresentationId, Number(slotId))) {
                          slotClassName = 'slot-unsuitable';
                        } else {
                          slotClassName = 'slot-exists';
                        }
                        if (selectedRestrictionPresentationId && isPresentationSlotAdditionalUnavailable(Number(slotId))) {
                          slotClassName = slotClassName + ' slot-additional-unavailable';
                        } else if (selectedRestrictionPresentationId && isPresentationSlotAdditionalUnsuitable(Number(slotId))) {
                          slotClassName = slotClassName + ' slot-additional-unsuitable';
                        }
                      } else {
                        slotClassName = 'slot-not-exists';
                      }
                      console.log("slotClassName: ", slotClassName);
                      return (
                        <td
                          key={`presentation-${date}-${hour}`}
                          className={slotClassName}
                          onClick={(slot && !isComputing) ? () => handleRestrictionPresentationSlotClick(selectedRestrictionPresentationId, Number(slotId)) : undefined} // Handle slot click
                        >
                          {slot && <div className="slot-content"></div>}
                        </td>
                      );
                  })}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
};

export default Page;
