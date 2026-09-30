"use client";

import { Inter } from "next/font/google";
import "./globals.css";

import React, { useState } from 'react';

import { GoogleOAuthProvider } from '@react-oauth/google';

import { StopChangeContext } from '@/app/context/stopChangeContext/context';


const inter = Inter({ subsets: ["latin"] });

const CLIENT_ID = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID ?? "Missing NEXT_PUBLIC_GOOGLE_CLIENT_ID from .env"

// console.log(CLIENT_ID)

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const [stopChange, setStopChange] = useState(false);

  // Toggle stop state between 'true' and 'false'
  const toggleStopChange = () => {
    const nextState = stopChange ? false : true
    setStopChange(nextState);
  };

  // Change state between 'true' and 'false'
  const changeStopChange = (nextState : boolean) => {
    setStopChange(nextState);
  };

  return (
    <html lang="en">
      <body className={`${inter.className} antialiased`}>
        <GoogleOAuthProvider clientId={CLIENT_ID}>
          <StopChangeContext.Provider value={{ stopChange, toggleStopChange, changeStopChange }}>
            {children}
          </StopChangeContext.Provider>
        </GoogleOAuthProvider>
      </body>
    </html>
  );
}
