import { HtmlSection, SectionConfig, socialMediaSites } from '@juniper/ui-core'
import { TextInput } from 'components/forms/TextInput'
import React from 'react'

/**
 * Returns an editor for the social media website section
 */
export const SocialMediaEditor = ({ section, updateSection }: {
    section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  return (
    <div>
      {socialMediaSites.map(site => {
        const handleKey = `${site.label.toLowerCase()}Handle`
        return (
          <div key={handleKey} className="d-flex align-items-center mb-2">
            <div className="col-3">
              <label className='fw-bold'>{site.label}</label>
            </div>
            <div className="col d-flex align-items-right col">
              <TextInput
                value={config[handleKey] as string}
                placeholder={`Enter ${site.label} handle. Leave blank to hide`}
                onChange={value => {
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, [handleKey]: value }) })
                }}/>
            </div>
          </div>
        )
      })}
    </div>
  )
}
