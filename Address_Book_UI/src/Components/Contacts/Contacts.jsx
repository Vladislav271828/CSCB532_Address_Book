import { useNavigate } from "react-router-dom";
import { useEffect, useContext } from "react";
import AuthContext from "../../Context/AuthProvider";

function Contacts() {
    const { auth } = useContext(AuthContext);
    const navigate = useNavigate();

    useEffect(() => {
        if (auth == "")
            navigate("/login", { replace: "true" })
    }, [])

    return (
        <div>Contacts</div>
    )
}

export default Contacts