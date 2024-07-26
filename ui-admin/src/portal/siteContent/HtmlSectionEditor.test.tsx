import { render, screen } from '@testing-library/react'
import React from 'react'
import { mockHtmlPage, mockHtmlSection } from 'test-utils/mock-site-content'
import HtmlSectionEditor from './HtmlSectionEditor'
import { userEvent } from '@testing-library/user-event'
import { sectionTemplates } from './sectionTemplates'
import { SectionType, setupRouterTest } from '@juniper/ui-core'
import { mockPortalEnvContext } from 'test-utils/mocking-utils'

test('readOnly disables section type selection', async () => {
  const mockPage = mockHtmlPage()
  const mockSection = mockPage.sections[0]
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor updateSection={jest.fn} section={mockSection} readOnly={true} allowTypeChange={true}
      portalEnvContext={mockPortalEnvContext('sandbox')}
      setSiteHasInvalidSection={jest.fn()} siteHasInvalidSection={false} siteMediaList={[]}/>)
  render(RoutedComponent)
  expect(screen.getByLabelText('Select section type')).toBeDisabled()
})

test('section type selection is enabled if the section type is unsaved', async () => {
  const mockPage = mockHtmlPage()
  const mockSection = mockPage.sections[0]
  mockSection.id = ''
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor section={mockSection} readOnly={false} allowTypeChange={true} siteMediaList={[]}
      portalEnvContext={mockPortalEnvContext('sandbox')}
      updateSection={jest.fn} setSiteHasInvalidSection={jest.fn()} siteHasInvalidSection={false}/>)
  render(RoutedComponent)
  expect(screen.getByLabelText('Select section type')).toBeEnabled()
})

test('switching section types sets the section config to the correct template', async () => {
  //Arrange
  const mockPage = mockHtmlPage()
  const mockSection = mockPage.sections[0]
  mockSection.id = ''
  const mockUpdateSectionFn = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor section={mockSection} readOnly={false} allowTypeChange={true} updateSection={mockUpdateSectionFn}
      portalEnvContext={mockPortalEnvContext('sandbox')}
      setSiteHasInvalidSection={jest.fn()} siteMediaList={[]} siteHasInvalidSection={false}/>)
  render(RoutedComponent)

  //Act
  const select = screen.getByLabelText('Select section type')
  await userEvent.click(select)
  const option = screen.getByText('FAQ')
  await userEvent.click(option)

  //Assert
  expect(mockUpdateSectionFn).toHaveBeenCalledWith({
    ...mockSection,
    sectionType: 'FAQ',
    sectionConfig: JSON.stringify(sectionTemplates['FAQ'])
  })
})

test('DeleteSection button removes the section', async () => {
  //Arrange
  const mockPage = mockHtmlPage()
  const mockSection = mockPage.sections[0]
  const mockDeleteFn = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor section={mockSection} readOnly={false} siteMediaList={[]} allowTypeChange={false}
      portalEnvContext={mockPortalEnvContext('sandbox')}
      updateSection={jest.fn()} removeSection={mockDeleteFn} setSiteHasInvalidSection={jest.fn()}
      siteHasInvalidSection={false}/>)
  render(RoutedComponent)

  //Act
  const deleteButton = screen.getByLabelText('Delete this section')
  await userEvent.click(deleteButton)

  //Assert
  expect(mockDeleteFn).toHaveBeenCalled()
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
  const mockMoveSectionFn = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor section={mockSection} readOnly={false} allowTypeChange={false} updateSection={jest.fn}
      portalEnvContext={mockPortalEnvContext('sandbox')}
      siteMediaList={[]}
      moveSection={mockMoveSectionFn} setSiteHasInvalidSection={jest.fn()} siteHasInvalidSection={false}/>)
  render(RoutedComponent)

  //Act
  const moveUpButton = screen.getByLabelText('Move this section before the previous one')
  await userEvent.click(moveUpButton)

  //Assert
  expect(mockMoveSectionFn).toHaveBeenCalledWith('up')
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
  const mockMoveSectionFn = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor section={mockSection} readOnly={false} updateSection={jest.fn} allowTypeChange={false}
      portalEnvContext={mockPortalEnvContext('sandbox')}
      siteMediaList={[]}
      moveSection={mockMoveSectionFn} setSiteHasInvalidSection={jest.fn()} siteHasInvalidSection={false}/>)
  render(RoutedComponent)

  //Act
  const moveDownButton = screen.getByLabelText('Move this section after the next one')
  await userEvent.click(moveDownButton)

  //Assert
  expect(mockMoveSectionFn).toHaveBeenCalledWith('down')
})

test('invalid JSON shows an error around the textbox', async () => {
  //Arrange
  const mockPage = mockHtmlPage()
  const mockSection = mockPage.sections[0]
  const mockUpdateSectionFn = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor section={mockSection} readOnly={false} allowTypeChange={false}
      portalEnvContext={mockPortalEnvContext('sandbox')}
      siteMediaList={[]}
      updateSection={mockUpdateSectionFn} setSiteHasInvalidSection={jest.fn()} siteHasInvalidSection={false}/>)
  render(RoutedComponent)

  //Act
  const input = screen.getByRole('textbox')
  await userEvent.type(input, '{\\\\}}') //testing-library requires escaping, this equates to "}"

  //Assert
  expect(mockUpdateSectionFn).not.toHaveBeenCalled()
  expect(input).toHaveClass('is-invalid')
})

test('invalid JSON disables moveSection buttons', async () => {
  //Arrange
  const mockPage = mockHtmlPage()
  const mockSection = mockPage.sections[0]
  const mockUpdateSectionFn = jest.fn()
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor section={mockSection} readOnly={false} moveSection={jest.fn()} removeSection={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}
      allowTypeChange={false} updateSection={mockUpdateSectionFn} siteMediaList={[]}
      setSiteHasInvalidSection={jest.fn()} siteHasInvalidSection={true}/>)
  render(RoutedComponent)

  //Act
  const input = screen.getByRole('textbox')
  await userEvent.type(input, '{\\\\}}') //testing-library requires escaping, this equates to "}"

  //Assert
  expect(screen.getByLabelText('Move this section before the previous one')).toHaveAttribute('aria-disabled', 'true')
  expect(screen.getByLabelText('Move this section after the next one')).toHaveAttribute('aria-disabled', 'true')
})

test('RAW_HTML sections should load content from the rawContent field', async () => {
  //Arrange
  const mockSection = {
    id: 'testSection',
    sectionType: 'RAW_HTML' as SectionType,
    rawContent: '<p>Test</p>'
  }

  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor section={mockSection} readOnly={false} allowTypeChange={false} updateSection={jest.fn()}
      portalEnvContext={mockPortalEnvContext('sandbox')}
      siteMediaList={[]}
      setSiteHasInvalidSection={jest.fn()} siteHasInvalidSection={false}/>)
  render(RoutedComponent)

  //Act
  const input = screen.getByRole('textbox')

  //Assert
  expect(input).toHaveValue(mockSection.rawContent)
})
