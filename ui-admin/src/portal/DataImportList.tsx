import React, { useState } from 'react'
import Api, { DataImport } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import {
  basicTableLayout,
  DownloadControl,
  IndeterminateCheckbox,
  renderEmptyMessage,
  RowVisibilityCount
} from 'util/tableUtils'
import { Button } from 'components/forms/Button'
import { failureNotification, successNotification } from '../util/notifications'
import { Store } from 'react-notifications-component'
import Modal from 'react-bootstrap/Modal'
import { useLoadingEffect } from '../api/api-utils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faAdd, faTrash } from '@fortawesome/free-solid-svg-icons'
import { renderPageHeader } from 'util/pageUtils'
import { StudyEnvContextT, useStudyEnvParamsFromPath } from '../study/StudyEnvironmentRouter'
import AddDataImportModal from './AddDataImportModal'
import { Link } from 'react-router-dom'
import { useAdminUserContext } from '../providers/AdminUserProvider'
import { currentIsoDate, instantToDefaultString } from '@juniper/ui-core'


/** show the dataImport list in table */
export default function DataImportList({ studyEnvContext }:
                                           { studyEnvContext: StudyEnvContextT }) {
  const [dataImports, setDataImports] = useState<DataImport[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const [showAddDataImportModal, setShowAddDataImportModal] = useState(false)
  const { users } = useAdminUserContext()
  const columns: ColumnDef<DataImport>[] = [{
    id: 'select',
    header: ({ table }) => <IndeterminateCheckbox
      checked={table.getIsAllRowsSelected()} indeterminate={table.getIsSomeRowsSelected()}
      onChange={table.getToggleAllRowsSelectedHandler()}/>,
    cell: ({ row }) => (
      <div className="px-1">
        <IndeterminateCheckbox
          checked={row.getIsSelected()} indeterminate={row.getIsSomeSelected()}
          onChange={row.getToggleSelectedHandler()} disabled={!row.getCanSelect()}/>
      </div>
    )
  },
  {
    header: 'Imported Date',
    accessorKey: 'createdAt',
    meta: {
      columnType: 'instant'
    },
    cell: ({ row }) => {
      return <Link to={`${studyEnvContext.currentEnvPath}/dataimports/${row.original.id}`}
        className="me-1"> {instantToDefaultString(row.original.createdAt)}</Link>
    }
  },

  {
    header: 'Operator',
    accessorKey: 'responsibleUserId',
    cell: info => users.find(user => user.id === info.getValue())?.username ||
                `superuser (${(info.getValue() as string)?.substring(27)})`
  },
  {
    header: 'Import type',
    accessorKey: 'importType'
  },
  {
    header: 'Status',
    accessorKey: 'status'
  }]

  const table = useReactTable({
    data: dataImports,
    columns,
    state: {
      sorting,
      rowSelection
    },
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    onRowSelectionChange: setRowSelection
  })

  const numSelected = Object.keys(rowSelection).length
  const studyEnvParams = useStudyEnvParamsFromPath()
  const studyShortCode = studyEnvParams.studyShortcode
  if (!studyShortCode) {
    return <></>
  }

  const { isLoading, reload } = useLoadingEffect(async () => {
    const result = await Api.fetchDataImports(studyEnvContext.portal.shortcode, studyShortCode,
      studyEnvContext.currentEnv.environmentName)
    setDataImports(result)
  }, [studyEnvContext.portal.shortcode, studyEnvContext.currentEnv.environmentName])

  const performDelete = async () => {
    const contactsSelected = Object.keys(rowSelection)
      .filter(key => rowSelection[key])
      .map(key => dataImports[parseInt(key)])
    try {
      await Promise.all(
        contactsSelected.map(contact => {
          if (contact.id) {
            return Api.deleteDataImport(
              studyEnvContext.portal.shortcode, studyEnvContext.currentEnv.environmentName, contact.id)
          }
        })
      )
      Store.addNotification(successNotification(`${contactsSelected.length} entries removed`))
    } catch {
      Store.addNotification(failureNotification('Error: some entries could not be removed'))
    }
    reload() // just reload the whole thing to be safe
    setShowDeleteConfirm(false)
  }

  return <div className="container-fluid px-4 py-2">
    {renderPageHeader('Data Imports')}
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-center justify-content-between">
        <div className="d-flex">
          <RowVisibilityCount table={table}/>
        </div>
        <div className="d-flex">
          <Button onClick={() => setShowAddDataImportModal(!showAddDataImportModal)}
            variant="light" className="border m-1" tooltip="Upload new Data Import">
            <FontAwesomeIcon icon={faAdd} className="fa-lg"/> New Data Import
          </Button>
          <DownloadControl
            table={table}
            fileName={`${studyEnvContext.portal.shortcode}-DataImport-${currentIsoDate()}.tsv`}
          />
          <Button onClick={() => setShowDeleteConfirm(!showDeleteConfirm)}
            variant="light" className="border m-1" disabled={!numSelected}
            tooltip={numSelected ? 'Remove selected imports' : 'You must select imports to remove'}>
            <FontAwesomeIcon icon={faTrash} className="fa-lg"/> Remove
          </Button>
        </div>
      </div>

      {basicTableLayout(table)}
      {renderEmptyMessage(dataImports, 'No data imports')}
      {showDeleteConfirm && <Modal show={true} onHide={() => setShowDeleteConfirm(false)}>
        <Modal.Body>
          <div>Do you want to delete the <strong>{numSelected}</strong> selected entries?</div>

          <div className="pt-3">This operation CANNOT BE UNDONE.</div>
        </Modal.Body>
        <Modal.Footer>
          <button type="button" className="btn btn-danger" onClick={performDelete}>
                        Delete
          </button>
          <button type="button" className="btn btn-secondary" onClick={() => setShowDeleteConfirm(false)}>
                        Cancel
          </button>
        </Modal.Footer>
      </Modal>}
      {showAddDataImportModal &&
                <AddDataImportModal
                  portalShortcode={studyEnvContext.portal.shortcode}
                  studyShortcode={studyEnvContext.study.shortcode}
                  envName={studyEnvContext.currentEnv.environmentName}
                  onDismiss={() => setShowAddDataImportModal(false)}
                  onSubmit={() => {
                    setShowAddDataImportModal(false)
                    reload()
                  }}
                />
      }
    </LoadingSpinner>
  </div>
}
