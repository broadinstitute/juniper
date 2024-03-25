import { Page } from '@playwright/test'
import PageBase from 'src/ourhealth/pages/page-base'

/** URL: /studies/ourheart/join/preEnroll **/
export default class StudyEligibility extends PageBase {
  constructor(protected readonly page: Page) {
    super(page)
  }

  async waitReady(): Promise<void> {
    await super.waitReady()
  }
}
