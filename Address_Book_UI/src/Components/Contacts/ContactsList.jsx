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

    const convertToCSS = (colorArray) => {
        return `rgb(${colorArray[0]}, ${colorArray[1]}, ${colorArray[2]})`;
    }

    return (
        <>
            {contacts.sort(compareFN).map((item) => {

                const color = (item?.label != null) ? item.label.colorRGB.split(", ", 3) : [255, 255, 255];
                const hoverColor = color.map((value) => Math.round(value * 0.917));

                return (
                    <div key={item.id}>
                        <Link to={`/contact/${item.id}`}>
                            <div className="contact"
                                style={
                                    { backgroundColor: convertToCSS(color), color: convertToCSS(hoverColor) }}>
                                <p style={{ fontWeight: "600" }}>{item.name} {item.lastName}</p>
                                <p>{item.phoneNumber}</p>
                            </div>
                        </Link>
                    </div>
                )
            })}
        </>
    )
}

export default ContactsList