import { HtmlSection, SectionConfig } from '@juniper/ui-core'
import React, { useId } from 'react'
import { TextInput } from 'components/forms/TextInput'
import { Checkbox } from 'components/forms/Checkbox'
import Select from 'react-select'
import { CollapsibleSectionButton } from '../components/CollapsibleSectionButton'

/**
 * Returns an editor for the style options of a website section
 */
export const StyleEditor = ({ section, updateSection }: {
    section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const contentId = useId()
  const targetSelector = `#${contentId}`

  const imagePositionOptions = [
    { label: 'Left', value: 'left' }, { label: 'Right', value: 'right' }
  ]
  const blurbAlignOptions = [
    { label: 'Left', value: 'left' }, { label: 'Center', value: 'center' }, { label: 'Right', value: 'right' }
  ]

  return (
    <div>
      <CollapsibleSectionButton targetSelector={targetSelector} sectionLabel={'Style Options'}/>
      <div className="collapse hide rounded-3 mb-2" id={contentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <TextInput label="Background Color" className="mb-2" value={config.background as string}
          placeholder={'Enter a value to override default'}
          onChange={value => {
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, background: value }) })
          }}/>
        <TextInput label="Text Color" className="mb-2" value={config.color as string}
          placeholder={'Enter a value to override default'}
          onChange={value => {
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, color: value }) })
          }}/>
        { Object.hasOwnProperty.call(config, 'image') &&
            <div className='my-2'>
              <label className='form-label fw-semibold'>Image Position</label>
              <Select options={imagePositionOptions}
                value={config.imagePosition ? imagePositionOptions.find(opt => opt.value === config.imagePosition)
                  : undefined}
                onChange={opt => {
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, imagePosition: opt?.value }) })
                }}/>
            </div>}
        { Object.hasOwnProperty.call(config, 'image') && <Checkbox label={'Full Width'} className="mb-2"
          checked={config.fullWidth as boolean == undefined ? false : config.fullWidth as boolean}
          onChange={value => {
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, fullWidth: value }) })
          }}/>}
        { Object.hasOwnProperty.call(config, 'blurbAlign') &&
            <div className='my-2'>
              <label className='form-label fw-semibold'>Blurb Text Position</label>
              <Select options={blurbAlignOptions}
                value={config.blurbAlign ? blurbAlignOptions.find(opt => opt.value === config.blurbAlign)
                  : undefined}
                onChange={opt => {
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, blurbAlign: opt?.value }) })
                }}/>
            </div>}
      </div>
    </div>
  )
}
