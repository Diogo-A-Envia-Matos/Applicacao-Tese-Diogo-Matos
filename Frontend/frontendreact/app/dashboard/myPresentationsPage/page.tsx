"use client";

import ComputationWarning from '@/app/components/computationWarning/component';
import React, { useState, useEffect, useContext } from 'react';
import axios, { AxiosError } from 'axios';
import { FaExclamationCircle } from 'react-icons/fa';

import { StopChangeContext } from '@/app/context/stopChangeContext/context';

import './Page.css'; 

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
    arguerName: string;
    optionalParticipant1Name: string | null;
    optionalParticipant2Name: string | null;
}
interface User {
    id: number;
    name: string;
}

enum PresentationType {
    ALL = "All my presentations",
    ARGUER = "My presentations as examiner",
    ADVISER = "My presentations as adviser",
    EXTRA_PARTICIPANT = "My presentations as extra participant"
}

interface ErrorResponse {
  message: string;
}

const COMPUTATION_IN_PROGRESS = "There is a computation in progress."
const CHANGES_NOT_ALLOWED = "Currently, changes are not allowed to be performed."

export default function Page() {
    const [error, setError] = useState<string | null>(null);
    const [presentations, setPresentations] = useState<Presentation[]>([]);
    const [presentationType, setPresentationType] = useState<PresentationType>(PresentationType.ALL);
    const [users, setUsers] = useState<User[]>([]);
    const [addPresentationErrorMessage, setAddPresentationErrorMessage] = useState<string>('');
    const [loggedInUserName, setLoggedInUserName] = useState<string>(''); // Initialize loggedInUserName state variable with an empty string
    const [isComputing, setIsComputing] = useState(false);
    
    const { stopChange, toggleStopChange, changeStopChange } = useContext(StopChangeContext);

    useEffect(() => {
        const checkComputation = async () => {
            try{
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
                    } else if(axiosError.response?.status == 422){
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

    const fetchPresentationData = async () => {
        try {
            const token = sessionStorage.getItem('token');
            const userId = sessionStorage.getItem('id');
            let url = '';
            switch (presentationType) {
                case PresentationType.ALL:
                    url = `http://localhost:8080/api/presentation/user/${userId}`;
                    break;
                case PresentationType.ARGUER:
                    url = `http://localhost:8080/api/presentation/arguer/${userId}`;
                    break;
                case PresentationType.ADVISER:
                    url = `http://localhost:8080/api/presentation/adviser/${userId}`;
                    break;
                case PresentationType.EXTRA_PARTICIPANT:
                    url = `http://localhost:8080/api/presentation/optional/${userId}`;
                    break;
                default:
                    url = `http://localhost:8080/api/presentation/user/${userId}`;
            }
            const response = await axios.get<Presentation[]>(url, {
                headers: {
                  Authorization: `Bearer ${token}`
                }
            });
            setPresentations(response.data);
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
                  //setErrorMessage('There was an error on the server side, please try again later.');
              }
              } else {
                //setErrorMessage('There was an error on the server side, please try again later.');
              }
        }
    };

    useEffect(() => {
        fetchPresentationData();
    }, [presentationType]);

    const formatStartingHour = (startingHour: string | null): string => {
        if (startingHour === null) return 'Not assigned yet';
        
        // Parse the time string to get the hour and minute components
        const [hour, minute] = startingHour.split(':').map(Number);
    
        // Ensure hour and minute are padded with leading zeros if necessary
        const formattedHour = hour.toString().padStart(2, '0');
        const formattedMinute = minute.toString().padStart(2, '0');
    
        return `${formattedHour}:${formattedMinute}`;
    };

    useEffect(() => {
        fetchUsers();
        fetchLoggedInUserName();
    }, []);
    
    const fetchUsers = async () => {
        try {
            const token = sessionStorage.getItem('token');
            const response = await axios.get<User[]>('http://localhost:8080/api/user/names', {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
            setUsers(response.data);
        } catch (error) {
            console.error('Error fetching users:', error);
        }
    };

    const handleOptionalChange = async (userName: string, presentationId: number) => {
        try{
            setAddPresentationErrorMessage('');
            if(userName === ""){
                const token = sessionStorage.getItem('token');
                await axios.delete(`http://localhost:8080/api/presentation/delete/optional1/${presentationId}`, {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                });
                fetchPresentationData();
            } else {
                const token = sessionStorage.getItem('token');
                const response = await axios.post(`http://localhost:8080/api/presentation/add/optional1/${presentationId}`, {name: userName}, {
                    headers: {
                    Authorization: `Bearer ${token}`
                    }
                });
                fetchPresentationData();
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
                } else if(axiosError.response?.status == 500){
                    setAddPresentationErrorMessage('There was an error with the server. Please try again later.');
                } else if(axiosError.response?.status == 422){
                    setAddPresentationErrorMessage('You can not add as an extra participant the examiner, neither the adviser, nor the other extra participant (if there is one).');
                } else if (axiosError.response?.status === 409) {
                    // const errorMessage = axiosError.response?.data.message
                    const errorMessage = axiosError.response?.data
                    console.log("errorMessage: ", errorMessage)
                    if (errorMessage == COMPUTATION_IN_PROGRESS) {
                        setIsComputing(true);
                        setError(COMPUTATION_IN_PROGRESS);
                    } else if (errorMessage == CHANGES_NOT_ALLOWED) {
                        changeStopChange(true);
                        setError(CHANGES_NOT_ALLOWED);
                    } else {
                        setError('There was an error on the server side, please try again later.');
                    }
                } else {
                    setAddPresentationErrorMessage('There was an error on the server side, please try again later.');
                }
            } else {
            setAddPresentationErrorMessage('There was an error on the server side, please try again later.');
            }
        }
    };

    const fetchLoggedInUserName = async () => {
        try {
            const userId = sessionStorage.getItem('id');
            const token = sessionStorage.getItem('token');
            const response = await axios.get<User>(`http://localhost:8080/api/user/${userId}`, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });
            setLoggedInUserName(response.data.name);
        } catch (error) {
            console.error('Error fetching logged-in user data:', error);
            // Handle error fetching logged-in user data
        }
    };
    

    const isLoggedInUserAdviserOrArguer = (presentation: Presentation): boolean => {
        return loggedInUserName === presentation.adviserName || loggedInUserName === presentation.arguerName;
    };
    
    return (
        <div className="container">
            <h2 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>My Presentations</h2>
            <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem' }}>
                {/* {message} */}
                <p>
                    Here you can see information on every presentation where your are a participant.
                    You can also filter the presentations according to your role in them.
                </p>
                <p>
                    For presentations where you are the adviser or the examiner, and can also assign or change the first extra participant.
                </p>
            </div>
            <div className="dropdown">
                <select className="input-field" value={presentationType} onChange={(e) => setPresentationType(e.target.value as PresentationType)}>
                    {Object.values(PresentationType).map(type => (
                        <option key={type} value={type}>{type}</option>
                    ))}
                </select>
            </div>
            {error && <div className="error-message"><FaExclamationCircle className="error-icon" /> {error}</div>}
            {addPresentationErrorMessage && <div className="manager-error-message"><FaExclamationCircle className="error-icon" /> {addPresentationErrorMessage}</div>}
            {presentations.length === 0 ? (
                <div>You don't have presentations.</div>
            ) : (
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
                            {presentations.sort((a, b) => a.id - b.id).map((presentation) => (
                                <tr key={presentation.id}>
                                    <td>{presentation.id}</td>
                                    <td>{presentation.studentNumber}</td>
                                    <td>{presentation.studentName}</td>
                                    <td>{presentation.thesisTitle}</td>
                                    <td>{presentation.roomNumber ?? 'Not assigned yet'}</td>
                                    <td>{presentation.roomBuilding ?? 'Not assigned yet'}</td>
                                    <td>{presentation.date ?? 'Not assigned yet'}</td>
                                    <td>{formatStartingHour(presentation.startingHour)}</td>
                                    <td>{presentation.adviserName}</td>
                                    <td>{presentation.arguerName}</td>
                                    <td>
                                        {(!isComputing && !stopChange && isLoggedInUserAdviserOrArguer(presentation)) ? (
                                            <select 
                                                className="drop-down" 
                                                value={presentation.optionalParticipant1Name !== null ? presentation.optionalParticipant1Name: ""}
                                                onChange={(e) => handleOptionalChange(e.target.value, presentation.id)}>
                                                <option value= "">No extra participant</option>
                                                {users.sort((a, b) => a.name.localeCompare(b.name)).map(user => (
                                                    <option key={user.id} value={user.name}>
                                                        {user.name}
                                                    </option>
                                                    ))}
                                            </select>
                                        ) : (
                                            presentation.optionalParticipant1Name ?? ''
                                        )}
                                    </td>
                                    <td>{presentation.optionalParticipant2Name ?? ''}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
