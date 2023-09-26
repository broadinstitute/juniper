import React, { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { StudyParams } from '../../StudyRouter'
import Api, { Enrollee } from 'api/api'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import EnrolleeView from './EnrolleeView'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import { useLoadingEffect } from 'api/api-utils'

export type EnrolleeParams = StudyParams & {
  enrolleeShortcode: string,
  surveyStableId: string,
  consentStableId: string
}

/** Handles loading a specific enrollee from the server, and then delegating to EnrolleeView for rendering */
export default function EnrolleeLoader({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
  const params = useParams<EnrolleeParams>()
  const enrolleeShortcode = params.enrolleeShortcode as string
  const [enrollee, setEnrollee] = useState<Enrollee | null>(null)

  const { isLoading, reload } = useLoadingEffect(async () => {
    const result = await Api.getEnrollee(
      portal.shortcode, study.shortcode, currentEnv.environmentName, enrolleeShortcode
    )
    setEnrollee(result)
  }, [enrolleeShortcode])

  return <LoadingSpinner isLoading={isLoading}>
    <NavBreadcrumb value={enrollee?.shortcode || ''}>
      <Link to={`${currentEnvPath}/participants/${enrolleeShortcode}`}>
        {enrollee?.shortcode}</Link>
    </NavBreadcrumb>
    <EnrolleeView enrollee={enrollee as Enrollee} studyEnvContext={studyEnvContext} onUpdate={reload}/>
  </LoadingSpinner>
}
