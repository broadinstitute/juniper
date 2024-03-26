import { expect } from '@playwright/test'
import { test } from 'lib/fixtures/ourhealth-fixture'
import { Study } from 'src/data/constants-en'
import { PreEnrollResponse } from 'src/models/preenroll-response'
import { goToStudyEligibility, waitForResponse } from 'tests/e2e-utils'

test.describe('POST /preEnroll request', () => {
  test('validate response', {
    annotation: [
      { type: 'request and response test example', description: 'response status should be OK' }
    ]
  }, async ({ page }) => {
    await test.step('Answer "Yes" to all eligibility questions', async () => {
      const prequal = await goToStudyEligibility(page)

      const submitResponse = await prequal.select(Study.OurHealth.QLabel.SouthAsianAncestry, 'Yes')
        .then(async () => prequal.select(Study.Common.QLabel.UnderstandEnglish, 'Yes'))
        .then(async () => prequal.select(Study.Common.QLabel.IsAdult, 'Yes'))
        .then(async () => prequal.select(Study.Common.QLabel.LiveInUS, 'Yes'))
        .then(async () => prequal.submit({ callback: async () => await waitForResponse(page, { uri: '/preEnroll' }) }))

      expect(submitResponse).not.toBeNull()

      const respJson = await submitResponse?.json() as PreEnrollResponse
      console.log(`createdAt: ${respJson.createdAt}`)
      console.log(`fullData: ${JSON.stringify(JSON.parse(respJson.fullData), null, 2)}`)
    })
  })
})

