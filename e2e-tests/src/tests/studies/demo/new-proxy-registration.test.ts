import { expect } from '@playwright/test'
import { test } from 'lib/fixtures/demo-fixture'
import * as process from 'process'
import StudyConsent from 'pages/demo/study-consent'
import StudyCreateAcct from 'pages/demo/study-create-acct'
import StudyDashboard, { Activities } from 'pages/demo/study-dashboard'
import {
  emailAlias,
  goToDemoPreEnroll
} from 'tests/e2e-utils'
import data from 'src/data/demo-en.json'
import StudyEligibilityDemo from 'pages/demo/study-eligibility'

const { PARTICIPANT_EMAIL_1 } = process.env

test.describe('Proxy', () => {
  test('@heart demo', {
    annotation: [
      { type: 'New registration workflow unfinished example', description: 'Participant is older than 18 years' }
    ]
  }, async ({ page }) => {
    const testEmail = emailAlias(PARTICIPANT_EMAIL_1 ? PARTICIPANT_EMAIL_1 : 'dbush@broadinstitute.org')

    await test.step('Answer "Yes" to all eligibility questions', async () => {
      const prequal = await goToDemoPreEnroll(page)

      await prequal.getQuestion(data.PreEnroll.ProxyQuestion).select(
        'I am enrolling on behalf of my child / my legal dependent.'
      )
      await expect(await prequal.progress()).toHaveText('Answered 1/10 questions')

      await prequal.getQuestion(data.PreEnroll.ProxyGivenName).fillIn('Jonas')
      await expect(await prequal.progress()).toHaveText('Answered 2/10 questions')

      await prequal.getQuestion(data.PreEnroll.ProxyFamilyName).fillIn('Salk')
      await expect(await prequal.progress()).toHaveText('Answered 3/10 questions')

      await prequal.getQuestion(data.PreEnroll.GivenName, { exact: true }).fillIn('Peter')
      await expect(await prequal.progress()).toHaveText('Answered 4/10 questions')

      await prequal.getQuestion(data.PreEnroll.FamilyName, { exact: true }).fillIn('Salk')
      await expect(await prequal.progress()).toHaveText('Answered 5/10 questions')

      await prequal.clickByRole('button', 'Next')

      await prequal.getQuestion(data.PreEnroll.SouthAsianAncestry).select('Yes')
      await expect(await prequal.progress()).toHaveText('Answered 6/10 questions')

      await prequal.getQuestion(data.PreEnroll.UnderstandEnglish).select('Yes')
      await expect(await prequal.progress()).toHaveText('Answered 7/10 questions')

      await prequal.getQuestion(data.PreEnroll.IsAdult).select('Yes')
      await expect(await prequal.progress()).toHaveText('Answered 8/10 questions')

      await prequal.getQuestion(data.PreEnroll.LiveInUS).select('Yes')
      await expect(await prequal.progress()).toHaveText('Answered 9/10 questions')

      await prequal.getQuestion(data.PreEnroll.marketing).select('Yes')

      await prequal.submit()
    })

    await test.step('Create new account with test email', async () => {
      const createAcct = new StudyCreateAcct(page)
      await createAcct.fillEmail(testEmail)
      await createAcct.clickByRole('button', 'Complete')
    })

    await test.step('Landing on Dashboard for the first time', async (): Promise<StudyDashboard> => {
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
    await test.step('Step 1: Complete Consent on Behalf of Proxy', async () => {
      const consent = new StudyConsent(page)

      let progress = await consent.progress()
      await expect(progress).toHaveText('Page 1 of 3')
      await consent.clickByRole('button', 'Next')

      progress = await consent.progress()
      await expect(progress).toHaveText('Page 2 of 3')
      await consent.agree()
      const matrix = await consent.getQuestion(data.Consent.Initials)
      await matrix.fillAllInWith('JS')
      await consent.clickByRole('button', 'Next')


      progress = await consent.progress()
      await expect(progress).toHaveText('Page 3 of 3')
      await page.locator('body').getByLabel('Full legal name *').fill('Peter Salk')
      await consent.drawSignature(data.Consent.Signature)

      await consent.clickByRole('button', 'Complete')
    })

    await test.step('Step 2: Change Users', async () => {
      const dashboard = new StudyDashboard(page)
      await dashboard.waitReady()

      await page.locator('//button[@aria-label="Select Participant"]').click()
      await dashboard.clickByRole('button', 'Jonas Salk', { exact: false }) // click from dropdown

      // no activities on screen
      for (const name of Object.values(Activities)) {
        const dashboardActivity = dashboard.activity
        const isVisible = await dashboardActivity.isVisible(name, { timeout: 200 })
        expect(isVisible).toBe(false)
      }
    })

    await test.step('Step 3: Go to new pre-enroll page', async () => {
      const dashboard = new StudyDashboard(page)
      await dashboard.waitReady()

      await page.locator('//button[@aria-label="Select Participant"]').click()
      await page.locator('//a[normalize-space()="Add New Participant"]').click() // click from dropdown

      const prequal = new StudyEligibilityDemo(page)
      await prequal.waitReady()
    })


    // await page.pause()
  })
})
