import { ButtonConfig, HtmlSection, SectionConfig } from '@juniper/ui-core'
import React, { useId } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import Select from 'react-select'
import { TextInput } from 'components/forms/TextInput'
import { Button } from 'components/forms/Button'
import { CollapsibleSectionButton } from '../components/CollapsibleSectionButton'
import { ListElementController } from '../components/ListElementController'

/**
 * Returns an editor allowing the user to add and edit buttons
 */
export const ButtonsEditor = ({ section, updateSection }: {
    section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const buttons = config.buttons as ButtonConfig[] || []
  const buttonsContentId = useId()
  const buttonsTargetSelector = `#${buttonsContentId}`
  return (
    <div>
      <CollapsibleSectionButton targetSelector={buttonsTargetSelector} sectionLabel={`Buttons (${buttons.length})`}/>
      <div className="collapse hide rounded-3 mb-2" id={buttonsContentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <div>
          {buttons.map((button, i) => (
            <div key={i}>
              <div className="d-flex justify-content-between align-items-center">
                <span className="h6">Edit button</span>
                <ListElementController<ButtonConfig>
                  index={i}
                  items={buttons}
                  updateItems={newButtons => {
                    updateSection({
                      ...section,
                      sectionConfig: JSON.stringify({ ...config, buttons: newButtons })
                    })
                  }}
                />
              </div>
              <ButtonEditor key={i} button={button} updateButton={newButton => {
                const newButtons = [...buttons]
                newButtons[i] = newButton
                updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, buttons: newButtons }) })
              }}/>
            </div>
          ))}
        </div>
        <Button onClick={() => {
          const newButtons = [...buttons]
          newButtons.push({ type: 'internalLink', text: '', href: '' })
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, buttons: newButtons }) })
        }}><FontAwesomeIcon icon={faPlus}/> Add Button
        </Button>
      </div>
    </div>
  )
}

/**
 * Returns an editor for a single button
 */
export const ButtonEditor = ({ button, updateButton }: {
  button: ButtonConfig, updateButton: (button: ButtonConfig) => void
}) => {
  return (
    <div style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
      <label className='form-label fw-semibold mb-2'>Button Type</label>
      <Select className='w-100 mb-2' options={BUTTON_TYPES}
        value={BUTTON_TYPES[
          BUTTON_TYPES.findIndex(opt => opt.value === (button.type || 'externalLink'))
        ]}
        aria-label={'Select button type'}
        onChange={opt => {
          if (opt != undefined) {
            const newButton = { ...button }
            newButton.type = ButtonTypeTemplates[opt.value].type
            updateButton(newButton)
          }
        }}/>
      <TextInput label="Button Text" className="mb-2" value={button.text} onChange={value => {
        const newButton = { ...button }
        newButton.text = value
        updateButton(newButton)
      }}/>
      {(!button.type || button.type === 'internalLink') &&
          <TextInput label="Button Link" className="mb-2" value={button.href} onChange={value => {
            const newButton = { ...button }
            // @ts-ignore we've already checked that it's a link button, which requires an href
            newButton.href = value
            updateButton(newButton)
          }}/>}
      {button.type === 'join' &&
          <TextInput label="Study Shortcode" className="mb-2"
            value={button.studyShortcode} onChange={value => {
              const newButton = { ...button }
              // @ts-ignore we've already checked that it's a join button, which requires a shortcode
              newButton.studyShortcode = value
              updateButton(newButton)
            }}/>}
    </div>
  )
}

const BUTTON_TYPES = [
  { label: 'Internal Link', value: 'internalLink' },
  { label: 'External Link', value: 'externalLink' },
  { label: 'Join Study', value: 'join' },
  { label: 'Join Mailing List', value: 'mailingList' }
]

const ButtonTypeTemplates: Record<string, ButtonConfig> = {
  'internalLink': {
    type: 'internalLink',
    text: '',
    href: ''
  },
  'join': {
    type: 'join',
    text: 'Join Study',
    studyShortcode: ''
  },
  'mailingList': {
    type: 'mailingList',
    text: 'Join Mailing List'
  },
  'externalLink': {
    type: undefined,
    text: '',
    href: ''
  }
}
