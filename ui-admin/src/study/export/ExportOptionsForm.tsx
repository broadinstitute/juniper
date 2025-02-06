import React, { useState } from 'react'
import { ExportOptions } from 'api/api'
import { buildFilter } from 'util/exportUtils'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faChevronDown,
  faChevronUp
} from '@fortawesome/free-solid-svg-icons'
import Select from 'react-select'
import { useReactMultiSelect } from 'util/react-select-utils'
import InfoPopup from 'components/forms/InfoPopup'
import {
  DocsKey,
  ZendeskLink
} from 'util/zendeskUtils'

export const FILE_FORMATS = [{
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

const MODULE_EXCLUDE_OPTIONS: Record<string, string> = { surveys: 'Surveys', profile: 'Profile', account: 'Account' }

function ExportOptionsForm({ exportOptions, setExportOptions }:
  { exportOptions: ExportOptions, setExportOptions: (opts: ExportOptions) => void }) {
  const [showAdvancedOptions, setShowAdvancedOptions] = useState(false)

  const { selectInputId, selectedOptions, options, onChange } = useReactMultiSelect<string>(
    Object.keys(MODULE_EXCLUDE_OPTIONS),
    key => ({ label: MODULE_EXCLUDE_OPTIONS[key], value: key }),
    (excludeModules: string[]) => setExportOptions({ ...exportOptions, excludeModules }),
    exportOptions.excludeModules
  )

  const enrolledBefore = exportOptions.filterString?.match(/{enrollee.createdAt} < '(.+?)T/)?.[1]
  const enrolledAfter = exportOptions.filterString?.match(/{enrollee.createdAt} > '(.+?)T/)?.[1]

  const filterOpts = {
    includeProxiesAsRows: !exportOptions.filterString?.includes('{enrollee.subject} = true'),
    includeUnconsented: !exportOptions.filterString?.includes('{enrollee.consented} = true'),
    enrolledBefore: enrolledBefore ? new Date(enrolledBefore) : undefined,
    enrolledAfter: enrolledAfter ? new Date(enrolledAfter) : undefined
  }

  return <div className="container-md ms-0">
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
            className="me-1"/>
          Only include most recent
        </label>
        <label className="form-control border-0">
          <input type="radio" name="onlyIncludeMostRecent" value="false" checked={!exportOptions.onlyIncludeMostRecent}
            onChange={() => setExportOptions({ ...exportOptions, onlyIncludeMostRecent: false })}
            className="me-1"/>
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
          <input type="checkbox" name="includeUnconsented" checked={filterOpts.includeUnconsented}
            onChange={e => setExportOptions({
              ...exportOptions,
              filterString: buildFilter({ ...filterOpts, includeUnconsented: e.target.checked })
            })}
            className="me-1"/>
          Include enrollees who have not consented
        </label>
        <label className="form-control border-0">
          <input type="checkbox" name="includeProxiesAsRows" checked={filterOpts.includeProxiesAsRows}
            onChange={e => setExportOptions({
              ...exportOptions,
              filterString: buildFilter({ ...filterOpts, includeProxiesAsRows: e.target.checked })
            })}
            className="me-1"/>
          Include proxies as rows
        </label>
        <div className="d-flex pt-2 my-2">
          <label className="form-control border-0">
            Enrolled on/after <input type="date" name="enrolledBeforeDate"
              value={enrolledAfter || ''}
              onChange={e => setExportOptions({
                ...exportOptions,
                filterString: buildFilter({
                  ...filterOpts,
                  enrolledAfter: e.target.value ? new Date(e.target.value) : undefined
                })
              })}
              className="me-1"/>
          </label>
          <label className="form-control border-0">
            Enrolled before <input type="date" name="enrolledAfterDate"
              value={enrolledBefore || ''}
              onChange={e => setExportOptions({
                ...exportOptions,
                filterString: buildFilter({
                  ...filterOpts,
                  enrolledBefore: e.target.value ? new Date(e.target.value) : undefined
                })
              })}
              className="me-1"/>
          </label>
        </div>
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
      see the <ZendeskLink doc={DocsKey.EXPORT_FORMATS}>help page</ZendeskLink>.
    </div>
  </div>
}

export default ExportOptionsForm
