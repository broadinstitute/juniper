import { arrayMove, SortableContext, useSortable, verticalListSortingStrategy } from '@dnd-kit/sortable'
import React, { CSSProperties, Dispatch, SetStateAction } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faGripVertical } from '@fortawesome/free-solid-svg-icons'
import { flexRender, Row, Table } from '@tanstack/react-table'
import { CSS } from '@dnd-kit/utilities'
import {
  closestCenter,
  DndContext,
  DragEndEvent,
  KeyboardSensor,
  MouseSensor,
  TouchSensor,
  UniqueIdentifier,
  useSensor,
  useSensors
} from '@dnd-kit/core'
import { restrictToVerticalAxis } from '@dnd-kit/modifiers'
import { BasicTableConfig, defaultBasicTableConfig, tableHeader } from './tableUtils'

export const RowDragHandleCell = ({ rowId }: { rowId: string }) => {
  const { attributes, listeners, isDragging } = useSortable({
    id: rowId
  })
  const style: CSSProperties = {
    cursor: isDragging ? 'grabbing' : 'grab'
  }

  return (
    // Alternatively, you could set these attributes on the rows themselves
    <button className="ms-2 p-0 btn btn-secondary" style={style} {...attributes} {...listeners}>
      <FontAwesomeIcon icon={faGripVertical}/>
    </button>
  )
}

// Row Component
const DraggableRow = function<T>({ row, idFunc }: { row: Row<T>, idFunc: (row: Row<T>) => string | number }) {
  const { transform, transition, setNodeRef, isDragging } = useSortable({
    id: idFunc(row)
  })

  const style: CSSProperties = {
    transform: CSS.Transform.toString(transform), //let dnd-kit do its thing
    transition,
    opacity: isDragging ? 0.8 : 1,
    zIndex: isDragging ? 1 : 0,
    position: 'relative'
  }
  return (
    // connect row ref to dnd-kit, apply important styles
    <tr ref={setNodeRef} style={style}>
      {row.getVisibleCells().map(cell => (
        <td key={cell.id} style={{ width: cell.column.getSize(), verticalAlign: 'middle' }}>
          {flexRender(cell.column.columnDef.cell, cell.getContext())}
        </td>
      ))}
    </tr>
  )
}


/** helper function for simple table layouts */
export function useDraggableTableLayout<T>(table: Table<T>, config: BasicTableConfig<T> = defaultBasicTableConfig,
  idList: UniqueIdentifier[], setIdList: Dispatch<SetStateAction<UniqueIdentifier[]>>,
  idFunc: (row: Row<T>) => UniqueIdentifier) {
  const { filterable } = { ...defaultBasicTableConfig, ...config }
  const sensors = useSensors(
    useSensor(MouseSensor, {}),
    useSensor(TouchSensor, {}),
    useSensor(KeyboardSensor, {})
  )

  // reorder rows after drag & drop
  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event
    if (active && over && active.id !== over.id) {
      setIdList(data => {
        const oldIndex = idList.indexOf(active.id)
        const newIndex = idList.indexOf(over.id)
        return arrayMove(data, oldIndex, newIndex) //this is just a splice util
      })
    }
  }

  return <DndContext onDragEnd={handleDragEnd} collisionDetection={closestCenter}
    modifiers={[restrictToVerticalAxis]}
    sensors={sensors}>
    <table className={config.tableClass ? config.tableClass : 'table'}>
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
        <SortableContext
          items={idList}
          strategy={verticalListSortingStrategy}
        >
          {table.getRowModel().rows.map(row => (
            <DraggableRow key={row.id} row={row} idFunc={idFunc} />
          ))}
        </SortableContext>
      </tbody>
    </table>
  </DndContext>
}
