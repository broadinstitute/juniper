import React, { useEffect, useState } from 'react'
import { Route, Routes, useParams } from 'react-router-dom'
import { StudyParams } from '../StudyRouter'
import Api, { Enrollee } from '../../api/api'
import { Store } from 'react-notifications-component'
import { failureNotification } from '../../util/notifications'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import LoadingSpinner from '../../util/LoadingSpinner'
import EnrolleeView from './EnrolleeView'

export type EnrolleeParams = StudyParams & {
  enrolleeShortcode: string
}

export default function EnrolleeRouter({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv } = studyEnvContext
  const params = useParams<EnrolleeParams>()
  const enrolleeShortcode = params.enrolleeShortcode as string
  const [enrollee, setEnrollee] = useState<Enrollee | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    Api.getEnrollee(portal.shortcode, study.shortcode, currentEnv.environmentName, enrolleeShortcode).then(result => {
      setEnrollee(result)
      setIsLoading(false)
    }).catch(e => {
      Store.addNotification(failureNotification(`Error loading participants`))
    })
  }, [enrolleeShortcode])

  return <LoadingSpinner isLoading={isLoading}>
    <Routes>
      <Route path="/" element={<EnrolleeView enrollee={enrollee as Enrollee} studyEnvContext={studyEnvContext}/>}>
        <Route path="profile" element={<div>profile</div>}/>
        <Route path="consents" element={<div>consents</div>}/>
        <Route path="preRegistration" element={<div>preEnroll</div>}/>
        <Route path="surveys">
          <Route path=":surveyStableId" element={<div>survey</div>}/>
          <Route path="*" element={<div>Unknown participant survey page</div>}/>
        </Route>
        <Route path="consents">
          <Route path=":consentStableId" element={<div>consent</div>}/>
          <Route path="*" element={<div>Unknown participant survey page</div>}/>
        </Route>
        <Route path="*" element={<div>unknown enrollee route</div>}/>
      </Route>
    </Routes>
  </LoadingSpinner>
}
