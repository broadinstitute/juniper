import {
  HtmlSection,
  SectionConfig
} from '@juniper/ui-core'
import React, { useId } from 'react'
import { TextInput } from 'components/forms/TextInput'
import { Checkbox } from 'components/forms/Checkbox'
import Select from 'react-select'
import { CollapsibleSectionButton } from '../components/CollapsibleSectionButton'
import useReactSingleSelect from 'util/react-select-utils'

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

  const {
    selectInputId: blurbSizeSelectInputId,
    onChange: blurbSizeOnChange,
    selectedOption: blurbSizeSelectedOption,
    options: blurbSizeOptions
  } = useReactSingleSelect<string>(
    ['fs-1', 'fs-2', 'fs-3', 'fs-4', 'fs-5', 'fs-6'],
    (val: string) => ({ label: val, value: val }),
    (opt: string | undefined) => {
      updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, blurbSize: opt }) })
    },
    config.blurbSize ? config.blurbSize as string : undefined
  )

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
        {section.sectionType === 'HERO_WITH_IMAGE' && <Checkbox label={'Image As Background'} className="mb-2"
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
        {section.sectionType === 'HERO_WITH_IMAGE' && config.imageAsBackground as boolean && <TextInput
          label="Aspect Ratio"
          infoContent={'Fixes the size of this section to a specific aspect ratio. '
            + 'If set correctly, your background image will not be cropped. For example: 16/9, 4/3, 1/1.'}
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
          <label className="form-label fw-semibold" htmlFor={blurbSizeSelectInputId}>Blurb Text Size</label>
          <Select
            options={blurbSizeOptions}
            value={blurbSizeSelectedOption}
            onChange={blurbSizeOnChange}
            inputId={blurbSizeSelectInputId}
          />
        </div>}
      </div>
    </div>
  )
}
