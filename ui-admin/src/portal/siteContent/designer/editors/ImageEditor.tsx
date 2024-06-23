import { HtmlSection, ImageConfig, SectionConfig } from '@juniper/ui-core'
import { SiteMediaMetadata } from 'api/api'
import React from 'react'
import { ImageSelector } from '../components/ImageSelector'
import { PortalEnvContext } from '../../../PortalRouter'

/**
 * Returns an editor for an image element of a website section
 */
export const ImageEditor = ({ portalEnvContext, section, updateSection, siteMediaList }: {
    portalEnvContext: PortalEnvContext,
    section: HtmlSection, updateSection: (section: HtmlSection) => void, siteMediaList: SiteMediaMetadata[]
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  return (
    <div>
      <label className='form-label fw-semibold'>Image</label>
      <ImageSelector portalEnvContext={portalEnvContext}
        imageList={siteMediaList} image={config.image as ImageConfig} onChange={image => {
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, image }) })
        }}/>
    </div>
  )
}
