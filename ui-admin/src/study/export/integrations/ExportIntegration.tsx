import React, { useState } from 'react'
import { paramsFromContext, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { renderPageHeader } from 'util/pageUtils'
import Api, { ExportIntegration } from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { useParams } from 'react-router-dom'
import { instantToDefaultString } from '@juniper/ui-core'


export default function ExportIntegration({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const [integration, setIntegration] = useState<ExportIntegration>()
  const id = useParams().id

  const { isLoading } = useLoadingEffect(async () => {
    const response = await Api.fetchExportIntegration(paramsFromContext(studyEnvContext), id)
    setIntegration(response)
  }, [id])

  return <div className="container">
    {renderPageHeader('Export Integration')}
    <LoadingSpinner isLoading={isLoading}>
      <div>
        <div>Destination: {integration?.destinationType}</div>
        <div>Created: {instantToDefaultString(integration?.createdAt)}</div>
        <div>Enabled: {integration?.enabled ? 'Yes' : 'No'}</div>
      </div>
    </LoadingSpinner>
  </div>
}
