"use client";

import React from 'react';
import NavLinks from '@/app/ui/navigation/nav-links';
import { PowerIcon } from '@heroicons/react/24/outline';

const SideNav: React.FC = () => {
  const handleSignOut = () => {
    // Remove token and id from sessionStorage
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('id');

    // Redirect to the desired location
    window.location.href = 'http://localhost:3000';
  };

  return (
    <div className="flex h-full flex-col px-3 py-4 md:px-2">
      <div className="overflow-y-auto flex grow flex-row justify-between space-x-2 md:flex-col md:space-x-0 md:space-y-2">
        <NavLinks />
        <div className="hidden h-auto w-full grow rounded-md bg-gray-50 md:block"></div>
        <button
          onClick={handleSignOut}
          className="flex h-[48px] w-full grow items-center justify-center gap-2 rounded-md bg-gray-50 p-3 text-sm font-medium hover:bg-sky-100 hover:text-blue-600 md:flex-none md:justify-start md:p-2 md:px-3"
        >
          <PowerIcon className="w-6" />
          <div className="hidden md:block">Sign Out</div>
        </button>
      </div>
    </div>
  );
};

export default SideNav;
