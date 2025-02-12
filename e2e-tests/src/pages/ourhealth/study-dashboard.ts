import { expect, Locator, Page } from '@playwright/test'
import OurHealthPageBase from 'pages/ourhealth/ourhealth-page-base'

export enum Activities
{
  Consent = 'OurHealth Consent',
  Basics = 'The Basics',
  CardiometabolicMedicalHistory = 'OurHealth Medical History',
  OtherMedicalHistory = 'Other Medical History',
  FamilyHistory = 'Family History',
  Medications = 'Medications',
  Lifestyle = 'Lifestyle',
  MentalHealth = 'Mental Health'
}

export default class StudyDashboard extends OurHealthPageBase {
  title = 'Dashboard | OurHealth'

  private readonly root: Locator

  constructor(page: Page) {
    super(page)
    this.root = this.page.locator('main.hub-dashboard')
  }

  private get locator(): Locator {
    return this.root
  }

  async waitReady(): Promise<this> {
    await super.waitReady()
    await this.activity.isVisible(Activities.Consent)
    return this
  }

  /* inner class */
  get activity() {
    const page = this.page
    const parent = this.locator
    const states = ['Not Started', 'Locked', 'Completed', 'Print OurHealth Consent']

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

      async isVisible(name: string | RegExp, opts: { timeout?: number } = {}): Promise<boolean> {
        const { timeout } = opts
        try {
          await expect(this.row(name)).toBeVisible({ timeout })
          return true
        } catch {
          return false
        }
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
