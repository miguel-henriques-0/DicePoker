/*
 * Some functions to *simulate* an authentication remote API.
 */
import {fetchHelper, RequestParams} from "./helpers/fetchHelper";

type inviteCodeType = string | undefined;

export async function authenticate(username: string, password: string, inviteCode: inviteCodeType): Promise<boolean> {

    if(inviteCode != undefined) {
        // Registration flow
        const reqParams: RequestParams = {
            url: "/api/users",
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: {
                username: username,
                password: password,
                inviteCode: inviteCode
            }
        };
        try {
            await fetchHelper(reqParams);
            return true;
        } catch (error) {
            console.error("Registration failed:", error);
            return false;
        }
    }

    // Login flow
    const reqParams: RequestParams = {
        url: "/api/users/token",
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: {
            username: username,
            password: password
        }
    };
    try {
        await fetchHelper(reqParams);
        return true;
    } catch (error) {
        console.error("Authentication failed:", error);
        return false;
    }

}