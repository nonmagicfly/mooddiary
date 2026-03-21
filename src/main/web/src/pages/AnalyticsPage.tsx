import React, { useEffect, useMemo, useState } from 'react'
import { getAnalyticsDaily, getAnalyticsMonthly, getAnalyticsWeekly } from '../api/api'
import { AnalyticsCorrelations, MoodAnalyticsResponse } from '../api/types'

type Period = 'daily' | 'weekly' | 'monthly'

function isoDate(d: Date): string {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function defaultCorrelations(): AnalyticsCorrelations {
  return { sleepToMood: null, sleepToEnergy: null, stressToProductivity: null }
}

export default function AnalyticsPage() {
  const [period, setPeriod] = useState<Period>('weekly')
  const [date] = useState(() => isoDate(new Date()))
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [data, setData] = useState<MoodAnalyticsResponse | null>(null)

  const fetcher = useMemo(() => {
    if (period === 'daily') return () => getAnalyticsDaily(date)
    if (period === 'monthly') return () => getAnalyticsMonthly(date)
    return () => getAnalyticsWeekly(date)
  }, [period, date])

  useEffect(() => {
    setLoading(true)
    setError(null)
    void fetcher()
      .then((res) => setData(res))
      .catch((e) => setError(e instanceof Error ? e.message : 'Ошибка загрузки аналитики'))
      .finally(() => setLoading(false))
  }, [fetcher])

  const correlations = data?.correlations ?? defaultCorrelations()

  return (
    <div className="mx-auto max-w-4xl">
      <div className="mb-6">
        <h1 className="font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">Аналитика</h1>
        <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Средние значения и корреляции</div>
      </div>

      <div className="mb-4 flex flex-wrap gap-2">
        <button
          type="button"
          className={`rounded-sm border px-3 py-1 text-sm ${period === 'daily' ? 'border-journal-accent bg-journal-accent text-white dark:border-journalDark-accent dark:bg-journalDark-accent' : 'journal-btn-secondary'}`}
          onClick={() => setPeriod('daily')}
        >
          День
        </button>
        <button
          type="button"
          className={`rounded-sm border px-3 py-1 text-sm ${period === 'weekly' ? 'border-journal-accent bg-journal-accent text-white dark:border-journalDark-accent dark:bg-journalDark-accent' : 'journal-btn-secondary'}`}
          onClick={() => setPeriod('weekly')}
        >
          Неделя
        </button>
        <button
          type="button"
          className={`rounded-sm border px-3 py-1 text-sm ${period === 'monthly' ? 'border-journal-accent bg-journal-accent text-white dark:border-journalDark-accent dark:bg-journalDark-accent' : 'journal-btn-secondary'}`}
          onClick={() => setPeriod('monthly')}
        >
          Месяц
        </button>
      </div>

      {loading ? (
        <div className="journal-card p-4">Загрузка…</div>
      ) : error ? (
        <div className="rounded border border-amber-300 bg-amber-50/80 p-3 text-sm text-amber-800 dark:border-amber-700 dark:bg-amber-950/50 dark:text-amber-200">{error}</div>
      ) : data ? (
        <div className="space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <MetricCard title="Сред. настроение" value={data.avgMoodScore} />
            <MetricCard title="Сред. энергия" value={data.avgEnergyScore} />
            <MetricCard title="Сред. продуктивность" value={data.avgProductivityScore} />
          </div>

          <div className="journal-card p-4">
            <div className="font-heading text-sm font-medium">Динамика (по дням)</div>
            <div className="journal-card mt-3 p-3">
              <SeriesLineChart
                points={data.series}
                height={140}
              />
            </div>
          </div>

          <div className="journal-card p-4">
            <div className="font-heading text-sm font-medium">Средние значения</div>
            <div className="mt-3 space-y-3">
              <BarRow
                label="Настроение"
                value={data.avgMoodScore}
                max={10}
                color="bg-journal-accent dark:bg-journalDark-accent"
              />
              <BarRow
                label="Энергия"
                value={data.avgEnergyScore}
                max={10}
                color="bg-emerald-600"
              />
              <BarRow
                label="Продуктивность"
                value={data.avgProductivityScore}
                max={10}
                color="bg-violet-600"
              />
            </div>
          </div>

          <div className="journal-card p-4">
            <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">
              Period: {data.periodStart} .. {data.periodEnd}
            </div>
            <div className="mt-1 font-medium">Заполнено дней: {data.completedDaysCount}</div>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <MetricCard title="Сон → Настроение (корр.)" value={correlations.sleepToMood} />
            <MetricCard title="Сон → Энергия (корр.)" value={correlations.sleepToEnergy} />
            <MetricCard title="Стресс → Продуктивность (корр.)" value={correlations.stressToProductivity} />
          </div>

          <div className="journal-card p-4">
            <div className="font-heading text-sm font-medium">Корреляции</div>
            <div className="mt-3 space-y-3">
              <SignedBarRow
                label="Сон → Настроение"
                value={correlations.sleepToMood}
              />
              <SignedBarRow
                label="Сон → Энергия"
                value={correlations.sleepToEnergy}
              />
              <SignedBarRow
                label="Стресс → Продуктивность"
                value={correlations.stressToProductivity}
              />
            </div>
          </div>

          <div className="journal-card p-4">
            <div className="font-heading text-sm font-medium">Популярные теги</div>
            {data.tagFrequencies.length === 0 ? (
              <div className="mt-2 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Нет тегов за этот период.</div>
            ) : (
              <div className="mt-3 space-y-2">
                <TagBarList items={data.tagFrequencies.slice(0, 10)} />
              </div>
            )}
          </div>
        </div>
      ) : null}
    </div>
  )
}

function MetricCard({ title, value }: { title: string; value: number | null }) {
  return (
    <div className="journal-card p-4">
      <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">{title}</div>
      <div className="mt-2 font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">{value === null || value === undefined ? '—' : value.toFixed(2)}</div>
    </div>
  )
}

function BarRow({ label, value, max, color }: { label: string; value: number | null; max: number; color: string }) {
  const v = value ?? 0
  const percent = max <= 0 ? 0 : Math.max(0, Math.min(100, (v / max) * 100))
  return (
    <div>
      <div className="flex items-center justify-between text-sm">
        <span className="text-journal-ink dark:text-journalDark-ink">{label}</span>
        <span className="font-medium">{value === null || value === undefined ? '—' : value.toFixed(2)}</span>
      </div>
      <div className="mt-1 h-2 w-full rounded bg-journal-line dark:bg-journalDark-line">
        <div className={`h-2 rounded ${color}`} style={{ width: `${percent}%` }} />
      </div>
    </div>
  )
}

function SignedBarRow({ label, value }: { label: string; value: number | null }) {
  const v = value ?? 0
  const abs = Math.abs(v)
  const percent = Math.max(0, Math.min(100, abs * 100))
  const isNegative = v < 0
  const color = isNegative ? 'bg-rose-600' : 'bg-emerald-600'
  return (
    <div>
      <div className="flex items-center justify-between text-sm">
        <span className="text-journal-ink dark:text-journalDark-ink">{label}</span>
        <span className="font-medium">{value === null || value === undefined ? '—' : value.toFixed(2)}</span>
      </div>
      <div className="mt-1 h-2 w-full rounded bg-journal-line dark:bg-journalDark-line">
        <div className={`h-2 rounded ${color}`} style={{ width: `${percent}%` }} />
      </div>
    </div>
  )
}

function TagBarList({ items }: { items: { tagId: string; tagName: string; tagColor: string | null; count: number }[] }) {
  const maxCount = Math.max(...items.map((i) => i.count), 0)
  return (
    <>
      {items.map((t) => {
        const percent = maxCount === 0 ? 0 : Math.round((t.count / maxCount) * 100)
        const color = t.tagColor ? t.tagColor : '#6366f1'
        return (
          <div key={t.tagId}>
            <div className="flex items-center justify-between text-sm">
              <span className="text-journal-ink dark:text-journalDark-ink">{t.tagName}</span>
              <span className="text-journal-inkMuted dark:text-journalDark-inkMuted">{t.count}</span>
            </div>
            <div className="mt-1 h-2 w-full rounded bg-journal-line dark:bg-journalDark-line">
              <div className="h-2 rounded" style={{ width: `${percent}%`, backgroundColor: color }} />
            </div>
          </div>
        )
      })}
    </>
  )
}

function SeriesLineChart({ points, height }: { points: { entryDate: string; moodScore: number; energyScore: number; productivityScore: number }[]; height: number }) {
  if (!points || points.length === 0) {
    return <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Нет записей за этот период.</div>
  }

  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null)
  const hovered = hoveredIndex === null ? null : points[hoveredIndex]

  const width = 600
  const paddingX = 24
  const paddingY = 12
  const innerWidth = width - paddingX * 2
  const innerHeight = height - paddingY * 2

  const maxScore = 10
  const y = (score: number) => {
    const v = Math.max(0, Math.min(maxScore, score))
    const ratio = v / maxScore
    return paddingY + (innerHeight - ratio * innerHeight)
  }

  const x = (index: number) => {
    if (points.length === 1) return paddingX + innerWidth / 2
    return paddingX + (index / (points.length - 1)) * innerWidth
  }

  const moodPoints = points.map((p, i) => `${x(i)},${y(p.moodScore)}`).join(' ')
  const energyPoints = points.map((p, i) => `${x(i)},${y(p.energyScore)}`).join(' ')
  const productivityPoints = points.map((p, i) => `${x(i)},${y(p.productivityScore)}`).join(' ')

  const first = points[0].entryDate
  const last = points[points.length - 1].entryDate

  const y0 = y(0)
  const y5 = y(5)
  const y10 = y(10)

  return (
    <div>
      <div className="flex items-center justify-between pb-2 text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">
        <span>{first}</span>
        <span>{last}</span>
      </div>

      {hovered ? (
        <div className="journal-card mb-2 p-2 text-xs">
          <div className="font-medium text-journal-ink dark:text-journalDark-ink">{hovered.entryDate}</div>
          <div className="mt-1 flex flex-wrap gap-3 text-journal-inkMuted dark:text-journalDark-inkMuted">
            <span className="flex items-center gap-2"><span className="inline-block h-2 w-2 rounded bg-indigo-600" />Настроение: {hovered.moodScore}</span>
            <span className="flex items-center gap-2"><span className="inline-block h-2 w-2 rounded bg-emerald-600" />Энергия: {hovered.energyScore}</span>
            <span className="flex items-center gap-2"><span className="inline-block h-2 w-2 rounded bg-violet-600" />Продуктивность: {hovered.productivityScore}</span>
          </div>
        </div>
      ) : null}

      <svg width="100%" viewBox={`0 0 ${width} ${height}`} preserveAspectRatio="none">
        <g>
          <line x1={paddingX} y1={paddingY} x2={paddingX + innerWidth} y2={paddingY} stroke="rgba(148,163,184,0.25)" strokeWidth="1" />
          <line x1={paddingX} y1={paddingY + innerHeight / 2} x2={paddingX + innerWidth} y2={paddingY + innerHeight / 2} stroke="rgba(148,163,184,0.25)" strokeWidth="1" />
          <line x1={paddingX} y1={paddingY + innerHeight} x2={paddingX + innerWidth} y2={paddingY + innerHeight} stroke="rgba(148,163,184,0.25)" strokeWidth="1" />

          <text x={8} y={y10} fill="rgba(100,116,139,1)" fontSize="10" dominantBaseline="middle">10</text>
          <text x={8} y={y5} fill="rgba(100,116,139,1)" fontSize="10" dominantBaseline="middle">5</text>
          <text x={8} y={y0} fill="rgba(100,116,139,1)" fontSize="10" dominantBaseline="middle">0</text>
        </g>
        <polyline points={moodPoints} fill="none" stroke="#4f46e5" strokeWidth="2.5" />
        <polyline points={energyPoints} fill="none" stroke="#10b981" strokeWidth="2.5" />
        <polyline points={productivityPoints} fill="none" stroke="#7c3aed" strokeWidth="2.5" />
        {points.map((p, i) => (
          <g key={p.entryDate + i}>
            <circle
              cx={x(i)}
              cy={y(p.moodScore)}
              r={4}
              fill="#4f46e5"
              onMouseEnter={() => setHoveredIndex(i)}
              onMouseLeave={() => setHoveredIndex((prev) => (prev === i ? null : prev))}
            />
            <circle
              cx={x(i)}
              cy={y(p.energyScore)}
              r={4}
              fill="#10b981"
              onMouseEnter={() => setHoveredIndex(i)}
              onMouseLeave={() => setHoveredIndex((prev) => (prev === i ? null : prev))}
            />
            <circle
              cx={x(i)}
              cy={y(p.productivityScore)}
              r={4}
              fill="#7c3aed"
              onMouseEnter={() => setHoveredIndex(i)}
              onMouseLeave={() => setHoveredIndex((prev) => (prev === i ? null : prev))}
            />
          </g>
        ))}
      </svg>
      <div className="mt-2 flex flex-wrap gap-3 text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">
        <span className="flex items-center gap-1"><span className="inline-block h-2 w-2 rounded bg-indigo-600" />Настроение</span>
        <span className="flex items-center gap-1"><span className="inline-block h-2 w-2 rounded bg-emerald-600" />Энергия</span>
        <span className="flex items-center gap-1"><span className="inline-block h-2 w-2 rounded bg-violet-600" />Продуктивность</span>
      </div>
    </div>
  )
}

