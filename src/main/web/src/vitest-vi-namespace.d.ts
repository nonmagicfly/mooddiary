export {}

declare global {
  namespace vi {
    type Mock = import('vitest').Mock
  }
}
