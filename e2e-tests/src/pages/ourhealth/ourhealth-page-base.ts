import { Page } from '@playwright/test'
import RegistrationPageBase from 'src/models/registration-page-base'
import Footer from 'src/page-components/footer'
import Navbar from 'src/page-components/navbar'

export default abstract class OurHealthPageBase extends RegistrationPageBase {
  title = 'OurHealth'

  // navbar and footer are specific to OurHealth
  navbar: Navbar

  footer: Footer

  protected constructor(page: Page) {
    super(page)
    this.navbar = new Navbar(page)
    this.footer = new Footer(page)
  }
}
