import { Page } from '@playwright/test'
import DemoPageBase from 'pages/demo/demo-page-base'

export default class StudyConsent extends DemoPageBase {
  title = 'OurHealth Consent | Juniper Demo'

  constructor(page: Page) {
    super(page)
  }
}
