// import { useState } from 'react'
import './App.css'

import Login from './Components/LoginSignup/Login.jsx'
import SignUp from './Components/LoginSignup/SignUp.jsx'
import Root from './Components/Root.jsx'
import FourOhFour from './Components/404.jsx'


import { AppHeader, AppFooter } from './Components/HeaderFooter/HeaderFooter'
import { Route, Routes } from 'react-router-dom';

function App() {
  return (
    <div className="App">
      <AppHeader />
      <Routes>
        <Route exact path="/" element={<Root />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="*" element={<FourOhFour />} />
      </Routes>
      <AppFooter />
    </div>
  )
}

export default App;
