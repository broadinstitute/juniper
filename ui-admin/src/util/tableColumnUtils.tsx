import { instantToDefaultString } from '@juniper/ui-core'
import { ColumnDef } from '@tanstack/react-table'

/**
 * Creates a column definition for a "created at" column.
 */
export function createCreatedAtColumn<T>(): ColumnDef<T> {
  return {
    header: 'Created At',
    accessorKey: 'createdAt',
    enableColumnFilter: false,
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as unknown as number)
  }
}
