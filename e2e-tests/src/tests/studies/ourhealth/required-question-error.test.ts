import { expect } from '@playwright/test'
import { test } from 'lib/fixtures/ourhealth-fixture'
import { QLabel } from 'src/ourhealth/pages/study-eligibility'
import Question from 'src/page-components/question'
import { goToStudyEligibility } from 'tests/e2e-utils'

test.describe('Question validation', () => {
  test('UI @ourhealth', {
    annotation: [
      { type: 'required question unit test example', description: 'No answers' }
    ]
  }, async ({ page }) => {
    await test.step('"Response required" question error should be visible', async () => {
      const expErr = 'Response not required.'

      const prequal = await goToStudyEligibility(page)
      await prequal.submit()

      const q = new Question(page, { qText: QLabel.SouthAsianAncestry })
      const err = await q.error()
      expect(err).toMatch(expErr)
    })
  })
})
