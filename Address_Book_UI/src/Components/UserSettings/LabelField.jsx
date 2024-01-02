import trash from "../../Icons/trash.webp";

const LabelField = ({ setLabels, labels, setDeletedLabels, deletedLabels }) => {

    const handleFormChange = (event, index) => {
        let data = [...labels];
        data[index][event.target.name] = event.target.value;
        setLabels(data);
    }

    const handleDelete = (index) => {
        if (labels[index]?.id)
            setDeletedLabels([...deletedLabels, labels[index].id])
        else {
            let data = [...labels];
            data.splice(index, 1);
            setLabels(data);
        }
    }

    return (
        <>{labels.map((form, index) => {
            const isDeleted = deletedLabels.includes(form?.id);
            return (
                <div key={index}
                    style={isDeleted ? { opacity: "50%" } : {}}>
                    <input
                        name='name'
                        onChange={event => handleFormChange(event, index)}
                        placeholder="Label Name"
                        value={form?.name}
                        disabled={isDeleted}
                    />
                    <select
                        name='colorRGB'
                        value={form?.colorRGB}
                        disabled={isDeleted}
                        onChange={event => handleFormChange(event, index)}>
                        <option value="255, 255, 255">White</option>
                        <option value="244, 191, 174">Red</option>
                        <option value="191, 244, 174">Green</option>
                        <option value="174, 191, 244">Blue</option>
                        <option value="191, 161, 244">Purple</option>
                    </select>
                    <button
                        title="Delete Label"
                        className="small-button"
                        type="button"
                        style={!isDeleted ? { backgroundColor: "rgb(244, 191, 174)" } : {}}
                        disabled={isDeleted}
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