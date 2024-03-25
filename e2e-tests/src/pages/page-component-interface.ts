export interface PageComponentInterface {
  waitReady(): Promise<void>
  isVisible(): Promise<boolean>
}
