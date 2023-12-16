import ContactCustomRowField from "./ContactCustomRowField";


const ContactEditRows = ({ customRows, setCustomRows, newCustomRows, setNewCustomRows, deletedCustomRows, setDeletedCustomRows }) => {

    const addRow = () => {
        let object = {
            customName: "",
            customField: ""
        }
        setNewCustomRows([...newCustomRows, object])
    }

    return (
        <>
            <ContactCustomRowField
                setCustomRows={setCustomRows}
                customRows={customRows}
                setDeletedCustomRows={setDeletedCustomRows}
                deletedCustomRows={deletedCustomRows} />
            <ContactCustomRowField
                setCustomRows={setNewCustomRows}
                customRows={newCustomRows}
                setDeletedCustomRows={setDeletedCustomRows}
                deletedCustomRows={deletedCustomRows} />
            <div style={{ textAlign: "center" }}>
                <button type="button" className='small-button add-row'
                    onClick={() => addRow()}>
                    Add Field
                </button>
            </div>
        </>
    )
}

export default ContactEditRows