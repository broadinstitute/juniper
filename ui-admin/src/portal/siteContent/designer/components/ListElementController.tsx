import React from 'react'
import { IconButton } from 'components/forms/Button'
import { faChevronDown, faChevronUp, faTimes } from '@fortawesome/free-solid-svg-icons'

type ListControllerProps<T> = {
    items: T[],
    updateItems: (items: T[]) => void,
    index: number,
}

/**
 * Controller for re-ordering or deleting elements in a list
 */
export const ListElementController = <T, >({ items, updateItems, index }: ListControllerProps<T>) => {
  const moveItem = (direction: 'up' | 'down') => {
    if (index === 0 && direction === 'up') { return }
    const newItems = [...items]
    const itemToMove = newItems[index]
    newItems.splice(index, 1)
    if (direction === 'up') {
      newItems.splice(index - 1, 0, itemToMove)
    } else {
      newItems.splice(index + 1, 0, itemToMove)
    }
    updateItems(newItems)
  }

  return (
    <div className="d-flex justify-content-end">
      <IconButton icon={faChevronUp} aria-label={'Move Up'} disabled={index < 1}
        onClick={() => moveItem('up')}/>
      <IconButton icon={faChevronDown} aria-label={'Move Down'} disabled={index > items.length - 2}
        onClick={() => moveItem('down')}/>
      <IconButton icon={faTimes} className={'text-danger'} aria-label={'Delete'} onClick={() => {
        const newItems = [...items]
        newItems.splice(index, 1)
        updateItems(newItems)
      }}/>
    </div>
  )
}
