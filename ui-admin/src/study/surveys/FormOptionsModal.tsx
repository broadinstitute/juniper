import React from 'react'
import { Survey, VersionedForm } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { SaveableFormProps } from './SurveyView'
import { DocsKey, ZendeskLink } from 'util/zendeskUtils'
import InfoPopup from 'components/forms/InfoPopup'
import { SearchQueryBuilder } from '../../search/SearchQueryBuilder'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { userHasPermission, useUser } from '../../user/UserProvider'

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

  const { user } = useUser()

  return <>
    {(workingForm && workingForm.surveyType) &&
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
              <input type="checkbox" checked={workingForm.assignToAllNewEnrollees}
                onChange={e => updateWorkingForm({
                  ...workingForm, assignToAllNewEnrollees: e.target.checked
                })}
              /> Auto-assign to new participants
            </label>
            <label className="form-label d-block">
              <input type="checkbox" checked={workingForm.assignToExistingEnrollees}
                onChange={e => updateWorkingForm({
                  ...workingForm, assignToExistingEnrollees: e.target.checked
                })}
              /> Auto-assign to existing participants
            </label>
            <label className="form-label d-block">
              <input type="checkbox" checked={workingForm.autoUpdateTaskAssignments}
                onChange={e => updateWorkingForm({
                  ...workingForm, autoUpdateTaskAssignments: e.target.checked
                })}
              /> Auto-update participant tasks to the latest version of this survey after publishing
            </label>
            { (workingForm.surveyType !== 'ADMIN' && workingForm.surveyType !== 'OUTREACH') &&
            <label className="form-label d-block">
              <input type="checkbox" checked={workingForm.allowAdminEdit}
                onChange={e => updateWorkingForm({
                  ...workingForm, allowAdminEdit: e.target.checked
                })}
              /> Allow study staff to edit participant responses
            </label>}
            <div className='pt-2'>Eligibility Rule</div>
            {userHasPermission(user, studyEnvContext.portal.id, 'prototype_tester')
                && <div className="my-2"><SearchQueryBuilder
                  studyEnvContext={studyEnvContext}
                  onSearchExpressionChange={exp => updateWorkingForm({
                    ...workingForm, eligibilityRule: exp
                  })}/></div>}

            <input type="text" className="form-control" value={workingForm.eligibilityRule || ''}
              onChange={e => {
                updateWorkingForm({
                  ...workingForm, eligibilityRule: e.target.value
                })
              }
              }/>
          </div>
        </div>
    }
    {!!workingForm.surveyType && <>
        This form has no configurable options
    </>}
  </>
}
