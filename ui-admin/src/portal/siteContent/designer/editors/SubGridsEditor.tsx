import { PortalEnvContext } from 'portal/PortalRouter'
import { HtmlSection, SectionConfig, SiteMediaMetadata } from 'api/api'
import { ImageConfig, PhotoBio, SubGrid } from '@juniper/ui-core'
import React, { useId } from 'react'
import classNames from 'classnames'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp, faPlus } from '@fortawesome/free-solid-svg-icons'
import { TextInput } from 'components/forms/TextInput'
import { Textarea } from 'components/forms/Textarea'
import { ImageSelector } from '../components/ImageSelector'
import { ListElementController } from '../components/ListElementController'
import { Button } from '../../../../components/forms/Button'

/**
 * Returns an editor for the photo bio element of a website section
 */
export const SubGridsEditor = ({ portalEnvContext, mediaList, section, updateSection }: {
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
          aria-expanded="false"
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
                <SubGridEditor portalEnvContext={portalEnvContext}
                  index={i}
                  mediaList={mediaList} section={section} updateSection={updateSection}/>
              </div>
            </div>
          })}
          <Button onClick={() => {
            const newSubGrids = [...subGrids, { title: '', photoBios: [] }]
            updateSection({
              ...section,
              sectionConfig: JSON.stringify({ ...config, subGrids: newSubGrids })
            })
          }
          }>
            <FontAwesomeIcon icon={faPlus}/> Add section
          </Button>
        </div>
      </div>
    </div>
  )
}

const SubGridEditor = ({ portalEnvContext, index, mediaList, section, updateSection }: {
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
      <div className="d-flex justify-content-between align-items-center">
        <button
          aria-controls={subGridsTargetSelector}
          aria-expanded="false"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={subGridsTargetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>
            {subGrid.title ? subGrid.title : 'Bio sub-section'} ({subGrid.photoBios.length})
          </span>
          <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
        <ListElementController items={subGrids} index={index} updateItems={newSubGrids => {
          updateSection({
            ...section,
            sectionConfig: JSON.stringify({ ...config, subGrids: newSubGrids })
          })
        }}/>
      </div>
      <div className="collapse hide rounded-3 mb-2" id={subGridsContentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <TextInput label="Title" className="mb-2" value={subGrid.title} onChange={value => {
          const newSubGrids = [...subGrids]
          newSubGrids[index].title = value
          updateSection({
            ...section,
            sectionConfig: JSON.stringify({ ...config, subGrids: newSubGrids })
          })
        }}/>
        <div>
          {subGrid.photoBios.map((photoBio, i) => {
            return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
              <div className="d-flex justify-content-between align-items-center">
                <span className="h5">Edit bio</span>
                <ListElementController<PhotoBio>
                  index={i}
                  items={subGrid.photoBios}
                  updateItems={newPhotoBios => {
                    const newSubGrids = [...subGrids]
                    newSubGrids[index].photoBios = newPhotoBios
                    updateSection({
                      ...section,
                      sectionConfig: JSON.stringify({ ...config, subGrids: newSubGrids })
                    })
                  }}
                />
              </div>
              <PhotoBioEditor portalEnvContext={portalEnvContext}
                mediaList={mediaList} photoBio={photoBio} updatePhotoBio={updatedPhotoBio => {
                  const newSubGrids = [...subGrids]
                  newSubGrids[index].photoBios[i] = updatedPhotoBio
                  updateSection({
                    ...section,
                    sectionConfig: JSON.stringify({ ...config, subGrids: newSubGrids })
                  })
                }}/>
            </div>
          })}
          <Button onClick={() => {
            const newPhotoBios = [...subGrid.photoBios,
              { name: '', title: '', blurb: '', detail: '', image: { cleanFileName: '', version: 0 } }]
            const newSubGrids = [...subGrids]
            newSubGrids[index].photoBios = newPhotoBios
            updateSection({
              ...section,
              sectionConfig: JSON.stringify({ ...config, subGrids: newSubGrids })
            })
          }
          }>
            <FontAwesomeIcon icon={faPlus}/> Add Bio
          </Button>

        </div>
      </div>
    </div>
  )
}

/**
 *
 */
export const PhotoBioEditor = ({ portalEnvContext, mediaList, photoBio, updatePhotoBio }: {
    portalEnvContext: PortalEnvContext, mediaList: SiteMediaMetadata[],
    photoBio: PhotoBio, updatePhotoBio: (photoBio: PhotoBio) => void
}) => {
  return (
    <div>
      <label className='form-label fw-semibold'>Image</label>
      <ImageSelector portalEnvContext={portalEnvContext}
        imageList={mediaList} image={photoBio.image as ImageConfig} onChange={image => {
          updatePhotoBio({ ...photoBio, image })
        }}/>
      <TextInput label="Name" className="mb-2" value={photoBio.name} onChange={value => {
        updatePhotoBio({ ...photoBio, name: value })
      }}/>
      <TextInput label="Title" className="mb-2" value={photoBio.title} onChange={value => {
        updatePhotoBio({ ...photoBio, title: value })
      }}/>
      <Textarea rows={2} label="Bio" className="mb-2" value={photoBio.blurb} onChange={value => {
        updatePhotoBio({ ...photoBio, blurb: value })
      }}/>
      <Textarea rows={2} label="Detail" className="mb-2" value={photoBio.detail} onChange={value => {
        updatePhotoBio({ ...photoBio, detail: value })
      }}/>
    </div>
  )
}
