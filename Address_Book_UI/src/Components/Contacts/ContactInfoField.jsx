const ContactInfoField = ({ labelName, labelContents, isHidden = true }) => {
    return (
        <>
            {(isHidden) ? <div>
                <h3>{labelName}</h3>
                <p>{labelContents}</p>
            </div> : <></>}</>
    )
}

export default ContactInfoField