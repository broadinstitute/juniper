import { Page } from '@playwright/test'
import { PageInterface } from 'src/pages/page-interface'

export  default  class Home implements PageInterface {
  title = 'OurHealth'

  constructor(readonly page: Page) {}
}
