import React from 'react'
import { flexRender, Header } from '@tanstack/react-table'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCaretDown, faCaretUp } from '@fortawesome/free-solid-svg-icons'

export const sortableTableHeader = (header: Header<any, any>) => {
  return <th key={header.id}>
    <div
      {...{
        className: header.column.getCanSort() ? 'cursor-pointer select-none' : '',
        onClick: header.column.getToggleSortingHandler(),
        role: 'button'
      }}>
      {flexRender(header.column.columnDef.header, header.getContext())}
      {{
        asc: <FontAwesomeIcon icon={faCaretUp}/>,
        desc: <FontAwesomeIcon icon={faCaretDown}/>
      }[header.column.getIsSorted() as string] ?? null}
    </div>
  </th>
}
