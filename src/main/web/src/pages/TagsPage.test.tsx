import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import TagsPage from './TagsPage'

vi.mock('../api/api', () => {
  return {
    getTags: vi.fn(),
    createTag: vi.fn(),
    updateTag: vi.fn(),
    deleteTag: vi.fn()
  }
})

import { createTag, deleteTag, getTags, updateTag } from '../api/api'

describe('TagsPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    localStorage.clear()
  })

  it('should create a new tag and edit an existing one', async () => {
    const user = userEvent.setup()

    const existing = { userId: 'u', id: 't1', name: 'work', color: 'red', createdAt: '', updatedAt: null }
    const created = { userId: 'u', id: 't2', name: 'sport', color: '#00ff00', createdAt: '', updatedAt: null }

    ;(getTags as unknown as vi.Mock)
      .mockResolvedValueOnce([existing])
      .mockResolvedValueOnce([existing, created])

    ;(createTag as unknown as vi.Mock).mockResolvedValue(created)
    ;(updateTag as unknown as vi.Mock).mockResolvedValue({ ...existing, name: 'work2' })
    ;(deleteTag as unknown as vi.Mock).mockResolvedValue(undefined)

    render(<TagsPage />)

    await waitFor(() => expect(screen.getByText('work')).toBeInTheDocument())

    // Create
    await user.clear(screen.getByLabelText('Tag name'))
    await user.type(screen.getByLabelText('Tag name'), 'sport')
    await user.clear(screen.getByLabelText('Tag color'))
    await user.type(screen.getByLabelText('Tag color'), '#00ff00')

    await user.click(screen.getByRole('button', { name: /Создать тег/i }))

    await waitFor(() => expect(createTag).toHaveBeenCalled())
    const createPayload = (createTag as unknown as vi.Mock).mock.calls[0][0]
    expect(createPayload).toEqual({ name: 'sport', color: '#00ff00' })

    // Edit existing "work" (may be multiple "Изменить" buttons after refresh)
    const editButtons = screen.getAllByRole('button', { name: 'Изменить' })
    let clicked = false
    for (const btn of editButtons) {
      await user.click(btn)
      try {
        await waitFor(() => expect(screen.getByLabelText('Tag name')).toHaveValue('work'))
        clicked = true
        break
      } catch {
        // Try next edit button
      }
    }
    if (!clicked) throw new Error('Could not switch form to edit tag "work"')
    await expect(screen.getByLabelText('Tag name')).toHaveValue('work')
    await user.clear(screen.getByLabelText('Tag name'))
    await user.type(screen.getByLabelText('Tag name'), 'work2')
    await user.click(screen.getByRole('button', { name: /Обновить тег/i }))

    await waitFor(() => expect(updateTag).toHaveBeenCalled())
    expect((updateTag as unknown as vi.Mock).mock.calls[0][0]).toBe('t1')
  })
})

