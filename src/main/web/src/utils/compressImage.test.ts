import { afterEach, describe, expect, it, vi } from 'vitest'
import { compressImageFile, compressImageFiles } from './compressImage'

describe('compressImageFile', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('returns non-image files unchanged', async () => {
    const file = new File(['hello'], 'note.txt', { type: 'text/plain' })
    await expect(compressImageFile(file)).resolves.toBe(file)
  })

  it('returns original file when createImageBitmap fails', async () => {
    vi.stubGlobal('createImageBitmap', vi.fn().mockRejectedValue(new Error('unsupported')))
    const file = new File(['hello'], 'photo.heic', { type: 'image/heic' })
    await expect(compressImageFile(file)).resolves.toBe(file)
  })

  it('compresses large images to jpeg with resized dimensions', async () => {
    const close = vi.fn()
    vi.stubGlobal(
      'createImageBitmap',
      vi.fn().mockResolvedValue({
        width: 4000,
        height: 3000,
        close
      })
    )

    const drawImage = vi.fn()
    const toBlob = vi.fn((callback: BlobCallback) => {
      callback(new Blob(['jpeg-data'], { type: 'image/jpeg' }))
    })

    vi.spyOn(document, 'createElement').mockImplementation((tagName: string) => {
      if (tagName !== 'canvas') {
        return document.createElement.bind(document)(tagName)
      }
      return {
        width: 0,
        height: 0,
        getContext: () => ({ drawImage }),
        toBlob
      } as unknown as HTMLCanvasElement
    })

    const file = new File(['raw'], 'selfie.png', { type: 'image/png' })
    const result = await compressImageFile(file, { maxDimension: 1920, quality: 0.85 })

    expect(result.type).toBe('image/jpeg')
    expect(result.name).toBe('selfie.jpg')
    expect(drawImage).toHaveBeenCalledWith(expect.anything(), 0, 0, 1920, 1440)
    expect(toBlob).toHaveBeenCalledWith(expect.any(Function), 'image/jpeg', 0.85)
    expect(close).toHaveBeenCalled()
  })

  it('keeps small images at original dimensions', async () => {
    const close = vi.fn()
    vi.stubGlobal(
      'createImageBitmap',
      vi.fn().mockResolvedValue({
        width: 800,
        height: 600,
        close
      })
    )

    const drawImage = vi.fn()
    const toBlob = vi.fn((callback: BlobCallback) => {
      callback(new Blob(['jpeg-data'], { type: 'image/jpeg' }))
    })

    vi.spyOn(document, 'createElement').mockImplementation((tagName: string) => {
      if (tagName !== 'canvas') {
        return document.createElement.bind(document)(tagName)
      }
      return {
        width: 0,
        height: 0,
        getContext: () => ({ drawImage }),
        toBlob
      } as unknown as HTMLCanvasElement
    })

    const file = new File(['raw'], 'small.jpg', { type: 'image/jpeg' })
    await compressImageFile(file)

    expect(drawImage).toHaveBeenCalledWith(expect.anything(), 0, 0, 800, 600)
  })
})

describe('compressImageFiles', () => {
  it('compresses each file in the batch', async () => {
    const close = vi.fn()
    vi.stubGlobal(
      'createImageBitmap',
      vi.fn().mockResolvedValue({
        width: 100,
        height: 100,
        close
      })
    )

    vi.spyOn(document, 'createElement').mockImplementation((tagName: string) => {
      if (tagName !== 'canvas') {
        return document.createElement.bind(document)(tagName)
      }
      return {
        width: 0,
        height: 0,
        getContext: () => ({ drawImage: vi.fn() }),
        toBlob: (callback: BlobCallback) => {
          callback(new Blob(['jpeg-data'], { type: 'image/jpeg' }))
        }
      } as unknown as HTMLCanvasElement
    })

    const files = [
      new File(['a'], 'a.png', { type: 'image/png' }),
      new File(['b'], 'b.png', { type: 'image/png' })
    ]

    const result = await compressImageFiles(files)
    expect(result).toHaveLength(2)
    expect(result.every((file) => file.type === 'image/jpeg')).toBe(true)
  })
})
