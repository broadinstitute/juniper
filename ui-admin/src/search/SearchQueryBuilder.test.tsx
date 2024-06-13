import React from 'react'
import { setupRouterTest } from '@juniper/ui-core'
import { mockStudyEnvContext } from '../test-utils/mocking-utils'
import {
  render,
  screen,
  waitFor
} from '@testing-library/react'
import { SearchQueryBuilder } from './SearchQueryBuilder'
import userEvent from '@testing-library/user-event'

jest.mock('../api/api', () => ({
  ...jest.requireActual('../api/api'),
  getExpressionSearchFacets: jest.fn().mockResolvedValue([
    {
      'profile.mailingAddress.country': {
        type: 'STRING',
        choices: [{ stableId: 'US', text: 'United States' }]
      }
    }
  ])
}))

describe('SearchQueryBuilder', () => {
  it('should render with basic options', async () => {
    const onSearchExpressionChange = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <SearchQueryBuilder
        studyEnvContext={mockStudyEnvContext()}
        onSearchExpressionChange={onSearchExpressionChange}
        searchExpression={''}
      />)
    render(RoutedComponent)

    await waitFor(() => expect(screen.getByText('+Rule')).toBeInTheDocument())


    await userEvent.click(screen.getByText('+Rule'))


    const fieldInput = (await screen.findAllByRole('combobox'))[1]


    await userEvent.click(fieldInput)
    await userEvent.type(fieldInput, 'profile.mailingAddress.country{enter}')

    const valueInput = (await screen.findAllByRole('combobox'))[3]

    await userEvent.click(valueInput)
    await userEvent.type(fieldInput, 'Cana{enter}')


    waitFor(() => {
      expect(onSearchExpressionChange).toHaveBeenCalledWith(`{profile.mailingAddress.country} = 'CA'`)
    })
  })

  it('should render advanced', async () => {
    const onSearchExpressionChange = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <SearchQueryBuilder
        studyEnvContext={mockStudyEnvContext()}
        onSearchExpressionChange={onSearchExpressionChange}
        searchExpression={''}
      />)
    render(RoutedComponent)

    await waitFor(() => expect(screen.getByText('+Rule')).toBeInTheDocument())


    await userEvent.click(screen.getByText('(switch to advanced view)'))

    await userEvent.type(
      screen.getByLabelText('Search expression'),
      '{{profile.mailingAddress.country}} = \'CA\'{enter}')


    waitFor(() => {
      expect(onSearchExpressionChange).toHaveBeenCalledWith(`{profile.mailingAddress.country} = 'CA'`)
    })
  })
})
