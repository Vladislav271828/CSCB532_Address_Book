import { createContext, useState, useContext } from "react";
import AuthContext from "./AuthProvider";
import axios from "../API/axios";

const UserContext = createContext({});
const FETCH_USER_URL = '/user-profile/get-profile'

export const UserProvider = ({ children }) => {
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [email, setEmail] = useState("");

    const { auth } = useContext(AuthContext);


    const fetchUser = async () => {
        try {
            const response = await axios.get(FETCH_USER_URL, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
            setFirstName(response.data.firstName);
            setLastName(response.data.lastName);
            setEmail(response.data.email);
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

    return (
        <UserContext.Provider value={{ firstName, lastName, email, fetchUser }}>
            {children}
        </UserContext.Provider>
    )
}

export default UserContext;