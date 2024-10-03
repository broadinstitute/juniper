import { setupRouterTest } from '@juniper/ui-core'
import { mockAdminUser, MockUserProvider } from 'test-utils/user-mocking-utils'
import { mockPortalContext, mockStudyEnvContext, mockStudyEnvironmentConfig } from 'test-utils/mocking-utils'
import { render, screen } from '@testing-library/react'
import React from 'react'
import { KitSettings } from './KitSettings'

describe('Kit Settings', () => {
  test('Does not render superuser-only kit options for non-superusers', () => {
    const { RoutedComponent } = setupRouterTest(<MockUserProvider user={mockAdminUser(false)}>
      <KitSettings
        studyEnvContext={mockStudyEnvContext()}
        portalContext={mockPortalContext()}
        config={mockStudyEnvironmentConfig()}
        updateConfig={() => {}}
      />
    </MockUserProvider>, [''])

    render(RoutedComponent)

    expect(screen.queryByText('Enable in-person kits')).toBeInTheDocument()
    expect(screen.queryByText('Use kit request development realm')).not.toBeInTheDocument()
    expect(screen.queryByText('Use mock kit requests')).not.toBeInTheDocument()
    expect(screen.queryByText('Kit types')).not.toBeInTheDocument()
  })

  test('Renders superuser-only kit options for superusers', () => {
    const { RoutedComponent } = setupRouterTest(<MockUserProvider user={mockAdminUser(true)}>
      <KitSettings
        studyEnvContext={mockStudyEnvContext()}
        portalContext={mockPortalContext()}
        config={mockStudyEnvironmentConfig()}
        updateConfig={() => {}}
      />
    </MockUserProvider>, [''])

    render(RoutedComponent)

    expect(screen.queryByText('Enable in-person kits')).toBeInTheDocument()
    expect(screen.queryByText('Use kit request development realm')).toBeInTheDocument()
    expect(screen.queryByText('Use mock kit requests')).toBeInTheDocument()
    expect(screen.queryByText('Kit types')).toBeInTheDocument()
  })
})
