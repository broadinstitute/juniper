import React, {
  Fragment,
  HTMLProps,
  useEffect,
  useState
} from 'react'
import {
  CellContext,
  Column,
  flexRender,
  Header,
  PaginationState,
  Row,
  RowData,
  Table
} from '@tanstack/react-table'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCaretDown,
  faCaretUp,
  faCheck,
  faColumns,
  faDownload
} from '@fortawesome/free-solid-svg-icons'
import Select from 'react-select'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'
import {
  escapeCsvValue,
  saveBlobAsDownload
} from './downloadUtils'
import { instantToDefaultString } from '@juniper/ui-core'
import {
  isEmpty,
  isNil
} from 'lodash'
import { useSearchParams } from 'react-router-dom'
import { TextInput } from 'components/forms/TextInput'
import classNames from 'classnames'


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
    <TextInput
      placeholder={props.placeholder}
      value={value}
      onChange={v => {
        setValue(v)
      }}
    />
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
 * controlled by 'filterOptions'  field in column meta
 * */
function SelectFilter<A>({
  column
}: {
  column: Column<A>
}) {
  const options = column.columnDef.meta?.filterOptions || []
  const initialValue = options.find(opt => opt.value === column.getFilterValue())
  const [selectedValue, setSelectedValue] =
      useState<{ value: boolean | string, label: string } | undefined>(initialValue)

  return <div>
    <Select
      options={options}
      isClearable={true}
      styles={{
        control: baseStyles => ({
          ...baseStyles,
          fontWeight: 'normal'
        }),
        menu: baseStyles => ({
          ...baseStyles,
          fontWeight: 'normal'
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
      placeholder={`Filter...`}
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
  options: { sortable?: boolean, filterable?: boolean, useSize?: boolean }
) {
  const sortDirection = options.sortable && header.column.getIsSorted()
  const ariaSort = options.sortable ?
    sortDirection ? (sortDirection === 'desc' ? 'descending' : 'ascending') : 'none' : undefined

  return <th key={header.id}
    aria-sort={ariaSort}
    className={classNames(
      options.useSize ? `col-${header.getSize()}` : undefined
    )}
  >
    <div>
      { options.sortable ? sortableTableHeader(header) : null }
      { options.filterable ? filterableTableHeader(header) : null }
    </div>

  </th>
}

/**
 * returns a clickable header column with up/down icons indicating sort direction
 * adapted from https://tanstack.com/table/v8/docs/examples/react/sorting
 * */
export function sortableTableHeader<A, B>(header: Header<A, B>) {
  return <div className={header.column.getCanSort() ? 'cursor-pointer select-none flex-grow-1' : 'flex-grow-1'}
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
  ) : <div style={{ height: '2em' }}> &nbsp;</div>
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
  return <div className="ms-auto">
    <Button onClick={() => setShow(!show)}
      variant="light" className="border m-1"
      tooltip={'Show or hide columns'}>
      <FontAwesomeIcon icon={faColumns} className="fa-lg"/> Columns
    </Button>
    { show && <Modal show={show} onHide={() => setShow(false)}>
      <Modal.Header closeButton>
        <Modal.Title>
          Toggle column visibility
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <div className="border-b border-black">
          <label>
            <input
              type='checkbox'
              checked={table.getIsAllColumnsVisible()}
              onChange={table.getToggleAllColumnsVisibilityHandler()}
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
                  type='checkbox'
                  checked={column.getIsVisible()}
                  onChange={column.getToggleVisibilityHandler()}
                />
                <span className="ps-2">{ column.columnDef.header as string ?? column.columnDef.id }</span>
              </label>
            </div>
          )
        })}
      </Modal.Body>
      <Modal.Footer>
        <Button variant="primary" onClick={() => setShow(false)}>Ok</Button>
      </Modal.Footer>
    </Modal> }
  </div>
}

/**
 * Returns a count of the total number of rows in the table, and the number of rows
 * visible if a filter is applied
 */
export function RowVisibilityCount<T>({ table }: {table: Table<T>}) {
  const numSelected = Object.keys(table.getState().rowSelection).length
  const numPrefilteredRows = table.getPreFilteredRowModel().rows.length
  const numFilteredRows = table.getFilteredRowModel().rows.length

  if (numPrefilteredRows === numFilteredRows) {
    return <span>{numSelected} of {numPrefilteredRows} selected</span>
  } else {
    return <span>{numSelected} of {numPrefilteredRows} selected ({numFilteredRows} shown)</span>
  }
}

/**
 * Renders a message if the table is empty
 */
export const renderEmptyMessage = (arr: unknown[], content: React.ReactNode) => {
  if (arr.length > 0) { return null }
  return <span className="d-flex justify-content-center text-muted fst-italic">{content}</span>
}

/**
 * Converts a cell value to an escaped string for csv export
 */
