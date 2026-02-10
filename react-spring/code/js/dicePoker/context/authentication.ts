import { useContext } from 'react';
import { createContext } from 'react';

export type AuthenticationState = {
    /** read the currently authenticated user, or `undefined` if not authenticated user exists */
    username: string | undefined;
    userId: number | undefined;
    /** set the currently authenticated user or logout (set `undefined`) */
    setUsername: (username: string | undefined) => void;
    setUserId: (userId: number | undefined) => void;
};

export const AuthenticationContext = createContext<AuthenticationState>({
    // username: undefined, setUsername: () => {}
    username: undefined, setUsername: () => {},
    userId: undefined, setUserId: () => {},
});

/**
 * Custom hook to access the authentication state
 * @returns a pair with the currently authentication user and a function to set it
 */
export function useAuthentication(): [
    string | undefined, (value: string | undefined) => void,
    number | undefined, (value: number | undefined) => void,
] {
    const state: AuthenticationState = useContext(AuthenticationContext);
    return [
        state.username,
        state.setUsername,
        state.userId,
        state.setUserId
    ]
}