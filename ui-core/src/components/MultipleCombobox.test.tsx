import { render, screen } from '@testing-library/react'
import React from 'react'

import { MultipleComboBox } from './MultipleCombobox'
import { ItemValue } from 'src/surveyjs/multiple-combobox-question'

jest.mock('@tanstack/react-virtual', () => ({
  ...jest.requireActual('@tanstack/react-virtual'),
  useVirtualizer: jest.fn()
}))

describe('MultipleCombobox', () => {
  test('renders a combobox', () => {
    render(<MultipleComboBox<ItemValue>
      id={'test'}
      itemToString={(item: ItemValue) => item.text}
      placeholder={'Select an option'}
      choices={[]} />)

    expect(screen.getByRole('combobox')).toBeInTheDocument()
    expect(screen.getByLabelText('toggle menu')).toBeInTheDocument()
  })

  test('fetches combobox choices by url', () => {
    const mockResponse = new Response(JSON.stringify(['optionA', 'optionB']))
    const fetchSpy = jest.spyOn(global, 'fetch').mockImplementation(() => Promise.resolve(mockResponse))

    render(<MultipleComboBox<ItemValue>
      id={'test'}
      itemToString={(item: ItemValue) => item.text}
      placeholder={'Select an option'}
      choices={[]}
      choicesByUrl={'/test.json'} />)

    expect(fetchSpy).toHaveBeenCalledWith('/test.json')
  })
})
