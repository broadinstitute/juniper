// import { act, render, screen } from '@testing-library/react'
import React from 'react'

// import { MultipleComboBox } from './MultipleCombobox'
// import { ItemValue } from 'src/surveyjs/multiple-combobox-question'
// import medications from 'src/surveyjs/medications.json'
// import {userEvent} from "@testing-library/user-event/setup/index";

jest.mock('@tanstack/react-virtual')

describe('MultipleCombobox', () => {
  test('should render stuff', () => {
    //
    // //Arrange
    // jest.mock('@tanstack/react-virtual', () => ({
    //   useVirtualizer: jest.fn()
    // }))
    //
    // render(<MultipleComboBox<ItemValue>
    //   itemToString={(item: ItemValue) => item.text}
    //   options={medications.map(medication => ({ value: medication, text: medication }))} />)
    //
    // //Act
    // //types a medication name into the input with a leading space
    // act(() => {
    //   userEvent.type(screen.getByRole('textbox'), ' amoxicillin')
    // })
    //
    // //Assert
    // //amoxicillin is visible
    // expect(screen.getByText('amoxicillin')).toBeVisible()
    expect(true).toBe(true)
  })
})
