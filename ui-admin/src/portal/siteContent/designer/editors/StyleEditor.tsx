import {
  HtmlSection,
  SectionConfig
} from '@juniper/ui-core'
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
        {
          Object.hasOwnProperty.call(config, 'steps') && <Checkbox label={'Vertical'} className="mb-2"
            checked={config.vertical as boolean == undefined ? false : config.vertical as boolean}
            onChange={value => {
              updateSection({
                ...section,
                sectionConfig: JSON.stringify({
                  ...config,
                  vertical: value
                })
              })
            }}/>
        }
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
        {Object.hasOwnProperty.call(config, 'image') && <Checkbox label={'Image As Background'} className="mb-2"
          checked={config.imageAsBackground as boolean == undefined ? false : config.imageAsBackground as boolean}
          onChange={value => {
            updateSection({
              ...section,
              sectionConfig: JSON.stringify({
                ...config,
                imageAsBackground: value
              })
            })
          }}/>}
        {Object.hasOwnProperty.call(config, 'image') && config.imageAsBackground as boolean && <TextInput
          label="Aspect Ratio"
          className="mb-2"
          value={config.aspectRatio as string}
          onChange={value => {
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, aspectRatio: value }) })
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
        {Object.hasOwnProperty.call(config, 'blurb') && <div>
          <label className="form-label fw-semibold">Blurb Text Size</label>
          <Select
            options={[
              { label: 'fs-1', value: 'fs-1' },
              { label: 'fs-2', value: 'fs-2' },
              { label: 'fs-3', value: 'fs-3' },
              { label: 'fs-4', value: 'fs-4' },
              { label: 'fs-5', value: 'fs-5' },
              { label: 'fs-6', value: 'fs-6' }
            ]}
            value={config.blurbSize ? { label: config.blurbSize, value: config.blurbSize } : undefined}
            onChange={
              opt => {
                updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, blurbSize: opt?.value }) })
              }
            }
          />
        </div>}
      </div>
    </div>
  )
}
