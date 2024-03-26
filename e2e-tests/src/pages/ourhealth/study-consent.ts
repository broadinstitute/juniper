import { Page } from '@playwright/test'
import OurHealthPageBase from 'pages/ourhealth/ourhealth-page-base'

export default class StudyConsent extends OurHealthPageBase {
  title = 'OurHealth Consent | OurHealth'

  constructor(page: Page) {
    super(page)
  }
}
