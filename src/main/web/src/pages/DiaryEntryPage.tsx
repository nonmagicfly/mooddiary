import React, { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import {
  createDiaryEntry,
  getDiaryEntry,
  getSymptoms,
  getTags,
  uploadDiaryEntryPhotos,
  updateDiaryEntry,
  sendSummaryToTelegram
} from '../api/api'
import { DiaryEntry, DiaryEntryUpsertPayload, Tag, Symptom, UUID } from '../api/types'
import { compressImageFiles } from '../utils/compressImage'

type Mode = 'create' | 'edit'
const LEGACY_SLEEP_QUALITY_SCORE = 5

function todayIsoDate(): string {
  const d = new Date()
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export default function DiaryEntryPage({ mode }: { mode: Mode }) {
  const navigate = useNavigate()
  const location = useLocation()
  const params = useParams()

  const entryId = mode === 'edit' ? params.id : undefined

  const initialForm = useMemo(
    () => ({
      entryDate: todayIsoDate(),
      moodScore: 5,
      energyScore: 5,
      productivityScore: 5,
      stressScore: 5,
      note: ''
    }),
    []
  )

  const [form, setForm] = useState(initialForm)
  const [entryReadOnly, setEntryReadOnly] = useState(false)
  const [loading, setLoading] = useState(mode === 'edit')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [warning, setWarning] = useState<string | null>(null)
  const [selectedPhotos, setSelectedPhotos] = useState<File[]>([])
  const [preparingPhotos, setPreparingPhotos] = useState(false)
  const [telegramSending, setTelegramSending] = useState(false)
  const [telegramError, setTelegramError] = useState<string | null>(null)

  const [availableTags, setAvailableTags] = useState<Tag[]>([])
  const [availableSymptoms, setAvailableSymptoms] = useState<Symptom[]>([])
  const [uiSelectedTagIds, setUiSelectedTagIds] = useState<UUID[]>([])
  const [uiSelectedSymptomIds, setUiSelectedSymptomIds] = useState<UUID[]>([])

  useEffect(() => {
    const uploadWarning = (location.state as { uploadWarning?: string } | null)?.uploadWarning
    if (!uploadWarning) return
    setWarning(uploadWarning)
    navigate(location.pathname, { replace: true, state: null })
  }, [location.pathname, location.state, navigate])

  useEffect(() => {
    if (mode === 'create') setEntryReadOnly(false)
  }, [mode])

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
          note: entry.note ?? ''
        })
        setEntryReadOnly(entry.isCompleted)
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
      sleepQualityScore: LEGACY_SLEEP_QUALITY_SCORE,
      note: form.note.trim() ? form.note : null,
      isCompleted: false,
      tagIds: Array.from(new Set(uiSelectedTagIds)),
      symptomIds: Array.from(new Set(uiSelectedSymptomIds))
    }
  }, [form, uiSelectedTagIds, uiSelectedSymptomIds])

  const saveEntry = async (finalPayload: DiaryEntryUpsertPayload): Promise<DiaryEntry> => {
    if (mode === 'edit') {
      if (!entryId) throw new Error('Entry id is missing')
      return updateDiaryEntry(entryId, finalPayload)
    }
    return createDiaryEntry(finalPayload)
  }

  const submit = async () => {
    if (entryReadOnly) return
    setError(null)
    setWarning(null)
    const finalPayload: DiaryEntryUpsertPayload = payload

    setSaving(true)
    try {
      const saved = await saveEntry(finalPayload)
      if (selectedPhotos.length > 0) {
        try {
          await uploadDiaryEntryPhotos(saved.id, selectedPhotos)
        } catch (e) {
          const uploadWarning = e instanceof Error ? e.message : 'Запись сохранена, но фото не загрузились'
          navigate(`/diary/entry/${saved.id}`, { state: { uploadWarning } })
          return
        }
      }
      navigate(`/diary/entry/${saved.id}`)
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
          Оцените эмоции по шкале от 1 до 10 и оставьте пару строк. За один день можно создать несколько записей. Редактирование доступно в течение трёх дней.
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
          {entryReadOnly ? (
            <div className="rounded-2xl border border-amber-300/80 bg-amber-50/90 p-4 text-sm text-amber-950 dark:border-amber-700 dark:bg-amber-950/40 dark:text-amber-100">
              Запись зафиксирована: прошло больше трёх дней с даты записи. Редактирование и новые фото недоступны.
            </div>
          ) : null}

          <div className="grid grid-cols-1 gap-4">
            <label className="space-y-2">
              <span className="journal-eyebrow">Дата</span>
              <input
                className="journal-input w-full"
                type="date"
                value={form.entryDate}
                onChange={(e) => setForm((s) => ({ ...s, entryDate: e.target.value }))}
                disabled={entryReadOnly}
                required
              />
            </label>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <ScaleField
              fieldId="mood"
              label="Настроение"
              value={form.moodScore}
              onChange={(v) => setForm((s) => ({ ...s, moodScore: v }))}
              disabled={entryReadOnly}
            />
            <ScaleField
              fieldId="energy"
              label="Энергия"
              value={form.energyScore}
              onChange={(v) => setForm((s) => ({ ...s, energyScore: v }))}
              disabled={entryReadOnly}
            />
            <ScaleField
              fieldId="productivity"
              label="Продуктивность"
              value={form.productivityScore}
              onChange={(v) => setForm((s) => ({ ...s, productivityScore: v }))}
              disabled={entryReadOnly}
            />
            <ScaleField
              fieldId="stress"
              label="Стресс"
              value={form.stressScore}
              onChange={(v) => setForm((s) => ({ ...s, stressScore: v }))}
              disabled={entryReadOnly}
            />
          </div>

          <label className="block space-y-2">
            <span className="journal-eyebrow">Заметка</span>
            <textarea
              className="journal-input min-h-40 w-full p-4 text-lg leading-relaxed"
              value={form.note}
              onChange={(e) => setForm((s) => ({ ...s, note: e.target.value }))}
              maxLength={10000}
              disabled={entryReadOnly}
              placeholder="Что произошло сегодня? Какие детали хочется сохранить?"
            />
          </label>

          <div>
            <div className="journal-eyebrow">Метки состояния</div>
            <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">
              Выберите теги и симптомы. Управление списками — в разделе «Настройки».
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
                        disabled={entryReadOnly}
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
                        disabled={entryReadOnly}
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
              disabled={entryReadOnly}
              onChange={(e) => {
                const input = e.currentTarget
                const files = input.files
                if (!files || files.length === 0) {
                  setSelectedPhotos([])
                  return
                }
                const picked = Array.from(files)
                setPreparingPhotos(true)
                void compressImageFiles(picked)
                  .then((compressed) => {
                    setSelectedPhotos(compressed)
                  })
                  .catch(() => {
                    setSelectedPhotos(picked)
                  })
                  .finally(() => {
                    setPreparingPhotos(false)
                  })
              }}
            />
            <div className="text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">
              {preparingPhotos
                ? 'Подготовка фото…'
                : selectedPhotos.length > 0
                  ? `Выбрано файлов: ${selectedPhotos.length}`
                  : 'Необязательно. Фото автоматически сжимаются для быстрой загрузки.'}
            </div>
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
              disabled={saving || telegramSending || entryReadOnly}
            >
              {telegramSending ? 'Отправка…' : '📤 В Telegram'}
            </button>
            <button
              type="button"
              className="journal-btn-secondary min-h-[44px]"
              onClick={() => navigate('/diary/entry/new')}
              disabled={saving || entryReadOnly}
            >
              Отмена
            </button>
            <button
              type="submit"
              className="journal-btn-primary min-h-[44px] disabled:opacity-50"
              disabled={saving || entryReadOnly || preparingPhotos}
            >
              {preparingPhotos ? 'Подготовка фото…' : saving ? 'Сохранение…' : mode === 'create' ? 'Создать запись' : 'Сохранить'}
            </button>
          </div>
        </form>
      )}
    </div>
  )
}

const SCORE_OPTIONS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] as const

function ScaleField({
  fieldId,
  label,
  value,
  onChange,
  disabled
}: {
  fieldId: string
  label: string
  value: number
  onChange: (v: number) => void
  disabled?: boolean
}) {
  const percent = Math.max(0, Math.min(100, (value / 10) * 100))
  const selectId = React.useId()

  return (
    <div className="journal-card block space-y-3 p-4">
      <div className="flex items-center justify-between gap-3">
        <label htmlFor={selectId} className="font-heading text-sm font-semibold text-journal-ink dark:text-journalDark-ink">
          {label}
        </label>
        <span className="rounded-full bg-journal-fold px-3 py-1 font-heading text-sm text-journal-accent dark:bg-journalDark-fold dark:text-journalDark-accent">
          {value}/10
        </span>
      </div>
      <select
        id={selectId}
        data-testid={`diary-scale-${fieldId}`}
        className="journal-input min-h-[44px] w-full"
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        disabled={disabled}
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
    </div>
  )
}

