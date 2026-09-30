"use client";

import ComputationWarning from '@/app/components/computationWarning/component';
import React, { useState, useEffect, useContext } from 'react';
import axios, { AxiosError } from 'axios';
import { FaCheckCircle, FaExclamationCircle, FaInfoCircle } from 'react-icons/fa';

import { StopChangeContext } from '@/app/context/stopChangeContext/context';

import './Page.css';

interface ErrorResponse {
  message: string;
}

const COMPUTATION_IN_PROGRESS = "There is a computation in progress."

const Page: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [isComputing, setIsComputing] = useState(false);
  
  // const { stopChange, toggleStopChange, changeStopChange } = useContext(StopChangeContext);
  
  //TODO Test this
  const changeStateOfComputation = async (state: Boolean) => {
    try {
      console.log("State:")
      console.log(state)
      const inProgress: String = state.toString()
      const token = sessionStorage.getItem('token');
      //TODO New HTTP request to check computationstate
      axios.post(`http://localhost:8080/api/python/changeState`, {
        inProgress
      },{
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
    } catch(error) {
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError<ErrorResponse>;
        // if (axiosError.response?.status === 401) {
        //     // Unauthorized, remove token and id, and redirect to login page
        //     sessionStorage.removeItem('token');
        //     sessionStorage.removeItem('id');
        //     if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
        //       sessionStorage.setItem('sessionExpiredAlertShown', 'true');
        //       alert("Your session has expired. Please log in again if you want to continue.");
        //     }
        //     window.location.href = 'http://localhost:3000';
        // }  else
        if(axiosError.response?.status == 422){
          setError(axiosError.response.data.message);
        } else {
          setError('There was an error on the server side, please try again later.');
        }
      } else {
        setError('There was an error on the server side, please try again later.');
      }
      // setSuccess(false);
    }
    // finally {
    //   // setIsLoading(false);
    // }
  };


  //TODO Get state of assignment computation from H2 Database
  //TODO Change if success or error states change (might require user to stay in the page? How about different users?)
  useEffect(() => {
    const checkComputation = async () => {
      try{
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
          setIsLoading(true);
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
          }  else if(axiosError.response?.status == 422){
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
  // }, [success, error]);

  //TODO Send message to user when computation starts
  const handleComputeButtonClick = async () => {
    //TODO Send response to user
    setIsLoading(true);
    changeStateOfComputation(true);
    const token = sessionStorage.getItem('token');
    axios.post(`http://localhost:8080/api/python/execute`, {}, {
    // axios.post(`http://localhost:8080/api/python/execute`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    .then(() => {
      setSuccess(true);
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
      } else if (error.response?.status === 409) {
          // const errorMessage = axiosError.response?.data.message
          const errorMessage = error.response?.data
          console.log("errorMessage: ", errorMessage)
          if (errorMessage == COMPUTATION_IN_PROGRESS) {
            setIsComputing(true);
            setError(COMPUTATION_IN_PROGRESS);
          } else {
            setError('There was an error on the server side, please try again later.');
          }
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
      setIsLoading(false);
    });
    // setSuccess(true);
    // setError(null);
  };

  //TODO Use warning box style
  return (
    <div className="content-container">
        <div className="compute-info">
          <h2 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Schedule Computation</h2>
          {isComputing && (<ComputationWarning />)}
          <p>
            Before starting the schedule computation, please ensure that the following tasks are completed:
          </p>
          <ol>
            <li>All users have added their unavailabilities.</li>
            <li>All presentation information has been added.</li>
            <li>All rooms have been created.</li>
            <li>All time slots have been created.</li>
            <li>All unavailabilities related to presentations have been identified.</li>
            <li>All presentations that need to be scheduled one after the other have been identified.</li>
            <li>All additional restrictions for the users have been added.</li>
            <li>All additional restrictions for the presentations have been added.</li>
            <li>All optional restrictions that need to be ignored have been identified.</li>
          </ol>
        </div>
      
      <button className="compute-button" onClick={handleComputeButtonClick} disabled={isLoading}>
        {isLoading ? 'Computing...' : 'Compute'}
      </button>
      {success && (
        <div className="success-message">
          <FaCheckCircle className="icon" />
          The schedule creation has started. This operation should take from 10 to 20 minutes.
        </div>
      )}
      {error && (
        <div className="error-message">
          <FaExclamationCircle className="icon" />
          {error}
        </div>
      )}
    </div>
  );
};

export default Page;
