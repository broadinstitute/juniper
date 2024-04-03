import { Locator, Page } from '@playwright/test'
import WebComponentBase from 'src/page-components/web-component-base'

/* footer is located at the bottom of OurHealth page */
export default class Footer extends WebComponentBase {
  root: Locator

  constructor(page: Page) {
    super(page)
    this.root = this.page.locator('footer')
  }
}
