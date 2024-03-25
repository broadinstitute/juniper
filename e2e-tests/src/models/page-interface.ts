import { Page } from '@playwright/test'

// eslint-disable-next-line @typescript-eslint/no-unused-vars
interface InterfaceConstructor {
  new(page: Page): PageInterface;
}


export interface PageInterface {
  readonly title: string
  waitReady(): Promise<void>
}
