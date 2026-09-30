"use client";

import React, { useState, useEffect, useRef } from 'react';
import axios, { AxiosError } from 'axios';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

import './Page.css'; 

interface Presentation {
    id: number;
    studentNumber: string;
    studentName: string;
    thesisTitle: string;
    roomId: number | null;
    roomNumber: String | null;
    roomBuilding: String | null;
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
    email: string;
    role: String;
}

export default function Page() {
    const [presentations, setPresentations] = useState<Presentation[]>([]);
    const tableRef = useRef<HTMLTableElement>(null);
    const [loggedInUserRole, setLoggedInUserRole] = useState<String>('');

    useEffect(() => {
        const fetchPresentationsData = async () => {
            try{
                const token = sessionStorage.getItem('token');
                const response = await axios.get<Presentation[]>('http://localhost:8080/api/presentation', {
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

        const fetchUserRole = async () => {
            try {
                const token = sessionStorage.getItem('token');
                const userId = sessionStorage.getItem('id');

                const response = await axios.get<User>(`http://localhost:8080/api/user/${userId}`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });

                setLoggedInUserRole(response.data.role);
            } catch (error) {
                console.error('Error fetching user role:', error);
                if (axios.isAxiosError(error)) {
                    const axiosError = error as AxiosError;
                    if (axiosError.response?.status === 401) {
                        sessionStorage.removeItem('token');
                        sessionStorage.removeItem('id');
                        alert("Your session has expired. Please log in again.");
                        window.location.href = 'http://localhost:3000';
                    }
                }
            }
        };

        fetchPresentationsData();
        fetchUserRole();
    }, []);

    const downloadPDF = () => {
        if (tableRef.current) {
            const pdf = new jsPDF('l', 'mm', 'a4'); // Landscape orientation
            pdf.setFontSize(12);

            const tableData = [];
            const table = tableRef.current;

            // Get table headers
            const headers = [];
            for (let i = 0; i < table.rows[0].cells.length; i++) {
                if (i !== 10 && i !== 11) { // Skip "Extra Participant" columns
                    headers.push(table.rows[0].cells[i].innerText);
                }
            }

            // Add headers to tableData
            tableData.push(headers);

            // Get table rows data
            for (let i = 1; i < table.rows.length; i++) {
                const rowData = [];
                for (let j = 0; j < table.rows[i].cells.length; j++) {
                    if (j !== 10 && j !== 11) { // Skip "Extra Participant" columns
                        rowData.push(table.rows[i].cells[j].innerText);
                    }
                }
                tableData.push(rowData);
            }

            // Create PDF table
            autoTable(pdf, {
                head: [headers],
                body: tableData.slice(1), 
                startY: 20 // Position of the table relative to the top of the page
            });

            pdf.save('presentations.pdf');
        }
    };

    const downloadCSV = () => {
        if (tableRef.current) {
            const table = tableRef.current;
            let csvContent = '';
    
            // Get table headers, skipping "Extra Participant" columns (10th and 11th indices)
            const headers = [];
            for (let i = 0; i < table.rows[0].cells.length; i++) {
                if (i !== 10 && i !== 11) { // Skip "Extra Participant" columns
                    headers.push(table.rows[0].cells[i].innerText);
                }
            }
            csvContent += headers.join(',') + '\n';
    
            // Get table rows data, skipping "Extra Participant" columns (10th and 11th indices)
            for (let i = 1; i < table.rows.length; i++) {
                const rowData = [];
                for (let j = 0; j < table.rows[i].cells.length; j++) {
                    if (j !== 10 && j !== 11) { // Skip "Extra Participant" columns
                        rowData.push(`"${table.rows[i].cells[j].innerText}"`); // Wrap each cell data with quotes to handle commas within text
                    }
                }
                csvContent += rowData.join(',') + '\n';
            }
    
            // Create a blob and trigger the download
            const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.setAttribute('download', 'presentations.csv');
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    };
    

    const formatStartingHour = (startingHour: string | null): string => {
        if (startingHour === null) return 'Not assigned yet';
        
        // Parse the time string to get the hour and minute components
        const [hour, minute] = startingHour.split(':').map(Number);
    
        // Ensure hour and minute are padded with leading zeros if necessary
        const formattedHour = hour.toString().padStart(2, '0');
        const formattedMinute = minute.toString().padStart(2, '0');
    
        return `${formattedHour}:${formattedMinute}`;
    };
    

    return (
        <div className="page-container">
            <h2 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Home Page — Presentations</h2>
            <div className="description">
                <p>
                    The table below lists every existing presentation, ordered by their ids.
                </p>
                {(loggedInUserRole === 'ORGANIZER' || loggedInUserRole === 'ADMIN') && (
                    <p>
                        You can also download this table as a PDF or CSV file.
                    </p>
                )}
            </div>
            {presentations.length === 0 ? (
                <div>There are no presentations.</div>
            ) : (
            <>
                {(loggedInUserRole === 'ORGANIZER' || loggedInUserRole === 'ADMIN') && (
                    <>
                        <button className="download-button" onClick={downloadPDF}>Download PDF</button>
                        <button className="download-button" onClick={downloadCSV}>Download CSV</button>
                    </>
                )}
                <div className="presentation-table" style={{ overflowX: 'auto' }}>
                    <table ref={tableRef}>
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
                                    <td>{presentation.optionalParticipant1Name ?? ''}</td>
                                    <td>{presentation.optionalParticipant2Name ?? ''}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </>
        )}
        </div>
    );
}
