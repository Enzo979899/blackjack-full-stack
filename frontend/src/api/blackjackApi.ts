import type { Player, Round, RankingEntry } from '../types/blackjack'

const API_URL = '/api'

async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })

  if (!response.ok) {
    throw new Error(
      `Error HTTP ${response.status}: ${response.statusText}`
    )
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

export const blackjackApi = {
  createPlayer(name: string): Promise<Player> {
    return request<Player>('/players', {
      method: 'POST',
      body: JSON.stringify({ name }),
    })
  },

  startRound(playerId: string): Promise<string> {
    return request<string>('/rounds', {
      method: 'POST',
      body: JSON.stringify({ playerId }),
    })
  },

  getRound(roundId: string): Promise<Round> {
    return request<Round>(`/rounds/${roundId}`)
  },

  hit(roundId: string): Promise<void> {
    return request<void>(`/rounds/${roundId}/hit`, {
      method: 'POST',
    })
  },

  stand(roundId: string): Promise<void> {
    return request<void>(`/rounds/${roundId}/stand`, {
      method: 'POST',
    })
  },

  getRanking(): Promise<RankingEntry[]> {
    return request<RankingEntry[]>('/ranking')
  },
}