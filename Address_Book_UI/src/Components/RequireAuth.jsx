import { useLocation, Navigate, Outlet } from "react-router-dom";
import AuthContext from "../Context/AuthProvider";
import { useContext } from "react";

const RequireAuth = () => {
    const { auth } = useContext(AuthContext);
    const location = useLocation();

    return (
        auth
            ? <Outlet />
            : <Navigate to="/login" state={{ from: location }} replace />
    );
}

export default RequireAuth;