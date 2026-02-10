
export type Game = {
    id: number;
    name: string;
    description: string;
    maxPlayers: number;
    minPlayers: number;
    rounds: number;
    status: string;
    timeout: number;
    gameStateType: string;
    gameState: GameState;
}

export type GameState = {
    currentPlayerId: number;
    currentRound: number;
    gameId: number;
    maxRounds: number;
    playerList: [PlayerSummary];
    playerHands: {number: Hand} //number is the player id
    playersExcluded: [number];
    ante: number;
    playersPaidAnte: {number: number;};
    pot: number;
    roundWinner: [PlayerSummary];
    winners: [PlayerSummary];
    turnTimer: number;
    turnStarted: number;
    rollsForTurn: number;
}


export type PlayerSummary = {
    userId: number;
    balance: number;
    currentHand?: Hand;
    hasPlayed: boolean;
    isHost: boolean;
}

export type Hand = {
    dices: [Dice];
    handRank: string;
    points: number;
}

export type Dice = {
    face: string;
    points: number;
}