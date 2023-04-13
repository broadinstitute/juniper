import React, { useEffect, useState } from 'react'
import { useUser } from 'providers/UserProvider'
import { Route, Routes, useNavigate, useParams } from 'react-router-dom'
import { usePortalEnv } from 'providers/PortalProvider'
import Api, { ParticipantUser, Portal, StudyEnvironment } from 'api/api'
import NavBar from '../../Navbar'
import PreEnrollView from './PreEnroll'
import StudyIneligible from './StudyIneligible'
import { HubUpdate } from 'hub/hubUpdates'
import PortalRegistrationRouter from 'landing/registration/PortalRegistrationRouter'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { useHasProvidedStudyPassword, usePreEnrollResponseId } from 'browserPersistentState'

import { StudyEnrollPasswordGate } from './StudyEnrollPasswordGate'

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
  const { user, enrollees, updateEnrollee } = useUser()
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

  // when either preEnrollment or login status changes, navigate accordingly
  useEffect(() => {
    const isAlreadyEnrolled = !!enrollees.find(rollee => rollee.studyEnvironmentId === studyEnv.id)
    if (isAlreadyEnrolled) {
      alert('you are already enrolled in this study')
      navigate('/hub', { replace: true })
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
        Api.createEnrollee({ studyShortcode, preEnrollResponseId }).then(response => {
          updateEnrollee(response.enrollee)
          const hubUpdate: HubUpdate = {
            message: {
              title: 'Welcome to the study.',
              detail: 'Please read and sign the consent form below to complete registration.',
              type: 'info'
            }
          }
          navigate('/hub', { replace: true, state: hubUpdate })
        }).catch(() => {
          alert('an error occurred, please try again, or contact support')
        })
      }
    } else {
      navigate('preEnroll', { replace: true })
    }
  }, [mustProvidePassword, preEnrollSatisfied, user.username])

  const enrollContext: StudyEnrollContext = {
    studyShortcode, studyEnv, user, preEnrollResponseId, updatePreEnrollResponseId
  }
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
          <Route path="preEnroll" element={<PreEnrollView enrollContext={enrollContext}/>}/>
          <Route path="ineligible" element={<StudyIneligible portal={portal} studyName={studyName}/>}/>
          <Route path="register/*" element={<PortalRegistrationRouter portal={portal} returnTo={null}/>}/>
          <Route index element={<PageLoadingIndicator/>}/>
        </Routes>
      )}
  </>
}
