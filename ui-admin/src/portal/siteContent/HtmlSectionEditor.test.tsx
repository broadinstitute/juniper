import { render, screen } from '@testing-library/react'
import React from 'react'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockHtmlPage, mockHtmlSection } from 'test-utils/mock-site-content'
import HtmlSectionEditor from './HtmlSectionEditor'
import userEvent from '@testing-library/user-event'

test('readOnly disables section type selection', async () => {
  const mockPage = mockHtmlPage()
  const mockSection = mockPage.sections[0]
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor sectionIndex={0} section={mockSection} readOnly={true}
      htmlPage={mockPage} updatePage={jest.fn}/>)
  render(RoutedComponent)
  expect(screen.getByLabelText('Select section type')).toBeDisabled()
})

test('section type selection is enabled if the section type is unsaved', async () => {
  const mockPage = mockHtmlPage()
  const mockSection = mockPage.sections[0]
  mockSection.id = ''
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor sectionIndex={0} section={mockSection} readOnly={false}
      htmlPage={mockPage} updatePage={jest.fn}/>)
  render(RoutedComponent)
  expect(screen.getByLabelText('Select section type')).toBeEnabled()
})

test('DeleteSection button removes the section', async () => {
  //Arrange
  const mockPage = mockHtmlPage()
  const mockSection = mockPage.sections[0]
  const mockUpdatePageFn = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor sectionIndex={0} section={mockSection} readOnly={false}
      htmlPage={mockPage} updatePage={mockUpdatePageFn}/>)
  render(RoutedComponent)

  //Act
  const deleteButton = screen.getByLabelText('Delete this section')
  await userEvent.click(deleteButton)

  //Assert
  expect(mockUpdatePageFn).toHaveBeenCalledWith({
    ...mockPage,
    sections: []
  })
})

test('MoveSectionUp button allows reordering', async () => {
  //Arrange
  const mockPage = {
    ...mockHtmlPage(),
    sections: [
      { ...mockHtmlSection(), id: 'firstSection' },
      { ...mockHtmlSection(), id: 'secondSection' }
    ]
  }
  const mockSection = mockPage.sections[1]
  const mockUpdatePageFn = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor sectionIndex={1} section={mockSection} readOnly={false}
      htmlPage={mockPage} updatePage={mockUpdatePageFn}/>)
  render(RoutedComponent)

  //Act
  const moveUpButton = screen.getByLabelText('Move this section before the previous one')
  await userEvent.click(moveUpButton)

  //Assert
  expect(mockUpdatePageFn).toHaveBeenCalledWith({
    ...mockPage,
    sections: [mockPage.sections[1], mockPage.sections[0]]
  })
})

test('MoveSectionDown button allows reordering', async () => {
  //Arrange
  const mockPage = {
    ...mockHtmlPage(),
    sections: [
      { ...mockHtmlSection(), id: 'firstSection' },
      { ...mockHtmlSection(), id: 'secondSection' }
    ]
  }
  const mockSection = mockPage.sections[0]
  const mockUpdatePageFn = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor sectionIndex={0} section={mockSection} readOnly={false}
      htmlPage={mockPage} updatePage={mockUpdatePageFn}/>)
  render(RoutedComponent)

  //Act
  const moveDownButton = screen.getByLabelText('Move this section after the next one')
  await userEvent.click(moveDownButton)

  //Assert
  expect(mockUpdatePageFn).toHaveBeenCalledWith({
    ...mockPage,
    sections: [mockPage.sections[1], mockPage.sections[0]]
  })
})
