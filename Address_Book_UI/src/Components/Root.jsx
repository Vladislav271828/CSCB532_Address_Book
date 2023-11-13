import { Link } from "react-router-dom"

function Root() {
    return (
        <div>
            <p>The best homepage you've ever seen!!!</p>
            <Link to="/signup">
                Click me to sign up.
            </Link>
            <br />
            <Link to="/login">
                Click me to log in.
            </Link>
        </div>
    )
}

export default Root