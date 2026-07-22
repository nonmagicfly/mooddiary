import React, { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAnalyticsMonthly, getAnalyticsWeekly, getSymptoms, listDiaryEntries } from '../api/api'
import { DiaryEntry, MoodAnalyticsResponse, Symptom } from '../api/types'

function isoDate(d: Date): string {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function toSet(values: string[]) {
  return new Set(values)
}

export default function DiaryHistoryPage() {
  const navigate = useNavigate()

  type ViewMode = 'list' | 'calendar' | 'weekly' | 'monthly'
  const [viewMode, setViewMode] = useState<ViewMode>('list')

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [fromDate, setFromDate] = useState(() => {
    const d = new Date()
    d.setDate(d.getDate() - 30)
    return isoDate(d)
  })
  const [toDate, setToDate] = useState(() => isoDate(new Date()))
  const [limit, setLimit] = useState(200)

  const [symptoms, setSymptoms] = useState<Symptom[]>([])

  const [selectedSymptomIds, setSelectedSymptomIds] = useState<string[]>([])

  const [entries, setEntries] = useState<DiaryEntry[]>([])

  useEffect(() => {
    void (async () => {
      setLoading(true)
      setError(null)
      try {
        const symptomsRes = await getSymptoms()
        setSymptoms(symptomsRes)
        const res = await listDiaryEntries({ from: fromDate, to: toDate, limit })
        setEntries(res)
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Ошибка загрузки истории')
      } finally {
        setLoading(false)
      }
    })()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const filteredEntries = useMemo(() => {
    const symptomFilter = selectedSymptomIds.length > 0 ? toSet(selectedSymptomIds) : null

    return entries
      .filter((e) => {
        if (symptomFilter) {
          const entrySymptoms = toSet(e.symptomIds)
          let ok = false
          for (const id of symptomFilter) {
            if (entrySymptoms.has(id)) {
              ok = true
              break
            }
          }
          if (!ok) return false
        }

        return true
      })
      .sort((a, b) => {
        if (a.entryDate !== b.entryDate) return a.entryDate < b.entryDate ? 1 : -1
        return a.createdAt < b.createdAt ? 1 : -1
      })
  }, [entries, selectedSymptomIds])

  const applyDateRange = async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await listDiaryEntries({ from: fromDate, to: toDate, limit })
      setEntries(res)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Ошибка загрузки истории')
    } finally {
      setLoading(false)
    }
  }

  // Calendar / analytics states are loaded on-demand based on `viewMode`.
  const [calendarMonth, setCalendarMonth] = useState(() => {
    const d = new Date()
    return new Date(d.getFullYear(), d.getMonth(), 1)
  })
  const [calendarLoading, setCalendarLoading] = useState(false)
  const [calendarError, setCalendarError] = useState<string | null>(null)
  const [calendarEntries, setCalendarEntries] = useState<DiaryEntry[]>([])

  const monthRange = useMemo(() => {
    const year = calendarMonth.getFullYear()
    const month = calendarMonth.getMonth()

    const firstOfMonth = new Date(year, month, 1)
    const firstDay = firstOfMonth.getDay() // 0..6 (Sun..Sat)
    const mondayFirstOffset = (firstDay + 6) % 7 // Mon=0..Sun=6
    const gridStart = new Date(year, month, 1 - mondayFirstOffset)
    const gridEnd = new Date(gridStart)
    gridEnd.setDate(gridStart.getDate() + 41)

    return { gridStart, gridEnd }
  }, [calendarMonth])

  const dayCells = useMemo(() => {
    const cells: string[] = []
    const d = new Date(monthRange.gridStart)
    for (let i = 0; i < 42; i++) {
      cells.push(isoDate(d))
      d.setDate(d.getDate() + 1)
    }
    return cells
  }, [monthRange.gridStart])

  useEffect(() => {
    if (viewMode !== 'calendar') return
    setCalendarLoading(true)
    setCalendarError(null)
    void (async () => {
      try {
        const res = await listDiaryEntries({
          from: isoDate(monthRange.gridStart),
          to: isoDate(monthRange.gridEnd),
          limit: 200
        })
        setCalendarEntries(res)
      } catch (e) {
        setCalendarError(e instanceof Error ? e.message : 'Ошибка загрузки календаря')
      } finally {
        setCalendarLoading(false)
      }
    })()
  }, [viewMode, monthRange.gridEnd, monthRange.gridStart])

  const calendarEntryByDate = useMemo(() => {
    const map = new Map<string, DiaryEntry>()
    for (const e of calendarEntries) map.set(e.entryDate, e)
    return map
  }, [calendarEntries])

  const scoreToBg = (score: number) => {
    const s = Math.max(1, Math.min(10, score))
    const ratio = (s - 1) / 9
    const hue = 10 + ratio * 110 // red..green
    return `hsl(${hue}, 85%, 85%)`
  }

  const [overviewDate, setOverviewDate] = useState(() => new Date())
  const [overviewLoading, setOverviewLoading] = useState(false)
  const [overviewError, setOverviewError] = useState<string | null>(null)
  const [weeklyData, setWeeklyData] = useState<MoodAnalyticsResponse | null>(null)
  const [monthlyData, setMonthlyData] = useState<MoodAnalyticsResponse | null>(null)

  useEffect(() => {
    if (viewMode !== 'weekly' && viewMode !== 'monthly') return
    setOverviewLoading(true)
    setOverviewError(null)
    void (async () => {
      try {
        const dateIso = isoDate(overviewDate)
        if (viewMode === 'weekly') {
          const res = await getAnalyticsWeekly(dateIso)
          setWeeklyData(res)
          setMonthlyData(null)
        } else {
          const res = await getAnalyticsMonthly(dateIso)
          setMonthlyData(res)
          setWeeklyData(null)
        }
      } catch (e) {
        setOverviewError(e instanceof Error ? e.message : 'Ошибка загрузки обзора')
      } finally {
        setOverviewLoading(false)
      }
    })()
  }, [viewMode, overviewDate])

  return (
    <div className="mx-auto max-w-5xl">
      <div className="mb-6">
        <h1 className="font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">История дневника</h1>
        <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Фильтры по дате и симптомам</div>
        <div className="mt-3 flex flex-wrap gap-2">
          <button type="button" onClick={() => setViewMode('list')} className={`rounded-sm border px-3 py-1 text-sm ${viewMode === 'list' ? 'border-journal-accent bg-journal-accent text-white dark:border-journalDark-accent dark:bg-journalDark-accent' : 'journal-btn-secondary'}`}>
            Список
          </button>
          <button type="button" onClick={() => setViewMode('calendar')} className={`rounded-sm border px-3 py-1 text-sm ${viewMode === 'calendar' ? 'border-journal-accent bg-journal-accent text-white dark:border-journalDark-accent dark:bg-journalDark-accent' : 'journal-btn-secondary'}`}>
            Календарь
          </button>
          <button type="button" onClick={() => setViewMode('weekly')} className={`rounded-sm border px-3 py-1 text-sm ${viewMode === 'weekly' ? 'border-journal-accent bg-journal-accent text-white dark:border-journalDark-accent dark:bg-journalDark-accent' : 'journal-btn-secondary'}`}>
            Неделя
          </button>
          <button type="button" onClick={() => setViewMode('monthly')} className={`rounded-sm border px-3 py-1 text-sm ${viewMode === 'monthly' ? 'border-journal-accent bg-journal-accent text-white dark:border-journalDark-accent dark:bg-journalDark-accent' : 'journal-btn-secondary'}`}>
            Месяц
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        {viewMode === 'list' ? (
          <>
        <div className="journal-card p-4 lg:col-span-1">
          <div className="font-heading text-sm font-medium">Период</div>
          <div className="mt-3 space-y-3">
            <label className="block space-y-1">
              <span className="text-sm">С</span>
              <input className="journal-input w-full" type="date" value={fromDate} onChange={(e) => setFromDate(e.target.value)} />
            </label>
            <label className="block space-y-1">
              <span className="text-sm">По</span>
              <input className="journal-input w-full" type="date" value={toDate} onChange={(e) => setToDate(e.target.value)} />
            </label>
            <label className="block space-y-1">
              <span className="text-sm">Лимит</span>
              <input
                className="journal-input w-full"
                type="number"
                value={limit}
                min={1}
                max={200}
                onChange={(e) => setLimit(Number(e.target.value))}
              />
            </label>
            <button type="button" className="journal-btn-primary w-full" onClick={() => void applyDateRange()} disabled={loading}>
              {loading ? 'Загрузка…' : 'Применить'}
            </button>
          </div>
        </div>

        <div className="journal-card p-4 lg:col-span-2">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <div className="font-heading text-sm font-medium">Быстрые фильтры</div>
              <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">По симптомам</div>
            </div>
            <button type="button" className="journal-btn-secondary" onClick={() => navigate('/diary/entry/new')}>
              Новая запись
            </button>
          </div>

          <div className="mt-4 grid grid-cols-1 gap-4">
            <div className="space-y-2">
              <div className="font-heading text-sm font-medium">Симптомы</div>
              {symptoms.length === 0 ? <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Нет симптомов</div> : null}
              <div className="flex flex-wrap gap-3">
                {symptoms.map((s) => {
                  const checked = selectedSymptomIds.includes(s.id)
                  return (
                    <label key={s.id} className="flex items-center gap-2 text-sm">
                      <input
                        type="checkbox"
                        checked={checked}
                        onChange={(e) => {
                          setSelectedSymptomIds((prev) => {
                            const next = e.target.checked ? [...prev, s.id] : prev.filter((x) => x !== s.id)
                            return Array.from(new Set(next))
                          })
                        }}
                      />
                      <span>{s.name}</span>
                    </label>
                  )
                })}
              </div>
            </div>
          </div>

          {error ? <div className="mt-4 rounded border border-amber-300 bg-amber-50/80 p-3 text-sm text-amber-800 dark:border-amber-700 dark:bg-amber-950/50 dark:text-amber-200">{error}</div> : null}

          <div className="mt-4 space-y-2">
            <div className="flex items-center justify-between">
              <div className="font-heading text-sm font-medium">Записи</div>
              <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Найдено: {filteredEntries.length}</div>
            </div>

            {loading ? (
              <div className="journal-card p-3 text-sm">Загрузка…</div>
            ) : filteredEntries.length === 0 ? (
              <div className="journal-card p-3 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">По текущим фильтрам ничего не найдено.</div>
            ) : (
              <div className="space-y-2">
                {filteredEntries.slice(0, 50).map((e) => (
                  <button
                    key={e.id}
                    type="button"
                    className="journal-card w-full p-3 text-left transition-colors hover:bg-journal-fold/50 dark:hover:bg-journalDark-fold/50"
                    onClick={() => navigate(`/diary/entry/${e.id}`)}
                  >
                    <div className="flex items-center justify-between gap-3">
                      <div className="text-sm font-medium">{e.entryDate}</div>
                      <div className="text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">настроение {e.moodScore} / энергия {e.energyScore} / продуктивность {e.productivityScore}</div>
                    </div>
                    <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">
                      {e.note ? e.note.slice(0, 140) : 'Без заметки'}
                      {e.note && e.note.length > 140 ? '...' : null}
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>
      </>
      ) : null}
      {viewMode === 'calendar' ? (
        <div className="journal-card p-4 lg:col-span-3">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <div className="font-heading text-sm font-medium">Календарь</div>
              <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">
                {calendarMonth.toLocaleString('ru-RU', { month: 'long', year: 'numeric' })}
              </div>
            </div>
            <div className="flex items-center gap-2">
              <button
                type="button"
                className="journal-btn-secondary text-sm"
                onClick={() => setCalendarMonth((d) => new Date(d.getFullYear(), d.getMonth() - 1, 1))}
              >
                Назад
              </button>
              <button
                type="button"
                className="journal-btn-secondary text-sm"
                onClick={() => setCalendarMonth(() => new Date())}
              >
                Сегодня
              </button>
              <button
                type="button"
                className="journal-btn-secondary text-sm"
                onClick={() => setCalendarMonth((d) => new Date(d.getFullYear(), d.getMonth() + 1, 1))}
              >
                Вперёд
              </button>
            </div>
          </div>

          {calendarLoading ? (
            <div className="journal-card mt-4 p-3 text-sm">Загрузка…</div>
          ) : calendarError ? (
            <div className="mt-4 rounded border border-amber-300 bg-amber-50/80 p-3 text-sm text-amber-800 dark:border-amber-700 dark:bg-amber-950/50 dark:text-amber-200">{calendarError}</div>
          ) : (
            <>
              <div className="mt-4 grid grid-cols-7 gap-2 text-center text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">
                {['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'].map((d) => (
                  <div key={d}>{d}</div>
                ))}
              </div>

              <div className="mt-2 grid grid-cols-7 gap-2">
                {dayCells.map((iso, idx) => {
                  const entry = calendarEntryByDate.get(iso)
                  const cellDate = new Date(monthRange.gridStart)
                  cellDate.setDate(cellDate.getDate() + idx)
                  const isCurrentMonth = cellDate.getMonth() === calendarMonth.getMonth()

                  return (
                    <button
                      key={iso}
                      type="button"
                      className={`journal-card h-12 p-1 text-left text-xs ${isCurrentMonth ? '' : 'opacity-70'}`}
                      style={entry ? { backgroundColor: scoreToBg(entry.moodScore) } : undefined}
                      onClick={() => {
                        if (!entry) {
                          navigate('/diary/entry/new')
                          return
                        }
                        navigate(`/diary/entry/${entry.id}`)
                      }}
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div className={isCurrentMonth ? 'font-medium' : 'font-medium opacity-60'}>{cellDate.getDate()}</div>
                        {entry ? <div className="text-[11px] font-semibold text-journal-ink dark:text-journalDark-ink">{entry.moodScore}</div> : <div />}
                      </div>
                    </button>
                  )
                })}
              </div>
            </>
          )}
        </div>
      ) : null}

      {viewMode === 'weekly' ? (
        <div className="journal-card p-4 lg:col-span-3">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <div className="font-heading text-sm font-medium">Обзор за неделю</div>
              <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Неделя с {isoDate(overviewDate)}</div>
            </div>
            <div className="flex items-center gap-2">
              <button type="button" className="journal-btn-secondary text-sm" onClick={() => setOverviewDate((d) => new Date(d.getTime() - 7 * 86400000))}>
                Назад
              </button>
              <button type="button" className="journal-btn-secondary text-sm" onClick={() => setOverviewDate(() => new Date())}>
                Сегодня
              </button>
              <button type="button" className="journal-btn-secondary text-sm" onClick={() => setOverviewDate((d) => new Date(d.getTime() + 7 * 86400000))}>
                Вперёд
              </button>
            </div>
          </div>

          {overviewLoading ? (
            <div className="journal-card mt-4 p-3 text-sm">Загрузка…</div>
          ) : overviewError ? (
            <div className="mt-4 rounded border border-amber-300 bg-amber-50/80 p-3 text-sm text-amber-800 dark:border-amber-700 dark:bg-amber-950/50 dark:text-amber-200">{overviewError}</div>
          ) : weeklyData ? (
            <div className="mt-4 space-y-4">
              <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                <div className="journal-card p-4">
                  <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Сред. настроение</div>
                  <div className="mt-2 font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">{weeklyData.avgMoodScore === null ? '—' : weeklyData.avgMoodScore.toFixed(2)}</div>
                </div>
                <div className="journal-card p-4">
                  <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Сред. энергия</div>
                  <div className="mt-2 font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">{weeklyData.avgEnergyScore === null ? '—' : weeklyData.avgEnergyScore.toFixed(2)}</div>
                </div>
                <div className="journal-card p-4">
                  <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Сред. продуктивность</div>
                  <div className="mt-2 font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">{weeklyData.avgProductivityScore === null ? '—' : weeklyData.avgProductivityScore.toFixed(2)}</div>
                </div>
              </div>

              <div className="journal-card p-4 text-sm">Заполнено дней: {weeklyData.completedDaysCount}</div>

              <div className="journal-card p-4">
                <div className="font-heading text-sm font-medium">Динамика</div>
                <div className="mt-2 space-y-2">
                  {weeklyData.series && weeklyData.series.length > 0 ? (
                    weeklyData.series.map((p) => (
                      <div key={p.entryDate} className="flex items-center justify-between gap-3 text-sm">
                        <div className="text-journal-ink dark:text-journalDark-ink">{p.entryDate}</div>
                        <div className="text-journal-inkMuted dark:text-journalDark-inkMuted">настроение {p.moodScore} / энергия {p.energyScore} / продуктивность {p.productivityScore}</div>
                      </div>
                    ))
                  ) : (
                    <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Нет заполненных записей.</div>
                  )}
                </div>
              </div>
            </div>
          ) : null}
        </div>
      ) : null}

      {viewMode === 'monthly' ? (
        <div className="journal-card p-4 lg:col-span-3">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <div className="font-heading text-sm font-medium">Обзор за месяц</div>
              <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">{isoDate(overviewDate).slice(0, 7)}</div>
            </div>
            <div className="flex items-center gap-2">
              <button
                type="button"
                className="journal-btn-secondary text-sm"
                onClick={() => setOverviewDate((d) => new Date(d.getFullYear(), d.getMonth() - 1, 1))}
              >
                Назад
              </button>
              <button type="button" className="journal-btn-secondary text-sm" onClick={() => setOverviewDate(() => new Date())}>
                Сегодня
              </button>
              <button
                type="button"
                className="journal-btn-secondary text-sm"
                onClick={() => setOverviewDate((d) => new Date(d.getFullYear(), d.getMonth() + 1, 1))}
              >
                Вперёд
              </button>
            </div>
          </div>

          {overviewLoading ? (
            <div className="journal-card mt-4 p-3 text-sm">Загрузка…</div>
          ) : overviewError ? (
            <div className="mt-4 rounded border border-amber-300 bg-amber-50/80 p-3 text-sm text-amber-800 dark:border-amber-700 dark:bg-amber-950/50 dark:text-amber-200">{overviewError}</div>
          ) : monthlyData ? (
            <div className="mt-4 space-y-4">
              <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                <div className="journal-card p-4">
                  <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Сред. настроение</div>
                  <div className="mt-2 font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">{monthlyData.avgMoodScore === null ? '—' : monthlyData.avgMoodScore.toFixed(2)}</div>
                </div>
                <div className="journal-card p-4">
                  <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Сред. энергия</div>
                  <div className="mt-2 font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">{monthlyData.avgEnergyScore === null ? '—' : monthlyData.avgEnergyScore.toFixed(2)}</div>
                </div>
                <div className="journal-card p-4">
                  <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Сред. продуктивность</div>
                  <div className="mt-2 font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">{monthlyData.avgProductivityScore === null ? '—' : monthlyData.avgProductivityScore.toFixed(2)}</div>
                </div>
              </div>

              <div className="journal-card p-4 text-sm">Заполнено дней: {monthlyData.completedDaysCount}</div>

              <div className="journal-card p-4">
                <div className="font-heading text-sm font-medium">Популярные теги</div>
                <div className="mt-2 flex flex-wrap gap-2">
                  {monthlyData.tagFrequencies && monthlyData.tagFrequencies.length > 0 ? (
                    monthlyData.tagFrequencies.slice(0, 10).map((t) => (
                      <div key={t.tagId} className="journal-card px-3 py-1 text-sm">
                        <div className="font-medium">{t.tagName}</div>
                        <div className="text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">
                          {t.count} {t.count === 1 ? 'запись' : t.count < 5 ? 'записи' : 'записей'}
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Нет тегов за период.</div>
                  )}
                </div>
              </div>
            </div>
          ) : null}
        </div>
      ) : null}

      </div>
    </div>
  )
}

