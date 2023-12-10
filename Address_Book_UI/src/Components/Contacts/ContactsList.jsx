import { Link } from "react-router-dom";

const ContactsList = ({ contacts }) => {
    const compareFN = (a, b) => {
        if (!a.label?.name && b.label?.name)
            return 1;
        if (!b.label?.name && a.label?.name)
            return -1;

        let result;
        if (a.label == b.label || a.label?.name == b.label?.name) {
            result = a.name.localeCompare(b.name);
            if (result == 0) {
                result = a.lastName.localeCompare(b.lastName);
            }
            return result;
        }
        result = a.label.name.localeCompare(b.label.name)
        return result;
    }

    return (
        <>
            {contacts.sort(compareFN).map((item) => (
                <div key={item.id}>
                    <Link to={`/contact/${item.id}`}>
                        <div className="contact">
                            <p style={{ fontWeight: "600" }}>{item.name} {item.lastName}</p>
                            <p>{item.phoneNumber}</p>
                        </div>
                    </Link>
                </div>
            ))}
        </>
    )
}

export default ContactsList