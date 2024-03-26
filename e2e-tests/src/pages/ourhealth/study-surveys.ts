import { Page } from '@playwright/test'
import PageBase from 'src/models/page-base'

export default class StudySurveys extends PageBase {
  title = 'The Basics | OurHealth'

  constructor(page: Page) {
    super(page)
  }
}
