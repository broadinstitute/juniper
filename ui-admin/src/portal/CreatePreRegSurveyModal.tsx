import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import Modal from 'react-bootstrap/Modal'
import InfoPopup from 'components/forms/InfoPopup'
import LoadingSpinner from 'util/LoadingSpinner'
import { ApiErrorResponse, defaultApiErrorHandle, doApiLoad } from 'api/api-utils'
import { PortalEnvContext } from './PortalRouter'
import { useFormCreationNameFields } from 'study/surveys/useFormCreationNameFields'
import { StudyEnvParams, studyEnvPreRegPath, useStudyEnvParamsFromPath } from '../study/StudyEnvironmentRouter'
import { DocsKey, ZendeskLink } from '../util/zendeskUtils'
import { defaultSurvey } from '@juniper/ui-core'

const EXAMPLE_PREREG_TEMPLATE = `
{
    "title": "pre-registration",
    "logoPosition": "right",
    "pages": [
      {
        "name": "page1",
        "elements": [
          {
            "type": "html",
            "name": "descriptionText",
            "html": "<h3 class=\\"text-center fw-bold\\">Join our portal to take part in many studies!</h3>\\n\\n"
          },
          {
            "type": "radiogroup",
            "name": "default_prereg_age",
            "title": "Are you 18 or older?",
            "isRequired": true,
            "choices": [
              {
                "value": "yes",
                "text": "Yes"
              },
              {
                "value": "no",
                "text": "No"
              }
            ]
          },
          {
            "type": "radiogroup",
            "name": "default_prereg_heartHx",
            "title": "Do you have a history of heart disease ` +
    `or cardiomyopathy or a genetic diagnosis that places you at an increased risk of heart disease?",
            "isRequired": true,
            "choices": [
              {
                "value": "yes",
                "text": "Yes"
              },
              {
                "value": "no",
                "text": "No"
              }
            ]
          }
        ]
      }
    ],
    "calculatedValues": [
      {
        "name": "qualified",
        "expression": "{default_prereg_heartHx} = 'yes' && {default_prereg_age} = 'yes'",
        "includeIntoResult": true
      }
    ]
  }`

/** dialog for adding a new PreReg survey */
export default function CreatePreRegSurveyModal({ portalEnvContext, onDismiss }:
                                                       {portalEnvContext: PortalEnvContext, onDismiss: () => void}) {
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const { formName, formStableId, clearFields, nameInput, stableIdInput } = useFormCreationNameFields()
  const studyEnvParams = useStudyEnvParamsFromPath()
  const createSurvey =async () => {
    await doApiLoad(async () => {
      const createdSurvey = await Api.createNewSurvey(portalEnvContext.portal.shortcode,
        {
          ...defaultSurvey,
          createdAt: 0, id: '', lastUpdatedAt: 0, version: 1, surveyType: 'RESEARCH',
          content: EXAMPLE_PREREG_TEMPLATE, name: formName, stableId: formStableId
        })
      Store.addNotification(successNotification('Survey created'))
      try {
        await Api.updatePortalEnv(portalEnvContext.portal.shortcode,
          portalEnvContext.portalEnv.environmentName,
          {
            ...portalEnvContext.portalEnv,
            preRegSurveyId: createdSurvey.id
          }
        )
      } catch (err) {
        defaultApiErrorHandle(err as ApiErrorResponse, 'Error configuring survey: ')
      }

      await portalEnvContext.reloadPortal(portalEnvContext.portal.shortcode)
      navigate(studyEnvPreRegPath(studyEnvParams as StudyEnvParams))
    }, { setIsLoading })
    onDismiss()
  }


  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Create Pre-registration Survey</Modal.Title>
      <div className="ms-4">
        {portalEnvContext.portal.name}: {portalEnvContext.portalEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <p>This survey will be shown to all participants wishing to create a user account on the portal.
            This form is associated with the portal as a whole, not any particular study.
      <ZendeskLink className="px-1" doc={DocsKey.PREREG_SURVEYS}>More info.</ZendeskLink>
      </p>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label" htmlFor="inputFormName">Survey Name</label>
        { nameInput }
        <label className="form-label mt-3" htmlFor="inputFormStableId">Survey Stable ID</label>
        <InfoPopup content={'A stable and unique identifier for the form. May be shown in exported datasets.'}/>
        { stableIdInput }
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button
          className="btn btn-primary"
          disabled={!formName || !formStableId}
          onClick={createSurvey}
        >Create</button>
        <button className="btn btn-secondary" onClick={() => {
          onDismiss()
          clearFields()
        }}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
