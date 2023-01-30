import React, { useEffect, useState } from 'react'
import { Link, Route, Routes, useParams } from 'react-router-dom'
import { StudyParams } from '../StudyRouter'
import Api, { Enrollee } from '../../api/api'
import { Store } from 'react-notifications-component'
import { failureNotification } from '../../util/notifications'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import LoadingSpinner from '../../util/LoadingSpinner'
import EnrolleeView from './EnrolleeView'
import { NavBreadcrumb } from '../../navbar/AdminNavbar'

export type EnrolleeParams = StudyParams & {
  enrolleeShortcode: string,
  surveyStableId: string,
  consentStableId: string
}

export default function EnrolleeRouter({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
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
    <NavBreadcrumb>
      <Link className="text-white" to={`${currentEnvPath}/enrollees/${enrolleeShortcode}`}>
        {enrollee?.shortcode}</Link>
    </NavBreadcrumb>
    <EnrolleeView enrollee={enrollee as Enrollee} studyEnvContext={studyEnvContext}/>
  </LoadingSpinner>
}
