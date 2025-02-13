import { NavLink } from 'react-router-dom'
import React from 'react'
import { sidebarNavLinkClasses } from 'navbar/AdminSidebar'
import CollapsableMenu from 'navbar/CollapsableMenu'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faEye,
  faEyeSlash
} from '@fortawesome/free-solid-svg-icons'

export type SidebarItemT = {
  key: string
  label: string
  link: string
}

export type SidebarSectionT = {
  key: string
  label: string
  items: SidebarItemT[]
}

const navStyleFunc = ({ isActive }: { isActive: boolean }) => {
  return isActive ? { background: 'rgba(255, 255, 255, 0.3)' } : {}
}

export const SidebarSection = ({
  section, isEditing, hiddenItems, toggleHiddenItem
}: {
  section: SidebarSectionT, isEditing: boolean, hiddenItems: string[], toggleHiddenItem: (key: string) => void
}) => {
  const shownItems = isEditing
    ? section.items // show all items when editing
    : section.items.filter(item => !hiddenItems.includes(item.key)) || []

  if (shownItems.length === 0) {
    return <></>
  }

  return <CollapsableMenu header={section.label} content={<ul className="list-unstyled">
    {shownItems
      .map(item => <SidebarItem
        key={item.key}
        item={item}
        isEditing={isEditing}
        hiddenItems={hiddenItems}
        toggleHiddenItem={toggleHiddenItem}
      />)}
  </ul>}/>
}

export const SidebarItem = ({
  item, isEditing, hiddenItems, toggleHiddenItem
}: { item: SidebarItemT, isEditing: boolean, hiddenItems: string[], toggleHiddenItem: (key: string) => void }) => {
  return <>
    <li className="mb-2 d-flex">
      <NavLink
        to={item.link}
        className={sidebarNavLinkClasses}
        style={navStyleFunc}>{item.label}</NavLink>
      {isEditing && <>
        <button
          className="btn btn-secondary btn-sm text-white hover-opacity-50"
          onClick={() => toggleHiddenItem(item.key)}
          aria-label={`Toggle visibility for ${item.label}`}
        >
          {hiddenItems.includes(item.key) ? <FontAwesomeIcon icon={faEyeSlash} /> : <FontAwesomeIcon icon={faEye}/>}
        </button>
      </>}
    </li>
  </>
}
