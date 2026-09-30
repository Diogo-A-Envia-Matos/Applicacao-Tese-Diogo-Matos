"use client"

import SideNav from '@/app/ui/navigation/sidenav';
// import { StopChangeProvider, useStopChange } from '@/app/context/stopChangeContext/context';
import React, { useContext, useEffect } from 'react';

// import { useStopChange } from '@/app/context/stopChangeContext/context';
import { StopChangeContext } from '@/app/context/stopChangeContext/context';
import StoppedChangesWarning from '@/app/components/stoppedChangesWarning/component';

// import React, { useState, useEffect, useRef } from 'react';
export default function Layout({ children }: { children: React.ReactNode }) {
  // const { stopChange, toggleStopChange } = useContext(StopChangeContext);
  const { stopChange, toggleStopChange, changeStopChange } = useContext(StopChangeContext);

  useEffect(() => {
    const boolString = sessionStorage.getItem('stoppedChanges')
    const bool = boolString === "true" ? true : false
    changeStopChange(bool)
  }, [])

  return (
    <div className="flex h-screen flex-col md:flex-row md:overflow-hidden">
      <div className="w-full flex-none md:w-64">
        <SideNav/>
      </div>
      {/* <div className="flex-grow p-6 md:overflow-y-auto md:p-12">{children}</div> */}

      <div className="flex-grow p-6 md:overflow-y-auto md:p-12">
        {(stopChange) && (
          <StoppedChangesWarning />
        )}
        {children}
      </div>
    </div>
  );
}