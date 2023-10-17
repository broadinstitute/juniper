import { render, screen } from '@testing-library/react'
import React from 'react'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockHtmlPage } from 'test-utils/mock-site-content'
import HtmlPageEditView from './HtmlPageEditView'
import userEvent from '@testing-library/user-event'

test('readOnly disables insert new section button', async () => {
  const mockPage = mockHtmlPage()
  const { RoutedComponent } = setupRouterTest(
    <HtmlPageEditView htmlPage={mockPage} readOnly={true} updatePage={jest.fn()}/>)
  render(RoutedComponent)
  expect(screen.getByLabelText('Insert a blank section')).toHaveAttribute('aria-disabled', 'true')
})

test('Insert Section button calls updatePage with a new blank HERO_WITH_IMAGE section', async () => {
  //Arrange
  const mockPage = mockHtmlPage()
  const mockUpdatePageFn = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <HtmlPageEditView htmlPage={mockPage} readOnly={false} updatePage={mockUpdatePageFn}/>)
  render(RoutedComponent)

  //Act
  const insertSectionButton = screen.getByLabelText('Insert a blank section')
  await userEvent.click(insertSectionButton)

  //Assert
  expect(insertSectionButton).toHaveAttribute('aria-disabled', 'false')
  expect(mockUpdatePageFn).toHaveBeenCalledWith({
    ...mockPage,
    sections: [
      ...mockPage.sections,
      { id: '', sectionType: 'HERO_WITH_IMAGE' }
    ]
  })
})
