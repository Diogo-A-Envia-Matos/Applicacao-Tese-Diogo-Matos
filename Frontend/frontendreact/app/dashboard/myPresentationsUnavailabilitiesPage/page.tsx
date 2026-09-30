"use client";

import ComputationWarning from '@/app/components/computationWarning/component';
import React, { useState, useEffect, useContext } from 'react';
import axios, { AxiosError } from 'axios';
import { FaInfoCircle, FaExclamationCircle } from 'react-icons/fa';

import { StopChangeContext } from '@/app/context/stopChangeContext/context';

import './Page.css';

interface PresentationDTO {
  id: number;
  thesisTitle: string;
}

interface SlotDTO {
  id: string;
  date: string;
  startingHour: string;
}

interface UnavailabilityDTO {
  id: number;
  unsuitable: boolean;
  slotsID: string[];
  presentationID: number;
  userID: number;
}

interface PresentationUnavailabilitiesDTO {
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
  const [presentations, setPresentations] = useState<PresentationDTO[]>([]);
  const [selectedPresentation, setSelectedPresentation] = useState<number | null>(null);
  const [slots, setSlots] = useState<SlotDTO[]>([]);
  const [dates, setDates] = useState<string[]>([]);
  const [hours, setHours] = useState<string[]>([]);
  const [unavailability, setUnavailability] = useState<UnavailabilityDTO | null>(null);
  const [unsuitable, setUnsuitable] = useState<UnavailabilityDTO | null>(null);
  const [dateUnavailability, setDateUnavailability] = useState<string[]>([]);
  const [dateUnsuitable, setDateUnsuitable] = useState<string[]>([]);
  const [hourUnavailability, setHourUnavailability] = useState<string[]>([]);
  const [hourUnsuitable, setHourUnsuitable] = useState<string[]>([]);
  const [isSlotLoading, setIsSlotLoading] = useState<number[]>([]);
  const [message, setMessage] = useState<string>(
    "Here you can add restrictions to a single presentation.\n Please choose a presentation and click on the slots below.\nGreen Slot -> You are available.\nYellow Slot -> You are available, but another (green) slot would be better.\nRed Slot -> You are unavailable."
  );
  const [isComputing, setIsComputing] = useState(false);
  
  const { stopChange, toggleStopChange, changeStopChange } = useContext(StopChangeContext);
  
