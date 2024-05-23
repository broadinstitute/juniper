import React from 'react'

import { mockAdminUser, MockUserProvider } from 'test-utils/user-mocking-utils'
import { render, screen } from '@testing-library/react'
import { StudySidebar } from './StudySidebar'
import { mockPortal, mockStudyEnvContext } from '../test-utils/mocking-utils'
import { setupRouterTest } from '@juniper/ui-core'

test('renders the study selector and sub menus', async () => {
  const { study } = mockStudyEnvContext()
  const portal = mockPortal()
  portal.portalStudies.push({
    study
  })
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={mockAdminUser(true)}>
      <StudySidebar study={study} portalList={[portal]} portalShortcode={portal.shortcode}/>
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.getByText(study.name)).toBeInTheDocument()
  expect(screen.getByText('Research Coordination')).toBeVisible()
  expect(screen.getByText('Participant List')).toBeVisible()
  expect(screen.getByText('Analytics & Data')).toBeVisible()
})
