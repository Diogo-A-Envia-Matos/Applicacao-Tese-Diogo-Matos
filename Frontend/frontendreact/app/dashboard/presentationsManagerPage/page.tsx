"use client";

import ComputationWarning from '@/app/components/computationWarning/component';
import React, { useState, useEffect, useRef, useContext } from 'react';
import { FaCheckCircle, FaExclamationCircle, FaTimes, FaCheck} from 'react-icons/fa';
import axios, { AxiosError } from 'axios';
import uploadPresentationsTemplate from '/public/uploadPresentationsTemplate.svg';
import Image from 'next/image';

import { StopChangeContext } from '@/app/context/stopChangeContext/context';

import './Page.css'; 

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

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
}

const COMPUTATION_IN_PROGRESS = "There is a computation in progress."
const INCORRECT_EXCEL_FORMAT = "Excel file not formatted correctly."

const Page: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [presentations, setPresentations] = useState<Presentation[]>([]);
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploaded, setUploaded] = useState(false);
  const [uploadError, setUploadError] = useState(false);
  const [formatError, setFormatError] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [deleteConfirmation, setDeleteConfirmation] = useState<{
    show: boolean;
    presentationId?: number;
  }>({ show: false });
  const [deleteConfirmationErrorMessage, setDeleteConfirmationErrorMessage] = useState<string>('');
  const [showAddPresentation, setShowAddPresentation] = useState(false);
  // const [newPresentation, setNewPresentation] = useState<{ studentNumber: string; studentName: string; thesisTitle: string; adviserName: string; adviserId: number; arguerName: string; arguerId: number }>({
  const [newPresentation, setNewPresentation] = useState<{ studentNumber: string; studentName: string; thesisTitle: string; adviserName: string; arguerName: string }>({
    studentNumber: '',
    studentName: '',
    thesisTitle: '',
    adviserName: '',
    // adviserId: -1,
    arguerName: '',
    // arguerId: -1,
  });
  const [addPresentationErrorMessage, setAddPresentationErrorMessage] = useState<string>('');
  const [isComputing, setIsComputing] = useState(false);
  
  // const { stopChange, toggleStopChange, changeStopChange } = useContext(StopChangeContext);

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
          }
          else if(axiosError.response?.status == 422){
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

  const modalRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
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

  const fetchPresentationsData = async (userId: number | null) => {
    const token = sessionStorage.getItem('token');
    let url = 'http://localhost:8080/api/presentation';
    if (userId !== null) {
      url = `http://localhost:8080/api/presentation/user/${userId}`;
    }
    try {
      const response = await axios.get<Presentation[]>(url, {
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

  const handleUserChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedValue = parseInt(event.target.value);
    setSelectedUserId(selectedValue === 0 ? null : selectedValue);
  };

  useEffect(() => {
    fetchPresentationsData(selectedUserId);
  }, [selectedUserId]);

  const formatStartingHour = (startingHour: string | null): string => {
    if (startingHour === null) return 'Not assigned yet';
    
    // Parse the time string to get the hour and minute components
    const [hour, minute] = startingHour.split(':').map(Number);

    // Ensure hour and minute are padded with leading zeros if necessary
    const formattedHour = hour.toString().padStart(2, '0');
    const formattedMinute = minute.toString().padStart(2, '0');

    return `${formattedHour}:${formattedMinute}`;
  };

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files.length > 0) {
      setFile(event.target.files[0]);
      setUploadError(false);
      setFormatError(false);
      setErrorMessage('');
      setAddPresentationErrorMessage('');
      setUploaded(false);
    }
  };

  const handleDrop = (event: React.DragEvent<HTMLDivElement>) => {
    if (isComputing) {
      return
    }
    event.preventDefault();
    event.stopPropagation();
    setUploadError(false);
    setFormatError(false);
    setErrorMessage('');
    setAddPresentationErrorMessage('');
    if (!uploading && event.dataTransfer.files && event.dataTransfer.files.length > 0) {
      setFile(event.dataTransfer.files[0]);
    }
  };

  const handleUpload = async () => {
    if (file) {
      setUploading(true);
      const formData = new FormData();
      formData.append('excelFile', file);
      try {
        const token = sessionStorage.getItem('token');
        const response = await axios.post('http://localhost:8080/api/presentation/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: `Bearer ${token}`
          }
        });
        // Handle successful upload
        setUploaded(true);
        fetchPresentationsData(selectedUserId);
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
          } else if (axiosError.response?.status === 422) {
            const errorMessage = axiosError.response.data;
            if (errorMessage === undefined || errorMessage.toString() === INCORRECT_EXCEL_FORMAT) {
              const excelMessage = '\nMake sure your excel file has a similar structure to this one:';
              setErrorMessage(`${errorMessage}${excelMessage}`);
              setFormatError(true);
            } else {
              setErrorMessage(`${errorMessage}`);
            }
            //setErrorMessage('There was an error, make sure your excel file has a similar structure to this one:');
            setUploadError(true);
          } else {
            setErrorMessage('There was an error on the server side, please try again later.');
            setUploadError(true);
          }
        } else {
          setErrorMessage('There was an error on the server side, please try again later.');
          setUploadError(true);
        }
      } finally {
        setUploading(false);
        setFile(null);
      }
    }
  };

  const handleDragOver = (event: React.DragEvent<HTMLDivElement>) => {
    if (isComputing) {
      return
    }
    event.preventDefault();
    event.stopPropagation();
  };

  const handleRemoveFile = () => {
    setFile(null);
  };

  const deletePresentation = async (presentationId: number) => {
    try {
      const token = sessionStorage.getItem('token');
      // Perform deletion
      await axios.delete(`http://localhost:8080/api/presentation/${presentationId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      setPresentations(prevPresentations=> prevPresentations.filter(presentation => presentation.id !== presentationId));

      // Close the confirmation modal
      setDeleteConfirmation({ show: false });
      setDeleteConfirmationErrorMessage('');
    } catch (error) {
      console.error('Error deleting user:', error);
      setDeleteConfirmationErrorMessage('Failed to delete user. Please try again.');
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
            setAddPresentationErrorMessage(COMPUTATION_IN_PROGRESS);
          } else {
            setAddPresentationErrorMessage('There was an error on the server side, please try again later.');
          }
        } else {
          setErrorMessage('There was an error on the server side, please try again later.');
        }
      } else {
        setErrorMessage('There was an error on the server side, please try again later.');
      }
    }
  };

  const handleClickOutside = (event: MouseEvent) => {
    if (modalRef.current && !modalRef.current.contains(event.target as Node)) {
      // Clicked outside the modal, treat it as clicking "No"
      setDeleteConfirmation({ show: false });
      setDeleteConfirmationErrorMessage('');
    }
  };

  useEffect(() => {
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []); // Empty dependency array to run the effect only once on component mount

  const handleAddPresentation = () => {
    setShowAddPresentation(prevState => !prevState);
    if (!showAddPresentation) {
        // setNewPresentation({ studentNumber: '', studentName: '', thesisTitle: '', adviserName: '', adviserId: -1, arguerName: '', arguerId: -1 }); // Reset input fields when showing the add presentation form
        setNewPresentation({ studentNumber: '', studentName: '', thesisTitle: '', adviserName: '', arguerName: '', }); // Reset input fields when showing the add presentation form
        setErrorMessage(''); // Clear any previous error messages
        setAddPresentationErrorMessage('');
      }
  };

  const handleSubmitPresentation = async () => {
    // if (!newPresentation.studentNumber || !newPresentation.studentName || !newPresentation.thesisTitle || !newPresentation.adviserName || !newPresentation.adviserId || !newPresentation.arguerName || !newPresentation.arguerId ) {
    if (!newPresentation.studentNumber || !newPresentation.studentName || !newPresentation.thesisTitle || !newPresentation.adviserName || !newPresentation.arguerName) {
      setAddPresentationErrorMessage('Please enter all information.');
      return;
    }
    try{
      const token = sessionStorage.getItem('token');
      const response = await axios.post('http://localhost:8080/api/presentation', 
        newPresentation
      ,{
        headers: {
          Authorization: `Bearer ${token}`
        }
      })
      setPresentations(prevPresentations => [...prevPresentations, response.data]);
      // setNewPresentation({ studentNumber: '', studentName: '', thesisTitle: '', adviserName: '', adviserId: '', arguerName: '', arguerId: '' });
      setNewPresentation({ studentNumber: '', studentName: '', thesisTitle: '', adviserName: '', arguerName: '' });
      setAddPresentationErrorMessage(''); // Clear any previous error messages
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
            setAddPresentationErrorMessage(COMPUTATION_IN_PROGRESS);
          } else {
            setAddPresentationErrorMessage('There was an error on the server side, please try again later.');
          }
        } else if(axiosError.response?.status == 500){
          setAddPresentationErrorMessage('There was an error with the server. Please try again later.');
        } else {
          setAddPresentationErrorMessage('There was an error on the server side, please try again later.');
      }
      } else {
        setAddPresentationErrorMessage('There was an error on the server side, please try again later.');
      }
    }
  };

  const handleOptionalChange = async (userName: string, presentationId: number) => {
    try{
      setAddPresentationErrorMessage('');
      if(userName === ""){
        const token = sessionStorage.getItem('token');
        await axios.delete(`http://localhost:8080/api/presentation/delete/optional2/${presentationId}`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        fetchPresentationsData(null);
      } else{
        const token = sessionStorage.getItem('token');
        const response = await axios.post(`http://localhost:8080/api/presentation/add/optional2/${presentationId}`, {name: userName}, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        fetchPresentationsData(null);
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
            setAddPresentationErrorMessage(COMPUTATION_IN_PROGRESS);
          } else {
            setAddPresentationErrorMessage('There was an error on the server side, please try again later.');
          }
        } else if(axiosError.response?.status == 500){
          setAddPresentationErrorMessage('There was an error with the server. Please try again later.');
        } else if(axiosError.response?.status == 422){
          setAddPresentationErrorMessage('You can not add as an extra participant the examiner, neither the adviser, neither the other extra participant (if there is one).');
        } else {
          setAddPresentationErrorMessage('There was an error on the server side, please try again later.');
      }
      } else {
        setAddPresentationErrorMessage('There was an error on the server side, please try again later.');
      }
    }
  };

  return (
    <div className="page-container">
      <h2 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Presentations Manager</h2>
      {isComputing && (<ComputationWarning />)}
      <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem' }}>
        <p>
            Here you can see information on every presentation.
            You can also filter the presentations to only show presentations in which a given user is a participant.
        </p>
        <p>
            It is possible to add presentations by pressing the "Add One Presentation" button and filling the form that appears or by adding multiple presentations by submitting an excel file.
        </p>
        <p>
            It is also possible to delete presentations by clicking the cross on the "Action" column of the corresponding presentation.
        </p>
      </div>
      <div className = "upload" style={{ textAlign: 'center', marginTop: '1rem' }}>
        {!file && (
          <div
            id="drop-zone"
            className={(isComputing) ? "drop-zone cross-out" : "drop-zone"}
            onDrop={handleDrop}
            onDragOver={handleDragOver}
          >
            <h1>Here you can upload an excel file with all the presentations information.</h1>
            <p style={{ marginBottom: '10px' }}>Before uploading the file, ensure that the names of the examiner and the adviser match the ones they are registered with and are case sensitive.</p>
            <p style={{ marginBottom: '20px', color: 'blue' }}>Drag & Drop your file here</p>
            <input
              type="file"
              id="file-input"
              className={"file-input-button"}
              accept=".xlsx, .xls"
              onChange={handleFileChange}
              style={{ display: 'none' }}
              disabled={isComputing}
            />
            <label htmlFor="file-input" className={(isComputing) ? "custom-file-upload-disabled" : "custom-file-upload"}>
              Choose File
            </label>
          </div>
        )}
        {file && (
          <div style={{ marginBottom: '20px', display: 'inline-block' }}>
            <div
              className={(isComputing) ? "drop-zone file-loaded cross-out" : "drop-zone file-loaded"}
            >
              <p style={{ margin: '0' }}>Selected file: {file.name}</p>
              <button onClick={handleRemoveFile} style={{ marginLeft: '10px', verticalAlign: 'middle' }}>
                <FaTimes style={{ fontSize: '12px' }} />
              </button>
            </div>
          </div>
        )}
        {file && (
          <button
            onClick={handleUpload}
            style={{
              padding: '10px 20px',
              borderRadius: '5px',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              cursor: 'pointer',
              display: 'block',
              margin: 'auto',
              marginBottom: '1rem'
            }}
            disabled={uploading || isComputing}
          >
            {uploading ? 'Uploading...' : 'Upload'}
          </button>
        )}
        {uploaded && (
          <div style={{ marginBottom: '20px', color: 'green', display: 'flex', justifyContent: 'center' }}>
              <FaCheckCircle style={{ marginLeft: '5px' }} />
              <p style={{ marginLeft: '5px' }}>File uploaded successfully</p>
          </div>
        )}
        {uploadError && (
          <div style={{ marginBottom: '20px', color: 'red', display: 'flex', alignItems: 'center', flexDirection: 'column' }}>
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <FaExclamationCircle className="icon" />
              <span style={{ marginLeft: '5px', whiteSpace: 'pre-line'}}>{errorMessage}</span>
            </div>
            {(formatError) && (errorMessage !== '' && errorMessage !== 'There was an error on the server side, please try again later.') && (
              <div>
                <Image src={uploadPresentationsTemplate} alt="Excel Template" /> 
              </div>
            )}
          </div>
        )}
      </div>
      <div style={{ marginTop: '10px' }}>
        <button className="add-presentation-button" onClick={handleAddPresentation} disabled={isComputing}>
          {showAddPresentation ? 'Cancel' : 'Add One Presentation'}
        </button>
      </div>
      {showAddPresentation && (
        <div className="add-presentation-form">
          <div>
            <input
              className="input-field"
              type="text"
              placeholder="Student Number"
              value={newPresentation.studentNumber}
              onChange={e => setNewPresentation({ ...newPresentation, studentNumber: e.target.value })}
            />
            <input
              className="input-field"
              type="text"
              placeholder="Student Name"
              value={newPresentation.studentName}
              onChange={e => setNewPresentation({ ...newPresentation, studentName: e.target.value })}
            />
            <input
              className="input-field"
              type="text"
              placeholder="Thesis Title"
              value={newPresentation.thesisTitle}
              onChange={e => setNewPresentation({ ...newPresentation, thesisTitle: e.target.value })}
            />
            <input
              className="input-field"
              type="text"
              list="adviser-list"
              placeholder="Adviser Name"
              value={newPresentation.adviserName}
              // value={{adviserId: newPresentation.adviserId, adviserName: newPresentation.adviserName}}
              onChange={e => setNewPresentation({ ...newPresentation, adviserName: e.target.value })}
              // onChange={e => setNewPresentation({ ...newPresentation, adviserId: e.target.value.adviserId, adviserName: e.target.value.adviserName })}
            />
            <datalist id="adviser-list">
              {users.sort((a, b) => a.name.localeCompare(b.name)).map((user) =>(
                // <option key={`adviser-${user.id}`} label={user.name} value={{ adviserId: user.id, adviserName: user.name }}></option>
                <option key={`adviser-${user.id}`} label={user.name} value={user.name}></option>
              ))}
            </datalist>
            <input
              className="input-field"
              type="text"
              list="arguer-list"
              placeholder="Examiner Name"
              value={newPresentation.arguerName}
              // value={{arguerId: newPresentation.arguerId, arguerName: newPresentation.arguerName}}
              onChange={e => setNewPresentation({ ...newPresentation, arguerName: e.target.value })}
              // onChange={e => setNewPresentation({ ...newPresentation, arguerId: e.target.value.arguerId, arguerName: e.target.value.arguerName })}
            />
            <datalist id="arguer-list">
              {users.sort((a, b) => a.name.localeCompare(b.name)).map((user) =>(
                // <option key={`arguer-${user.id}`} label={user.name} value={{ arguerId: user.id, arguerName: user.name }}></option>
                <option key={`arguer-${user.id}`} label={user.name} value={user.name}></option>
              ))}
            </datalist>
            {/* <select id="adviserSelect" className="input-field" value={selectedUserId || 0} onChange={handleUserChange}>
              <option value="0">All users presentations</option>
              {users.sort((a, b) => a.name.localeCompare(b.name)).map(user => (
                <option key={user.id} value={user.id}>
                  {user.name}
                </option>
              ))}
            </select>
            <select id="examinerSelect" className="input-field" value={selectedUserId || 0} onChange={handleUserChange}>
              <option value="0">All users presentations</option>
              {users.sort((a, b) => a.name.localeCompare(b.name)).map(user => (
                <option key={user.id} value={user.id}>
                  {user.name}
                </option>
              ))}
            </select> */}
            {/* <input
              className="input-field"
              type="text"
              placeholder="Adviser Name"
              value={newPresentation.adviserName}
              onChange={e => setNewPresentation({ ...newPresentation, adviserName: e.target.value })}
            />
            <input
              className="input-field"
              type="text"
              placeholder="Examiner Name"
              value={newPresentation.arguerName}
              onChange={e => setNewPresentation({ ...newPresentation, arguerName: e.target.value })}
            /> */}
            <button className="submit-presentation-button" onClick={handleSubmitPresentation}>Submit</button>
          </div>
        </div>
      )}
      {addPresentationErrorMessage && <div className="manager-error-message"><FaExclamationCircle className="error-icon" /> {addPresentationErrorMessage}</div>}
      <select id="userSelect" className="input-field" value={selectedUserId || 0} onChange={handleUserChange}>
        <option value="0">All users presentations</option>
        {users.sort((a, b) => a.name.localeCompare(b.name)).map(user => (
          <option key={user.id} value={user.id}>
            {user.name}
          </option>
        ))}
      </select>
      {presentations.length === 0 ? (
        <div>There are no presentations.</div>
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
                  <th>Action</th>
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
                  <td>{presentation.optionalParticipant1Name ?? ''}</td>
                  <td>
                    <select 
                      className="drop-down" 
                      value={presentation.optionalParticipant2Name !== null ? presentation.optionalParticipant2Name: ""}
                      onChange={(e) => handleOptionalChange(e.target.value, presentation.id)}>
                        <option value= "">No extra participant</option>
                        {users.sort((a, b) => a.name.localeCompare(b.name)).map(user => (
                          <option key={user.id} value={user.name}>
                            {user.name}
                          </option>
                        ))}
                    </select>
                  </td>
                  <td>
                  {((!isComputing) && (
                      <button onClick={() => setDeleteConfirmation({ show: true, presentationId: presentation.id })}>
                        <FaTimes />
                      </button> 
                  ))}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {deleteConfirmation.show && (
        <div className="confirm-delete-overlay">
          <div className="confirm-delete-modal" ref={modalRef}>
            <p>Are you sure you want to delete this presentation?</p>
            <div className="confirm-delete-buttons">
              <button className="confirm-delete-button" onClick={() => deletePresentation(deleteConfirmation.presentationId ?? -1)}>
                <FaCheck /> Yes
              </button>
              <button className="confirm-delete-button" onClick={() => {
                setDeleteConfirmation({ show: false });
                setDeleteConfirmationErrorMessage('');
              }}>
                <FaTimes /> No
              </button>
            </div>
            {deleteConfirmationErrorMessage && <div className="manager-error-message"><FaExclamationCircle className="error-icon"/> {deleteConfirmationErrorMessage}</div>}
          </div>
        </div>
      )}
    </div>
  );
};

export default Page;
