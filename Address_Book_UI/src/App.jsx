import './App.css'

import Contacts from './Components/Contacts/Contacts.jsx'
import Login from './Components/LoginSignup/Login.jsx'
import SignUp from './Components/LoginSignup/SignUp.jsx'
import FourOhFour from './Components/404.jsx'
import ContactDetails from './Components/Contacts/ContactDetails.jsx'
import ContactEdit from './Components/Contacts/ContactEdit.jsx'
import UserSettings from './Components/UserSettings/UserSettings.jsx'

import { AppHeader, AppFooter } from './Components/HeaderFooter/HeaderFooter'
import { Route, Routes } from 'react-router-dom';
import RequireAuth from './Components/RequireAuth.jsx'


function App() {
  return (
    <div className="App">
      <AppHeader />
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />

        {/* protected routes */}
        <Route element={<RequireAuth />}>
          <Route path="/" element={<Contacts />} />
          <Route path="/contact/:id" element={<ContactDetails />} />
          <Route path="/contact/:id/edit" element={<ContactEdit />} />
          <Route path="/settings" element={<UserSettings />} />
        </Route>

        <Route path="*" element={<FourOhFour />} />
      </Routes>
      <AppFooter />
    </div>
  )
}

export default App;
