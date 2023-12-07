const ContactsList = ({ contacts }) => {
    return (
        <>
            {contacts.map((item) => (
                <div className="contact" key={item.id}>
                    <p style={{ fontWeight: "600" }}>{item.name} {item.lastName}</p>
                    <p>{item.phoneNumber}</p>
                </div>
            ))}
        </>
    )
}

export default ContactsList