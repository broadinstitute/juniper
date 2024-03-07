import classNames from 'classnames'
import React from 'react'

import { SectionConfig } from '../../../types/landingPageConfig'
import { getSectionStyle } from '../../util/styleUtils'
import { withValidatedSectionConfig } from '../../util/withValidatedSectionConfig'
import { requireOptionalArray, requireOptionalString, requirePlainObject, requireString }
  from '../../util/validationUtils'

import ConfiguredMedia, { MediaConfig, validateMediaConfig } from '../ConfiguredMedia'
import { Markdown } from '../Markdown'

import { TemplateComponentProps } from './templateUtils'
import { useApiContext } from '../../../participant/ApiProvider'
import { Modal } from 'react-bootstrap'

type PhotoBlurbGridConfig = {
  title?: string,
  subGrids: SubGrid[]
}

type SubGrid = {
  title?: string
  photoBios: PhotoBio[]
}

type PhotoBio = {
  image: MediaConfig,
  name: string,
  title?: string,
  blurb?: string,
  detail?: string
}

const validatePhotoBio = (config: unknown): PhotoBio => {
  const message = 'Invalid Invalid PhotoBlurbGridConfig: invalid photoBio'
  const configObj = requirePlainObject(config, message)
  const image = validateMediaConfig(configObj.image)
  const name = requireString(configObj, 'name', message)
  const title = requireOptionalString(configObj, 'title', message)
  const blurb = requireOptionalString(configObj, 'blurb', message)
  const detail = requireOptionalString(configObj, 'detail', message)
  return { image, name, title, blurb, detail }
}

const validateSubGrid = (config: unknown): SubGrid => {
  const message = 'Invalid PhotoBlurbGridConfig: Invalid subGrid'
  const configObj = requirePlainObject(config, message)
  const photoBios = requireOptionalArray(configObj, 'photoBios', validatePhotoBio, message)
  const title = requireOptionalString(configObj, 'title', message)
  return { photoBios, title }
}

/** Validate that a section configuration object conforms to PhotoBlurbGridConfig */
const validatePhotoBlurbGridConfig = (config: SectionConfig): PhotoBlurbGridConfig => {
  const message = 'Invalid PhotoBlurbGridConfig'
  const subGrids = requireOptionalArray(config, 'subGrids', validateSubGrid, message)
  const title = requireOptionalString(config, 'title', message)
  return { subGrids, title }
}

type PhotoBlurbGridProps = TemplateComponentProps<PhotoBlurbGridConfig>

/**
 * Template for rendering a hero with centered content.
 */
function PhotoBlurbGrid(props: PhotoBlurbGridProps) {
  const { anchorRef, config } = props
  const { subGrids, title } = config
  const { getImageUrl } = useApiContext()
  const hasTitle = !!title
  // Heading levels must increase one at a time, so if the grid has no
  // title, then the subgrid headings should be h2 elements.
  const subGridHeadingLevel = hasTitle ? 3 : 2

  return <div id={anchorRef} style={getSectionStyle(config, getImageUrl)}>
    {!!title && (
      <h2 className="fs-1 fw-normal lh-sm text-center mb-4">
        {title}
      </h2>
    )}
    {(subGrids ?? []).map((subGrid, index) => {
      return (
        <SubGridView
          key={index}
          className={index === 0 ? undefined : 'mt-4'}
          headingLevel={subGridHeadingLevel}
          subGrid={subGrid}
        />
      )
    })}
  </div>
}

type SubGridViewProps = {
  className?: string
  headingLevel: 2 | 3
  subGrid: SubGrid
}

/** renders a subgrouping of photos (e.g. "Our researchers") */
function SubGridView(props: SubGridViewProps) {
  const { className, headingLevel, subGrid } = props
  const Heading: 'h2' | 'h3' = `h${headingLevel}`
  return <div className={classNames('row mx-0', className)}>
    <div className="col-12 col-sm-10 col-lg-8 mx-auto">
      {subGrid.title && <Heading className="text-center mb-4">{subGrid.title}</Heading>}
      <div className="row mx-0">
        {subGrid.photoBios.map((bio, index) => <PhotoBioView key={index} photoBio={bio}/>)}
      </div>
    </div>
  </div>
}

/** renders a single bio with pic */
export function PhotoBioView({ photoBio }: { photoBio: PhotoBio }) {
  const [showDetail, setShowDetail] = React.useState(false)
  // Default alt text to person's name
  photoBio.image.alt ||= photoBio.name

  return <div className="col-sm-6 col-md-4 col-lg-3 gx-4 gx-lg-3 gy-3 text-center text-sm-start pb-2 hover-shadow">
    <button onClick={() => setShowDetail(!showDetail)}
      className="btn h-100 p-0 pb-2 text-start"
      style={{ border: '1px solid #ddd' }} >
      <div className="h-100">
        <ConfiguredMedia media={photoBio.image} className="img-fluid" style={{
          borderTopLeftRadius: '5px',
          borderTopRightRadius: '5px'
        }}/>
        <div className="my-2 fw-bold px-2">
          {photoBio.name} {photoBio.title}
        </div>
        {!!photoBio.blurb && (
          <Markdown className="fst-italic lh-1 px-2" style={{ fontSize: '0.9em' }}>
            {photoBio.blurb}
          </Markdown>
        )}
      </div>
    </button>
    {showDetail && <PhotoBioDetailModal photoBio={photoBio} onDismiss={() => setShowDetail(false)}/>}
  </div>
}

function PhotoBioDetailModal({ photoBio, onDismiss }: { photoBio: PhotoBio, onDismiss: () => void }) {
  return <Modal show={true} onHide={onDismiss} className="modal-lg" >
    <Modal.Body style={{ padding: '0px' }}>
      <div className="d-flex w-100" style={{ borderBottom: '1px solid #ccc' }}>
        <div style={{ maxWidth: '300px' }}>
          <ConfiguredMedia media={photoBio.image} className="img-fluid" style={{ borderTopLeftRadius: '5px' }}/>
        </div>
        <div className="flex-grow-1 pt-5 ps-5">
          <h3 className="fw-bold">{photoBio.name} </h3>
          <h5>{photoBio.title}</h5>
          <h5>{photoBio.blurb}</h5>
        </div>
        <div>
          <button className="btn-close p-3" onClick={onDismiss}/>
        </div>
      </div>
      {!!photoBio.detail && <div className="p-5">
        <Markdown>{photoBio.detail}</Markdown>
      </div>}
    </Modal.Body>
  </Modal>
}


export default withValidatedSectionConfig(validatePhotoBlurbGridConfig, PhotoBlurbGrid)
