
type ErrorHandlerProps = {
    response: Response,
    setLobbyError: (error: string) => void,
}

export async function ErrorHandler({response, setLobbyError}: ErrorHandlerProps) {
    const contentType = (response.headers.get("content-type") || "").toLowerCase();

    if (contentType.includes("application/problem+json")) {
        const problem = await response.json();
        const title = problem.title || `Error ${response.status}`;
        setLobbyError(`${title}`);
        return;
    }
}