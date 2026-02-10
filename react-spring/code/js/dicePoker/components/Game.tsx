import {Link, useNavigate, useParams} from "react-router";
import {useContext, useEffect, useReducer, useState} from "react";
import {fetchHelper, RequestParams} from "../helpers/fetchHelper";
import { createSSEConnection } from "../helpers/sseHelper";
import {Game as GameType} from "../domain/Game";
import {AuthenticationContext} from "../context/authentication";
import {DiceDisplay} from "./DiceDisplay";
import {TurnPoints} from "./TurnPoints";

type GameViewState =
    | { type: "WaitingForGameStart"; game: GameType }
    | { type: "InGame"; game: GameType }
    | { type: "GameOver"; game: GameType }
    | { type: "TurnTimedOut"; game: GameType }
    | { type: "RoundComplete"; game: GameType }
    | { type: "WaitingForAnte"; game: GameType }
    | { type: "Error"; error: string }
    | { type: "Loading" }
    | { type: "Leaving" };

type GameAction =
    | { type: "gameState"; game: GameType }
    | { type: "errorState"; error: string }
    | { type: "loadingState" }
    | { type: "leavingState" };

function gameReducer(state: GameViewState, action: GameAction): GameViewState {
    switch (action.type) {
        case "loadingState":
            return { type: "Loading" };
        case "leavingState":
            return { type: "Leaving" };
        case "errorState":
            return { type: "Error", error: action.error };
        case "gameState":
            const game = action.game;
            // console.log("The gamestate is:", game);
            // console.log("Game STATE Type:", game.gameStateType);
            switch (game.gameStateType) {
                case "InLobby":
                    return { type: "WaitingForGameStart", game };
                case "WaitingForPlayerAction":
                    return { type: "InGame", game };
                case "TurnTimedOut":
                    return { type: "TurnTimedOut", game };
                case "GameOver":
                    return { type: "GameOver", game };
                case "RoundComplete":
                    return { type: "RoundComplete", game };
                case "WaitingForAnte":
                    return { type: "WaitingForAnte", game };
                default:
                    return { type: "Error", error: `Unknown game state: ${game.gameStateType}` };
            }
        default:
            return state;
    }
}

