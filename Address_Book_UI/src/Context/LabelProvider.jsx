import { createContext, useState, useContext } from "react";
import AuthContext from "./AuthProvider";
import axios from "../API/axios";

const LabelContext = createContext({});
const FETCH_LABEL_URL = '/label/get-all-labels'
const FETCH_ADMIN_LABEL_URL = '/admin/get-all-labels-as-admin'

export const LabelProvider = ({ children }) => {
    const [labels, setLabels] = useState([]);
    const [adminLabels, setAdminLabels] = useState([]);
    const [error, setErr] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [labelsTemp, setLabelsTemp] = useState([]);

    const { auth } = useContext(AuthContext);

    const fetchLabels = async () => {
        try {
            const response = await axios.get(FETCH_LABEL_URL, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
            setIsLoading(true);
            const sorted = response.data.sort((a, b) => a.name.localeCompare(b.name))
            setLabels(sorted);
            setLabelsTemp(JSON.parse(JSON.stringify(sorted)))
            setErr(null);
            setIsLoading(false);
        } catch (err) {
            if (!err?.response) {
                setErr('Unable to connect to server.');
            }
            else if (err.response.status == 401) {
                alert("Token expired, please login again.");
                location.reload();
            }
            else {
                setErr(err.response.data.message);
            }
            setIsLoading(false);
        }
    }

    const fetchAdminLabels = async () => {
        try {
            const response = await axios.get(FETCH_ADMIN_LABEL_URL, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
            const sorted = response.data.sort((a, b) => a.id - b.id)
            setAdminLabels(sorted);
        } catch (err) {
            if (!err?.response) {
                console.log('Unable to connect to server.');
            }
            else if (err.response.status == 401) {
                alert("Token expired, please login again.");
                location.reload();
            }
            else {
                console.log(err.response.data.message);
            }
        }
    }

    const labelsToString = (labels) => {
        return labels.sort((a, b) => a.name.localeCompare(b.name)).map((item, index) => {
            if (index == 0)
                return <span key={item.id} style={{ backgroundColor: `rgb(${item.colorRGB})` }}>{item.name}</span>
            else return <span key={item.id}>, <span style={{ backgroundColor: `rgb(${item.colorRGB})` }}>{item.name}</span></span>
        }
        )
    }

    return (
        <LabelContext.Provider value={{ labels, setLabels, fetchLabels, setErr, error, isLoading, labelsTemp, setLabelsTemp, labelsToString, fetchAdminLabels, adminLabels }}>
            {children}
        </LabelContext.Provider>
    )
}

export default LabelContext;