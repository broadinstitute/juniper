import React, { useMemo } from 'react'
import {
  Enrollee,
  EnrolleeRelation,
  Family
} from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  Row,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import Creatable from 'react-select/creatable'
import { EnrolleeLink } from 'study/participants/enrolleeView/EnrolleeLink'
import { EnrolleeSearchbar } from 'study/participants/enrolleeView/EnrolleeSearchbar'
import {
  faFloppyDisk,
  faPlus,
  faX
} from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTrashCan } from '@fortawesome/free-solid-svg-icons/faTrashCan'
import {
  cloneDeep,
  isEmpty
} from 'lodash'
import Api from 'api/api'
import JustifyChangesModal from 'study/participants/JustifyChangesModal'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'

/**
 * Editable view of the relationships within a family.
 */
export const FamilyRelations = ({
  family, studyEnvContext, reloadFamily
}:{
  family: Family, studyEnvContext: StudyEnvContextT, reloadFamily: () => void
}) => {
  const [relations, setRelations] = React.useState<EnrolleeRelation[]>(family.relations || [])
  const [isAddingNewRelation, setIsAddingNewRelation] = React.useState<boolean>(false)

  const [newRelation, setNewRelation] = React.useState<Partial<EnrolleeRelation>>({})
  const [openSaveNewRelationModal, setOpenSaveNewRelationModal] = React.useState<boolean>(false)

  const [relationSelectedForDeletion, setRelationSelectedForDeletion] = React.useState<string>()

  const isNewRelationCreationRow = (row: Row<Partial<EnrolleeRelation>>) => {
    return isAddingNewRelation && row.index === 0
  }

  const createNewRelation = async (justification: string) => {
    try {
      if (!newRelation) {
        return
      }

      if (isEmpty(newRelation.enrolleeId)
        || isEmpty(newRelation.targetEnrolleeId)
        || isEmpty(newRelation.familyRelationship)) {
        return
      }

      const relation = cloneDeep(newRelation)

      relation.relationshipType = 'FAMILY'
      relation.familyId = family.id

      const createdRelation = await Api.createRelation(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        relation as EnrolleeRelation,
        justification
      )

      setRelations(old => [...old, createdRelation])
      setNewRelation({})
      setIsAddingNewRelation(false)
      setOpenSaveNewRelationModal(false)
      reloadFamily()
    } catch (e) {
      Store.addNotification(failureNotification('Could not create relationship.'))
    }
  }

  const isMemberOfFamily = (enrolleeId: string) => {
    return family.members?.findIndex(m => m.id === enrolleeId) !== -1
  }

  const deleteRelation = async (relationId: string, justification: string) => {
    try {
      await Api.deleteRelation(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        relationId,
        justification
      )
      setRelations(old => old.filter(r => r.id !== relationId))
      setRelationSelectedForDeletion(undefined)
      reloadFamily()
    } catch (e) {
      Store.addNotification(failureNotification('Could not delete relationship.'))
    }
  }

  const columns: ColumnDef<Partial<EnrolleeRelation>>[] = [{
    header: 'Enrollee',
    accessorKey: 'enrolleeId',
    size: 500,
    cell: ({ row }) => {
      if (isNewRelationCreationRow(row)) {
        return <EnrolleeSearchbar
          studyEnvContext={studyEnvContext}
          onEnrolleeSelected={enrollee => {
            setNewRelation(old => {
              return { ...old, enrollee, enrolleeId: enrollee?.id }
            })
          }}
          selectedEnrollee={newRelation?.enrollee}
        />
      }

      const enrollee = row.original.enrollee || family.members?.find(m => m.id === row.original.targetEnrolleeId)
      return <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={enrollee}/>
    }
  }, {
    header: 'Relation',
    accessorKey: 'familyRelationship',
    size: 500,
    cell: ({ row }) => {
      const relationship = row.original?.familyRelationship
      return <span className="fst-italic">
        is the <Creatable
          isDisabled={!isNewRelationCreationRow(row)}
          options={[]}
          styles={{
            control: provided => ({ ...provided, minWidth: '120px' })
          }}
          value={{ value: relationship, label: relationship }}
          onChange={option => setNewRelation(old => {
            return { ...old, familyRelationship: option?.value || '' }
          })}
          className={'d-inline-block'}
        /> of
      </span>
    }
  }, {
    header: 'Target',
    accessorKey: 'targetEnrolleeId',
    size: 500,
    cell: ({ row }) => {
      if (isNewRelationCreationRow(row)) {
        return <EnrolleeSearchbar
          studyEnvContext={studyEnvContext}
          onEnrolleeSelected={enrollee => {
            setNewRelation(old => {
              return { ...old, targetEnrollee: enrollee, targetEnrolleeId: enrollee?.id }
            })
          }}
          selectedEnrollee={newRelation?.targetEnrollee}
        />
      }

      const enrollee = row.original.targetEnrollee || family.members?.find(m => m.id === row.original.targetEnrolleeId)
      return <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={enrollee}/>
    }
  },
  {
    header: 'Actions',
    size: 130,
    maxSize: 130,
    cell: ({ row }) => {
      if (isNewRelationCreationRow(row)) {
        return <>
          <button
            className='btn btn-primary'
            onClick={() => setOpenSaveNewRelationModal(true)}
            disabled={!newRelation?.enrolleeId || !newRelation?.targetEnrolleeId}
          >
            <FontAwesomeIcon icon={faFloppyDisk} aria-label={'Add relation to family'}/>
          </button>
          <button
            className='btn btn-secondary'
            onClick={() => {
              setIsAddingNewRelation(false)
              setNewRelation({})
            }}
          >
            <FontAwesomeIcon icon={faX} aria-label={'Cancel adding relation to family'}/>
          </button>
        </>
      } else {
        return <button
          className='btn btn-secondary border-0'
          onClick={async () => {
            if (row.original.id) {
              setRelationSelectedForDeletion(row.original.id)
            }
          }}
        >
          <FontAwesomeIcon icon={faTrashCan} aria-label={'Remove relation from family'}/>
        </button>
      }
    }
  }]


  const table = useReactTable({
    data: useMemo(() => {
      return (isAddingNewRelation ? [newRelation] : []).concat(relations as Partial<EnrolleeRelation>[])
    }, [relations, isAddingNewRelation, newRelation]),
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })
  return <div>
    <h4>
      Relations
      <button className='btn btn-secondary ms-2' onClick={() => setIsAddingNewRelation(true)}>
        <FontAwesomeIcon className='me-2' icon={faPlus} aria-label={'Add new relation'}/>
        Add new relation
      </button>
    </h4>
    {basicTableLayout(table, { trClass: 'align-middle', useSize: true })}

    {relationSelectedForDeletion && <JustifyChangesModal
      saveWithJustification={justification => deleteRelation(relationSelectedForDeletion, justification)}
      onDismiss={() => setRelationSelectedForDeletion(undefined)}
    />}

    {openSaveNewRelationModal &&
        <JustifyChangesModal
          saveWithJustification={createNewRelation}
          onDismiss={() => setOpenSaveNewRelationModal(false)}
          bodyText={<>
            {(!isMemberOfFamily(newRelation.enrolleeId || '')
                  || !isMemberOfFamily(newRelation.targetEnrolleeId || ''))
                && <p>
                  <strong>Warning:</strong> creating this relation will also add new members of the family:
                  <ul>
                    {!isMemberOfFamily(newRelation.enrolleeId || '') && <li>
                      <EnrolleeLink studyEnvContext={studyEnvContext}
                        enrollee={newRelation.enrollee as Enrollee}/>
                    </li>}
                    {!isMemberOfFamily(newRelation.targetEnrolleeId || '') && <li>
                      <EnrolleeLink studyEnvContext={studyEnvContext}
                        enrollee={newRelation.targetEnrollee as Enrollee}/>
                    </li>}
                  </ul>
                </p>}
          </>}
        />}
  </div>
}
