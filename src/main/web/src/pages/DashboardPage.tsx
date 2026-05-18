import React, { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAnalyticsDaily, listDiaryEntries, sendSummaryToTelegram } from '../api/api'
import { DiaryEntry, MoodAnalyticsResponse } from '../api/types'

function isoDate(d: Date): string {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatScore(v: number | null | undefined): string {
  if (v === null || v === undefined) return '—'
  return v.toFixed(2)
}

export default function DashboardPage() {
  const navigate = useNavigate()

  const today = useMemo(() => new Date(), [])
  const todayIso = useMemo(() => isoDate(today), [today])

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [telegramSending, setTelegramSending] = useState(false)
  const [telegramError, setTelegramError] = useState<string | null>(null)

  const [analytics, setAnalytics] = useState<MoodAnalyticsResponse | null>(null)
  const [latestEntries, setLatestEntries] = useState<DiaryEntry[]>([])

  useEffect(() => {
    setLoading(true)
    setError(null)

    void (async () => {
      try {
        const from = isoDate(new Date(today.getTime() - 60 * 86400000))

        const [a, entries] = await Promise.all([
          getAnalyticsDaily(todayIso),
          listDiaryEntries({ from, to: todayIso, limit: 5 })
        ])

        setAnalytics(a)
        setLatestEntries(entries)
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Ошибка загрузки обзора')
      } finally {
        setLoading(false)
      }
    })()
  }, [today, todayIso])

  const correlations = analytics?.correlations ?? null

  const handleSendToTelegram = async () => {
    setTelegramError(null)
    setTelegramSending(true)
    try {
      await sendSummaryToTelegram(todayIso)
    } catch (e) {
      setTelegramError(e instanceof Error ? e.message : 'Ошибка отправки')
    } finally {
      setTelegramSending(false)
    }
  }

  return (
    <div className="journal-page">
      <div className="journal-card mb-6 overflow-hidden p-6 md:p-8">
        <div className="flex flex-wrap items-start justify-between gap-6">
        <div>
            <div className="journal-eyebrow">Обзор дня</div>
            <h1 className="journal-title mt-3">Обзор</h1>
            <p className="journal-subtitle">
              Как звучит сегодняшний день? Отметьте состояние, вернитесь к последним страницам и найдите мягкие закономерности без лишнего шума.
            </p>
            <div className="mt-4 flex flex-wrap gap-2">
              <span className="journal-chip">Сегодня: {todayIso}</span>
              <span className="journal-chip">Заполнено дней: {analytics?.completedDaysCount ?? 0}</span>
            </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
            <button type="button" className="journal-btn-primary text-sm" onClick={() => navigate('/diary/entry/new')}>
            Новая запись
          </button>
          <button
            type="button"
            className="journal-btn-secondary text-sm disabled:opacity-50"
            onClick={handleSendToTelegram}
            disabled={telegramSending}
            title="Отправить саммари за сегодня в Telegram"
          >
            {telegramSending ? 'Отправка…' : '📤 В Telegram'}
          </button>
        </div>
      </div>
      </div>

      {error ? <div className="journal-card border-amber-300 bg-amber-50/80 p-3 text-sm text-amber-900 dark:border-amber-800 dark:bg-amber-950/30 dark:text-amber-200">{error}</div> : null}
      {telegramError ? <div className="journal-card border-amber-300 bg-amber-50/80 p-3 text-sm text-amber-900 dark:border-amber-800 dark:bg-amber-950/30 dark:text-amber-200">{telegramError}</div> : null}

      {loading ? (
        <div className="journal-card p-4 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Загрузка…</div>
      ) : (
        <div className="space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <MetricCard title="Сред. настроение" value={analytics?.avgMoodScore ?? null} />
            <MetricCard title="Сред. энергия" value={analytics?.avgEnergyScore ?? null} />
            <MetricCard title="Сред. продуктивность" value={analytics?.avgProductivityScore ?? null} />
          </div>

          <div className="journal-card p-5">
            <div className="flex items-center justify-between gap-3">
              <div>
                <div className="journal-eyebrow">Связи</div>
                <div className="mt-1 font-heading text-xl font-semibold text-journal-ink dark:text-journalDark-ink">Корреляции</div>
              </div>
              <span className="journal-chip">наблюдения</span>
            </div>
            <div className="journal-divider mt-2 space-y-2 pt-2">
              {correlations ? (
                (['stressToProductivity'] as const).map((k) => (
                  <div key={k} className="flex items-center justify-between gap-3">
                    <div className="text-journal-inkMuted dark:text-journalDark-inkMuted">Стресс → Продуктивность (корр.)</div>
                    <div className="font-medium">{formatScore(correlations[k] ?? null)}</div>
                  </div>
                ))
              ) : (
                <div className="text-journal-inkMuted dark:text-journalDark-inkMuted">—</div>
              )}
            </div>
          </div>

          <div className="journal-card p-5">
            <div className="flex items-center justify-between gap-3">
              <div>
                <div className="journal-eyebrow">Архив</div>
                <div className="mt-1 font-heading text-xl font-semibold text-journal-ink dark:text-journalDark-ink">Последние записи</div>
              </div>
              <div className="journal-chip">Показано: {latestEntries.length}</div>
            </div>

            {latestEntries.length === 0 ? (
              <div className="journal-divider mt-3 pt-3 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Записей нет.</div>
            ) : (
              <div className="journal-divider mt-4 grid gap-3 pt-4 md:grid-cols-2">
                {latestEntries.map((e) => (
                  <button
                    key={e.id}
                    type="button"
                    className="w-full rounded-2xl border border-journal-line/80 bg-journal-paper/60 p-4 text-left transition-all hover:-translate-y-0.5 hover:bg-journal-fold/70 hover:shadow-paperFold dark:border-journalDark-line/80 dark:bg-journalDark-paper/60 dark:hover:bg-journalDark-fold"
                    onClick={() => navigate(`/diary/entry/${e.id}`)}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="font-heading text-lg font-semibold">{e.entryDate}</div>
                      <div className="text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">
                        {e.isCompleted ? 'зафиксировано' : 'можно править'}
                      </div>
                    </div>
                    <div className="mt-2 flex flex-wrap gap-2 text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">
                      <span className="journal-chip">настроение {e.moodScore}</span>
                      <span className="journal-chip">энергия {e.energyScore}</span>
                      <span className="journal-chip">фокус {e.productivityScore}</span>
                    </div>
                    {e.note ? <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">{e.note.slice(0, 120)}{e.note.length > 120 ? '...' : null}</div> : null}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

function MetricCard({ title, value }: { title: string; value: number | null }) {
  return (
    <div className="journal-card overflow-hidden p-5">
      <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">{title}</div>
      <div className="mt-2 font-heading text-4xl font-semibold text-journal-ink dark:text-journalDark-ink">{formatScore(value)}</div>
      <div className="mt-4 h-1.5 overflow-hidden rounded-full bg-journal-line/70 dark:bg-journalDark-line">
        <div
          className="h-full rounded-full bg-journal-accent dark:bg-journalDark-accent"
          style={{ width: `${Math.max(0, Math.min(100, ((value ?? 0) / 10) * 100))}%` }}
        />
      </div>
    </div>
  )
}

