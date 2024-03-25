import { expect } from '@playwright/test'
import { test } from 'lib/fixtures/ourhealth-fixture'
import * as process from 'process'
import { Study } from 'src/data/constants'
import StudyConsent from 'src/ourhealth/pages/study-consent'
import StudyCreateAcct from 'src/ourhealth/pages/study-create-acct'
import StudyDashboard from 'src/ourhealth/pages/study-dashboard'
import { emailAlias, goToStudyEligibility } from 'tests/e2e-utils'

test.describe('Home page', () => {
  test('UI @visual @ourhealth', {
    annotation: [
      { type: 'New registration workflow example', description: 'Participant is older than 18 years' }
    ]
  }, async ({ page }) => {
    const testEmail = emailAlias(process.env.PARTICIPANT_EMAIL_1!) // not real

    const activities = [
      'OurHealth Consent',
      'The Basics',
      'Cardiometabolic Medical History',
      'Other Medical History',
      'Family History',
      'Medications',
      'Lifestyle',
      'Mental Health'
    ]

    await test.step('Answer "Yes" to all eligibility questions', async () => {
      const prequal = await goToStudyEligibility(page)

      await prequal.select(Study.OurHealth.QLabel.SouthAsianAncestry, 'Yes')
      expect(await prequal.progress()).toMatch('Answered 1/4 questions')

      await prequal.select(Study.Standard.QLabel.UnderstandEnglish, 'Yes')
      expect(await prequal.progress()).toMatch('Answered 2/4 questions')

      await prequal.select(Study.Standard.QLabel.IsAdult, 'Yes')
      expect(await prequal.progress()).toMatch('Answered 3/4 questions')

      await prequal.select(Study.Standard.QLabel.LiveInUS, 'Yes')
      expect(await prequal.progress()).toMatch('Answered 4/4 questions')

      await prequal.submit()
    })

    await test.step('Create new account with test email', async () => {
      const createAcct = new StudyCreateAcct(page)
      await createAcct.fillEmail(testEmail)
      await createAcct.click('button', 'Complete')
    })

    const dashboard = await test.step('Landing on Dashboard for the first time', async (): Promise<StudyDashboard> => {
      const dashboard = new StudyDashboard(page)

      // activities are visible and match expected status
      for (const name of activities) {
        const dashboardActivity = dashboard.activity
        const isVisible = await dashboardActivity.isVisible(name)
        expect(isVisible).toBe(true)
        if (name === 'OurHealth Consent') {
          expect(await dashboardActivity.status(name)).toStrictEqual('Not Started')
        } else {
          expect(await dashboardActivity.status(name)).toStrictEqual('Locked')
        }
      }

      await dashboard.click('link', 'Start Consent')
      return dashboard
    })

    /* NOTE: text contents on each page ARE NOT verified */
    await test.step('Step 1: Start Consent', async () => {
      const consent = new StudyConsent(page)

      let progress = await consent.progress()
      expect(progress).toStrictEqual('Page 1 of 3')
      await consent.click('button', 'Next')

      progress = await consent.progress()
      expect(progress).toStrictEqual('Page 2 of 3')
      await consent.agree()
      await consent.click('button', 'Next')


      progress = await consent.progress()
      expect(progress).toStrictEqual('Page 3 of 3')
      await consent.fillIn(Study.Standard.QLabel.FullName, 'Tony Junior Stark') // TODO randomize
      await consent.draw(Study.Standard.QLabel.Signature)

      await consent.click('button', 'Complete')
    })

    await page.pause()
  })
})
