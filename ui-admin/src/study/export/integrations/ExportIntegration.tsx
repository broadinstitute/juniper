import React, { useState } from 'react'
import { paramsFromContext, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { renderPageHeader } from 'util/pageUtils'
import Api, { ExportIntegration } from 'api/api'
import { doApiLoad, useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { useParams } from 'react-router-dom'
import { instantToDefaultString } from '@juniper/ui-core'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'
import { ExportDataForm } from '../ExportDataControl'
import { TextInput } from '../../../components/forms/TextInput'


export default function ExportIntegration({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const [integration, setIntegration] = useState<ExportIntegration>()
  const id = useParams().id
  const [showOptions, setShowOptions] = useState(false)
  const { isLoading } = useLoadingEffect(async () => {
    const response = await Api.fetchExportIntegration(paramsFromContext(studyEnvContext), id)
    setIntegration(response)
  }, [id])

  const runIntegration = async () => {
    if (!id) {
      return
    }
    doApiLoad(async () => {
      await Api.runExportIntegration(paramsFromContext(studyEnvContext), id)
    })
  }

  return <div className="container-fluid p-4">
    {renderPageHeader('Export Integration')}
    <LoadingSpinner isLoading={isLoading}>
      { integration && <dl>
        <dt>Name:</dt><dd>{integration.name}</dd>
        <dt>Destination:</dt><dd>{integration.destinationType}</dd>
        <dt>Runs:</dt><dd>Nightly: 1:00am</dd>
        <dt>Created:</dt><dd>{instantToDefaultString(integration.createdAt)}</dd>
        <dt>Enabled:</dt><dd>
          <label className="form-control border-0">
            <input type="checkbox" name="includeProxiesAsRows" checked={integration.enabled}
              onChange={e => setIntegration({
                ...integration,
                enabled: e.target.checked
              })}
              className="me-1"/>
          </label>
        </dd>
        <dt>Url:</dt><dd className="d-flex align-items-center">
          <code>https://api.airtable.com/</code><TextInput value={integration.destinationUrl}
            onChange={val => setIntegration({ ...integration, destinationUrl: val })} />
        </dd>
      </dl> }
      <div className="py-2">
        <Button variant="secondary" onClick={() => setShowOptions(!showOptions)}>
          <FontAwesomeIcon icon={showOptions ? faChevronDown : faChevronUp}/> Options
        </Button>
      </div>
      { (showOptions && integration?.exportOptions) && <div>
        <ExportDataForm exportOptions={integration?.exportOptions}
          setExportOptions={opts => setIntegration({
            ...integration,
            exportOptions: opts
          })}/>
      </div>}
      <div className="mt-5">
        <Button variant="primary" onClick={() => {}} className="me-3">Save</Button>
        <Button variant="secondary" outline={true} className="me-5" >View run history</Button>
        <Button variant="secondary" outline={true} onClick={runIntegration}>Run now</Button>
      </div>
    </LoadingSpinner>
  </div>
}
