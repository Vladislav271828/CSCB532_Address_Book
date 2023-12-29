import { useContext } from "react"
import LabelContext from "../../Context/LabelProvider"

const ContactEditLabels = ({ labels, showLabelDropdown, setShowLabelDropdown, selectedLabels, setSelectedLabels }) => {
    const { labelsToString } = useContext(LabelContext);

    const handleChange = (event, label) => {
        if (event.target.checked) {
            setSelectedLabels([...selectedLabels, label]);
        }
        else {
            const data = selectedLabels.filter((item) => item.id !== label.id)
            setSelectedLabels(data)
        }
        console.log(selectedLabels)
    }

    console.log(labelsToString(labels))

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
                            // checked={adminCheck} 
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