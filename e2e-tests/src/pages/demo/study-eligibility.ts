import { Page } from '@playwright/test'
import DemoPageBase from 'pages/demo/demo-page-base'

/** URL: /studies/ourheart/join/preEnroll **/
export default class StudyEligibility extends DemoPageBase {
  constructor(page: Page) {
    super(page)
  }
}
