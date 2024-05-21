import { Locator } from '@playwright/test'
import { WebPageInterface } from 'src/models/web-page-interface'

export interface WebElementInterface extends WebPageInterface {
  root: Locator
  get locator(): Locator
  isVisible(): Promise<boolean>
}
