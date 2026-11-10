import type { Player, Round, RankingEntry } from '../types/blackjack'

const API_URL = '/api'

async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  let response: Response

try {
  response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })
} catch {
  throw new Error('No se puede conectar con el servidor. Inténtalo de nuevo.')
}

  if (!response.ok) {
  let message = `Error HTTP ${response.status}: ${response.statusText}`

  try {
    const errorBody: { error?: string; message?: string } =
      await response.json()

    message = errorBody.error ?? errorBody.message ?? message
  } catch {
    // La respuesta no contiene un JSON válido.
  }

  if (response.status === 409 && endpoint === '/players') {
    message = 'Este nombre de jugador ya está registrado. Elige otro nombre.'
  }

  throw new Error(message)
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