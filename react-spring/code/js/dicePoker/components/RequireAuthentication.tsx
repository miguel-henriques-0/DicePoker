import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router';
import {useAuthentication} from "../context/authentication";

type RequireAuthenticationProps = {
    children: ReactNode,
}
export function RequireAuthentication({ children }: RequireAuthenticationProps) {
    const [username] = useAuthentication(); // from our own context
    const location = useLocation(); // from React Router
    if (username) {
        return children;
    } else {
        return <Navigate to="/login" state={{ source: location.pathname }} replace={true} />;
    }
}