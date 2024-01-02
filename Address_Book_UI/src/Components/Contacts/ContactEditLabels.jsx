import { useContext, useEffect } from "react"
import LabelContext from "../../Context/LabelProvider"

const ContactEditLabels = ({ showLabelDropdown, setShowLabelDropdown, selectedLabels, setSelectedLabels, addedLabels, setAddedLabels, removedLabels, setRemovedLabels }) => {
    const { labels, labelsToString, fetchLabels } = useContext(LabelContext);

    useEffect(() => {
        fetchLabels();
    }, [])

    const handleChange = (event, label) => {
        if (event.target.checked) {
            //set display
            setSelectedLabels([...selectedLabels, label]);
            //add to added
            setAddedLabels([...addedLabels, label.id]);
            //remove from removed
            const rmData = removedLabels.filter((id) => id !== label.id)
            setRemovedLabels(rmData);
        }
        else {
            //remove from display
            const data = selectedLabels.filter((item) => item.id !== label.id)
            setSelectedLabels(data)
            //add to removed
            setRemovedLabels([...removedLabels, label.id]);
            //remove from added
            const addData = addedLabels.filter((id) => id !== label.id)
            setAddedLabels(addData)
        }
    }


    return (
        <div className="one-line-field">
            <h3>Labels</h3>
            <div>
                <div
                    className="input-style-box prevent-select"
                    onClick={() => setShowLabelDropdown(!showLabelDropdown)}
                    onMouseLeave={() => setShowLabelDropdown(false)}>
                    {labelsToString(selectedLabels)}
                </div>
                <div className="labels-dropdown-container"
                    style={showLabelDropdown ? { display: "flex" } : {}}>
                    {(labels.length) ? labels.map((item) => (
                        <div key={item.id}>
                            <input
                                className="checkbox"
                                type="checkbox"
                                value={item.id}
                                onChange={(e) => handleChange(e, item)}
                                checked={selectedLabels.filter(e => e.id == item.id).length > 0}
                            />
                            <label>{item.name}</label>
                        </div>
                    )) : <p>You don't have any labels.</p>}
                </div>
            </div>
        </div>
    )
}

export default ContactEditLabels