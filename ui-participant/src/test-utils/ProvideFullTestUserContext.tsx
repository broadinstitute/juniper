import Api, { Enrollee, EnrolleeRelation, ParticipantUser, Portal, PortalParticipantUser, Profile } from '../api/api'
import UserProvider, { useUser } from '../providers/UserProvider'
import React, { useEffect } from 'react'
import { AuthProvider } from 'react-oidc-context'
import PortalProvider from '../providers/PortalProvider'
import ActiveUserProvider, { useActiveUser } from '../providers/ActiveUserProvider'


type ProvideTestUserProps = {
  profile?: Profile,
  ppUsers?: PortalParticipantUser[],
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
  ppUsers,
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
    if (!ppUsers) {
      ppUsers = [{
        id: 'testppuserid0',
        profile: {},
        profileId: profile?.id || 'testppuserprofileid0',
        participantUserId: ''
      }]
    }

    ppUsers.forEach((ppUser, idx) => {
      if (!ppUser.id) {
        ppUser.id = `testppuserid${idx}`
      }
      if (!ppUser.profileId) {
        ppUser.profileId = profile?.id || `testppuserprofileid${idx}`
      }
    })

    if (profile && (!enrollees || enrollees.length === 0)) {
      enrollees = [{
        id: 'testenrolleeid',
        profileId: profile?.id || ppUsers[0].profileId,
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
        if (!enrollee.profileId) {
          enrollee.profileId = profile.id || (ppUsers || [])[0]?.profileId || 'testppuserprofileid'
        }
        if (!enrollee.profile) {
          enrollee.profile = profile
        }
      })
    }

    loginUserInternal({
      profile: profile || {},
      ppUsers,
      user: user || {
        id: '',
        username: '',
        token: ''
      },
      enrollees,
      relations
    })

    setActiveUser(ppUsers[0].id)
  }, [])

  return <>
    {children}
  </>
}
