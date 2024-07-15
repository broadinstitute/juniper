import React from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import {
  ColumnDef,
  getCoreRowModel,
  useReactTable
} from '@tanstack/react-table'
import {
  basicTableLayout,
  renderEmptyMessage
} from 'util/tableUtils'
import {
  faCheck,
  faPlus
} from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  Enrollee,
  Family,
  instantToDefaultString
} from '@juniper/ui-core'
import { AddFamilyModal } from 'study/families/AddFamilyModal'
import { Link } from 'react-router-dom'
import { faTrashCan } from '@fortawesome/free-solid-svg-icons/faTrashCan'
import Api from 'api/api'
import JustifyChangesModal from 'study/participants/JustifyChangesModal'
import { EnrolleeLink } from 'study/participants/enrolleeView/EnrolleeLink'
import { FamilyLink } from 'study/families/FamilyLink'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import InfoPopup from 'components/forms/InfoPopup'
import classNames from 'classnames'
import {
  InfoCard,
  InfoCardHeader
} from 'components/InfoCard'
import { Button } from 'components/forms/Button'


/** Shows a list of all kit requests for an enrollee. */
export default function Families({ enrollee, studyEnvContext, onUpdate }:
                                      {
                                        enrollee: Enrollee,
                                        studyEnvContext: StudyEnvContextT,
                                        onUpdate: () => void
                                      }) {
  const [addFamily, setAddFamily] = React.useState<boolean>(false)

  const [familySelectedForRemoval, setFamilySelectedForRemoval] = React.useState<Family>()

  const removeFromFamily = async (justification: string) => {
    if (!familySelectedForRemoval) {
      return
    }

    try {
      await Api.removeMemberFromFamily(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        familySelectedForRemoval.shortcode,
        enrollee.shortcode,
        justification
      )
    } catch (e) {
      Store.addNotification(failureNotification('Failed to remove from family'))
    }

    onUpdate()
  }

  const columns: ColumnDef<Family>[] = [{
    header: 'Shortcode',
    accessorKey: 'shortcode',
    cell: ({ row }) => <Link
      to={`${studyEnvContext.currentEnvPath}/families/${row.original.shortcode}`}>
      {row.original.shortcode}
    </Link>
  }, {
    header: 'Date Added',
    accessorKey: 'dateAdded',
    accessorFn: data => {
      const familyEnrollee = enrollee.familyEnrollees?.find(familyEnrollee => familyEnrollee.familyId === data.id)
      return familyEnrollee ? instantToDefaultString(familyEnrollee.createdAt) : ''
    }
  }, {
    header: 'Proband',
    accessorKey: 'proband',
    accessorFn: data => data.probandEnrolleeId === enrollee.id,
    cell: info => info.getValue() ? <FontAwesomeIcon icon={faCheck}/> : ''
  }, {
    header: 'Actions',
    cell: ({ row }) => {
      const isProband = row.original.probandEnrolleeId === enrollee.id
      return <div>
        <button
          disabled={isProband}

          className={classNames('btn btn-secondary', isProband && 'btn-outline-secondary')}
          onClick={() => setFamilySelectedForRemoval(row.original)}>
          <FontAwesomeIcon icon={faTrashCan}/>
        </button>
        {isProband && <InfoPopup
          content={'It isn\'t possible to remove the proband from the family. ' +
            'To remove this user, please first assign a new proband.'}
        />}
      </div>
    }
  }]

  const table = useReactTable({
    data: enrollee.families || [],
    columns,
    getCoreRowModel: getCoreRowModel()
  })

  return <InfoCard>

    <InfoCardHeader>
      <div className="d-flex align-items-center justify-content-between w-100">

        <div className="fw-bold lead my-1">Families</div>

        <Button onClick={() => setAddFamily(true)}
          variant="light" className="border m-1">
          <FontAwesomeIcon icon={faPlus}/> Add family
        </Button>
      </div>
    </InfoCardHeader>


    {basicTableLayout(table, { tableClass: 'table m-0' })}

    {enrollee.families?.length === 0 && <div className='my-3'>
      {renderEmptyMessage(enrollee.families || [], 'No families')}
    </div>}

    {addFamily && <AddFamilyModal
      enrollee={enrollee}
      studyEnvContext={studyEnvContext}
      onAddFamily={onUpdate}
      onClose={() => setAddFamily(false)}
    />}

    {familySelectedForRemoval && <JustifyChangesModal
      saveWithJustification={removeFromFamily}
      bodyText={<p>
        Are you sure you want to remove <span className="fst-italic"><EnrolleeLink
          studyEnvContext={studyEnvContext}
          enrollee={enrollee}
        /></span> from <span className="fst-italic"><FamilyLink
          family={familySelectedForRemoval}
          studyEnvContext={studyEnvContext}
        /></span>?
      </p>}
      onDismiss={() => setFamilySelectedForRemoval(undefined)}/>}
  </InfoCard>
}
