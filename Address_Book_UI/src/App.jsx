import './App.css'

import Contacts from './Components/Contacts/Contacts.jsx'
import Login from './Components/LoginSignup/Login.jsx'
import SignUp from './Components/LoginSignup/SignUp.jsx'
import FourOhFour from './Components/404.jsx'

import { AppHeader, AppFooter } from './Components/HeaderFooter/HeaderFooter'
import { Route, Routes } from 'react-router-dom';

function App() {
  return (
    <div className="App">
      <AppHeader />
      <Routes>
        <Route path="/" element={<Contacts />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="*" element={<FourOhFour />} />
      </Routes>
      <AppFooter />
    </div>
  )
}

export default App;
