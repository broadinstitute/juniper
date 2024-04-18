import Api, { Enrollee, ParticipantUser, Portal, PortalParticipantUser, Profile } from '../api/api'
import UserProvider, { useUser } from '../providers/UserProvider'
import React, { useEffect } from 'react'
import { AuthProvider } from 'react-oidc-context'
import PortalProvider from '../providers/PortalProvider'


type ProvideTestUserProps = {
  profile?: Profile,
  ppUser?: PortalParticipantUser,
  user?: ParticipantUser,
  portal?: Portal,
  enrollees?: Enrollee[],
  children: React.ReactNode
}
/**
 * Provides a Portal and User context like in the real application.
 * The props allow you to override certain aspects of the context,
 * e.g. user or portal. Useful for more deeply testing code
 * interactions with the context.
 */
export default function ProvideFullTestUserContext(
  props: ProvideTestUserProps
) {
  jest.spyOn(Api, 'getPortal').mockImplementation(() => Promise.resolve(
    props.portal || {
      id: '',
      name: '',
      shortcode: '',
      portalEnvironments: [
        {
          environmentName: 'sandbox',
          portalEnvironmentConfig: {
            initialized: true,
            acceptingRegistration: false,
            passwordProtected: false,
            password: '',
            defaultLanguage: ''
          },
          supportedLanguages: [],
          siteContent: {
            id: '',
            defaultLanguage: '',
            createdAt: 0,
            stableId: '',
            version: 0,
            localizedSiteContents: [
              {
                primaryBrandColor: 'blue',
                language: '',
                landingPage: {
                  title: '',
                  path: '',
                  sections: []
                },
                navbarItems: [],
                navLogoCleanFileName: '',
                navLogoVersion: 0
              }
            ]
          }
        }
      ],
      portalStudies: []
    }))

  return <AuthProvider>
    <PortalProvider>
      <UserProvider>
        <_ProvideTestUser
          {...props}
        >
          {props.children}
        </_ProvideTestUser>
      </UserProvider>
    </PortalProvider>
  </AuthProvider>
}

const _ProvideTestUser = ({
  profile,
  ppUser,
  user,
  enrollees = [],
  children
}: ProvideTestUserProps) => {
  const {
    loginUserInternal
  } = useUser()
  useEffect(() => {
    loginUserInternal({
      profile: profile || {},
      ppUser: ppUser || {
        id: '',
        profile: {},
        profileId: ''
      },
      user: user || {
        username: '',
        token: ''
      },
      enrollees: enrollees || []
    })
  }, [])

  return <>
    {children}
  </>
}
