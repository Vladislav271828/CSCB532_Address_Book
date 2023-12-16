import { createContext, useState, useContext } from "react";
import axios from "../API/axios";
import { useNavigate } from "react-router-dom";
import AuthContext from "./AuthProvider";

const CREATE_CONTACT_URL = '/contact/create-contact'
const FETCH_CONTACTS_URL = '/contact/get-all-contacts'
const ContactsContext = createContext({});

export const ContactsProvider = ({ children }) => {
    const [contacts, setContacts] = useState([]);
    const [fetchError, setFetchErr] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();
    const { auth } = useContext(AuthContext);

    const fetchContacts = async () => {
        try {
            const response = await axios.get(FETCH_CONTACTS_URL, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
            setIsLoading(true);
            setContacts(response.data);
            setFetchErr(null);
            setIsLoading(false);
        } catch (err) {
            if (!err?.response.data?.message) {
                setFetchErr('Unable to connect to server.');
            }
            else if (err.response.status == 401) {
                alert("Token expired, please login again.");
                location.reload();
            }
            else {
                setFetchErr(err.response.data.message);
            }
            setIsLoading(false);
        }
    }

    const createContact = async () => {
        try {
            const response = await axios.post(CREATE_CONTACT_URL, {
                name: "New",
                lastName: "Contact",
                phoneNumber: "0000000000"
            }, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
            setContacts([...contacts, response.data]);
            setFetchErr(null);
            navigate("contact/" + response.data.id + "/edit")
        } catch (err) {
            if (!err?.response.data?.message) {
                setFetchErr('Unable to connect to server.');
            }
            else {
                setFetchErr(err.response.data.message);
            }
        }
    }


    return (
        <ContactsContext.Provider value={{ contacts, fetchError, isLoading, fetchContacts, createContact, setContacts }}>
            {children}
        </ContactsContext.Provider>
    )
}

export default ContactsContext;