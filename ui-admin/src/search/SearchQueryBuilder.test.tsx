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
import Api, { SearchValueTypeDefinition } from 'api/api'
import clearAllMocks = jest.clearAllMocks


const mailingAddressCountryFacet: { [index: string]: SearchValueTypeDefinition } = {
  'profile.mailingAddress.country': {
    type: 'STRING',
    choices: [
      { stableId: 'US', text: 'United States' }
    ],
    allowMultiple: false,
    allowOtherDescription: false
  }
}


describe('SearchQueryBuilder', () => {
  it('should render with basic options', async () => {
    jest.spyOn(Api, 'getExpressionSearchFacets').mockResolvedValue(mailingAddressCountryFacet)

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
    await userEvent.type(fieldInput, 'US{enter}')

    await waitFor(() => {
      expect(onSearchExpressionChange).toHaveBeenLastCalledWith(`{profile.mailingAddress.country} = 'US'`)
    })
  })

  it('should render advanced', async () => {
    jest.clearAllMocks()
    jest.spyOn(Api, 'getExpressionSearchFacets').mockResolvedValue(mailingAddressCountryFacet)

    const onSearchExpressionChange = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <SearchQueryBuilder
        studyEnvContext={mockStudyEnvContext()}
        onSearchExpressionChange={onSearchExpressionChange}
        searchExpression={''}
      />)
    render(RoutedComponent)

    await waitFor(() => expect(screen.getByText('+Rule')).toBeInTheDocument())


    await waitFor(() => expect(screen.getByText('(switch to advanced view)')).not.toBeDisabled())
    await userEvent.click(screen.getByText('(switch to advanced view)'))

    await userEvent.type(
      screen.getByLabelText('Search expression'),
      '{{profile.mailingAddress.country} = \'US\'')


    await waitFor(() => {
      expect(onSearchExpressionChange).toHaveBeenLastCalledWith(`{profile.mailingAddress.country} = 'US'`)
    })
  })

  it('should render with a saved search', async () => {
    jest.spyOn(Api, 'getExpressionSearchFacets').mockResolvedValue(mailingAddressCountryFacet)

    const onSearchExpressionChange = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <SearchQueryBuilder
        studyEnvContext={mockStudyEnvContext()}
        onSearchExpressionChange={onSearchExpressionChange}
        searchExpression={'{profile.mailingAddress.country} = \'US\''}
      />)
    render(RoutedComponent)

    await waitFor(() => expect(screen.getByText('+Rule')).toBeInTheDocument())

    expect(screen.getByText('profile.mailingAddress.country')).toBeInTheDocument()
    expect(screen.getByText('United States')).toBeInTheDocument()
  })

  it('should render advanced editor if error in search expression', async () => {
    jest.spyOn(Api, 'getExpressionSearchFacets').mockResolvedValue(mailingAddressCountryFacet)

    const onSearchExpressionChange = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <SearchQueryBuilder
        studyEnvContext={mockStudyEnvContext()}
        onSearchExpressionChange={onSearchExpressionChange}
        searchExpression={'{profile.mailingAddress.country} =  AAAAAAAA nooOOOOO'}
      />)
    render(RoutedComponent)

    await waitFor(() => {
      expect(screen.getByText(
        'unknown token', { exact: false }
      )).toBeInTheDocument()
    })

    expect(screen.getByText('(switch to basic view)').className).toContain('disabled')
  })

  it('should render advanced editor if functions used', async () => {
    jest.spyOn(Api, 'getExpressionSearchFacets').mockResolvedValue(mailingAddressCountryFacet)

    const onSearchExpressionChange = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <SearchQueryBuilder
        studyEnvContext={mockStudyEnvContext()}
        onSearchExpressionChange={onSearchExpressionChange}
        searchExpression={'lower({profile.mailingAddress.country}) = \'us\''}
      />)
    render(RoutedComponent)

    await waitFor(() => {
      expect(screen.getByText(
        'The current search expression cannot be represented in the basic query builder.'
      )).toBeInTheDocument()
    })

    expect(screen.getByText('(switch to basic view)').className).toContain('disabled')
  })

  it('should disable basic editor if error introduced in advanced editor', async () => {
    jest.spyOn(Api, 'getExpressionSearchFacets').mockResolvedValue(mailingAddressCountryFacet)
    jest.spyOn(Api, 'executeSearchExpression').mockResolvedValue([])

    const { RoutedComponent } = setupRouterTest(
      <TestFullQueryBuilderState/>)
    render(RoutedComponent)

    await waitFor(() => expect(screen.getByText('(switch to advanced view)').className).not.toContain('disabled'))

    await userEvent.click(screen.getByText('(switch to advanced view)'))

    await waitFor(() => {
      expect(screen.getByText('(switch to basic view)')).toBeInTheDocument()
      expect(screen.getByText('(switch to basic view)').className).not.toContain('disabled')
    })

    await userEvent.type(
      screen.getByLabelText('Search expression'),
      '{{profile.mailingAddress.country} = ooOOoa aa asdfas asdf asid  !!!')

    await waitFor(() => {
      expect(screen.getByText(
        'unknown token', { exact: false }
      )).toBeInTheDocument()
    })

    expect(screen.getByText('(switch to basic view)').className).toContain('disabled')
  })

  it('should disable basic editor if function introduced in advanced editor', async () => {
    jest.spyOn(Api, 'getExpressionSearchFacets').mockResolvedValue(mailingAddressCountryFacet)
    jest.spyOn(Api, 'executeSearchExpression').mockResolvedValue([])

    const { RoutedComponent } = setupRouterTest(
      <TestFullQueryBuilderState/>)
    render(RoutedComponent)

    await waitFor(() => expect(screen.getByText('(switch to advanced view)').className).not.toContain('disabled'))
    await userEvent.click(screen.getByText('(switch to advanced view)'))

    await waitFor(() => {
      expect(screen.getByText('(switch to basic view)')).toBeInTheDocument()
      expect(screen.getByText('(switch to basic view)').className).not.toContain('disabled')
    })

    await userEvent.type(
      screen.getByLabelText('Search expression'),
      '{{profile.mailingAddress.country} = lower(\'us\')')

    await waitFor(() => {
      expect(screen.getByText(
        'The current search expression cannot be represented in the basic query builder.'
      )).toBeInTheDocument()
    })

    expect(screen.getByText('(switch to basic view)').className).toContain('disabled')
  })

  it('should disable basic editor if not introduced in advanced editor', async () => {
    jest.spyOn(Api, 'getExpressionSearchFacets').mockResolvedValue(mailingAddressCountryFacet)
    jest.spyOn(Api, 'executeSearchExpression').mockResolvedValue([])

    const { RoutedComponent } = setupRouterTest(
      <TestFullQueryBuilderState/>)
    render(RoutedComponent)

    await waitFor(() => expect(screen.getByText('(switch to advanced view)').className).not.toContain('disabled'))
    await userEvent.click(screen.getByText('(switch to advanced view)'))

    expect(screen.getByText('(switch to basic view)').className).not.toContain('disabled')
    await waitFor(() => {
      expect(screen.getByText('(switch to basic view)')).toBeInTheDocument()
    })

    await userEvent.type(
      screen.getByLabelText('Search expression'),
      '!{{profile.mailingAddress.country} = \'us\'')

    await waitFor(() => {
      expect(screen.getByText(
        'The current search expression cannot be represented in the basic query builder.'
      )).toBeInTheDocument()
    })

    expect(screen.getByText('(switch to basic view)').className).toContain('disabled')
  })
})


const TestFullQueryBuilderState = () => {
  const [searchExpression, setSearchExpression] = React.useState('')
  return <SearchQueryBuilder
    studyEnvContext={mockStudyEnvContext()}
    onSearchExpressionChange={setSearchExpression}
    searchExpression={searchExpression}
  />
}
