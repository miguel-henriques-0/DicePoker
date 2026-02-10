import { useState, type ReactNode } from "react";
import { AuthenticationContext, type AuthenticationState } from "./authentication";

type AuthenticationProviderProp = {
    children: ReactNode,
}
export function AuthenticationProvider({ children }: AuthenticationProviderProp) {
    const [observedUsername, setUsername] = useState<string | undefined>(undefined);
    const [observedUserId, setUserId] = useState<number | undefined>(undefined);
    const value: AuthenticationState  = {
        username: observedUsername,
        userId: observedUserId,
        setUserId: (userId: number) => setUserId(userId),
        setUsername: (username) => setUsername(username),
    };
    return <AuthenticationContext.Provider value={value}>{children}</AuthenticationContext.Provider>;
}