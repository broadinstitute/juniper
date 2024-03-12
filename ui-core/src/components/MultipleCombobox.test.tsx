import { act, render, screen } from '@testing-library/react'
import React from 'react'

import { MultipleComboBox } from './MultipleCombobox'
import { ItemValue } from 'src/surveyjs/multiple-combobox-question'
import { useApiContext } from 'src/participant/ApiProvider'

jest.mock('@tanstack/react-virtual', () => ({
  ...jest.requireActual('@tanstack/react-virtual'),
  useVirtualizer: jest.fn()
}))

jest.mock('src/participant/ApiProvider', () => ({
  useApiContext: jest.fn()
}))

describe('MultipleCombobox', () => {
  beforeEach(() => {
    (useApiContext as jest.Mock).mockImplementation(() => ({
      getImageUrl: (fileName: string, fileVersion: number) => `/${fileVersion}/${fileName}`
    }))
  })

  test('renders a combobox', () => {
    render(<MultipleComboBox<ItemValue>
      id={'test'}
      itemToString={(item: ItemValue) => item.text}
      placeholder={'Select an option'}
      options={[]} />)

    expect(screen.getByRole('combobox')).toBeInTheDocument()
    expect(screen.getByLabelText('toggle menu')).toBeInTheDocument()
  })

  test('fetches combobox choices by url', async () => {
    const mockResponse = new Response(JSON.stringify(['optionA', 'optionB']))
    const fetchSpy = jest.spyOn(global, 'fetch').mockImplementation(() => Promise.resolve(mockResponse))

    await act(async () => render(<MultipleComboBox<ItemValue>
      id={'test'}
      itemToString={(item: ItemValue) => item.text}
      placeholder={'Select an option'}
      options={[]}
      choicesByUrl={'/test.json'}/>))

    expect(fetchSpy).toHaveBeenCalledWith('/test.json')
  })

  test('fetches combobox choices by file', async () => {
    const mockResponse = new Response(JSON.stringify(['optionA', 'optionB']))
    const fetchSpy = jest.spyOn(global, 'fetch').mockImplementation(() => Promise.resolve(mockResponse))

    await act(async () => render(<MultipleComboBox<ItemValue>
      id={'test'}
      itemToString={(item: ItemValue) => item.text}
      placeholder={'Select an option'}
      options={[]}
      choicesByFile={{ fileName: 'test.json', fileVersion: 2 }}/>))

    expect(fetchSpy).toHaveBeenCalledWith('/2/test.json')
  })
})
