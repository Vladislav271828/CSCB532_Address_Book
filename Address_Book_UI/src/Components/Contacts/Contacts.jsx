import './Contacts.css'
import { useState, useEffect, useContext } from "react";
import SearchBar from "../SearchBar";
import axios from "../../API/axios";
import AuthContext from "../../Context/AuthProvider";
import ContactsList from "./ContactsList";

const FETCH_USER_URL = '/user-profile/get-profile'
const FETCH_CONTACTS_URL = '/contact/get-all-contacts'

function Contacts() {
    const [search, setSearch] = useState('');
    const [isLoading, setIsLoading] = useState(true);
    const [fetchError, setFetchErr] = useState(null);
    const [contacts, setContacts] = useState([]);
    const [userNames, setUserNames] = useState("");

    const { auth } = useContext(AuthContext);

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
                    console.log(err.response.data.message);
                }
            }
        }

        const fetchPosts = async () => {
            try {
                const response = await axios.get(FETCH_CONTACTS_URL, {
                    headers: { "Authorization": `Bearer ${auth}` }
                });
                setContacts(response.data);
                setFetchErr(null);
                setIsLoading(false);
            } catch (err) {
                if (!err?.response) {
                    setFetchErr('Unable to connect to server.');
                }
                else {
                    setFetchErr(err.response.data.message);
                }
            }
        }

        fetchUser();
        fetchPosts();
    }, [])

    return (
        <div className="contacts-container">
            <h2 className='contacts-greeting'>Hello {userNames}</h2>
            <div className='contacts-search-container'>
                <SearchBar
                    search={search}
                    setSearch={setSearch}
                    placeholder="Search Contacts"
                />
                <button className='add-button'>+</button>
            </div>
            <hr />
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
        </div>
    )
}

export default Contacts