import './LoginSignup.css'
import { useState } from "react";
import axios from '../../API/axios'

const REGISTER_URL = '/api/v1/auth/register'
const AUTH_URL = '/api/v1/auth/authenticate'

function LoginSignup() {
    const [state, setState] = useState("Log In");

    const [firstName, setFN] = useState('');
    const [lastName, setLN] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPwd] = useState('');

    const [errMsg, setErrMsg] = useState('');
    const [success, setSuccess] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (state == "Sign Up") {
                const response = await axios.post(REGISTER_URL,
                    JSON.stringify({ firstName, lastName, email, password }),
                    {
                        headers: { 'Content-Type': 'application/json' },
                    }
                )
                setSuccess(true);
                alert(JSON.stringify(response.data));
            }
            else {
                const response = await axios.post(AUTH_URL,
                    JSON.stringify({ email, password }),
                    {
                        headers: { 'Content-Type': 'application/json' },
                    }
                )
                setSuccess(true);
                alert(JSON.stringify(response));
            }
        } catch (err) {
            setErrMsg(err.response.data.message);
        }

    }

    return (
        <>
            {success ? (
                <p>Success!</p>
            ) : (
                <div className="login-form-container">
                    <h1 className="login-form-header">{state}</h1>
                    {errMsg == "" ? <></> : <p>{errMsg}</p>}
                    <form onSubmit={handleSubmit}>
                        {(state == "Sign Up") ? <>
                            <input type="text"
                                placeholder='First Name'
                                id="firstName"
                                required
                                onChange={(e) => setFN(e.target.value)}

                            />
                            <input
                                type="text"
                                placeholder='Last Name'
                                id="lastName"
                                onChange={(e) => setLN(e.target.value)}
                            />
                        </> : <></>
                        }
                        <input type="email"
                            placeholder='Email'
                            id="email"
                            required
                            onChange={(e) => setEmail(e.target.value)}
                        />
                        <input
                            type="password"
                            placeholder='Password'
                            id="password"
                            required
                            onChange={(e) => setPwd(e.target.value)}
                        />

                        <div className='switch-text'>
                            {(state == "Sign Up") ? "Don't" : "Already"} have an account? <a
                                tabIndex={0}
                                onClick={
                                    () => setState(
                                        (state == "Sign Up") ? "Log in" : "Sign Up"
                                    )}>
                                Click here to {(state == "Sign Up") ? "log in" : "sign up"}.
                            </a>
                        </div>
                        <button
                            className="submit-btn"
                            tabIndex={0}
                            type='submit'>
                            {state}
                        </button>
                    </form>
                </div>
            )}
        </>
    )
}

export default LoginSignup;