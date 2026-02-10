import {ListGame} from "./ListGame";
import {useAuthentication} from "../context/authentication";
import {Link} from "react-router";
import {InviteCode} from "./InviteCode";

export function Home() {
    const [username] = useAuthentication()
    return (
        <div>
            {username ?
                <div>
                    <h2>Home</h2>
                    <p>Join a game or create one</p>
                    <Link to={'/statistics'}>
                        <button>Statistics</button>
                    </Link>
                    <Link to={'/create-game'}>
                        <button>Create Game</button>
                    </Link>
                    <ListGame />
                    <InviteCode />
                </div>
                :
                <p>Welcome! Please log in to start playing</p>
            }
        </div>
    )
}