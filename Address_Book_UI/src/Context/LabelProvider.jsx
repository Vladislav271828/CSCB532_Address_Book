import { createContext, useState, useContext } from "react";
import AuthContext from "./AuthProvider";
import axios from "../API/axios";

const LabelContext = createContext({});
const FETCH_LABEL_URL = 'label/get-all-labels'

export const LabelProvider = ({ children }) => {
    const [labels, setLabels] = useState([]);

    const { auth } = useContext(AuthContext);


    const fetchLabels = async () => {
        try {
            const response = await axios.get(FETCH_LABEL_URL, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
            setLabels(response.data);
        } catch (err) {
            if (!err?.response.data?.message) {
                console.log(err);
            }
            else {
                console.log("fetchLabel: " + err.response.data.message);
            }
        }
    }

    return (
        <LabelContext.Provider value={{ labels, fetchLabels }}>
            {children}
        </LabelContext.Provider>
    )
}

export default LabelContext;