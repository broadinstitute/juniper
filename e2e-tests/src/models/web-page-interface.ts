import { Page } from '@playwright/test'

// eslint-disable-next-line @typescript-eslint/no-unused-vars
interface InterfaceConstructor {
  new(page: Page): WebPageInterface;
}

export interface WebPageInterface {
  waitReady(): Promise<this>
}
