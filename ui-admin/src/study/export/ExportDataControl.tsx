import React, { useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from '../../util/LoadingSpinner'
import Api, { ExportOptions } from '../../api/api'
import { currentIsoDate } from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import { saveBlobAsDownload } from '../../util/downloadUtils'
import { doApiLoad } from '../../api/api-utils'
import { buildFilter } from '../../util/exportUtils'
import { Button } from '../../components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'
import Select from 'react-select'
import { useReactMultiSelect } from '../../util/react-select-utils'

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

const MODULE_EXCLUDE_OPTIONS: Record<string, string> = { surveys: 'Surveys' }

/** form for configuring and downloading enrollee data */
const ExportDataControl = ({ studyEnvContext, show, setShow }: {studyEnvContext: StudyEnvContextT, show: boolean,
                           setShow:  React.Dispatch<React.SetStateAction<boolean>>}) => {
  const [humanReadable, setHumanReadable] = useState(true)
  const [onlyIncludeMostRecent, setOnlyIncludeMostRecent] = useState(true)
  const [fileFormat, setFileFormat] = useState(FILE_FORMATS[0])
  const [includeProxiesAsRows, setIncludeProxiesAsRows] = useState(false)
  const [includeUnconsented, setIncludeUnconsented] = useState(false)
  const [includeSubheaders, setIncludeSubheaders] = useState(true)
  const [showAdvancedOptions, setShowAdvancedOptions] = useState(false)
  const [excludeModules, setExcludeModules] = useState<string[]>([])
  const [isLoading, setIsLoading] = useState(false)

  const { selectInputId, selectedOptions, options, onChange } = useReactMultiSelect<string>(
    Object.keys(MODULE_EXCLUDE_OPTIONS),
    key => ({ label: MODULE_EXCLUDE_OPTIONS[key], value: key }),
    setExcludeModules,
    excludeModules
  )

  const optionsFromState = (): ExportOptions => {
    return {
      onlyIncludeMostRecent,
      splitOptionsIntoColumns: !humanReadable,
      stableIdsForOptions: !humanReadable,
      includeSubheaders,
      excludeModules,
      filter: buildFilter({ includeProxiesAsRows, includeUnconsented }),
      fileFormat: fileFormat.value
    }
  }

  const doExport = () => {
    doApiLoad(async () => {
      const response = await Api.exportEnrollees(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, optionsFromState())
      const fileName = `${currentIsoDate()}-enrollees.${fileFormat.fileSuffix}`
      const blob = await response.blob()
      saveBlobAsDownload(blob, fileName)
    }, { setIsLoading })
  }

  const doDictionaryExport = () => {
    doApiLoad(async () => {
      const response = await Api.exportDictionary(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, optionsFromState())
      const fileName = `${currentIsoDate()}-DataDictionary.xlsx`
      const blob = await response.blob()
      saveBlobAsDownload(blob, fileName)
    }, { setIsLoading })
  }

  const humanReadableChanged = (e: React.ChangeEvent<HTMLInputElement>) => {
    setHumanReadable(e.target.value === 'true')
  }
  const includeRecentChanged = (e: React.ChangeEvent<HTMLInputElement>) => {
    setOnlyIncludeMostRecent(e.target.value === 'true')
  }

  const inlcudeSubheadersChanged = (e: React.ChangeEvent<HTMLInputElement>) => {
    setIncludeSubheaders(e.target.value === 'true')
  }

  return <Modal show={show} onHide={() => setShow(false)}>
    <Modal.Header closeButton>
      <Modal.Title>
        Download
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div className="py-2">
          <p className="fw-bold mb-1">
            Data format
          </p>
          <label className="form-control border-0">
            <input type="radio" name="humanReadable" value="true" checked={humanReadable}
              onChange={humanReadableChanged} className="me-1"/> Human-readable
          </label>
          <label className="form-control border-0">
            <input type="radio" name="humanReadable" value="false" checked={!humanReadable}
              onChange={humanReadableChanged} className="me-1"/> Analysis-friendly
          </label>
        </div>
        <div className="py-2">
          <span className="fw-bold">File format</span><br/>
          {FILE_FORMATS.map(format => <label className="form-control border-0" key={format.value}>
            <input type="radio" name="fileFormat" value="TSV" checked={fileFormat.value === format.value}
              onChange={() => setFileFormat(format)}
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
              <input type="radio" name="onlyIncludeMostRecent" value="true" checked={onlyIncludeMostRecent}
                onChange={includeRecentChanged} className="me-1" disabled={true}/>
              Only include most recent
            </label>
            <label className="form-control border-0">
              <input type="radio" name="onlyIncludeMostRecent" value="false" checked={!onlyIncludeMostRecent}
                onChange={includeRecentChanged} className="me-1" disabled={true}/>
              Include all completions
            </label>
          </div>
          <div className="py-2">
            <p className="fw-bold mb-1">
              Include subheaders for columns
            </p>
            <label className="me-3">
              <input type="radio" name="includeSubheaders" value="true" checked={includeSubheaders}
                onChange={inlcudeSubheadersChanged} className="me-1"/> Yes
            </label>
            <label>
              <input type="radio" name="includeSubheaders" value="false" checked={!includeSubheaders}
                onChange={inlcudeSubheadersChanged} className="me-1"/> No
            </label>
          </div>
          <div className="py-2">
            <p className="fw-bold mb-1">
              Filter Options
            </p>
            <label className="form-control border-0">
              <input type="checkbox" name="includeUnconsented" checked={includeUnconsented}
                onChange={() => setIncludeUnconsented(!includeUnconsented)} className="me-1"/>
              Include enrollees who have not consented
            </label>
            <label className="form-control border-0">
              <input type="checkbox" name="includeProxiesAsRows" checked={includeProxiesAsRows}
                onChange={() => setIncludeProxiesAsRows(!includeProxiesAsRows)} className="me-1"/>
              Include proxies as rows
            </label>
            <label className="form-control border-0" htmlFor={selectInputId}>
              Exclude data from the following modules:
            </label>
            <Select options={options}
              isMulti={true} value={selectedOptions}
              inputId={selectInputId}
              onChange={onChange}/>
          </div>
        </div> }
        <hr/>

        <div>
          For more information about download formats,
          see the <Link to="https://broad-juniper.zendesk.com/hc/en-us/articles/18259824756123" target="_blank">
          help page</Link>.
        </div>
      </form>
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

export default ExportDataControl
