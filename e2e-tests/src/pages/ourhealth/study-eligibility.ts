import { Page } from '@playwright/test'
import PageBase from 'pages/ourhealth/page-base'

/** URL: /studies/ourheart/join/preEnroll **/
export default class StudyEligibility extends PageBase {
  constructor(protected readonly page: Page) {
    super(page)
  }
}
