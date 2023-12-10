import { useContext } from "react";
import ContactsContext from "../../Context/ContactsProvider";
import { useParams } from "react-router-dom";

function ContactDetails() {
    const { contacts } = useContext(ContactsContext);
    const { id } = useParams();
    const contact = contacts.find(contact => (contact.id).toString() === id);

    return (
        <div className="main-container">
            <h2 className='main-header'>
                Contact Details
            </h2>
            <hr style={{ marginTop: "20px" }} />
            <p>{contact.name}</p>
        </div>
    )
}

export default ContactDetails