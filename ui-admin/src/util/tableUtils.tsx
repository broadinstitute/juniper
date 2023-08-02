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
  debounce = 500,
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
    <input {...props} value={value} style={{ height: 32 }} onChange={e => setValue(e.target.value)} />
  )
}

export enum FilterType {
  Select,
  Search
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
  switch (column.columnDef.meta?.filterType) {
    case FilterType.Select:
      return SelectFilter({ column })
    case FilterType.Search:
      return SearchFilter({ column })
    default: {
      return column.columnDef.meta?.columnType === 'boolean' ? (
        SelectFilter({ column })
      ) : (
        //Defaults to the text search filter
        SearchFilter({ column })
      )
    }
  }
}

/**
 * returns a SelectFilter to handle fields filtered by dropdown
 * */
function SelectFilter<A>({
  column
}: {
  column: Column<A>
}) {
  const [selectedValue, setSelectedValue] = useState<{ value: boolean | string, label: string }>()

  return <div>
    <Select
      options={column.columnDef.meta?.filterOptions || []}
      isClearable={true}
      styles={{
        control: baseStyles => ({
          ...baseStyles,
          width: 200,
          height: 32,
          minHeight: 32,
          fontWeight: 'normal'
        }),
        menu: baseStyles => ({
          ...baseStyles,
          fontWeight: 'normal'
        }),
        valueContainer: baseStyles => ({
          ...baseStyles,
          padding: '0 0.25rem'
        })
      }}
      value={selectedValue}
      // @ts-ignore
      onChange={(newValue: { value: boolean | string, label: string }) => {
        //setFilterValue on Column cannot natively handle dropdown options,
        //so we need to manage the filter value used by the column separately
        //from the selected value used by the Select component
        setSelectedValue(newValue)
        newValue !== null ?
          column.setFilterValue(newValue.value) :
          column.setFilterValue(undefined)
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

/**
 * Configuration for basicTableLayout. This can have configuration properties that affect different parts of the table,
 * such as headers, rows, etc. All configuration properties are optional.
 */
export type BasicTableConfig = {
  filterable?: boolean
}

/** Default configuration if no `BasicTableConfig` is provided or any of its attributes are not specified. */
const defaultBasicTableConfig = {
  filterable: false
}

/** helper function for simple table layouts */
export function basicTableLayout<T>(table: Table<T>, config: BasicTableConfig = {}) {
  const { filterable } = { ...defaultBasicTableConfig, ...config }
  return <table className="table table-striped">
    <thead>
      <tr>
        {table.getFlatHeaders().map(header => tableHeader(header, { sortable: true, filterable }))}
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

declare module '@tanstack/table-core' {
  //Extra column metadata for extending the built-in filter functionality of react-table
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  interface ColumnMeta<TData extends RowData, TValue> {
    //Specifies the type of the column data. By default, columns will be treated as strings
    columnType?: string
    filterType?: FilterType
    //Specifies the Select options if using a dropdown filter (i.e. for booleans)
    filterOptions?: { value: boolean | string, label: string }[]
  }
}
