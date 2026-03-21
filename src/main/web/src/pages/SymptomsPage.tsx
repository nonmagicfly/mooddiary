import React, { useEffect, useState } from 'react'
import { createSymptom, deleteSymptom, getSymptoms, updateSymptom } from '../api/api'
import { Symptom, SymptomUpsertPayload } from '../api/types'

export default function SymptomsPage() {
  const [symptoms, setSymptoms] = useState<Symptom[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const [editingId, setEditingId] = useState<Symptom['id'] | null>(null)
  const [name, setName] = useState('')

  const refresh = async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await getSymptoms()
      setSymptoms(res ?? [])
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Ошибка загрузки симптомов')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void refresh()
  }, [])

  const submit = async () => {
    setError(null)
    const trimmedName = name.trim()
    if (!trimmedName) {
      setError('Укажите название')
      return
    }

    const payload: SymptomUpsertPayload = { name: trimmedName }

    setSaving(true)
    try {
      if (editingId) {
        await updateSymptom(editingId, payload)
      } else {
        await createSymptom(payload)
      }
      setEditingId(null)
      setName('')
      await refresh()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Ошибка сохранения симптома')
    } finally {
      setSaving(false)
    }
  }

  const startEdit = (s: Symptom) => {
    setEditingId(s.id)
    setName(s.name)
  }

  const cancelEdit = () => {
    setEditingId(null)
    setName('')
    setError(null)
  }

  return (
    <div className="mx-auto max-w-3xl">
      <div className="mb-6">
        <h1 className="font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">Симптомы</h1>
        <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Создание, редактирование и удаление симптомов</div>
      </div>

      <form
        className="journal-card space-y-3 p-4"
        onSubmit={(e) => {
          e.preventDefault()
          void submit()
        }}
      >
        <label className="space-y-1">
          <span className="text-sm">Название симптома</span>
          <input
            aria-label="Symptom name"
            className="journal-input w-full"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="например: боль"
          />
        </label>

        {error ? <div className="rounded border border-amber-300 bg-amber-50/80 p-3 text-sm text-amber-800 dark:border-amber-700 dark:bg-amber-950/50 dark:text-amber-200">{error}</div> : null}

        <div className="flex items-center justify-end gap-3 pt-2">
          {editingId ? (
            <button type="button" className="journal-btn-secondary py-2" onClick={cancelEdit} disabled={saving}>
              Отмена
            </button>
          ) : null}
          <button type="submit" className="journal-btn-primary disabled:opacity-50" disabled={saving}>
            {saving ? 'Сохранение…' : editingId ? 'Обновить симптом' : 'Создать симптом'}
          </button>
        </div>
      </form>

      <div className="mt-5">
        <div className="mb-2 flex items-center justify-between">
          <div className="font-heading text-sm font-medium">Существующие симптомы</div>
          <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">{loading ? '' : `${symptoms.length}`}</div>
        </div>

        {loading ? (
          <div className="journal-card p-3 text-sm">Загрузка…</div>
        ) : symptoms.length === 0 ? (
          <div className="journal-card p-3 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Симптомов пока нет.</div>
        ) : (
          <div className="space-y-2">
            {symptoms.map((s) => (
              <div key={s.id} className="journal-card flex items-center justify-between gap-3 p-3">
                <div>
                  <div className="text-sm font-medium">{s.name}</div>
                </div>

                <div className="flex items-center gap-2">
                  <button type="button" className="journal-btn-secondary text-sm" onClick={() => startEdit(s)} disabled={saving}>
                    Изменить
                  </button>
                  <button
                    type="button"
                    className="rounded-sm border border-amber-400 bg-amber-100 px-3 py-1 text-sm text-amber-900 dark:border-amber-700 dark:bg-amber-950/50 dark:text-amber-200"
                    onClick={async () => {
                      const ok = window.confirm(`Удалить симптом «${s.name}»?`)
                      if (!ok) return
                      setSaving(true)
                      setError(null)
                      try {
                        await deleteSymptom(s.id)
                        await refresh()
                      } catch (e) {
                        setError(e instanceof Error ? e.message : 'Ошибка удаления симптома')
                      } finally {
                        setSaving(false)
                      }
                    }}
                    disabled={saving}
                  >
                    Удалить
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

