import React, { useId, useState } from 'react'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { Survey, VersionedForm } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'
import { instantToDefaultString } from 'util/timeUtils'
import Select from 'react-select'
import { faArrowRightFromBracket, faDownload } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { StudyEnvContextT, studyEnvFormsPath } from '../StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'
import { saveBlobAsDownload } from 'util/downloadUtils'
import { SaveableFormProps } from './SurveyView'
import { DocsKey, ZendeskLink } from 'util/zendeskUtils'
import InfoPopup from 'components/forms/InfoPopup'

/** component for selecting versions of a form */
export default function FormOptions({
  studyEnvContext, workingForm, updateWorkingForm, onDismiss, isDirty,
  visibleVersionPreviews, setVisibleVersionPreviews
}:
                                          {studyEnvContext: StudyEnvContextT, workingForm: VersionedForm,
                                            isDirty: boolean,
                                            updateWorkingForm: (props: SaveableFormProps) => void,
                                            onDismiss: () => void
                                            visibleVersionPreviews: VersionedForm[],
                                            setVisibleVersionPreviews: (versions: VersionedForm[]) => void}) {
  const stableId = workingForm.stableId

  const downloadJSON = () => {
    const content = workingForm.content
    // To get this formatted nicely and not as one giant line, need to parse
    // this as an object and then stringify the result.
    const blob = new Blob(
      [JSON.stringify(JSON.parse(content), null, 2)],
      { type: 'application/json' })
    const filename = `${stableId}_v${workingForm.version}${isDirty ? '_draft' : ''}.json`
    saveBlobAsDownload(blob, filename)
  }
  const isSurvey = !!(workingForm as Survey).surveyType

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>{workingForm.name} <InfoPopup placement="bottom" content={<div>
        See the &quot;Options&quot; section in
        our <ZendeskLink doc={DocsKey.SURVEY_EDIT}>survey editing docs</ZendeskLink> for
        more detail.  Note that you need to  &quot;Save&quot; the survey
        for any changes to options on this page to take effect.
      </div>}/></Modal.Title>
    </Modal.Header>
    <Modal.Body>
      { isSurvey &&
        <div>
          <h3 className="h5">Configuration</h3>
          <label className="form-label d-block">
            <input type="checkbox" checked={(workingForm as Survey).required}
              onChange={e => updateWorkingForm({
                ...workingForm, required: e.target.checked
              })}
            /> Required
          </label>
          <Button variant="primary" onClick={onDismiss}>
            Ok
          </Button>
          <hr/>
        </div>

      }
      <h3 className="h5">Actions</h3>
      <Button variant="secondary" title="Download the current contents of the JSON Editor as a file"
        onClick={downloadJSON}>
        <FontAwesomeIcon icon={faDownload}/> Download JSON
      </Button>
      <VersionSelector studyEnvContext={studyEnvContext}
        workingForm={workingForm}
        visibleVersionPreviews={visibleVersionPreviews}
        setVisibleVersionPreviews={setVisibleVersionPreviews}
        isConsentForm={!isSurvey}
        onDismiss={onDismiss}/>
    </Modal.Body>
  </Modal>
}


/**
 *
 */
export const VersionSelector = ({
  studyEnvContext, workingForm, visibleVersionPreviews, setVisibleVersionPreviews, isConsentForm = false, onDismiss
}:
  {studyEnvContext: StudyEnvContextT, workingForm: VersionedForm,
    visibleVersionPreviews: VersionedForm[],
    setVisibleVersionPreviews: (versions: VersionedForm[]) => void,
    isConsentForm?: boolean, onDismiss: () => void}) => {
  const [versionList, setVersionList] = useState<VersionedForm[]>([])
  const [selectedVersion, setSelectedVersion] = useState<number>()
  const stableId = workingForm.stableId
  const selectId = useId()

  const { isLoading } = useLoadingEffect(async () => {
    let result
    if (isConsentForm) {
      result = await Api.getConsentFormVersions(studyEnvContext.portal.shortcode, stableId)
    } else {
      result = await Api.getSurveyVersions(studyEnvContext.portal.shortcode, stableId)
    }
    setVersionList(result.sort((a, b) => b.version - a.version))
  }, [stableId])

  async function loadVersion(version: number) {
    let result
    if (isConsentForm) {
      result = await Api.getConsentForm(studyEnvContext.portal.shortcode, stableId, version)
    } else {
      result = await Api.getSurvey(studyEnvContext.portal.shortcode, stableId, version)
    }
    setVisibleVersionPreviews([...visibleVersionPreviews, result])
  }

  const versionOpts = versionList.map(formVersion => ({
    label: <span>
            Version <strong>{ formVersion.version }</strong>
      <span className="text-muted fst-italic ms-2">
                ({instantToDefaultString(formVersion.createdAt)})
      </span>
    </span>,
    value: formVersion.version
  })).filter(opt => visibleVersionPreviews.every(previewedVersion => previewedVersion.version !== opt.value))

  const selectedOpt = versionOpts.find(opt => opt.value === selectedVersion)

  return <LoadingSpinner isLoading={isLoading}>
    <div className="d-flex align-items-baseline">
      <label htmlFor={selectId} className="mt-3 d-block">Other versions</label>
      <InfoPopup content={<span>Viewing as a preview will open a new tab in the current editor.
        Opening in read-only mode will allow you to view the full editor in an entirely new browser tab.</span>}/>
    </div>
    <Select inputId={selectId} options={versionOpts} value={selectedOpt} onChange={opt =>
      setSelectedVersion(opt?.value)}/>
    <div className="flex ps-4 pt-2">
      <Button
        variant="secondary"
        disabled={!selectedVersion}
        onClick={() => {
          if (selectedVersion) {
            loadVersion(selectedVersion)
          }
          onDismiss()
        }}
      >
        View preview
      </Button>
      <a href={`${studyEnvFormsPath(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName
      )}/${isConsentForm ? 'consentForms' : 'surveys'}/${stableId}/${selectedVersion}?readOnly=true`}
      className="btn btn-secondary"
      aria-disabled={!selectedVersion}
      style={{ pointerEvents: selectedVersion ? undefined : 'none' }}
      onClick={onDismiss}
      target="_blank"
      >
        Open read-only editor <FontAwesomeIcon icon={faArrowRightFromBracket}/>
      </a>

    </div>
  </LoadingSpinner>
}
