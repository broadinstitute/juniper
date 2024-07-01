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
  faPlus,
  faX
} from '@fortawesome/free-solid-svg-icons'
import { faTrashCan } from '@fortawesome/free-solid-svg-icons/faTrashCan'
import { EnrolleeLink } from 'study/participants/enrolleeView/EnrolleeLink'
import { EnrolleeSearchbar } from 'study/participants/enrolleeView/EnrolleeSearchbar'
import { isNil } from 'lodash'
import Api from 'api/api'
import { concatSearchExpressions } from 'util/searchExpressionUtils'
import JustifyChangesModal from 'study/participants/JustifyChangesModal'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import classNames from 'classnames'
import InfoPopup from 'components/forms/InfoPopup'

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
  const [openSaveNewMemberModal, setOpenSaveNewMemberModal] = React.useState<boolean>(false)

  const deleteMember = async (shortcode: string, justification: string) => {
    try {
      await Api.removeMemberFromFamily(studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        family.shortcode,
        shortcode,
        justification)

      setMembers(old => old.filter(m => m.shortcode !== shortcode))
      setMemberSelectedForDeletion(undefined)
      reloadFamily()
    } catch (e) {
      Store.addNotification(failureNotification('Could not remove member from family.'))
    }
  }


  const createNewMember = async (justification: string) => {
    try {
      await Api.addMemberToFamily(studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        family.shortcode,
        newMember!.shortcode,
        justification)

      setIsAddingNewMember(false)
      setMembers(old => [...old, newMember!])
      setNewMember(undefined)
      setOpenSaveNewMemberModal(false)
      reloadFamily()
    } catch (e) {
      Store.addNotification(failureNotification('Could not add member to family.'))
    }
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
      const isProband = row.original.id === family.probandEnrolleeId
      if (isNewMemberCreationRow(row)) {
        return <>
          <button
            className='btn btn-primary'
            onClick={async () => {
              setOpenSaveNewMemberModal(true)
            }}
            disabled={isNil(newMember)}
          >
            Save
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
        return <>
          <button
            className={classNames('btn btn-secondary border-0', isProband && 'btn-outline-secondary')}
            type={'button'}
            disabled={isProband}
            onClick={async () => {
              if (isNil(row.original.shortcode)) {
                return
              }

              setMemberSelectedForDeletion(row.original as Enrollee)
            }}
          >
            <FontAwesomeIcon
              icon={faTrashCan}
              aria-label={'Remove enrollee from family'}
            />
          </button>
          {isProband && <InfoPopup content="The proband cannot be removed from the family."/>}
        </>
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
    {memberSelectedForDeletion && <JustifyChangesModal
      bodyText={<>
        <p>Are you sure you want to remove <EnrolleeLink
          studyEnvContext={studyEnvContext}
          enrollee={memberSelectedForDeletion}
        /> from the family? All associated family relations will also be deleted.</p>
        <p>
            Note that this will not remove the enrollee from the study.
        </p>
      </>}
      saveWithJustification={justification => deleteMember(
        memberSelectedForDeletion?.shortcode || '',
        justification)}
      onDismiss={() => setMemberSelectedForDeletion(undefined)}
    />}

    {
      openSaveNewMemberModal &&
        <JustifyChangesModal
          saveWithJustification={createNewMember}
          onDismiss={() => setOpenSaveNewMemberModal(false)}
          bodyText={<p>
              Are you sure you want to add <EnrolleeLink
              studyEnvContext={studyEnvContext}
              enrollee={newMember as Enrollee}/> to the family?
          </p>
          }
        />
    }
  </div>
}
