import { Page } from '@playwright/test'

// eslint-disable-next-line @typescript-eslint/no-unused-vars
interface InterfaceConstructor {
  new(page: Page): PageInterface;
}


export interface PageInterface {
  page: Page
  title: string
  waitReady(): Promise<this>
}
