import React from 'react'
import { MemoryRouter } from 'react-router-dom'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { routerFutureFlags } from '../test/routerFuture'
import DiaryHistoryPage from './DiaryHistoryPage'

vi.mock('../api/api', () => {
  return {
    getSymptoms: vi.fn(),
    listDiaryEntries: vi.fn(),
    getAnalyticsWeekly: vi.fn(),
    getAnalyticsMonthly: vi.fn()
  }
})

import { getAnalyticsMonthly, getAnalyticsWeekly, getSymptoms, listDiaryEntries } from '../api/api'

describe('DiaryHistoryPage', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.restoreAllMocks()
  })

  it('should render entries and filter by selected symptom ids', async () => {
    const user = userEvent.setup()

    const symptom1 = { userId: 'u', id: '22222222-2222-4222-8222-222222222222', name: 'pain', createdAt: '', updatedAt: null }

    ;(getSymptoms as unknown as vi.Mock).mockResolvedValue([symptom1])

    const entries = [
      {
        userId: 'u',
        id: 'e1',
        entryDate: '2026-03-19',
        moodScore: 5,
        energyScore: 5,
        productivityScore: 5,
        stressScore: 5,
        sleepQualityScore: 5,
        note: null,
        isCompleted: false,
        tagIds: [],
        symptomIds: [symptom1.id],
        createdAt: '',
        updatedAt: null
      },
      {
        userId: 'u',
        id: 'e2',
        entryDate: '2026-03-18',
        moodScore: 5,
        energyScore: 5,
        productivityScore: 5,
        stressScore: 5,
        sleepQualityScore: 5,
        note: null,
        isCompleted: false,
        tagIds: [],
        symptomIds: [],
        createdAt: '',
        updatedAt: null
      }
    ]

    ;(listDiaryEntries as unknown as vi.Mock).mockResolvedValue(entries)

    render(
      <MemoryRouter future={routerFutureFlags}>
        <DiaryHistoryPage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getAllByText('Записи').length).toBeGreaterThan(0)
    })

    const painCheckbox = screen.getByLabelText('pain') as HTMLInputElement
    await user.click(painCheckbox)

    expect(await screen.findByText('Найдено: 1')).toBeInTheDocument()
  })

  it('should render calendar when switching to Calendar view', async () => {
    const user = userEvent.setup()

    ;(getSymptoms as unknown as vi.Mock).mockResolvedValue([])

    ;(listDiaryEntries as unknown as vi.Mock).mockResolvedValueOnce([]).mockResolvedValueOnce([])

    render(
      <MemoryRouter future={routerFutureFlags}>
        <DiaryHistoryPage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getAllByText('Записи').length).toBeGreaterThan(0)
    })

    await user.click(screen.getAllByRole('button', { name: 'Календарь' })[0])

    await waitFor(() => {
      // Calendar grid always includes weekday header.
      expect(screen.getByText('Пн')).toBeInTheDocument()
    })
  })

  it('should render weekly overview when switching to Weekly view', async () => {
    const user = userEvent.setup()

    ;(getSymptoms as unknown as vi.Mock).mockResolvedValue([])

    ;(listDiaryEntries as unknown as vi.Mock).mockResolvedValueOnce([])

    ;(getAnalyticsWeekly as unknown as vi.Mock).mockResolvedValue({
      periodStart: '2026-03-13',
      periodEnd: '2026-03-19',
      avgMoodScore: 5,
      avgEnergyScore: 6,
      avgProductivityScore: 4,
      completedDaysCount: 3,
      tagFrequencies: [],
      correlations: { sleepToMood: null, sleepToEnergy: null, stressToProductivity: null },
      series: [
        {
          entryDate: '2026-03-13',
          moodScore: 5,
          energyScore: 6,
          productivityScore: 4,
          stressScore: 5,
          sleepQualityScore: 6
        }
      ]
    })

    render(
      <MemoryRouter future={routerFutureFlags}>
        <DiaryHistoryPage />
      </MemoryRouter>
    )

    await user.click(screen.getAllByRole('button', { name: 'Неделя' })[0])

    await waitFor(() => {
      expect(screen.getByText('Обзор за неделю')).toBeInTheDocument()
      expect(screen.getByText('Сред. настроение')).toBeInTheDocument()
    })
  })

  it('should render monthly overview and top tags when switching to Monthly view', async () => {
    const user = userEvent.setup()

    ;(getSymptoms as unknown as vi.Mock).mockResolvedValue([])

    ;(listDiaryEntries as unknown as vi.Mock).mockResolvedValueOnce([])

    ;(getAnalyticsMonthly as unknown as vi.Mock).mockResolvedValue({
      periodStart: '2026-03-01',
      periodEnd: '2026-03-31',
      avgMoodScore: 5,
      avgEnergyScore: 5,
      avgProductivityScore: 7,
      completedDaysCount: 10,
      tagFrequencies: [{ tagId: 't1', tagName: 'work', tagColor: null, count: 4 }],
      correlations: { sleepToMood: null, sleepToEnergy: null, stressToProductivity: null },
      series: []
    })

    render(
      <MemoryRouter future={routerFutureFlags}>
        <DiaryHistoryPage />
      </MemoryRouter>
    )

    await user.click(screen.getAllByRole('button', { name: 'Месяц' })[0])

    await waitFor(() => {
      expect(screen.getByText('Обзор за месяц')).toBeInTheDocument()
      expect(screen.getByText('work')).toBeInTheDocument()
    })
  })
})

