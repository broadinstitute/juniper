import React from 'react'

import { render, screen } from '@testing-library/react'
import { makeEmptyHtmlSection } from 'test-utils/mock-site-content'
import { mockPortalEnvContext } from 'test-utils/mocking-utils'
import { setupRouterTest } from '@juniper/ui-core'
import { SectionDesigner } from './SectionDesigner'

function checkElementPresence(elements: string[]) {
  elements.forEach(element => {
    expect(screen.getByText(element)).toBeInTheDocument()
  })
}

function checkElementAbsence(elements: string[]) {
  elements.forEach(element => {
    expect(screen.queryByText(element)).not.toBeInTheDocument()
  })
}

describe('SectionDesigner', () => {
  // eslint-disable-next-line jest/expect-expect
  it('should render a HERO_CENTERED designer', () => {
    const { RoutedComponent } = setupRouterTest(
      <SectionDesigner portalEnvContext={mockPortalEnvContext('sandbox')}
        section={makeEmptyHtmlSection('HERO_CENTERED')}
        siteMediaList={[]}
        updateSection={jest.fn()}/>)
    render(RoutedComponent)

    checkElementPresence(['Title', 'Blurb', 'Buttons (0)', 'Style Options'])
    checkElementAbsence(['Logos', 'Image', 'Steps (0)', 'Photo Bio Sections (0)', 'Sections (0)', 'Questions (0)'])
  })

  // eslint-disable-next-line jest/expect-expect
  it('should render a HERO_WITH_IMAGE designer', () => {
    const { RoutedComponent } = setupRouterTest(
      <SectionDesigner portalEnvContext={mockPortalEnvContext('sandbox')}
        section={makeEmptyHtmlSection('HERO_WITH_IMAGE')}
        siteMediaList={[]}
        updateSection={jest.fn()}/>)
    render(RoutedComponent)

    checkElementPresence(['Title', 'Blurb', 'Buttons (0)', 'Image', 'Style Options'])
    checkElementAbsence(['Logos', 'Steps (0)', 'Photo Bio Sections (0)', 'Sections (0)', 'Questions (0)'])
  })

  // eslint-disable-next-line jest/expect-expect
  it('should render an FAQ designer', () => {
    const { RoutedComponent } = setupRouterTest(
      <SectionDesigner portalEnvContext={mockPortalEnvContext('sandbox')}
        section={makeEmptyHtmlSection('FAQ')}
        siteMediaList={[]}
        updateSection={jest.fn()}/>)
    render(RoutedComponent)

    checkElementPresence(['Title', 'Blurb', 'Questions (0)', 'Style Options'])
    checkElementAbsence(['Logos', 'Image', 'Steps (0)', 'Buttons (0)', 'Photo Bio Sections (0)', 'Sections (0)'])
  })

  // eslint-disable-next-line jest/expect-expect
  it('should render a BANNER_IMAGE designer', () => {
    const { RoutedComponent } = setupRouterTest(
      <SectionDesigner portalEnvContext={mockPortalEnvContext('sandbox')}
        section={makeEmptyHtmlSection('BANNER_IMAGE')}
        siteMediaList={[]}
        updateSection={jest.fn()}/>)
    render(RoutedComponent)

    checkElementPresence(['Image', 'Style Options'])
    checkElementAbsence([
      'Title', 'Blurb', 'Logos', 'Steps (0)', 'Buttons (0)', 'Questions (0)', 'Photo Bio Sections (0)', 'Sections (0)'
    ])
  })

  // eslint-disable-next-line jest/expect-expect
  it('should render a PARTICIPATION_DETAIL designer', () => {
    const { RoutedComponent } = setupRouterTest(
      <SectionDesigner portalEnvContext={mockPortalEnvContext('sandbox')}
        section={makeEmptyHtmlSection('PARTICIPATION_DETAIL')}
        siteMediaList={[]}
        updateSection={jest.fn()}/>)
    render(RoutedComponent)

    checkElementPresence(['Title', 'Blurb', 'Image', 'Style Options'])
    checkElementAbsence([
      'Logos', 'Steps (0)', 'Photo Bio Sections (0)', 'Sections (0)', 'Questions (0)', 'Buttons (0)'
    ])
  })

  // eslint-disable-next-line jest/expect-expect
  it('should render a STEP_OVERVIEW designer', () => {
    const { RoutedComponent } = setupRouterTest(
      <SectionDesigner portalEnvContext={mockPortalEnvContext('sandbox')}
        section={makeEmptyHtmlSection('STEP_OVERVIEW')}
        siteMediaList={[]}
        updateSection={jest.fn()}/>)
    render(RoutedComponent)

    checkElementPresence(['Title', 'Steps (0)', 'Buttons (0)', 'Style Options'])
    checkElementAbsence(['Logos', 'Blurb', 'Image', 'Photo Bio Sections (0)', 'Sections (0)', 'Questions (0)'])
  })

  // eslint-disable-next-line jest/expect-expect
  it('should render a PHOTO_BLURB_GRID designer', () => {
    const { RoutedComponent } = setupRouterTest(
      <SectionDesigner portalEnvContext={mockPortalEnvContext('sandbox')}
        section={makeEmptyHtmlSection('PHOTO_BLURB_GRID')}
        siteMediaList={[]}
        updateSection={jest.fn()}/>)
    render(RoutedComponent)

    checkElementPresence(['Title', 'Photo Bio Sections (0)', 'Style Options'])
    checkElementAbsence(['Logos', 'Blurb', 'Image', 'Steps (0)', 'Buttons (0)', 'Questions (0)', 'Sections (0)'])
  })

  // eslint-disable-next-line jest/expect-expect
  it('should render a FOOTER designer', () => {
    const { RoutedComponent } = setupRouterTest(
      <SectionDesigner portalEnvContext={mockPortalEnvContext('sandbox')}
        section={makeEmptyHtmlSection('LINK_SECTIONS_FOOTER')}
        siteMediaList={[]}
        updateSection={jest.fn()}/>)
    render(RoutedComponent)

    checkElementPresence(['Sections (0)'])
    checkElementAbsence([
      'Style Options', 'Title', 'Blurb', 'Image', 'Logos',
      'Steps (0)', 'Buttons (0)', 'Questions (0)', 'Photo Bio Sections (0)'
    ])
  })
})
