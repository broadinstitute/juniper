import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronRight } from '@fortawesome/free-solid-svg-icons'
import React, { Dispatch, SetStateAction, useRef, useState } from 'react'

import { FormContent, FormElement } from '@juniper/ui-core'

type TableOfContentsEntryType =
  | 'form'
  | 'pages'
  | 'questionTemplates'
  | 'page'
  | 'panel'
  | 'questionOrHtml'

type TableOfContentsEntry = {
  name: string
  type: TableOfContentsEntryType
  children: TableOfContentsEntry[]
}

const getTableOfContentsHelper = (formElement: FormElement): TableOfContentsEntry => {
  if ('type' in formElement && formElement.type === 'panel') {
    return {
      name: `Panel (${formElement.elements.length} elements)`,
      type: 'panel',
      children: formElement.elements.map(getTableOfContentsHelper)
    }
  } else {
    return {
      name: formElement.name,
      type: 'questionOrHtml',
      children: []
    }
  }
}

const getTableOfContents = (formContent: FormContent): TableOfContentsEntry => {
  return {
    type: 'form',
    name: 'Form',
    children: [
      {
        name: 'Pages',
        type: 'pages',
        children: (formContent.pages || []).map((page, pageIndex) => ({
          name: `Page ${pageIndex + 1}`,
          type: 'page',
          children: page.elements.map(getTableOfContentsHelper)
        }))
      },
      {
        name: 'Question templates',
        type: 'questionTemplates',
        children: (formContent.questionTemplates || []).map(question => ({
          name: question.name,
          type: 'questionOrHtml',
          children: []
        }))
      }
    ]
  }
}

const isEntryTypeExpandable = (entryType: string): boolean => {
  return entryType !== 'questionOrHtml'
}

const isEntryTypeSelectable = (entryType: string): boolean => {
  return entryType === 'questionOrHtml'
}

type EntryContentsProps = {
  activeDescendant: string
  entry: TableOfContentsEntry
  level: number
  parentId: string
  selectedEntryName: string | undefined
  setActiveDescendant: Dispatch<SetStateAction<string>>
  onSelectEntry: (name: string) => void
}

export const EntryContents = (props: EntryContentsProps) => {
  const {
    activeDescendant,
    entry,
    level,
    parentId,
    selectedEntryName,
    setActiveDescendant,
    onSelectEntry
  } = props

  return (
    <ul
      aria-label={`${entry.name} children`}
      role="group"
      style={{
        padding: 0,
        margin: 0,
        listStyleType: 'none'
      }}
    >
      {entry.children.map((childEntry, index) => {
        return (
          <Entry
            key={index}
            activeDescendant={activeDescendant}
            entry={childEntry}
            id={`${parentId}-${index}`}
            level={level + 1}
            selectedEntryName={selectedEntryName}
            setActiveDescendant={setActiveDescendant}
            onSelectEntry={onSelectEntry}
          />
        )
      })}
    </ul>
  )
}

type EntryProps = {
  activeDescendant: string
  entry: TableOfContentsEntry
  id: string
  level: number
  selectedEntryName: string | undefined
  setActiveDescendant: Dispatch<SetStateAction<string>>
  onSelectEntry: (name: string) => void
}

export const Entry = (props: EntryProps) => {
  const {
    activeDescendant,
    entry,
    id,
    level,
    selectedEntryName,
    setActiveDescendant,
    onSelectEntry
  } = props

  const hasChildren = entry.children.length > 0
  const isExpandable = isEntryTypeExpandable(entry.type) && hasChildren
  const [isExpanded, setIsExpanded] = useState(true)

  const isSelected = selectedEntryName && selectedEntryName === entry.name

  return (
    <li
      aria-expanded={isExpandable ? isExpanded : undefined}
      // Label with the link to read only the entry name instead of both the entry name and the children list label.
      aria-labelledby={`${id}-link`}
      // aria-level starts at 1, level starts at 0.
      aria-level={level + 1}
      // aria-selected: false results in every tree item being read as "selected".
      aria-selected={isSelected ? true : undefined}
      // Data attribute allows getting the entry from the activedescendant element ID.
      data-name={entry.name}
      data-type={entry.type}
      id={id}
      role="treeitem"
      style={{
        display: 'flex',
        flexDirection: 'column',
        position: 'relative'
      }}
    >
      {/* Wrapper span provides a larger click target than just the icon. */}
      <span
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
          // If the active descendant is a child of this entry and thus will be removed,
          // set the active descendant to this entry.
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
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          ...(isSelected && {
            background: 'var(--bs-primary)',
            color: 'white'
          })
        }}
        onClick={() => {
          setIsExpanded(true)
          if (isEntryTypeSelectable(entry.type)) {
            onSelectEntry(entry.name)
          }
        }}
      >
        {entry.name}
      </a>
      {isExpanded && (
        <EntryContents
          activeDescendant={activeDescendant}
          entry={entry}
          level={level}
          parentId={id}
          selectedEntryName={selectedEntryName}
          setActiveDescendant={setActiveDescendant}
          onSelectEntry={onSelectEntry}
        />
      )}
    </li>
  )
}

type FormTableOfContentsProps = {
  formContent: FormContent
  selectedElementName: string | undefined
  onSelectElement: (name: string) => void
}

export const FormTableOfContents = (props: FormTableOfContentsProps) => {
  const { formContent, selectedElementName, onSelectElement } = props

  const treeElementRef = useRef<HTMLUListElement | null>(null)

  const [activeDescendant, setActiveDescendant] = useState('node-0')

  return (
    <ul
      ref={treeElementRef}
      // aria-activedescendant tells which tree item is "focused", while actual focus stays on the tree itself.
      aria-activedescendant={activeDescendant}
      aria-label="Table of contents"
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
          // Otherwise, select the question for the current tree item if it is a question.
          const name = currentTreeItem.dataset.name!
          const type = currentTreeItem.dataset.type!
          if (isEntryTypeSelectable(type)) {
            onSelectElement(name)
          }
          // onSelectElement(name)
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
      <Entry
        activeDescendant={activeDescendant}
        entry={getTableOfContents(formContent)}
        id="node-0"
        level={0}
        selectedEntryName={selectedElementName}
        setActiveDescendant={setActiveDescendant}
        onSelectEntry={onSelectElement}
      />
    </ul>
  )
}
