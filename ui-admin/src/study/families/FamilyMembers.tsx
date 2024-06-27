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
  faPlus,
  faX
} from '@fortawesome/free-solid-svg-icons'
import { faTrashCan } from '@fortawesome/free-solid-svg-icons/faTrashCan'
import { EnrolleeLink } from 'study/participants/enrolleeView/EnrolleeLink'
import { EnrolleeSearchbar } from 'study/participants/enrolleeView/EnrolleeSearchbar'
import { isNil } from 'lodash'
import Api from 'api/api'
import { concatSearchExpressions } from 'util/searchExpressionUtils'
import { ConfirmationModal } from 'components/ConfirmationModal'

/**
 * Editable view of the members of a family.
 */
export const FamilyMembers = ({
  family, studyEnvContext, reloadFamily
}:{
  family: Family, studyEnvContext: StudyEnvContextT, reloadFamily: () => void
}) => {
  const [isAddingNewMember, setIsAddingNewMember] = useState<boolean>(false)
  const [newMember, setNewMember] = useState<Enrollee>()

  const isNewMemberCreationRow = (row: Row<Partial<Enrollee>>) => {
    return isAddingNewMember && row.index === 0
  }

  const [members, setMembers] = useState<Enrollee[]>(family.members || [])

  const [memberSelectedForDeletion, setMemberSelectedForDeletion] = React.useState<Enrollee>()

  const deleteMember = async (shortcode: string) => {
    await Api.removeMemberFromFamily(studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      family.shortcode,
      shortcode)

    setMembers(old => old.filter(m => m.shortcode !== shortcode))
    reloadFamily()
  }

  const columns: ColumnDef<Partial<Enrollee>>[] = useMemo(() => [{
    id: 'member',
    header: 'Member',
    accessorKey: 'member',
    minSize: 800,
    cell: ({ row }) => {
      if (isNewMemberCreationRow(row)) {
        return <EnrolleeSearchbar
          studyEnvContext={studyEnvContext}
          selectedEnrollee={newMember}
          onEnrolleeSelected={enrollee => setNewMember(enrollee)}
          searchExpFilter={
            // filter out the current members from the search results;
            // there might be a better way to do this, but I couldn't
            // figure out how to get the SQL to come out right without
            // any weird edge cases, and families should be O(10) anyway
            concatSearchExpressions(
              members.map(enrollee => `{enrollee.shortcode} != '${enrollee.shortcode}'`)
            )}
        />
      }

      return <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={row.original as Enrollee}/>
    }
  }, {
    header: 'Actions',
    size: 100,
    maxSize: 100,
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
              setNewMember(undefined)
              reloadFamily()
            }}
            disabled={isNil(newMember)}
          >
            <FontAwesomeIcon icon={faCheck} aria-label={'Add enrollee to family'}/>
          </button>
          <button
            className='btn btn-secondary'
            onClick={() => {
              setIsAddingNewMember(false)
              setNewMember(undefined)
            }}
          >
            <FontAwesomeIcon icon={faX} aria-label={'Cancel adding enrollee to family'}/>
          </button>
        </>
      } else {
        return <button
          className='btn btn-secondary border-0'
          onClick={async () => {
            if (isNil(row.original.shortcode)) {
              return
            }

            setMemberSelectedForDeletion(row.original as Enrollee)
          }}
        >
          <FontAwesomeIcon icon={faTrashCan} aria-label={'Remove enrollee from family'}/>
        </button>
      }
    }
  }], [isAddingNewMember, newMember, family.members, studyEnvContext])

  const data = useMemo(() => {
    return (isAddingNewMember ? [{}] : []).concat((members as Partial<Enrollee>[]))
  }, [members, isAddingNewMember])
  const table = useReactTable<Partial<Enrollee>>({
    data,
    columns,
    enableSorting: false,
    getCoreRowModel: getCoreRowModel()
  })

  return <div>
    <h4>
      Members
      <button className='btn btn-secondary ms-2' onClick={() => setIsAddingNewMember(true)}>
        <FontAwesomeIcon className={'me-2'} icon={faPlus} aria-label={'Add new member'}/>
        Add new member
      </button>
    </h4>
    {basicTableLayout(table, { useSize: true })}
    {memberSelectedForDeletion && <ConfirmationModal
      title={<>
          Are you sure you want to remove <EnrolleeLink
          studyEnvContext={studyEnvContext}
          enrollee={memberSelectedForDeletion}
        /> from the family?
      </>}
      body={'This action cannot be undone.'}
      onConfirm={() => deleteMember(memberSelectedForDeletion?.shortcode || '')}
      onCancel={() => setMemberSelectedForDeletion(undefined)}
      confirmButtonStyle={'btn-danger'}
    />}
  </div>
}
