import React from 'react'

import {
  mockAdminUser,
  MockUserProvider
} from 'test-utils/user-mocking-utils'
import {
  render,
  screen
} from '@testing-library/react'
import { StudySidebar } from './StudySidebar'
import {
  mockPortal,
  mockStudyEnvContext
} from '../test-utils/mocking-utils'
import { setupRouterTest } from '@juniper/ui-core'
import { act } from 'react-dom/test-utils'

test('renders the study selector and sub menus', async () => {
  const { study } = mockStudyEnvContext()
  const portal = mockPortal()
  portal.portalStudies.push({
    createdAt: 0,
    study
  })
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={mockAdminUser(true)}>
      <StudySidebar
        study={study}
        portalList={[portal]}
        portalShortcode={portal.shortcode}
        studySidebarConfig={{ hidden: [] }}
        isEditingSidebarConfig={false}
        toggleHiddenItem={() => {
        }}
      />
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.getByText(study.name)).toBeInTheDocument()
  expect(screen.getByText('Research Coordination')).toBeVisible()
  expect(screen.getByText('Participants')).toBeVisible()
  expect(screen.getByText('Study Trends')).toBeVisible()

  expect(screen.queryByText('Toggle visibility for Participants')).not.toBeInTheDocument()
})


test('hides hidden items', async () => {
  const { study } = mockStudyEnvContext()
  const portal = mockPortal()
  portal.portalStudies.push({
    createdAt: 0,
    study
  })
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={mockAdminUser(true)}>
      <StudySidebar
        study={study}
        portalList={[portal]}
        portalShortcode={portal.shortcode}
        studySidebarConfig={{ hidden: ['participants'] }}
        isEditingSidebarConfig={false}
        toggleHiddenItem={() => {
        }}
      />
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.getByText(study.name)).toBeInTheDocument()
  expect(screen.getByText('Research Coordination')).toBeVisible()
  expect(screen.queryByText('Participants')).not.toBeInTheDocument()
  expect(screen.getByText('Study Trends')).toBeVisible()
})

test('toggles hidden items', async () => {
  const { study } = mockStudyEnvContext()
  const portal = mockPortal()
  portal.portalStudies.push({
    createdAt: 0,
    study
  })

  const toggleHiddenItem = jest.fn()

  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={mockAdminUser(true)}>
      <StudySidebar
        study={study}
        portalList={[portal]}
        portalShortcode={portal.shortcode}
        studySidebarConfig={{ hidden: [] }}
        isEditingSidebarConfig={true}
        toggleHiddenItem={toggleHiddenItem}
      />
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.getByText(study.name)).toBeInTheDocument()
  expect(screen.queryByText('Participants')).toBeVisible()

  expect(toggleHiddenItem).not.toHaveBeenCalled()

  act(() => screen.getByLabelText('Toggle visibility for Participants').click())

  expect(toggleHiddenItem).toHaveBeenCalledWith('participants')
})
