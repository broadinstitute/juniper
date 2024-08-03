import React from 'react'

import SiteContentEditor from './SiteContentEditor'
import { render, screen, waitFor } from '@testing-library/react'
import { emptyApi, mockSiteContent } from 'test-utils/mock-site-content'
import { userEvent } from '@testing-library/user-event'
import { mockPortal, mockPortalEnvContext, mockTwoLanguagePortal, renderInPortalRouter } from 'test-utils/mocking-utils'
import { MockI18nProvider, NavbarItemExternal, renderWithRouter, setupRouterTest } from '@juniper/ui-core'
import { select } from 'react-select-event'
import { Store } from 'react-notifications-component'

jest.mock('api/api', () => ({
  ...jest.requireActual('api/api'),
  getPortalMedia: jest.fn().mockResolvedValue([])
}))

jest.spyOn(Store, 'addNotification').mockImplementation(() => '')

test('enables live-preview json editing', async () => {
  const siteContent = mockSiteContent()
  const createNewVersionFunc = jest.fn()
  renderInPortalRouter(mockPortal(),
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={createNewVersionFunc} switchToVersion={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}/>)

  expect(screen.getByText('Landing page')).toBeInTheDocument()

  const jsonEditorTab = screen.getByText('JSON Editor')
  await userEvent.click(jsonEditorTab)

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
  renderInPortalRouter(mockPortal(),
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={true}
      loadSiteContent={jest.fn()} createNewVersion={createNewVersionFunc}
      switchToVersion={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}/>)
  expect(screen.getByText('Landing page')).toBeInTheDocument()
  expect(screen.queryByText('Save')).not.toBeInTheDocument()
})

test('clicking on the Preview tab shows full page preview', async () => {
  const siteContent = mockSiteContent()
  renderInPortalRouter(mockPortal(),
    <MockI18nProvider>
      <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
        loadSiteContent={jest.fn()} createNewVersion={jest.fn()}
        switchToVersion={jest.fn()}
        portalEnvContext={mockPortalEnvContext('sandbox')}/>
    </MockI18nProvider>
  )

  await userEvent.click(screen.getByText('Preview'))
  expect(screen.queryByText('Insert section')).not.toBeInTheDocument()
  expect(screen.queryByText('we are the best')).toBeInTheDocument()
})

test('invalid site JSON disables Save button', async () => {
  //Arrange
  const siteContent = mockSiteContent()
  renderInPortalRouter(mockPortal(),
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()}
      switchToVersion={jest.fn()} portalEnvContext={mockPortalEnvContext('sandbox')}/>)

  const jsonEditorTab = screen.getByText('JSON Editor')
  await userEvent.click(jsonEditorTab)

  //Act
  const sectionInput = screen.getByRole('textbox')
  await userEvent.type(sectionInput, '{\\\\}}') //testing-library requires escaping, this equates to "}"

  //Assert
  expect(screen.queryByText('Save')).toHaveAttribute('aria-disabled', 'true')
})

test('invalid site JSON disables Add navbar button', async () => {
  //Arrange
  const siteContent = mockSiteContent()
  renderInPortalRouter(mockPortal(),
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()}
      switchToVersion={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}/>)

  const jsonEditorTab = screen.getByText('JSON Editor')
  await userEvent.click(jsonEditorTab)

  //Act
  const sectionInput = screen.getByRole('textbox')
  await userEvent.type(sectionInput, '{\\\\}}') //testing-library requires escaping, this equates to "}"

  //Assert
  const addPageButton = screen.getByText('Add')
  expect(addPageButton).toHaveAttribute('aria-disabled', 'true')
})

test('invalid site JSON disables page selector', async () => {
  //Arrange
  const siteContent = mockSiteContent()
  renderInPortalRouter(mockPortal(),
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()} switchToVersion={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}/>)

  const jsonEditorTab = screen.getByText('JSON Editor')
  await userEvent.click(jsonEditorTab)

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
  const deletePageButton = screen.getByText('Delete')
  expect(deletePageButton).toHaveAttribute('aria-disabled', 'true')
})

test('renders a language selector when there are multiple languages', async () => {
  const siteContent = mockSiteContent()
  const portalEnvContext = mockPortalEnvContext('sandbox')
  renderInPortalRouter(mockTwoLanguagePortal(),
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()} switchToVersion={jest.fn()}
      portalEnvContext={portalEnvContext}/>)

  const languageSelector = screen.getByLabelText('Select a language')
  expect(languageSelector).toBeInTheDocument()
})

test('shows no content if nothing for a selected language', async () => {
  const siteContent = mockSiteContent()
  const portalEnvContext = mockPortalEnvContext('sandbox')
  renderInPortalRouter(mockTwoLanguagePortal(),
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()} switchToVersion={jest.fn()}
      portalEnvContext={portalEnvContext}/>)
  expect(screen.queryByText('No content has been configured for this language.')).not.toBeInTheDocument()
  await select(screen.getByLabelText('Select a language'), 'Español')
  expect(screen.getByText('No content has been configured for this language.')).toBeInTheDocument()

  await userEvent.click(screen.getByText('Clone from default'))
  expect(screen.queryByText('No content has been configured for this language.')).not.toBeInTheDocument()
})

test('selected language routes from url', async () => {
  const siteContent = mockSiteContent()
  const portalEnvContext = mockPortalEnvContext('sandbox')
  renderWithRouter(
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()} switchToVersion={jest.fn()}
      portalEnvContext={portalEnvContext}/>, ['/?lang=es'])
  expect(screen.getByText('No content has been configured for this language.')).toBeInTheDocument()
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
  renderWithRouter(
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()} switchToVersion={jest.fn()}
      portalEnvContext={mockContextOnlyEnglish}/>)

  expect(screen.queryByLabelText('Select a language')).not.toBeInTheDocument()
})

test('renders href editor for external links', async () => {
  const siteContent = {
    ...mockSiteContent(),
    localizedSiteContents: [
      {
        ...mockSiteContent().localizedSiteContents[0],
        navbarItems: [
          {
            text: 'external1',
            itemType: 'EXTERNAL',
            itemOrder: 1,
            href: 'https://example.com'
          } as NavbarItemExternal
        ]
      }
    ]
  }
  const mockContext = mockPortalEnvContext('sandbox')

  renderInPortalRouter(mockPortal(),
    <SiteContentEditor siteContent={siteContent} previewApi={emptyApi} readOnly={false}
      loadSiteContent={jest.fn()} createNewVersion={jest.fn()} switchToVersion={jest.fn()}
      portalEnvContext={mockContext}/>)

  await select(screen.getByLabelText('Select a page'), 'external1')

  expect(screen.getByLabelText('External link')).toHaveValue('https://example.com')
})
