import React from 'react'
import { render, screen } from '@testing-library/react'

import PortalEnvPublishControl from './PortalEnvPublishControl'
import { Portal, PortalEnvironment } from 'api/api'
import { studyDiffPath } from 'study/StudyRouter'
import { setupRouterTest } from '@juniper/ui-core'

test('renders a copy link', () => {
  const sandboxEnv :PortalEnvironment = {
    environmentName: 'sandbox',
    portalEnvironmentConfig: {
      initialized: true,
      acceptingRegistration: false,
      passwordProtected: false,
      password: '',
      defaultLanguage: 'en'
    },
    supportedLanguages: [],
    createdAt: 0
  }
  const irbEnv :PortalEnvironment = {
    environmentName: 'irb',
    portalEnvironmentConfig: {
      initialized: false,
      acceptingRegistration: false,
      passwordProtected: false,
      password: '',
      defaultLanguage: 'en'
    },
    supportedLanguages: [],
    createdAt: 0
  }
  const portal: Portal = {
    id: '11111111-1111-1111-1111-111111111111',
    shortcode: 'foo',
    name: 'testPortal',
    portalStudies: [],
    portalEnvironments: [sandboxEnv, irbEnv]
  }
  const { RoutedComponent } = setupRouterTest(<PortalEnvPublishControl
    portal={portal} studyShortcode={'bar'}  destEnvName={'irb'} />)
  render(RoutedComponent)
  const copyLink = screen.getByText('Compare to sandbox')
  expect(copyLink).toBeInTheDocument()
  expect(copyLink).toHaveAttribute('href',
    studyDiffPath(portal.shortcode, 'bar', 'sandbox', 'irb'))
  // irb link shouldn't exist since irb env isn't initialized
  expect(screen.queryByText('Compare to irb')).toBeNull()
})
