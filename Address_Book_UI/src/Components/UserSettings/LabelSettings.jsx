import LabelContext from "../../Context/LabelProvider";
import AuthContext from "../../Context/AuthProvider";
import { useContext, useEffect, useState } from "react"
import axios from "../../API/axios";
import { useNavigate } from "react-router-dom";
import LabelField from "./LabelField";

const CREATE_LABEL_URL = '/label/create-label'
const UPDATE_LABEL_URL = '/label/update-label/'
const DELETE_LABEL_URL = '/label/delete-label/'

function LabelSettings() {
    const { labels, setLabels, fetchLabels, error, setErr, isLoading, labelsTemp, setLabelsTemp } = useContext(LabelContext);
    const [newLabels, setNewLabels] = useState([])
    const [deletedLabels, setDeletedLabels] = useState([])
    const navigate = useNavigate();
    const { auth } = useContext(AuthContext);

    useEffect(() => {
        fetchLabels();
    }, [])

    const addLabel = () => {
        const num = labels.length + newLabels.length + 1;
        let object = {
            name: "Label Name #" + num,
            colorRGB: "255, 255, 255"
        }
        setNewLabels([...newLabels, object])
    }

    const createLabel = async (label) => {
        try {
            await axios.post(CREATE_LABEL_URL, {
                "name": label.name,
                "colorRGB": label.colorRGB
            }, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
        } catch (err) {
            if (!err?.response) {
                setErr('Unable to connect to server.');
            }
            else {
                setErr(err.response.data.message);
            }
            throw err
        }
    }

    const updateLabel = async (label) => {
        try {
            await axios.patch(UPDATE_LABEL_URL + label.id, {
                "name": label.name,
                "colorRGB": label.colorRGB
            }, {
                headers: { "Authorization": `Bearer ${auth}` }
            });
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
            throw err
        }
    }

    const deleteLabelFunc = async (id) => {
        try {
            await axios.delete(DELETE_LABEL_URL + id, {
                headers: {
                    "Authorization": `Bearer ${auth}`
                }
            });
        } catch (err) {
            if (!err?.response) {
                setErr("Unable to connect to server.");
            }
            else {
                setErr(err.response.data.message);
            }
            throw err
        }
    }

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const updateLabelsPromise = labels.map(async (label, index) => {
                if (label.name == "") {
                    setErr("Label names cannot be blank.");
                    throw "Label names cannot be blank.";
                }
                if (label.name != labelsTemp[index].name || label.colorRGB != labelsTemp[index].colorRGB) {
                    await updateLabel(label)
                }

            });
            await Promise.all(updateLabelsPromise);

            const newLabelsPromise = newLabels.map(async (label) => {
                if (label.name == "") {
                    setErr("Label names cannot be blank.");
                    throw "Label names cannot be blank.";
                }
                await createLabel(label)
            });
            await Promise.all(newLabelsPromise);

            const deleteLabelsPromise = deletedLabels.map(async (id) => {
                await deleteLabelFunc(id)
            });
            await Promise.all(deleteLabelsPromise);
            navigate("..", { relative: "path" });
        } catch (err) {
            console.log(err);
        }
    }

    return (
        <div className="main-container">
            <div className="main-header-container">
                <h2 className='main-header-text'>
                    Labels
                </h2>
                <button className='small-button'
                    type="button"
                    onClick={() => addLabel()}>
                    +
                </button>
            </div>
            <hr style={{ marginTop: "20px" }} />
            <form className="label-container" onSubmit={handleSubmit}>
                {isLoading && <p>Fetching labels, please wait.</p>}
                {error && <p style={{ color: "red" }}>{error}</p>}
                {!isLoading &&
                    (labels.length + newLabels.length) ? (
                    <>
                        <LabelField
                            setLabels={setLabels}
                            labels={labels}
                            setDeletedLabels={setDeletedLabels}
                            deletedLabels={deletedLabels} />
                        <LabelField
                            setLabels={setNewLabels}
                            labels={newLabels}
                            setDeletedLabels={setDeletedLabels}
                            deletedLabels={deletedLabels} />
                    </>) : (
                    <p>You don't have any labels.</p>
                )}
                <button type="submit" className='big-btn force-bottom-btn'>
                    Save Changes
                </button>
            </form>
        </div>
    )
}

export default LabelSettings