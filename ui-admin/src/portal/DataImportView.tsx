import React, { useState } from 'react'
import Api, { DataImportItem } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import {
  basicTableLayout,
  DownloadControl,
  renderEmptyMessage,
  RowVisibilityCount
} from '../util/table/tableUtils'
import {
  currentIsoDate,
  Enrollee,
  instantToDefaultString
} from '@juniper/ui-core'
import { useLoadingEffect } from '../api/api-utils'
import { renderPageHeader } from 'util/pageUtils'
import {
  StudyEnvContextT,
  useStudyEnvParamsFromPath
} from '../study/StudyEnvironmentRouter'
import {
  Link,
  useParams
} from 'react-router-dom'
import AdHocEmailModal from 'study/participants/AdHocEmailModal'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faEnvelope } from '@fortawesome/free-solid-svg-icons'


/** show the dataImportItem list in table */
export default function DataImportView({ studyEnvContext }:
                                         { studyEnvContext: StudyEnvContextT }) {
  const [dataImportItems, setDataImportItems] = useState<DataImportItem[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const [showSendEmailModal, setShowSendEmailModal] = useState(false)

  const enrolleesToInvite: Enrollee[] = getSingleEnrolleePerAccount(dataImportItems)

  const importedDate = instantToDefaultString(dataImportItems[0]?.createdAt)
  const columns: ColumnDef<DataImportItem>[] = [
    {
      header: 'Shortcode',
      accessorKey: 'createdEnrollee.shortcode',
      cell: ({ row }) => {
        const enrolleeIdLast8 = row.original?.createdEnrolleeId?.slice(-8)
        if (row.original.status == 'DELETED') {
          return <p>detail-{enrolleeIdLast8}</p>
        } else if (row.original.status == 'FAILED') {
          return <p></p>
        } else {
          return <Link to={`${studyEnvContext.currentEnvPath}/participants/${row.original.createdEnrolleeId}`}
            className="me-1">{row.original.createdEnrollee?.shortcode || enrolleeIdLast8}</Link>
        }
      }
    },
    {
      header: 'Type',
      accessorKey: 'createdEnrollee.subject',
      cell: ({ row }) => {
        const enrollee = row.original.createdEnrollee
        if (!enrollee) {
          return <p></p>
        }

        return enrollee.subject ? 'participant' : 'proxy'
      }
    },
    {
      header: 'Status',
      accessorKey: 'status'
    },
    {
      header: 'Message',
      accessorKey: 'message',
      cell: ({ row }) => {
        const message = row.original.message
        if (message) {
          return <p className="text-break" style={{ width: '500px' }}>{message}</p>
        } else {
          return <p></p>
        }
      }
    },
    {
      header: 'Created At',
      accessorKey: 'createdAt',
      cell: ({ row }) => instantToDefaultString(row.original.createdAt)
    }
  ]

  const table = useReactTable({
    data: dataImportItems,
    columns,
    state: {
      sorting
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  const { dataImportId } = useParams()
  if (!dataImportId) {
    return <></>
  }
  const studyEnvParams = useStudyEnvParamsFromPath()
  const studyShortCode = studyEnvParams.studyShortcode
  if (!studyShortCode) {
    return <></>
  }

  const { isLoading } = useLoadingEffect(async () => {
    const result = await Api.fetchDataImport(studyEnvContext.portal.shortcode, studyShortCode,
      studyEnvContext.currentEnv.environmentName, dataImportId)
    setDataImportItems(result.importItems)
  }, [studyEnvContext.portal.shortcode, studyEnvContext.currentEnv.environmentName])

  return <div className="container-fluid px-4 py-2">
    {renderPageHeader('Data Import Items')}
    {`Imported Date:${importedDate}`}
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-center justify-content-between">
        <div className="d-flex">
          <RowVisibilityCount table={table}/>
        </div>
        <div className="d-flex">
          <DownloadControl
            table={table}
            fileName={`${studyEnvContext.portal.shortcode}-DataImportItem-${currentIsoDate()}`}
          />

          <Button className={'border m-1'} variant={'light'}
            onClick={() => setShowSendEmailModal(true)}>
            <FontAwesomeIcon icon={faEnvelope} className={'me-2'}/>
            Send Email to Participants
          </Button>
        </div>
      </div>

      {basicTableLayout(table)}
      {renderEmptyMessage(dataImportItems, 'No data import items')}
    </LoadingSpinner>

    {showSendEmailModal &&
        <AdHocEmailModal
          studyEnvContext={studyEnvContext}
          onDismiss={() => setShowSendEmailModal(false)}
          recipient={{
            type: 'shortcodes',
            enrolleeShortcodes: enrolleesToInvite.map(enrollee => enrollee.shortcode)
          }}/>}
  </div>
}

const removeProxyEmailSuffix = (email: string) => {
  // remove -prox-ABCD from the end of the email

  const regex = /-prox-[A-Z0-9]{4}$/

  const match = email.match(regex)
  if (match) {
    return email.slice(0, match.index)
  }
  return email
}

// if there are multiple enrollee for the same email, e.g. with proxies,
// make sure we only return one enrollee per email. that way the
// invitation email doesn't go to the same email multiple times
const getSingleEnrolleePerAccount = (dataImportItems: DataImportItem[]): Enrollee[] => {
  return dataImportItems
    .filter(item => item.createdEnrollee && item.createdEnrollee.shortcode && item.createdEnrollee.subject)
    .map(item => {
      return {
        enrollee: item.createdEnrollee,
        email: removeProxyEmailSuffix(item.createdParticipantUser?.username || '')
      }
    })
    .reduce((acc, data) => {
      // remove any duplicate emails
      if (!acc.some(item => item.email === data.email)) {
        acc.push(data)
      }
      return acc
    }, [] as { enrollee: Enrollee | undefined, email: string }[])
    .map(data => data.enrollee)
    .filter(enrollee => enrollee !== undefined) as Enrollee[]
}
