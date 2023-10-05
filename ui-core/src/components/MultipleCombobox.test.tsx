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
    //Arrange
    render(<MultipleComboBox<ItemValue>
      id={'test'}
      itemToString={(item: ItemValue) => item.text}
      placeholder={'Select an option'}
      options={[]} />)

    //Assert
    expect(screen.getByRole('combobox')).toBeInTheDocument()
    expect(screen.getByLabelText('toggle menu')).toBeInTheDocument()
  })
})
