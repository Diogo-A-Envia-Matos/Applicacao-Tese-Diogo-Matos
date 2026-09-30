"use client";

import ComputationWarning from '@/app/components/computationWarning/component';
import React, { useState, useEffect, useRef, useContext } from 'react';
import axios, { AxiosError } from 'axios';
import { FaExclamationCircle } from 'react-icons/fa';

import { StopChangeContext } from '@/app/context/stopChangeContext/context';

import './Page.css'; 

// interface User {
//     id: number;
//     name: string;
//     email: string;
//     role: string;
// }

interface ErrorResponse {
  message: string;
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

interface SlotDTO {
    id: string;
    date: string;
    startingHour: string;
}

// interface UnavailabilityDTO {
//   id: number;
//   unsuitable: boolean;
//   slotsID: number[];
//   userID: number;
// }

// interface UserUnavailabilitiesDTO {
//   first: UnavailabilityDTO;
//   second: UnavailabilityDTO;
// }

interface IgnoredPresentationUnsuitableDTO {
  id: number;
  slotsID: number[];
  presentationID: number;
  userID: number;
}


interface PresentationUnavailabilityDTO {
  id: number;
  unsuitable: boolean;

  slotsID: number[];
  presentationID: number;
  userID: number;
}

interface PresentationUnavailabilitiesDTO {
  first: PresentationUnavailabilityDTO;
  second: PresentationUnavailabilityDTO;
}

const COMPUTATION_IN_PROGRESS = "There is a computation in progress."
const CHANGES_NOT_ALLOWED = "Currently, changes are not allowed to be performed."

export default function Page() {
    // const [users, setUsers] = useState<User[]>([]);
    const [errorMessage, setErrorMessage] = useState<string>('');
    const [presentations, setPresentations] = useState<Presentation[]>([]);
    // const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
    const [selectedPresentationId, setSelectedPresentationId] = useState<number | null>(null);
    const [slots, setSlots] = useState<SlotDTO[]>([]);
    const [dates, setDates] = useState<string[]>([]);
    const [hours, setHours] = useState<string[]>([]);
    // const [userUnavailabilitySlots, setUserUnavailabilitySlots] = useState<number[]>([]);
    // const [userUnsuitableSlots, setUserUnsuitableSlots] = useState<number[]>([]);
    const [presentationUnavailabilitySlots, setPresentationUnavailabilitySlots] = useState<number[]>([]);
    const [presentationUnsuitableSlots, setPresentationUnsuitableSlots] = useState<number[]>([]);
    const [presentationAdditionalUnavailabilitySlots, setAdditionalPresentationUnavailabilitySlots] = useState<number[]>([]);
    const [presentationAdditionalUnsuitableSlots, setAdditionalPresentationUnsuitableSlots] = useState<number[]>([]);
    const [presentationIgnoredUnsuitableSlots, setPresentationIgnoredUnsuitableSlots] = useState<number[]>([]);

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
                    }
                    // else if(axiosError.response?.status == 422){
                    //     setError(axiosError.response.data.message);
                    // } else {
                    //     setError('There was an error on the server side, please try again later.');
                    // }
                }
                // else {
                //     setError('There was an error on the server side, please try again later.');
                // }
            }
        }

        checkComputation()
    }, []);

    useEffect(() => {
        fetchData();
    }, []);
    
    const fetchData = async () => {
        try {
            const token = sessionStorage.getItem('token');
            // const userResponse = await axios.get<User[]>('http://localhost:8080/api/user', {
            //     headers: {
            //         Authorization: `Bearer ${token}`
            //     }
            // });
            // setUsers(userResponse.data);
            const response = await axios.get<Presentation[]>('http://localhost:8080/api/presentation', {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });
            setPresentations(response.data);
            const slotResponse = await axios.get<SlotDTO[]>(`http://localhost:8080/api/slot`, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });
            const data = slotResponse.data;
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

    const fetchPresentationUnavailabilitySlots = async (presentationId: number) => {
        try {
            const token = sessionStorage.getItem('token');
            const response1 = await axios.get<PresentationUnavailabilitiesDTO>(`http://localhost:8080/api/unavailability/presentation/${presentationId}`, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });
            const data1 = response1.data;
            const unavailabilityData = data1.first;
            const unsuitableData = data1.second;
            
            const response2 = await axios.get<PresentationUnavailabilitiesDTO>(`http://localhost:8080/api/unavailability/additional/presentation/${presentationId}`, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });
            const data2 = response2.data;
            const additionalUnavailabilityData = data2.first;
            const additionalUnsuitableData = data2.second;

            const response3 = await axios.get<IgnoredPresentationUnsuitableDTO>(`http://localhost:8080/api/unavailability/ignored/presentation/${presentationId}`, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });
            const ignoredUnsuitableData = response3.data;

            setPresentationUnavailabilitySlots(unavailabilityData.slotsID);
            setPresentationUnsuitableSlots(unsuitableData.slotsID);
            setAdditionalPresentationUnavailabilitySlots(additionalUnavailabilityData.slotsID);
            setAdditionalPresentationUnsuitableSlots(additionalUnsuitableData.slotsID);
            setPresentationIgnoredUnsuitableSlots(ignoredUnsuitableData.slotsID);
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
    
    const handlePresentationChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
        const selectedValue = parseInt(event.target.value);
        setSelectedPresentationId(selectedValue === 0 ? null : selectedValue);
        if (selectedValue !== null && selectedValue !== 0) {
            fetchPresentationUnavailabilitySlots(selectedValue);
        }
    };

    const handleUnsuitableSlotClick = async (presentationId: number | null, slotID: number, isIgnored: boolean) => {
        try {
            const token = sessionStorage.getItem('token');
            if (isIgnored) {
                const response = await axios.delete('http://localhost:8080/api/unavailability/ignored/presentation', {
                    data: { slotID, presentationID: presentationId } ,
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                });
                const slotList = response.data.slotsID;
                setPresentationIgnoredUnsuitableSlots(slotList);
            } else {
                const response = await axios.post<IgnoredPresentationUnsuitableDTO>(`http://localhost:8080/api/unavailability/ignored/presentation`, {
                    slotID,
                    presentationID: presentationId
                },{
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                });
                const slotList = response.data.slotsID;
                setPresentationIgnoredUnsuitableSlots(slotList);
            }
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
                    } else {
                        setErrorMessage('There was an error on the server side, please try again later.');
                    }
                } else {
                    setErrorMessage('There was an error on the server side, please try again later.');
                }
            } else {
                setErrorMessage('There was an error on the server side, please try again later.');
            }
        }
    }

    const calculateFinishHour = (startingHour: string): string => {
        const [hour, minute] = startingHour.split(':').map(parseFloat);
        const finishHour = hour + (minute + 45) / 60;
        const finishHourHour = Math.floor(finishHour);
        const finishHourMinute = Math.round((finishHour - finishHourHour) * 60);
        return `${finishHourHour.toString().padStart(2, '0')}:${finishHourMinute.toString().padStart(2, '0')}`;
    };

    const formatDate = (date: string): string => {
        const [year, month, day] = date.split('-');
        return `${day}-${month}-${year}`;
    };

    return (
        <div className="container">
            <h1 className="title" style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Presentation Unavailabilities Manager</h1>
            {isComputing && (<ComputationWarning />)}
            <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem' }}>
                <p>
                    This page allows you to view the current unavailabilities of every presentation.
                    The restrictions displayed on the table are the most prohibitive restriction assigned to the slot, between the base and additional restrictions.
                </p>
                <p>
                    You can also choose optional restrictions to ignore when computing assignments by clicking on the slot.
                    Slots with ignored optional restrictions are represented by slots with a dark green color.
                    If the base restriction is changed later, then this flag is removed.
                </p>
                <p>
                    The slots in this table may contain any of the following colors
                </p>
                <ol>
                    <li>{"Green Slot -> The slot has no restrictions."}</li>
                    <li>{"Yellow Slot -> The slot has a base optional restriction."}</li>
                    <li>{"Red Slot -> The slot has a base obligatory restriction."}</li>
                    <li>{"Dark Green Slot -> The slot has an optional restriction, but it has been chosen to be ignored."}</li>
                    <li>{"Dark Yellow Slot -> The slot has an additional optional restriction."}</li>
                    <li>{"Dark Red Slot -> The slot has an additional obligatory restriction."}</li>
                </ol>
            </div>
            {errorMessage && <div className="error-message"><FaExclamationCircle className="error-icon" /> {errorMessage}</div>}
            <select id="presentationSelect" className="input-field" value={selectedPresentationId || 0} onChange={handlePresentationChange}>
                <option value="0">Select one presentation</option>
                {presentations.sort((a, b) => a.id - b.id).map(presentation => (
                <option key={presentation.id} value={presentation.id}>
                    {presentation.thesisTitle}
                </option>
                ))}
                {/* <select className="input-field" value={presentationType} onChange={(e) => setPresentationType(e.target.value as PresentationType)}>
                    {Object.values(PresentationType).map(type => (
                        <option key={type} value={type}>{type}</option>
                ))}
                </select> */}
            </select>
            {slots.length === 0 ? (
                <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem'}}>
                    There are no slots.
                </div>
            ) : (
            <>
                <div className="table-container" style={{ overflowX: 'auto' }}>
                    <table className="slots-table">
                        <thead>
                            <tr>
                                <th className="hour-cell"></th>
                                {dates.sort().map(date => (
                                    <th key={date} className="date-cell">{formatDate(date)}</th>
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
                                        <td className="hour-cell">{startHour} - {finishHour}</td>
                                        {dates.sort().map(date => {
                                            const slot = slots.find(s => s.date === date && s.startingHour === hour);
                                            const slotId = parseInt(slot?.id ?? '0');
                                            let slotClassName = 'slot-not-exists'; // Default to slot-not-exists
                                            let unavailability = false
                                            let additionalUnavailability = false
                                            let unsuitable = false
                                            let additionalUnsuitable = false
                                            let ignoredUnsuitable = false
                                            if (slot) {
                                                //TODO Add userUnsuitableSlots
                                                // Slot exists, apply user unavailability logic if a user is selected 
                                                slotClassName = 'slot-exists'; // Default to slot-not-exists
                                                if (selectedPresentationId) {
                                                    // console.log("presentationUnavailabilitySlots.includes(parseInt(slot.id)): ", presentationUnavailabilitySlots.includes(parseInt(slot.id)))
                                                    // unavailability = presentationUnavailabilitySlots.includes(parseInt(slot.id))
                                                    unavailability = presentationUnavailabilitySlots.includes(slotId)
                                                    slotClassName = unavailability ? 'slot-unavailable' : slotClassName;
                                                    if (!unavailability) {
                                                        console.log("Check Additional Unavailability");
                                                        additionalUnavailability = presentationAdditionalUnavailabilitySlots.includes(slotId)
                                                        slotClassName = additionalUnavailability ? 'slot-additional-unavailable' : slotClassName
                                                    }
                                                    if (!(unavailability || additionalUnavailability)) {
                                                        console.log("Check Unsuitability");
                                                        unsuitable = presentationUnsuitableSlots.includes(slotId)
                                                        slotClassName = unsuitable ? 'slot-unsuitable' : slotClassName
                                                        if (!unsuitable) {
                                                            console.log("Check Additional Unsuitability");
                                                            additionalUnsuitable = presentationAdditionalUnsuitableSlots.includes(slotId)
                                                            slotClassName = additionalUnsuitable ? 'slot-additional-unsuitable' : slotClassName
                                                        }
                                                    }
                                                    if (!(unavailability || additionalUnavailability) && (unsuitable || additionalUnsuitable)) {
                                                        console.log("Check Ignore Unsuitability");
                                                        ignoredUnsuitable = presentationIgnoredUnsuitableSlots.includes(slotId)
                                                        slotClassName = ignoredUnsuitable ? 'slot-ignored' : slotClassName
                                                    }
                                                } else {
                                                    slotClassName = 'slot-exists';
                                                }
                                            }
                                            return (
                                                <td key={`${date}-${hour}`}
                                                    className={slotClassName}
                                                    // onClick={(unsuitable || additionalUnsuitable) ? () => handleUnsuitableSlotClick(selectedPresentationId, parseInt(slot.id)) : undefined}
                                                    // onClick={(slot && !isComputing && !stopChange && (unsuitable || additionalUnsuitable)) ? () => handleUnsuitableSlotClick(selectedPresentationId, slotId, ignoredUnsuitable) : undefined}
                                                    onClick={(slot && !isComputing && (unsuitable || additionalUnsuitable)) ? () => handleUnsuitableSlotClick(selectedPresentationId, slotId, ignoredUnsuitable) : undefined}
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
}
