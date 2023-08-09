import React from 'react'

import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockAdminUser, MockUserProvider } from 'test-utils/user-mocking-utils'
import { render, screen } from '@testing-library/react'
import { StudySidebar } from './StudySidebar'
import { mockPortal, mockStudyEnvContext } from '../test-utils/mocking-utils'

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
  expect(screen.getByText('Participant list')).toBeVisible()
  expect(screen.getByText('Analytics & Data')).toBeVisible()
})
