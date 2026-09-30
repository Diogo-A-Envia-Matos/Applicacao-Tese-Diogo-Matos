"use client";

import ComputationWarning from '@/app/components/computationWarning/component';
import React, { useState, useEffect, useRef, useContext } from 'react';
import axios, { AxiosError } from 'axios';
import { FaTimes, FaCheck, FaExclamationCircle } from 'react-icons/fa'; // Import icons

// import { StopChangeContext } from '@/app/context/stopChangeContext/context';

import './Page.css';

interface UserDTO {
  id: number;
  name: string;
  email: string;
  role: String;
}

interface ErrorResponse {
  message: string;
}

export default function Page() {
  const [users, setUsers] = useState<UserDTO[]>([]);
  const [deleteConfirmation, setDeleteConfirmation] = useState<{
    show: boolean;
    userId?: number;
  }>({ show: false });
  const [deleteConfirmationErrorMessage, setDeleteConfirmationErrorMessage] = useState<string>('');
  const [showAddUserForm, setShowAddUserForm] = useState<boolean>(false);
  const [newUserName, setNewUserName] = useState<string>('');
  const [newUserEmail, setNewUserEmail] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [loggedInUserRole, setLoggedInUserRole] = useState<String>(''); 
  const [isComputing, setIsComputing] = useState(false);
  
  // const { stopChange, toggleStopChange, changeStopChange } = useContext(StopChangeContext);

  const modalRef = useRef<HTMLDivElement>(null);

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

  useEffect(() => {
    async function fetchData() {
      try {
        const token = sessionStorage.getItem('token');
        const response = await axios.get<UserDTO[]>('http://localhost:8080/api/user', {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        setUsers(response.data);
        const loggedInUser = response.data.find(user => user.id === parseInt(sessionStorage.getItem('id') || '0', 10));
        if (loggedInUser) {
          setLoggedInUserRole(loggedInUser.role);
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
          } else {
            setErrorMessage('There was an error on the server side, please try again later.');
          }
        } else {
          setErrorMessage('There was an error on the server side, please try again later.');
        }
      }
    }
    fetchData();
  }, []); // Empty dependency array to run the effect only once on component mount

  const deleteUser = async (userId: number) => {
    try {
      const token = sessionStorage.getItem('token');
      // Perform deletion
      await axios.delete(`http://localhost:8080/api/user/${userId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      // After successful deletion, remove the user from the state
      setUsers(prevUsers => prevUsers.filter(user => user.id !== userId));

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

  const toggleAddUserForm = () => {
    setShowAddUserForm(prev => !prev);
    // Reset form fields and error message when toggling form visibility
    if (!showAddUserForm) {
      setNewUserName('');
      setNewUserEmail('');
      setErrorMessage('');
    }
  };

  const handleSubmitNewUser = async () => {
    if (!newUserName.trim() || !validateEmail(newUserEmail)) {
      setErrorMessage('Please enter valid name and email.');
      return;
    }

    try {
      const token = sessionStorage.getItem('token');
      // Perform submission
      const newUser = { name: newUserName, email: newUserEmail };
      const response = await axios.post('http://localhost:8080/api/user', newUser, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      // Assuming successful submission, update the state with the new user
      setUsers(prevUsers => [...prevUsers, response.data]);

      // Close the add user form
      setShowAddUserForm(false);

      // Clear form fields and error message
      setNewUserName('');
      setNewUserEmail('');
      setErrorMessage('');
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

  const validateEmail = (email: string) => {
    // Basic email validation
    const re = /\S+@\S+\.\S+/;
    return re.test(email);
  };

  const handleRoleChange = async (userId: number, newRole: string) => {
    try {
      const token = sessionStorage.getItem('token');
      const response = await axios.put('http://localhost:8080/api/user', {
        userID: userId,
        newRole: newRole
      }, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      // Assuming the server responds with updated user data
      const updatedUser = response.data;
      setUsers(prevUsers => prevUsers.map(user => user.id === updatedUser.id ? updatedUser : user));
    } catch (error) {
      console.error('Error updating user role:', error);
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
  }

  return (
    <div className="container">
      <h2 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Users Manager</h2>
      {(isComputing && loggedInUserRole === 'ADMIN') && (<ComputationWarning />)}
      <div className="users-manager-info">
        <p>
          The table below lists every existing user, in alphabetical order.
        </p>
        <p>
          For every user, their name, email and roles are shown.
        </p>
        {(loggedInUserRole === 'ADMIN') && (
            <p>
              The dropdown menu on the "Role" column allows you to change to role of any user that does not have the ADMIN role.
              The last column allows you to delete any user that does not have the ADMIN role.
            </p>
            // <p>
            //   The last column allows you to delete any user that does not have the ADMIN role.
            // </p>
          )}
      </div>
      {(loggedInUserRole === 'ADMIN') && (
        // <button className="add-user-button" onClick={toggleAddUserForm} disabled={isComputing || stopChange}>
        <button className="add-user-button" onClick={toggleAddUserForm} disabled={isComputing}>
          {showAddUserForm ? 'Cancel' : 'Add User'}
        </button>
      )}
      {showAddUserForm && (
        <div>
          {errorMessage && (<div className="error-message"><FaExclamationCircle className="error-icon" />{errorMessage}</div>)}
          <div className="add-user-form">
            <div className="input-field">
              <input
                type="text"
                value={newUserName}
                onChange={e => setNewUserName(e.target.value)}
                placeholder="Enter Name"
              />
            </div>
            <div className="input-field">
              <input
                type="email"
                value={newUserEmail}
                onChange={e => setNewUserEmail(e.target.value)}
                placeholder="Enter Email"
              />
            </div>
            <button className="submit-user-button" onClick={handleSubmitNewUser}>
              Submit
            </button>
          </div>
        </div>
      )}
      <div className="table-container" style={{ overflowX: 'auto' }}>
        <table className="user-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {users.sort((a, b) => a.name.localeCompare(b.name)).map(user => (
              <tr key={user.id}>
                <td>{user.name}</td>
                <td>{user.email}</td>
                <td>
                  {!isComputing && loggedInUserRole === 'ADMIN' && user.role !== 'ADMIN' && user.id !== parseInt(sessionStorage.getItem('id') || '0', 10) ? (
                    <select value={String(user.role)} onChange={(e) => handleRoleChange(user.id, e.target.value)}>
                      <option value="USER">USER</option>
                      <option value="ORGANIZER">ORGANIZER</option>
                      <option value="ADMIN">ADMIN</option>
                    </select>
                  ) : (
                    user.role
                  )}
                </td>
                <td>
                  {(!isComputing && loggedInUserRole === 'ADMIN' && user.role !== 'ADMIN') && (  
                      <button onClick={() => setDeleteConfirmation({ show: true, userId: user.id })}>
                        <FaTimes />
                      </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {deleteConfirmation.show && (
        <div className="confirm-delete-overlay">
          <div className="confirm-delete-modal" ref={modalRef}>
            <p>Are you sure you want to delete this user?</p>
            <div className="confirm-delete-buttons">
              <button className="confirm-delete-button" onClick={() => deleteUser(deleteConfirmation.userId ?? -1)}>
                <FaCheck /> Yes
              </button>
              <button className="confirm-delete-button" onClick={() => {
                setDeleteConfirmation({ show: false });
                setDeleteConfirmationErrorMessage('');
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
}
