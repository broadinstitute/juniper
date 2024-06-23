import { PortalEnvContext } from '../../../PortalRouter'
import { HtmlSection, ImageConfig, MediaConfig, SectionConfig } from '@juniper/ui-core'
import { SiteMediaMetadata } from 'api/api'
import React, { useId } from 'react'
import classNames from 'classnames'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp, faPlus, faTimes } from '@fortawesome/free-solid-svg-icons'
import { TextInput } from 'components/forms/TextInput'
import { Button, IconButton } from 'components/forms/Button'
import { ImageSelector } from './ImageSelector'

/**
 *
 */
export const LogoEditor = ({ portalEnvContext, section, updateSection, siteMediaList }: {
    portalEnvContext: PortalEnvContext,
    section: HtmlSection, updateSection: (section: HtmlSection) => void, siteMediaList: SiteMediaMetadata[]
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const logos = config.logos as ImageConfig[] || []

  const logosContentId = useId()
  const logosTargetSelector = `#${logosContentId}`
  return (
    <div>
      <div className="pb-1">
        <button
          aria-controls={logosTargetSelector}
          aria-expanded="true"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={logosTargetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>Logos ({logos.length})</span>
          <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
      </div>
      <div className="collapse hide rounded-3 mb-2" id={logosContentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <div>
          {logos.map((logo, i) => {
            return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
              <div className="d-flex justify-content-between align-items-center">
                <span className="h5">Edit logo</span>
                <div className="d-flex justify-content-end">
                  <IconButton icon={faChevronUp} aria-label={'Move Up'} disabled={i < 1}
                    onClick={() => {
                      const newLogos = [...config.logos as MediaConfig[]]
                      const temp = newLogos[i]
                      newLogos[i] = newLogos[i - 1]
                      newLogos[i - 1] = temp
                      updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, logos: newLogos }) })
                    }}/>
                  <IconButton icon={faChevronDown} aria-label={'Move Down'} disabled={i > logos.length - 2}
                    onClick={() => {
                      const newLogos = [...config.logos as MediaConfig[]]
                      const temp = newLogos[i]
                      newLogos[i] = newLogos[i + 1]
                      newLogos[i + 1] = temp
                      updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, logos: newLogos }) })
                    }}/>
                  <IconButton icon={faTimes} className={'text-danger'} aria-label={'Delete'} onClick={() => {
                    const newLogos = [...config.logos as MediaConfig[]]
                    newLogos.splice(i, 1)
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, logos: newLogos }) })
                  }}/>
                </div>
              </div>
              <div>
                <label className='form-label fw-semibold'>Image</label>
                <ImageSelector portalEnvContext={portalEnvContext}
                  imageList={siteMediaList} image={logo as ImageConfig} onChange={image => {
                    const parsed = JSON.parse(section.sectionConfig || '{}')
                    const newLogos = [...config.logos as MediaConfig[]]
                    newLogos[i] = {
                      cleanFileName: image.cleanFileName,
                      version: image.version
                    }
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, logos: newLogos }) })
                  }}/>
                <TextInput label="Image Alt Text" value={logo.alt} onChange={value => {
                  const parsed = JSON.parse(section.sectionConfig || '{}')
                  const newLogos = [...config.logos as MediaConfig[]]
                  newLogos[i].alt = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, logos: newLogos }) })
                }}/>

                <TextInput label="Link" value={logo.link} onChange={value => {
                  const parsed = JSON.parse(section.sectionConfig || '{}')
                  const newLogos = [...config.logos as ImageConfig[]]
                  newLogos[i].link = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, logos: newLogos }) })
                }}/>

              </div>
            </div>
          })}
        </div>
        <Button onClick={() => {
          const parsed = JSON.parse(section.sectionConfig!)
          const newLogos = [...logos]
          newLogos.push({ cleanFileName: '', version: 1 })
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, logos: newLogos }) })
        }}><FontAwesomeIcon icon={faPlus}/> Add Logo</Button>
      </div>
    </div>
  )
}
