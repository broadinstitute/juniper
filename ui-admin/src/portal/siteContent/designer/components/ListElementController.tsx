import React from 'react'
import { IconButton } from '../../../../components/forms/Button'
import { faChevronDown, faChevronUp, faTimes } from '@fortawesome/free-solid-svg-icons'

type ListControllerProps<T> = {
    items: T[],
    updateItems: (items: T[]) => void,
    index: number,
}

/**
 *
 */
export const ListElementController = <T, >({ items, updateItems, index }: ListControllerProps<T>) => {
  return (
    <div className="d-flex justify-content-end">
      <IconButton icon={faChevronUp} aria-label={'Move Up'} disabled={index < 1}
        onClick={() => {
          const newItems = [...items]
          const temp = newItems[index]
          newItems[index] = newItems[index - 1]
          newItems[index - 1] = temp
          updateItems(newItems)
        }}/>
      <IconButton icon={faChevronDown} aria-label={'Move Down'} disabled={index > items.length - 2}
        onClick={() => {
          const newItems = [...items]
          const temp = newItems[index]
          newItems[index] = newItems[index + 1]
          newItems[index + 1] = temp
          updateItems(newItems)
        }}/>
      <IconButton icon={faTimes} className={'text-danger'} aria-label={'Delete'} onClick={() => {
        const newItems = [...items]
        newItems.splice(index, 1)
        updateItems(newItems)
      }}/>
    </div>
  )
}
