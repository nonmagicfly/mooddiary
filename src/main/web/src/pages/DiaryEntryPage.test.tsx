import React from 'react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi, describe, it, expect, beforeEach } from 'vitest'
import DiaryEntryPage from './DiaryEntryPage'

vi.mock('../api/api', () => {
  return {
    getDiaryEntry: vi.fn(),
    createDiaryEntry: vi.fn(),
    updateDiaryEntry: vi.fn(),
    getTags: vi.fn().mockResolvedValue([]),
    getSymptoms: vi.fn().mockResolvedValue([]),
    uploadDiaryEntryPhotos: vi.fn().mockResolvedValue([])
  }
})

import { createDiaryEntry, getDiaryEntry, getSymptoms, getTags, uploadDiaryEntryPhotos } from '../api/api'

describe('DiaryEntryPage', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('should submit selected tag/symptom ids on create', async () => {
    const user = userEvent.setup()

    const tag1 = { userId: 'u', id: '11111111-1111-4111-8111-111111111111', name: 'work', color: 'red', createdAt: '', updatedAt: null }
    const symptom1 = { userId: 'u', id: '22222222-2222-4222-8222-222222222222', name: 'pain', createdAt: '', updatedAt: null }

    ;(getTags as unknown as vi.Mock).mockResolvedValue([tag1])
    ;(getSymptoms as unknown as vi.Mock).mockResolvedValue([symptom1])
    ;(createDiaryEntry as unknown as vi.Mock).mockResolvedValue({
      userId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
      id: 'cccccccc-cccc-cccc-cccc-cccccccccccc',
      entryDate: '2026-03-19',
      moodScore: 5,
      energyScore: 5,
      productivityScore: 5,
      stressScore: 5,
      sleepQualityScore: 5,
      note: null,
      isCompleted: false,
      tagIds: [tag1.id],
      symptomIds: [symptom1.id],
      createdAt: new Date().toISOString(),
      updatedAt: null
    })
    ;(uploadDiaryEntryPhotos as unknown as vi.Mock).mockResolvedValue([])

    render(
      <MemoryRouter initialEntries={['/diary/entry/new']}>
        <Routes>
          <Route path="/diary/entry/new" element={<DiaryEntryPage mode="create" />} />
        </Routes>
      </MemoryRouter>
    )

    const tagCheckbox = await screen.findByLabelText('work')
    const symptomCheckbox = await screen.findByLabelText('pain')

    await user.click(tagCheckbox)
    await user.click(symptomCheckbox)

    const submitButton = screen.getByRole('button', { name: /Создать запись/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(createDiaryEntry).toHaveBeenCalled()
    })

    const payload = (createDiaryEntry as unknown as vi.Mock).mock.calls[0][0]
    expect(payload.tagIds).toEqual([tag1.id])
    expect(payload.symptomIds).toEqual([symptom1.id])
  })

  it('should load entry and populate form on edit', async () => {
    const entry = {
      userId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
      id: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
      entryDate: '2026-03-19',
      moodScore: 1,
      energyScore: 2,
      productivityScore: 3,
      stressScore: 4,
      sleepQualityScore: 5,
      note: 'hello',
      isCompleted: true,
      tagIds: ['11111111-1111-4111-8111-111111111111'],
      symptomIds: ['22222222-2222-4222-8222-222222222222'],
      createdAt: new Date().toISOString(),
      updatedAt: null
    }

    ;(getDiaryEntry as unknown as vi.Mock).mockResolvedValue(entry)

    render(
      <MemoryRouter initialEntries={['/diary/entry/bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb']}>
        <Routes>
          <Route path="/diary/entry/:id" element={<DiaryEntryPage mode="edit" />} />
        </Routes>
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(getDiaryEntry).toHaveBeenCalled()
    })

    await waitFor(() => {
      expect((screen.getByDisplayValue('2026-03-19') as HTMLInputElement).value).toBe('2026-03-19')
    })

    expect(screen.getByText('Настроение')).toBeInTheDocument()
    const moodSelect = screen.getByRole('combobox', { name: /настроение/i })
    expect(moodSelect).toHaveValue('1')
    expect(screen.getByDisplayValue('hello')).toBeInTheDocument()
  })

  it('should upload selected photos on create', async () => {
    const user = userEvent.setup()

    const created = {
      userId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
      id: 'cccccccc-cccc-cccc-cccc-cccccccccccc',
      entryDate: '2026-03-19',
      moodScore: 5,
      energyScore: 5,
      productivityScore: 5,
      stressScore: 5,
      sleepQualityScore: 5,
      note: null,
      isCompleted: false,
      tagIds: [],
      symptomIds: [],
      createdAt: new Date().toISOString(),
      updatedAt: null
    }

    ;(createDiaryEntry as unknown as vi.Mock).mockResolvedValue(created)
    ;(uploadDiaryEntryPhotos as unknown as vi.Mock).mockResolvedValue([])

    const { container } = render(
      <MemoryRouter initialEntries={['/diary/entry/new']}>
        <Routes>
          <Route path="/diary/entry/new" element={<DiaryEntryPage mode="create" />} />
        </Routes>
      </MemoryRouter>
    )

    const fileInput = container.querySelector('input[type="file"]') as HTMLInputElement
    const file = new File(['hello'], 'a.png', { type: 'image/png' })
    await user.upload(fileInput, file)

    const submitButton = screen.getByRole('button', { name: /Создать запись/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(createDiaryEntry).toHaveBeenCalled()
      expect(uploadDiaryEntryPhotos).toHaveBeenCalled()
      expect(uploadDiaryEntryPhotos).toHaveBeenCalledWith(created.id, expect.any(Array))
    })
  })
})

