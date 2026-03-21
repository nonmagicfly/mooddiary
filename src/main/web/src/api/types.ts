export type UUID = string

export type DiaryEntry = {
  userId: UUID
  id: UUID
  entryDate: string
  moodScore: number
  energyScore: number
  productivityScore: number
  stressScore: number
  sleepQualityScore: number
  note: string | null
  isCompleted: boolean
  tagIds: UUID[]
  symptomIds: UUID[]
  createdAt: string
  updatedAt: string | null
}

export type DiaryEntryUpsertPayload = {
  entryDate: string
  moodScore: number
  energyScore: number
  productivityScore: number
  stressScore: number
  sleepQualityScore: number
  note: string | null
  isCompleted: boolean
  tagIds: UUID[]
  symptomIds: UUID[]
}

export type Tag = {
  userId: UUID
  id: UUID
  name: string
  color: string | null
  createdAt: string
  updatedAt: string | null
}

export type Symptom = {
  userId: UUID
  id: UUID
  name: string
  createdAt: string
  updatedAt: string | null
}

export type Photo = {
  id: UUID
  entryId: UUID
  fileName: string
  filePath: string
  contentType: string
  size: number
  createdAt: string
}

export type TagFrequencyAnalytics = {
  tagId: UUID
  tagName: string
  tagColor: string | null
  count: number
}

export type AnalyticsCorrelations = {
  sleepToMood: number | null
  sleepToEnergy: number | null
  stressToProductivity: number | null
}

export type MoodAnalyticsResponse = {
  periodStart: string
  periodEnd: string
  avgMoodScore: number | null
  avgEnergyScore: number | null
  avgProductivityScore: number | null
  completedDaysCount: number
  tagFrequencies: TagFrequencyAnalytics[]
  correlations: AnalyticsCorrelations
  series: {
    entryDate: string
    moodScore: number
    energyScore: number
    productivityScore: number
    stressScore: number
    sleepQualityScore: number
  }[]
}

export type TagUpsertPayload = {
  name: string
  // Optional color value (backend supports null/empty strings; we send empty string for "no color").
  color: string
}

export type SymptomUpsertPayload = {
  name: string
}

