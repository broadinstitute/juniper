import React, {
  useEffect,
  useState
} from 'react'
import { useUser } from 'providers/UserProvider'
import {
  Route,
  Routes,
  useNavigate,
  useParams, useSearchParams
} from 'react-router-dom'
import { usePortalEnv } from 'providers/PortalProvider'
import Api, {
  Portal,
  StudyEnvironment,
  Survey
} from 'api/api'
import NavBar from 'Navbar'
import PreEnrollView from './PreEnroll'
import StudyIneligible from './StudyIneligible'
import { HubUpdate } from 'hub/hubUpdates'
import PortalRegistrationRouter from 'landing/registration/PortalRegistrationRouter'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import {
  useHasProvidedStudyPassword,
  usePreEnrollResponseId
} from 'browserPersistentState'

import { StudyEnrollPasswordGate } from './StudyEnrollPasswordGate'
import {
  HubResponse,
  ParticipantUser,
  useI18n
} from '@juniper/ui-core'
import {
  enrollCurrentUserInStudy,
  enrollProxyUserInStudy
} from 'util/enrolleeUtils'
import { logError } from 'util/loggingUtils'
import { getNextConsentTask, getTaskPath } from 'hub/task/taskUtils'
import { useEnrollmentParams } from './useEnrollmentParams'

export type StudyEnrollContext = {
  user: ParticipantUser | null,
  studyEnv: StudyEnvironment,
  studyShortcode: string,
  portalShortcode: string,
  preEnrollResponseId: string | null,
  updatePreEnrollResponseId: (newId: string | null) => void,
  isProxyEnrollment: boolean
  isSubjectEnrollment: boolean
}

/** Handles routing and loading for enrollment in a study */
export default function StudyEnrollRouter() {
  const studyShortcode = useParams().studyShortcode

  const { portal } = usePortalEnv()
  const matchedStudy = portal.portalStudies.find(pStudy => pStudy.study.shortcode === studyShortcode)?.study
  const studyEnv = matchedStudy?.studyEnvironments[0]
  if (!studyEnv || !studyShortcode) {
    return <div>no matching study</div>
  }
  return (
    <StudyEnrollOutletMatched
      portal={portal}
      studyEnv={studyEnv}
      studyName={matchedStudy.name}
      studyShortcode={studyShortcode}
    />
  )
}

type StudyEnrollOutletMatchedProps = {
  portal: Portal
  studyEnv: StudyEnvironment
  studyName: string
  studyShortcode: string
}

