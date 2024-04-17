import React, { useEffect, useState } from 'react'
import { useUser } from 'providers/UserProvider'
import { Route, Routes, useNavigate, useParams } from 'react-router-dom'
import { usePortalEnv } from 'providers/PortalProvider'
import Api, { ParticipantUser, Portal, StudyEnvironment, Survey } from 'api/api'
import NavBar from '../../Navbar'
import PreEnrollView from './PreEnroll'
import StudyIneligible from './StudyIneligible'
import { HubUpdate } from 'hub/hubUpdates'
import PortalRegistrationRouter from 'landing/registration/PortalRegistrationRouter'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { useHasProvidedStudyPassword, usePreEnrollResponseId } from 'browserPersistentState'

import { StudyEnrollPasswordGate } from './StudyEnrollPasswordGate'
import { alertDefaults, AlertLevel } from '@juniper/ui-core'
import { enrollCurrentUserInStudy } from '../../util/enrolleeUtils'
import { logError } from '../../util/loggingUtils'

export type StudyEnrollContext = {
  user: ParticipantUser,
  studyEnv: StudyEnvironment,
  studyShortcode: string,
  preEnrollResponseId: string | null,
  updatePreEnrollResponseId: (newId: string | null) => void
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
  const { user, enrollees, refreshLogin } = useUser()
  const enrolleesForUser = enrollees.filter(enrollee => enrollee.participantUserId === user.id)

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

  /** route to a page depending on where in the pre-enroll/registration process the user is */
  const determineNextRoute = async () => {
    const isAlreadyEnrolled = !!enrolleesForUser.find(rollee => rollee.studyEnvironmentId === studyEnv.id)
    if (isAlreadyEnrolled) {
      const hubUpdate: HubUpdate = {
        message: {
          title: `You are already enrolled in ${studyName}.`,
          type: alertDefaults['STUDY_ALREADY_ENROLLED'].type as AlertLevel
        }
      }
      navigate('/hub', { replace: true, state: hubUpdate })
      return
    }
    if (mustProvidePassword) {
      return
    }
    if (preEnrollSatisfied) {
      if (user.isAnonymous) {
        navigate('register', { replace: true })
      } else {
        // when preEnroll is satisfied, and we have a user, we're clear to create an Enrollee
        try {
          const hubUpdate = enrollCurrentUserInStudy(studyShortcode, studyName, preEnrollResponseId, refreshLogin)
          // todo: refresh user state
          navigate('/hub', { replace: true, state: hubUpdate })
        } catch (e) {
          logError({ message: 'Error on StudyEnroll' }, (e as ErrorEvent)?.error?.stack)
          navigate('/hub', { replace: true })
        }
      }
    } else {
      navigate('preEnroll', { replace: true })
    }
  }

  // when either preEnrollment or login status changes, navigate accordingly
  useEffect(() => {
    determineNextRoute()
  }, [mustProvidePassword, preEnrollSatisfied, user.username])

  const enrollContext: StudyEnrollContext = {
    studyShortcode, studyEnv, user, preEnrollResponseId, updatePreEnrollResponseId
  }
  const hasPreEnroll = !!enrollContext.studyEnv.preEnrollSurvey
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
