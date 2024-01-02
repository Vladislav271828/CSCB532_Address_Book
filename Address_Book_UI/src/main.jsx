import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './Context/AuthProvider';
import { ContactsProvider } from './Context/ContactsProvider.jsx';
import { UserProvider } from './Context/UserProvider.jsx';
import { LabelProvider } from './Context/LabelProvider.jsx';

ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <AuthProvider><LabelProvider><ContactsProvider><UserProvider>
      <Routes>
        <Route path="*" element={<App />} />
      </Routes>
    </UserProvider></ContactsProvider></LabelProvider></AuthProvider>
  </BrowserRouter>
)
