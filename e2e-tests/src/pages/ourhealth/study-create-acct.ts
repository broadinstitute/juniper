import { Page } from '@playwright/test'
import OurhealthPageBase from 'pages/ourhealth/ourhealth-page-base'

export default class StudyCreateAcct extends OurhealthPageBase {
  constructor(page: Page) {
    super(page)
  }

  async fillEmail(value: string): Promise<void> {
    await this.page.getByPlaceholder('name@email.com').fill(value)
  }
}
