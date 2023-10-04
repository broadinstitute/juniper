import React from 'react'
import {Table} from "@tanstack/react-table";

/** renders a client-side pagination control (as in, the data is all loaded from the server at once, but
 * paged on the UI for readability */
export default function TableClientPagination<R>({table}: {table: Table<R>}) {
    return <div>
        <div className="flex items-center gap-2">
            <button
                className="border rounded p-1"
                onClick={() => table.setPageIndex(0)}
                disabled={!table.getCanPreviousPage()}
            >
                {'<<'}
            </button>
            <button
                className="border rounded p-1"
                onClick={() => table.previousPage()}
                disabled={!table.getCanPreviousPage()}
            >
                {'<'}
            </button>
            <button
                className="border rounded p-1"
                onClick={() => table.nextPage()}
                disabled={!table.getCanNextPage()}
            >
                {'>'}
            </button>
            <button
                className="border rounded p-1"
                onClick={() => table.setPageIndex(table.getPageCount() - 1)}
                disabled={!table.getCanNextPage()}
            >
                {'>>'}
            </button>
            <span className="flex items-center gap-1">
          <div>Page</div>
          <strong>
            {table.getState().pagination.pageIndex + 1} of{' '}
              {table.getPageCount()}
          </strong>
        </span>
            <span className="flex items-center gap-1">
          | Go to page:
          <input
              type="number"
              defaultValue={table.getState().pagination.pageIndex + 1}
              onChange={e => {
                  const page = e.target.value ? Number(e.target.value) - 1 : 0
                  table.setPageIndex(page)
              }}
              className="border p-1 rounded w-16"
          />
        </span>
            <select
                value={table.getState().pagination.pageSize}
                onChange={e => {
                    table.setPageSize(Number(e.target.value))
                }}
            >
                {[10, 20, 30, 40, 50].map(pageSize => (
                    <option key={pageSize} value={pageSize}>
                        Show {pageSize}
                    </option>
                ))}
            </select>
        </div>
        <div>{table.getRowModel().rows.length} Rows</div>
        <pre>{JSON.stringify(table.getState().pagination, null, 2)}</pre>
    </div>
}