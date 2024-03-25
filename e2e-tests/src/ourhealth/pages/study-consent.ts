import { Page } from '@playwright/test'
import PageBase from 'src/ourhealth/pages/page-base'

export default class StudyConsent extends PageBase {
  title = 'OurHealth Consent | OurHealth'

  constructor(protected readonly page: Page) {
    super(page)
  }
}
