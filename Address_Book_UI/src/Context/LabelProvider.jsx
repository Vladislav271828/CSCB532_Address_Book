import { createContext, useState, useContext } from "react";
import AuthContext from "./AuthProvider";
import axios from "../API/axios";

const LabelContext = createContext({});
const FETCH_LABEL_URL = '/label/get-all-labels'

export const LabelProvider = ({ children }) => {
    const [labels, setLabels] = useState([]);
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

    const labelsToString = (labels) => {
        return labels.map((item, index) => {
            if (index == 0)
                return <strong style={{ fontWeight: "600" }}>{item.name}</strong>
            else return <>, {item.name}</>
        }
        )
    }

    return (
        <LabelContext.Provider value={{ labels, setLabels, fetchLabels, setErr, error, isLoading, labelsTemp, setLabelsTemp, labelsToString }}>
            {children}
        </LabelContext.Provider>
    )
}

export default LabelContext;