/** handles the rendering and useEffect logic */
function StudyEnrollOutletMatched(props: StudyEnrollOutletMatchedProps) {
  const { portal, studyEnv, studyName, studyShortcode } = props
  const { i18n } = useI18n()

  const [searchParams] = useSearchParams()
  const { skipPreEnroll, isProxyEnrollment, ppUserId } = useEnrollmentParams()

  const { user, ppUsers, enrollees, refreshLoginState } = useUser()

  // ppUser / enrollees for the user or the proxied user depending on the context
  const ppUser = isProxyEnrollment
    ? ppUsers.find(ppUser => ppUser.id === ppUserId) // could be null if new user enrollment
    : ppUsers.find(ppUser => ppUser.participantUserId === user?.id)

  const enrolleesForUser = enrollees.filter(enrollee => enrollee.profileId === ppUser?.profileId)

  const navigate = useNavigate()
  const [preEnrollResponseId, setPreEnrollResponseId] = usePreEnrollResponseId()
  const [preEnrollSatisfied, setPreEnrollSatisfied] = useState(!studyEnv.preEnrollSurvey)

  const [hasProvidedPassword, setHasProvidedPassword] = useHasProvidedStudyPassword(studyShortcode)
  const mustProvidePassword = studyEnv.studyEnvironmentConfig.passwordProtected && !hasProvidedPassword

  /** updates the state and localStorage */
  function updatePreEnrollResponseId(preEnrollId: string | null) {
    setPreEnrollResponseId(preEnrollId)
    setPreEnrollSatisfied(!!preEnrollId)
  }

  useEffect(() => {
    // if there's a preRegResponseId on initial load (because it was in local storage) validate it and then redirect
    if (preEnrollResponseId) {
      Api.confirmPreEnrollResponse(preEnrollResponseId).then(() => {
        //this is a valid pre-reg, redirect to the enroll page
        setPreEnrollSatisfied(true)
      }).catch(() => {
        updatePreEnrollResponseId(null)
      })
    }
    // when this component is unmounted, clear the localstorage
    return () => {
      setPreEnrollResponseId(null)
    }
  }, [])

  const matchedEnrollee = enrolleesForUser.find(rollee => rollee.studyEnvironmentId === studyEnv.id)

  /** route to a page depending on where in the pre-enroll/registration process the user is */
  const determineNextRoute = async () => {
    // if the user is a proxy, they still can enroll in the study
    const isAlreadyEnrolled = !!matchedEnrollee && matchedEnrollee.subject

    if (isAlreadyEnrolled) {
      const hubUpdate: HubUpdate = {
        message: {
          title: isProxyEnrollment
            ? i18n('hubUpdateGovernedUserAlreadyEnrolledTitle', { substitutions: { studyName } })
            : i18n('hubUpdateAlreadyEnrolledTitle', { substitutions: { studyName } }),
          type: 'INFO'
        }
      }
      navigate('/hub', { replace: true, state: hubUpdate })
      return
    }
    if (mustProvidePassword) {
      return
    }
    if (preEnrollSatisfied || skipPreEnroll) {
      if (!user) {
        navigate('register', { replace: true })
      } else {
        // when preEnroll is satisfied, and we have a user, we're clear to create an Enrollee
        try {
          const hubResponse = isProxyEnrollment
            ? await enrollProxyUserInStudy(
              studyShortcode, preEnrollResponseId, ppUserId, refreshLoginState
            )
            : await enrollCurrentUserInStudy(
              studyShortcode, preEnrollResponseId, refreshLoginState
            )

          handleNewStudyEnroll(hubResponse, studyShortcode, navigate, i18n, studyName)
        } catch (e) {
          logError({ message: 'Error on StudyEnroll' }, (e as ErrorEvent)?.error?.stack)
          navigate('/hub', { replace: true })
        }
      }
    } else {
      navigate(`preEnroll?${searchParams.toString()}`, { replace: true })
    }
  }

  const [isLoading, setIsLoading] = useState(false)
  // when either preEnrollment or login status changes, navigate accordingly
  useEffect(() => {
    setIsLoading(true)
    determineNextRoute()
      .finally(() => setIsLoading(false))
  }, [mustProvidePassword, preEnrollSatisfied, user?.username])

  const enrollContext: StudyEnrollContext = {
    studyShortcode,
    portalShortcode: portal.shortcode,
    studyEnv,
    user,
    preEnrollResponseId,
    updatePreEnrollResponseId,
    isSubjectEnrollment: !!matchedEnrollee && !matchedEnrollee.subject,
    isProxyEnrollment
  }
  const hasPreEnroll = !!enrollContext.studyEnv.preEnrollSurvey

  if (isLoading) { return <PageLoadingIndicator/> }
  return <>
    <NavBar/>
    {mustProvidePassword
      ? (
        <StudyEnrollPasswordGate
          studyEnv={studyEnv}
          studyName={studyName}
          onSubmitCorrectPassword={setHasProvidedPassword}
        />
      ) : (
        <Routes>
          {skipPreEnroll &&
            <Route path="preEnroll/*" element={<PortalRegistrationRouter portal={portal} returnTo={null}/>}/>}
          {hasPreEnroll && <Route path="preEnroll" element={
            <PreEnrollView enrollContext={enrollContext} survey={enrollContext.studyEnv.preEnrollSurvey as Survey}/>
          }/>}
          <Route path="ineligible" element={<StudyIneligible portal={portal} studyName={studyName}/>}/>
          <Route path="register/*" element={<PortalRegistrationRouter portal={portal} returnTo={null}/>}/>
          <Route index element={<PageLoadingIndicator/>}/>
        </Routes>
      )}
  </>
}

export function handleNewStudyEnroll(
  hubResponse: HubResponse,
  studyShortcode: string,
  navigate: (path: string, options?: { replace?: boolean, state?: object }) => void,
  i18n: (key: string, options?: { substitutions?: { [key: string]: string } }) => string,
  studyName: string
) {
  const nextConsentTask = getNextConsentTask(hubResponse)

  if (nextConsentTask) {
    const consentTaskPath = getTaskPath(nextConsentTask, hubResponse.enrollee.shortcode, studyShortcode)
    navigate(`/hub/${consentTaskPath}`, { replace: true })
  } else {
    const hubUpdate: HubUpdate = {
      message: {
        title: i18n('hubUpdateWelcomeToStudyTitle', { substitutions: { studyName } }),
        detail: i18n('hubUpdateWelcomeToStudyDetail'),
        type: 'INFO'
      }
    }
    navigate('/hub', { replace: true, state: hubUpdate })
  }
}
