import Api, { Enrollee, EnrolleeRelation, ParticipantUser, Portal, PortalParticipantUser, Profile } from '../api/api'
import UserProvider, { useUser } from '../providers/UserProvider'
import React, { useEffect } from 'react'
import { AuthProvider } from 'react-oidc-context'
import PortalProvider from '../providers/PortalProvider'
import ActiveUserProvider, { useActiveUser } from '../providers/ActiveUserProvider'


type ProvideTestUserProps = {
  profile?: Profile,
  ppUser?: PortalParticipantUser,
  user?: ParticipantUser,
  portal?: Portal,
  enrollees?: Enrollee[],
  relations?: EnrolleeRelation[]
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
            password: ''
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
        <ActiveUserProvider>
          <_ProvideTestUser
            {...props}
          >
            {props.children}
          </_ProvideTestUser>
        </ActiveUserProvider>
      </UserProvider>
    </PortalProvider>
  </AuthProvider>
}

const _ProvideTestUser = ({
  profile,
  ppUser,
  user,
  enrollees = [],
  relations = [],
  children
}: ProvideTestUserProps) => {
  const {
    loginUserInternal
  } = useUser()
  const {
    setActiveUser
  } = useActiveUser()
  useEffect(() => {
    if (!ppUser) {
      ppUser = {
        id: 'testppuserid',
        profile: {},
        profileId: ''
      }
    }
    if (!ppUser.id) {
      ppUser.id = 'testppuserid'
    }
    if (!ppUser.profileId) {
      ppUser.profileId = 'testppuserprofileid'
    }

    if (profile && (!enrollees || enrollees.length === 0)) {
      enrollees = [{
        id: 'testenrolleeid',
        profileId: ppUser.profileId,
        subject: true,
        consented: true,
        profile,
        consentResponses: [],
        kitRequests: [],
        createdAt: 0,
        lastUpdatedAt: 0,
        participantTasks: [],
        participantUserId: '',
        shortcode: 'AABBCC',
        studyEnvironmentId: '',
        surveyResponses: []
      }]
    } else if (profile) {
      enrollees.forEach(enrollee => {
        enrollee.profileId = ppUser?.profileId || 'testppuserprofileid'
        enrollee.profile = profile
      })
    }

    loginUserInternal({
      profile: profile || {},
      ppUsers: [ppUser || {
        id: 'testppuserid',
        profile: {},
        profileId: ''
      }],
      user: user || {
        id: '',
        username: '',
        token: ''
      },
      enrollees: enrollees || [],
      relations: relations || []
    })

    setActiveUser(ppUser.id)
  }, [])

  return <>
    {children}
  </>
}
