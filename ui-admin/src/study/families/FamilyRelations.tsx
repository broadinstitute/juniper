import React, { useMemo } from 'react'
import {
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
  faCheck,
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
import { ConfirmationModal } from 'components/ConfirmationModal'

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

  const [relationSelectedForDeletion, setRelationSelectedForDeletion] = React.useState<string>()

  const isNewRelationCreationRow = (row: Row<Partial<EnrolleeRelation>>) => {
    return isAddingNewRelation && row.index === 0
  }

  const createNewRelation = async () => {
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
      relation as EnrolleeRelation
    )

    setRelations(old => [...old, createdRelation])
    setNewRelation({})
    setIsAddingNewRelation(false)
    reloadFamily()
  }

  const deleteRelation = async (relationId: string) => {
    await Api.deleteRelation(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      relationId
    )
    setRelations(old => old.filter(r => r.id !== relationId))
    reloadFamily()
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
            className='btn btn-success'
            onClick={createNewRelation}
            disabled={!newRelation?.enrolleeId || !newRelation?.targetEnrolleeId}
          >
            <FontAwesomeIcon icon={faCheck} aria-label={'Add relation to family'}/>
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

    {relationSelectedForDeletion && <ConfirmationModal
      title={'Are you sure you want to delete this relation?'}
      body={'This action cannot be undone.'}
      onConfirm={() => deleteRelation(relationSelectedForDeletion)}
      onCancel={() => setRelationSelectedForDeletion(undefined)}
      confirmButtonStyle={'btn-danger'}
    />}
  </div>
}
