import React from 'react'
import { useDraggable, useDroppable } from '@dnd-kit/core'
import { CSS } from '@dnd-kit/utilities'

/**
 *
 */
export function Draggable(props: {id: string; item: object, children: React.ReactNode}) {
  const { attributes, listeners, setNodeRef, transform, isDragging } =
    useDraggable({
      id: props.id,
      data: props.item
    })
  const style = {
    // Outputs `translate3d(x, y, 0)`
    transform: CSS.Translate.toString(transform),
    cursor: isDragging ? 'grabbing' : undefined
  }

  return <div ref={setNodeRef} style={style} {...listeners} {...attributes} className="dnd-draggable">
    {props.children}
  </div>
}

/** don't allow dragging items onto themselves, or putting pages into elements or making elements pages */
export const isDropAllowed = (active: { id: unknown } | null, over: { id: unknown } | null) => {
  if (!active || !over) {
    return false
  }
  return active.id !== over.id
}


/**
 *
 */
export function Droppable(props: {id: string, item: object}) {
  const { active, over, isOver, setNodeRef } = useDroppable({
    id: props.id,
    data: props.item
  })
  const allowDrop = isOver && isDropAllowed((active), (over))
  const style = {
    minHeight: allowDrop ? '1px' : '1px',
    minWidth: '100%',
    border: allowDrop ? '1px solid #999' : 'none'
  }

  return (
    <div ref={setNodeRef} style={style}></div>
  )
}
