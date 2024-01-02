import { useContext } from "react";
import ContactsContext from "../../Context/ContactsProvider";
import { Link, useParams, Navigate } from "react-router-dom";
import edit from "../../Icons/edit.webp";
import ContactInfoField from "./ContactInfoField";
import LabelContext from "../../Context/LabelProvider";

function ContactDetails() {
    const { contacts } = useContext(ContactsContext);
    const { labelsToString } = useContext(LabelContext)
    const { id } = useParams();
    const contact = contacts.find(contact => (contact.id).toString() === id);

    return (
        <div className="main-container">
            <div className="main-header-container"
                style={{ backgroundColor: `rgb(${contact?.label?.colorRGB})`, paddingBottom: "15px" }}>
                <h2 className='main-header-text'>
                    Contact Details
                </h2>
                <Link to="edit" >
                    <button className='small-button'
                        title="Edit Contact">
                        <img src={edit}
                            loading="eager"
                            alt="Edit Contact" />
                    </button>
                </Link>
            </div>
            <hr />

            {(!contact) ? <Navigate to=".." replace={true} /> : <>
                <div className="contact-info-field-container">
                    <ContactInfoField
                        labelName="First Name"
                        labelContents={contact?.name}
                        isHidden={contact?.name}
                    />
                    <ContactInfoField
                        labelName="Last Name"
                        labelContents={contact?.lastName}
                        isHidden={contact?.lastName}
                    />
                    <ContactInfoField
                        labelName="Phone Number"
                        labelContents={<a href={"tel:" + contact?.phoneNumber}>{contact?.phoneNumber}</a>}
                        isHidden={contact?.phoneNumber}
                    />
                    <ContactInfoField
                        labelName="Company"
                        labelContents={contact?.nameOfCompany}
                        isHidden={contact?.nameOfCompany}
                    />
                    <ContactInfoField
                        labelName="Address"
                        labelContents={contact?.address}
                        isHidden={contact?.address}
                    />
                    <ContactInfoField
                        labelName="Email"
                        labelContents={<a href={"mailto:" + contact?.email}>{contact?.email}</a>}
                        isHidden={contact?.email}
                    />
                    <ContactInfoField
                        labelName="Fax"
                        labelContents={contact?.fax}
                        isHidden={contact?.fax}
                    />
                    <ContactInfoField
                        labelName="Mobile Number"
                        labelContents={<a href={"tel:" + contact?.mobileNumber}>{contact?.mobileNumber}</a>}
                        isHidden={contact?.mobileNumber}
                    />

                    {/* Custom Rows */}
                    {contact.customRows?.map((row) => (
                        <ContactInfoField
                            labelName={row.customName}
                            labelContents={row.customField}
                            key={row.id}
                        />))}

                    {/* Label */}
                    <ContactInfoField
                        labelName="Label"
                        labelContents={(contact?.labels.length > 0) ? labelsToString(contact?.labels) : "None"}
                    />
                    {/* Comment */}
                    {(contact?.comment) ?
                        <div className="one-line-field">
                            <ContactInfoField
                                labelName="Comment"
                                labelContents={contact?.comment}
                            />
                        </div> : <></>}
                </div>


            </>}
        </div>
    )
}

export default ContactDetails