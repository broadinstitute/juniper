import { expect } from '@playwright/test'
import { test } from 'lib/fixtures/ourhealth-fixture'
import { ApiPreenrollResponse } from 'src/models/api-preenroll-response'
import { goToStudyEligibility, waitForResponse } from 'tests/e2e-utils'
import data from 'src/data/ourhealth-en.json'

test.describe('POST /preEnroll request', () => {
  test('validate response', {
    annotation: [
      { type: 'request and response test example', description: 'response status should be OK' }
    ]
  }, async ({ page }) => {
    await test.step('Answer "Yes" to all eligibility questions', async () => {
      const prequal = await goToStudyEligibility(page)

      const submitResponse = await prequal.getQuestion(data.QLabel.SouthAsianAncestry).select('Yes')
        .then(async () => prequal.getQuestion(data.QLabel.UnderstandEnglish).select('Yes'))
        .then(async () => prequal.getQuestion(data.QLabel.IsAdult).select('Yes'))
        .then(async () => prequal.getQuestion(data.QLabel.LiveInUS).select('Yes'))
        .then(async () => prequal.submit({ callback: async () => await waitForResponse(page, { uri: '/preEnroll' }) }))

      expect(submitResponse).not.toBeNull()

      const respJson = await submitResponse?.json() as ApiPreenrollResponse
      console.log(`createdAt: ${respJson.createdAt}`)
      console.log(`fullData: ${JSON.stringify(JSON.parse(respJson.fullData), null, 2)}`)
    })
  })
})

