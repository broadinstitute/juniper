export interface PageInterface {
  readonly title: string
  waitReady(): Promise<void>
}
