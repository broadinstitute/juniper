import { expect } from '@playwright/test'
import { test } from 'lib/fixtures/ourhealth-fixture'
import Question from 'src/page-components/question'
import { goToOurhealthPreEnroll } from 'tests/e2e-utils'
import data from 'src/data/ourhealth-en.json'

test.describe('Question validation', () => {
  test('UI @ourhealth', {
    annotation: [
      { type: 'required question unit-test example', description: 'No answers' }
    ]
  }, async ({ page }) => {
    await test.step('"Response required" question error should be visible', async () => {
      const expErr = 'Response required.'

      const prequal = await goToOurhealthPreEnroll(page)
      await prequal.submit()

      let question = new Question(page, { dataName: data.QLabel.SouthAsianAncestry })
      let err = await question.error()
      expect(err).toMatch(expErr)

      question = new Question(page, { dataName: data.QLabel.IsAdult })
      err = await question.error()
      expect(err).toMatch(expErr)
    })
  })
})
