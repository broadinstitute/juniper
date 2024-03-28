import React from 'react'
import { Survey, VersionedForm } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { SaveableFormProps } from './SurveyView'
import { DocsKey, ZendeskLink } from 'util/zendeskUtils'
import InfoPopup from 'components/forms/InfoPopup'
import { useSearchExpressionQueryBuilder } from '../../search/SearchQueryBuilder'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'

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
        <FormOptions studyEnvContext={studyEnvContext} workingForm={workingForm} updateWorkingForm={updateWorkingForm}/>
      </form>
      <div className="fw-light fst-italic mt-4">
        Note: you must  &quot;Save&quot; the form
        for changes to these options to take effect.
      </div>
    </Modal.Body>
    <Modal.Footer>
      <Button variant="secondary" onClick={onDismiss}>
        Done
      </Button>
    </Modal.Footer>
  </Modal>
}

/**
 * Renders the 'options' for a form, e.g. who is allowed to take it, if it's required, how it's assigned, etc...
 */
export const FormOptions = ({ studyEnvContext, workingForm, updateWorkingForm }:
                              {
                                studyEnvContext: StudyEnvContextT,
                                workingForm: VersionedForm,
                                updateWorkingForm: (props: SaveableFormProps) => void
                              }) => {
  const isSurvey = !!(workingForm as Survey).surveyType

  const {
    enrolleeSearchExpression,
    EnrolleeSearchQueryBuilder
  } = useSearchExpressionQueryBuilder({ studyEnvContext })

  return <>
    { isSurvey &&
        <div>
          <div className="d-flex mt-3">Survey options <InfoPopup placement="right" content={<div>
            See the Options <FontAwesomeIcon icon={faArrowRight}/> Configuration section in
            our <ZendeskLink doc={DocsKey.SURVEY_EDIT}>survey editing docs</ZendeskLink> for
            information on the options below.
          </div>}/>
          </div>
          <div className="p-2">
            <label className="form-label d-block">
              <input type="checkbox" checked={(workingForm as Survey).required}
                onChange={e => updateWorkingForm({
                  ...workingForm, required: e.target.checked
                })}
              /> Required
            </label>
            <label className="form-label d-block">
              <input type="checkbox" checked={(workingForm as Survey).assignToAllNewEnrollees}
                onChange={e => updateWorkingForm({
                  ...workingForm, assignToAllNewEnrollees: e.target.checked
                })}
              /> Auto-assign to new participants
            </label>
            <label className="form-label d-block">
              <input type="checkbox" checked={(workingForm as Survey).assignToExistingEnrollees}
                onChange={e => updateWorkingForm({
                  ...workingForm, assignToExistingEnrollees: e.target.checked
                })}
              /> Auto-assign to existing participants
            </label>
            <label className="form-label d-block">
              <input type="checkbox" checked={(workingForm as Survey).autoUpdateTaskAssignments}
                onChange={e => updateWorkingForm({
                  ...workingForm, autoUpdateTaskAssignments: e.target.checked
                })}
              /> Auto-update participant tasks to the latest version of this survey after publishing
            </label>
            {EnrolleeSearchQueryBuilder}
            <p>{enrolleeSearchExpression}</p>
          </div>
        </div>
    }
    { !isSurvey && <>
      This form has no configurable options
    </> }
  </>
}
