"use client";

import ComputationWarning from '@/app/components/computationWarning/component';
import React, { useState, useEffect, useContext } from 'react';
import axios, { AxiosError } from 'axios';
import { FaCheckCircle, FaExclamationCircle, FaInfoCircle } from 'react-icons/fa';

import { Button, Box, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';

import './Page.css';

import { StopChangeContext } from '@/app/context/stopChangeContext/context';

interface ErrorResponse {
  message: string;
}

const COMPUTATION_IN_PROGRESS = "There is a computation in progress."

const Page: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [stopChangesDialogOpen, setStopChangesDialogOpen] = useState(false);
  const [allowChangesDialogOpen, setAllowChangesDialogOpen] = useState(false);

  // const [isComputing, setIsComputing] = useState(false);

  // const [isStopped, setIsStopped] = useState(false);
  
  const { stopChange, toggleStopChange, changeStopChange } = useContext(StopChangeContext);

  // useEffect(() => {
  //   const boolString = sessionStorage.getItem('stoppedChanges')
  //   const bool = boolString === "true" ? true : false
  //   setIsStopped(bool)
  // }, [])
  
  //TODO Send message to user when computation starts
  const handleStopChangesButtonClick = () => {
    setStopChangesDialogOpen(true)
  };

  const handleStopChangesClose = () => {
    setStopChangesDialogOpen(false)
  }

  //TODO Send message to user when computation starts
  const handleStopChangesSubmit = async () => {
    //TODO Send response to user
    setSuccess(false);
    setIsLoading(true);
    // changeStateOfComputation(true);
    const token = sessionStorage.getItem('token');
    await axios.post(`http://localhost:8080/api/stopChanges`, {}, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    .then(() => {
      setSuccess(true);
      // setIsStopped(true);
      sessionStorage.setItem('stoppedChanges', true.toString());
      changeStopChange(true)
      // changeStateOfComputation(false);
      setError(null);
    })
    .catch((error) => {
      // changeStateOfComputation(false);
      if (error.response?.status === 401) {
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('id');
        if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
          sessionStorage.setItem('sessionExpiredAlertShown', 'true');
          alert("Your session has expired. Please log in again if you want to continue.");
        }
        window.location.href = 'http://localhost:3000';
      } else if(error.response?.status == 422) {
        setError(error.response.data.message);
      } else {
        setError('There was an error on the server side, please try again later.');
        console.log(error.response)
      }
      setSuccess(false);
    })
    .finally(() => {
      // changeStateOfComputation(false);
      setStopChangesDialogOpen(false)
      setIsLoading(false);
    });
    // setSuccess(true);
    // setError(null);
  };

  //TODO Send message to user when computation starts
  const handleAllowChangesButtonClick = async () => {
    setAllowChangesDialogOpen(true)
  };

  //TODO Send message to user when computation starts
  const handleAllowChangesClose = async () => {
    setAllowChangesDialogOpen(false)
  };

  //TODO Send message to user when computation starts
  const handleAllowChangesSubmit = async () => {
    //TODO Send response to user
    setSuccess(false);
    setIsLoading(true);
    // changeStateOfComputation(true);
    const token = sessionStorage.getItem('token');
    await axios.delete(`http://localhost:8080/api/stopChanges`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    .then(() => {
      setSuccess(true);
      // setIsStopped(false);
      sessionStorage.setItem('stoppedChanges', false.toString());
      changeStopChange(false);
      // changeStateOfComputation(false);
      setError(null);
    })
    .catch((error) => {
      // changeStateOfComputation(false);
      if (error.response?.status === 401) {
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('id');
        if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
          sessionStorage.setItem('sessionExpiredAlertShown', 'true');
          alert("Your session has expired. Please log in again if you want to continue.");
        }
        window.location.href = 'http://localhost:3000';
      } else if(error.response?.status == 422) {
        setError(error.response.data.message);
      } else {
        setError('There was an error on the server side, please try again later.');
        console.log(error.response)
      }
      setSuccess(false);
    })
    .finally(() => {
      // changeStateOfComputation(false);
      setAllowChangesDialogOpen(false)
      setIsLoading(false);
    });
    // setSuccess(true);
    // setError(null);
  };

  //TODO Use warning box style
  return (
    <>
      <div className="content-container">
        <div className="stop-changes-info">
          <h2 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Stop Changes</h2>
        </div>
        
        <div className="stop-changes-description">
          <p>
            This page allows you to prevent any more changes to be made on the base restrictions or add or remove any rooms or slots.
            When changes are stopped, ORGANIZERS/ADMINS are still allowed to change additional restrictions and choose to ignore optional restrictions.
          </p>
        </div>

        {(!stopChange) && (
          <button className="stop-changes-button" onClick={handleStopChangesButtonClick} disabled={isLoading}>
            {isLoading ? 'Stopping...' : 'Stop changes'}
          </button>
        )}
        {(stopChange) && (
          <button className="allow-changes-button" onClick={handleAllowChangesButtonClick} disabled={isLoading}>
            {isLoading ? 'Allowing...' : 'Allow changes'}
          </button>
        )}
        {(success && stopChange) && (
          <div className="success-message">
            <FaCheckCircle className="icon" />
            The changes have been stopped.
          </div>
        )}
        {(success && !stopChange) && (
          <div className="success-message">
            <FaCheckCircle className="icon" />
            The changes have been allowed.
          </div>
        )}
        {error && (
          <div className="error-message">
            <FaExclamationCircle className="icon" />
            {error}
          </div>
        )}
      </div>

      {/* Stop changes confirmation */}
      <Dialog
        open={stopChangesDialogOpen}
        onClose={handleStopChangesClose}
        maxWidth="xl"
        fullWidth={true}
      >
        <DialogTitle>Stop changes confirmation</DialogTitle>
        <DialogContent>
          <div className="stop-changes-dialog" style={{ overflowX: 'auto' }}>
            Are you sure that you want to stop all changes on the base restrictions?
          </div>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleStopChangesClose} color="secondary">
            No
          </Button>
          <Button onClick={handleStopChangesSubmit} color="primary">
            Yes
          </Button>
        </DialogActions>
      </Dialog>

      {/* Alow changes confirmation */}
      <Dialog
        open={allowChangesDialogOpen}
        onClose={handleAllowChangesClose}
        maxWidth="xl"
        fullWidth={true}
      >
        <DialogTitle>Allow changes confirmation</DialogTitle>
        <DialogContent>
          <div className="allow-changes-dialog" style={{ overflowX: 'auto' }}>
            Are you sure that you want to allow changes to be made on the base restrictions?
          </div>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleAllowChangesClose} color="secondary">
            No
          </Button>
          <Button onClick={handleAllowChangesSubmit} color="primary">
            Yes
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default Page;
