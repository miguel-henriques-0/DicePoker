import {useState} from "react";
import {RequireAuthentication} from "./RequireAuthentication";
import {Link, useNavigate} from "react-router";
import {fetchHelper, RequestParams} from "../helpers/fetchHelper";
import {ErrorHandler} from "./ErrorHandler";

type CreateGameRequest = {
    name: string;
    description: string;
    rounds: number;
    maxPlayers: number;
    minPlayers: number;
    timeout: number;
}

export function CreateGame() {
    const [lobbyNameValue, setLobbyNameValue] = useState("");
    const [lobbyDescriptionValue, setLobbyDescriptionValue] = useState("");
    const [roundsValue, setRoundsValue] = useState(2);
    const [minPlayersValue, setMinPlayersValue] = useState(2);
    const [maxPlayersValue, setMaxPlayersValue] = useState(6);
    const [timeoutValue, setTimeoutValue] = useState(120);

    const [lobbyNameError, setLobbyNameError] = useState("");
    const [lobbyDescriptionError, setLobbyDescriptionError] = useState("");
    const [lobbyError, setLobbyError] = useState("");

    const navigate = useNavigate();
    let formValid = false;

    async function createGame() {

        const request: RequestParams = {
            url: "api/game/create",
            method: "POST",
            body: {
                "name": lobbyNameValue,
                "description": lobbyDescriptionValue,
                "rounds": roundsValue,
                "maxPlayers": maxPlayersValue,
                "minPlayers": minPlayersValue,
                "timeout": timeoutValue,
            },
            jsonResponse: false,
        }

        const response = await fetchHelper(request);

        if (response && response.ok) {
            const location = response.headers.get("Location");
            if (location) {
                const gameId = location.split("/").pop(); // get the game id from the location header
                console.log("Navigating to game id:", gameId);
                navigate(`/game/${gameId}`);
            }
        } else {
            await ErrorHandler({response: response!, setLobbyError});
        }

    }

    function handleSubmit() {
        console.log("Submitting form");
        createGame().then(
            () => {
                // console.log("Game created successfully");
            }
        )
    }

    return (
        <RequireAuthentication>
        <div>
            <h2>Create Lobby</h2>
            <form action="">
                <fieldset>
                    <div>
                        <label htmlFor="lobbyName">Lobby Name</label>
                        <input
                            id="lobbyName"
                            type="text"
                            name="lobbyName"
                            value={lobbyNameValue}
                            onChange={(e) => {
                                setLobbyNameValue(e.target.value)
                                if (e.target.value.trim() !== "") setLobbyNameError("");
                            }}
                            onBlur={() => {
                                if (lobbyNameValue.trim() === "") {
                                    setLobbyNameError("Lobby name cannot be empty");
                                }
                            }}
                        />
                        {lobbyNameError && <p style={{color: "red", margin: "4px 0 0 0"}}>{lobbyNameError}</p>}
                    </div>
                    <div>
                        <label htmlFor="lobbyDescription">Lobby Description</label>
                        <input
                            id="lobbyDescription"
                            type="text"
                            name="lobbyDescription"
                            value={lobbyDescriptionValue}
                            onChange={(e) => {
                                setLobbyDescriptionValue(e.target.value);
                                if (e.target.value.trim() !== "") setLobbyDescriptionError("");
                            }}
                            onBlur={() => {
                                if (lobbyDescriptionValue.trim() === "") {
                                    setLobbyDescriptionError("Lobby description cannot be empty");
                                }
                            }}
                        />
                        {lobbyDescriptionError && <p style={{color: "red", margin: "4px 0 0 0"}}>{lobbyDescriptionError}</p>}
                    </div>
                    <div>
                        <label htmlFor="rounds">Number of Rounds</label>
                        <input
                            id="rounds"
                            type="number"
                            name="rounds"
                            min={2}
                            max={60}
                            value={roundsValue}
                            onChange={(e) => {setRoundsValue(parseInt(e.target.value));}}
                        />
                    </div>
                    <div>
                        <label htmlFor="minPlayers">Minimum Players needed</label>
                        <input
                            id="minPlayers"
                            type="number"
                            name="minPlayers"
                            min={2}
                            max={6}
                            value={minPlayersValue}
                            onChange={(e) => {setMinPlayersValue(parseInt(e.target.value));}}
                        />
                    </div>
                    <div>
                        <label htmlFor="maxPlayers">Max Players allowed</label>
                        <input
                            id="maxPlayers"
                            type="number"
                            name="maxPlayers"
                            min={2}
                            max={6}
                            value={maxPlayersValue}
                            onChange={(e) => {setMaxPlayersValue(parseInt(e.target.value));}}
                        />
                    </div>
                    <div>
                        <label htmlFor="timeout">Game Autostart in:</label>
                        <input
                            id="timeout"
                            type="number"
                            name="timeout"
                            value={timeoutValue}
                            onChange={(e) => {setTimeoutValue(parseInt(e.target.value));}}
                        />
                    </div>
                    <div>
                        <button
                            type="submit"
                            onClick={(e) => {
                                e.preventDefault(); // blocks the default behavior that is reloading the page
                                let valid = true;

                                if (lobbyNameValue.trim() === "") {
                                    setLobbyNameError("Lobby name cannot be empty");
                                    valid = false;
                                } else {
                                    setLobbyNameError("");
                                }

                                if (lobbyDescriptionValue.trim() === "") {
                                    setLobbyDescriptionError("Lobby description cannot be empty");
                                    valid = false;
                                } else {
                                    setLobbyDescriptionError("");
                                }

                                if (valid) {
                                    handleSubmit();
                                }
                            }}
                        >Create Lobby</button>
                        <Link to="/">
                            <button>Cancel</button>
                        </Link>
                    </div>
                </fieldset>
            </form>
            <p>
                {
                    lobbyError && !(lobbyDescriptionError || lobbyNameError) ?
                    <span style={{color: "red"}}>{lobbyError}</span> :
                    ""
                }
            </p>
        </div>
        </RequireAuthentication>
    )
}


//https://stackoverflow.com/questions/37609049/how-to-correctly-catch-change-focusout-event-on-text-input-in-react-js
// onBlur -> If input loses focus and its empty alert