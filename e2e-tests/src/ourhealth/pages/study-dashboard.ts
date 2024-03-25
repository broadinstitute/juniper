import { Locator, Page } from '@playwright/test'
import PageBase from 'src/ourhealth/pages/page-base'

export default class StudyDashboard extends PageBase {
  title = 'Dashboard | OurHealth'

  private readonly root: Locator

  constructor(protected readonly page: Page) {
    super(page)
    this.root = this.page.locator('main.hub-dashboard')
  }

  get locator(): Locator {
    return this.root
  }

  /* inner class */
  get activity() {
    const page = this.page
    const parent = this.locator
    const states = ['Not Started', 'Locked', 'Completed']

    return new class {
      async status(name: string | RegExp): Promise<string> {
        const li = this.row(name)
        const text = await li.locator('div').nth(2).innerText()
        const matchExpected = states.find(value => value === text)
        if (!matchExpected) {
          throw new Error(`Unexpected status found on Dashboard. "${text}"`)
        }
        return text
      }

      async isVisible(name: string | RegExp): Promise<boolean> {
        return this.row(name).isVisible()
      }

      async all(): Promise<string []> {
        const rows = await parent.locator('ol li .flex-row').all()
        const texts = []
        for (const row of rows) {
          texts.push(await row.locator('div').nth(2).innerText())
        }
        return texts
      }

      async click(name: string | RegExp): Promise<void> {
        return this.row(name).locator('a').click()
      }

      private row(name: string | RegExp): Locator {
        if (typeof name === 'string') {
          return parent.locator('ol li .flex-row')
            .filter({ has: page.locator(`//*[normalize-space()="${name}"]`) })
        }
        return parent.locator('ol li .flex-row').filter({ hasText: name })
      }
    }
  }
}
