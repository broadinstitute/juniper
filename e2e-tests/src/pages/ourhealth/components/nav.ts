import { Locator, Page } from '@playwright/test'

export const enum Item {
  REGISTER = 'Register',
  LOGIN = 'Log In',
  FAQ = 'FAQ',
  SCIENTIFIC_BACKGROUND = 'Scientific Background',
  PARTICIPATION = 'Participation',
  ABOUT_US = 'About Us',
}

interface Nav {
  link(name: string): Locator;
}

/* navbar is located at the top of OurHealth page */
export default  class Navbar implements  Navbar {
  constructor(private readonly  page: Page) {}

  link(name: Item): Locator {
    return this.page.locator('.navbar-nav .nav-item').locator('a.nav-link, a.btn', { hasText: name })
  }

  async click(name: Item): Promise<Nav> {
    const href = await this.link(name).getAttribute('href')
    await this.link(name).click()
    switch (href) {
      case '/aboutUs':
        console.log(`To about us page`)
        break
      case '/participation':
        console.log(`To participation page`)
        break
      case '/background':
        console.log(`To scientific background page`)
        break
      case '/faq':
        console.log(`To faq page`)
        break
      case '/hub':
        console.log(`To login page`)
        break
      case '/studies/ourheart/join':
        console.log(`To register page`)
        break
      default:
        throw new Error(`Undefined href: ${href}`)
    }
    return this
  }
}
