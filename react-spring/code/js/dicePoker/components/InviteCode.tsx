import { useAuthentication } from "../context/authentication";
import { useEffect, useState } from "react";
import { fetchHelper, RequestParams } from "../helpers/fetchHelper";

type inviteCodeResponse = {
    inviteCode: string
}

export function InviteCode() {
    const [username] = useAuthentication();
    const [inviteCode, setInviteCode] = useState<string | null>(null);

    async function getInviteCode() {
        if (!username) return;

        // {console.log(inviteCode)}

        const params: RequestParams = {
            url: "/api/users/createInvite",
            method: "GET",
        };

        try {
            const response: inviteCodeResponse = await fetchHelper(params);
            setInviteCode(response.inviteCode);
        } catch (err) {
            console.error("Failed to fetch invite code", err);
        }
    }

    useEffect(() => {
        getInviteCode();
    }, [username]);

    return (
        <div>
            <p>Invite Code: {inviteCode}</p>
            <button type="button" onClick={getInviteCode}>
                Generate Code
            </button>
        </div>
    );
}