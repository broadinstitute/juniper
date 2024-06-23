import { ButtonConfig, HtmlSection, SectionConfig } from '@juniper/ui-core'
import React, { useId } from 'react'
import classNames from 'classnames'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp, faPlus } from '@fortawesome/free-solid-svg-icons'
import Select from 'react-select'
import { TextInput } from 'components/forms/TextInput'
import { Button } from 'components/forms/Button'
import { ListElementController } from '../components/ListElementController'

/**
 * Returns an editor for a button that appears in a website section
 */
export const ButtonEditor = ({ section, updateSection }: {
    section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const buttons = config.buttons as ButtonConfig[] || []
  const buttonsContentId = useId()
  const buttonsTargetSelector = `#${buttonsContentId}`
  return (
    <div>
      <div className="pb-1">
        <button
          aria-controls={buttonsTargetSelector}
          aria-expanded="true"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={buttonsTargetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>Buttons ({buttons.length})</span>
          <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
      </div>
      <div className="collapse hide rounded-3 mb-2" id={buttonsContentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <div>
          {buttons.map((button, i) => {
            return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
              <div className="d-flex justify-content-between align-items-center">
                <span className="h5">Edit button</span>
                <ListElementController<ButtonConfig>
                  index={i}
                  items={buttons}
                  updateItems={newButtons => {
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, buttons: newButtons }) })
                  }}
                />
              </div>
              <div>
                <label className='form-label fw-semibold mb-2'>Button Type</label>
                <Select className='w-100 mb-2' options={BUTTON_TYPES} value={BUTTON_TYPES[
                  BUTTON_TYPES.findIndex(opt => opt.value === button.type)
                ]} aria-label={'Select section type'}
                onChange={opt => {
                  if (opt != undefined) {
                    const newButtons = [...buttons]
                    newButtons[i].type = ButtonTypeTemplates[opt.value].type
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, buttons: newButtons }) })
                  }
                }}/>
                <TextInput label="Button Text" className="mb-2" value={button.text} onChange={value => {
                  const newButtons = [...buttons]
                  newButtons[i].text = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, buttons: newButtons }) })
                }}/>
                {(!button.type || button.type === 'internalLink') &&
                    <TextInput label="Button Link" className="mb-2" value={button.href} onChange={value => {
                      const newButtons = [...buttons]
                      // @ts-ignore we've already checked that it's a link button, which requires an href
                      newButtons[i].href = value
                      updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, buttons: newButtons }) })
                    }}/>}
                {button.type === 'join' &&
                    <TextInput label="Study Shortcode" className="mb-2"
                      value={button.studyShortcode} onChange={value => {
                        const newButtons = [...buttons]
                        // @ts-ignore we've already checked that it's a join button, which requires a shortcode
                        newButtons[i].studyShortcode = value
                        updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, buttons: newButtons }) })
                      }}/>}
              </div>
            </div>
          })}
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
