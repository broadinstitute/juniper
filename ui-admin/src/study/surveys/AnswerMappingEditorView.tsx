import React, { useEffect, useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { useLoadingEffect } from '../../api/api-utils'
import Api, { AnswerMapping, AnswerMappingMapType, AnswerMappingTargetType, Survey, VersionedForm } from '../../api/api'
import LoadingSpinner from '../../util/LoadingSpinner'
import { faCheck, faPlus, faTrash, faX } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import Select from 'react-select'
import Creatable from 'react-select/creatable'
import { getFormContent, getFormElements } from '@juniper/ui-core'
import { isEmpty } from 'lodash'
import { Modal, ModalBody, ModalFooter, ModalHeader, ModalTitle } from 'react-bootstrap'


const AnswerMappingLabels: {[key in AnswerMappingTargetType]: string} = {
  'PROXY': 'Proxy',
  'PROFILE': 'Profile',
  'PROXY_PROFILE': 'Proxy Profile'
}

const ProfileFields: { [key: string]: AnswerMappingMapType } = {
  'givenName': 'STRING_TO_STRING',
  'familyName': 'STRING_TO_STRING',
  'contactEmail': 'STRING_TO_STRING',
  'phoneNumber': 'STRING_TO_STRING',
  'birthDate': 'STRING_TO_LOCAL_DATE'
}

const ProxyFields: { [key: string]: AnswerMappingMapType } = {
  'isProxy': 'STRING_TO_BOOLEAN'
}

const AnswerMappingTargets = {
  'PROFILE': ProfileFields,
  'PROXY': ProxyFields,
  'PROXY_PROFILE': ProfileFields
}

const AnswerMappingMapTypeLabels: { [key in AnswerMappingMapType]: string} = {
  'STRING_TO_STRING': 'Text',
  'STRING_TO_BOOLEAN': 'Boolean',
  'STRING_TO_LOCAL_DATE': 'Date'
}

/**
 * TODO
 */
export default function AnswerMappingEditorView(
  {
    studyEnvContext, formContent
  } : {
    studyEnvContext: StudyEnvContextT, formContent: VersionedForm
  }
) {
  const [answerMappings, setAnswerMappings] = useState<AnswerMapping[]>([])
  const [survey, setSurvey] = useState<Survey>()

  const stableId = formContent.stableId
  const version = formContent.version

  const [openDeleteMappingModal, setOpenDeleteMappingModal] = useState<AnswerMapping | null>(null)

  const { isLoading } = useLoadingEffect(async () => {
    const survey = await Api.getSurvey(studyEnvContext.portal.shortcode, stableId, version)

    setSurvey(survey)
    const newAnswerMappings = await Api.getSurveyAnswerMappings(
      studyEnvContext.portal.shortcode,
      survey.stableId,
      survey.version)
    setAnswerMappings(newAnswerMappings)
  })

  const deleteAnswerMapping = async () => {
    if (!openDeleteMappingModal || !survey) {
      return
    }

    try {
      await Api.deleteAnswerMapping(
        studyEnvContext.portal.shortcode,
        survey.stableId,
        survey.version,
        openDeleteMappingModal.id)
    } finally {
      setAnswerMappings(answerMappings.filter(m => m.id !== openDeleteMappingModal.id))
      setOpenDeleteMappingModal(null)
    }
  }

  const [addNewAnswerMapping, setAddNewAnswerMapping] = useState(false)

  if (isLoading) {
    return <LoadingSpinner/>
  }

  const saveNewMapping = async (mapping: AnswerMapping) => {
    if (!survey) {
      return
    }

    const newMapping = await Api.createAnswerMapping(
      studyEnvContext.portal.shortcode, survey.stableId, survey.version, mapping
    )
    setAnswerMappings([...answerMappings, newMapping])
    setAddNewAnswerMapping(false)
  }

  return <div className='px-3 pt-1'>
    <p>
      Answer mappings allow you to map answers from questions in the survey to other parts of the system.
      For example, you could map a question which collects the participant&apos;s first name to the profile&apos;s.
      given name field. Then, any changes the participant makes to the survey will result in changes to their profile.
    </p>
    <table className={'table'}>
      <thead>
        <tr>
          <th scope="col" className="col-1">Question ID</th>
          <th scope="col" className="col-1">Target</th>
          <th scope="col" className="col-1">Field</th>
          <th scope="col" className="col-1">Format</th>
          <th scope="col" className="col-1">Type</th>
          <th scope="col" className="col-1">Actions</th>
        </tr>
      </thead>
      <tbody>
        {
          answerMappings.map((answerMapping, index) => {
            return <tr key={index}>
              <td>
                {answerMapping.questionStableId}
              </td>
              <td >
                {AnswerMappingLabels[answerMapping.targetType]}
              </td>
              <td >
                {answerMapping.targetField}
              </td>
              <td >
                {answerMapping.formatString}
              </td>
              <td >
                {AnswerMappingMapTypeLabels[answerMapping.mapType] || answerMapping.mapType}
              </td>
              <td>
                <button className='btn btn-danger' onClick={() => {
                  setOpenDeleteMappingModal(answerMapping)
                }}>
                  <FontAwesomeIcon icon={faTrash}/>
                </button>
              </td>
            </tr>
          })
        }
        {addNewAnswerMapping && survey ? <AddNewAnswerMappingRow
          onSave={saveNewMapping}
          existingMappings={answerMappings}
          survey={survey}
          onCancel={() => setAddNewAnswerMapping(false)}/>: <tr>
          <td className={'border-0'}>
          </td>
          <td className={'border-0'}>
          </td>
          <td className={'border-0'}>
          </td>
          <td className={'border-0'}>
          </td>
          <td className={'border-0'}>
          </td>
          <td className={'border-0'}>
            <button className='btn btn-primary' onClick={() => setAddNewAnswerMapping(true)}>
              <FontAwesomeIcon icon={faPlus}/>
            </button>
          </td>
        </tr>}
      </tbody>
    </table>
    {openDeleteMappingModal && <DeleteAnswerMappingModal
      onConfirm={deleteAnswerMapping}
      onCancel={() => setOpenDeleteMappingModal(null)}/>}
    <div>
    </div>
  </div>
}

const AddNewAnswerMappingRow = (
  { onSave, onCancel, survey, existingMappings }
    :
  { onSave: (mapping: AnswerMapping) => void, onCancel: () => void, survey: Survey, existingMappings: AnswerMapping[] }
) => {
  const [questionStableId, setQuestionStableId] = useState<string>()
  const [targetType, setTargetType] = useState<AnswerMappingTargetType>()
  const [targetField, setTargetField] = useState<string>()
  const [formatString, setFormatString] = useState<string>()
  const [mapType, setMapType] = useState<AnswerMappingMapType>('STRING_TO_STRING')

  const content = getFormContent(survey)
  const elements = getFormElements(content)
  const names = elements.filter(e => 'name' in e).map(e => 'name' in e ? e.name : '')

  useEffect(() => {
    // automatically set map type
    if (targetType && targetField) {
      setMapType(
        (AnswerMappingTargets[targetType] && AnswerMappingTargets[targetType][targetField]) || 'STRING_TO_STRING'
      )
    }
  }, [targetType, targetField])

  return <tr>
    <td>
      {/* question stable id */}
      <Creatable
        options={names.map(name => {
          return {
            value: name,
            label: name,
            isDisabled: existingMappings.some(m => m.questionStableId === name)
          }
        })}
        value={questionStableId && { value: questionStableId, label: questionStableId }}
        onChange={e => e && setQuestionStableId(e.value)}
      />
    </td>
    <td>
      {/* target */}
      <Select
        isDisabled={isEmpty(questionStableId)}
        options={Object.keys(AnswerMappingTargets).map(target => {
          return {
            value: target as string,
            label: AnswerMappingLabels[target as AnswerMappingTargetType] || target
          }
        })}
        onChange={e => e && setTargetType(e.value as AnswerMappingTargetType)}
        value={targetType && { value: targetType as string, label: AnswerMappingLabels[targetType] || targetType }}
      />
    </td>
    <td>
      {/* field */}
      <Select
        isDisabled={isEmpty(targetType)}
        options={targetType && Object.keys(AnswerMappingTargets[targetType] || {})?.map(field => {
          return {
            value: field,
            label: field,
            isDisabled: existingMappings.some(m => m.targetType === targetType && m.targetField === field)
          }
        })}
        value={targetField && { value: targetField, label: targetField }}
        onChange={e => e && setTargetField(e.value)}
      />
    </td>
    <td>
      {/* format */}
      {
        mapType === 'STRING_TO_LOCAL_DATE' ?
          <Select
            options={[{
              value: 'MM/dd/yyyy',
              label: 'MM/dd/yyyy'
            }]}
            onChange={e => e && setFormatString(e.value)}
          />
          : ''
      }
    </td>
    <td>
      {/* map type (automatically set) */}
      {AnswerMappingMapTypeLabels[mapType]}
    </td>
    <td>
      <button
        className='btn btn-success me-2'
        disabled={isEmpty(questionStableId) || isEmpty(targetType) || isEmpty(targetField)}
        onClick={() => onSave({
          id: '',
          questionStableId: questionStableId!,
          targetType: targetType!,
          targetField: targetField!,
          formatString: formatString || '',
          mapType,
          surveyId: survey.id,
          errorOnFail: false
        })}>
        <FontAwesomeIcon icon={faCheck}/>
      </button>
      <button className='btn btn-danger' onClick={onCancel}>
        <FontAwesomeIcon icon={faX}/>
      </button>
    </td>
  </tr>
}

const DeleteAnswerMappingModal = (
  { onConfirm, onCancel } : { onConfirm: () => void, onCancel: () => void }
) => {
  return <Modal onHide={onCancel} show={true}>
    <ModalHeader>
      <ModalTitle>
        Are you sure you want to delete this answer mapping?
      </ModalTitle>
    </ModalHeader>
    <ModalBody>
      This action cannot be undone.
    </ModalBody>
    <ModalFooter>
      <button className='btn btn-danger' onClick={onConfirm}>
        Yes
      </button>
      <button className='btn btn-secondary' onClick={onCancel}>
        No
      </button>
    </ModalFooter>
  </Modal>
}
