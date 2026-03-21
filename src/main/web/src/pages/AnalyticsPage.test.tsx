import React from 'react'
import { MemoryRouter } from 'react-router-dom'
import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import AnalyticsPage from './AnalyticsPage'

vi.mock('../api/api', () => {
  return {
    getAnalyticsDaily: vi.fn(),
    getAnalyticsWeekly: vi.fn(),
    getAnalyticsMonthly: vi.fn()
  }
})

import { getAnalyticsDaily, getAnalyticsMonthly, getAnalyticsWeekly } from '../api/api'

describe('AnalyticsPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    localStorage.clear()
  })

  it('should render averages and correlations', async () => {
    const response = {
      periodStart: '2026-03-10',
      periodEnd: '2026-03-16',
      avgMoodScore: 4,
      avgEnergyScore: 3,
      avgProductivityScore: 5,
      completedDaysCount: 7,
      tagFrequencies: [],
      correlations: { sleepToMood: 1, sleepToEnergy: 0.5, stressToProductivity: null },
      series: []
    }

    ;(getAnalyticsWeekly as unknown as vi.Mock).mockResolvedValue(response)
    ;(getAnalyticsDaily as unknown as vi.Mock).mockResolvedValue(response)
    ;(getAnalyticsMonthly as unknown as vi.Mock).mockResolvedValue(response)

    render(
      <MemoryRouter>
        <AnalyticsPage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(getAnalyticsWeekly).toHaveBeenCalled()
    })

    expect(screen.getByText('Сред. настроение')).toBeInTheDocument()
    expect(screen.getAllByText('4.00').length).toBeGreaterThan(0)
    expect(screen.getByText('Заполнено дней: 7')).toBeInTheDocument()
    expect(screen.getByText('Сон → Настроение (корр.)')).toBeInTheDocument()
  })
})

