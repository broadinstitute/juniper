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
  faX
} from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTrashCan } from '@fortawesome/free-solid-svg-icons/faTrashCan'
import {
  cloneDeep,
  isEmpty
} from 'lodash'
import Api from 'api/api'

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

  const isNewRelationCreationRow = (row: Row<Partial<EnrolleeRelation>>) => {
    return isAddingNewRelation && row.index === family.relations?.length
  }

  console.log(relations)
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
          searchExpFilter={`{family.shortcode} = '${family.shortcode}'`}
        />
      }

      const enrollee = row.original.enrollee || family.members?.find(m => m.id === row.original.targetEnrolleeId)
      return <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={enrollee}/>
    }
  }, {
    header: 'Relation',
    accessorKey: 'familyRelationship',
    cell: ({ row }) => {
      const relationship = row.original?.familyRelationship
      return <span className="fst-italic">
        is the <Creatable
          isDisabled={!isNewRelationCreationRow(row)}
          options={[]}
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
          searchExpFilter={`{family.shortcode} = '${family.shortcode}'`}
        />
      }

      const enrollee = row.original.targetEnrollee || family.members?.find(m => m.id === row.original.targetEnrolleeId)
      return <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={enrollee}/>
    }
  },
  {
    header: 'Actions',
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
          className='btn btn-outline-danger border-0'
          onClick={async () => {
            if (row.original.id) {
              await deleteRelation(row.original.id)
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
      return (relations as Partial<EnrolleeRelation>[]).concat(isAddingNewRelation ? [newRelation] : [])
    }, [relations, isAddingNewRelation, newRelation]),
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })
  return <div>
    {basicTableLayout(table, { trClass: 'align-middle' })}
    <div className="d-flex justify-content-end">
      <button className="btn btn-link" onClick={() => setIsAddingNewRelation(true)}>+ Add new family relation</button>
    </div>
  </div>
}
