import './LoginSignup.css'
import { useState } from "react";

function LoginSignup() {
    const [state, setState] = useState("Log In");

    return (
        <>
            <div className="login-form-container">
                <h1 className="login-form-header">{state}</h1>
                <form action="">
                    {(state == "Sign Up") ? <>
                        <input type="text" placeholder='First Name' />
                        <input type="text" placeholder='Last Name' />
                    </> : <></>
                    }
                    <input type="email" placeholder='Email' />
                    <input type="password" placeholder='Password' />
                </form>
                <div className='switch-text'>Already have an account? <a onClick={
                    () => setState(
                        (state == "Sign Up") ? "Log in" : "Sign Up"
                    )}>Click here to {(state == "Sign Up") ? "log in" : "sign up"}.</a></div>
                <div className="submit-btn">{state}</div>
            </div>
        </>
    )
}

export default LoginSignup;