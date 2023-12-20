import { useState, useContext, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom";

import ContactsContext from "../../Context/ContactsProvider";
import AuthContext from "../../Context/AuthProvider";
import LabelContext from "../../Context/LabelProvider";

import axios from '../../API/axios'
import trash from "../../Icons/trash.png";
import ContactEditRows from "./ContactEditRows";

const UPDATE_CONTACT_URL = "/contact/update-contact/"
const DELETE_CONTACT_URL = "/contact/delete-contact/"
const CREATE_CUSTOM_ROW_URL = "/custom-row/create-custom-row"
const DELETE_CUSTOM_ROW_URL = "/custom-row/delete-custom-row-by-id/"
const UPDATE_CUSTOM_ROW_URL = "/custom-row/update-custom-row/"

function ContactEdit() {
    const { auth } = useContext(AuthContext);
    const { contacts, setContacts } = useContext(ContactsContext);
    const { labels, fetchLabels } = useContext(LabelContext);

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
    const [comment, setComment] = useState(contact.comment)
    const [labelId, setLabelId] = useState((contact.label?.id) ? contact.label.id : "0")

    const [customRows, setCustomRows] = useState((contact?.customRows) ? contact.customRows : [])
    const [newCustomRows, setNewCustomRows] = useState([])
    const [deletedCustomRows, setDeletedCustomRows] = useState([])

    // customRowTemp is used to prevent rerendering while handing custom rows
    // JSON.parse(JSON.stringify()) is used to make a deep copy
    const [customRowTemp, setCustomRowTemp] = useState(JSON.parse(JSON.stringify(customRows)))

    const [errMsg, setErrMsg] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        fetchLabels();
    }, [])

    const changeLabelOfContact = async (url) => {
        // works only if body is null
        try {
            await axios.patch(url, null, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
        } catch (err) {
            if (!err?.response) {
                console.log(err);
            }
            else {
                console.log("changeLabelOfContact: " + err.response.data.message);
            }
        }
    }

    const createCustomRow = async (name, field) => {
        try {
            const response = await axios.post(CREATE_CUSTOM_ROW_URL, {
                "contactId": id,
                "customName": name,
                "customField": field
            }, {
                headers: { "Authorization": `Bearer ${auth}`, 'Content-Type': 'application/json' }
            });
            setCustomRowTemp([...customRowTemp, response])

        } catch (err) {
            if (!err?.response) {
                console.log(err);
            }
            else {
                console.log("createCustomRow: " + err.response.data.message);
            }
        }
    }

    const updateCustomRow = async (name, field, rowId) => {
        try {
            const response = await axios.patch(UPDATE_CUSTOM_ROW_URL + rowId, {
                "customName": name,
                "customField": field
            }, {
                headers: { "Authorization": `Bearer ${auth}`, 'Content-Type': 'application/json' }
            });
            const listCustomRow = customRowTemp.map((item) => item.id == id ? response.data : item);
            setCustomRowTemp(listCustomRow)
        } catch (err) {
            if (!err?.response) {
                console.log(err);
            }
            else {
                console.log("createCustomRow: " + err.response.data.message);
            }
        }
    }

    const deleteCustomRowFunc = async (rowId) => {
        try {
            await axios.delete(DELETE_CUSTOM_ROW_URL + rowId, {
                headers: {
                    "Authorization": `Bearer ${auth}`
                }
            });
            const rows = customRowTemp.filter((row) => row.id !== rowId)
            setCustomRowTemp(rows);

        } catch (err) {
            if (!err?.response) {
                console.log(err);
            }
            else {
                console.log("deleteCustomRow: " + err.response.data.message);
            }
        }
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const lastLabelId = (contact.label?.id) ? contact.label.id : "0";
            if (lastLabelId != labelId) {
                if (labelId == "0")
                    await changeLabelOfContact("/contact/" + id + "/remove-label");
                else
                    await changeLabelOfContact("/contact/" + id + "/add-label/" + labelId);
            }

            const updateRowsPromise = customRows.map(async (row, index) => {
                if (row.customName != customRowTemp[index].customName || row.customField != customRowTemp[index].customField) {
                    await updateCustomRow(row.customName, row.customField, row.id)
                }
            });
            await Promise.all(updateRowsPromise);

            const newRowsPromise = newCustomRows.map(async (row) => {
                await createCustomRow(row.customName, row.customField)
            });
            await Promise.all(newRowsPromise);

            const deleteRowsPromise = deletedCustomRows.map(async (id) => {
                await deleteCustomRowFunc(id)
            });
            await Promise.all(deleteRowsPromise);

            const response = await axios.patch(UPDATE_CONTACT_URL + id,
                JSON.stringify({ name, lastName, phoneNumber, nameOfCompany, address, email, fax, mobileNumber, comment }),
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

    const deleteContact = async () => {
        if (confirm("Are you sure you want to delete this contact?")) {
            try {
                await axios.delete(DELETE_CONTACT_URL + id, {
                    headers: { "Authorization": `Bearer ${auth}` }
                });
                navigate("/", { replace: true });
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
    }

    return (
        <div className="main-container">
            <div className="main-header-container">
                <h2 className='main-header-text'>
                    Edit Contact
                </h2>
                <button className='small-button'
                    style={{ backgroundColor: "rgb(244, 191, 174)" }}
                    onClick={() => deleteContact()}>
                    <img src={trash}
                        alt="Delete Contact" />
                </button>
            </div>
            <hr style={{ marginTop: "15px" }} />
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

                {/* Label */}
                <div>
                    <h3>Label</h3>
                    <select
                        value={labelId}
                        onChange={e => setLabelId(e.target.value)}>
                        <option value="0">None</option>
                        {labels.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
                    </select></div>

                {/* Custom Row */}
                <ContactEditRows
                    customRows={customRows}
                    setCustomRows={setCustomRows}
                    customRowTemp={customRowTemp}
                    setCustomRowTemp={setCustomRowTemp}
                    newCustomRows={newCustomRows}
                    setNewCustomRows={setNewCustomRows}
                    deletedCustomRows={deletedCustomRows}
                    setDeletedCustomRows={setDeletedCustomRows} />

                {/* Comment */}
                <div className="comment">
                    <h3>Comment</h3>
                    <textarea
                        id="comment"
                        defaultValue={comment}
                        onChange={(e) => setComment(e.target.value)}
                        onFocus={() => setErrMsg('')}
                        rows={4} />
                </div>

                <button className="big-btn force-bottom-btn">
                    Save Changes
                </button>
            </form>
        </div>
    )
}

export default ContactEdit