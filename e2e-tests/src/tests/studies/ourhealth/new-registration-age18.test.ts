import { expect } from '@playwright/test'
import { test } from 'lib/fixtures/ourhealth-fixture'
import * as process from 'process'
import StudyConsent from 'pages/ourhealth/study-consent'
import StudyCreateAcct from 'pages/ourhealth/study-create-acct'
import StudyDashboard, { Activities } from 'pages/ourhealth/study-dashboard'
import { emailAlias, goToStudyEligibility } from 'tests/e2e-utils'
import data from 'src/data/ourhealth-en.json'

const { PARTICIPANT_EMAIL_1 } = process.env

test.describe('Home page', () => {
  test('UI @ourhealth @registration', {
    annotation: [
      { type: 'New registration workflow unfinished example', description: 'Participant is older than 18 years' }
    ]
  }, async ({ page }) => {
    const testEmail = emailAlias(PARTICIPANT_EMAIL_1 ? PARTICIPANT_EMAIL_1 : 'dbush@broadinstitute.org')

    await test.step('Answer "Yes" to all eligibility questions', async () => {
      const prequal = await goToStudyEligibility(page)

      await prequal.getQuestion(data.QLabel.SouthAsianAncestry).select('Yes')
      expect(await prequal.progress()).toMatch('Answered 1/4 questions')

      await prequal.getQuestion(data.QLabel.UnderstandEnglish).select('Yes')
      expect(await prequal.progress()).toMatch('Answered 2/4 questions')

      await prequal.getQuestion(data.QLabel.IsAdult).select('Yes')
      expect(await prequal.progress()).toMatch('Answered 3/4 questions')

      await prequal.getQuestion(data.QLabel.LiveInUS).select('Yes')
      expect(await prequal.progress()).toMatch('Answered 4/4 questions')

      await prequal.submit()
    })

    await test.step('Create new account with test email', async () => {
      const createAcct = new StudyCreateAcct(page)
      await createAcct.fillEmail(testEmail)
      await createAcct.clickByRole('button', 'Complete')
    })

    const dashboard = await test.step('Landing on Dashboard for the first time', async (): Promise<StudyDashboard> => {
      const dashboard = new StudyDashboard(page)
      await dashboard.waitReady()

      // activities are visible and match expected initial status
      for (const name of Object.values(Activities)) {
        const dashboardActivity = dashboard.activity
        const isVisible = await dashboardActivity.isVisible(name)
        expect(isVisible, `"${name}" activity is not visible in Dashboard`).toBe(true)
        if (name === 'OurHealth Consent') {
          expect(await dashboardActivity.status(name)).toStrictEqual('Not Started')
        } else {
          expect(await dashboardActivity.status(name)).toStrictEqual('Locked')
        }
      }

      await dashboard.clickByRole('link', 'Start Consent')
      return dashboard
    })

    /* NOTE: text contents on each page ARE NOT verified */
    await test.step('Step 1: Start Consent', async () => {
      const consent = new StudyConsent(page)

      let progress = await consent.progress()
      expect(progress).toStrictEqual('Page 1 of 3')
      await consent.clickByRole('button', 'Next')

      progress = await consent.progress()
      expect(progress).toStrictEqual('Page 2 of 3')
      await consent.agree()
      await consent.clickByRole('button', 'Next')


      progress = await consent.progress()
      expect(progress).toStrictEqual('Page 3 of 3')
      await consent.fillIn(data.QLabel.FullName, 'Tony Junior Stark', page.locator('body')) // TODO randomize
      await consent.drawSignature(data.QLabel.Signature)

      await consent.clickByRole('button', 'Complete')
    })

    await test.step('Step 2: Start Surveys', async () => {
      // back on Dashboard automatically
      await dashboard.waitReady()

      await dashboard.clickByRole('link', 'Start Surveys')
    })

    // await page.pause()
  })
})
