import {GameCard} from "./GameCard";
import {Link, useNavigate} from "react-router";
import {useState, useEffect} from "react";
import {fetchHelper, RequestParams} from "../helpers/fetchHelper";

type Game = {
    id: number;
    name: string;
    currentPlayers: number,
    maxPlayers: number;
    rounds: number;j
}

export function ListGame() {
    //Last game id for scrolling pagination when a number is passed theres more to load otherwise its null
    const [observedLastGameId, setLastGameId] = useState<number | null>(null);
    //Game list
    const [observedGameList, setGameList] = useState<Game[]>([]);
    // Loading flag
    const [isLoading, setIsLoading] = useState(false);
    // if more pages are available based on the last game id not being null
    const [hasMore, setHasMore] = useState(true);
    const navigate = useNavigate();


    // if more to load, fetch games passing the last id
    const loadMore = () => {
        if (hasMore && !isLoading && observedLastGameId) {
            fetchGames(observedLastGameId);
        }
    };

    // main fetchGames functions
    // its declared here since we cant call async on use effect see this
    //https://stackoverflow.com/questions/53332321/react-hook-warnings-for-async-function-in-useeffect-useeffect-function-must-ret
    async function fetchGames(observedLastGameId: number|null = null) {
        // console.log("Fetching Games");
        // dont make calls while already loading
        if (isLoading) return;
        // arm loading flag
        setIsLoading(true);

        // if last id pass it as a url param else basic url
        const url = observedLastGameId ? `api/game/list?lastGameId=${observedLastGameId}` : 'api/game/list';

        const request: RequestParams = {
            url: url,
            method: "GET",
        };

        const response = await fetchHelper(request);
        // console.log("Fetched games", response);
        if (response) {
            // concatenate the response if its paginated else initial response
            setGameList(observedLastGameId ? [...observedGameList, ...response.games] : response.games);
            setLastGameId(response.lastGameId);
            setHasMore(response.lastGameId !== null);
        }
        //disarm loading flag so future request can be made
        setIsLoading(false);
    }

    async function refresh() {
        fetchGames();
    }

    // load games on mount
    useEffect(() => {
        // console.log("Fetching Games use effect");
        fetchGames();
    },[]);

    return (
        <div>
            {observedGameList.map((elem) => (
                <Link to={`/game/${elem.id}`}
                >
                    <div key={elem.id} style={{background:"Grey"}}>
                        <GameCard
                        id={elem.id}
                        name={elem.name}
                        currentPlayers={elem.currentPlayers}
                        maxPlayers={elem.maxPlayers}
                        rounds={elem.rounds}
                        />
                    </div>
                </Link>
            ))}
            {hasMore && (
                <button onClick={loadMore} disabled={isLoading}>
                    {isLoading ? "Loading..." : "Load More"}
                </button>
            )}
            <button
            onClick={refresh}
            >
                Refresh
            </button>
        </div>
    )
}