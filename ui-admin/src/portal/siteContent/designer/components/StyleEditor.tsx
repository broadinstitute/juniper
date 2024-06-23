import { HtmlSection, SectionConfig } from '@juniper/ui-core'
import React, { useId } from 'react'
import classNames from 'classnames'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'
import { TextInput } from 'components/forms/TextInput'
import { Checkbox } from 'components/forms/Checkbox'
import Select from 'react-select'

/**
 * Returns an editor for the style options of a website section
 */
export const StyleEditor = ({ section, updateSection }: {
    section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const contentId = useId()
  const targetSelector = `#${contentId}`
  const imagePositionOptions = [{ label: 'Left', value: 'left' }, { label: 'Right', value: 'right' }]
  return (
    <div>
      <div className="pb-1">
        <button
          aria-controls={targetSelector}
          aria-expanded="true"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={targetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>Style Options</span>
          <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
      </div>
      <div className="collapse hide rounded-3 mb-2" id={contentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <TextInput label="Background Color" value={config.background as string}
          placeholder={'Enter a value to override default'}
          onChange={value => {
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, background: value }) })
          }}/>
        <TextInput label="Text Color" value={config.color as string}
          placeholder={'Enter a value to override default'}
          onChange={value => {
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, color: value }) })
          }}/>
        { Object.hasOwnProperty.call(config, 'image') && <div className='mt-2'><Checkbox label={'Full Width'}
          checked={config.fullWidth as boolean == undefined ? false : config.fullWidth as boolean}
          onChange={value => {
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, fullWidth: value }) })
          }}/></div>}
        { Object.hasOwnProperty.call(config, 'image') &&
            <div className='mt-2'>
              <label className='form-label fw-semibold'>Image Position</label>
              <Select options={imagePositionOptions}
                value={config.imagePosition ? imagePositionOptions.find(opt => opt.value === config.imagePosition)
                  : undefined}
                onChange={opt => {
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, imagePosition: opt?.value }) })
                }}/></div>}
      </div>
    </div>
  )
}
