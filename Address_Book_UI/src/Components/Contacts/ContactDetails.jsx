import { useContext } from "react";
import ContactsContext from "../../Context/ContactsProvider";
import { Link, useParams, Navigate } from "react-router-dom";
import edit from "./edit100.png";
import ContactInfoField from "./ContactInfoField";

function ContactDetails() {
    const { contacts } = useContext(ContactsContext);
    const { id } = useParams();
    const contact = contacts.find(contact => (contact.id).toString() === id);

    return (
        <div className="main-container">
            <div className="contact-info-header-container">
                <h2 className='main-header-text'>
                    Contact Details
                </h2>
                <Link to="edit" >
                    <button className='small-button edit-button'>
                        <img src={edit}
                            alt="Edit Contact" />
                    </button>
                </Link>
            </div>
            <hr style={{ marginTop: "15px" }} />

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
                        labelContents={(contact?.label) ? contact.label.name : "None"}
                    />
                    {/* Comment */}
                    {(contact?.comment) ?
                        <div className="comment">
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