import React, { useEffect } from 'react'
import { Table } from '@tanstack/react-table'
import { faCaretLeft, faCaretRight } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { useSearchParams } from 'react-router-dom'

/** renders a client-side pagination control (as in, the data is all loaded from the server at once, but
 * paged on the UI for readability */
export default function TableClientPagination<R>({ table, preferredNumRowsKey }: {
  table: Table<R>,
  preferredNumRowsKey: string | undefined
}) {
  const [searchParams, setSearchParams] = useSearchParams()

  useEffect(() => {
    searchParams.set('pageIndex', table.getState().pagination.pageIndex.toString())
    searchParams.set('pageSize', table.getState().pagination.pageSize.toString())
    setSearchParams(searchParams)
  }, [table.getState().pagination.pageIndex, table.getState().pagination.pageSize])

  return <div style={{
    padding: '0 1rem 1rem 1rem',
    display: 'flex', justifyContent: 'space-between'
  }}>
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <div>Showing {table.getRowModel().rows.length} of {table.getFilteredRowModel().rows.length} rows</div>
      <select
        aria-label={'Number of rows per page'}
        className="form-select m-1 w-auto"
        value={table.getState().pagination.pageSize}
        onChange={e => {
          table.setPageSize(Number(e.target.value))
          // save the preferred number of rows to local storage so the user doesn't have to keep changing it
          if (preferredNumRowsKey) { localStorage.setItem(preferredNumRowsKey, e.target.value) }
        }}
      >
        {[10, 25, 50, 100].map(pageSize => (
          <option key={pageSize} value={pageSize}>
            {pageSize}
          </option>
        ))}
      </select>
    </div>
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <button
        aria-label={'Previous page'}
        className="btn btn-light border m-1"
        onClick={() => table.previousPage()}
        disabled={!table.getCanPreviousPage()}
      >
        <FontAwesomeIcon icon={faCaretLeft} className="fa-sm"/>
      </button>
      <span>
        Page {table.getState().pagination.pageIndex + 1} of {table.getPageCount()}
      </span>
      <button
        aria-label={'Next page'}
        className="btn btn-light border m-1"
        onClick={() => table.nextPage()}
        disabled={!table.getCanNextPage()}
      >
        <FontAwesomeIcon icon={faCaretRight} className="fa-sm"/>
      </button>
    </div>
  </div>
}
