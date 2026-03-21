import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import SymptomsPage from './SymptomsPage'

vi.mock('../api/api', () => {
  return {
    getSymptoms: vi.fn(),
    createSymptom: vi.fn(),
    updateSymptom: vi.fn(),
    deleteSymptom: vi.fn()
  }
})

import { createSymptom, getSymptoms, updateSymptom } from '../api/api'

describe('SymptomsPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    localStorage.clear()
  })

  it('should create a symptom and then update it', async () => {
    const user = userEvent.setup()

    const existing = { userId: 'u', id: 's1', name: 'pain', createdAt: '', updatedAt: null }
    const created = { userId: 'u', id: 's2', name: 'fatigue', createdAt: '', updatedAt: null }

    ;(getSymptoms as unknown as vi.Mock)
      .mockResolvedValueOnce([existing])
      .mockResolvedValueOnce([existing, created])

    ;(createSymptom as unknown as vi.Mock).mockResolvedValue(created)
    ;(updateSymptom as unknown as vi.Mock).mockResolvedValue({ ...existing, name: 'pain2' })

    render(<SymptomsPage />)

    await waitFor(() => expect(screen.getByText('pain')).toBeInTheDocument())

    await user.clear(screen.getByLabelText('Symptom name'))
    await user.type(screen.getByLabelText('Symptom name'), 'fatigue')

    await user.click(screen.getByRole('button', { name: /Создать симптом/i }))

    await waitFor(() => expect(createSymptom).toHaveBeenCalled())
    const createPayload = (createSymptom as unknown as vi.Mock).mock.calls[0][0]
    expect(createPayload).toEqual({ name: 'fatigue' })

    const editButtons = screen.getAllByRole('button', { name: 'Изменить' })
    await user.click(editButtons[0])
    await expect(screen.getByLabelText('Symptom name')).toHaveValue('pain')

    await user.clear(screen.getByLabelText('Symptom name'))
    await user.type(screen.getByLabelText('Symptom name'), 'pain2')
    await user.click(screen.getByRole('button', { name: /Обновить симптом/i }))

    await waitFor(() => expect(updateSymptom).toHaveBeenCalled())
    expect((updateSymptom as unknown as vi.Mock).mock.calls[0][0]).toBe('s1')
  })
})

