import React, { HTMLProps } from 'react'
import { flexRender, Header } from '@tanstack/react-table'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCaretDown, faCaretUp } from '@fortawesome/free-solid-svg-icons'

/**
 * returns a clickable header column with up/down icons indicating sort direction
 * adapted from https://tanstack.com/table/v8/docs/examples/react/sorting
 * */
export function sortableTableHeader<A, B>(header: Header<A, B>) {
  return <th key={header.id}>
    <div className={header.column.getCanSort() ? 'cursor-pointer select-none' : ''}
      onClick={header.column.getToggleSortingHandler()} role="button">
      {flexRender(header.column.columnDef.header, header.getContext())}
      {{
        asc: <FontAwesomeIcon icon={faCaretUp}/>,
        desc: <FontAwesomeIcon icon={faCaretDown}/>
      }[header.column.getIsSorted() as string] ?? null}
    </div>
  </th>
}

/**
 * checkbox supporting the "indeterminate" state
 * from https://tanstack.com/table/v8/docs/examples/react/sorting
 * */
export function IndeterminateCheckbox({
  indeterminate,
  className = '',
  ...rest
}: { indeterminate?: boolean } & HTMLProps<HTMLInputElement>) {
  const ref = React.useRef<HTMLInputElement>(null)

  React.useEffect(() => {
    if (typeof indeterminate === 'boolean' && ref.current) {
      ref.current.indeterminate = !rest.checked && indeterminate
    }
  }, [ref, indeterminate])

  return (
    <input
      type="checkbox"
      ref={ref}
      className={`${className  } cursor-pointer`}
      {...rest}
    />
  )
}
