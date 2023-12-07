const ContactsList = ({ contacts }) => {
    return (
        <>
            {contacts.map((item) => (
                <div className="contact" key={item.id}>
                    <p>{item.name} {item.lastName}</p>
                    <p>{item.phoneNumber}</p>
                </div>
            ))}
        </>
    )
}

export default ContactsList