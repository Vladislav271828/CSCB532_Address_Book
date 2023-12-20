import { useContext, useState } from "react"
import AuthContext from "../../Context/AuthProvider";
import axios from "../../API/axios";
import UserContext from "../../Context/UserProvider";

const FETCH_FILE_URL = "contact/export/"
const FETCH_FILE_ADMIN_URL = "/admin/export-as-admin/"

function ExportSettings() {
    const [format, setFormat] = useState("csv")
    const [errMsg, setErrMsg] = useState("")

    const { auth } = useContext(AuthContext);
    const { role, adminCheck, setAdminCheck } = useContext(UserContext);

    const fetchFile = async () => {
        setErrMsg("")
        try {
            //doesn't seem to work unless I use this syntax
            const response = await axios({
                method: 'get',
                url: ((adminCheck) ? FETCH_FILE_ADMIN_URL : FETCH_FILE_URL) + format,
                headers: {
                    "Authorization": `Bearer ${auth}`
                },
                responseType: "blob"
            });
            // diabolical piece of code incoming
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement("a");
            link.href = url;
            const ext = (format == "excel") ? "xlsx" : format;
            link.download = "export_" + Date.now() + "." + ext;
            document.body.appendChild(link);
            link.click();
            link.remove()

        } catch (err) {
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
                console.log(err);
            }
            else {
                setErrMsg(err.response.data.message);
            }
        }
    }

    return (
        <div className="main-container">
            <div className="main-header-container">
                <h2 className='main-header-text'>
                    Export Contacts
                </h2>
            </div>
            <hr style={{ marginTop: "20px" }} />
            {errMsg == "" ? <></> : <p className='login-form-text' style={{ color: "red", marginTop: "10px" }}>{errMsg}</p>}
            <div className="user-settings-form">
                <h3>Export Contacts</h3>
                <p>File Format: </p>
                <div>
                    <select
                        name='format'
                        value={format}
                        onChange={event => setFormat(event.target.value)}>
                        <option value="csv">CSV File</option>
                        <option value="json">JSON File</option>
                        <option value="excel">Excel File</option>
                    </select>
                    <button
                        className="small-button"
                        style={{ backgroundColor: "rgb(191, 244, 174)" }}
                        type="button"
                        onClick={() => fetchFile()}
                    >
                        ✓
                    </button>
                </div>
                {(role == "ADMIN") && <div><input
                    className="checkbox"
                    type="checkbox"
                    onChange={() => setAdminCheck(!adminCheck)}
                    checked={adminCheck} /><p>Admin Export</p></div>
                }
            </div>
        </div>
    )
}

export default ExportSettings