import {Hand} from "../domain/Game";

type TurnPointsProps = {
    hand: Hand;
    points: number;
};

export function TurnPoints({hand, points}: TurnPointsProps) {
    return (
        <div>
            <span>
                <p>Points: {points} | Current Hand: {hand.handRank}</p>
            </span>
        </div>
    )
}