import { Page } from '@playwright/test'
import PageBase from 'src/ourhealth/pages/page-base'

export default class StudyCreateAcct extends PageBase {
  constructor(protected readonly page: Page) {
    super(page)
  }

  async fillEmail(value: string): Promise<void> {
    await this.page.getByPlaceholder('name@email.com').fill(value)
  }
}
