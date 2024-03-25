import { Page } from '@playwright/test'
import PageBase from 'pages/ourhealth/page-base'

export default class StudySurveys extends PageBase {
  title = 'The Basics | OurHealth'

  constructor(protected readonly page: Page) {
    super(page)
  }
}
