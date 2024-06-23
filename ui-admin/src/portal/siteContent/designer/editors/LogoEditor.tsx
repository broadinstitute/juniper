import { PortalEnvContext } from 'portal/PortalRouter'
import { HtmlSection, ImageConfig, SectionConfig } from '@juniper/ui-core'
import { SiteMediaMetadata } from 'api/api'
import React, { useId } from 'react'
import classNames from 'classnames'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp, faPlus } from '@fortawesome/free-solid-svg-icons'
import { TextInput } from 'components/forms/TextInput'
import { Button } from 'components/forms/Button'
import { ImageSelector } from '../components/ImageSelector'
import { ListElementController } from '../components/ListElementController'

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
          aria-expanded="false"
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
                <ListElementController<ImageConfig>
                  index={i}
                  items={logos}
                  updateItems={newLogos => {
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, logos: newLogos }) })
                  }}
                />
              </div>
              <div>
                <label className='form-label fw-semibold'>Image</label>
                <ImageSelector portalEnvContext={portalEnvContext}
                  imageList={siteMediaList} image={logo} onChange={image => {
                    const newLogos = [...logos]
                    newLogos[i] = {
                      cleanFileName: image.cleanFileName,
                      version: image.version
                    }
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, logos: newLogos }) })
                  }}/>
                <TextInput label="Image Alt Text" className="mb-2" value={logo.alt}
                  placeholder={'Enter alt text for the image'}
                  onChange={value => {
                    const newLogos = [...logos]
                    newLogos[i].alt = value
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, logos: newLogos }) })
                  }}/>

                <TextInput label="Link" value={logo.link}
                  placeholder={'Clicking the logo will take the user to this URL'}
                  onChange={value => {
                    const newLogos = [...logos]
                    newLogos[i].link = value
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, logos: newLogos }) })
                  }}/>

              </div>
            </div>
          })}
        </div>
        <Button onClick={() => {
          const newLogos = [...logos]
          newLogos.push({ cleanFileName: '', version: 1 })
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, logos: newLogos }) })
        }}><FontAwesomeIcon icon={faPlus}/> Add Logo</Button>
      </div>
    </div>
  )
}
