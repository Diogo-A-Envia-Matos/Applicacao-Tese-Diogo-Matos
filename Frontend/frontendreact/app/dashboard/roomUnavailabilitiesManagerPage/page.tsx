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

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
}

interface SlotDTO {
  id: string;
  date: string;
  startingHour: string; 
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

interface RoomUnavailabilityDTO {
  id: number;
  unsuitable: boolean;

  slotsID: number[];
  roomID: number;
}

interface RoomUnavailabilitiesDTO {
  first: RoomUnavailabilityDTO;
  second: RoomUnavailabilityDTO;
}

const COMPUTATION_IN_PROGRESS = "There is a computation in progress."

const Page: React.FC = () => {
  const [isComputing, setIsComputing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [selectedRestrictionRoomId, setSelectedRestrictionRoomId] = useState<number | null>(null);
  
  const [isRoomSlotLoading, setIsRoomSlotLoading] = useState<number[]>([]);

  const [roomDateUnavailability, setRoomDateUnavailability] = useState<string[]>([]);
  const [roomDateUnsuitable, setRoomDateUnsuitable] = useState<string[]>([]);
  const [roomHourUnavailability, setRoomHourUnavailability] = useState<string[]>([]);
  const [roomHourUnsuitable, setRoomHourUnsuitable] = useState<string[]>([]);
  
  const [loggedInUserRole, setLoggedInUserRole] = useState<String>('');
  const [slots, setSlots] = useState<SlotDTO[]>([]);
  const [dates, setDates] = useState<string[]>([]);
  const [hours, setHours] = useState<string[]>([]);
  const [rooms, setRooms] = useState<RoomDTO[]>([]);

  const [roomUnavailable, setRoomUnavailable] = useState<RoomUnavailabilityDTO | null>(null);
  const [roomUnsuitable, setRoomUnsuitable] = useState<RoomUnavailabilityDTO | null>(null);
  
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

  const fetchRoomsData = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const response = await axios.get<RoomDTO[]>('http://localhost:8080/api/room', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      setRooms(response.data);
    } catch (error) {
      console.error('Error fetching rooms:', error);
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

  const fetchUnavailabilitiesData = async () => {
    const token = sessionStorage.getItem('token');
    try{
      const response = await axios.get<RoomUnavailabilitiesDTO>(`http://localhost:8080/api/unavailability/room/${selectedRestrictionRoomId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      const roomUnavailabilities = response.data;

      console.log("roomUnavailabilities: ", roomUnavailabilities)

      const data = response.data;
      const roomUnavailabilityData = data.first;
      const roomUnsuitableData = data.second;
      setRoomUnavailable(roomUnavailabilityData)
      setRoomUnsuitable(roomUnsuitableData)
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

  const formatDate = (date: string): string => {
    const [year, month, day] = date.split('-');
    return `${day}-${month}-${year}`;
  };

  const formatRoom = (roomNr: string, building: string): string => {
    return `Room Number: ${roomNr} - Building: ${building}`;
  };

  function compareRooms(a: RoomDTO, b: RoomDTO) {
    const res1 = a.building.localeCompare(b.building)
    if (res1 === 0) {
      return a.roomNr.localeCompare(b.roomNr)
    } else {
      return res1;
    }
  }
  
  const calculateFinishHour = (startingHour: string): string => {
    const [hour, minute] = startingHour.split(':').map(parseFloat);
    const finishHour = hour + (minute + 45) / 60;
    const finishHourHour = Math.floor(finishHour);
    const finishHourMinute = Math.round((finishHour - finishHourHour) * 60);
    return `${finishHourHour.toString().padStart(2, '0')}:${finishHourMinute.toString().padStart(2, '0')}`;
  };
  
  const handleRestrictionRoomChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedValue = parseInt(event.target.value);
    setSelectedRestrictionRoomId(selectedValue === 0 ? null : selectedValue);
    setIsRoomSlotLoading([])
    setRoomDateUnavailability([])
    setRoomDateUnsuitable([])
    setRoomHourUnavailability([])
    setRoomHourUnsuitable([])
  };
  
  const handleRestrictionRoomSlotClick = async (roomId: number | null, slotId: number) => {
    if (roomId === null) {
      return
    }
    console.log("roomId", roomId);
    console.log("slotId", slotId);
    try {
      const token = sessionStorage.getItem('token');
      setIsRoomSlotLoading([...isRoomSlotLoading, slotId])
      if (isRoomSlotUnavailable(slotId)) {
        console.log("isRoomSlotUnavailable");
        await axios.delete('http://localhost:8080/api/unavailability/room', {
          data: { slotID: slotId, roomID: roomId } ,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
      } else if (isRoomSlotUnsuitable(slotId)) {
        console.log("isRoomSlotUnsuitable");
        await axios.put('http://localhost:8080/api/unavailability/room', {
          slotID: slotId,
          roomID: roomId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
      } else {
        console.log("!isRoomSlotUnavailable");
        await axios.post('http://localhost:8080/api/unavailability/room', {
          slotID: slotId,
          roomID: roomId
          },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
      }
      await fetchUnavailabilitiesData(); // Refresh user unavailability slots after toggle
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
      setIsRoomSlotLoading(
        isRoomSlotLoading.filter((value) =>
          value !== slotId
        )
      );
    }
  };

  const toggleRoomDateAvailability = async (roomId: number | null, date: string) => {
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
      console.log("roomDateUnsuitable: ", roomDateUnsuitable)
      console.log("roomDateUnavailability: ", roomDateUnavailability)
      setIsRoomSlotLoading(isRoomSlotLoading.concat(slotGroupIDs))
      if (roomDateUnsuitable.includes(date)) {
        // console.log("dateUnsuitable")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.put('http://localhost:8080/api/unavailability/room/groupSlot', {
          slotsID: slotGroupIDs,
          roomID: roomId
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
        setRoomDateUnsuitable(roomDateUnsuitable.filter(value => value !== date))
        setRoomDateUnavailability([...roomDateUnavailability, date])
      } else if (roomDateUnavailability.includes(date)) {
        // console.log("userDateUnavailability")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.delete('http://localhost:8080/api/unavailability/room/groupSlot', {
          data: { slotsID: slotGroupIDs, roomID: roomId } ,
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
        // setRoomDateUnsuitable(roomDateUnsuitable.filter(value => value === date))
        setRoomDateUnavailability(roomDateUnavailability.filter(value => value !== date))
      } else {
        // console.log("!dateUserUnsuitable")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.post('http://localhost:8080/api/unavailability/room/groupSlot', {
          slotsID: slotGroupIDs,
          roomID: roomId
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
        setRoomDateUnsuitable([...roomDateUnsuitable, date])
        // setRoomDateUnavailability([...roomDateUnavailability, date])
      }
      await fetchUnavailabilitiesData(); // Refresh user unavailability slots after toggle
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
      setIsRoomSlotLoading(
        isRoomSlotLoading.filter(value =>
          !slotGroupIDs.includes(value)
        )
      );
    }
  };

  const toggleRoomHourAvailability = async (roomId: number | null, startingHour: string) => {
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
      console.log("roomHourUnsuitable: ", roomHourUnsuitable)
      console.log("roomHourUnavailability: ", roomHourUnavailability)
      setIsRoomSlotLoading(isRoomSlotLoading.concat(slotGroupIDs))
      if (roomHourUnsuitable.includes(startingHour)) {
        // console.log("roomHourUnsuitable")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.put('http://localhost:8080/api/unavailability/room/groupSlot', {
          slotsID: slotGroupIDs,
          roomID: roomId
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
        setRoomHourUnsuitable(roomHourUnsuitable.filter(value => value !== startingHour))
        setRoomHourUnavailability([...roomHourUnavailability, startingHour])
      } else if (roomHourUnavailability.includes(startingHour)) {
        // console.log("roomHourUnavailability")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.delete('http://localhost:8080/api/unavailability/room/groupSlot', {
          data: { slotsID: slotGroupIDs, roomID: roomId } ,
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
        // setRoomHourUnsuitable(roomHourUnsuitable.filter(value => value !== startingHour))
        setRoomHourUnavailability(roomHourUnavailability.filter(value => value !== startingHour))
      } else {
        // console.log("!roomHourUnsuitable")
        // setIsSlotLoading([...isSlotLoading, slotGroupIDs])
        await axios.post('http://localhost:8080/api/unavailability/room/groupSlot', {
          slotsID: slotGroupIDs,
          roomID: roomId,
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
        setRoomHourUnsuitable([...roomHourUnsuitable, startingHour])
        // setRoomHourUnavailability([...roomHourUnavailability, startingHour])
      }
      await fetchUnavailabilitiesData(); // Refresh user unavailability slots after toggle
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
      setIsRoomSlotLoading(
        isRoomSlotLoading.filter(value =>
          !slotGroupIDs.includes(value)
        )
      );
    }
  };

  const isRoomSlotUnavailable = (slotId: number): boolean => {
    return roomUnavailable?.slotsID.includes(slotId) ?? false;
  };

  const isRoomSlotUnsuitable = (slotId: number): boolean => {
    return roomUnsuitable?.slotsID.includes(slotId) ?? false;
  };
  
  useEffect(() => {
    fetchUserRole();
    fetchRoomsData();
    fetchSlotsData();
    // fetchUnavailabilitiesData();
  }, []);

  useEffect(() => {
    if (selectedRestrictionRoomId !== null) {
      fetchUnavailabilitiesData();
    } else {
      setRoomUnavailable(null);
      setRoomUnsuitable(null);
    }
  }, [selectedRestrictionRoomId]);

  return (
    <>
      <div className="container">
        <div className="header">
          <h2 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Rooms Unavailabilities Manager</h2>
          {(isComputing && (loggedInUserRole === 'ADMIN' || loggedInUserRole === 'ORGANIZER')) && (<ComputationWarning />)}
          <p>
            Here you can set restrictions on rooms by clicking on the slots, after choosing the desired room.
            You can also click the date and hour cells at the headers of the columns and rows of the tables, respectively, to change restrictions across the column/row.
          </p>
          <p>
            You can choose a room through the dropdown menu below.
          </p>
          <p>
            The slots can have the following colors:
          </p>
          <ol>
            <li>{"Green Slot -> You are available."}</li>
            <li>{"Yellow Slot -> You are available, but another (green) slot would be better."}</li>
            <li>{"Red Slot -> You are unavailable."}</li>
          </ol>
        </div>

        <div className="room-input-division">
          <select id="room-select" className="room-input-field" value={selectedRestrictionRoomId || 0} onChange={handleRestrictionRoomChange}>
            <option value="0">Select one room</option>
            {rooms.sort(compareRooms).map(room => (
              <option key={room.id} value={room.id}>
                {formatRoom(room.roomNr, room.building)}
              </option>
            ))}
          </select>
        </div>
        
        {selectedRestrictionRoomId && (
          <div className="table-container" style={{ overflowX: 'auto' }}>
            {slots.length === 0 ? (
              <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem'}}>
                  Wait for an organizer to add slots.
              </div>
            ) : (
              <div className="room-unavailability-table" style={{ overflowX: 'auto' }}>
                
                <table className="room-unavailability-slots-table">
                  <thead>
                    <tr>
                      <th className="hour-cell"></th>
                      {dates.sort().map(date => (
                        <th
                          key={`room-${date}`}
                          className="date-cell"
                          onClick={() => {
                            if (!isComputing) {
                              toggleRoomDateAvailability(selectedRestrictionRoomId, date);
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
                                toggleRoomHourAvailability(selectedRestrictionRoomId, hour);
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
                              // if (selectedRestrictionRoomId && isRoomSlotUnavailable(selectedRestrictionRoomId, Number(slotId))) {
                              if (selectedRestrictionRoomId && isRoomSlotUnavailable(Number(slotId))) {
                                slotClassName = 'slot-unavailable';
                              // } else if (selectedRestrictionRoomId && isRoomSlotUnsuitable(selectedRestrictionRoomId, Number(slotId))) {
                              } else if (selectedRestrictionRoomId && isRoomSlotUnsuitable(Number(slotId))) {
                                slotClassName = 'slot-unsuitable';
                              } else {
                                slotClassName = 'slot-exists';
                              }
                            }
                            console.log("slotClassName: ", slotClassName);
                            return (
                              <td
                                key={`room-${date}-${hour}`}
                                className={slotClassName}
                                onClick={(slot && !isComputing) ? () => handleRestrictionRoomSlotClick(selectedRestrictionRoomId, Number(slotId)) : undefined} // Handle slot click
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
            )}
          </div>
        )}
      </div>
    </>
  );
};

export default Page;
