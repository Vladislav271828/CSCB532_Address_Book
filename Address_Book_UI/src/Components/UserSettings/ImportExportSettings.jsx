import { useContext, useState, useRef } from "react"
import AuthContext from "../../Context/AuthProvider";
import axios from "../../API/axios";
import UserContext from "../../Context/UserProvider";

const FETCH_FILE_URL = "/export/"
const UPLOAD_FILE_URL = "/import/"
const FETCH_FILE_ADMIN_URL = "/admin/export-as-admin/json"

function ExportSettings() {
    const [formatExport, setFormatExport] = useState("csv")
    const [files, setFiles] = useState()
    const [success, setSuccess] = useState(false)
    const [errMsg, setErrMsg] = useState("")
    const inputRef = useRef(null);

    const { auth } = useContext(AuthContext);
    const { role } = useContext(UserContext);

    const handleUploadClick = () => {
        inputRef.current?.click();
    };

    const handleFileChange = (e) => {
        if (e.target.files) {
            setFiles(e.target.files);
        }
    };

    const fetchFile = async () => {
        setSuccess(false)
        setErrMsg("")
        try {
            //doesn't seem to work unless I use this syntax
            const response = await axios({
                method: 'get',
                url: (formatExport == "json-admin") ? FETCH_FILE_ADMIN_URL : FETCH_FILE_URL + formatExport,
                headers: {
                    "Authorization": `Bearer ${auth}`
                },
                responseType: "blob"
            });
            // diabolical piece of code incoming
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement("a");
            link.href = url;
            const format = (formatExport == "json-admin") ? "json" : formatExport;
            const ext = (format == "excel") ? "xlsx" : format;
            link.download = "export_" + Date.now() + "." + ext;
            document.body.appendChild(link);
            link.click();
            link.remove()

        } catch (err) {
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
                console.log(err);
            } else if (err.response.status == 401) {
                alert("Token expired, please login again.");
                location.reload();
            }
            else {
                setErrMsg(err.response.data.message);
            }
        }
    }

    const uploadFile = async () => {
        setSuccess(false)

        if (!files) {
            setErrMsg("No file uploaded")
            return;
        }

        const filesiterable = files ? [...files] : [];

        const data = new FormData();
        filesiterable.forEach((file) => {
            data.append(`file`, file, file.name);
        });

        let format = "";
        switch (files[0].type) {
            case "text/csv":
                format = "csv";
                break;
            case "application/json":
                format = "json";
                break;
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                format = "excel";
                break;
            default:
                setErrMsg("Invalid file format")
                return;
        }
        setErrMsg("")
        try {
            const response = await axios.post(
                UPLOAD_FILE_URL + format,
                (format == "excel") ? data : files[0],
                {
                    headers: {
                        "Authorization": `Bearer ${auth}`,
                        'content-type': (format == "excel") ? 'multipart/form-data' : files[0].type,
                    }
                });
            setErrMsg(response.data)
            setSuccess(true)
        } catch (err) {
            setSuccess(false)
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
                console.log(err);
            } else if (err.response.status == 401) {
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
            <div className="main-header-container">
                <h2 className='main-header-text'>
                    Import/Export Contacts
                </h2>
            </div>
            <hr style={{ marginTop: "20px" }} />
            {errMsg == "" ? <></> : <p className='login-form-text' style={success ? { padding: "10px 0px 0px 0px" } : { color: "red", padding: "10px 0px 0px 0px" }}>{errMsg}</p>}

            <div className="user-settings-form">
                <h3>Import Contacts</h3>
                <div>
                    <button
                        className="wide-button"
                        onClick={() => handleUploadClick()}>
                        {files ? `${files[0].name}` : 'Click to upload file'}
                    </button>
                    <input
                        type="file"
                        ref={inputRef}
                        onChange={e => handleFileChange(e)}
                        style={{ display: 'none' }}
                        accept=".xlsx,.json,.csv"
                    />
                    {files &&
                        <button
                            title="Submit"
                            className="small-button"
                            style={(files) ? { backgroundColor: "rgb(191, 244, 174)" } : {}}
                            type="button"
                            onClick={() => uploadFile()}
                        >
                            ✓
                        </button>}
                </div>
            </div>
            <div className="user-settings-form">
                <h3>Export Contacts</h3>
                <div>
                    <select
                        name='format'
                        value={formatExport}
                        onChange={event => setFormatExport(event.target.value)}>
                        <option value="csv">CSV File</option>
                        <option value="json">JSON File</option>
                        <option value="excel">Excel File</option>
                        {(role == "ADMIN") && <option value="json-admin">JSON File - ADMIN</option>}
                    </select>
                    <button
                        title="Submit"
                        className="small-button"
                        style={{ backgroundColor: "rgb(191, 244, 174)" }}
                        type="button"
                        onClick={() => fetchFile()}
                    >
                        ✓
                    </button>
                </div>
            </div>
        </div>
    )
}

export default ExportSettings