import { Page } from '@playwright/test'
import PageBase from 'src/models/page-base'

export default class StudyConsent extends PageBase {
  title = 'OurHealth Consent | OurHealth'

  constructor(page: Page) {
    super(page)
  }
}
