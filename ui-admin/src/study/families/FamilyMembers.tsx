import {
  Enrollee,
  Family
} from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  ColumnDef,
  getCoreRowModel,
  Row,
  useReactTable
} from '@tanstack/react-table'
import React, {
  useMemo,
  useState
} from 'react'
import { basicTableLayout } from 'util/tableUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCheck,
  faX
} from '@fortawesome/free-solid-svg-icons'
import { faTrashCan } from '@fortawesome/free-solid-svg-icons/faTrashCan'
import { EnrolleeLink } from 'study/participants/enrolleeView/EnrolleeLink'
import { EnrolleeSearchbar } from 'study/participants/enrolleeView/EnrolleeSearchbar'
import { isNil } from 'lodash'
import Api from 'api/api'

/**
 * Editable view of the members of a family.
 */
export const FamilyMembers = ({
  family, studyEnvContext
}:{
  family: Family, studyEnvContext: StudyEnvContextT
}) => {
  const [isAddingNewMember, setIsAddingNewMember] = useState<boolean>(false)
  const [newMember, setNewMember] = useState<Enrollee | null>(null)

  const isNewMemberCreationRow = (row: Row<Partial<Enrollee>>) => {
    return isAddingNewMember && row.index === family.members?.length
  }

  const [members, setMembers] = useState<Enrollee[]>(family.members || [])

  const columns: ColumnDef<Partial<Enrollee>>[] = useMemo(() => [{
    id: 'member',
    header: 'Member',
    accessorKey: 'member',
    cell: ({ row }) => {
      if (isNewMemberCreationRow(row)) {
        return <EnrolleeSearchbar
          studyEnvContext={studyEnvContext}
          selectedEnrollee={newMember}
          onEnrolleeSelected={enrollee => setNewMember(enrollee)}/>
      }

      return <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={row.original as Enrollee}/>
    }
  }, {
    header: 'Actions',
    cell: ({ row }) => {
      if (isNewMemberCreationRow(row)) {
        return <>
          <button
            className='btn btn-success'
            onClick={async () => {
              await Api.addMemberToFamily(studyEnvContext.portal.shortcode,
                studyEnvContext.study.shortcode,
                studyEnvContext.currentEnv.environmentName,
                family.shortcode,
                newMember!.shortcode)

              setIsAddingNewMember(false)
              setMembers(old => [...old, newMember!])
              setNewMember(null)
            }}
            disabled={isNil(newMember)}
          >
            <FontAwesomeIcon icon={faCheck} aria-label={'Add enrollee to family'}/>
          </button>
          <button
            className='btn btn-secondary'
            onClick={() => {
              setIsAddingNewMember(false)
              setNewMember(null)
            }}
          >
            <FontAwesomeIcon icon={faX} aria-label={'Cancel adding enrollee to family'}/>
          </button>
        </>
      } else {
        return <button
          className='btn btn-outline-danger border-0'
          onClick={async () => {
            if (isNil(row.original.shortcode)) {
              return
            }

            await Api.removeMemberFromFamily(studyEnvContext.portal.shortcode,
              studyEnvContext.study.shortcode,
              studyEnvContext.currentEnv.environmentName,
              family.shortcode,
              row.original.shortcode)

            setMembers(old => old.filter(m => m.shortcode !== row.original.shortcode))
          }}
        >
          <FontAwesomeIcon icon={faTrashCan} aria-label={'Remove enrollee from family'}/>
        </button>
      }
    }
  }], [isAddingNewMember, newMember, family.members, studyEnvContext])

  const data = useMemo(() => {
    return ((members as Partial<Enrollee>[]).concat((isAddingNewMember ? [{}] : []))) || []
  }, [members, isAddingNewMember])
  const table = useReactTable<Partial<Enrollee>>({
    data,
    columns,
    enableSorting: false,
    getCoreRowModel: getCoreRowModel()
  })

  return <div>
    {basicTableLayout(table)}
    <div className="d-flex justify-content-end">
      <button className="btn btn-link" onClick={() => setIsAddingNewMember(true)}>+ Add new family member</button>
    </div>
  </div>
}
