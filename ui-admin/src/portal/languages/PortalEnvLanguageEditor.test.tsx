import React from 'react'
import { screen } from '@testing-library/react'

import { PortalEnvironmentLanguage, renderWithRouter } from '@juniper/ui-core'
import PortalEnvLanguageEditor from './PortalEnvLanguageEditor'
import { getTableCell } from 'test-utils/table-testing-utils'
import { userEvent } from '@testing-library/user-event'
import { select } from 'react-select-event'


const initialLanguages: PortalEnvironmentLanguage[] = [
  { languageName: 'English', languageCode: 'en', id: '1' },
  { languageName: 'Español', languageCode: 'es', id: '1' }
]
test('renders a list', async () => {
  const setItemsSpy = jest.fn()
  renderWithRouter(<PortalEnvLanguageEditor items={initialLanguages} setItems={setItemsSpy} readonly={true} />)
  expect(screen.getByText('English')).toBeInTheDocument()
  expect(screen.getByText('Español')).toBeInTheDocument()
})

test('removes items after confirmation dialog', async () => {
  const setItemsSpy = jest.fn()
  renderWithRouter(<PortalEnvLanguageEditor items={initialLanguages} setItems={setItemsSpy} readonly={false} />)
  await userEvent.click(getTableCell(screen.getByRole('table'), 'English', 'Actions').querySelector('button')!)
  expect(screen.getByText('Remove this language from the dropdown?')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Yes'))
  expect(setItemsSpy).toHaveBeenCalledTimes(1)
  expect(setItemsSpy.mock.lastCall[0](initialLanguages))
    .toEqual([{ languageName: 'Español', languageCode: 'es', id: '1' }])
})

test('add items after confirmation click', async () => {
  const setItemsSpy = jest.fn()
  renderWithRouter(<PortalEnvLanguageEditor items={initialLanguages} setItems={setItemsSpy} readonly={false} />)
  await userEvent.click(screen.getByLabelText('Add New'))
  await select(screen.getByLabelText('Language name'), 'Deutsch')
  await userEvent.click(screen.getByLabelText('Accept'))
  expect(setItemsSpy).toHaveBeenCalledTimes(1)
  expect(setItemsSpy.mock.lastCall[0](initialLanguages)).toEqual([
    ...initialLanguages,
    { languageName: 'Deutsch', languageCode: 'de', id: '' }
  ])
})

test('add items does not appear if readonly', async () => {
  const setItemsSpy = jest.fn()
  renderWithRouter(<PortalEnvLanguageEditor items={initialLanguages} setItems={setItemsSpy} readonly={true} />)
  expect(screen.queryByLabelText('Add New')).not.toBeInTheDocument()
})

