import React from 'react'
import ReactMarkdown from 'react-markdown'

import { SectionConfig } from 'api/api'
import PearlImage, { PearlImageConfig, validatePearlImageConfig } from 'util/PearlImage'
import { getSectionStyle } from 'util/styleUtils'
import { withValidatedSectionConfig } from 'util/withValidatedSectionConfig'
import { requireOptionalArray, requireOptionalString, requirePlainObject, requireString } from 'util/validationUtils'

type PhotoBlurbGridConfig = {
  title?: string,
  subGrids: SubGrid[]
}

type SubGrid = {
  title?: string
  photoBios: PhotoBio[]
}

type PhotoBio = {
  image: PearlImageConfig,
  name: string,
  title?: string,
  blurb?: string
}

const validatePhotoBio = (config: unknown): PhotoBio => {
  const message = 'Invalid Invalid PhotoBlurbGridConfig: invalid photoBio'
  const configObj = requirePlainObject(config, message)
  const image = validatePearlImageConfig(configObj.image)
  const name = requireString(configObj, 'name', message)
  const title = requireOptionalString(configObj, 'title', message)
  const blurb = requireOptionalString(configObj, 'blurb', message)
  return { image, name, title, blurb }
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

type PhotoBlurbGridProps = {
  anchorRef?: string
  config: PhotoBlurbGridConfig
}

/**
 * Template for rendering a hero with centered content.
 */
function PhotoBlurbGrid(props: PhotoBlurbGridProps) {
  const { anchorRef, config } = props
  const { subGrids, title } = config

  return <div id={anchorRef} className="py-5" style={getSectionStyle(config)}>
    {title && <h1 className="fs-1 fw-normal lh-sm text-center mb-4">
      {title}
    </h1>}
    {(subGrids ?? []).map((subGrid, index) => <SubGridView key={index} subGrid={subGrid}/>)}
  </div>
}

/** renders a subgrouping of photos (e.g. "Our researchers") */
function SubGridView({ subGrid }: { subGrid: SubGrid }) {
  return <div className="row mx-0">
    <div className="col-12 col-sm-10 col-lg-8 mx-auto">
      {subGrid.title && <h3 className="text-center mb-4">{subGrid.title}</h3>}
      <div className="row mx-0">
        {subGrid.photoBios.map((bio, index) => <PhotoBioView key={index} photoBio={bio}/>)}
      </div>
    </div>
  </div>
}

/** renders a single bio with pic */
function PhotoBioView({ photoBio }: { photoBio: PhotoBio }) {
  return <div className="col-sm-6 col-md-4 gx-5 gy-3 text-center text-sm-start">
    <PearlImage image={photoBio.image} className="img-fluid"/>
    <div className="my-2">
      {photoBio.name}
      {!!photoBio.title && (
        <span className="detail" style={{ marginLeft: '0.5ch' }}>{photoBio.title}</span>
      )}
    </div>
    {!!photoBio.blurb && (
      <div className="detail" style={{ lineHeight: 1 }}>
        <ReactMarkdown>{photoBio.blurb}</ReactMarkdown>
      </div>
    )}
  </div>
}


export default withValidatedSectionConfig(validatePhotoBlurbGridConfig, PhotoBlurbGrid)
