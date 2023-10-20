import { render, screen } from '@testing-library/react'
import React from 'react'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockHtmlPage } from 'test-utils/mock-site-content'
import HtmlSectionEditor from './HtmlSectionEditor'

test('readOnly disables section type selection', async () => {
  const mockSection = mockHtmlPage().sections[0]
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor sectionIndex={0} section={mockSection} readOnly={true} updateSection={jest.fn()}/>)
  render(RoutedComponent)
  expect(screen.getByLabelText('Select section type')).toBeDisabled()
})

test('section type selection is enabled if the section type is unsaved', async () => {
  const mockSection = mockHtmlPage().sections[0]
  mockSection.id = ''
  const { RoutedComponent } = setupRouterTest(
    <HtmlSectionEditor sectionIndex={0} section={mockSection} readOnly={false} updateSection={jest.fn()}/>)
  render(RoutedComponent)
  expect(screen.getByLabelText('Select section type')).toBeEnabled()
})