export function Game() {
    const params = useParams();
    const navigate = useNavigate();
    const [viewState, dispatch] = useReducer(gameReducer, { type: "Loading" });
    const [betAmount, setBetAmount] = useState<number>(1);
    const [betError, setBetError] = useState<string | null>(null);
    const loggedUser = useContext(AuthenticationContext)

    async function fetchGame() {
        dispatch({ type: "loadingState" });
        const response = await fetchHelper({ url: `/api/game/${params.id}`, method: "GET" });
        if (response) {
            if (response.title) {
                dispatch({type: "errorState", error: response.title});
                return;
            }
            dispatch({ type: "gameState", game: response as GameType });
        } else {
            dispatch({ type: "errorState", error: "Failed to fetch game" });
        }
    }

    // async function listen() {
    //     const res = await fetchHelper({
    //         url: `/api/events/listen/${params.id}`,
    //         method: "GET",
    //     }).then(_ =>
    //     console.log("Listening", res))
    // }

    useEffect(() => {
        fetchGame()

        const eventSource = createSSEConnection({
            url: `/api/events/listen/${params.id}`,
            onMessage: (game) => dispatch({ type: "gameState", game }),
            onError: () => dispatch({ type: "errorState", error: "SSE connection failed" }),
        });

        return () => {
            eventSource.close();
        };
    }, []);

    async function leaveGame() {
        dispatch({ type: "leavingState" });
        try {
            await fetchHelper({ url: `/api/game/${params.id}/leave`, method: 'PUT' });
            navigate('/');
        } catch (error) {
            dispatch({ type: "errorState", error: "Failed to leave game" });
        }
    }

    async function joinGame() {
        dispatch({ type: "loadingState" });
        try {
            const game = await fetchHelper({ url: `/api/game/${params.id}/join`, method: 'PUT' });
            if (game) {
                if (game.title) {
                    dispatch({type: "errorState", error: game.title});
                    return;
                }
                dispatch({ type: "gameState", game: game as GameType });
            }
        } catch (error) {
            dispatch({ type: "errorState", error: "Failed to join game" });
        }
    }

    async function startGame() {
        dispatch({ type: "loadingState" });
        try {
            const game = await fetchHelper({ url: `/api/game/${params.id}/start`, method: 'PUT' });
            console.log("GAME", game);
            if (game) {
                if (game.title) {
                    dispatch({type: "errorState", error: game.title});
                    return;
                }
                dispatch({ type: "gameState", game: game as GameType });
            }
        } catch (error) {
            dispatch({ type: "errorState", error: "Failed to start game" });
        }
    }

    async function placeBet(ante: number) {
        setBetError(null);

        if (viewState.type === "WaitingForAnte") {
            const currentPlayer = viewState.game.gameState.playerList.find(
                p => p.userId === loggedUser.userId
            );
            const playerBalance = currentPlayer?.balance ?? 0;

            if (ante <= 0) {
                setBetError("Bet must be greater than 0");
                return;
            }
            if (ante > playerBalance) {
                setBetError(`Insufficient balance. You have ${playerBalance} chips.`);
                return;
            }
        }

        try {
            const game = await fetchHelper({
                url: `/api/game/${params.id}/play`,
                method: 'PUT',
                body: {
                    playAction: "BET",
                    bet: ante,
                }
            });
            if (game) {
                if (game.title) {
                    dispatch({type: "errorState", error: game.title});
                    return;
                }
                dispatch({ type: "gameState", game: game as GameType });
            }
        } catch (error) {
            dispatch({ type: "errorState", error: "Failed to place bet" });
        }
    }

    async function rollDices(diceIndexes: number[]) {
        try {
            const game = await fetchHelper({
                url: `/api/game/${params.id}/play`,
                method: 'PUT',
                body: {
                    playAction: "ROLL",
                    dices: diceIndexes
                }
            });
            if (game) {
                if (game.title) {
                    dispatch({type: "errorState", error: game.title});
                    return;
                }
                dispatch({ type: "gameState", game: game as GameType });
            }
        } catch (error) {
            dispatch({ type: "errorState", error: "Failed to roll dices" });
        }
    }

    async function endTurn() {
        try {
            const game = await fetchHelper({
                url: `/api/game/${params.id}/play`,
                method: 'PUT',
                body: {
                    playAction: "END",
                }
            });
            if (game){
                if (game.title) {
                    dispatch({type: "errorState", error: game.title});
                    return;
                }
                dispatch({ type: "gameState", game: game as GameType });
            }
        } catch (error) {
            dispatch({ type: "errorState", error: "Failed to end turn" });
        }
    }

    async function nextRound() {
        try {
           const game = await fetchHelper({
                url: `/api/game/${params.id}/nextRound`,
                method: 'PUT',
            })
            if (game) {
                if (game.title) {
                    dispatch({type: "errorState", error: game.title});
                    return;
                }
                dispatch({type: "gameState", game: game as GameType});
            }
        } catch (error) {
            dispatch({ type: "errorState", error: "Failed to next round" });
        }
    }

    async function getNextGameState() {
        try {
            const game = await fetchHelper({
                url: `/api/game/${params.id}`,
                method: "GET",
            });
            if (game) {
                if (game.title) {
                    dispatch({type: "errorState", error: game.title});
                    return;
                }
                dispatch({ type: "gameState", game: game as GameType});
            }
        } catch (error) {
            console.log("Failed to get next game state:", error);
            dispatch({ type: "errorState", error: `Failed to get next game state: ${error}` });
        }
    }

    function whenError() {
        fetchGame();
    }

    function renderContent() {
        switch (viewState.type) {
            case "Loading":
                return <p>Loading...</p>;
            case "Leaving":
                return <p>Leaving...</p>;
            case "Error":
                return (
                    <div>
                        <p>Error: {viewState.error}</p>
                        <button onClick={whenError}>Retry</button>
                    </div>
                );
            case "WaitingForGameStart":
                const isAlreadyInGame = viewState.game.gameState.playerList.some(
                    p => p.userId === loggedUser.userId
                );
                return (
                    <div>
                        <p>Waiting for game to start...</p>
                        <p>Players: {viewState.game.gameState.playerList.length}/{viewState.game.maxPlayers}</p>
                        <button onClick={startGame}>Start Game</button>
                        <button
                            disabled={isAlreadyInGame}
                            onClick={joinGame}
                        >
                            Join
                        </button>
                    </div>
                );
            case "WaitingForAnte":
                const player = viewState.game.gameState.playerList.find(
                    p => p.userId === loggedUser.userId
                );
                const balance = player?.balance ?? 0;

                return (
                    <div>
                        <p>Place your ante</p>
                        <p>Your balance: {balance}</p>
                        <input
                            type="number"
                            min={1}
                            max={balance}
                            defaultValue={1}
                            onChange={ event => {
                                setBetAmount(Number(event.target.value));
                                setBetError(null);
                            }}
                        />
                        {betError && <p style={{ color: 'red' }}>{betError}</p>}
                        <button onClick={() => placeBet(betAmount)}>Bet</button>
                    </div>
                );
            case "InGame":
                // console.log("Game in this state", viewState.game);
                const isCurrentPlayer = viewState.game.gameState.currentPlayerId === loggedUser.userId;
                const rollsLeft = viewState.game.gameState.rollsForTurn;

                const currentPlayer = viewState.game.gameState.currentPlayerId;
                const playerHand = viewState.game.gameState.playerHands[currentPlayer];
                const dices = playerHand?.dices || [];

                const turnTime = viewState.game.gameState.turnTimer;
                return (
                    <div>
                        <p>Game in progress - Round {viewState.game.gameState.currentRound}</p>
                        <p>Rolls Left: {rollsLeft}</p>
                        <div> { turnTime ?
                            <p>Time left for turn: {turnTime} seconds</p>
                            : undefined
                        }
                        </div>
                        <TurnPoints
                            hand={playerHand}
                            points={playerHand.points}
                        />
                        <DiceDisplay
                            dices={dices}
                            isCurrentPlayer={isCurrentPlayer}
                            rollsLeft={rollsLeft}
                            onRoll={rollDices}
                            onEndTurn={endTurn}
                        />
                    </div>
                );
            case "RoundComplete":
                const winnerPlayer = viewState.game.gameState.roundWinner
                const winner = winnerPlayer[0].userId;

                return (
                    <div>
                        <p>Round complete!</p>
                        <p>
                            {
                                winner === loggedUser.userId
                                    ? "You won this round!"
                                    : `Player ${winner} won this round!`
                            }
                            <p>
                                Winning Hand: {viewState.game.gameState.playerHands[winner].handRank} with {viewState.game.gameState.playerHands[winner].points} points
                            </p>
                        </p>
                        <button
                            onClick={nextRound}
                        >
                            Next Round
                        </button>
                    </div>

            );
            case "GameOver":
                return (
                    <><p>Game Over!</p>
                        <p>
                        Winners summary:
                        {viewState.game.gameState.winners.map(winner => (
                            <div key={winner.userId}>
                                {winner.userId} with balance {winner.balance}
                            </div>
                        ))}
                    </p></>
            )
            case "TurnTimedOut":
                return (
                    <div>
                        <p>Turn timed out</p>
                        <button
                            onClick={getNextGameState}
                        >
                            Continue
                        </button>
                    </div>
                );
        }
    }

    return (
        <div>
            GAME {params.id}
            {renderContent()}
            <div>
                <button onClick={leaveGame} disabled={viewState.type === "Leaving"}>
                    {viewState.type === "Leaving" ? 'Leaving...' : 'Leave'}
                </button>
                {/*<Link to={'/'}><button>Back</button></Link>*/}
            </div>
        </div>
    );
}


// leave as is, is too fast and the game is still being loaded on the list - Fixed
