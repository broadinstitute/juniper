import React from 'react'
import { render, screen } from '@testing-library/react'

import PortalEnvPublishControl from './PortalEnvPublishControl'
import { Portal, PortalEnvironment } from 'api/api'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { studyDiffPath } from '../../study/StudyRouter'

test('renders a copy link', () => {
  const sandboxEnv :PortalEnvironment = {
    environmentName: 'sandbox',
    portalEnvironmentConfig: {
      initialized: true,
      acceptingRegistration: false,
      passwordProtected: false,
      password: ''
    }
  }
  const irbEnv :PortalEnvironment = {
    environmentName: 'irb',
    portalEnvironmentConfig: {
      initialized: false,
      acceptingRegistration: false,
      passwordProtected: false,
      password: ''
    }
  }
  const portal: Portal = {
    id: '11111111-1111-1111-1111-111111111111',
    shortcode: 'foo',
    name: 'testPortal',
    portalStudies: [],
    portalEnvironments: [sandboxEnv, irbEnv],
    portalLanguages: []
  }
  const { RoutedComponent } = setupRouterTest(<PortalEnvPublishControl
    portal={portal} studyShortcode={'bar'}  destEnvName={'irb'} />)
  render(RoutedComponent)
  const copyLink = screen.getByText('Copy from sandbox')
  expect(copyLink).toBeInTheDocument()
  expect(copyLink).toHaveAttribute('href',
    studyDiffPath(portal.shortcode, 'bar', 'sandbox', 'irb'))
  // irb link shouldn't exist since irb env isn't initialized
  expect(screen.queryByText('Copy from irb')).toBeNull()
})
