import React from 'react'
import { MemoryRouter } from 'react-router-dom'
import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { routerFutureFlags } from '../test/routerFuture'
import DashboardPage from './DashboardPage'

vi.mock('../api/api', () => {
  return {
    getAnalyticsDaily: vi.fn(),
    listDiaryEntries: vi.fn()
  }
})

import { getAnalyticsDaily, listDiaryEntries } from '../api/api'

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    localStorage.clear()
  })

  it('should render today metrics and latest entries', async () => {
    ;(getAnalyticsDaily as unknown as vi.Mock).mockResolvedValue({
      periodStart: '2026-03-19',
      periodEnd: '2026-03-19',
      avgMoodScore: 4,
      avgEnergyScore: 6,
      avgProductivityScore: 5,
      completedDaysCount: 2,
      tagFrequencies: [],
      correlations: { stressToProductivity: 0.1 },
      series: []
    })

    ;(listDiaryEntries as unknown as vi.Mock).mockResolvedValue([
      {
        userId: 'u',
        id: 'e1',
        entryDate: '2026-03-19',
        moodScore: 4,
        energyScore: 6,
        productivityScore: 5,
        stressScore: 3,
        sleepQualityScore: 2,
        note: 'hello',
        isCompleted: true,
        tagIds: [],
        symptomIds: [],
        createdAt: '',
        updatedAt: null
      }
    ])

    render(
      <MemoryRouter future={routerFutureFlags}>
        <DashboardPage />
      </MemoryRouter>
    )

    await waitFor(() => expect(screen.getByText('Обзор')).toBeInTheDocument())
    expect(screen.getByText('Заполнено дней: 2')).toBeInTheDocument()

    // "4.00" can appear multiple times (cards/correlations), so just assert it exists.
    expect(screen.getAllByText('4.00').length).toBeGreaterThan(0)
    expect(screen.getByText('2026-03-19')).toBeInTheDocument()
    expect(screen.getByText('редактирование закрыто')).toBeInTheDocument()
  })
})

