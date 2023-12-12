import user from "./user100.png";
import './Contacts.css'

import { useState, useEffect, useContext } from "react";

import SearchBar from "../SearchBar";
import axios from "../../API/axios";
import ContactsList from "./ContactsList";

import AuthContext from "../../Context/AuthProvider";
import ContactsContext from '../../Context/ContactsProvider';
import { Link, useNavigate } from "react-router-dom";

const FETCH_USER_URL = '/user-profile/get-profile'
const FETCH_CONTACTS_URL = '/contact/get-all-contacts'
const CREATE_CONTACT_URL = '/contact/create-contact'

function Contacts() {
    const [search, setSearch] = useState('');
    const [isLoading, setIsLoading] = useState(true);
    const [fetchError, setFetchErr] = useState(null);
    const [userNames, setUserNames] = useState("");

    const { auth } = useContext(AuthContext);
    const { contacts, setContacts } = useContext(ContactsContext);

    const navigate = useNavigate();

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const response = await axios.get(FETCH_USER_URL, {
                    headers: { "Authorization": `Bearer ${auth}` }
                });
                setUserNames(response.data.firstName + ' ' + response.data.lastName);
                console.log(auth);
            } catch (err) {
                if (!err?.response) {
                    console.log('fetchUser: Unable to connect to server.');
                }
                else {
                    console.log("fetchUser: " + err.response.data.message);
                }
            }
        }



        fetchUser();
    }, [])

    useEffect(() => {
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
                if (!err?.response) {
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

        fetchContacts();
    }, [])

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
            if (!err?.response) {
                setFetchErr('Unable to connect to server.');
            }
            else {
                setFetchErr(err.response.data.message);
            }
        }
    }

    return (
        <div className="main-container">
            <h2 className='main-header-text' style={{ fontWeight: "400" }}>
                Hello <p
                    style={{ display: "inline", fontWeight: "800" }}>
                    {userNames}
                </p>
            </h2>
            <div className='contacts-search-container'>
                <SearchBar
                    search={search}
                    setSearch={setSearch}
                    placeholder="Search Contacts"
                />
                <Link to="/settings">
                    <button className='user-button'>
                        <img src={user}
                            alt="User Settings"
                            style={{ width: "1.5rem" }} />
                    </button>
                </Link>
            </div>
            <hr style={{ marginTop: "25px" }} />
            <div className='contact-list-container'>
                {isLoading && <p>Fetching contacts, please wait.</p>}
                {fetchError && <p style={{ color: "red" }}>{fetchError}</p>}
                {!fetchError && !isLoading &&
                    contacts.length ? (
                    <ContactsList
                        contacts={contacts.filter(item => (
                            (item.name + " " + item.lastName).toLowerCase()).includes(search.toLowerCase()
                            ))}
                    />
                ) : (
                    <p>Your address book is empty.</p>
                )}
            </div>
            <button
                className="big-btn new-contacts-btn"
                onClick={() => createContact()}>Create Contact
            </button>
        </div>
    )
}

export default Contacts