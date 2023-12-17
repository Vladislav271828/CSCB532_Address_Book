import trash from "../../Icons/trash.png";

const ContactCustomRowField = ({ setCustomRows, customRows, setDeletedCustomRows, deletedCustomRows, customRowTemp, setCustomRowTemp }) => {
    const handleFormChange = (event, index) => {
        let data = [...customRows];
        data[index][event.target.name] = event.target.value;
        setCustomRows(data);
    }

    const handleDelete = (index) => {
        if (customRows[index]?.id)
            setDeletedCustomRows([...deletedCustomRows, customRows[index].id])

        let data = [...customRows];
        data.splice(index, 1)
        setCustomRows(data)

        let dataTemp = [...customRowTemp];
        dataTemp.splice(index, 1);
        setCustomRowTemp(dataTemp);
    }

    return (
        <>{customRows.map((form, index) => {
            return (
                <div key={index}>
                    <div style={{ display: "flex" }}>
                        <input
                            className="custom-row-name"
                            name='customName'
                            onChange={event => handleFormChange(event, index)}
                            placeholder="Field Name"
                            value={form?.customName}
                        />
                        <button
                            className="small-button delete-row"
                            onClick={() => handleDelete(index)}>
                            <img src={trash}
                                loading="eager"
                                alt="Delete Field" />
                        </button>
                    </div>
                    <input
                        name='customField'
                        onChange={event => handleFormChange(event, index)}
                        value={form?.customField}
                    />
                </div>
            )
        })}</>
    )
}

export default ContactCustomRowField