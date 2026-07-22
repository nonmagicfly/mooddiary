import {
  AnalyticsCorrelations,
  MoodAnalyticsResponse,
  DiaryEntry,
  DiaryEntryUpsertPayload,
  Photo,
  Symptom,
  Tag,
  UUID,
  TagUpsertPayload,
  SymptomUpsertPayload
} from './types'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ? String(import.meta.env.VITE_API_BASE_URL) : ''

function getAccessToken(): string | null {
  return localStorage.getItem('access_token')
}

export class ApiRequestError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiRequestError'
    this.status = status
  }
}

function errorMessageForStatus(status: number, message?: string): string {
  if (message) return message
  if (status === 409) return 'Запись за эту дату уже существует'
  if (status === 413) return 'Файл слишком большой. Попробуйте фото меньшего размера.'
  return `Request failed with status ${status}`
}

async function apiRequest<T>(path: string, init: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, init)
  if (!res.ok) {
    let message: string | undefined
    try {
      const text = await res.text()
      if (text) {
        const body = JSON.parse(text) as { message?: string }
        message = body.message
      }
    } catch {
      // non-JSON error body (e.g. nginx 413 page)
    }
    throw new ApiRequestError(errorMessageForStatus(res.status, message), res.status)
  }
  if (res.status === 204) {
    return undefined as T
  }
  const text = await res.text()
  if (!text) {
    return undefined as T
  }
  return JSON.parse(text) as T
}

function authHeaders(): Record<string, string> {
  const token = getAccessToken()
  const headers: Record<string, string> = {}
  if (token) headers.Authorization = `Bearer ${token}`
  return headers
}

export async function getDiaryEntry(id: UUID): Promise<DiaryEntry> {
  return apiRequest<DiaryEntry>(`/api/v1/diary-entries/${id}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    }
  })
}

export async function listDiaryEntries(params?: { from?: string; to?: string; limit?: number }): Promise<DiaryEntry[]> {
  const qs = new URLSearchParams()
  if (params?.from) qs.set('from', params.from)
  if (params?.to) qs.set('to', params.to)
  if (params?.limit) qs.set('limit', String(params.limit))

  const path = `/api/v1/diary-entries${qs.toString() ? `?${qs.toString()}` : ''}`

  return apiRequest<DiaryEntry[]>(path, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    }
  })
}

export async function createDiaryEntry(payload: DiaryEntryUpsertPayload): Promise<DiaryEntry> {
  return apiRequest<DiaryEntry>(`/api/v1/diary-entries`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    },
    body: JSON.stringify(payload)
  })
}

export async function updateDiaryEntry(id: UUID, payload: DiaryEntryUpsertPayload): Promise<DiaryEntry> {
  return apiRequest<DiaryEntry>(`/api/v1/diary-entries/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    },
    body: JSON.stringify(payload)
  })
}

export async function uploadDiaryEntryPhotos(entryId: UUID, files: File[]): Promise<Photo[]> {
  const form = new FormData()
  for (const file of files) {
    form.append('files', file)
  }

  return apiRequest<Photo[]>(`/api/v1/diary-entries/${entryId}/photos`, {
    method: 'POST',
    headers: {
      ...authHeaders()
    },
    body: form
  })
}

export async function getAnalyticsDaily(date?: string): Promise<MoodAnalyticsResponse> {
  const qs = new URLSearchParams()
  if (date) qs.set('date', date)
  const path = `/api/v1/analytics/daily${qs.toString() ? `?${qs.toString()}` : ''}`
  return apiRequest<MoodAnalyticsResponse>(path, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    }
  })
}

export async function getAnalyticsWeekly(date?: string): Promise<MoodAnalyticsResponse> {
  const qs = new URLSearchParams()
  if (date) qs.set('date', date)
  const path = `/api/v1/analytics/weekly${qs.toString() ? `?${qs.toString()}` : ''}`
  return apiRequest<MoodAnalyticsResponse>(path, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    }
  })
}

export async function getAnalyticsMonthly(date?: string): Promise<MoodAnalyticsResponse> {
  const qs = new URLSearchParams()
  if (date) qs.set('date', date)
  const path = `/api/v1/analytics/monthly${qs.toString() ? `?${qs.toString()}` : ''}`
  return apiRequest<MoodAnalyticsResponse>(path, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    }
  })
}


export async function getTags(): Promise<Tag[]> {
  return apiRequest<Tag[]>(`/api/v1/tags`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    }
  })
}

export async function getSymptoms(): Promise<Symptom[]> {
  return apiRequest<Symptom[]>(`/api/v1/symptoms`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    }
  })
}

export async function createTag(payload: TagUpsertPayload): Promise<Tag> {
  return apiRequest<Tag>(`/api/v1/tags`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    },
    body: JSON.stringify(payload)
  })
}

export async function updateTag(id: UUID, payload: TagUpsertPayload): Promise<Tag> {
  return apiRequest<Tag>(`/api/v1/tags/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    },
    body: JSON.stringify(payload)
  })
}

export async function deleteTag(id: UUID): Promise<void> {
  return apiRequest<void>(`/api/v1/tags/${id}`, {
    method: 'DELETE',
    headers: {
      ...authHeaders()
    }
  })
}

export async function createSymptom(payload: SymptomUpsertPayload): Promise<Symptom> {
  return apiRequest<Symptom>(`/api/v1/symptoms`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    },
    body: JSON.stringify(payload)
  })
}

export async function updateSymptom(id: UUID, payload: SymptomUpsertPayload): Promise<Symptom> {
  return apiRequest<Symptom>(`/api/v1/symptoms/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    },
    body: JSON.stringify(payload)
  })
}

export async function deleteSymptom(id: UUID): Promise<void> {
  return apiRequest<void>(`/api/v1/symptoms/${id}`, {
    method: 'DELETE',
    headers: {
      ...authHeaders()
    }
  })
}

export async function getTelegramChatId(): Promise<{ telegramChatId: string }> {
  return apiRequest<{ telegramChatId: string }>(`/api/v1/settings/telegram-chat-id`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    }
  })
}

export async function updateTelegramChatId(telegramChatId: string): Promise<void> {
  return apiRequest<void>(`/api/v1/settings/telegram-chat-id`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders()
    },
    body: JSON.stringify({ telegramChatId })
  })
}

export async function sendSummaryToTelegram(date?: string): Promise<void> {
  const qs = date ? `?date=${date}` : ''
  return apiRequest<void>(`/api/v1/summary/send-telegram${qs}`, {
    method: 'POST',
    headers: {
      ...authHeaders()
    }
  })
}

