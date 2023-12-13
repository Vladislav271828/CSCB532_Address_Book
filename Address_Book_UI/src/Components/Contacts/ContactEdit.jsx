import { useState, useContext } from "react"
import { useParams, useNavigate } from "react-router-dom";
import ContactsContext from "../../Context/ContactsProvider";
import AuthContext from "../../Context/AuthProvider";
import axios from '../../API/axios'

const UPDATE_CONTACT_URL = "/contact/update-contact/"

function ContactEdit() {
    const { auth } = useContext(AuthContext);
    const { contacts, setContacts } = useContext(ContactsContext);
    const { id } = useParams();
    const contact = contacts.find(contact => (contact.id).toString() === id);

    const [name, setName] = useState(contact.name)
    const [lastName, setLastName] = useState(contact.lastName)
    const [phoneNumber, setPhoneNumber] = useState(contact.phoneNumber)
    const [nameOfCompany, setCompany] = useState(contact.nameOfCompany)
    const [address, setAddress] = useState(contact.address)
    const [email, setEmail] = useState(contact.email)
    const [fax, setFax] = useState(contact.fax)
    const [mobileNumber, setMobileNumber] = useState(contact.mobileNumber)

    const [errMsg, setErrMsg] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.patch(UPDATE_CONTACT_URL + id,
                JSON.stringify({ name, lastName, phoneNumber, nameOfCompany, address, email, fax, mobileNumber }),
                {
                    headers: { "Authorization": `Bearer ${auth}`, 'Content-Type': 'application/json' }
                }
            )

            const listContacts = contacts.map((item) => item.id == id ? response.data : item);
            setContacts(listContacts);
            navigate("..", { relative: "path" });
        } catch (err) {
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
            }
            else if (err.response.status == 401) {
                alert("Token expired, please login again.");
                location.reload();
            }
            else {
                setErrMsg(err.response.data.message);
            }
        }
    }

    return (
        <div className="main-container">
            <h2 className='main-header-text'>
                Edit Contact
            </h2>
            <hr style={{ marginTop: "20px" }} />
            {errMsg == "" ? <></> : <p className='login-form-text' style={{ color: "red", padding: "10px 0px 0px 0px" }}>{errMsg}</p>}
            <form className="contact-info-field-container" onSubmit={handleSubmit}>
                <div>
                    <h3>First Name</h3>
                    <input type="text"
                        id="name"
                        defaultValue={name}
                        onChange={(e) => setName(e.target.value)}
                        onFocus={() => setErrMsg('')}
                    />
                </div>
                <div>
                    <h3>Last Name</h3>
                    <input type="text"
                        id="lastName"
                        defaultValue={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        onFocus={() => setErrMsg('')}
                    />
                </div>
                <div>
                    <h3>Phone Number</h3>
                    <input type="tel"
                        id="phoneNumber"
                        defaultValue={phoneNumber}
                        required
                        onChange={(e) => setPhoneNumber(e.target.value)}
                        onFocus={() => setErrMsg('')}
                    /></div>
                <div>
                    <h3>Company</h3>
                    <input type="text"
                        id="nameOfCompany"
                        defaultValue={nameOfCompany}
                        onChange={(e) => setCompany(e.target.value)}
                        onFocus={() => setErrMsg('')}
                    /></div>
                <div>
                    <h3>Address</h3>
                    <input type="text"
                        id="address"
                        defaultValue={address}
                        onChange={(e) => setAddress(e.target.value)}
                        onFocus={() => setErrMsg('')}
                    /></div>
                <div>
                    <h3>Email</h3>
                    <input type="email"
                        id="email"
                        defaultValue={email}
                        onChange={(e) => setEmail(e.target.value)}
                        onFocus={() => setErrMsg('')}
                    /></div>
                <div>
                    <h3>Fax</h3>
                    <input type="tel"
                        id="fax"
                        defaultValue={fax}
                        onChange={(e) => setFax(e.target.value)}
                        onFocus={() => setErrMsg('')}
                    /></div>
                <div>
                    <h3>Mobile Number</h3>
                    <input type="tel"
                        id="mobileNumber"
                        defaultValue={mobileNumber}
                        onChange={(e) => setMobileNumber(e.target.value)}
                        onFocus={() => setErrMsg('')}
                    /></div>
                <button className="big-btn new-contacts-btn">
                    Save Changes
                </button>
            </form>
        </div>
    )
}

export default ContactEdit