import React, { useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { ExportOptions } from 'api/api'
import { currentIsoDate } from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import { saveBlobAsDownload } from 'util/downloadUtils'
import { doApiLoad } from 'api/api-utils'
import { buildFilter } from 'util/exportUtils'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'
import Select from 'react-select'
import { useReactMultiSelect } from 'util/react-select-utils'
import InfoPopup from '../../components/forms/InfoPopup'

const FILE_FORMATS = [{
  label: 'Tab-delimited (.tsv)',
  value: 'TSV',
  fileSuffix: 'tsv'
}, {
  label: 'Comma-delimited (.csv)',
  value: 'CSV',
  fileSuffix: 'csv'
}, {
  label: 'Excel (.xlsx)',
  value: 'EXCEL',
  fileSuffix: 'xlsx'
}]

const DEFAULT_EXPORT_OPTS: ExportOptions = {
  splitOptionsIntoColumns: false,
  stableIdsForOptions: false,
  fileFormat: 'TSV',
  includeSubHeaders: true,
  onlyIncludeMostRecent: true,
  filterString: undefined,
  excludeModules: [],
  includeFields: []
}

const MODULE_EXCLUDE_OPTIONS: Record<string, string> = { surveys: 'Surveys', profile: 'Profile', account: 'Account' }

/** form for configuring and downloading enrollee data */
const ExportDataModal = ({ studyEnvContext, show, setShow }: {studyEnvContext: StudyEnvContextT, show: boolean,
                           setShow:  React.Dispatch<React.SetStateAction<boolean>>}) => {
  const [isLoading, setIsLoading] = useState(false)
  const [exportOptions, setExportOptions] = useState<ExportOptions>(DEFAULT_EXPORT_OPTS)

  const doExport = () => {
    doApiLoad(async () => {
      const response = await Api.exportEnrollees(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, exportOptions)
      const fileSuffix = FILE_FORMATS.find(format =>
        exportOptions.fileFormat === format.value)?.fileSuffix
      const fileName = `${currentIsoDate()}-enrollees.${fileSuffix}`
      const blob = await response.blob()
      saveBlobAsDownload(blob, fileName)
    }, { setIsLoading })
  }

  const doDictionaryExport = () => {
    doApiLoad(async () => {
      const response = await Api.exportDictionary(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, exportOptions)
      const fileName = `${currentIsoDate()}-DataDictionary.xlsx`
      const blob = await response.blob()
      saveBlobAsDownload(blob, fileName)
    }, { setIsLoading })
  }

  return <Modal show={show} onHide={() => setShow(false)} size="lg">
    <Modal.Header closeButton>
      <Modal.Title>
        Download
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <ExportOptionsForm exportOptions={exportOptions} setExportOptions={setExportOptions}/>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={doExport}>Download</button>
        <button className="btn btn-secondary" onClick={doDictionaryExport}>Download dictionary (.xlsx)</button>
        <button className="btn btn-secondary" onClick={() => setShow(false)}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export function ExportOptionsForm({ exportOptions, setExportOptions }:
  { exportOptions: ExportOptions, setExportOptions: (opts: ExportOptions) => void }) {
  const [showAdvancedOptions, setShowAdvancedOptions] = useState(false)


  const { selectInputId, selectedOptions, options, onChange } = useReactMultiSelect<string>(
    Object.keys(MODULE_EXCLUDE_OPTIONS),
    key => ({ label: MODULE_EXCLUDE_OPTIONS[key], value: key }),
    (excludeModules: string[]) => setExportOptions({ ...exportOptions, excludeModules }),
    exportOptions.excludeModules
  )

  const includeUnconsented =
    !exportOptions.filterString?.includes('{enrollee.consented} = true')

  const includeProxiesAsRows =
    !exportOptions.filterString?.includes('{enrollee.subject} = true')


  return <div>
    <div className="py-2">
      <p className="fw-bold mb-1">
        Data format
      </p>
      <label className="form-control border-0">
        <input type="radio" name="humanReadable" value="true" checked={!exportOptions.stableIdsForOptions}
          onChange={e => {
            setExportOptions({
              ...exportOptions,
              splitOptionsIntoColumns: e.target.value !== 'true',
              stableIdsForOptions: e.target.value !== 'true'
            })
          }} className="me-1"/> Human-readable
      </label>
      <label className="form-control border-0">
        <input type="radio" name="humanReadable" value="false" checked={exportOptions.stableIdsForOptions}
          onChange={e => {
            setExportOptions({
              ...exportOptions,
              splitOptionsIntoColumns: e.target.value !== 'true',
              stableIdsForOptions: e.target.value !== 'true'
            })
          }} className="me-1"/> Analysis-friendly
      </label>
    </div>
    <div className="py-2">
      <span className="fw-bold">File format</span><br/>
      {FILE_FORMATS.map(format => <label className="form-control border-0" key={format.value}>
        <input type="radio" name="fileFormat" value="TSV" checked={exportOptions.fileFormat === format.value}
          onChange={() => setExportOptions({ ...exportOptions, fileFormat: format.value })}
          className="me-1"/>
        {format.label}
      </label>)}
    </div>
    <div className="py-2">
      <Button variant="secondary" onClick={() => setShowAdvancedOptions(!showAdvancedOptions)}>
        <FontAwesomeIcon icon={showAdvancedOptions ? faChevronDown : faChevronUp}/> Advanced Options
      </Button>
    </div>
    { showAdvancedOptions && <div className="px-3">
      <div className="py-2">
        <p className="fw-bold mb-1">
          Completions included of a survey (for recurring surveys)
        </p>
        <label className="form-control border-0">
          <input type="radio" name="onlyIncludeMostRecent" value="true" checked={exportOptions.onlyIncludeMostRecent}
            onChange={() => setExportOptions({ ...exportOptions, onlyIncludeMostRecent: true })}
            className="me-1" disabled={true}/>
          Only include most recent
        </label>
        <label className="form-control border-0">
          <input type="radio" name="onlyIncludeMostRecent" value="false" checked={!exportOptions.onlyIncludeMostRecent}
            onChange={() => setExportOptions({ ...exportOptions, onlyIncludeMostRecent: false })}
            className="me-1" disabled={true}/>
          Include all completions
        </label>
      </div>
      <div className="py-2">
        <p className="fw-bold mb-1">
          Include subheaders for columns
        </p>
        <label className="me-3">
          <input type="radio" name="includeSubheaders" value="true" checked={exportOptions.includeSubHeaders}
            onChange={() => setExportOptions({ ...exportOptions, includeSubHeaders: true })} className="me-1"/> Yes
        </label>
        <label>
          <input type="radio" name="includeSubheaders" value="false" checked={!exportOptions.includeSubHeaders}
            onChange={() => setExportOptions({ ...exportOptions, includeSubHeaders: false })} className="me-1"/> No
        </label>
      </div>
      <div className="py-2">
        <p className="fw-bold mb-1">
          Filter Options
        </p>
        <label className="form-control border-0">
          <input type="checkbox" name="includeUnconsented" checked={includeUnconsented}
            onChange={e => setExportOptions({
              ...exportOptions,
              filterString: buildFilter({ includeProxiesAsRows, includeUnconsented: e.target.checked })
            })}
            className="me-1"/>
          Include enrollees who have not consented
        </label>
        <label className="form-control border-0">
          <input type="checkbox" name="includeProxiesAsRows" checked={includeProxiesAsRows}
            onChange={e => setExportOptions({
              ...exportOptions,
              filterString: buildFilter({ includeUnconsented, includeProxiesAsRows: e.target.checked })
            })}
            className="me-1"/>
          Include proxies as rows
        </label>
        <label className="form-control border-0">
          Limit number of enrollees to <input type="number" name="rowLimit"
            onChange={e => setExportOptions({
              ...exportOptions,
              rowLimit: e.target.value ? parseInt(e.target.value) : undefined
            })}
            className="me-1"/> <InfoPopup content={<span>
                If left blank, all enrollees will be included.
              If a limit is specified, the most recent X enrollees will be included.
          </span>}/>
        </label>
        <label className="form-control border-0" htmlFor={selectInputId}>
          Exclude data from the following modules:
        </label>
        <Select options={options}
          isMulti={true} value={selectedOptions}
          inputId={selectInputId}
          onChange={onChange}/>
        <div className="d-flex pt-3 ps-2">
          <label className="" htmlFor="exportFields">
            Only include fields:
          </label>
          <InfoPopup content={<span>
                Space-or-comma delimited list of field names. e.g. <pre>enrollee.shortcode</pre>
            If any fields are specified here, only those fields will be included in the export.
          </span>}/>
        </div>

        <textarea name="exportFields" id="exportFields" cols={70} value={exportOptions.includeFields?.join(' ')}
          onChange={e => setExportOptions({
            ...exportOptions,
            includeFields: e.target.value ? e.target.value
              .replace(/[\s,]+/g, ' ')
              .split(' ') : []
          })}
          className="me-1"/>
      </div>
    </div> }
    <hr/>

    <div>
      For more information about download formats,
      see the <Link to="https://broad-juniper.zendesk.com/hc/en-us/articles/18259824756123" target="_blank">
      help page</Link>.
    </div>
  </div>
}

export default ExportDataModal
