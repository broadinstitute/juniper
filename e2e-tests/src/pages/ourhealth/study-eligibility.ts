import { Page } from '@playwright/test'
import OurHealthPageBase from 'pages/ourhealth/ourhealth-page-base'

/** URL: /studies/ourheart/join/preEnroll **/
export default class StudyEligibility extends OurHealthPageBase {
  constructor(page: Page) {
    super(page)
  }
}
