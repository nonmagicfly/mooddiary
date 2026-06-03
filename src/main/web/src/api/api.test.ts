import { beforeEach, describe, it, expect, vi } from 'vitest'
import { createDiaryEntry, deleteSymptom, getDiaryEntry } from './api'

describe('api', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.restoreAllMocks()
  })

  it('should attach Authorization header when access_token exists', async () => {
    localStorage.setItem('access_token', 'token-1')

    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      text: async () => JSON.stringify({ id: 'e', userId: 'u', entryDate: '2026-01-01', moodScore: 1, energyScore: 1, productivityScore: 1, stressScore: 1, sleepQualityScore: 1, note: null, isCompleted: false, tagIds: [], symptomIds: [], createdAt: '2026-01-01T00:00:00.000Z', updatedAt: null })
    } as unknown as Response)

    ;(globalThis as unknown as { fetch: typeof fetch }).fetch = fetchMock

    await getDiaryEntry('e')

    expect(fetchMock).toHaveBeenCalled()
    const init = fetchMock.mock.calls[0][1] as RequestInit
    const headers = init?.headers as Record<string, string>
    expect(headers.Authorization).toBe('Bearer token-1')
  })

  it('should send JSON payload for createDiaryEntry', async () => {
    localStorage.setItem('access_token', 'token-1')

    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 201,
      text: async () => JSON.stringify({ id: 'e', userId: 'u', entryDate: '2026-01-01', moodScore: 1, energyScore: 1, productivityScore: 1, stressScore: 1, sleepQualityScore: 1, note: null, isCompleted: false, tagIds: [], symptomIds: [], createdAt: '2026-01-01T00:00:00.000Z', updatedAt: null })
    } as unknown as Response)

    ;(globalThis as unknown as { fetch: typeof fetch }).fetch = fetchMock

    await createDiaryEntry({
      entryDate: '2026-01-01',
      moodScore: 1,
      energyScore: 1,
      productivityScore: 1,
      stressScore: 1,
      sleepQualityScore: 1,
      note: null,
      isCompleted: false,
      tagIds: [],
      symptomIds: []
    })

    const init = fetchMock.mock.calls[0][1] as RequestInit
    const body = init?.body as string
    expect(body).toContain('"entryDate":"2026-01-01"')
  })

  it('should treat 204 no content as successful void response', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 204,
      text: async () => ''
    } as unknown as Response)

    ;(globalThis as unknown as { fetch: typeof fetch }).fetch = fetchMock

    await expect(deleteSymptom('symptom-1')).resolves.toBeUndefined()
  })
})

