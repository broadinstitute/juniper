import { expect } from '@playwright/test'
import { test } from 'lib/fixtures/ourhealth-fixture'
import Home, { Label } from 'src/ourhealth/pages/home'

test.describe('Home page', () => {
  test('UI @visual @ourhealth', {
    annotation: [
      { type: 'visual test example', description: 'screenshots comparisons' }
    ]
  }, async ({ page }) => {
    const home = new Home(page)
    await home.waitReady()

    await test.step('Verify "Help us understand cardiovascular disease risk"', async () => {
      const header = home.divFor(Label.HelpUsUnderstand).locator('h2')
      await expect(header).toHaveCount(1)
      await expect(home.linkBecomeParticipant).toHaveScreenshot('link-Become-Participant.png')
    })

    await test.step('Verify "Why is this needed"', async () => {
      const header = home.divFor(Label.WhyIsNeeded).locator('h2')
      await expect(header).toHaveCount(1)
      await expect(home.linkScientificBackground).toHaveScreenshot('link-scientific-background.png')
    })

    await test.step('Verify "Who can join"', async () => {
      const header = home.divFor(Label.WhoCanJoin).locator('h2')
      await expect(header).toHaveCount(1)
      await expect(home.linkGetStarted).toHaveScreenshot('link-get-started.png')
      // verify list
      const ul = home.divFor('Who can join').locator('//ul')
      await expect(ul).toHaveScreenshot(`anyone-who-is-list.png`)
    })

    await test.step('Verify "How to Participate"', async () => {
      const header = home.divFor(Label.HowToParticipant).locator('h2')
      await expect(header).toHaveCount(1)
      await expect(home.linkLearnMoreAboutParticipation).toHaveScreenshot('link-learn-more-participation.png')
      // verify steps display
      const section = home.divFor('How to Participate')
      for (let i=1; i<5; i++) {
        await expect(section.locator(`//div[./img[contains(@src, "step_${i}.png")]]`))
          .toHaveScreenshot(`how-to-participant-step_${i}.png`)
      }
    })

    await test.step('Verify "Frequently Asked Questions"', async () => {
      const header = home.divFor(Label.FAQ).locator('h2')
      await expect(header).toHaveCount(1)
      await expect(home.linkMoreFAQ).toHaveScreenshot('link-more-faq.png')
      // verify expand & collapse question
      const faqs = [
        'What is the OurHealth study?',
        'Why are we doing this study?',
        'How can I participate?',
        'What does OurHealth do with the data?'
      ]
      for (let i=0; i<faqs.length; i++) {
        const markdownId = await home.togglePlusMinus(faqs[i], { expand: true })
        await expect(page.locator(markdownId!)).toHaveScreenshot(`faq-expand-question-${i}.png`)
        await home.togglePlusMinus(faqs[i], { expand: false }) // collapse
      }
    })
  })
})
