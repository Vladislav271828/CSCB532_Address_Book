import { Link } from "react-router-dom";

const ContactsList = ({ contacts, isLabelSorting }) => {
    const compareFN = (a, b) => {
        let result;
        if (isLabelSorting) {
            const aLabelsSorted = a.labels.sort((aL, bL) => aL.name.localeCompare(bL.name)).map((item) => { return item.name }).join('')
            const bLabelsSorted = b.labels.sort((aL, bL) => aL.name.localeCompare(bL.name)).map((item) => { return item.name }).join('')

            if (aLabelsSorted == bLabelsSorted) {
                result = a.name.localeCompare(b.name);
                if (result == 0) {
                    result = a.lastName.localeCompare(b.lastName);
                }
                return result;
            }
            if (aLabelsSorted == '') return 1
            if (bLabelsSorted == '') return -1
            result = aLabelsSorted.localeCompare(bLabelsSorted)
            return result;
        }
        else {
            result = a.name.localeCompare(b.name);
            if (result == 0) {
                result = a.lastName.localeCompare(b.lastName);
            }
            return result;
        }

    }

    const convertToCSS = (colorArray) => {
        return `rgb(${colorArray[0]}, ${colorArray[1]}, ${colorArray[2]})`;
    }

    return (
        <>
            {contacts.sort(compareFN).map((item) => {

                const sortedLabels = item.labels.sort((a, b) => a.name.localeCompare(b.name))
                const color = (sortedLabels.length > 0) ? sortedLabels[0].colorRGB.split(", ", 3) : [255, 255, 255];
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