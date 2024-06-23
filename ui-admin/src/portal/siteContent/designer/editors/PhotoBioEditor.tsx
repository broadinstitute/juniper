import { PortalEnvContext } from '../../../PortalRouter'
import { HtmlSection, SectionConfig, SiteMediaMetadata } from 'api/api'
import { ImageConfig, SubGrid } from '@juniper/ui-core'
import React, { useId } from 'react'
import classNames from 'classnames'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'
import { TextInput } from 'components/forms/TextInput'
import { Textarea } from 'components/forms/Textarea'
import { ImageSelector } from '../components/ImageSelector'

/**
 * Returns an editor for the photo bio element of a website section
 */
export const PhotoBioEditor = ({ portalEnvContext, mediaList, section, updateSection }: {
    portalEnvContext: PortalEnvContext, mediaList: SiteMediaMetadata[],
    section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const subGrids = config.subGrids as SubGrid[] || []

  const photosContentId = useId()
  const photosTargetSelector = `#${photosContentId}`
  return (
    <div>
      <div className="pb-1">
        <button
          aria-controls={photosTargetSelector}
          aria-expanded="true"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={photosTargetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>Photo Bio Sections ({subGrids.length})</span>
          <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
      </div>
      <div className="collapse hide rounded-3 mb-2" id={photosContentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <div>
          {subGrids.map((subGrid, i) => {
            return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
              <div className="d-flex justify-content-between align-items-center">
                {/*<span className="h5">Photo Bio {i + 1}</span>*/}
                <PhotoBioSectionEditor portalEnvContext={portalEnvContext}
                  index={i}
                  mediaList={mediaList} section={section} updateSection={updateSection}/>
              </div>
            </div>
          })}
        </div>
      </div>
    </div>
  )
}

const PhotoBioSectionEditor = ({ portalEnvContext, index, mediaList, section, updateSection }: {
    portalEnvContext: PortalEnvContext,
    index: number, mediaList: SiteMediaMetadata[], section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const subGrids = config.subGrids as SubGrid[] || []
  const subGrid = subGrids[index]

  const subGridsContentId = useId()
  const subGridsTargetSelector = `#${subGridsContentId}`
  return (
    <div className='w-100'>
      <div className="pb-1">
        <button
          aria-controls={subGridsTargetSelector}
          aria-expanded="true"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={subGridsTargetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>
            {subGrid.title ? subGrid.title : 'Bio sub-section'} ({subGrid.photoBios.length})
          </span> <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
      </div>
      <div className="collapse hide rounded-3 mb-2" id={subGridsContentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <div>
          {subGrid.photoBios.map((photoBio, i) => {
            return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
              <div>
                <label className='form-label fw-semibold m-0'>Image</label>
                <ImageSelector portalEnvContext={portalEnvContext}
                  imageList={mediaList} image={photoBio.image as ImageConfig} onChange={image => {
                    const newSubGrids = [...config.subGrids as SectionConfig[]]
                    newSubGrids[i].image = image
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, subGrids: newSubGrids }) })
                  }}/>
                <TextInput label="Name" value={photoBio.name} onChange={value => {
                  const newSubGrids = [...config.subGrids as SectionConfig[]]
                  newSubGrids[i].name = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, subGrids: newSubGrids }) })
                }}/>
                <TextInput label="Title" value={photoBio.title} onChange={value => {
                  const newSubGrids = [...config.subGrids as SectionConfig[]]
                  newSubGrids[i].title = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, subGrids: newSubGrids }) })
                }}/>
                <Textarea rows={2} label="Bio" value={photoBio.blurb} onChange={value => {
                  const newSubGrids = [...config.subGrids as SectionConfig[]]
                  newSubGrids[i].bio = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, subGrids: newSubGrids }) })
                }}/>
                <Textarea rows={2} label="Detail" value={photoBio.detail} onChange={value => {
                  const newSubGrids = [...config.subGrids as SectionConfig[]]
                  newSubGrids[i].detail = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, subGrids: newSubGrids }) })
                }}/>
              </div>
            </div>
          })}
        </div>
      </div>
    </div>
  )
}
