
import { useState, type FormEvent } from 'react'
import { blackjackApi } from './api/blackjackApi'
import type {
  Card,
  Player,
  RankingEntry,
  Round,
  Suit,
  Rank,
} from './types/blackjack'
import './App.css'

type Screen = 'menu' | 'create' | 'select' | 'game' | 'ranking'

const SUITS: Record<Suit, string> = {
  HEARTS: '♥',
  DIAMONDS: '♦',
  SPADES: '♠',
  CLUBS: '♣',
}

const RANKS: Record<Rank, string> = {
  AS: 'A',
  TWO: '2',
  THREE: '3',
  FOUR: '4',
  FIVE: '5',
  SIX: '6',
  SEVEN: '7',
  EIGHT: '8',
  NINE: '9',
  TEN: '10',
  JACK: 'J',
  QUEEN: 'Q',
  KING: 'K',
}

const STORAGE_KEY = 'blackjack-players'

const ROUND_STORAGE_KEY = 'blackjack-active-rounds'

function getSavedRounds(): Record<string, string> {
  try {
    const saved = localStorage.getItem(ROUND_STORAGE_KEY)
    return saved ? JSON.parse(saved) as Record<string, string> : {}
  } catch {
    return {}
  }
}

function saveActiveRound(playerId: string, roundId: string) {
  const rounds = getSavedRounds()
  rounds[playerId] = roundId

  localStorage.setItem(ROUND_STORAGE_KEY, JSON.stringify(rounds))
}

function removeActiveRound(playerId: string) {
  const rounds = getSavedRounds()
  delete rounds[playerId]

  localStorage.setItem(ROUND_STORAGE_KEY, JSON.stringify(rounds))
}

function getSavedPlayers(): Player[] {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    return saved ? JSON.parse(saved) as Player[] : []
  } catch {
    return []
  }
}

function savePlayer(player: Player) {
  const players = getSavedPlayers()
    .filter((p) => p.playerId !== player.playerId)

  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify([...players, player])
  )
}

function PlayingCard({
  card,
  hidden = false,
}: {
  card?: Card
  hidden?: boolean
}) {
  if (hidden || !card) {
    return (
      <div className="playing-card card-back" aria-label="Carta oculta">
        <div className="card-back-pattern">
          <span>♠</span>
          <small>BLACKJACK</small>
          <span>♦</span>
        </div>
      </div>
    )
  }

  const red = card.suit === 'HEARTS' || card.suit === 'DIAMONDS'
  const suit = SUITS[card.suit]
  const rank = RANKS[card.rank]

  return (
    <div
      className={`playing-card ${red ? 'red' : 'black'}`}
      aria-label={`${card.rank} ${card.suit}`}
    >
      <div className="card-corner">
        <strong>{rank}</strong>
        <span>{suit}</span>
      </div>

      <div className="card-center">{suit}</div>

      <div className="card-corner bottom">
        <strong>{rank}</strong>
        <span>{suit}</span>
      </div>
    </div>
  )
}

