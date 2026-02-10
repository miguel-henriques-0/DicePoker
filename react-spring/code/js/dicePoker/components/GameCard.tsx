type GameCardProps = {
    id: number;
    name: string;
    currentPlayers: number;
    maxPlayers: number;
    rounds: number;
}

export function GameCard(props: GameCardProps) {
    return (
        <div key={props.id}>
            <p>{props.name}</p>
            <p>{props.currentPlayers}/{props.maxPlayers}</p>
            <p>{props.rounds}</p>
        </div>
    )
}