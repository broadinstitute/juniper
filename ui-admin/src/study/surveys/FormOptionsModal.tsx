import React from 'react'
import {
  Survey,
  VersionedForm
} from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { SaveableFormProps } from './SurveyView'
import {
  DocsKey,
  ZendeskLink
} from 'util/zendeskUtils'
import InfoPopup from 'components/forms/InfoPopup'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { LazySearchQueryBuilder } from 'search/LazySearchQueryBuilder'
import { TextInput } from '../../components/forms/TextInput'
import { useNonNullReactSingleSelect } from '../../util/react-select-utils'
import Select from 'react-select'


/** component for selecting versions of a form */
export default function FormOptionsModal({
  studyEnvContext, workingForm, updateWorkingForm, onDismiss
}:
                                           {
                                             studyEnvContext: StudyEnvContextT,
                                             workingForm: VersionedForm,
                                             updateWorkingForm: (props: SaveableFormProps) => void,
                                             onDismiss: () => void
                                           }) {
  return <Modal show={true} onHide={onDismiss} size="lg">
    <Modal.Header closeButton>
      <Modal.Title>{workingForm.name} - configuration</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form>
        <FormOptions studyEnvContext={studyEnvContext}
          initialWorkingForm={workingForm} updateWorkingForm={updateWorkingForm}/>
      </form>
      <div className="fw-light fst-italic mt-4">
        Note: you must  &quot;Save&quot; the form
        for changes to these options to take effect.
      </div>
    </Modal.Body>
    <Modal.Footer>
      <Button variant="secondary" onClick={onDismiss}
        disabled={!(workingForm as Survey).allowAdminEdit && !(workingForm as Survey).allowParticipantStart}>
        Done
      </Button>
    </Modal.Footer>
  </Modal>
}

const RECURRENCE_OPTS: {label: string, value: string}[] = [{
  value: 'NONE',
  label: 'None'
}, {
  value: 'LONGITUDINAL',
  label: 'Longitudinal'
}, {
  value: 'UPDATE',
  label: 'Update in-place'
}]

/**
 * Renders the 'options' for a form, e.g. who is allowed to take it, if it's required, how it's assigned, etc...
 */
export const FormOptions = ({ studyEnvContext, initialWorkingForm, updateWorkingForm }:
                              {
                                studyEnvContext: StudyEnvContextT,
                                initialWorkingForm: VersionedForm,
                                updateWorkingForm: (props: SaveableFormProps) => void
                              }) => {
  const workingForm = initialWorkingForm as Survey
  const {
    onChange: onRecurrenceTypeChange,
    options: recurrenceOpts,
    selectedOption: selectedRecurrenceType, selectInputId: recurrenceSelectInputId
  } =
    useNonNullReactSingleSelect(RECURRENCE_OPTS.map(opt => opt.value),
      val => RECURRENCE_OPTS.find(opt => opt.value === val)!,
      (val: string) => updateWorkingForm({
        ...workingForm,
        recurrenceType: val,
        recurrenceIntervalDays: val === 'NONE' ? undefined : workingForm.recurrenceIntervalDays || 365
      }),
      workingForm.recurrenceType || 'NONE')
  return <>
    {(workingForm && !!workingForm.surveyType) &&
        <div>
          <div className="d-flex mt-3">Survey options <InfoPopup placement="right" content={<div>
              See the Options <FontAwesomeIcon icon={faArrowRight}/> Configuration section in
              our <ZendeskLink doc={DocsKey.SURVEY_EDIT}>survey editing docs</ZendeskLink> for
              information on the options below.
          </div>}/>
          </div>
          <div className="p-2">
            <label className="form-label d-block">
              <input type="checkbox" checked={workingForm.required}
                onChange={e => updateWorkingForm({
                  ...workingForm, required: e.target.checked
                })}
              /> Required
            </label>
            <label className="form-label d-block">
              <input type="checkbox" checked={workingForm.autoAssign}
                onChange={e => updateWorkingForm({
                  ...workingForm, autoAssign: e.target.checked
                })}
              /> Auto-assign to participants based on eligibility <InfoPopup placement="right" content={<div>
              if unchecked, the survey must be manually assigned by staff
              </div>}/>
            </label>
            <label className="form-label d-block">
              <input type="checkbox" checked={workingForm.assignToExistingEnrollees}
                onChange={e => updateWorkingForm({
                  ...workingForm, assignToExistingEnrollees: e.target.checked
                })}
              /> Auto-assign to existing participants based on eligibility <InfoPopup placement="right" content={<div>
              If there are already-enrolled participants when this survey is first published, they will be assigned it
              </div>}/>
            </label>
            <label className="form-label d-block">
              <input type="checkbox" checked={workingForm.autoUpdateTaskAssignments}
                onChange={e => updateWorkingForm({
                  ...workingForm, autoUpdateTaskAssignments: e.target.checked
                })}
              /> Auto-update participant tasks to the latest version of this survey after publishing
            </label>
            {(workingForm.surveyType !== 'ADMIN' && workingForm.surveyType !== 'OUTREACH') &&
            <label className="form-label d-block">
              <input type="checkbox" checked={workingForm.allowAdminEdit}
                onChange={e => updateWorkingForm({
                  ...workingForm, allowAdminEdit: e.target.checked
                })}
              /> Allow study staff to edit participant responses
            </label>}
            <label className="form-label d-flex align-items-center mt-4">
              <label className="pe-2">
                <input type="checkbox" checked={!!workingForm.daysAfterEligible}
                  onChange={e => updateWorkingForm({
                    ...workingForm,
                    daysAfterEligible: e.target.checked ? 365 : undefined
                  })}
                /> Delay assigning this survey
              </label>
              <label className="d-flex align-items-center">
                <TextInput value={workingForm.daysAfterEligible} type="number" min={1} max={9999}
                  className="mx-2"
                  onChange={val => updateWorkingForm({
                    ...workingForm, daysAfterEligible: parseInt(val)
                  })}
                /> <span className="text-nowrap">days after enrollment</span>
              </label>
            </label>
            <div className="d-flex align-items-center mb-4">
              <label className="pe-2" htmlFor={recurrenceSelectInputId}>
                Recurrence:
              </label>
              <Select inputId={recurrenceSelectInputId}
                styles={{
                  control: baseStyles => ({
                    ...baseStyles,
                    minWidth: '13em'
                  })
                }}
                options={recurrenceOpts} onChange={onRecurrenceTypeChange}
                value={selectedRecurrenceType}/>
              <label className="d-flex align-items-center ms-3">
                every <TextInput value={workingForm.recurrenceIntervalDays} type="number" min={1} max={9999}
                  className="mx-2"
                  onChange={val => updateWorkingForm({
                    ...workingForm,
                    recurrenceIntervalDays: parseInt(val),
                    recurrenceType: workingForm.recurrenceType === 'NONE' ? 'LONGITUDINAL' : workingForm.recurrenceType
                  })}
                /> days
              </label>
            </div>
            <h3 className="h6 mt-4">Eligibility Rule</h3>
            <div className="mb-2">
              <LazySearchQueryBuilder
                studyEnvContext={studyEnvContext}
                onSearchExpressionChange={exp => updateWorkingForm({
                  ...workingForm, eligibilityRule: exp
                })}
                searchExpression={workingForm.eligibilityRule || ''}/>
            </div>
          </div>
        </div>
    }
    {!workingForm.surveyType && <>
        This form has no configurable options
    </>}
  </>
}
