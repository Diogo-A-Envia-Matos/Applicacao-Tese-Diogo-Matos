"use client";

import React, { useState, useEffect, useRef } from 'react';
import axios, { AxiosError } from 'axios';
import { FaCheckCircle, FaExclamationCircle, FaTimes, FaCheck } from 'react-icons/fa';
import './Page.css';

interface UserDTO {
  name: string;
  email: string;
  role: string;
}

const Page: React.FC = () => {
  const [userData, setUserData] = useState<UserDTO | null>(null);
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmNewPassword, setConfirmNewPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [deleteConfirmation, setDeleteConfirmation] = useState<{ show: boolean }>({ show: false });
  const [deleteConfirmationErrorMessage, setDeleteConfirmationErrorMessage] = useState('');
  const modalRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const token = sessionStorage.getItem('token');
        const userId = sessionStorage.getItem('id');
        const response = await axios.get<UserDTO>(`http://localhost:8080/api/user/${userId}`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        setUserData(response.data);
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

    fetchUserData();
  }, []);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (modalRef.current && !modalRef.current.contains(event.target as Node)) {
        setDeleteConfirmation({ show: false });
      }
    };

    document.addEventListener('mousedown', handleClickOutside);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleToggleForm = () => {
    setShowChangePassword(prevState => !prevState);
    // Reset form fields when hiding the form
    if (!showChangePassword) {
      setCurrentPassword('');
      setNewPassword('');
      setConfirmNewPassword('');
      setErrorMessage('');
      setSuccessMessage('');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');
    if (newPassword !== confirmNewPassword) {
      setErrorMessage('New password and confirm password do not match.');
      return;
    }
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/;
    if (!passwordRegex.test(newPassword)) {
      setErrorMessage("Your new password must contain: At least 8 characters, At least one uppercase letter, At least one lowercase letter, At least one number.");
      return;
    }
    try {
      const token = sessionStorage.getItem('token');
      const userId = sessionStorage.getItem('id');
      const response = await axios.post(
        `http://localhost:8080/api/user/changePassword`,
        {
          oldPassword: currentPassword,
          newPassword: newPassword
        },
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );
      setSuccessMessage('Password changed successfully.');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmNewPassword('');
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
        } else if(axiosError.response?.status === 409){
          setErrorMessage('Your current password is not correct.');
        } else {
          setErrorMessage('There was an error on the server side, please try again later.');
        }
      } else {
        // Handle non-Axios errors
        console.error('Error fetching presentations:', error);
        setErrorMessage('There was an error on the server side, please try again later.');
      }
    }
  };

  const handleInputChange = () => {
    setErrorMessage('');
    setSuccessMessage('');
  };

  const handleDeleteAccount = () => {
    setDeleteConfirmation({ show: true });
  };

  const confirmDeleteAccount = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const userId = sessionStorage.getItem('id');
      // await axios.delete(`http://localhost:8080/api/user/${userId}`, {
      //   headers: {
      //     Authorization: `Bearer ${token}`
      //   }
      // });
      await axios.delete(`http://localhost:8080/api/user/self/${userId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
          // data: 
          // {
          //   uid: userId
          // },
        }
      );
      sessionStorage.removeItem('token');
      sessionStorage.removeItem('id');

      // Redirect to the desired location
      window.location.href = 'http://localhost:3000';
    } catch (error) {
      setDeleteConfirmationErrorMessage('Error deleting account. Please try again.');
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

  return (
    <div className="page-container">
      {userData ? (
        <div className="user-info">
          <h2 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>User Information</h2>
          <p><strong>Name:</strong> {userData.name}</p>
          <p><strong>Email:</strong> {userData.email}</p>
          <p><strong>Role:</strong> {userData.role}</p>

          <button className="change-password-button" onClick={handleToggleForm}>
            {showChangePassword ? 'Cancel' : 'Change Password'}
          </button>

          {/* Delete Account Button */}
          {(userData.role === 'ADMIN') && (
            <button className="delete-account-button" onClick={handleDeleteAccount}>
              Delete Account
            </button>
          )}

          {showChangePassword && (
            <form onSubmit={handleSubmit} className="change-password-form">
              <div>
                <div>Current Password</div>
                <input
                  className="input-field"
                  type="password"
                  id="currentPassword"
                  value={currentPassword}
                  onChange={(e) => { setCurrentPassword(e.target.value); handleInputChange(); }}
                  required
                />
              </div>
              <div>
                <div>New Password</div>
                <input
                  className="input-field"
                  type="password"
                  id="newPassword"
                  value={newPassword}
                  onChange={(e) => { setNewPassword(e.target.value); handleInputChange(); }}
                  required
                />
              </div>
              <div>
                <div>Confirm New Password</div>
                <input
                  className="input-field"
                  type="password"
                  id="confirmNewPassword"
                  value={confirmNewPassword}
                  onChange={(e) => { setConfirmNewPassword(e.target.value); handleInputChange(); }}
                  required
                />
              </div>
              {errorMessage && <div className="error-message"><FaExclamationCircle className="error-icon"/>{errorMessage}</div>}
              {successMessage && <div className="success-message"><FaCheckCircle className="success-icon"/>{successMessage}</div>}
              <button className="submit-button" type="submit">Submit</button>
            </form>
          )}

          {deleteConfirmation.show && (
            <div className="confirm-delete-overlay">
              <div className="confirm-delete-modal" ref={modalRef}>
                <p>Are you sure you want to delete your account?</p>
                <div className="confirm-delete-buttons">
                  <button className="confirm-delete-button" onClick={confirmDeleteAccount}>
                    <FaCheck /> Yes
                  </button>
                  <button className="confirm-delete-button" onClick={() => setDeleteConfirmation({ show: false })}>
                    <FaTimes /> No
                  </button>
                </div>
                {deleteConfirmationErrorMessage && <div className="error-message"><FaExclamationCircle className="error-icon"/> {deleteConfirmationErrorMessage}</div>}
              </div>
            </div>
          )}
        </div>
      ) : (
        <p>Loading...</p>
      )}
    </div>
  );
}

export default Page;
