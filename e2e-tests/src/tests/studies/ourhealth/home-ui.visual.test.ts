import { expect } from '@playwright/test'
import { test } from 'lib/fixtures/ourhealth-fixture'
import Home from 'pages/ourhealth/home'
import data from 'src/data/ourhealth-en.json'

test.describe('Home page', () => {
  test('UI @visual @ourhealth', {
    annotation: [
      { type: 'screenshot-test example', description: 'screenshots comparisons' }
    ]
  }, async ({ page }) => {
    const home = new Home(page)
    await home.waitReady()

    await test.step('Verify "Help us understand cardiovascular disease risk"', async () => {
      const header = home.divFor(data.FAQ.HelpUsUnderstand).locator('h2')
      await expect(header).toHaveCount(1)
      await expect(home.linkBecomeParticipant).toHaveScreenshot('link-Become-Participant.png')
    })

    await test.step('Verify "Why is this needed"', async () => {
      const header = home.divFor(data.FAQ.WhyIsNeeded).locator('h2')
      await expect(header).toHaveCount(1)
      await expect(home.linkScientificBackground).toHaveScreenshot('link-scientific-background.png')
    })

    await test.step('Verify "Who can join"', async () => {
      const header = home.divFor(data.FAQ.WhoCanJoin).locator('h2')
      await expect(header).toHaveCount(1)
      await expect(home.linkGetStarted).toHaveText('Get Started')
      // verify list
      const ul = home.divFor('Who can join').locator('//ul')
      await expect(ul.locator('li').nth(0)).toHaveText('Living in the United States')
    })

    await test.step('Verify "How to Participate"', async () => {
      const header = home.divFor(data.FAQ.HowToParticipant).locator('h2')
      await expect(header).toHaveCount(1)
      await expect(home.linkLearnMoreAboutParticipation).toHaveText('Learn More About Participation')
    })

    await test.step('Verify "Frequently Asked Questions"', async () => {
      const header = home.divFor(data.FAQ.FAQ).locator('h2')
      await expect(header).toHaveCount(1)
      await expect(home.linkMoreFAQ).toHaveText('More FAQs')
    })
  })
})
