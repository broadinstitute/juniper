import { Page } from '@playwright/test'
import OurhealthPageBase from 'pages/ourhealth/ourhealth-page-base'

/** URL: /studies/ourheart/join/preEnroll **/
export default class StudyEligibility extends OurhealthPageBase {
  constructor(page: Page) {
    super(page)
  }
}
