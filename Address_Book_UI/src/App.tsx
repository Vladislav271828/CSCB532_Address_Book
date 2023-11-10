// import { useState } from 'react'
import './App.css'
import LoginSignup from './Components/LoginSignup/LoginSignup'
import {AppHeader, AppFooter} from './Components/HeaderFooter/HeaderFooter'

function App() {

  return (
    <>
      <AppHeader/>
      <LoginSignup/>
      <AppFooter/>
    </>
  )
}

export default App
