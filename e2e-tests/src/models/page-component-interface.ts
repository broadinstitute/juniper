export interface PageComponentInterface {
  waitReady(): Promise<this>
  isVisible(): Promise<boolean>
}
