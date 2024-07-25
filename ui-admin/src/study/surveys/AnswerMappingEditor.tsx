import React, {
  useEffect,
  useMemo,
  useState
} from 'react'
import {
  faCheck,
  faPlus,
  faX
} from '@fortawesome/free-solid-svg-icons'
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
import {
  isEmpty,
  isNil
} from 'lodash'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
  ModalTitle
} from 'react-bootstrap'
import { OnChangeAnswerMappings } from '../../forms/formEditorTypes'
import {
  ColumnDef,
  getCoreRowModel,
  useReactTable
} from '@tanstack/react-table'
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

type EditableAnswerMapping = Partial<AnswerMapping> & { isEditing: boolean }
type AnswerMappingRow = AnswerMapping | EditableAnswerMapping
const isEditable = (row: AnswerMappingRow): row is EditableAnswerMapping => {
  return !isNil((row as EditableAnswerMapping).isEditing)
}

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

  const [mappingSelectedForDeletion, setMappingSelectedForDeletion] = useState<AnswerMapping | null>(null)


  const deleteAnswerMapping = async () => {
    if (!mappingSelectedForDeletion) {
      return
    }

    const filteredMappings = answerMappings.filter(m => m.id !== mappingSelectedForDeletion.id)

    onChange([], filteredMappings)
    setAnswerMappings(filteredMappings)
    setMappingSelectedForDeletion(null)
  }

  const saveNewMapping = async (mapping: AnswerMapping) => {
    const newMappings = [...answerMappings, mapping]

    onChange([], newMappings)
    setAnswerMappings(newMappings)
    setNewAnswerMapping({ isEditing: false })
  }

  // state for new mapping
  const [newAnswerMapping, setNewAnswerMapping] = useState<EditableAnswerMapping>({ isEditing: false })

  const elements = getFormElements(formContent)
  const names = elements
    .filter(e => 'name' in e)
    .map(e => 'name' in e ? e.name : '')
    .concat(formContent.calculatedValues?.map(cv => cv.name) || [])

  useEffect(() => {
    // automatically set map type
    if (newAnswerMapping?.targetType && newAnswerMapping?.targetField) {
      setNewAnswerMapping({
        ...newAnswerMapping,
        mapType: (
          (
            AnswerMappingTargets[newAnswerMapping.targetType]
            && AnswerMappingTargets[newAnswerMapping.targetType][newAnswerMapping.targetField]
          ) || 'STRING_TO_STRING'
        )
      })
    }
  }, [newAnswerMapping?.targetType, newAnswerMapping?.targetField])

  const onNewAnswerMappingChange = (field: keyof EditableAnswerMapping, value: string | boolean) => {
    setNewAnswerMapping({
      ...newAnswerMapping,
      [field]: value
    })
  }

  const columns = useMemo<ColumnDef<AnswerMappingRow>[]>(() => [
    {
      header: 'Question ID',
      accessorKey: 'questionStableId',
      cell: ({ row }) => {
        const value = row.original.questionStableId
        if (isEditable(row.original)) {
          return row.original.isEditing && <Creatable
            aria-label={'New Answer Mapping Question ID'}
            options={names.map(name => {
              return {
                value: name,
                label: name,
                isDisabled: answerMappings.some(m => m.questionStableId === name)
              }
            })}
            value={row.original.questionStableId && {
              value: row.original.questionStableId,
              label: row.original.questionStableId
            }}
            onChange={e => e && onNewAnswerMappingChange('questionStableId', e.value as string)}
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
        if (isEditable(row.original)) {
          return row.original.isEditing && <Select
            aria-label={'New Answer Mapping Target Type'}
            isDisabled={isEmpty(row.original.questionStableId)}
            options={Object.keys(AnswerMappingTargets).map(target => {
              return {
                value: target as string,
                label: AnswerMappingLabels[target as AnswerMappingTargetType] || target
              }
            })}
            onChange={e => e && onNewAnswerMappingChange('targetType', e.value as AnswerMappingTargetType)}
            value={row.original.targetType && {
              value: row.original.targetType as string,
              label: AnswerMappingLabels[row.original.targetType] || row.original.targetType
            }}
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
        if (isEditable(row.original)) {
          return row.original.isEditing && <Select
            aria-label={'New Answer Mapping Target Field'}
            isDisabled={isEmpty(row.original.targetType)}
            options={
              row.original.targetType && Object.keys(
                AnswerMappingTargets[row.original.targetType] || {})
                ?.map(field => {
                  return {
                    value: field,
                    label: field,
                    isDisabled: answerMappings.some(
                      m => m.targetType === row.original.targetType && m.targetField === field)
                  }
                })}
            value={row.original.targetField && { value: row.original.targetField, label: row.original.targetField }}
            onChange={e => e && onNewAnswerMappingChange('targetField', e.value)}
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
        if (isEditable(row.original)) {
          return row.original.isEditing && row.original.mapType === 'STRING_TO_LOCAL_DATE' ?
            <Select
              aria-label={'New Answer Mapping Format'}
              options={[{
                value: 'MM/dd/yyyy',
                label: 'MM/dd/yyyy'
              }]}
              value={row.original.formatString && {
                value: row.original.formatString,
                label: row.original.formatString
              }}
              onChange={e => e && onNewAnswerMappingChange('formatString', e.value)}
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
        if (isEditable(row.original)) {
          return row.original.isEditing && AnswerMappingMapTypeLabels[row.original.mapType as AnswerMappingMapType]
        }
        return value && AnswerMappingMapTypeLabels[value]
      }
    },
    {
      header: 'Actions',
      id: 'actions',
      cell: ({ row }) => {
        if (isEditable(row.original)) {
          if (!row.original.isEditing) {
            return <button
              className='btn btn-primary border-0'
              onClick={() => onNewAnswerMappingChange('isEditing', true)}>
              <FontAwesomeIcon icon={faPlus} aria-label={'Create New Answer Mapping'}/>
            </button>
          }

          return <>
            <button
              className='btn btn-success me-2'
              disabled={
                isEmpty(row.original.questionStableId)
                || isEmpty(row.original.targetType)
                || isEmpty(row.original.targetField)}
              onClick={() => saveNewMapping({
                id: '',
                questionStableId: row.original.questionStableId!,
                targetType: row.original.targetType!,
                targetField: row.original.targetField!,
                formatString: row.original.formatString || '',
                mapType: row.original.mapType || 'STRING_TO_STRING',
                surveyId: '', // backend will set
                errorOnFail: false
              })}>
              <FontAwesomeIcon icon={faCheck} aria-label={'Accept New Answer Mapping'}/>
            </button>
            <button className='btn btn-danger' onClick={() => onNewAnswerMappingChange('isEditing', false)}>
              <FontAwesomeIcon icon={faX} aria-label={'Cancel New Answer Mapping'}/>
            </button>
          </>
        }
        return <button className='btn btn-outline-danger border-0' onClick={() => {
          setMappingSelectedForDeletion(row.original as AnswerMapping)
        }}>
          <FontAwesomeIcon icon={faTrashCan} aria-label={'Delete Answer Mapping'}/>
        </button>
      }
    }
  ], [newAnswerMapping, answerMappings])

  const data = useMemo(
    () => (answerMappings as AnswerMappingRow[]).concat(newAnswerMapping),
    [answerMappings, newAnswerMapping])

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
    {basicTableLayout(table, { tdClass: 'col-1 ' })}
    {mappingSelectedForDeletion && <DeleteAnswerMappingModal
      onConfirm={deleteAnswerMapping}
      onCancel={() => setMappingSelectedForDeletion(null)}/>}
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
