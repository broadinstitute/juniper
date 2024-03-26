import { Page } from '@playwright/test'
import PageBase from 'src/models/page-base'

/** URL: /studies/ourheart/join/preEnroll **/
export default class StudyEligibility extends PageBase {
  constructor(page: Page) {
    super(page)
  }
}
