import React, { useEffect, useMemo, useState } from 'react'
import { faCheck, faPlus, faX } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import Select from 'react-select'
import Creatable from 'react-select/creatable'
import {
  AnswerMapping,
  AnswerMappingMapType,
  AnswerMappingTargetType,
  FormContent,
  getFormElements
} from '@juniper/ui-core'
import { isEmpty } from 'lodash'
import { Modal, ModalBody, ModalFooter, ModalHeader, ModalTitle } from 'react-bootstrap'
import { OnChangeAnswerMappings } from '../../forms/formEditorTypes'
import { ColumnDef, getCoreRowModel, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from '../../util/tableUtils'
import { faTrashCan } from '@fortawesome/free-solid-svg-icons/faTrashCan'


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
  'birthDate': 'STRING_TO_LOCAL_DATE',
  'doNotEmail': 'STRING_TO_BOOLEAN',
  'doNotEmailSolicit': 'STRING_TO_BOOLEAN',
  'sexAtBirth': 'STRING_TO_STRING',
  'mailingAddress.street1': 'STRING_TO_STRING',
  'mailingAddress.street2': 'STRING_TO_STRING',
  'mailingAddress.city': 'STRING_TO_STRING',
  'mailingAddress.state': 'STRING_TO_STRING',
  'mailingAddress.postalCode': 'STRING_TO_STRING',
  'mailingAddress.country': 'STRING_TO_STRING'
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

type AnswerMappingRow = Partial<AnswerMapping> & { isLastRow?: boolean }

/**
 * Table which allows viewing, deleting, and creating new answer mappings.
 */
export default function AnswerMappingEditor(
  {
    formContent, initialAnswerMappings, onChange
  } : {
    formContent: FormContent, initialAnswerMappings: AnswerMapping[], onChange: OnChangeAnswerMappings
  }
) {
  const [answerMappings, setAnswerMappings] = useState<AnswerMapping[]>(initialAnswerMappings || [])

  const [openDeleteMappingModal, setOpenDeleteMappingModal] = useState<AnswerMapping | null>(null)


  const deleteAnswerMapping = async () => {
    if (!openDeleteMappingModal) {
      return
    }

    const filteredMappings = answerMappings.filter(m => m.id !== openDeleteMappingModal.id)

    onChange([], filteredMappings)
    setAnswerMappings(filteredMappings)
    setOpenDeleteMappingModal(null)
  }

  const [addNewAnswerMapping, setAddNewAnswerMapping] = useState(false)

  const saveNewMapping = async (mapping: AnswerMapping) => {
    const newMappings = [...answerMappings, mapping]

    onChange([], newMappings)
    setAnswerMappings(newMappings)
    setAddNewAnswerMapping(false)
    setQuestionStableId(undefined)
    setTargetType(undefined)
    setTargetField(undefined)
    setFormatString(undefined)
    setMapType('STRING_TO_STRING')
  }

  // state for new mapping
  const [questionStableId, setQuestionStableId] = useState<string>()
  const [targetType, setTargetType] = useState<AnswerMappingTargetType>()
  const [targetField, setTargetField] = useState<string>()
  const [formatString, setFormatString] = useState<string>()
  const [mapType, setMapType] = useState<AnswerMappingMapType>('STRING_TO_STRING')

  const elements = getFormElements(formContent)
  const names = elements.filter(e => 'name' in e).map(e => 'name' in e ? e.name : '')

  useEffect(() => {
    // automatically set map type
    if (targetType && targetField) {
      setMapType(
        (AnswerMappingTargets[targetType] && AnswerMappingTargets[targetType][targetField]) || 'STRING_TO_STRING'
      )
    }
  }, [targetType, targetField])

  const columns = useMemo<ColumnDef<AnswerMappingRow>[]>(() => [
    {
      header: 'Question ID',
      accessorKey: 'questionStableId',
      cell: ({ row }) => {
        const value = row.original.questionStableId
        if (row.original.isLastRow && addNewAnswerMapping) {
          return <Creatable
            options={names.map(name => {
              return {
                value: name,
                label: name,
                isDisabled: answerMappings.some(m => m.questionStableId === name)
              }
            })}
            value={questionStableId && { value: questionStableId, label: questionStableId }}
            onChange={e => e && setQuestionStableId(e.value)}
          />
        }
        return value
      }
    },
    {
      header: 'Target',
      accessorKey: 'targetType',
      cell: ({ row }) => {
        const value = row.original.targetType
        if (row.original.isLastRow && addNewAnswerMapping) {
          return <Select
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
        }
        return value && AnswerMappingLabels[value]
      }
    },
    {
      header: 'Field',
      accessorKey: 'targetField',
      cell: ({ row }) => {
        const value = row.original.targetField
        if (row.original.isLastRow && addNewAnswerMapping) {
          return <Select
            isDisabled={isEmpty(targetType)}
            options={targetType && Object.keys(AnswerMappingTargets[targetType] || {})?.map(field => {
              return {
                value: field,
                label: field,
                isDisabled: answerMappings.some(m => m.targetType === targetType && m.targetField === field)
              }
            })}
            value={targetField && { value: targetField, label: targetField }}
            onChange={e => e && setTargetField(e.value)}
          />
        }
        return value
      }
    },
    {
      header: 'Format',
      accessorKey: 'formatString',
      cell: ({ row }) => {
        const value = row.original.formatString
        if (row.original.isLastRow && addNewAnswerMapping) {
          return mapType === 'STRING_TO_LOCAL_DATE' ?
            <Select
              options={[{
                value: 'MM/dd/yyyy',
                label: 'MM/dd/yyyy'
              }]}
              onChange={e => e && setFormatString(e.value)}
            />
            : ''
        }
        return value
      }
    },
    {
      header: 'Type',
      accessorKey: 'mapType',
      cell: ({ row }) => {
        const value = row.original.mapType
        if (row.original.isLastRow && addNewAnswerMapping) {
          return AnswerMappingMapTypeLabels[mapType]
        }
        return value && AnswerMappingMapTypeLabels[value]
      }
    },
    {
      header: 'Actions',
      id: 'actions',
      cell: ({ row }) => {
        if (row.original.isLastRow) {
          if (!addNewAnswerMapping) {
            return <button className='btn btn-success border-0' onClick={() => setAddNewAnswerMapping(true)}>
              <FontAwesomeIcon icon={faPlus}/>
            </button>
          }

          return <>
            <button
              className='btn btn-success me-2'
              disabled={isEmpty(questionStableId) || isEmpty(targetType) || isEmpty(targetField)}
              onClick={() => saveNewMapping({
                id: '',
                questionStableId: questionStableId!,
                targetType: targetType!,
                targetField: targetField!,
                formatString: formatString || '',
                mapType,
                surveyId: '', // backend will set
                errorOnFail: false
              })}>
              <FontAwesomeIcon icon={faCheck}/>
            </button>
            <button className='btn btn-danger' onClick={() => setAddNewAnswerMapping(false)}>
              <FontAwesomeIcon icon={faX}/>
            </button>
          </>
        }
        return <button className='btn btn-outline-danger border-0' onClick={() => {
          setOpenDeleteMappingModal(row.original as AnswerMapping)
        }}>
          <FontAwesomeIcon icon={faTrashCan}/>
        </button>
      }
    }
  ], [addNewAnswerMapping, questionStableId, targetType, targetField, formatString, mapType, answerMappings])

  const data = useMemo(() => (answerMappings as AnswerMappingRow[]).concat({ isLastRow: true }), [answerMappings])

  const table = useReactTable<AnswerMappingRow>({
    data,
    columns,
    getCoreRowModel: getCoreRowModel()
  })

  return <div className='px-3 pt-1'>
    <p>
      Answer mappings allow you to map answers from questions in the survey to other parts of the system.
      For example, you could map a question which collects the participant&apos;s first name to the profile&apos;s
      given name field. Then, any changes the participant makes to the survey will result in changes to their profile.
    </p>
    {basicTableLayout(table)}
    {openDeleteMappingModal && <DeleteAnswerMappingModal
      onConfirm={deleteAnswerMapping}
      onCancel={() => setOpenDeleteMappingModal(null)}/>}
    <div>
    </div>
  </div>
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
