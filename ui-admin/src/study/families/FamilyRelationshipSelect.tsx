import React from 'react'
import Creatable from 'react-select/creatable'

export const FAMILY_RELATIONSHIP_OPTIONS = [
  'Father',
  'Mother',
  'Maternal Grandfather',
  'Maternal Grandmother',
  'Paternal Grandfather',
  'Paternal Grandmother',
  'Brother',
  'Sister',
  'Daughter',
  'Son'
]

/**
 * Selector for the family relationship ("sister", "father", etc...) between two enrollees.
 * Offers common relationships, but is creatable so other relationship types can be entered.
 */
export const FamilyRelationshipSelect = ({
  value, onChange, isDisabled = false, className, ariaLabel = 'Relationship'
}: {
  value?: string, onChange: (value: string) => void, isDisabled?: boolean, className?: string, ariaLabel?: string
}) => {
  return <Creatable
    aria-label={ariaLabel}
    isDisabled={isDisabled}
    className={className}
    options={FAMILY_RELATIONSHIP_OPTIONS.map(val => ({ label: val, value: val }))}
    styles={{
      control: baseStyles => ({
        ...baseStyles,
        minWidth: '200px',
        borderColor: 'var(--bs-border-color)' // use same border color as all other components
      })
    }}
    value={value ? { value, label: value } : null}
    onChange={option => onChange(option?.value || '')}
  />
}
