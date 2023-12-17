import trash from "../../Icons/trash.png";
import { useContext } from "react"
import LabelContext from "../../Context/LabelProvider";

const LabelField = ({ setLabels, labels, setDeletedLabels, deletedLabels }) => {

    const { labelsTemp, setLabelsTemp } = useContext(LabelContext);

    const handleFormChange = (event, index) => {
        let data = [...labels];
        data[index][event.target.name] = event.target.value;
        setLabels(data);
    }

    const handleDelete = (index) => {
        if (labels[index]?.id)
            setDeletedLabels([...deletedLabels, labels[index].id])

        let data = [...labels];
        data.splice(index, 1);
        setLabels(data);

        let dataTemp = [...labelsTemp];
        dataTemp.splice(index, 1);
        setLabelsTemp(dataTemp);
    }

    return (
        <>{labels.map((form, index) => {
            return (
                <div key={index}>
                    <input
                        name='name'
                        onChange={event => handleFormChange(event, index)}
                        placeholder="Label Name"
                        value={form?.name}
                    />
                    <select
                        name='colorRGB'
                        value={form?.colorRGB}
                        onChange={event => handleFormChange(event, index)}>
                        <option value="255, 255, 255">White</option>
                        <option value="244, 191, 174">Red</option>
                        <option value="191, 244, 174">Green</option>
                        <option value="174, 191, 244">Blue</option>
                        <option value="191, 174, 244">Purple</option>
                    </select>
                    <button
                        className="small-button"
                        type="button"
                        style={{ backgroundColor: "#f4c0ae" }}
                        onClick={() => handleDelete(index)}>
                        <img src={trash}
                            loading="eager"
                            alt="Delete Label" />
                    </button>
                </div>
            )
        })}</>
    )
}

export default LabelField