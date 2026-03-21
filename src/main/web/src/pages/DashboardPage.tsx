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

function scoreCorrLabel(key: keyof MoodAnalyticsResponse['correlations']): string {
  if (key === 'sleepToMood') return 'Сон → Настроение (корр.)'
  if (key === 'sleepToEnergy') return 'Сон → Энергия (корр.)'
  return 'Стресс → Продуктивность (корр.)'
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
    <div className="mx-auto max-w-5xl">
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">Обзор</h1>
          <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Сегодня: {todayIso}</div>
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

          <div className="journal-card p-4 text-sm">
            Заполнено дней: {analytics?.completedDaysCount ?? 0}
          </div>

          <div className="journal-card p-4">
            <div className="font-heading text-sm font-medium text-journal-ink dark:text-journalDark-ink">Корреляции</div>
            <div className="journal-divider mt-2 space-y-2 pt-2">
              {correlations ? (
                (Object.keys(correlations) as (keyof MoodAnalyticsResponse['correlations'])[]).map((k) => (
                  <div key={k} className="flex items-center justify-between gap-3">
                    <div className="text-journal-inkMuted dark:text-journalDark-inkMuted">{scoreCorrLabel(k)}</div>
                    <div className="font-medium">{formatScore(correlations[k] ?? null)}</div>
                  </div>
                ))
              ) : (
                <div className="text-journal-inkMuted dark:text-journalDark-inkMuted">—</div>
              )}
            </div>
          </div>

          <div className="journal-card p-4">
            <div className="flex items-center justify-between gap-3">
              <div className="font-heading text-sm font-medium text-journal-ink dark:text-journalDark-ink">Последние записи</div>
              <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Показано: {latestEntries.length}</div>
            </div>

            {latestEntries.length === 0 ? (
              <div className="journal-divider mt-3 pt-3 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Записей нет.</div>
            ) : (
              <div className="journal-divider mt-3 space-y-2 pt-3">
                {latestEntries.map((e) => (
                  <button
                    key={e.id}
                    type="button"
                    className="w-full rounded-sm border border-journal-line p-3 text-left transition-colors hover:bg-journal-fold dark:border-journalDark-line dark:hover:bg-journalDark-fold"
                    onClick={() => navigate(`/diary/entry/${e.id}`)}
                  >
                    <div className="flex items-center justify-between gap-3">
                      <div className="text-sm font-medium">{e.entryDate}</div>
                      <div className="text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">
                        настроение {e.moodScore} / энергия {e.energyScore} / продуктивность {e.productivityScore}
                      </div>
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
    <div className="journal-card p-4">
      <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">{title}</div>
      <div className="mt-2 font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">{formatScore(value)}</div>
    </div>
  )
}

