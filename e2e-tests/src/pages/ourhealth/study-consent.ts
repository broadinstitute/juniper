import { Page } from '@playwright/test'
import OurhealthPageBase from 'pages/ourhealth/ourhealth-page-base'

export default class StudyConsent extends OurhealthPageBase {
  title = 'OurHealth Consent | OurHealth'

  constructor(page: Page) {
    super(page)
  }
}
