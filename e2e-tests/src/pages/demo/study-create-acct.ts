import { Page } from '@playwright/test'
import DemoPageBase from 'pages/demo/demo-page-base'

export default class StudyCreateAcct extends DemoPageBase {
  constructor(page: Page) {
    super(page)
  }

  async fillEmail(value: string): Promise<void> {
    await this.page.getByPlaceholder('name@email.com').fill(value)
  }
}
