import './LoginSignup.css'

function LoginSignup() {
    return (
    <>
    <div className="login-form-container">
        <h1>MyAddressBook</h1>
        <div className="input">
            <input type="text" placeholder='First Name'/>
        </div>
        <div className="input">
            <input type="text" placeholder='Last Name'/>
        </div>
        <div className="input">
            <input type="email" placeholder='Email'/>
        </div>
        <div className="input">
            <input type="password" placeholder='Password'/>
        </div>
        <div className='switch-text'>Already have an account? <a>Click here to login.</a></div>
        <div className="submit-btn">Sign Up</div>
    </div>
    </>
    )
}

export default LoginSignup;