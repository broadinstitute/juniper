import { expect } from '@playwright/test'
import { test } from 'lib/fixtures/demo-fixture'
import * as process from 'process'
import StudyConsent from 'pages/demo/study-consent'
import StudyCreateAcct from 'pages/demo/study-create-acct'
import StudyDashboard, { Activities } from 'pages/demo/study-dashboard'
import { emailAlias, goToDemoPreEnroll } from 'tests/e2e-utils'
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

      await prequal.getQuestion(data.PreEnroll.ProxyQuestion).select(data.PreEnroll.ProxyAnswerYes)
      expect(await prequal.progress()).toMatch('Answered 1/10 questions')

      await prequal.getQuestion(data.PreEnroll.ProxyGivenName).fillIn('Jonas')
      expect(await prequal.progress()).toMatch('Answered 2/10 questions')

      await prequal.getQuestion(data.PreEnroll.ProxyFamilyName).fillIn('Salk')
      expect(await prequal.progress()).toMatch('Answered 3/10 questions')

      await prequal.getQuestion(data.PreEnroll.GivenName, { exact: true }).fillIn('Peter')
      expect(await prequal.progress()).toMatch('Answered 4/10 questions')

      await prequal.getQuestion(data.PreEnroll.FamilyName, { exact: true }).fillIn('Salk')
      expect(await prequal.progress()).toMatch('Answered 5/10 questions')

      await prequal.clickByRole('button', 'Next')

      await prequal.getQuestion(data.PreEnroll.SouthAsianAncestry).select('Yes')
      expect(await prequal.progress()).toMatch('Answered 6/10 questions')

      await prequal.getQuestion(data.PreEnroll.UnderstandEnglish).select('Yes')
      expect(await prequal.progress()).toMatch('Answered 7/10 questions')

      await prequal.getQuestion(data.PreEnroll.IsAdult).select('Yes')
      expect(await prequal.progress()).toMatch('Answered 8/10 questions')

      await prequal.getQuestion(data.PreEnroll.LiveInUS).select('Yes')
      expect(await prequal.progress()).toMatch('Answered 9/10 questions')

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
    })

    /* NOTE: text contents on each page ARE NOT verified */
    await test.step('Step 1: Complete Consent on Behalf of Proxy', async () => {
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
      await consent.fillIn(data.Consent.FullName, 'Peter Salk', page.locator('body'))
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
