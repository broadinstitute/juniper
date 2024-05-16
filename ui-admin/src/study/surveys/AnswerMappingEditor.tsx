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
import { Button } from '../../components/forms/Button'


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
type EditableAnswerMapping = Partial<AnswerMapping> & { isEditing: boolean }
type AnswerMappingRow = Partial<AnswerMapping> | EditableAnswerMapping

const isEditableMapping = (m: AnswerMappingRow): boolean => {
  return (m as EditableAnswerMapping)?.isEditing !== undefined
}
const isEditing = (m: AnswerMappingRow): boolean => {
  return !!(m as EditableAnswerMapping)?.isEditing
}

const EMPTY_EDITABLE_ANSWER_MAPPING: EditableAnswerMapping = {
  isEditing: false
}

/**
 * Table which allows viewing, deleting, and creating new answer mappings.
 */
export default function AnswerMappingEditor(
  {
    formContent, answerMappings, onChange
  } : {
    formContent: FormContent, answerMappings: AnswerMapping[], onChange: OnChangeAnswerMappings
  }
) {
  const [openDeleteMappingModal, setOpenDeleteMappingModal] = useState<AnswerMapping | null>(null)

  // state for new mapping
  const [newAnswerMapping, setNewAnswerMapping] = useState<EditableAnswerMapping>({
    ...EMPTY_EDITABLE_ANSWER_MAPPING
  })

  const deleteAnswerMapping = () => {
    if (!openDeleteMappingModal) {
      return
    }
    const filteredMappings = answerMappings.filter(m => m.id !== openDeleteMappingModal.id)
    onChange([], filteredMappings)
    setOpenDeleteMappingModal(null)
  }

  const saveNewMapping = (mapping: EditableAnswerMapping) => {
    const newMappings = [...answerMappings, {
      ...mapping,
      isEditing: undefined // clear editing flag -- it's not editable once confirmed
    } as AnswerMapping]

    onChange([], newMappings)
    setNewAnswerMapping({ ...EMPTY_EDITABLE_ANSWER_MAPPING })
  }

  const elements = getFormElements(formContent)
  const names = elements.filter(e => 'name' in e).map(e => 'name' in e ? e.name : '')

  useEffect(() => {
    // automatically set map type
    if (newAnswerMapping.targetType && newAnswerMapping.targetField) {
      setNewAnswerMapping({
        ...newAnswerMapping,
        mapType: (AnswerMappingTargets[newAnswerMapping.targetType] &&
          AnswerMappingTargets[newAnswerMapping.targetType][newAnswerMapping.targetField]) || 'STRING_TO_STRING'
      })
    }
  }, [newAnswerMapping.targetType, newAnswerMapping.targetField])

  const columns = useMemo<ColumnDef<AnswerMappingRow>[]>(() => [
    {
      header: 'Question ID',
      accessorKey: 'questionStableId',
      cell: ({ row }) => {
        const questionStableId = row.original.questionStableId
        if (isEditing(row.original)) {
          return <Creatable
            options={names.map(name => {
              return {
                value: name,
                label: name,
                isDisabled: answerMappings.some(m => m.questionStableId === name)
              }
            })}
            value={questionStableId && { value: questionStableId, label: questionStableId }}
            onChange={e => e && setNewAnswerMapping({
              ...row.original as EditableAnswerMapping,
              questionStableId: e.value as string
            })}
          />
        }
        return questionStableId
      }
    },
    {
      header: 'Target',
      accessorKey: 'targetType',
      cell: ({ row }) => {
        const value = row.original.targetType
        const questionStableId = row.original.questionStableId
        if (isEditing(row.original)) {
          return <Select
            isDisabled={isEmpty(questionStableId)}
            options={Object.keys(AnswerMappingTargets).map(target => {
              return {
                value: target as string,
                label: AnswerMappingLabels[target as AnswerMappingTargetType] || target
              }
            })}
            value={value && { value: value as string, label: AnswerMappingLabels[value] || value }}
            onChange={e => e && setNewAnswerMapping({
              ...row.original as EditableAnswerMapping,
              targetType: e.value as AnswerMappingTargetType
            })}
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
        if (isEditing(row.original)) {
          const targetType = row.original.targetType
          return <Select
            isDisabled={isEmpty(targetType)}
            options={targetType && Object.keys(AnswerMappingTargets[targetType] || {})?.map(field => {
              return {
                value: field,
                label: field,
                isDisabled: answerMappings.some(m => m.targetType === targetType && m.targetField === field)
              }
            })}
            value={value && { value, label: value }}
            onChange={e => e && setNewAnswerMapping({
              ...row.original as EditableAnswerMapping,
              targetField: e.value as string
            })}
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
        const mapType = row.original.mapType
        if (isEditing(row.original)) {
          return mapType === 'STRING_TO_LOCAL_DATE' ?
            <Select
              options={[{
                value: 'MM/dd/yyyy',
                label: 'MM/dd/yyyy'
              }]}
              onChange={e => e && setNewAnswerMapping({
                ...row.original as EditableAnswerMapping,
                formatString: e.value
              })}
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
        if (isEditing(row.original)) {
          return AnswerMappingMapTypeLabels[value as AnswerMappingMapType]
        }
        return value && AnswerMappingMapTypeLabels[value]
      }
    },
    {
      header: 'Actions',
      id: 'actions',
      cell: ({ row }) => {
        if (isEditableMapping(row.original)) {
          if (!isEditing(row.original)) {
            return <button className='btn btn-success border-0' onClick={() => setNewAnswerMapping({
              ...row.original as EditableAnswerMapping,
              isEditing: true
            })}>
              <FontAwesomeIcon icon={faPlus}/>
            </button>
          }
          return <>
            <Button
              className='btn btn-success me-2'
              disabled={isEmpty(row.original.questionStableId) ||
                isEmpty(row.original.targetType) || isEmpty(row.original.targetField)}
              onClick={() => saveNewMapping(row.original as EditableAnswerMapping)}>
              <FontAwesomeIcon icon={faCheck}/>
            </Button>
            <button className='btn btn-danger' onClick={() => setNewAnswerMapping({
              ...EMPTY_EDITABLE_ANSWER_MAPPING,
              isEditing: false
            })}>
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
  ], [])

  const data = useMemo(() => (answerMappings as AnswerMappingRow[])
    .concat(newAnswerMapping), [answerMappings, newAnswerMapping])

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
