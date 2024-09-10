import { render, screen } from '@testing-library/react'
import React from 'react'
import { mockHtmlPage } from 'test-utils/mock-site-content'
import HtmlPageEditView from './HtmlPageEditView'
import { userEvent } from '@testing-library/user-event'
import { sectionTemplates } from './sectionTemplates'
import { setupRouterTest } from '@juniper/ui-core'
import { mockPortalEnvContext } from 'test-utils/mocking-utils'
import { Store } from 'react-notifications-component'

jest.spyOn(Store, 'addNotification').mockImplementation(() => '')

jest.mock('api/api', () => ({
  ...jest.requireActual('api/api'),
  getPortalMedia: jest.fn().mockResolvedValue([])
}))

test('readOnly disables insert new section button', async () => {
  const mockPage = mockHtmlPage()
  const { RoutedComponent } = setupRouterTest(
    <HtmlPageEditView htmlPage={mockPage} readOnly={true} updatePage={jest.fn()} setSiteHasInvalidSection={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}
      siteHasInvalidSection={false} footerSection={undefined} updateFooter={jest.fn()}/>)
  render(RoutedComponent)
  expect(screen.getAllByLabelText('Insert a blank section')[0]).toHaveAttribute('aria-disabled', 'true')
})

test('Insert Section button calls updatePage with a new blank HERO_WITH_IMAGE section', async () => {
  //Arrange
  const mockPage = mockHtmlPage()
  const mockUpdatePageFn = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <HtmlPageEditView htmlPage={mockPage} readOnly={false} updatePage={mockUpdatePageFn}
      setSiteHasInvalidSection={jest.fn()} portalEnvContext={mockPortalEnvContext('sandbox')}
      siteHasInvalidSection={false} footerSection={undefined} updateFooter={jest.fn()}/>)
  render(RoutedComponent)

  //Act
  const insertSectionButton = screen.getAllByLabelText('Insert a blank section')[1]
  await userEvent.click(insertSectionButton)

  //Assert
  expect(insertSectionButton).toHaveAttribute('aria-disabled', 'false')
  expect(mockUpdatePageFn).toHaveBeenCalledWith({
    ...mockPage,
    sections: [
      ...mockPage.sections,
      { id: '', sectionType: 'HERO_WITH_IMAGE', sectionConfig: JSON.stringify(sectionTemplates['HERO_WITH_IMAGE']) }
    ]
  })
})

test('invalid JSON disables Insert Section button', async () => {
  const mockPage = mockHtmlPage()
  const { RoutedComponent } = setupRouterTest(
    <HtmlPageEditView htmlPage={mockPage} readOnly={false} updatePage={jest.fn()} setSiteHasInvalidSection={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}
      siteHasInvalidSection={true} footerSection={undefined} updateFooter={jest.fn()}/>)
  render(RoutedComponent)
  const sectionButtons= await screen.findAllByLabelText('Insert a blank section')

  sectionButtons.forEach(button => {
    expect(button).toHaveAttribute('aria-disabled', 'true')
  })
})
