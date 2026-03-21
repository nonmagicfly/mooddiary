import React from 'react'
import { MemoryRouter } from 'react-router-dom'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import DiaryHistoryPage from './DiaryHistoryPage'

vi.mock('../api/api', () => {
  return {
    getTags: vi.fn(),
    getSymptoms: vi.fn(),
    listDiaryEntries: vi.fn(),
    getAnalyticsWeekly: vi.fn(),
    getAnalyticsMonthly: vi.fn()
  }
})

import { getAnalyticsMonthly, getAnalyticsWeekly, getSymptoms, getTags, listDiaryEntries } from '../api/api'

describe('DiaryHistoryPage', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.restoreAllMocks()
  })

  it('should render entries and filter by selected tag ids', async () => {
    const user = userEvent.setup()

    const tag1 = { userId: 'u', id: '11111111-1111-4111-8111-111111111111', name: 'work', color: 'red', createdAt: '', updatedAt: null }
    const tag2 = { userId: 'u', id: '22222222-2222-4222-8222-222222222222', name: 'sport', color: null, createdAt: '', updatedAt: null }

    ;(getTags as unknown as vi.Mock).mockResolvedValue([tag1, tag2])
    ;(getSymptoms as unknown as vi.Mock).mockResolvedValue([])

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
        tagIds: [tag1.id],
        symptomIds: [],
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
        tagIds: [tag2.id],
        symptomIds: [],
        createdAt: '',
        updatedAt: null
      }
    ]

    ;(listDiaryEntries as unknown as vi.Mock).mockResolvedValue(entries)

    render(
      <MemoryRouter>
        <DiaryHistoryPage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getAllByText('Записи').length).toBeGreaterThan(0)
    })

    // pick tag "work"
    const workCheckbox = screen.getByLabelText('work') as HTMLInputElement
    await user.click(workCheckbox)

    expect(await screen.findByText('Найдено: 1')).toBeInTheDocument()
  })

  it('should render calendar when switching to Calendar view', async () => {
    const user = userEvent.setup()

    ;(getTags as unknown as vi.Mock).mockResolvedValue([])
    ;(getSymptoms as unknown as vi.Mock).mockResolvedValue([])

    ;(listDiaryEntries as unknown as vi.Mock).mockResolvedValueOnce([]).mockResolvedValueOnce([])

    render(
      <MemoryRouter>
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

    ;(getTags as unknown as vi.Mock).mockResolvedValue([])
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
      <MemoryRouter>
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

    ;(getTags as unknown as vi.Mock).mockResolvedValue([])
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
      <MemoryRouter>
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

