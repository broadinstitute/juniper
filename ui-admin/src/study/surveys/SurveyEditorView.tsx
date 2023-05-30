import React, { useCallback, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { VersionedForm } from 'api/api'
import VersionSelector from './VersionSelector'

import { SurveyCreatorComponent } from 'survey-creator-react'
import { useSurveyJSCreator } from '../../util/surveyJsUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCaretDown } from '@fortawesome/free-solid-svg-icons/faCaretDown'
import { useUser } from '../../user/UserProvider'
import { failureNotification } from '../../util/notifications'
import { Store } from 'react-notifications-component'

/** renders a survey for editing/viewing using the surveyJS editor */
export default function SurveyEditorView({
  portalShortcode, currentForm, readOnly = false,
  createNewVersion, changeVersion
}:
                         {portalShortcode: string, currentForm: VersionedForm, readOnly?: boolean,
                           createNewVersion: (updatedContent: string) => Promise<string>,
                           changeVersion: (version: number) => void}) {
  const navigate = useNavigate()
  const [isDirty, setIsDirty] = useState(false)
  const [showVersionSelector, setShowVersionSelector] = useState(false)
  const { user } = useUser()

  /** indicate the survey has been modified */
  const handleSurveyModification = useCallback(() => {
    //setIsDirty(true)
  }, [])

  const { surveyJSCreator } = useSurveyJSCreator(currentForm, handleSurveyModification)
  if (surveyJSCreator) {
    surveyJSCreator.readOnly = readOnly
  }


  /** when save is pressed, call the handling function, and then update the survey with the response */
  async function handleSave() {
    if (!surveyJSCreator) {
      return
    }
    if (!user.superuser) {
      Store.addNotification(failureNotification('you do not have permissions to save surveys'))
      return
    }
    surveyJSCreator.text = await createNewVersion(surveyJSCreator.text)
    setIsDirty(false)
  }

  /** handles the "cancel" button press */
  function handleCancel() {
    navigate('../../..')
  }

  return <div className="SurveyView">
    <div className="d-flex p-2 align-items-center">
      <div className="d-flex flex-grow-1">
        <h5>{currentForm.name}
          <span className="detail me-2 ms-2">version {currentForm.version}</span>
          { isDirty && <span className="badge" style={{ backgroundColor: 'rgb(51, 136, 0)' }} >
            <em>modified</em>
          </span> }
          <button className="btn-secondary btn" onClick={() => setShowVersionSelector(true)}>
            all versions <FontAwesomeIcon icon={faCaretDown}/>
          </button>
          { showVersionSelector && <VersionSelector studyShortname={portalShortcode}
            stableId={currentForm.stableId}
            show={showVersionSelector} setShow={setShowVersionSelector}
            updateVersion={changeVersion}/> }
        </h5>
      </div>
      {!readOnly && <button className="btn btn-primary me-md-2" type="button" onClick={handleSave}>
        Save
      </button> }
      <button className="btn btn-secondary" type="button" onClick={handleCancel}>Cancel</button>
    </div>
    {surveyJSCreator && <SurveyCreatorComponent creator={surveyJSCreator} /> }
  </div>
}
