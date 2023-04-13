import React, { HTMLProps, useState } from 'react'
import { flexRender, Header, Table } from '@tanstack/react-table'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCaretDown, faCaretUp, faColumns } from '@fortawesome/free-solid-svg-icons'

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

/**
 * adapted from https://tanstack.com/table/v8/docs/examples/react/column-visibility
 * For now, this control assumes that all the headers are simple strings.
 * */
export function ColumnVisibilityControl<T>({ table }: {table: Table<T>}) {
  const [show, setShow] = useState(false)
  return <div className="position-relative">
    <button className="btn btn-secondary" onClick={() => setShow(!show)} aria-label="show or hide columns">
      <FontAwesomeIcon icon={faColumns} className="fa-lg"/>
    </button>
    { show && <div className="position-absolute border border-gray rounded bg-white p-3"
      style={{ width: '300px', zIndex: 100, right: 0 }}>
      <div className="border-b border-black">
        <label>
          <input
            {...{
              type: 'checkbox',
              checked: table.getIsAllColumnsVisible(),
              onChange: table.getToggleAllColumnsVisibilityHandler()
            }}
          />
          <span className="ps-2">Toggle All</span>
        </label>
      </div>
      <hr/>
      {table.getAllLeafColumns().map(column => {
        return (
          <div key={column.id} className="pb-1">
            <label>
              <input
                {...{
                  type: 'checkbox',
                  checked: column.getIsVisible(),
                  onChange: column.getToggleVisibilityHandler()
                }}
              />
              <span className="ps-2">{ column.columnDef.header as string ?? column.columnDef.id }</span>
            </label>
          </div>
        )
      })}
    </div> }
  </div>
}

