import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronRight } from '@fortawesome/free-solid-svg-icons'
import { get } from 'lodash'
import React, { Dispatch, SetStateAction, useRef, useState } from 'react'

export type TreeItemT<T> = {
  /** User facing label. */
  label?: React.ReactNode

  /** Data associated with this tree item. */
  data: T

  /** Child tree items. */
  children?: TreeItemT<T>[]
}

type TreeItemProps<T> = {
  /** ID of the tree's current active descendant element. */
  activeDescendant: string

  /** ID for this tree item element. */
  id: string

  /** Should the given tree item be rendered as selected? */
  isItemSelected: (item: TreeItemT<T>) => boolean

  /** This tree item's data model. */
  item: TreeItemT<T>

  /** Level within the tree (root is level 0, its children are level 1, etc). */
  level: number

  /** Path to this item from the tree root in object notation. For example, 'children[0].children[1]'. */
  path: string

  /** Update the tree's active descendant. */
  setActiveDescendant: Dispatch<SetStateAction<string>>

  /** Callback when a tree item is clicked. */
  onClickItem: (item: TreeItemT<T>) => void
}

/** Render a tree item. */
export const TreeItem = <T, >(props: TreeItemProps<T>) => {
  const {
    activeDescendant,
    id,
    isItemSelected,
    item,
    level,
    path,
    setActiveDescendant,
    onClickItem
  } = props

  const isExpandable = item.children !== undefined
  const [isExpanded, setIsExpanded] = useState(isExpandable)

  const isSelected = isItemSelected(item)

  return (
    <li
      aria-expanded={isExpandable ? isExpanded : undefined}
      // Label with the link to read only the item label instead of both the item label and the children list label.
      aria-labelledby={`${id}-link`}
      // aria-level starts at 1, level starts at 0.
      aria-level={level + 1}
      // aria-selected: false results in every tree item being read as "selected".
      aria-selected={isSelected ? true : undefined}
      // Data attribute allows getting the item from the activedescendant element ID.
      data-path={path}
      id={id}
      role="treeitem"
      style={{
        display: 'flex',
        flexDirection: 'column',
        position: 'relative'
      }}
    >
      {/* Wrapper span provides a larger click target than just the icon. */}
      {item.label && <><span
        aria-hidden
        style={{
          position: 'absolute',
          top: '1px',
          left: `${level - 1}rem`,
          display: 'flex',
          justifyContent: 'flex-end',
          alignItems: 'center',
          width: '2rem',
          height: '2rem'
        }}
        onClick={() => {
          setIsExpanded(!isExpanded)
          // If the active descendant is a child of this item and thus will be removed,
          // set the active descendant to this item.
          if (isExpanded && activeDescendant.startsWith(`${id}-`)) {
            setActiveDescendant(id)
          }
        }}
      >
        {isExpandable && <FontAwesomeIcon icon={isExpanded ? faChevronDown : faChevronRight} />}
      </span>
      <a
        id={`${id}-link`}
        role="presentation"
        tabIndex={-1}
        style={{
          display: 'inline-block',
          overflow: 'hidden',
          maxWidth: '100%',
          padding: `0.25rem 0.5rem 0.25rem ${level + 1.25}rem`,
          borderColor: id === activeDescendant ? 'var(--bs-primary)' : 'transparent',
          borderStyle: 'solid',
          borderWidth: '1px 0',
          cursor: 'default',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          ...(isSelected && {
            background: 'var(--bs-primary)',
            color: 'white'
          })
        }}
        onClick={() => {
          setIsExpanded(true)
          onClickItem(item)
        }}
      >
        {item.label}
      </a></>}
      {isExpanded && (
        <ul
          aria-label={`${item.label} children`}
          role="group"
          style={{
            padding: 0,
            margin: 0,
            listStyleType: 'none'
          }}
        >
          {item.children?.map((childItem, index) => {
            return (
              <TreeItem<T>
                key={index}
                activeDescendant={activeDescendant}
                id={`${id}-${index}`}
                isItemSelected={isItemSelected}
                item={childItem}
                level={level + 1}
                path={`${path}${path === '' ? '' : '.'}children[${index}]`}
                setActiveDescendant={setActiveDescendant}
                onClickItem={onClickItem}
              />
            )
          })}
        </ul>
      )}
    </li>
  )
}

