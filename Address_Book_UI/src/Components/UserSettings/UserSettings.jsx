import { useContext, useState } from "react"
import UserContext from "../../Context/UserProvider"

function UserSettings() {
    const { firstName, lastName, email, changeUsernames, changePassword, changeEmail, deleteUser, setErrMsg, errMsg, setSuccess, success } = useContext(UserContext)

    const [firstNameTemp, setFirstNameTemp] = useState(firstName)
    const [lastNameTemp, setLastNameTemp] = useState(lastName)
    const [emailTemp, setEmailTemp] = useState(email)
    const [password, setPassword] = useState("")
    const [passwordConfirm, setPasswordConfirm] = useState("")

    const [deletePrepState, setDeletePrepState] = useState(false);
    const [deleteConfirm, setDeleteConfirm] = useState("")

    const handlePasswordChange = () => {
        setSuccess(false);
        if (password == passwordConfirm) {
            changePassword(password);
        }
        else {
            setErrMsg('Passwords don\'t match.');
        }
    }

    const handleDelete = () => {
        setSuccess(false);
        if (deleteConfirm == "DELETEME!") {
            deleteUser();
        }
        else {
            setErrMsg('Delete confirmation code doesn\'t match.');
        }
    }

    return (
        <div className="main-container">
            <h2 className='main-header-text'>
                User Details
            </h2>
            <hr style={{ marginTop: "20px" }} />
            {errMsg == "" ? <></> : <p
                style={success ? { marginTop: "10px" } : { color: "red", marginTop: "10px" }}>{errMsg}</p>}
            <form className="user-settings-form" onSubmit={(e) => e.preventDefault()}>
                <h3>Change User Names</h3>
                <div>
                    <input type="text"
                        id="firstName"
                        defaultValue={firstName}
                        placeholder="First Name"
                        onChange={(e) => setFirstNameTemp(e.target.value)}
                        onFocus={() => setErrMsg('')}
                    />
                    <input type="text"
                        id="lastName"
                        defaultValue={lastName}
                        placeholder="Last Name"
                        style={{ marginLeft: "10px" }}
                        onChange={(e) => setLastNameTemp(e.target.value)}
                        onFocus={() => setErrMsg('')}
                    />
                    {(firstName != firstNameTemp || lastName != lastNameTemp) &&
                        <button
                            className="small-button"
                            style={{ backgroundColor: "rgb(191, 244, 174)" }}
                            type="button"
                            onClick={() => changeUsernames(firstNameTemp, lastNameTemp)}
                        >
                            ✓
                        </button>}
                </div>
            </form >
            <form className="user-settings-form" onSubmit={(e) => e.preventDefault()}>
                <h3>Change Email</h3>
                <div>
                    <input type="text"
                        id="email"
                        defaultValue={email}
                        placeholder="Email"
                        onChange={(e) => setEmailTemp(e.target.value)}
                        onFocus={() => setErrMsg('')}
                    />
                    {(email != emailTemp) &&
                        <button
                            className="small-button"
                            style={{ backgroundColor: "rgb(191, 244, 174)" }}
                            type="button"
                            onClick={() => changeEmail(emailTemp)}
                        >
                            ✓
                        </button>}
                </div>
            </form >
            <form className="user-settings-form" onSubmit={(e) => e.preventDefault()}>
                <h3>Change Password</h3>
                <div>
                    <input type="password"
                        id="password"
                        placeholder="Password"
                        onChange={(e) => setPassword(e.target.value)}
                        onFocus={() => setErrMsg('')
                        }
                    />
                </div>
                {(password) && <div style={{ marginTop: "10px" }}>
                    <input type="password"
                        id="passwordConfirm"
                        placeholder="Confirm Password"
                        onChange={(e) => setPasswordConfirm(e.target.value)}
                        onFocus={() => setErrMsg('')}
                    />

                    <button
                        className="small-button"
                        style={{ backgroundColor: "rgb(191, 244, 174)" }}
                        type="button"
                        onClick={() => handlePasswordChange()}
                    >
                        ✓
                    </button>
                </div>}
            </form >
            <form className="user-settings-form" onSubmit={(e) => e.preventDefault()}>
                <h3>Delete User</h3>
                {(!deletePrepState) ?
                    <button
                        className="delete-user-button"
                        type="button"
                        onClick={() => setDeletePrepState(true)}>
                        Delete User
                    </button>
                    : <>
                        <h3>Warning! This action is permanent.</h3><p> Are you sure you want to delete this user?</p>
                        <p>To confirm this action, please enter <em>DELETEME!</em> in the form below.</p>
                        <div style={{ margin: "10px 0 30px 0" }}>
                            <input type="text"
                                id="deleteConfirm"
                                placeholder="Confirm Deletion"
                                onChange={(e) => setDeleteConfirm(e.target.value)}
                                onFocus={() => setErrMsg('')}
                            />

                            <button
                                className="small-button"
                                style={{ backgroundColor: "rgb(244, 191, 174)" }}
                                type="button"
                                onClick={() => handleDelete()}
                            >
                                ✓
                            </button>
                        </div>
                    </>}
            </form >
        </div >

    )
}

export default UserSettings