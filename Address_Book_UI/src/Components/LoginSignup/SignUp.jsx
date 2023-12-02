import './LoginSignup.css'
import { useState } from "react";
import axios from '../../API/axios'
import { Link } from "react-router-dom"

const REGISTER_URL = '/api/v1/auth/register'

function SignUp() {
    const [firstName, setFN] = useState('');
    const [lastName, setLN] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPwd] = useState('');

    const [errMsg, setErrMsg] = useState('');
    const [success, setSuccess] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post(REGISTER_URL,
                JSON.stringify({ firstName, lastName, email, password }),
                {
                    headers: { 'Content-Type': 'application/json' },
                }
            )
            setSuccess(true);
            setErrMsg('We have sent a verification link to ' + email);
            setEmail('');
            setFN('');
            setLN('');
            setPwd('');
        } catch (err) {
            setSuccess(false);
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
            }
            else {
                setErrMsg(err.response.data.message);
            }
        }

    }

    return (

        <div className="login-form-container">
            <h1 className="login-form-header">Sign Up</h1>
            {errMsg == "" ? <></> : <p
                className='login-form-text'
                style={success ? {} : { color: "red" }}>{errMsg}</p>}
            <form onSubmit={handleSubmit}>
                <input type="text"
                    placeholder='First Name'
                    id="firstName"
                    required
                    onChange={(e) => setFN(e.target.value)}
                    onFocus={() => setErrMsg('')}
                />
                <input
                    type="text"
                    placeholder='Last Name'
                    id="lastName"
                    onChange={(e) => setLN(e.target.value)}
                    onFocus={() => setErrMsg('')}
                />
                <input type="email"
                    placeholder='Email'
                    id="email"
                    required
                    onChange={(e) => setEmail(e.target.value)}
                    onFocus={() => setErrMsg('')}
                />
                <input
                    type="password"
                    placeholder='Password'
                    id="password"
                    required
                    onChange={(e) => setPwd(e.target.value)}
                    onFocus={() => setErrMsg('')}
                />

                <p className='login-form-text'>
                    Don't have an account? <Link
                        to="/login"
                        tabIndex={0}>
                        <span>Click here to log in.</span>
                    </Link>
                </p>
                <button
                    className="submit-btn"
                    tabIndex={0}
                    type='submit'>
                    Sign Up
                </button>
            </form>
        </div>
    )
}

export default SignUp;