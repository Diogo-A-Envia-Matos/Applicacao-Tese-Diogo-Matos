"use client";

import ComputationWarning from '@/app/components/computationWarning/component';
import React, { useState, useEffect, useRef, useContext } from 'react';
import { FaTimes, FaCheck, FaExclamationCircle, FaInfoCircle } from 'react-icons/fa';
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import dayjs, {Dayjs} from 'dayjs';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';
import axios, { AxiosError } from 'axios';

import { StopChangeContext } from '@/app/context/stopChangeContext/context';

import './Page.css';

interface SlotDTO {
  id: string;
  date: string;
  startingHour: string; 
}

interface ErrorResponse {
  message: string;
}

const COMPUTATION_IN_PROGRESS = "There is a computation in progress."
const CHANGES_NOT_ALLOWED = "Currently, changes are not allowed to be performed."

const Page: React.FC = () => {
  const [slots, setSlots] = useState<SlotDTO[]>([]);
  const [dates, setDates] = useState<string[]>([]);
  const [hours, setHours] = useState<string[]>([]); 
  const [deleteConfirmation, setDeleteConfirmation] = useState<{ show: boolean, slotId?: string }>({ show: false });
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [addSeveralSlotsErrorMessage, setAddSeveralSlotsErrorMessage] = useState<string>(''); 
  const [showAddSlotForm, setShowAddSlotForm] = useState<boolean>(false);
  const [newSlotDate, setNewSlotDate] = useState<Dayjs | null>(null);
  const [newSlotStartingHour, setNewSlotStartingHour] = useState<string | null>(null); 
  const [newSlotStartingMinute, setNewSlotStartingMinute] = useState<string | null>(null); 
  const [showAddSeveralSlotsForm, setShowAddSeveralSlotsForm] = useState<boolean>(false);
  const [addSeveralSlotDate, setAddSeveralSlotDate] = useState<Dayjs | null>(null);
  const [numberOfSlots, setNumberOfSlots] = useState<number>(1);
  const [deleteConfirmationErrorMessage, setDeleteConfirmationErrorMessage] = useState<string>(''); 
  const [isComputing, setIsComputing] = useState(false);
  
  // const { stopChange, toggleStopChange, changeStopChange } = useContext(StopChangeContext);

  const modalRef = useRef<HTMLDivElement>(null);

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
        }
        else {
          setErrorMessage('There was an error on the server side, please try again later.');
        }
      }
    }

    checkComputation()
  }, []);

  useEffect(() => {
    fetchData();
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleClickOutside = (event: MouseEvent) => {
    if (modalRef.current && !modalRef.current.contains(event.target as Node)) {
      setDeleteConfirmation({ show: false });
      setDeleteConfirmationErrorMessage('');
    }
  };

  const fetchData = async () => {
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

  const formatDate = (date: string): string => {
    const [year, month, day] = date.split('-');
    return `${day}-${month}-${year}`;
  };

  const deleteSlot = async (slotId: string | undefined) => {
    if(slotId == undefined){
      console.error("Invalid slotId");
      return;
    }
    try {
      const token = sessionStorage.getItem('token');
      await axios.delete(`http://localhost:8080/api/slot/${slotId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      setDeleteConfirmation({ show: false });
      fetchData();
    } catch (error) {
      console.error('Error deleting slot:', error);
      setDeleteConfirmationErrorMessage('Error deleting slot');
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
  };

  const handleSubmitNewSlot = async () => {
    try {
      if (!newSlotDate || !newSlotStartingHour || !newSlotStartingMinute) {
        setErrorMessage('Please select the date, the hour and the minutes.');
        return;
      }
      const today = dayjs().startOf('day');
      if (newSlotDate.isBefore(today) || newSlotDate.startOf('day').isSame(today)) {
        setErrorMessage('Please insert a date in the future.');
        return;
      }
      const adjustedDate = newSlotDate?.format('YYYY-MM-DD');
      const startingHour = `${newSlotStartingHour}:${newSlotStartingMinute}`;
      const token = sessionStorage.getItem('token');
      await axios.post('http://localhost:8080/api/slot', {
        date: adjustedDate,
        startingHour: startingHour
      }, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      setShowAddSlotForm(false);
      fetchData();
    } catch (error) {
      console.error('Error adding slot:', error);
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
  };

  const handleSubmitSeveralSlots = async () => {
    try {  
      if (!addSeveralSlotDate) {
        setAddSeveralSlotsErrorMessage('Please select date.');
        return;
      }
      const today = dayjs().startOf('day');
      if (addSeveralSlotDate.isBefore(today) || addSeveralSlotDate.startOf('day').isSame(today)) {
        setAddSeveralSlotsErrorMessage('Please insert a date in the future.');
        return;
      }
      const formattedDate = addSeveralSlotDate?.format('YYYY-MM-DD');
      const token = sessionStorage.getItem('token');
      await axios.post('http://localhost:8080/api/slot/addSeveral', {
        date: formattedDate,
        numberOfSlots: numberOfSlots
      }, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      
      setShowAddSeveralSlotsForm(false);
      fetchData();
    } catch (error) {
      console.error('Error adding slots:', error);
      //setAddSeveralSlotsErrorMessage(error.response?.data?.message || 'Error adding slots');
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
            setAddSeveralSlotsErrorMessage(COMPUTATION_IN_PROGRESS);
          } else {
            setAddSeveralSlotsErrorMessage('Please insert a date with no slots.');
          }
        } else {
          setAddSeveralSlotsErrorMessage('There was an error on the server side, please try again later.');
        }
      } else {
        setAddSeveralSlotsErrorMessage('There was an error on the server side, please try again later.');
      }
    }
  };

  const toggleAddSlotForm = () => {
    if (showAddSeveralSlotsForm) {
      setShowAddSeveralSlotsForm(false);
    }
    setShowAddSlotForm(prevState => !prevState);
    if (!showAddSlotForm) {
      setNewSlotDate(null);
      setNewSlotStartingHour(null);
      setNewSlotStartingMinute(null);
      setErrorMessage('');
    }
  };
  
  const toggleAddSeveralSlotsForm = () => {
    if (showAddSlotForm) {
      setShowAddSlotForm(false);
    }
    setShowAddSeveralSlotsForm(prevState => !prevState);
    if (!showAddSeveralSlotsForm) {
      setAddSeveralSlotDate(null);
      setNumberOfSlots(1);
      setAddSeveralSlotsErrorMessage('');
    }
  };

  dayjs.extend(utc);
  dayjs.extend(timezone);
  //const tomorrow = dayjs().add(1, 'day').startOf('day').tz('UTC');

  return (
    <div className="container">
      <div className="header">
        <h1 className="title" style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Existing Slots</h1>
        {isComputing && (<ComputationWarning />)}
        <div className="message">
          <p>
            Here you can see the slots in the system, in a table organized by dates and time slots.
          </p>
          <p>
            It is possible to add new slots by pressing the "Add Slot" or the "Add Several Slots" buttons and filling the form that appear afterwards.
          </p>
          <ol>
            <li>{"When adding one slot, you choose a date and a starting hour for the new slot."}</li>
            <li>{"When adding several slots, you choose a date without any slots and choose the number of slots to add. These slots last for 45 minutes and start every full hour, starting from 9:00."}</li>
          </ol>
        </div>
      </div>
      <div className="table-container" style={{ overflowX: 'auto' }}>
      {slots.length === 0 ? (
          <>
            <div className="message" style={{ whiteSpace: 'pre-line'}}>
                There are no slots.
            </div>
            <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem'}}>
                You can add slots with the buttons bellow.
            </div>
          </>
        ) : (
          <>
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
                        return (
                          <td key={`${date}-${hour}`} className={slot ? 'slot-exists' : 'slot-not-exists'}>
                            {slot ? (
                              <div className="slot-content">
                                <span>Slot Exists</span>
                                {(!isComputing) && (
                                  <button className="delete-button" onClick={() => setDeleteConfirmation({ show: true, slotId: slot.id })}>
                                    <FaTimes />
                                  </button>
                                )}
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
      <div style={{ marginTop: '10px' }}>
        <button className="add-slot-button" onClick={toggleAddSlotForm} disabled={isComputing}>
          {showAddSlotForm ? 'Cancel' : 'Add Slot'}
        </button>
        <button className="add-slot-button" onClick={toggleAddSeveralSlotsForm} disabled={isComputing}>
          {showAddSeveralSlotsForm ? 'Cancel' : 'Add Several Slots'}
        </button>
      </div>
      {showAddSlotForm && (
        <div>
          <div className="add-slot-form" >
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <DateTimePicker
                label="Select Date and Hour"
                ampm={false}
                format="DD-MM-YY HH:mm"
                onChange={(date) => {
                  setErrorMessage('');
                  if (date) {
                    setNewSlotDate(date);
                    setNewSlotStartingHour(dayjs(date).format('HH'));
                    setNewSlotStartingMinute(dayjs(date).format('mm'));
                  } else {
                    setNewSlotDate(null);
                    setNewSlotStartingHour(null);
                    setNewSlotStartingMinute(null);
                  }
                }}
              />
            </LocalizationProvider>
            <button className="submit-slot-button" onClick={handleSubmitNewSlot} disabled={isComputing}>Submit</button>
          </div>
          {errorMessage && (<div className="error-message"><FaExclamationCircle className="error-icon" />{errorMessage}</div>)}
        </div>
      )}
      {showAddSeveralSlotsForm && (
        <div>
          <p className='message-spacing'>To add several slots you need to choose the day and the number of slots you want to add in that day. </p>
          <p className='message-spacing'>This slots have default starting hours: 9:00, 10:00, 11:00, 12:00, 14:00, 15:00, 16:00, 17:00.</p>
          <div className="add-slot-form" >
            <div style={{ marginRight: '10px' }}>
              <LocalizationProvider dateAdapter={AdapterDayjs}> 
                <DatePicker
                  label="Select Date"
                  onChange={(date) => {
                    setAddSeveralSlotsErrorMessage('');
                    if(date){
                      setAddSeveralSlotDate(date);
                    } else{
                      setAddSeveralSlotDate(null);
                    }
                  }}
                  format="DD-MM-YYYY"
                />
              </LocalizationProvider>
            </div>
            <select
              value={numberOfSlots}
              onChange={e =>{
                setNumberOfSlots(parseInt(e.target.value));
                setAddSeveralSlotsErrorMessage('');
              }}
              className="input-field"
            >
              {Array.from({ length: 8 }, (_, i) => i + 1).map(number => (
                <option key={number} value={number}>{number}</option>
              ))}
            </select>
            <button className="submit-slot-button" onClick={handleSubmitSeveralSlots} disabled={isComputing}>Submit</button>
          </div>
          {addSeveralSlotsErrorMessage && (<div className="error-message"><FaExclamationCircle className="error-icon" />{addSeveralSlotsErrorMessage}</div>)}
        </div>
      )}
      {deleteConfirmation.show && (
        <div className="confirm-delete-overlay">
          <div className="confirm-delete-modal" ref={modalRef}>
            <p>Are you sure you want to delete this slot?</p>
            <div className="confirm-delete-buttons">
              <button className="confirm-delete-button" onClick={() => deleteSlot(deleteConfirmation.slotId)}>
                <FaCheck /> Yes
              </button>
              <button className="confirm-delete-button" onClick={() => {
                    setDeleteConfirmation({ show: false });
                    setDeleteConfirmationErrorMessage(''); // Reset the error message state when the user clicks "No"
                }}>
                <FaTimes /> No
              </button>
            </div>
            {deleteConfirmationErrorMessage && <div className="error-message"><FaExclamationCircle className="error-icon"/> {deleteConfirmationErrorMessage}</div>}
          </div>
        </div>
      )}
    </div>
  );
};

export default Page;

