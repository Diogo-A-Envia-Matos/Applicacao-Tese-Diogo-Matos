"use client"

import React, { useState, createContext, useContext } from 'react';

export const StopChangeContext = createContext({
  stopChange: false,
  toggleStopChange: () => {},
  changeStopChange: (value: boolean) => {}
});

// const StopChangeContext = createContext();

// Create a provider component
// export const StopChangeProvider = ({ children }: { children: React.ReactNode }) => {
//   const [stopChange, setStopChange] = useState(false);

//   // Toggle stop state between 'true' and 'false'
//   const toggleStopChange = () => {
//     const nextState = stopChange ? false : true
//     setStopChange(nextState);
//   };

//   // Change state between 'true' and 'false'
//   const changeStopChange = (nextState : boolean) => {
//     setStopChange(nextState);
//   };

//   // return (
//   //   <StopChangeContext.Provider value={{ stopChange, toggleStopChange, changeStopChange }}>
//   //     {children}
//   //   </StopChangeContext.Provider>
//   // );
//   return (
//     <StopChangeContext.Provider value={{ stopChange, toggleStopChange, changeStopChange }}>
//       {children}
//     </StopChangeContext.Provider>
//   );
// };

// Custom hook to use the context
// export const useStopChange = () => useContext(StopChangeContext);
