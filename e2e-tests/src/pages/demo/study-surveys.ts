import { Page } from '@playwright/test'
import OurHealthPageBase from 'pages/ourhealth/ourhealth-page-base'

export default class StudySurveys extends OurHealthPageBase {
  title = 'The Basics | Juniper Demo'

  constructor(page: Page) {
    super(page)
  }
}
