import classNames from 'classnames'
import React from 'react'

import { SectionConfig } from 'api/api'
import { getSectionStyle } from 'util/styleUtils'
import { withValidatedSectionConfig } from 'util/withValidatedSectionConfig'
import { requireOptionalArray, requireOptionalString, requirePlainObject, requireString } from 'util/validationUtils'

import { Markdown } from '../Markdown'
import PearlImage, { PearlImageConfig, validatePearlImageConfig } from '../PearlImage'

import { TemplateComponentProps } from './templateUtils'

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

type PhotoBlurbGridProps = TemplateComponentProps<PhotoBlurbGridConfig>

/**
 * Template for rendering a hero with centered content.
 */
function PhotoBlurbGrid(props: PhotoBlurbGridProps) {
  const { anchorRef, config } = props
  const { subGrids, title } = config

  const hasTitle = !!title
  // Heading levels must increase one at a time, so if the grid has no
  // title, then the subgrid headings should be h2 elements.
  const subGridHeadingLevel = hasTitle ? 3 : 2

  return <div id={anchorRef} style={getSectionStyle(config)}>
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
function PhotoBioView({ photoBio }: { photoBio: PhotoBio }) {
  // Default alt text to person's name
  photoBio.image.alt ||= photoBio.name
  return <div className="col-sm-6 col-md-4 gx-5 gy-3 text-center text-sm-start">
    <PearlImage image={photoBio.image} className="img-fluid"/>
    <div className="my-2 fw-bold">
      {photoBio.name} {photoBio.title}
    </div>
    {!!photoBio.blurb && (
      <Markdown className="fst-italic lh-1" style={{ fontSize: '0.9em' }}>
        {photoBio.blurb}
      </Markdown>
    )}
  </div>
}


export default withValidatedSectionConfig(validatePhotoBlurbGridConfig, PhotoBlurbGrid)
