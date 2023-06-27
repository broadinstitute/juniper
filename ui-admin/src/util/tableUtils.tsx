import React, { HTMLProps, useEffect, useState } from 'react'
import { Column, flexRender, Header, RowData, Table } from '@tanstack/react-table'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCaretDown, faCaretUp, faColumns } from '@fortawesome/free-solid-svg-icons'
import Select from 'react-select'

/**
 * Returns a debounced input react component
 * Adapted from https://tanstack.com/table/v8/docs/examples/react/filters
 */
function DebouncedInput({
  value: initialValue,
  onChange,
  debounce = 200,
  ...props
}: {
  value: string
  onChange: (value: string) => void
  debounce?: number
} & Omit<JSX.IntrinsicElements['input'], 'onChange'>) {
  const [value, setValue] = useState(initialValue)

  useEffect(() => {
    setValue(initialValue)
  }, [initialValue])

  useEffect(() => {
    const timeout = setTimeout(() => {
      onChange(value)
    }, debounce)

    return () => clearTimeout(timeout)
  }, [value])

  return (
    <input {...props} value={value} onChange={e => setValue(e.target.value)} />
  )
}

declare module '@tanstack/table-core' {
  //Extra column metadata for extending the built-in filter functionality of react-table
  interface ColumnMeta<TData extends RowData, TValue> {
    //Specifies the type of the column data. By default, columns will be treated as strings
    columnType?: string
    //Specifies the Select options if using a dropdown filter (i.e. for booleans)
    filterOptions?: object[]
  }
}

/**
 * returns a Filter to handle text fields
 * adapted from https://tanstack.com/table/v8/docs/examples/react/filters
 * */
function Filter<A>({
  column
}: {
  column: Column<A>
}) {
  return column.columnDef.meta?.columnType === 'boolean' ? (
    SelectFilter({ column })
  ) : (
    SearchFilter({ column })
  )
}

/**
 * returns a SelectFilter to handle fields filtered by dropdown
 * */
function SelectFilter<A>({
  column
}: {
  column: Column<A>
}) {
  const [selectedValue, setSelectedValue] = useState({})

  return <div>
    <Select
      options={column.columnDef.meta?.filterOptions || []}
      styles={{
        control: baseStyles => ({
          ...baseStyles,
          width: 200,
          fontWeight: 'normal'
        }),
        menu: baseStyles => ({
          ...baseStyles,
          fontWeight: 'normal'
        })
      }}
      value={selectedValue}
      // @ts-ignore
      onChange={(newValue: { value: boolean, label: string }) => {
        //setFilterValue on Column cannot natively handle dropdown options,
        //so we need to manage the filter value used by the column separately
        //from the selected value used by the Select component
        setSelectedValue(newValue)
        column.setFilterValue(newValue.value)
      }}
    />
    <div className="h-1" />
  </div>
}

function SearchFilter<A>({
  column
}: {
  column: Column<A>
}) {
  return <div>
    <DebouncedInput
      type="text"
      value={(column.getFilterValue() ?? '') as string}
      onChange={value => column.setFilterValue(value)}
      placeholder={`Search...`}
      list={`${column.id} list`}
    />
    <div className="h-1" />
  </div>
}

/**
 * returns a table header with optional sorting and filtering
 * adapted from https://tanstack.com/table/v8/docs/examples/react/sorting
 * */
export function tableHeader<A, B>(
  header: Header<A, B>,
  options: { sortable: boolean, filterable: boolean }
) {
  const sortDirection = options.sortable && header.column.getIsSorted()
  const ariaSort = options.sortable ?
    sortDirection ? (sortDirection === 'desc' ? 'descending' : 'ascending') : 'none' : undefined

  return <th key={header.id}
    aria-sort={ariaSort}>
    { options.sortable ? sortableTableHeader(header) : null }
    { options.filterable ? filterableTableHeader(header) : null }
  </th>
}

/**
 * returns a clickable header column with up/down icons indicating sort direction
 * adapted from https://tanstack.com/table/v8/docs/examples/react/sorting
 * */
export function sortableTableHeader<A, B>(header: Header<A, B>) {
  return <div className={header.column.getCanSort() ? 'cursor-pointer select-none' : ''}
    onClick={header.column.getToggleSortingHandler()} role="button">
    {flexRender(header.column.columnDef.header, header.getContext())}
    {{
      asc: <FontAwesomeIcon icon={faCaretUp}/>,
      desc: <FontAwesomeIcon icon={faCaretDown}/>
    }[header.column.getIsSorted() as string] ?? null}
  </div>
}

/**
 * returns a filterable and sortable header column
 * adapted from https://tanstack.com/table/v8/docs/examples/react/filters
 * */
export function filterableTableHeader<A, B>(header: Header<A, B>) {
  return header.column.getCanFilter() ? (
    <div>
      <Filter column={header.column}/>
    </div>
  ) : null
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

/** helper function for simple table layouts */
export function basicTableLayout<T>(table: Table<T>) {
  return <table className="table table-striped">
    <thead>
      <tr>
        {table.getFlatHeaders().map(header => sortableTableHeader(header))}
      </tr>
    </thead>
    <tbody>
      {table.getRowModel().rows.map(row => {
        return (
          <tr key={row.id}>
            {row.getVisibleCells().map(cell => {
              return (
                <td key={cell.id}>
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </td>
              )
            })}
          </tr>
        )
      })}
    </tbody>
  </table>
}
