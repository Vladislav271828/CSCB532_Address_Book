import { Link } from "react-router-dom"

function FourOhFour() {
    return (
        <div style={{
            animation: "fade-in-up 1s",
            animationTimingFunction: "ease"
        }}>
            <h1 style={{
                color: "#464646",
                fontSize: "min(32vw, 150px)",
                lineHeight: "1.2"
            }}>
                404
            </h1>
            <p style={{
                maxWidth: "600px"
            }}>The page you are looking for is not available. <Link
                to="/" replace>
                    <span className="spanlink">Click here</span>
                </Link> to go back to the home page.</p>
        </div >
    )
}

export default FourOhFour