  useEffect(() => {
    const checkComputation = async () => {
      try {
        const token = sessionStorage.getItem('token');
        //TODO New HTTP request to check computationstate
        const response = await axios.get(`http://localhost:8080/api/python/checkComputation`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        //TODO Obtain data from response
        // const state = response.data
        if (response.data === true) {
          //TODO Computation in progress
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
          } else if (axiosError.response?.status == 422) {
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

  const fetchData = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const userId = sessionStorage.getItem('id');
      const presentationResponse = await axios.get<PresentationDTO[]>(`http://localhost:8080/api/presentation/adviser/${userId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const presentationData = presentationResponse.data;
      setPresentations(presentationData);
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

  useEffect(() => {
    fetchData();
  }, []);

  const handlePresentationChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedPresentation(event.target.value ? Number(event.target.value) : null);
    setIsSlotLoading([])
    setDateUnavailability([])
    setDateUnsuitable([])
    setHourUnavailability([])
    setHourUnsuitable([])
  };

  useEffect(() => {
    if (selectedPresentation !== null) {
      fetchSlots();
      fetchUnavailability();
    } else {
      setSlots([]);
      setDates([]);
      setHours([]);
      setUnavailability(null);
      setUnsuitable(null);
    }
  }, [selectedPresentation]);

  const fetchSlots = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const response = await axios.get<SlotDTO[]>('http://localhost:8080/api/slot', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const slotData = response.data;
      setSlots(slotData);

      const uniqueDates = Array.from(new Set(slotData.map(slot => slot.date)));
      const uniqueHours = Array.from(new Set(slotData.map(slot => slot.startingHour)));
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

  //TODO Change to support changes
  const fetchUnavailability = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const response = await axios.get<PresentationUnavailabilitiesDTO>(`http://localhost:8080/api/unavailability/presentation/${selectedPresentation}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      //TODO Response
      // const unavailabilityData = response.data;
      // setUnavailability(unavailabilityData);
      //TODO Check extraction of data from tuple (using attributes from "Pair" class from Kotlin, which does not exist on TypeScript)
      const data = response.data;
      const unavailabilityData = data.first;
      const unsuitableData = data.second;
      setUnavailability(unavailabilityData);
      setUnsuitable(unsuitableData);
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

  const isSlotUnavailable = (slotId: string): boolean => {
    return unavailability?.slotsID.includes(slotId) ?? false;
  };

  const isSlotUnsuitable = (slotId: string): boolean => {
    return unsuitable?.slotsID.includes(slotId) ?? false;
  };

  const handleSlotClick = async (slotId: string) => {
    // console.log("handleSlotClick")
    // console.log("slotId: ", slotId)
    const isLoading = isSlotLoading.find((value) => value === parseInt(slotId))
    if (typeof isLoading === 'undefined') {
      // console.log("Click.")
      if (isSlotUnsuitable(slotId)) {
        // console.log("isSlotUnsuitable")
        await changeUnsuitableToUnavailability(slotId);
      } else if (isSlotUnavailable(slotId)) {
        // console.log("isSlotUnavailable")
        await removeUnavailability(slotId);
      } else {
        // console.log("!isSlotUnsuitable")
        await addUnsuitableSlot(slotId);
      }
    }
  };

  const addUnsuitableSlot = async (slotId: string) => {
    // const [isSlotLoading, setIsSlotLoading] = useState<number[]>([]);
    try {
      const token = sessionStorage.getItem('token');
      // console.log("addUnsuitableSlot")
      setIsSlotLoading([...isSlotLoading, parseInt(slotId)])
      await axios.post('http://localhost:8080/api/unavailability/presentation', {
        slotID: slotId,
        presentationID: selectedPresentation,
        userID: 1 
      },{
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      await fetchUnavailability(); // Refresh unavailability data after adding
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
          value !== parseInt(slotId)
        )
      );
    }
  };

  //TODO Prevent trying to change slots multiple times
  const changeUnsuitableToUnavailability = async (slotId: string) => {
    try {
      const token = sessionStorage.getItem('token');
      // console.log("changeUnsuitableToUnavailability")
      setIsSlotLoading([...isSlotLoading, parseInt(slotId)])
      await axios.put('http://localhost:8080/api/unavailability/presentation', {
        slotID: slotId,
        presentationID: selectedPresentation,
        userID: 1 
      },{
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      await fetchUnavailability(); // Refresh unavailability data after removing
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
          value !== parseInt(slotId)
        )
      );
    }
  };

  //TODO Prevent trying to change slots multiple times
  const removeUnavailability = async (slotId: string) => {
    try {
      const token = sessionStorage.getItem('token');
      // console.log("removeUnavailability")
      setIsSlotLoading([...isSlotLoading, parseInt(slotId)])
      await axios.delete('http://localhost:8080/api/unavailability/presentation', {
        data: {
          slotID: slotId,
          presentationID: selectedPresentation,
          userID: 1 
        }, 
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      await fetchUnavailability(); // Refresh unavailability data after removing
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
          value !== parseInt(slotId)
        )
      );
    }
  };

  //TODO Update this function (Based on toggleSlotAvailability)
  const toggleDateAvailability = async (date: string) => {
    //TODO Set loading for the group
    const slotGroup = slots.filter((value) => value.date === date)
    const slotGroupIDs = slotGroup.map((value) => parseInt(value.id))
    try {
      //TODO Check if slot loading before changes
      const token = sessionStorage.getItem('token');
      console.log("slotGroup: ", slotGroup)
      setIsSlotLoading(isSlotLoading.concat(slotGroupIDs))
      if (dateUnsuitable.includes(date)) {
        // setIsSlotLoading(isSlotLoading.concat(slotGroupIDs))
        await axios.put('http://localhost:8080/api/unavailability/presentation/groupSlot', {
          slotsID: slotGroupIDs,
          presentationID: selectedPresentation,
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
        setDateUnsuitable(dateUnsuitable.filter(value => value !== date))
        setDateUnavailability([...dateUnavailability, date])
      } else if (dateUnavailability.includes(date)) {
        // setIsSlotLoading(isSlotLoading.concat(slotGroupIDs))
        await axios.delete('http://localhost:8080/api/unavailability/presentation/groupSlot', {
          data: { slotsID: slotGroupIDs, presentationID: selectedPresentation },
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
        // setDateUnsuitable(dateUnsuitable.filter(value => value === date))
        setDateUnavailability(dateUnavailability.filter(value => value !== date))
      } else {
        // setIsSlotLoading(isSlotLoading.concat(slotGroupIDs))
        await axios.post('http://localhost:8080/api/unavailability/presentation/groupSlot', {
          slotsID: slotGroupIDs,
          presentationID: selectedPresentation
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
        setDateUnsuitable([...dateUnsuitable, date])
        // setDateUnavailability([...dateUnavailability, date])
      }
      await fetchUnavailability(); // Refresh unavailability data after adding
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
      // setIsSlotLoading(
      //   isSlotLoading.filter(value =>
      //     value !== parseInt(slotID)
      //   )
      // );
    }
  };

  //TODO Update this function (Based on toggleSlotAvailability)
  const toggleHourAvailability = async (startingHour: string) => {
    //TODO Set loading for the group
    const slotGroup = slots.filter((value) => value.startingHour === startingHour)
    const slotGroupIDs = slotGroup.map((value) => parseInt(value.id))
    try {
      //TODO Check if slot loading before changes
      const token = sessionStorage.getItem('token');
      console.log("slotGroup: ", slotGroup)
      setIsSlotLoading(isSlotLoading.concat(slotGroupIDs))
      if (hourUnsuitable.includes(startingHour)) {
        // setIsSlotLoading(isSlotLoading.concat(slotGroupIDs))
        await axios.put('http://localhost:8080/api/unavailability/presentation/groupSlot', {
          slotsID: slotGroupIDs,
          presentationID: selectedPresentation,
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
        setHourUnsuitable(hourUnsuitable.filter(value => value !== startingHour))
        setHourUnavailability([...hourUnavailability, startingHour])
      } else if (hourUnavailability.includes(startingHour)) {
        // setIsSlotLoading(isSlotLoading.concat(slotGroupIDs))
        await axios.delete('http://localhost:8080/api/unavailability/presentation/groupSlot', {
          data: { slotsID: slotGroupIDs, presentationID: selectedPresentation } ,
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
        // setHourUnsuitable(hourUnsuitable.filter(value => value !== startingHour))
        setHourUnavailability(hourUnavailability.filter(value => value !== startingHour))
      } else {
        // setIsSlotLoading(isSlotLoading.concat(slotGroupIDs))
        await axios.post('http://localhost:8080/api/unavailability/presentation/groupSlot', {
          slotsID: slotGroupIDs,
          presentationID: selectedPresentation,
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
        setHourUnsuitable([...hourUnsuitable, startingHour])
        // setHourUnavailability([...hourUnavailability, startingHour])
      }
      await fetchUnavailability(); // Refresh unavailability data after toggle
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
      // setIsSlotLoading(
      //   isSlotLoading.filter(value =>
      //     value !== parseInt(slotID)
      //   )
      // );
    }
  };

  return (
    <div className="container">
      <div className="header">
        <h1 className="title" style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>My Presentations Unavailabilities</h1>
        {isComputing && (<ComputationWarning />)}
        <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem' }}>
          {/* {message} */}
          <p>
            Here you can add restrictions to your presentations by clicking on the slots below.
            You can also click the date and hour cells at the headers of the columns and rows of the table, respectively, to change restrictions across the column/row.
          </p>
          <p>
            You can choose a presentation through the dropdown menu below.
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
        <select value={selectedPresentation ?? ''} className="input-field" onChange={handlePresentationChange}>
          <option value="">Select Your Presentation</option>
          {presentations.sort((a, b) => a.id - b.id).map(presentation => (
            <option key={presentation.id} value={presentation.id}>{presentation.thesisTitle}</option>
          ))}
        </select>
      </div>
        {selectedPresentation && (
          <div className="table-container" style={{ overflowX: 'auto' }}>
            {slots.length === 0 ? (
              <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem'}}>
                  Wait for an organizer to add slots.
              </div>
            ) : (
              <>
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
                            const slotId = slot?.id ?? '';
                            const isLoading = isSlotLoading.find((value) => value === parseInt(slotId))
                            let slotClassName = '';
                            if (slot) {
                              // slotClassName = isSlotUnavailable(slotId) ? 'slot-unavailable' : 'slot-exists';
                              if (isSlotUnavailable(slotId)) {
                                slotClassName = 'slot-unavailable';
                              } else if (isSlotUnsuitable(slotId)) {
                                slotClassName = 'slot-unsuitable';
                              } else {
                                slotClassName = 'slot-exists';
                              }
                            } else {
                              slotClassName = 'slot-not-exists';
                            }
                            return (
                              <td
                                key={`${date}-${hour}`}
                                className={slotClassName}
                                onClick={(slot && !isComputing && !stopChange && !isLoading) ? () => handleSlotClick(slotId) : undefined} // Handle slot click
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
              </>
            )}
          </div>
        )}
      </div>
    );
  };
  
  export default Page;
  


