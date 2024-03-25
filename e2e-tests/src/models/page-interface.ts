import { Page } from '@playwright/test'

interface InterfaceConstructor {
  new(page: Page): PageInterface;
}


export interface PageInterface {
  readonly title: string
  waitReady(): Promise<void>
}
