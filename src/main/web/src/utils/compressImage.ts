export type CompressImageOptions = {
  maxDimension?: number
  quality?: number
}

const DEFAULT_MAX_DIMENSION = 1920
const DEFAULT_QUALITY = 0.85

function scaledDimensions(width: number, height: number, maxDimension: number) {
  const longest = Math.max(width, height)
  if (longest <= maxDimension) {
    return { width, height }
  }
  const scale = maxDimension / longest
  return {
    width: Math.max(1, Math.round(width * scale)),
    height: Math.max(1, Math.round(height * scale))
  }
}

function jpegFileName(originalName: string): string {
  const trimmed = originalName.trim()
  if (!trimmed) return 'photo.jpg'
  const dotIndex = trimmed.lastIndexOf('.')
  const baseName = dotIndex > 0 ? trimmed.slice(0, dotIndex) : trimmed
  return `${baseName || 'photo'}.jpg`
}

export async function compressImageFile(file: File, options: CompressImageOptions = {}): Promise<File> {
  if (!file.type.startsWith('image/')) {
    return file
  }

  const maxDimension = options.maxDimension ?? DEFAULT_MAX_DIMENSION
  const quality = options.quality ?? DEFAULT_QUALITY

  let bitmap: ImageBitmap
  try {
    bitmap = await createImageBitmap(file)
  } catch {
    return file
  }

  try {
    const { width, height } = scaledDimensions(bitmap.width, bitmap.height, maxDimension)
    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height

    const ctx = canvas.getContext('2d')
    if (!ctx) {
      return file
    }

    ctx.drawImage(bitmap, 0, 0, width, height)

    const blob = await new Promise<Blob | null>((resolve) => {
      canvas.toBlob(resolve, 'image/jpeg', quality)
    })
    if (!blob) {
      return file
    }

    return new File([blob], jpegFileName(file.name), {
      type: 'image/jpeg',
      lastModified: file.lastModified
    })
  } finally {
    bitmap.close()
  }
}

export async function compressImageFiles(files: File[], options?: CompressImageOptions): Promise<File[]> {
  return Promise.all(files.map((file) => compressImageFile(file, options)))
}
