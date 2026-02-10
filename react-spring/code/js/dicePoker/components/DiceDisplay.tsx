import { useState } from "react";
import { Dice } from "../domain/Game";

type DiceDisplayProps = {
    dices: Dice[];
    isCurrentPlayer: boolean;
    rollsLeft: number;
    onRoll: (selectedIndices: number[]) => void;
    onEndTurn: () => void;
};

export function DiceDisplay({ dices, isCurrentPlayer, rollsLeft, onRoll, onEndTurn }: DiceDisplayProps) {
    const [selectedDice, setSelectedDice] = useState<Set<number>>(new Set());

    function toggleDice(index: number) {
        setSelectedDice(prev => {
            const newSet = new Set(prev);
            if (newSet.has(index)) {
                newSet.delete(index);
            } else {
                newSet.add(index);
            }
            return newSet;
        });
    }

    function handleRoll() {
        onRoll(Array.from(selectedDice));
        setSelectedDice(new Set());
    }

    const canRoll = isCurrentPlayer && selectedDice.size > 0 && rollsLeft > 0;

    return (
        <div>
            <div style={{ display: "flex", gap: "10px", marginBottom: "10px" }}>
                {dices.map((dice, index) => (
                    <button
                        key={index}
                        onClick={() => toggleDice(index)}
                        disabled={!isCurrentPlayer}
                        style={{
                            width: "50px",
                            height: "50px",
                            fontSize: "16px",
                            border: selectedDice.has(index) ? "3px solid blue" : "1px solid gray",
                            backgroundColor: selectedDice.has(index) ? "#e0e0ff" : "white",
                            cursor: isCurrentPlayer ? "pointer" : "not-allowed",
                        }}
                    >
                        {dice.face}
                    </button>
                ))}
            </div>
            <div style={{ display: "flex", gap: "10px" }}>
                <button onClick={handleRoll} disabled={!canRoll}>
                    Roll Dice
                </button>
                <button onClick={onEndTurn} disabled={!isCurrentPlayer}>
                    End Turn
                </button>
            </div>
        </div>
    );
}
