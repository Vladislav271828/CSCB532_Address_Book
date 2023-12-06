const ContactsList = ({ contacts }) => {
    return (
        <>
            {contacts.map((item) => (
                <div key={item.id}>
                    <p>{item.name}</p>
                    <p>{item.lastName}</p>
                    <p>{item.phoneNumber}</p>
                    <br />
                </div>
            ))}
        </>
    )
}

export default ContactsList