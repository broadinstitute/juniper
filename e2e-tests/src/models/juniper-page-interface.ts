import { Locator } from '@playwright/test'
import { WebPageInterface } from 'src/models/web-page-interface'

export interface JuniperPageInterface extends WebPageInterface {
  title: string
  clickByRole(role: string, name: string): Promise<this>
  clickByName(name: string): Promise<this>

  fillIn(value: string, parent?: Locator): Promise<this>
}
