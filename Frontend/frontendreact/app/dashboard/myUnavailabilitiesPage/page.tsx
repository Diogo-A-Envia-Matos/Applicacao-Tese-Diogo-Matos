"use client";

import ComputationWarning from '@/app/components/computationWarning/component';
import React, { useState, useEffect, useContext } from 'react';
import axios, { AxiosError } from 'axios';
import { FaInfoCircle, FaExclamationCircle } from 'react-icons/fa';

import { StopChangeContext } from '@/app/context/stopChangeContext/context';

import './Page.css';

interface SlotDTO {
  id: string;
  date: string;
  startingHour: string;
}

interface UnavailabilityDTO {
  id: number;
  unsuitable: boolean;
  slotsID: number[];
  userID: number;
}

interface UserUnavailabilitiesDTO {
  first: UnavailabilityDTO;
  second: UnavailabilityDTO;
}

interface ErrorResponse {
  message: string;
}

const COMPUTATION_IN_PROGRESS = "There is a computation in progress."
const CHANGES_NOT_ALLOWED = "Currently, changes are not allowed to be performed."

const Page: React.FC = () => {
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [slots, setSlots] = useState<SlotDTO[]>([]);
  const [dates, setDates] = useState<string[]>([]);
  const [hours, setHours] = useState<string[]>([]);
  const [userUnavailabilitySlots, setUserUnavailabilitySlots] = useState<number[]>([]);
  const [userUnsuitableSlots, setUserUnsuitableSlots] = useState<number[]>([]);
  const [dateUnavailability, setDateUnavailability] = useState<string[]>([]);
  const [dateUnsuitable, setDateUnsuitable] = useState<string[]>([]);
  const [hourUnavailability, setHourUnavailability] = useState<string[]>([]);
  const [hourUnsuitable, setHourUnsuitable] = useState<string[]>([]);
  const [isSlotLoading, setIsSlotLoading] = useState<number[]>([]);
  // const [message, setMessage] = useState<string>(
  //   "Here you can add your restrictions by clicking on the slots you are not available.\nGreen Slot -> You are available.\nYellow Slot -> You are available, but another (green) slot would be better.\nRed Slot -> You are unavailable."
  // );
  const [isComputing, setIsComputing] = useState(false);
  
  const { stopChange, toggleStopChange, changeStopChange } = useContext(StopChangeContext);

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
          } else if (axiosError.response?.status == 422){
            setErrorMessage(axiosError.response.data.message);
          } else {
            setErrorMessage('There was an error on the server side, please try again later.');
          }
        } else {
          setErrorMessage('There was an error on the server side, please try again later.');
        }
      }
    }

    checkComputation()
  }, []);

  useEffect(() => {
    fetchData();
    fetchUserUnavailabilitySlots();
  }, []);

  const fetchData = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const response = await axios.get<SlotDTO[]>(`http://localhost:8080/api/slot`, {
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
          setErrorMessage('There was an error on the server side, please try again later.');
        }
      } else {
        setErrorMessage('There was an error on the server side, please try again later.');
      }
    }
  };

  const fetchUserUnavailabilitySlots = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const userId = sessionStorage.getItem('id');
      const response = await axios.get<UserUnavailabilitiesDTO>(`http://localhost:8080/api/unavailability/user/${userId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const data = response.data;
      const unavailabilityData = data.first;
      const unsuitableData = data.second;
      setUserUnavailabilitySlots(unavailabilityData.slotsID);
      setUserUnsuitableSlots(unsuitableData.slotsID);
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
          setErrorMessage('There was an error on the server side, please try again later.');
        }
      } else {
        setErrorMessage('There was an error on the server side, please try again later.');
      }
    }
  };

  const calculateFinishHour = (startingHour: string): string => {
    const [hour, minute] = startingHour.split(':').map(parseFloat);
    const finishHour = hour + (minute + 45) / 60;
    const finishHourHour = Math.floor(finishHour);
    const finishHourMinute = Math.round((finishHour - finishHourHour) * 60);
    return `${finishHourHour.toString().padStart(2, '0')}:${finishHourMinute.toString().padStart(2, '0')}`;
  };

  const toggleSlotAvailability = async (slotID: string) => {
    try {
      const token = sessionStorage.getItem('token');
      const userId = sessionStorage.getItem('id');
      setIsSlotLoading([...isSlotLoading, parseInt(slotID)])
      if (userUnsuitableSlots.includes(parseInt(slotID))) {
        await axios.put('http://localhost:8080/api/unavailability', {
          slotID,
          userID: userId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
      } else if (userUnavailabilitySlots.includes(parseInt(slotID))) {
        await axios.delete('http://localhost:8080/api/unavailability', {
          data: { slotID, userID: userId } ,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
      } else {
        await axios.post('http://localhost:8080/api/unavailability', {
          slotID,
          userID: userId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
      }
      await fetchUserUnavailabilitySlots(); // Refresh user unavailability slots after toggle
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
          console.log("axiosError: ", axiosError)
          console.log("axiosError.response: ", axiosError.response)
          const errorMessage = axiosError.response?.data
          console.log("errorMessage: ", errorMessage)
          if (errorMessage == COMPUTATION_IN_PROGRESS) {
            setIsComputing(true);
            setErrorMessage(COMPUTATION_IN_PROGRESS);
          } else if (errorMessage == CHANGES_NOT_ALLOWED) {
            changeStopChange(true);
            setErrorMessage(CHANGES_NOT_ALLOWED);
          } else {
            setErrorMessage('There was an error on the server side, please try again later.');
          }
        } else {
          setErrorMessage('There was an error on the server side, please try again later.');
        }
      } else {
        setErrorMessage('There was an error on the server side, please try again later.');
      }
    } finally {
      setIsSlotLoading(
        isSlotLoading.filter(value =>
          value !== parseInt(slotID)
        )
      );
    }
  };

  const toggleDateAvailability = async (date: string) => {
    const slotGroup = slots.filter((value) => value.date === date)
    const slotGroupIDs = slotGroup.map((value) => parseInt(value.id))
    try {
      const token = sessionStorage.getItem('token');
      const userId = sessionStorage.getItem('id');
      console.log("slotGroup: ", slotGroup)
      setIsSlotLoading(isSlotLoading.concat(slotGroupIDs))
      if (dateUnsuitable.includes(date)) {
        console.log("dateUnsuitable")
        await axios.put('http://localhost:8080/api/unavailability/groupSlot', {
          slotsID: slotGroupIDs,
          userID: userId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        setDateUnsuitable(dateUnsuitable.filter(value => value !== date))
        setDateUnavailability([...dateUnavailability, date])
      } else if (dateUnavailability.includes(date)) {
        console.log("dateUnavailability")
        await axios.delete('http://localhost:8080/api/unavailability/groupSlot', {
          data: { slotsID: slotGroupIDs, userID: userId } ,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        setDateUnavailability(dateUnavailability.filter(value => value !== date))
      } else {
        console.log("!dateUnsuitable")
        await axios.post('http://localhost:8080/api/unavailability/groupSlot', {
          slotsID: slotGroupIDs,
          userID: userId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        setDateUnsuitable([...dateUnsuitable, date])
      }
      await fetchUserUnavailabilitySlots(); // Refresh user unavailability slots after toggle
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
          const errorMessage = axiosError.response?.data
          console.log("errorMessage: ", errorMessage)
          if (errorMessage == COMPUTATION_IN_PROGRESS) {
            setIsComputing(true);
            setErrorMessage(COMPUTATION_IN_PROGRESS);
          } else if (errorMessage == CHANGES_NOT_ALLOWED) {
            changeStopChange(true);
            setErrorMessage(CHANGES_NOT_ALLOWED);
          } else {
            setErrorMessage('There was an error on the server side, please try again later.');
          }
        } else {
          setErrorMessage('There was an error on the server side, please try again later.');
        }
      } else {
        setErrorMessage('There was an error on the server side, please try again later.');
      }
    } finally {
      setIsSlotLoading(
        isSlotLoading.filter(value =>
          !slotGroupIDs.includes(value)
        )
      );
    }
  };

  const toggleHourAvailability = async (startingHour: string) => {
    const slotGroup = slots.filter((value) => value.startingHour === startingHour)
    const slotGroupIDs = slotGroup.map((value) => parseInt(value.id))
    try {
      const token = sessionStorage.getItem('token');
      const userId = sessionStorage.getItem('id');
      console.log("slotGroup: ", slotGroup)
      setIsSlotLoading(isSlotLoading.concat(slotGroupIDs))
      if (hourUnsuitable.includes(startingHour)) {
        await axios.put('http://localhost:8080/api/unavailability/groupSlot', {
          slotsID: slotGroupIDs,
          userID: userId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        setHourUnsuitable(hourUnsuitable.filter(value => value !== startingHour))
        setHourUnavailability([...hourUnavailability, startingHour])
      } else if (hourUnavailability.includes(startingHour)) {
        await axios.delete('http://localhost:8080/api/unavailability/groupSlot', {
          data: { slotsID: slotGroupIDs, userID: userId },
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        setHourUnavailability(hourUnavailability.filter(value => value !== startingHour))
      } else {
        await axios.post('http://localhost:8080/api/unavailability/groupSlot', {
          slotsID: slotGroupIDs,
          userID: userId
        },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        setHourUnsuitable([...hourUnsuitable, startingHour])
      }
      await fetchUserUnavailabilitySlots(); // Refresh user unavailability slots after toggle
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
          const errorMessage = axiosError.response?.data
          console.log("errorMessage: ", errorMessage)
          if (errorMessage == COMPUTATION_IN_PROGRESS) {
            setIsComputing(true);
            setErrorMessage(COMPUTATION_IN_PROGRESS);
          } else if (errorMessage == CHANGES_NOT_ALLOWED) {
            changeStopChange(true);
            setErrorMessage(CHANGES_NOT_ALLOWED);
          } else {
            setErrorMessage('There was an error on the server side, please try again later.');
          }
        } else {
          setErrorMessage('There was an error on the server side, please try again later.');
        }
      } else {
        setErrorMessage('There was an error on the server side, please try again later.');
      }
    } finally {
      setIsSlotLoading(
        isSlotLoading.filter(value =>
          !slotGroupIDs.includes(value)
        )
      );
    }
  };

  const formatDate = (date: string): string => {
    const [year, month, day] = date.split('-');
    return `${day}-${month}-${year}`;
  };

  return (
    <div className="container">
      <h1 className="title" style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>My Unavailabilities</h1>
      {isComputing && (<ComputationWarning />)}
      {slots.length === 0 ? (
          <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem'}}>
              Wait for an organizer to add slots.
          </div>
        ) : (
          <>
            <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem'}}>
              {/* {message} */}
              <p>
                Here you can add your restrictions by clicking on the slots you are not available.
                You can also click the date and hour cells at the headers of the columns and rows of the table, respectively, to change restrictions across the column/row.
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
            {errorMessage && <div className="error-message"><FaExclamationCircle className="error-icon" /> {errorMessage}</div>}
            <div className="table-container" style={{ overflowX: 'auto' }}>
              <table className="slots-table">
                <thead>
                  <tr>
                    <th className="hour-cell"></th>
                    {dates.sort().map(date => (
                      <th
                        key={date}
                        className="date-cell"
                        onClick={() => {
                          if (!isComputing && !stopChange) {
                            toggleDateAvailability(date);
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
                            if (!isComputing && !stopChange) {
                              toggleHourAvailability(hour);
                            }
                          }}
                        >
                          {startHour} - {finishHour}
                        </td>
                        {dates.sort().map(date => {
                          const slot = slots.find(s => s.date === date && s.startingHour === hour);
                          const isUserUnavailabilitySlot = userUnavailabilitySlots.includes(parseInt(slot?.id ?? '0'));
                          const isUserUnsuitableSlot = userUnsuitableSlots.includes(parseInt(slot?.id ?? '0'));
                          const slotId = slot?.id ?? '';
                          const isLoading = isSlotLoading.find((value) => value === parseInt(slotId))
                          return (
                            <td
                              key={`${date}-${hour}`}
                              className={slot ? (isUserUnavailabilitySlot ? 'slot-unavailable' : (isUserUnsuitableSlot ? 'slot-unsuitable' : 'slot-exists')) : 'slot-not-exists'}
                              onClick={() => {
                                if (slot && !isComputing && !stopChange && !isLoading) {
                                  toggleSlotAvailability(slot.id);
                                }
                              }}
                            >
                            </td>
                          );
                        })}
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </>
        )}
    </div>
  );
};

export default Page;