type TreeProps<T> = {
  /** ID for the tree element. */
  id: string

  /** Should the given tree item be rendered as selected? */
  isItemSelected: (item: TreeItemT<T>) => boolean

  /** ARIA label for the tree element. */
  label: string

  /** Data model for the tree. */
  rootItem: TreeItemT<T>

  /** Callback when a tree item is clicked. */
  onClickItem: (item: TreeItemT<T>) => void
}

/** Render a tree. */
export const Tree = <T, >(props: TreeProps<T>) => {
  const { id, isItemSelected, label, rootItem, onClickItem } = props

  const treeElementRef = useRef<HTMLUListElement | null>(null)

  const [activeDescendant, setActiveDescendant] = useState(`${id}-node-0`)

  return (
    <ul
      ref={treeElementRef}
      // aria-activedescendant tells which tree item is "focused", while actual focus stays on the tree itself.
      aria-activedescendant={activeDescendant}
      aria-label={label}
      role="tree"
      tabIndex={0}
      style={{
        padding: 0,
        border: '2px solid transparent',
        margin: 0,
        listStyleType: 'none'
      }}
      onKeyDown={e => {
        // If the key isn't relevant to tree navigation, do nothing.
        if (!(e.key === 'Enter' || e.key.startsWith('Arrow'))) {
          return
        }

        e.preventDefault()
        e.stopPropagation()

        /* eslint-disable @typescript-eslint/no-non-null-assertion */
        const currentTreeItem = document.getElementById(activeDescendant)

        if (!currentTreeItem) {
          // If the active descendant isn't found (for example, if it was in a group that has been collapsed),
          // then reset the active descendant to the first item in the tree.
          setActiveDescendant('node-0')
        } else if (e.key === 'Enter') {
          // Otherwise, call click callback for the current tree item.
          const currentTreeItemPath = currentTreeItem.dataset.path!
          const treeItem: TreeItemT<T> = currentTreeItemPath === ''
            ? rootItem
            : get(rootItem, currentTreeItemPath)
          onClickItem(treeItem)
        } else if (e.key === 'ArrowLeft') {
          const isExpanded = currentTreeItem.getAttribute('aria-expanded') === 'true'
          if (isExpanded) {
            // Close the tree item if it is open.
            (currentTreeItem.firstElementChild as HTMLElement)!.click()
          } else {
            // If the tree item is closed, move to the parent tree item (if there is one).
            const parentGroup = currentTreeItem.parentElement!
            if (parentGroup.getAttribute('role') === 'group') {
              // If the parent group is a group within the tree, move up the tree.
              // Else if the parent group is the tree itself, do nothing.
              const parentTreeItem = parentGroup.parentElement!
              setActiveDescendant(parentTreeItem.id)
            }
          }
        } else if (e.key === 'ArrowRight') {
          const expanded = currentTreeItem.getAttribute('aria-expanded')
          if (expanded === 'false') {
            // Open the tree item if it is currently closed.
            (currentTreeItem.firstElementChild as HTMLElement)!.click()
          } else if (expanded === 'true') {
            // Move to the first child node.
            // If the current tree item has no children, then do nothing.
            const firstChildTreeItem = currentTreeItem.lastElementChild!.firstElementChild
            if (firstChildTreeItem) {
              setActiveDescendant(firstChildTreeItem.id)
            }
          }
        } else if (e.key === 'ArrowDown') {
          // Move to the next tree item without opening/closing any tree items.
          const allTreeItemIds = Array.from(treeElementRef.current!.querySelectorAll('[role="treeitem"]')).map(
            el => el.id
          )
          const indexOfCurrentTreeItem = allTreeItemIds.findIndex(id => id === activeDescendant)
          if (indexOfCurrentTreeItem < allTreeItemIds.length - 1) {
            setActiveDescendant(allTreeItemIds[indexOfCurrentTreeItem + 1])
          }
        } else if (e.key === 'ArrowUp') {
          // Move to the previous tree item without opening/closing any tree items.
          const allTreeItemIds = Array.from(treeElementRef.current!.querySelectorAll('[role="treeitem"]')).map(
            el => el.id
          )
          const indexOfCurrentTreeItem = allTreeItemIds.findIndex(id => id === activeDescendant)
          if (indexOfCurrentTreeItem > 0) {
            setActiveDescendant(allTreeItemIds[indexOfCurrentTreeItem - 1])
          }
        }
      }}
    >
      <TreeItem<T>
        activeDescendant={activeDescendant}
        id={`${id}-node-0`}
        isItemSelected={isItemSelected}
        item={rootItem}
        level={0}
        path=""
        setActiveDescendant={setActiveDescendant}
        onClickItem={onClickItem}
      />
    </ul>
  )
}
