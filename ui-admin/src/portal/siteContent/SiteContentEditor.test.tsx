import React from 'react'

import SiteContentEditor from './SiteContentEditor'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen, waitFor } from '@testing-library/react'
import { emptyApi, mockSiteContent } from 'test-utils/mock-site-content'
import userEvent from '@testing-library/user-event'
import { mockPortalEnvContext } from 'test-utils/mocking-utils'
import { MockI18nProvider } from '@juniper/ui-core'

test('enables live-preview text editing', async () => {
  const siteContent = mockSiteContent()
  const createNewVersionFunc = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={createNewVersionFunc} switchToVersion={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}/>)
  render(RoutedComponent)

  expect(screen.getByText('Landing page')).toBeInTheDocument()

  const sectionInput = screen.getByRole('textbox')
  const aboutUsHeading = screen.queryAllByRole('heading')
    .find(el => el.textContent === 'about us')
  expect(aboutUsHeading).toBeInTheDocument()
  userEvent.pointer({ target: sectionInput, offset: 22, keys: '[MouseLeft]' })
  userEvent.keyboard('!!')

  await waitFor(() => {
    const aboutUsNewHeading = screen.queryAllByRole('heading')
      .find(el => el.textContent === 'about us!!')
    return expect(aboutUsNewHeading).toBeInTheDocument()
  })

  await userEvent.click(screen.getByText('Save'))
  const expectedSaveObj = { ...siteContent }
  expectedSaveObj.localizedSiteContents[0].landingPage.sections[0].sectionConfig = JSON.stringify({
    title: 'about us!!', blurb: 'we are the best'
  }, null, 2)
  expect(createNewVersionFunc).toHaveBeenCalledWith(expectedSaveObj)
})

test('readOnly hides save button', async () => {
  const siteContent = mockSiteContent()
  const createNewVersionFunc = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={true}
      loadSiteContent={jest.fn()} createNewVersion={createNewVersionFunc}
      switchToVersion={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}/>)
  render(RoutedComponent)
  expect(screen.getByText('Landing page')).toBeInTheDocument()
  expect(screen.queryByText('Save')).not.toBeInTheDocument()
})

test('clicking on the Preview tab shows full page preview', async () => {
  const siteContent = mockSiteContent()
  const { RoutedComponent } = setupRouterTest(
    <MockI18nProvider>
      <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
        loadSiteContent={jest.fn()} createNewVersion={jest.fn()}
        switchToVersion={jest.fn()}
        portalEnvContext={mockPortalEnvContext('sandbox')}/>
    </MockI18nProvider>
  )
  render(RoutedComponent)

  await userEvent.click(screen.getByText('Preview'))
  expect(screen.queryByText('Insert section')).not.toBeInTheDocument()
  expect(screen.queryByText('we are the best')).toBeInTheDocument()
})

test('invalid site JSON disables Save button', async () => {
  //Arrange
  const siteContent = mockSiteContent()
  const { RoutedComponent } = setupRouterTest(
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()}
      switchToVersion={jest.fn()} portalEnvContext={mockPortalEnvContext('sandbox')}/>)
  render(RoutedComponent)

  //Act
  const sectionInput = screen.getByRole('textbox')
  await userEvent.type(sectionInput, '{\\\\}}') //testing-library requires escaping, this equates to "}"

  //Assert
  expect(screen.queryByText('Save')).toHaveAttribute('aria-disabled', 'true')
})

test('invalid site JSON disables Add Page button', async () => {
  //Arrange
  const siteContent = mockSiteContent()
  const { RoutedComponent } = setupRouterTest(
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()}
      switchToVersion={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}/>)
  render(RoutedComponent)

  //Act
  const sectionInput = screen.getByRole('textbox')
  await userEvent.type(sectionInput, '{\\\\}}') //testing-library requires escaping, this equates to "}"

  //Assert
  const addPageButton = screen.getByText('Add page')
  expect(addPageButton).toHaveAttribute('aria-disabled', 'true')
})

test('invalid site JSON disables page selector', async () => {
  //Arrange
  const siteContent = mockSiteContent()
  const { RoutedComponent } = setupRouterTest(
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()} switchToVersion={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}/>)
  render(RoutedComponent)

  //Act
  const sectionInput = screen.getByRole('textbox')
  await userEvent.type(sectionInput, '{\\\\}}') //testing-library requires escaping, this equates to "}"

  //Assert
  const pageSelector = screen.getByLabelText('Select a page')
  expect(pageSelector).toBeDisabled()
})

test('delete page button is disabled when Landing page is selected', async () => {
  //Arrange
  const siteContent = mockSiteContent()
  const { RoutedComponent } = setupRouterTest(
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()} switchToVersion={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}/>)
  render(RoutedComponent)

  //Assert
  const deletePageButton = screen.getByText('Delete page')
  expect(deletePageButton).toHaveAttribute('aria-disabled', 'true')
})

test('renders a language selector when there are multiple languages', async () => {
  const siteContent = mockSiteContent()
  const portalEnvContext = mockPortalEnvContext('sandbox')
  const { RoutedComponent } = setupRouterTest(
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()} switchToVersion={jest.fn()}
      portalEnvContext={portalEnvContext}/>)
  render(RoutedComponent)

  const languageSelector = screen.getByLabelText('Select a language')
  expect(languageSelector).toBeInTheDocument()
})

test('does not render a language selector when there is only one language', async () => {
  const siteContent = mockSiteContent()
  const mockContext = mockPortalEnvContext('sandbox')
  const mockContextOnlyEnglish = {
    ...mockPortalEnvContext('sandbox'),
    portalEnv: {
      ...mockContext.portalEnv,
      supportedLanguages: []
    }
  }
  const { RoutedComponent } = setupRouterTest(
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()} switchToVersion={jest.fn()}
      portalEnvContext={mockContextOnlyEnglish}/>)
  render(RoutedComponent)

  expect(screen.queryByLabelText('Select a language')).not.toBeInTheDocument()
})
