import React from 'react'
import { Table } from '@tanstack/react-table'
import { faCaretLeft, faCaretRight } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

/** renders a client-side pagination control (as in, the data is all loaded from the server at once, but
 * paged on the UI for readability */
export default function TableClientPagination<R>({ table }: {table: Table<R>}) {
  return <div style={{ padding: "0 1rem 1rem 1rem",
    display: 'flex', justifyContent: 'space-between' }}>
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <div>Showing {table.getRowModel().rows.length} of {table.getFilteredRowModel().rows.length} rows</div>
      <select
        className="form-select m-1 w-auto"
        value={table.getState().pagination.pageSize}
        onChange={e => {
          table.setPageSize(Number(e.target.value))
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
        className="btn btn-light border m-1"
        onClick={() => table.nextPage()}
        disabled={!table.getCanNextPage()}
      >
        <FontAwesomeIcon icon={faCaretRight} className="fa-sm"/>
      </button>
    </div>
  </div>
}
