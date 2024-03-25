import { test, expect } from '@playwright/test'
import { adminLogin, randomChars } from 'src/tests/e2e-utils'

test('shows mailing list dialog on direct link', async ({ page }) => {
  await page.goto(`${process.env.PARTICIPANT_URL}/?showJoinMailingList=true `)

  await expect.soft(page).toHaveTitle('Juniper Heart Demo')
  const mailingListDialog = page.locator('div.modal-dialog')
  await expect.soft(mailingListDialog).toBeVisible()
  await expect.soft(mailingListDialog.locator('h2'))
    .toHaveText('Join Mailing List')

  const randomStr = randomChars(6)
  const name = `mailingListTest-${randomStr}`
  const email = `mailingListTest-${randomStr}@foo.com`
  await mailingListDialog.locator('input[type="text"]').fill(name)
  await mailingListDialog.locator('input[type="email"]').fill(email)
  await mailingListDialog.locator('button:text("Join")').click()


  // check entry got added
  await adminLogin(page)
  await page.goto(`${process.env.ADMIN_URL}/demo/studies/heartdemo/env/sandbox/mailingList`)
  await expect.soft(page.getByTestId('loading-spinner')).not.toBeVisible()
  const tableBody = page.locator('table.table-striped').locator('tbody')
  const firstRow = tableBody.locator('tr').nth(0)
  await expect.soft(firstRow.locator('td').nth(1)).toHaveText(email)
  await expect.soft(firstRow.locator('td').nth(2)).toHaveText(name)

  expect(test.info().errors).toHaveLength(0)
})
