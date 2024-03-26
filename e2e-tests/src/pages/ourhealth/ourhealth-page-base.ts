import { Page } from '@playwright/test'
import PageBase from 'src/models/page-base'
import Navbar from 'src/page-components/navbar'

export default abstract class OurHealthPageBase extends PageBase {
  title = 'OurHealth'

  navbar: Navbar

  protected constructor(page: Page) {
    super(page)
    this.navbar = new Navbar(page)
  }
}
