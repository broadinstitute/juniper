import { Page } from '@playwright/test'
import DemoPageBase from 'pages/demo/demo-page-base'

export default class StudySurveys extends DemoPageBase {
  title = 'The Basics | Juniper Demo'

  constructor(page: Page) {
    super(page)
  }
}
