import { Locator } from '@playwright/test'

export interface PageComponentInterface {
  root: Locator
  get locator(): Locator
  waitReady(): Promise<this>
  isVisible(): Promise<boolean>
}
