import gear from "../../Icons/gear.webp";
import './Contacts.css'

import { useState, useEffect, useContext } from "react";
import { Link } from "react-router-dom";

import SearchBar from "../SearchBar";
import ContactsList from "./ContactsList";

import ContactsContext from '../../Context/ContactsProvider';
import UserContext from "../../Context/UserProvider";


function Contacts() {
    const [search, setSearch] = useState('');
    const { contacts, fetchError, isLoading, fetchContacts, createContact } = useContext(ContactsContext);
    const { firstName, lastName, fetchUser, isLabelSorting, setIsLabelSorting } = useContext(UserContext);

    useEffect(() => {
        fetchUser();
        fetchContacts();
    }, [])

    const contactSearchQuery = contacts?.map(item => {
        return (item.name + " " + item.lastName + " " +
            item.labels.map((l) => { return l.name }).join(" ")).toLowerCase();
    });

    return (
        <div className="main-container">
            <h2 className='main-header-text' style={{ fontWeight: "400" }}>
                Hello <p
                    style={{ display: "inline", fontWeight: "800" }}>
                    {firstName + ' ' + lastName}
                </p>
            </h2>
            <div className='contacts-search-container'>
                <SearchBar
                    search={search}
                    setSearch={setSearch}
                    placeholder="Search Contacts"
                />
                <button className='small-button'
                    title={"Toggle Sort, Currently Sorting By: " + (isLabelSorting ? "Label" : "Name")}
                    style={{ marginRight: "10px" }}
                    onClick={() => setIsLabelSorting(!isLabelSorting)}>
                    {isLabelSorting ? "L" : "N"}
                </button>
                <Link to="/settings">
                    <button className='small-button'
                        title="Settings">
                        <img src={gear}
                            style={{ width: "1.2rem" }}
                            loading="eager"
                            alt="Settings" />
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
                        contacts={contacts.filter((item, index) => (
                            (contactSearchQuery[index]).toLowerCase()).includes(search.toLowerCase()
                            ))}
                        isLabelSorting={isLabelSorting}
                    />
                ) : (
                    <p>Your address book is empty.</p>
                )}
            </div>
            <button
                className="big-btn force-bottom-btn"
                onClick={() => createContact()}>Create Contact
            </button>
        </div>
    )
}

export default Contacts