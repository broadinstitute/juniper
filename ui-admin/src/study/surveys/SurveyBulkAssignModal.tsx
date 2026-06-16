import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import { StudyEnvParams } from '@juniper/ui-core'
import Api from 'api/api'
import { Button } from 'components/forms/Button'
import {
  failureNotification,
  successNotification
} from 'util/notifications'
import { Store } from 'react-notifications-component'
import { useFileUploadButton } from 'util/uploadUtils'
import pluralize from 'pluralize'
import { isEmpty } from 'lodash'
import { uniq } from 'lodash/fp'
import { Textarea } from 'components/forms/Textarea'
import InfoPopup from 'components/forms/InfoPopup'

export type SurveyEnvironmentDetailModalProps = {
  studyEnvParams: StudyEnvParams
  onDismiss: () => void
  stableId: string
}

/**
 * Shows details for a given environment's survey history
 */
export default function SurveyBulkAssignModal({
  studyEnvParams,
  onDismiss,
  stableId
}: {
  studyEnvParams: StudyEnvParams
  onDismiss: () => void
  stableId: string
}) {
  const [shortcodeInput, setShortcodeInput] = useState<string>('')
  const [shortcodes, setShortcodes] = useState<string[]>([])
  const [overrideEligibility, setOverrideEligibility] = useState<boolean>(false)
  const [isLoading, setIsLoading] = useState(false)
  const { FileChooser } = useFileUploadButton(file => {
    const reader = new FileReader()
    reader.onload = () => {
      const shortcodes: string[] = parseShortcodeCsv(reader.result as string)
      setShortcodes(shortcodes)
      setShortcodeInput(shortcodes.join(','))
    }
    reader.readAsText(file)
  }, 'Import CSV')

  const bulkAssign = async () => {
    setIsLoading(true)
    try {
      const response = await Api.assignParticipantTasksToEnrollees(
        studyEnvParams,
        {
          taskType: 'SURVEY',
          enrolleeShortcodes: shortcodes,
          targetStableId: stableId,
          targetAssignedVersion: null, // assign latest
          assignAllUnassigned: false,
          overrideEligibility
        }
      )
      Store.addNotification(successNotification(
        `${response.length} new ${pluralize('task', response.length)} assigned`
      ))
      onDismiss()
    } catch {
      Store.addNotification(failureNotification('Error: could not assign task'))
    }
    setIsLoading(false)
  }

  return <Modal show={true} className="modal-xl" onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Add Users</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="pb-3">
        Bulk assign participants to the latest version of the survey.
      </div>
      <Textarea
        label="Shortcodes"
        rows={2}
        value={shortcodeInput}
        onChange={val => {
          setShortcodeInput(val)
          setShortcodes(parseShortcodes(val))
        }}
      />
      <div className={'d-flex flex-column align-items-start my-2'}>
        {FileChooser}
      </div>


      <div>
        <label
          className="form-label">
          <input
            type="checkbox"
            className="form-check-input me-2"

            checked={overrideEligibility}
            onChange={() => setOverrideEligibility(!overrideEligibility)}
          />
          Override Eligibility <InfoPopup
            content={
              'If checked, enrollees will be assigned the survey even ' +
              'if they do not meet the survey\'s eligiblity criteria'
            }
          />
        </label>
      </div>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <div className={'d-flex justify-content-between w-100'}>
          <div>
            <Button
              disabled={shortcodes.length === 0}
              onClick={bulkAssign}
              variant="primary"
            >
              Assign to {shortcodes.length} enrollees
            </Button>
            <Button onClick={() => {
              onDismiss()
              setShortcodes([])
            }}
            variant="secondary">
              Cancel
            </Button>
          </div>
        </div>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

/** shortcodes are 6 characters and case-insensitive on input; normalize to uppercase */
const SHORTCODE_LENGTH = 6

function normalizeShortcode(raw: string): string {
  const trimmed = raw.trim().toUpperCase()
  return trimmed.length === SHORTCODE_LENGTH ? trimmed : ''
}

/**
 * Parses a comma- or newline-separated list of shortcodes, trimming, uppercasing,
 * and dropping anything that isn't a valid shortcode. Returns a deduped list.
 */
export function parseShortcodes(input: string): string[] {
  return uniq(input.split(/[\n,]/).map(normalizeShortcode).filter(s => !isEmpty(s)))
}

/**
 * Parses a CSV assuming the first column of each row is the shortcode
 */
export function parseShortcodeCsv(csv: string): string[] {
  return uniq(csv.split('\n').map(line => normalizeShortcode(line.split(',')[0])).filter(s => !isEmpty(s)))
}
