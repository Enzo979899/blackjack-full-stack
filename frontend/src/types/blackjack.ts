export type Suit =
  | 'HEARTS'
  | 'DIAMONDS'
  | 'SPADES'
  | 'CLUBS'

export type Rank =
  | 'AS'
  | 'TWO'
  | 'THREE'
  | 'FOUR'
  | 'FIVE'
  | 'SIX'
  | 'SEVEN'
  | 'EIGHT'
  | 'NINE'
  | 'TEN'
  | 'JACK'
  | 'QUEEN'
  | 'KING'

export type RoundStatus =
  | 'WAITING_TO_START'
  | 'PLAYER_TURN'
  | 'DEALER_TURN'
  | 'FINISHED'

export type RoundResult =
  | 'PLAYER_WIN'
  | 'DEALER_WIN'
  | 'PUSH'

export interface Card {
  suit: Suit
  rank: Rank
}

export interface Hand {
  cards: Card[]
  score: number
}

export interface Player {
  playerId: string
  name: string
}

export interface Round {
  roundId: string
  playerId: string
  status: RoundStatus
  playerHand: Hand
  dealerHand: Hand
  result: RoundResult | null
}

export interface RankingEntry {
  playerId: string
  playerName: string
  wins: number
  losses: number
  draws: number
}