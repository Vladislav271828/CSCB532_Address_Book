import { useState, useContext, useEffect } from "react"
import UserContext from "../../Context/UserProvider";
import SearchBar from "../SearchBar";
import AuthContext from "../../Context/AuthProvider";
import axios from "../../API/axios";
import LabelContext from "../../Context/LabelProvider";

const SEARCH_CONTACTS_URL = '/contact/search-contact'
const SEARCH_LABELS_URL = "/contact/get-contacts-with-label/"

const FETCH_CONTACTS_ADMIN_URL = '/admin/get-all-contacts-as-admin'
const SEARCH_CONTACTS_ADMIN_URL = '/admin/search-contact-as-admin'
const SEARCH_LABELS_ADMIN_URL = "/admin/get-contacts-with-label-as-admin/"

function QuerySettings() {
    const [showTable, setShowTable] = useState(false)
    const [option, setOption] = useState(0);
    const [labelId, setLabelId] = useState(1);
    const [name, setName] = useState("");
    const [lastName, setLastName] = useState("");
    const [errMsg, setErrMsg] = useState("")
    const [tableData, setTableData] = useState([]);

    const { role, adminCheck, setAdminCheck } = useContext(UserContext);
    const { labels, fetchLabels, adminLabels, fetchAdminLabels } = useContext(LabelContext)
    const { auth } = useContext(AuthContext);

    useEffect(() => {
        adminCheck ? fetchAdminLabels() : fetchLabels();
    }, [adminCheck])

    const searchLabels = async () => {
        try {
            const response = await axios.get(
                (adminCheck ? SEARCH_LABELS_ADMIN_URL : SEARCH_LABELS_URL) + labelId, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
            setTableData(response.data)
        } catch (err) {
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
                console.log(err)
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

    const getAllRecords = async () => {
        try {
            const response = await axios.get(
                FETCH_CONTACTS_ADMIN_URL, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
            setTableData(response.data)
        } catch (err) {
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
                console.log(err)
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


    const searchContacts = async () => {
        try {
            const response = await axios.post(
                adminCheck ? SEARCH_CONTACTS_ADMIN_URL : SEARCH_CONTACTS_URL,
                {
                    "name": (option != "2") ? name : null,
                    "lastName": (option != "1") ? lastName : null
                }, {
                headers: { "Authorization": `Bearer ${auth}`, 'Content-Type': 'application/json' }
            });
            setTableData(response.data)
        } catch (err) {
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
                console.log(err)
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

    const handleSubmit = () => {
        setShowTable(true)
        switch (option) {
            case "0":
                searchLabels()
                break;
            case "4":
                getAllRecords();
                break;
            case "1":
            case "2":
            case "3":
                searchContacts();
                break;
            default:
                break;
        }
    }

    return (
        <div className="main-container"
            style={{ width: "1000px" }}>
            <div className="main-header-container">
                <h2 className='main-header-text'>
                    Queries
                </h2>
            </div>
            <hr style={{ marginTop: "20px" }} />
            <div className="query-container">
                {(role == "ADMIN") && <div className="admin"><input
                    className="checkbox"
                    type="checkbox"
                    onChange={() => setAdminCheck(!adminCheck)}
                    checked={adminCheck} /><p>Admin Queries</p></div>
                }
                <div>
                    <select
                        name='option'
                        value={option}
                        onChange={event => {
                            setOption(event.target.value);
                            setShowTable(false)
                            setTableData([])
                        }}>
                        <option value="0">Records with the most common labels.</option>
                        <option value="1">All records with the same first name and different last names.</option>
                        <option value="2">All records with different first names and the same last name.</option>
                        <option value="3">Record with a specific first and last name.</option>
                        {(adminCheck) && <option value="4">Get all records.</option>}
                    </select>
                    <button
                        className="small-button"
                        style={{ backgroundColor: "rgb(191, 244, 174)" }}
                        type="button"
                        onClick={() => handleSubmit()}
                    >
                        ✓
                    </button>
                </div>
                {option == "0" && <div><select
                    name='option'
                    disabled={(!labels.length)}
                    value={labelId}
                    onChange={e => setLabelId(e.target.value)}
                >
                    {adminCheck ? ((adminLabels.length) ? adminLabels.map((item) => <option key={item.id} value={item.id}>{item.id}: {item.name}</option>) : <option>No labels available</option>)
                        : ((labels.length) ? labels.map((item) => <option key={item.id} value={item.id}>{item.name}</option>) : <option>No labels available</option>)}
                </select></div>}
                {option == "1" && <div>
                    <SearchBar
                        search={name}
                        setSearch={setName}
                        placeholder={"Enter First Name"} />
                </div>}
                {option == "2" && <div>
                    <SearchBar
                        search={lastName}
                        setSearch={setLastName}
                        placeholder={"Enter Last Name"} />
                </div>}
                {option == "3" && <div className="qfnln">
                    <SearchBar
                        search={name}
                        setSearch={setName}
                        placeholder={"Enter First Name"} />
                    <SearchBar
                        search={lastName}
                        setSearch={setLastName}
                        placeholder={"Enter Last Name"} />
                </div>}
                {errMsg == "" ? <></> : <p style={{ color: "red", marginTop: "10px" }}>{errMsg}</p>}
            </div>
            <div className="table-container">
                {showTable && <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>First Name</th>
                            <th>Last Name</th>
                            <th>Phone Number</th>
                            <th>Company</th>
                            <th>Address</th>
                            <th>Email</th>
                            <th>Fax</th>
                            <th>Mobile Number</th>
                            <th>Label</th>
                            <th>Comment</th>
                            <th>Custom Rows</th>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            tableData.map((row) => {
                                const rowLabels = row?.labels.sort((aL, bL) => aL.name.localeCompare(bL.name))
                                return (
                                    <tr key={row.id} style={(rowLabels.length > 0) ?
                                        { backgroundColor: `rgb(${rowLabels[0].colorRGB})` } : {}}>
                                        <td>{row?.id}</td>
                                        <td>{row?.name}</td>
                                        <td>{row?.lastName}</td>
                                        <td>{row?.phoneNumber}</td>
                                        <td>{row?.nameOfCompany}</td>
                                        <td>{row?.address}</td>
                                        <td>{row?.email}</td>
                                        <td>{row?.fax}</td>
                                        <td>{row?.mobileNumber}</td>
                                        <td style={{ whiteSpace: "pre" }}>{(rowLabels.length > 0) ? rowLabels.map((obj) => `${obj.name}\n`) : "None"}</td>
                                        <td>{row?.comment}</td>
                                        <td style={{ whiteSpace: "pre" }}>{row?.customRows?.map((obj) => `${obj.customName}: ${obj.customField}\n`)}</td>
                                    </tr>
                                )
                            })}
                    </tbody>
                </table>}
            </div>
        </div>

    )
}

export default QuerySettings