function cellToCsvString(cellType: string, cellValue: unknown): string {
  switch (cellType) {
    case 'string':
      return cellValue ? escapeCsvValue(cellValue as string) : ''
    case 'instant':
      return escapeCsvValue(instantToDefaultString(cellValue as number))
    case 'boolean':
      return cellValue as boolean ? 'true' : 'false'
    default:
      return cellValue ? escapeCsvValue(cellValue as string) : ''
  }
}

/**
 * Adds a control to download the table data as a csv file
 */
export function DownloadControl<T>({ table, fileName, excludedColumns = ['select'], buttonClass='border m-1' }:{
  table: Table<T>, fileName: string, excludedColumns?: string[], buttonClass: string}
) {
  const [show, setShow] = useState(false)

  const download = () => {
    const headers = table.getFlatHeaders().filter(header => !excludedColumns.includes(header.id)).map(header => {
      return header.id
    }).join(',')

    const rows = table.getFilteredRowModel().rows.map(row => {
      const visibleCells = row.getVisibleCells().filter(cell => !excludedColumns.includes(cell.column.id))

      return visibleCells.map(cell => {
        const cellType = cell.column.columnDef.meta?.columnType || 'string'
        return cellToCsvString(cellType, cell.getValue())
      }).join(',')
    }).join('\n')

    const blob = new Blob([headers, '\n', rows], { type: 'text/plain' })
    saveBlobAsDownload(blob, `${fileName}.csv`)
  }

  const disableDownload = isEmpty(table.getFilteredRowModel().rows)

  return <div className="ms-auto">
    <Button onClick={() => setShow(!show)}
      variant="light" className={buttonClass} disabled={disableDownload}
      tooltip={!disableDownload ? 'Download table' : 'At least one row must be visible in order to download'}>
      <FontAwesomeIcon icon={faDownload} className="fa-lg"/> Download
    </Button>
    { show && <Modal show={show} onHide={() => setShow(false)}>
      <Modal.Header closeButton>
        <Modal.Title>
                Download
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <div className="border-b border-black">
                Download <strong>{table.getFilteredRowModel().rows.length}</strong> rows
                to <code>{fileName}.csv</code>. The current data filters and shown columns will be applied.
        </div>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="primary" onClick={() => {
          download()
          setShow(false)
        }}>Download</Button>
        <button className="btn btn-secondary" onClick={() => setShow(false)}>Cancel</button>
      </Modal.Footer>
    </Modal> }
  </div>
}

/**
 * Configuration for basicTableLayout. This can have configuration properties that affect different parts of the table,
 * such as headers, rows, etc. All configuration properties are optional.
 */
export type BasicTableConfig<T> = {
  filterable?: boolean,
  tableClass?: string,
  tdClass?: string,
  trClass?: string,
  customRowHeader?: (row: Row<T>) => React.ReactNode,
  customRowFooter?: (row: Row<T>) => React.ReactNode,
  useSize?: boolean
}

/** Default configuration if no `BasicTableConfig` is provided or any of its attributes are not specified. */
export const defaultBasicTableConfig = {
  filterable: false
}

/** helper function for simple table layouts */
export function basicTableLayout<T>(table: Table<T>, config: BasicTableConfig<T> = {}) {
  const { filterable } = { ...defaultBasicTableConfig, ...config }
  return <table className={config.tableClass ? config.tableClass : 'table'}>
    <thead>
      <tr>
        {table
          .getFlatHeaders()
          .map(header => tableHeader(header, {
            sortable: true, filterable, useSize: config?.useSize || false
          }))}
      </tr>
    </thead>
    <tbody>
      {table.getRowModel().rows.map(row => {
        return (
          <Fragment key={row.id}>
            {!isNil(config.customRowHeader) && config.customRowHeader(row)}
            <tr className={classNames(config.trClass)}>
              {row.getVisibleCells().map(cell => {
                return (
                  <td key={cell.id} className={config.tdClass ? config.tdClass : 'align-middle'}>
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </td>
                )
              })}
            </tr>
            {!isNil(config.customRowFooter) && config.customRowFooter(row)}
          </Fragment>
        )
      })}
    </tbody>
  </table>
}

/** renders a boolean value as a checkmark (true)  or a blank (false) */
export const checkboxColumnCell = <R, T>(props: CellContext<R, T>) =>
  props.getValue() ? <FontAwesomeIcon icon={faCheck}/> : ''

/**
 * hook for reading pagination params from the URL.  This should be used in conjunction with <TablePagination> for
 * url-persistent params, also backed by localstorage for storing preferred pageSie
 * @param tableIdentifier an identifier to distinguish table page sizes in local storage, the containing component name
 * is a good default for this
 */
export const useRoutableTablePaging = (tableIdentifier: string) => {
  const [searchParams] = useSearchParams()
  const preferredNumRowsKey = `${tableIdentifier}.preferredNumRows`
  const paginationState: PaginationState = {
    pageIndex: parseInt(searchParams.get('pageIndex') || '0'),
    pageSize: parseInt(searchParams.get('pageSize') || localStorage.getItem(preferredNumRowsKey) || '10')
  }
  return { paginationState, preferredNumRowsKey }
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
    filterInitialValue?: string | boolean
  }
}
