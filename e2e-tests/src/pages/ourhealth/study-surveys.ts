import { Page } from '@playwright/test'
import OurhealthPageBase from 'pages/ourhealth/ourhealth-page-base'

export default class StudySurveys extends OurhealthPageBase {
  title = 'The Basics | OurHealth'

  constructor(page: Page) {
    super(page)
  }
}
