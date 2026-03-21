import React, { useEffect, useState } from 'react'
import { deleteTag, getTags, createTag, updateTag } from '../api/api'
import { Tag, TagUpsertPayload } from '../api/types'

export default function TagsPage() {
  const [tags, setTags] = useState<Tag[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const [editingId, setEditingId] = useState<Tag['id'] | null>(null)
  const [name, setName] = useState('')
  const [color, setColor] = useState('')

  const refresh = async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await getTags()
      setTags(res ?? [])
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Ошибка загрузки тегов')
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

    const payload: TagUpsertPayload = {
      name: trimmedName,
      color: color.trim()
    }

    setSaving(true)
    try {
      if (editingId) {
        await updateTag(editingId, payload)
      } else {
        await createTag(payload)
      }
      setEditingId(null)
      setName('')
      setColor('')
      await refresh()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Ошибка сохранения тега')
    } finally {
      setSaving(false)
    }
  }

  const startEdit = (t: Tag) => {
    setEditingId(t.id)
    setName(t.name)
    setColor(t.color ?? '')
  }

  const cancelEdit = () => {
    setEditingId(null)
    setName('')
    setColor('')
    setError(null)
  }

  return (
    <div className="mx-auto max-w-3xl">
      <div className="mb-6">
        <h1 className="font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">Теги</h1>
        <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Создание, редактирование и удаление тегов</div>
      </div>

      <form
        className="journal-card space-y-3 p-4"
        onSubmit={(e) => {
          e.preventDefault()
          void submit()
        }}
      >
        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
          <label className="space-y-1">
            <span className="text-sm">Название тега</span>
            <input
              aria-label="Tag name"
              className="journal-input w-full"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="например: работа"
            />
          </label>

          <label className="space-y-1">
            <span className="text-sm">Цвет (необязательно)</span>
            <div className="flex items-center gap-3">
              <div
                className="h-8 w-8 rounded border border-journal-line dark:border-journalDark-line"
                style={color ? { backgroundColor: color } : undefined}
              />
              <input
                aria-label="Tag color"
                className="journal-input w-full"
                value={color}
                onChange={(e) => setColor(e.target.value)}
                placeholder="#ff0000"
              />
            </div>
            <div className="text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">Любая строка цвета (CSS/hex)</div>
          </label>
        </div>

        {error ? <div className="rounded border border-amber-300 bg-amber-50/80 p-3 text-sm text-amber-800 dark:border-amber-700 dark:bg-amber-950/50 dark:text-amber-200">{error}</div> : null}

        <div className="flex items-center justify-end gap-3 pt-2">
          {editingId ? (
            <button type="button" className="journal-btn-secondary py-2" onClick={cancelEdit} disabled={saving}>
              Отмена
            </button>
          ) : null}

          <button type="submit" className="journal-btn-primary disabled:opacity-50" disabled={saving}>
            {saving ? 'Сохранение…' : editingId ? 'Обновить тег' : 'Создать тег'}
          </button>
        </div>
      </form>

      <div className="mt-5">
        <div className="mb-2 flex items-center justify-between">
          <div className="font-heading text-sm font-medium">Существующие теги</div>
          <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">{loading ? '' : `${tags.length}`}</div>
        </div>

        {loading ? (
          <div className="journal-card p-3 text-sm">Загрузка…</div>
        ) : tags.length === 0 ? (
          <div className="journal-card p-3 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Тегов пока нет.</div>
        ) : (
          <div className="space-y-2">
            {tags.map((t) => (
              <div key={t.id} className="journal-card flex items-center justify-between gap-3 p-3">
                <div className="flex items-center gap-3">
                  <div className="h-6 w-6 rounded border border-journal-line dark:border-journalDark-line" style={t.color ? { backgroundColor: t.color } : undefined} />
                  <div>
                    <div className="text-sm font-medium">{t.name}</div>
                    {t.color ? <div className="text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">{t.color}</div> : <div className="text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">Без цвета</div>}
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <button type="button" className="journal-btn-secondary text-sm" onClick={() => startEdit(t)} disabled={saving}>
                    Изменить
                  </button>
                  <button
                    type="button"
                    className="rounded-sm border border-amber-400 bg-amber-100 px-3 py-1 text-sm text-amber-900 dark:border-amber-700 dark:bg-amber-950/50 dark:text-amber-200"
                    onClick={async () => {
                      const ok = window.confirm(`Удалить тег «${t.name}»?`)
                      if (!ok) return
                      setSaving(true)
                      setError(null)
                      try {
                        await deleteTag(t.id)
                        await refresh()
                      } catch (e) {
                        setError(e instanceof Error ? e.message : 'Ошибка удаления тега')
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

