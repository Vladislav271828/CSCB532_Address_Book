import { createContext, useState, useContext } from "react";
import AuthContext from "./AuthProvider";
import axios from "../API/axios";

const UserContext = createContext({});
const FETCH_USER_URL = '/user-profile/get-profile'

const CHANGE_PASSWORD_URL = "/user-profile/change-password"
const CHANGE_EMAIL_URL = "/user-profile/update-email-request"
const CHANGE_USERNAMES_URL = "/user-profile/update-user-names"
const DELETE_USER_URL = "user-profile/delete-user-profile"

export const UserProvider = ({ children }) => {
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [email, setEmail] = useState("");
    const [role, setRole] = useState("");
    const [adminCheck, setAdminCheck] = useState(false)

    const [errMsg, setErrMsg] = useState('');
    const [success, setSuccess] = useState(false);

    const { auth } = useContext(AuthContext);

    const fetchUser = async () => {
        try {
            const response = await axios.get(FETCH_USER_URL, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
            setFirstName(response.data.firstName);
            setLastName(response.data.lastName);
            setEmail(response.data.email);
            setRole(response.data.role);
            response.data.role == "ADMIN" && console.log(auth);
        } catch (err) {
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
            }
            else {
                setErrMsg(err.response.data.message);
            }
        }
    }

    const changeUsernames = async (firstName, lastName) => {
        setSuccess(false)
        setErrMsg("")
        try {
            const response = await axios.patch(CHANGE_USERNAMES_URL,
                JSON.stringify({ firstName, lastName }),
                {
                    headers: { "Authorization": `Bearer ${auth}`, 'Content-Type': 'application/json' }
                });
            setFirstName(response.data.firstName);
            setLastName(response.data.lastName);
            setSuccess(true)
            setErrMsg("User names changed successfully.")
        } catch (err) {
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
            } else if (err.response.status == 401) {
                alert("Token expired, please login again.");
                location.reload();
            }
            else {
                setErrMsg(err.response.data.message);
            }
        }
    }

    const changePassword = async (password) => {
        setSuccess(false)
        setErrMsg("")
        try {
            await axios.patch(CHANGE_PASSWORD_URL,
                JSON.stringify({ password }),
                {
                    headers: { "Authorization": `Bearer ${auth}`, 'Content-Type': 'application/json' }
                });
            setSuccess(true)
            setErrMsg("Password changed successfully.")
        } catch (err) {
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
            } else if (err.response.status == 401) {
                alert("Token expired, please login again.");
                location.reload();
            }
            else {
                setErrMsg(err.response.data.message);
            }
        }
    }

    const changeEmail = async (emailNew) => {
        setSuccess(false)
        setErrMsg("")
        try {
            await axios.post(CHANGE_EMAIL_URL,
                JSON.stringify({ email: emailNew }),
                {
                    headers: { "Authorization": `Bearer ${auth}`, 'Content-Type': 'application/json' }
                });
            setSuccess(true)
            setErrMsg("A verification email was sent to " + email + ".")
        } catch (err) {
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
            } else if (err.response.status == 401) {
                alert("Token expired, please login again.");
                location.reload();
            }
            else {
                setErrMsg(err.response.data.message);
            }
        }
    }

    const deleteUser = async () => {
        try {
            await axios.delete(DELETE_USER_URL, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
            alert("User deleted successfully")
            location.reload();
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
        <UserContext.Provider value={{ adminCheck, setAdminCheck, firstName, lastName, email, role, fetchUser, changeUsernames, changePassword, changeEmail, deleteUser, setErrMsg, errMsg, setSuccess, success }}>
            {children}
        </UserContext.Provider>
    )
}

export default UserContext;