import { Locator, Page } from '@playwright/test'
import ComponentBase from 'src/page-components/component-base'

/* footer is located at the bottom of OurHealth page */
export default class Footer extends ComponentBase {
  root: Locator

  constructor(page: Page) {
    super(page)
    this.root = this.page.locator('footer')
  }
}
