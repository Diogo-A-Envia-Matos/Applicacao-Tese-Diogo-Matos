'use client';

import React, { useEffect, useState } from 'react';
import {
  UserIcon,
  HomeIcon,
  PresentationChartBarIcon,
  ArchiveBoxXMarkIcon, //or XMarkIcon
  HandRaisedIcon,
  CpuChipIcon,
  BarsArrowUpIcon,// orArrowUpTrayIcon,
  AcademicCapIcon,
  CalendarDaysIcon,
  RectangleGroupIcon,
  UserGroupIcon,
  ClipboardDocumentListIcon,
  ListBulletIcon,
} from '@heroicons/react/24/outline';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import clsx from 'clsx';
import axios from 'axios';

const userlinks = [
  { name: 'Home', href: '/dashboard', icon: HomeIcon },
  { name: 'User', href: '/dashboard/userPage', icon: UserIcon},
  { name: 'My Presentations', href: '/dashboard/myPresentationsPage', icon: PresentationChartBarIcon },
  { name: 'My Unavailabilities', href: '/dashboard/myUnavailabilitiesPage', icon: ArchiveBoxXMarkIcon },
  { name: 'My Presentations Unavailabilities', href: '/dashboard/myPresentationsUnavailabilitiesPage', icon: ArchiveBoxXMarkIcon },
  { name: 'Group My Presentations', href: '/dashboard/groupPresentationsPage', icon: RectangleGroupIcon },
];

const organizerlinks = [
  ...userlinks,
  { name: 'Rooms Manager', href: '/dashboard/roomsPage', icon: AcademicCapIcon },
  { name: 'Slots Manager', href: '/dashboard/slotsPage', icon: CalendarDaysIcon },
  { name: 'Presentations Manager', href: '/dashboard/presentationsManagerPage', icon: BarsArrowUpIcon },
  { name: 'Unavailabilities Manager', href: '/dashboard/unavailabilitiesManagerPage', icon: ClipboardDocumentListIcon },
  { name: 'Presentation Unavailabilities Manager', href: '/dashboard/presentationUnavailabilitiesManagerPage', icon: ClipboardDocumentListIcon },
  { name: 'Rooms Unavailabilities Manager', href: '/dashboard/roomUnavailabilitiesManagerPage', icon: ArchiveBoxXMarkIcon },
  { name: 'Users Manager', href: '/dashboard/usersManagerPage', icon: UserGroupIcon },
  { name: 'Stop Changes Page', href: '/dashboard/stopChangesPage', icon: HandRaisedIcon },
  { name: 'Compute Assignments', href: '/dashboard/computeAssignmentsPage', icon: CpuChipIcon },
];

const adminlinks = [
  ...organizerlinks,
  // Add additional admin-specific links here
];

const finalUserLinks = [
  { name: 'Current Assignment', href: '/dashboard/currentAssignmentPage', icon: ListBulletIcon },
]

const finalOrganizerLinks = [
  ...finalUserLinks,
  { name: 'Additional Restrictions', href: '/dashboard/additionalRestrictionsPage', icon: ArchiveBoxXMarkIcon}
  // Add additional organizer-specific links here
]

const finalAdminLinks = [
  ...finalOrganizerLinks,
  // Add additional admin-specific links here
]

export default function NavLinks() {
  const [token, setToken] = useState<string | null>(null);
  const [userRole, setUserRole] = useState(null);
  const [links, setLinks] = useState(userlinks)
  const pathname = usePathname();

  useEffect(() => {
    const tokenFromStorage = sessionStorage.getItem('token');
    const userIDFromStorage = sessionStorage.getItem('id');
    if (tokenFromStorage) {
      setToken(tokenFromStorage);

      axios.get(`http://localhost:8080/api/user/${userIDFromStorage}`, {
        headers: {
          Authorization: `Bearer ${tokenFromStorage}`
        }
      })
      .then(response => {
        // Extract role from response and update state
        const role = response.data.role;
        setUserRole(role);

        // Update links based on role
        switch (role) {
          case 'ORGANIZER':
            setLinks([...organizerlinks, ...finalOrganizerLinks]);
            break;
          case 'ADMIN':
            setLinks([...adminlinks, ...finalAdminLinks]);
            break;
          default:
            setLinks([...userlinks, ...finalUserLinks]);
            break;
        }
      })
      .catch(error => {
        console.error('Error fetching user data:', error);
      });
    }
  }, []);

  return (
    <>
      {links.map((link) => {
        const LinkIcon = link.icon;
        return (
          <Link
            key={link.name}
            href={link.href}
            className={clsx(
              'flex h-[48px] grow items-center justify-center gap-2 rounded-md bg-gray-50 p-3 text-sm font-medium hover:bg-sky-100 hover:text-blue-600 md:flex-none md:justify-start md:p-2 md:px-3',
              {
                'bg-sky-100 text-blue-600': pathname === link.href,
              },
            )}
          >
            <LinkIcon className="w-6" />
            <p className="hidden md:block">{link.name}</p>
          </Link>
        );
      })}
    </>
  );
}
