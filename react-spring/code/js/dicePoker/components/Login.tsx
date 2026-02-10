// @ts-ignore
import styles from '../css/login.module.css';
import {use, useReducer} from "react";
import { Navigate, useLocation } from "react-router";
import {useAuthentication} from "../context/authentication";
import {authenticate} from "../authenticate";
import {fetchHelper} from "../helpers/fetchHelper";

type Mode = "login" | "register";

type State =
    | {
    tag: "editing";
    mode: Mode;
    error?: string;
    inputs: { username: string; password: string; inviteCode: string };
}
    | { tag: "submitting"; username: string; mode: Mode }
    | { tag: "redirect" };

type Action =
    | { type: "edit"; inputName: string; inputValue: string }
    | { type: "submit" }
    | { type: "error"; message: string }
    | { type: "success" }
    | { type: "setMode"; mode: Mode };

function logUnexpectedAction(state: State, action: Action) {
    console.log(`Unexpected action '${(action as any).type}' on state '${state.tag}'`);
}

function reduce(state: State, action: Action): State {
    switch (state.tag) {
        case "editing":
            if (action.type === "edit") {
                return {
                    tag: "editing",
                    mode: state.mode,
                    error: undefined,
                    inputs: { ...state.inputs, [action.inputName]: action.inputValue },
                };
            } else if (action.type === "submit") {
                return { tag: "submitting", username: state.inputs.username, mode: state.mode };
            } else if (action.type === "setMode") {
                return {
                    tag: "editing",
                    mode: action.mode,
                    error: undefined,
                    inputs: { ...state.inputs, password: "" },
                };
            } else {
                logUnexpectedAction(state, action);
                return state;
            }

        case "submitting":
            if (action.type === "success") {
                return { tag: "redirect" };
            } else if (action.type === "error") {
                return {
                    tag: "editing",
                    mode: state.mode,
                    error: action.message,
                    inputs: { username: state.username, password: "", inviteCode: "" },
                };
            } else {
                logUnexpectedAction(state, action);
                return state;
            }

        case "redirect":
            logUnexpectedAction(state, action);
            return state;
    }
}

export function Login() {
    const [state, dispatch] = useReducer(reduce, {
        tag: "editing",
        mode: "login" as Mode,
        inputs: { username: "", password: "", inviteCode: "" },
    });
    const [, setUsername, , setUserId] = useAuthentication(); // Single call, skip values you don't need
    const location = useLocation();

    if (state.tag === "redirect") {
        return <Navigate to={location.state?.source || "/"} replace={true} />;
    }

    function handleChange(ev: React.FormEvent<HTMLInputElement>) {
        dispatch({
            type: "edit",
            inputName: ev.currentTarget.name,
            inputValue: ev.currentTarget.value,
        });
    }

    function toggleMode() {
        const next: Mode = state.tag === "editing" && state.mode === "login" ? "register" : "login";
        dispatch({ type: "setMode", mode: next });
    }

    async function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault();
        if (state.tag !== "editing") {
            return;
        }
        dispatch({ type: "submit" });

        const username = state.inputs.username;
        const password = state.inputs.password;
        const currentMode: Mode = state.mode;
        const inviteCodeValue: string = state.inputs.inviteCode;

        try {
            const result = await authenticate(
                username,
                password,
                currentMode === "register" ? inviteCodeValue : undefined
            );
            if (!result) {
                dispatch({ type: "error", message: "invalid username or password" });
                return;
            }
            const user = await fetchHelper({
                url: '/api/me',
                method: 'GET',
            })
            if(!user){
                dispatch({type: "error", message: "an error occurred try again" });
                return;
            }
            setUsername(user.username);
            setUserId(user.id);
            dispatch({ type: "success" });
        } catch (error) {
            dispatch({
                type: "error",
                message: `operation could not be completed - ${error}`,
            });
        }
    }

    const username =
        state.tag === "submitting" ? state.username : state.tag === "editing" ? state.inputs.username : "";
    const password = state.tag === "submitting" ? "" : state.tag === "editing" ? state.inputs.password : "";
    const mode: Mode = state.tag === "editing" || state.tag === "submitting" ? state.mode : "login";
    const inviteCodeValue = state.tag === "editing" ? state.inputs.inviteCode : "";
    const validUsernameAndPassword = state.tag === "editing" && state.inputs.username.length > 0 && state.inputs.password.length > 0;

    console.log(validUsernameAndPassword)

    return (
        <form onSubmit={handleSubmit} className={styles.loginForm}>
            <fieldset className={styles.loginCard} disabled={state.tag !== "editing"}>

                {/* Username Input Group */}
                <div className={styles.inputGroup}>
                    <label htmlFor="username">Username</label>
                    <input id="username" type="text" name="username" value={username} onChange={handleChange} />
                </div>

                {/* Password Input Group */}
                <div className={styles.inputGroup}>
                    <label htmlFor="password">Password</label>
                    <input id="password" type="password" name="password" value={password} onChange={handleChange} />
                </div>

                {/* Invite Code Input Group (Conditional) */}
                {mode === "register" && (
                    <div className={styles.inputGroup}>
                        <label htmlFor="inviteCode">Invite Code</label>
                        <input
                            id="inviteCode"
                            type="text"
                            name="inviteCode"
                            value={inviteCodeValue}
                            onChange={handleChange}
                        />
                    </div>
                )}

                {/* Button Group */}
                <div className={styles.buttonGroup}>
                    <button className={styles.primaryButton} type="submit" disabled={state.tag === "submitting" && !validUsernameAndPassword}>
                        {mode === "register" ? "Register" : "Login"}
                    </button>
                    <button className={styles.secondaryButton} type="button" onClick={toggleMode}>
                        {mode === "register" ? "Switch to Login" : "Switch to Register"}
                    </button>
                </div>

            </fieldset>
            {/* Error Message */}
            {state.tag === "editing" && state.error && (
                <p className={styles.errorMessage}>{state.error}</p>
            )}
        </form>
    );
}