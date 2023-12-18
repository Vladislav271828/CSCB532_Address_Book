import './LoginSignup.css'
import { useState, useContext } from "react";
import axios from '../../API/axios'
import { Link, useNavigate } from "react-router-dom"
import AuthContext from "../../Context/AuthProvider";

const AUTH_URL = '/auth/authenticate'

function Login() {
    const [email, setEmail] = useState('');
    const [password, setPwd] = useState('');

    const [errMsg, setErrMsg] = useState('');
    // const [success, setSuccess] = useState(false);

    const { setAuth, auth } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post(AUTH_URL,
                JSON.stringify({ email, password }),
                {
                    headers: { 'Content-Type': 'application/json' },
                }
            )
            setEmail('');
            setPwd('');
            setAuth(response.data.token);
            navigate("/");
        } catch (err) {
            if (!err?.response) {
                setErrMsg('Unable to connect to server.');
            }
            else {
                setErrMsg('Invalid email or password.');
            }
        }
    }

    return (
        <>
            <div className="login-form-container">
                <h1 className="login-form-header">Log In</h1>
                {errMsg == "" ? <></> : <p className='login-form-text' style={{ color: "red" }}>{errMsg}</p>}
                <form onSubmit={handleSubmit}>
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
                        Already have an account? <Link
                            to="/signup"
                            tabIndex={0}>
                            <span>Click here to sign up.</span>
                        </Link>
                    </p>
                    <button
                        className="big-btn"
                        tabIndex={0}
                        type='submit'>
                        Log In
                    </button>
                </form>
            </div>
        </>
    )
}

export default Login;