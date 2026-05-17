import React, { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createDiaryEntry, getDiaryEntry, getSymptoms, getTags, uploadDiaryEntryPhotos, updateDiaryEntry, sendSummaryToTelegram } from '../api/api'
import { DiaryEntryUpsertPayload, Tag, Symptom, UUID } from '../api/types'

type Mode = 'create' | 'edit'

function todayIsoDate(): string {
  const d = new Date()
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export default function DiaryEntryPage({ mode }: { mode: Mode }) {
  const navigate = useNavigate()
  const params = useParams()

  const entryId = mode === 'edit' ? params.id : undefined

  const initialForm = useMemo(
    () => ({
      entryDate: todayIsoDate(),
      moodScore: 5,
      energyScore: 5,
      productivityScore: 5,
      stressScore: 5,
      sleepQualityScore: 5,
      note: '',
      isCompleted: false
    }),
    []
  )

  const [form, setForm] = useState(initialForm)
  const [loading, setLoading] = useState(mode === 'edit')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [warning, setWarning] = useState<string | null>(null)
  const [selectedPhotos, setSelectedPhotos] = useState<File[]>([])
  const [telegramSending, setTelegramSending] = useState(false)
  const [telegramError, setTelegramError] = useState<string | null>(null)

  const [availableTags, setAvailableTags] = useState<Tag[]>([])
  const [availableSymptoms, setAvailableSymptoms] = useState<Symptom[]>([])
  const [uiSelectedTagIds, setUiSelectedTagIds] = useState<UUID[]>([])
  const [uiSelectedSymptomIds, setUiSelectedSymptomIds] = useState<UUID[]>([])

  useEffect(() => {
    void (async () => {
      try {
        const [tags, symptoms] = await Promise.all([getTags(), getSymptoms()])
        setAvailableTags(tags)
        setAvailableSymptoms(symptoms)
      } catch {
        setAvailableTags([])
        setAvailableSymptoms([])
      }
    })()
  }, [])

  useEffect(() => {
    if (mode !== 'edit') return
    if (!entryId) {
      setError('Требуется id записи')
      setLoading(false)
      return
    }

    setLoading(true)
    setError(null)
    getDiaryEntry(entryId)
      .then((entry) => {
        setForm({
          entryDate: entry.entryDate,
          moodScore: entry.moodScore,
          energyScore: entry.energyScore,
          productivityScore: entry.productivityScore,
          stressScore: entry.stressScore,
          sleepQualityScore: entry.sleepQualityScore,
          note: entry.note ?? '',
          isCompleted: entry.isCompleted,
        })
        setUiSelectedTagIds(entry.tagIds)
        setUiSelectedSymptomIds(entry.symptomIds)
      })
      .catch((e) => {
        setError(e instanceof Error ? e.message : 'Ошибка загрузки записи')
      })
      .finally(() => setLoading(false))
  }, [mode, entryId])

  const payload = useMemo<DiaryEntryUpsertPayload>(() => {
    return {
      entryDate: form.entryDate,
      moodScore: form.moodScore,
      energyScore: form.energyScore,
      productivityScore: form.productivityScore,
      stressScore: form.stressScore,
      sleepQualityScore: form.sleepQualityScore,
      note: form.note.trim() ? form.note : null,
      isCompleted: form.isCompleted,
      tagIds: Array.from(new Set(uiSelectedTagIds)),
      symptomIds: Array.from(new Set(uiSelectedSymptomIds))
    }
  }, [form, uiSelectedTagIds, uiSelectedSymptomIds])

  const submit = async () => {
    setError(null)
    setWarning(null)
    const finalPayload: DiaryEntryUpsertPayload = payload

    setSaving(true)
    try {
      if (mode === 'create') {
        const created = await createDiaryEntry(finalPayload)
        if (selectedPhotos.length > 0) {
          await uploadDiaryEntryPhotos(created.id, selectedPhotos)
        }
        navigate(`/diary/entry/${created.id}`)
      } else {
        if (!entryId) throw new Error('Entry id is missing')
        const updated = await updateDiaryEntry(entryId, finalPayload)
        if (selectedPhotos.length > 0) {
          await uploadDiaryEntryPhotos(updated.id, selectedPhotos)
        }
        navigate(`/diary/entry/${updated.id}`)
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Ошибка сохранения')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="journal-page max-w-4xl">
      <div className="journal-card mb-6 p-6 md:p-8">
        <div className="journal-eyebrow">{mode === 'create' ? 'Новая страница' : 'Возвращение к странице'}</div>
        <h1 className="journal-title mt-3">{mode === 'create' ? 'Новая запись' : 'Редактирование записи'}</h1>
        <p className="journal-subtitle">
          Оцените день по шкале от 1 до 10 и оставьте пару строк, чтобы позже увидеть не только цифры, но и контекст.
        </p>
      </div>

      {loading ? (
        <div className="journal-card p-4 text-journal-inkMuted dark:text-journalDark-inkMuted">Загрузка…</div>
      ) : (
        <form
          className="journal-card space-y-6 p-5 md:p-7"
          onSubmit={(e) => {
            e.preventDefault()
            void submit()
          }}
        >
          <div className="grid grid-cols-1 gap-4 md:grid-cols-[1fr_auto]">
            <label className="space-y-2">
              <span className="journal-eyebrow">Дата</span>
              <input
                className="journal-input w-full"
                type="date"
                value={form.entryDate}
                onChange={(e) => setForm((s) => ({ ...s, entryDate: e.target.value }))}
                required
              />
            </label>

            <label className="flex items-center gap-3 rounded-2xl border border-journal-line/80 bg-journal-paper/60 px-4 py-3 md:self-end md:justify-self-end dark:border-journalDark-line dark:bg-journalDark-paper/60">
              <input type="checkbox" checked={form.isCompleted} onChange={(e) => setForm((s) => ({ ...s, isCompleted: e.target.checked }))} />
              <span className="font-heading text-sm">День завершён</span>
            </label>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <ScaleField label="Настроение" value={form.moodScore} onChange={(v) => setForm((s) => ({ ...s, moodScore: v }))} />
            <ScaleField label="Энергия" value={form.energyScore} onChange={(v) => setForm((s) => ({ ...s, energyScore: v }))} />
            <ScaleField label="Продуктивность" value={form.productivityScore} onChange={(v) => setForm((s) => ({ ...s, productivityScore: v }))} />
            <ScaleField label="Стресс" value={form.stressScore} onChange={(v) => setForm((s) => ({ ...s, stressScore: v }))} />
            <ScaleField label="Качество сна" value={form.sleepQualityScore} onChange={(v) => setForm((s) => ({ ...s, sleepQualityScore: v }))} />
          </div>

          <label className="block space-y-2">
            <span className="journal-eyebrow">Заметка</span>
            <textarea
              className="journal-input min-h-40 w-full p-4 text-lg leading-relaxed"
              value={form.note}
              onChange={(e) => setForm((s) => ({ ...s, note: e.target.value }))}
              maxLength={10000}
              placeholder="Что произошло сегодня? Какие детали хочется сохранить?"
            />
          </label>

          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <div className="journal-eyebrow">Метки состояния</div>
              <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Выберите теги и симптомы ниже</div>
            </div>
            <div className="flex items-center gap-2">
              <button type="button" className="journal-btn-secondary text-sm" onClick={() => navigate('/diary/tags')}>
                Управление тегами
              </button>
              <button type="button" className="journal-btn-secondary text-sm" onClick={() => navigate('/diary/symptoms')}>
                Управление симптомами
              </button>
            </div>
          </div>

          {availableTags.length > 0 ? (
            <div className="journal-card space-y-3 p-4">
              <div className="font-heading text-lg font-semibold">Теги</div>
              <div className="flex flex-wrap gap-2">
                {availableTags.map((t) => {
                  const checked = uiSelectedTagIds.includes(t.id)
                  return (
                    <label
                      key={t.id}
                      className={`flex cursor-pointer items-center gap-2 rounded-full border px-3 py-1.5 text-sm transition-all ${
                        checked
                          ? 'border-journal-accent bg-journal-accent text-white dark:border-journalDark-accent dark:bg-journalDark-accent dark:text-journalDark-bg'
                          : 'border-journal-line bg-journal-paper/60 text-journal-inkMuted hover:bg-journal-fold dark:border-journalDark-line dark:bg-journalDark-paper/60 dark:text-journalDark-inkMuted dark:hover:bg-journalDark-fold'
                      }`}
                    >
                      <input
                        className="sr-only"
                        type="checkbox"
                        checked={checked}
                        onChange={(e) => {
                          setUiSelectedTagIds((prev) => {
                            if (e.target.checked) return Array.from(new Set([...prev, t.id]))
                            return prev.filter((x) => x !== t.id)
                          })
                        }}
                      />
                      <span>{t.name}</span>
                    </label>
                  )
                })}
              </div>
            </div>
          ) : null}

          {availableSymptoms.length > 0 ? (
            <div className="journal-card space-y-3 p-4">
              <div className="font-heading text-lg font-semibold">Симптомы</div>
              <div className="flex flex-wrap gap-2">
                {availableSymptoms.map((s) => {
                  const checked = uiSelectedSymptomIds.includes(s.id)
                  return (
                    <label
                      key={s.id}
                      className={`flex cursor-pointer items-center gap-2 rounded-full border px-3 py-1.5 text-sm transition-all ${
                        checked
                          ? 'border-journal-accent bg-journal-accent text-white dark:border-journalDark-accent dark:bg-journalDark-accent dark:text-journalDark-bg'
                          : 'border-journal-line bg-journal-paper/60 text-journal-inkMuted hover:bg-journal-fold dark:border-journalDark-line dark:bg-journalDark-paper/60 dark:text-journalDark-inkMuted dark:hover:bg-journalDark-fold'
                      }`}
                    >
                      <input
                        className="sr-only"
                        type="checkbox"
                        checked={checked}
                        onChange={(e) => {
                          setUiSelectedSymptomIds((prev) => {
                            if (e.target.checked) return Array.from(new Set([...prev, s.id]))
                            return prev.filter((x) => x !== s.id)
                          })
                        }}
                      />
                      <span>{s.name}</span>
                    </label>
                  )
                })}
              </div>
            </div>
          ) : null}

          <label className="block space-y-2">
            <span className="journal-eyebrow">Фото</span>
            <input
              className="journal-input w-full"
              type="file"
              accept="image/*"
              multiple
              onChange={(e) => {
                const files = e.currentTarget.files
                if (!files) {
                  setSelectedPhotos([])
                  return
                }
                setSelectedPhotos(Array.from(files))
              }}
            />
            <div className="text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">{selectedPhotos.length > 0 ? `Выбрано файлов: ${selectedPhotos.length}` : 'Необязательно'}</div>
          </label>

          {error ? <div className="journal-card border-amber-300 bg-amber-50/80 p-3 text-sm text-amber-900 dark:border-amber-800 dark:bg-amber-950/30 dark:text-amber-200">{error}</div> : null}
          {warning ? <div className="journal-card border-amber-300 bg-amber-50/80 p-3 text-sm text-amber-700 dark:border-amber-800 dark:bg-amber-950/30 dark:text-amber-200">{warning}</div> : null}
          {telegramError ? <div className="journal-card border-amber-300 bg-amber-50/80 p-3 text-sm text-amber-900 dark:border-amber-800 dark:bg-amber-950/30 dark:text-amber-200">{telegramError}</div> : null}

          <div className="journal-divider flex flex-wrap items-center justify-end gap-3 pt-4">
            <button
              type="button"
              className="journal-btn-secondary min-h-[44px] disabled:opacity-50"
              onClick={async () => {
                setTelegramError(null)
                setTelegramSending(true)
                try {
                  await sendSummaryToTelegram(form.entryDate)
                } catch (e) {
                  setTelegramError(e instanceof Error ? e.message : 'Ошибка отправки')
                } finally {
                  setTelegramSending(false)
                }
              }}
              disabled={saving || telegramSending}
            >
              {telegramSending ? 'Отправка…' : '📤 В Telegram'}
            </button>
            <button
              type="button"
              className="journal-btn-secondary min-h-[44px]"
              onClick={() => navigate('/diary/entry/new')}
              disabled={saving}
            >
              Отмена
            </button>
            <button
              type="submit"
              className="journal-btn-primary min-h-[44px] disabled:opacity-50"
              disabled={saving}
            >
              {saving ? 'Сохранение…' : mode === 'create' ? 'Создать запись' : 'Сохранить'}
            </button>
          </div>
        </form>
      )}
    </div>
  )
}

const SCORE_OPTIONS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] as const

function ScaleField({ label, value, onChange }: { label: string; value: number; onChange: (v: number) => void }) {
  const percent = Math.max(0, Math.min(100, (value / 10) * 100))

  return (
    <label className="journal-card block space-y-3 p-4">
      <div className="flex items-center justify-between gap-3">
        <span className="font-heading text-sm font-semibold text-journal-ink dark:text-journalDark-ink">{label}</span>
        <span className="rounded-full bg-journal-fold px-3 py-1 font-heading text-sm text-journal-accent dark:bg-journalDark-fold dark:text-journalDark-accent">
          {value}/10
        </span>
      </div>
      <select
        className="journal-input min-h-[44px] w-full"
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        required
      >
        {SCORE_OPTIONS.map((n) => (
          <option key={n} value={n}>
            {n}
          </option>
        ))}
      </select>
      <div className="h-1.5 overflow-hidden rounded-full bg-journal-line/70 dark:bg-journalDark-line">
        <div className="h-full rounded-full bg-journal-accent dark:bg-journalDark-accent" style={{ width: `${percent}%` }} />
      </div>
    </label>
  )
}