function App() {
  const [screen, setScreen] = useState<Screen>('menu')
  const [playerName, setPlayerName] = useState('')
  const [player, setPlayer] = useState<Player | null>(null)
  const [round, setRound] = useState<Round | null>(null)
  const [ranking, setRanking] = useState<RankingEntry[]>([])
  const [players, setPlayers] = useState<Player[]>([])
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const finished = round?.status === 'FINISHED'
  const canPlay = round?.status === 'PLAYER_TURN' && !busy

  function goToMenu() {
    setScreen('menu')
    setRound(null)
    setPlayer(null)
    setError('')
  }
async function startRound(selectedPlayer: Player) {
  setBusy(true)
  setError('')

  try {
    const savedRoundId = getSavedRounds()[selectedPlayer.playerId]

    if (savedRoundId) {
      try {
        const savedRound = await blackjackApi.getRound(savedRoundId)

        if (savedRound.status !== 'FINISHED') {
          setPlayer(selectedPlayer)
          setRound(savedRound)
          setScreen('game')
          return
        }

        // Si la ronda terminó, no la recuperamos como activa.
        removeActiveRound(selectedPlayer.playerId)
      } catch {
        // Si no podemos comprobarla, conservamos su referencia.
        throw new Error(
          'No se pudo recuperar la ronda pendiente. Inténtalo de nuevo.'
        )
      }
    }

    // Si no existe una ronda activa, creamos una nueva.
    const roundId = await blackjackApi.startRound(
      selectedPlayer.playerId
    )

    saveActiveRound(selectedPlayer.playerId, roundId)

    const newRound = await blackjackApi.getRound(roundId)

    if (newRound.status === 'FINISHED') {
      removeActiveRound(selectedPlayer.playerId)
    }

    setPlayer(selectedPlayer)
    setRound(newRound)
    setScreen('game')
  } catch (err) {
    setError(getErrorMessage(err))
  } finally {
    setBusy(false)
  }
}

  async function createPlayer(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const name = playerName.trim()
    if (!name || busy) return

    setBusy(true)
    setError('')

    let created: Player

    try {
      created = await blackjackApi.createPlayer(name)
      savePlayer(created)
      setPlayerName('')
    } catch (err) {
      setError(getErrorMessage(err))
      setBusy(false)
      return
    }

    setBusy(false)
    await startRound(created)
  }

  async function loadPlayers() {
    setScreen('select')
    setBusy(true)
    setError('')

    try {
      const entries = await blackjackApi.getRanking()

      const all = new Map<string, Player>()

      for (const saved of getSavedPlayers()) {
        all.set(saved.playerId, saved)
      }

      for (const entry of entries) {
        all.set(entry.playerId, {
          playerId: entry.playerId,
          name: entry.playerName,
        })
      }

      setPlayers(
        [...all.values()].sort((a, b) =>
          a.name.localeCompare(b.name)
        )
      )
    } catch (err) {
      setPlayers(getSavedPlayers())
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  async function loadRanking() {
    setScreen('ranking')
    setBusy(true)
    setError('')

    try {
      const entries = await blackjackApi.getRanking()
      setRanking(entries)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  async function playAction(action: 'hit' | 'stand') {
  if (!round || !canPlay) return

  setBusy(true)
  setError('')

  try {
    if (action === 'hit') {
      await blackjackApi.hit(round.roundId)
    } else {
      await blackjackApi.stand(round.roundId)
    }

    const updated = await blackjackApi.getRound(round.roundId)

    setRound(updated)

    // Si la ronda ha terminado, deja de estar pendiente.
    if (updated.status === 'FINISHED') {
      removeActiveRound(updated.playerId)
    }
  } catch (err) {
    setError(getErrorMessage(err))
  } finally {
    setBusy(false)
  }
}

  function getResult() {
    if (!round || !finished) return null

    switch (round.result) {
      case 'PLAYER_WIN':
        return { title: '¡HAS GANADO!', icon: '★', kind: 'win' }
      case 'DEALER_WIN':
        return { title: 'GANA LA BANCA', icon: '✕', kind: 'lose' }
      case 'PUSH':
        return { title: 'EMPATE', icon: '=', kind: 'draw' }
      default:
        return null
    }
  }

  const result = getResult()

  return (
    <div className="app">
      <header className="topbar">
        <button className="brand" onClick={goToMenu}>
          <span className="brand-symbol">♠</span>
          <span>BLACKJACK</span>
        </button>
        <span className="topbar-caption">CASINO TABLE</span>
      </header>

      <main className="main-content">
        {screen === 'menu' && (
          <section className="menu-screen">
            <div className="menu-decoration">♠</div>
            <p className="eyebrow">THE CLASSIC CARD GAME</p>
            <h1 className="hero-title">BLACKJACK</h1>
            <div className="gold-line" />
            <p className="hero-description">
              Enfréntate a la banca. Acércate a 21 sin pasarte.
            </p>

            <div className="menu-actions">
              <button
                className="btn btn-gold"
                onClick={() => {
                  setError('')
                  setScreen('create')
                }}
              >
                <span>✦</span> Crear jugador nuevo
              </button>

              <button
                className="btn btn-outline"
                onClick={loadPlayers}
              >
                <span>♠</span> Jugar con jugador existente
              </button>

              <button
                className="btn btn-outline"
                onClick={loadRanking}
              >
                <span>♛</span> Consultar ranking
              </button>
            </div>

            <p className="menu-footer">
              UN JUGADOR · CONTRA LA BANCA · SIN APUESTAS
            </p>
          </section>
        )}

        {screen === 'create' && (
          <section className="panel-screen">
            <div className="panel">
              <div className="panel-symbol">♠</div>
              <p className="eyebrow">NUEVA PARTIDA</p>
              <h2>Crear jugador</h2>
              <p className="panel-description">
                Introduce tu nombre para sentarte a la mesa.
              </p>

              <form onSubmit={createPlayer} className="player-form">
                <label htmlFor="player-name">
                  NOMBRE DEL JUGADOR
                </label>

                <input
                  id="player-name"
                  autoFocus
                  type="text"
                  value={playerName}
                  onChange={(event) =>
                    setPlayerName(event.target.value)
                  }
                  placeholder="Introduce tu nombre"
                  maxLength={60}
                  required
                />

                <button
                  className="btn btn-gold"
                  type="submit"
                  disabled={busy || !playerName.trim()}
                >
                  {busy ? 'Preparando...' : 'Crear y jugar'}
                </button>
              </form>

              <button className="text-button" onClick={goToMenu}>
                ← Volver al menú
              </button>
            </div>
          </section>
        )}

        {screen === 'select' && (
          <section className="panel-screen">
            <div className="panel panel-wide">
              <p className="eyebrow">ELIGE TU PERFIL</p>
              <h2>Jugadores</h2>
              <p className="panel-description">
                Selecciona un jugador para enfrentarte a la banca.
              </p>

              {busy && <p className="muted">Cargando jugadores...</p>}

              {!busy && players.length === 0 && (
                <p className="empty-state">
                  No hay jugadores disponibles.
                  Crea uno desde el menú principal.
                </p>
              )}

              <div className="player-list">
                {!busy && players.map((item) => (
                  <button
                    key={item.playerId}
                    className="player-item"
                    onClick={() => startRound(item)}
                  >
                    <span className="player-avatar">
                      {item.name.charAt(0).toUpperCase()}
                    </span>
                    <span>{item.name}</span>
                    <span className="player-arrow">→</span>
                  </button>
                ))}
              </div>

              <button className="text-button" onClick={goToMenu}>
                ← Volver al menú
              </button>
            </div>
          </section>
        )}

        {screen === 'ranking' && (
          <section className="panel-screen">
            <div className="panel ranking-panel">
              <div className="panel-symbol">♛</div>
              <p className="eyebrow">CLASIFICACIÓN</p>
              <h2>Ranking</h2>
              <p className="panel-description">
                Estadísticas de los jugadores
              </p>

              {busy ? (
                <p className="muted">Cargando ranking...</p>
              ) : (
                <div className="ranking-scroll">
                  <table className="ranking-table">
                    <thead>
                      <tr>
                        <th>Jugador</th>
                        <th>Victorias</th>
                        <th>Derrotas</th>
                        <th>Empates</th>
                      </tr>
                    </thead>
                    <tbody>
                      {ranking.map((entry) => (
                        <tr key={entry.playerId}>
                          <td>{entry.playerName}</td>
                          <td className="stat-win">{entry.wins}</td>
                          <td className="stat-loss">{entry.losses}</td>
                          <td>{entry.draws}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>

                  {ranking.length === 0 && (
                    <p className="empty-state">
                      Todavía no hay estadísticas.
                    </p>
                  )}
                </div>
              )}

              <button
                className="btn btn-outline"
                onClick={goToMenu}
              >
                Volver al menú
              </button>
            </div>
          </section>
        )}

        {screen === 'game' && round && player && (
          <section className="game-screen">
            <div className="game-heading">
              <div>
                <p className="eyebrow">MESA DE JUEGO</p>
                <h2>BLACKJACK</h2>
              </div>
              <div className="current-player">
                <span className="online-dot" />
                {player.name}
              </div>
            </div>

            <div className="table-wood">
              <div className="table-felt">
                <div className="table-watermark">
                  BLACKJACK
                  <span>♠ ♥ ♦ ♣</span>
                </div>

                <div className="hand-area dealer-area">
                  <div className="hand-heading">
                    <span>BANCA</span>
                    <span className="score-badge">
                      {round.dealerHand.score}
                    </span>
                  </div>

                  <div className="cards-row">
                    {round.dealerHand.cards.map((card, index) => (
                      <PlayingCard key={index} card={card} />
                    ))}

                    {!finished && (
                      <PlayingCard hidden />
                    )}
                  </div>
                </div>

                <div className="table-divider">
                  <span>21</span>
                </div>

                <div className="hand-area player-area">
                  <div className="hand-heading">
                    <span>{player.name.toUpperCase()}</span>
                    <span className="score-badge">
                      {round.playerHand.score}
                    </span>
                  </div>

                  <div className="cards-row">
                    {round.playerHand.cards.map((card, index) => (
                      <PlayingCard key={index} card={card} />
                    ))}
                  </div>
                </div>
              </div>
            </div>

            <div className="game-controls">
              {finished && result ? (
                <div className={`result-banner ${result.kind}`}>
                  <div className="result-icon">{result.icon}</div>
                  <h3>{result.title}</h3>

                  <div className="result-actions">
                    <button
                      className="btn btn-gold"
                      disabled={busy}
                      onClick={() => startRound(player)}
                    >
                      {busy ? 'Preparando...' : 'Nueva ronda'}
                    </button>

                    <button
                      className="btn btn-outline"
                      onClick={goToMenu}
                    >
                      Volver al menú
                    </button>
                  </div>
                </div>
              ) : (
                <div className="action-bar">
                  <div className="action-buttons">
                    <button
                      className="btn btn-gold"
                      disabled={!canPlay}
                      onClick={() => playAction('hit')}
                    >
                      + Pedir carta
                    </button>

                    <button
                      className="btn btn-outline"
                      disabled={!canPlay}
                      onClick={() => playAction('stand')}
                    >
                      Plantarse
                    </button>
                  </div>

                  <p className="game-hint">
                    {busy
                      ? 'Procesando jugada...'
                      : round.status === 'PLAYER_TURN'
                        ? 'Tu turno: pide carta o plántate.'
                        : 'Esperando el resultado de la ronda.'}
                  </p>
                </div>
              )}
            </div>

            {!finished && (
              <button
                className="text-button game-exit"
                onClick={goToMenu}
                disabled={busy}
              >
                ← Volver al menú
              </button>
            )}
          </section>
        )}

        {error && (
          <div className="error-message" role="alert">
            {error}
            <button
              onClick={() => setError('')}
              aria-label="Cerrar error"
            >
              ×
            </button>
          </div>
        )}
      </main>

      <footer className="footer">
        BLACKJACK <span>♠</span> PLAYER VS DEALER
      </footer>
    </div>
  )
}

function getErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message
  }
  return 'Ha ocurrido un error inesperado.'
}

export default App
