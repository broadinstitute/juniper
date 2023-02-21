import React from 'react'
import PearlImage, { PearlImageProps } from '../../util/PearlImage'
import ReactMarkdown from 'react-markdown'

type PhotoBlurbGridProps = {
  background?: string, // background CSS style (e.g. `linear-gradient(...)`)
  backgroundColor?: string, // background color for the block
  color?: string,
  title?: string,
  subGrids?: SubGrid[]
}

type SubGrid = {
  title?: string
  photoBios: PhotoBio[]
}

type PhotoBio = {
  image: PearlImageProps,
  name: string,
  title: string,
  blurb: string
}

/**
 * Template for rendering a hero with centered content.
 */
function PhotoBlurbGrid({ config: { background, backgroundColor, color, subGrids, title } }:
                          { config: PhotoBlurbGridProps }) {
  if (!subGrids) {
    subGrids = []
  }
  return <div style={{ background, backgroundColor, color }}>
    {title && <h1 className="fs-1 fw-normal lh-sm text-center">
      {title}
    </h1>}
    {subGrids.map((subGrid, index) => <SubGridView subGrid={subGrid} key={index}/>)}
  </div>
}

/** renders a subgrouping of photos (e.g. "Our researchers") */
function SubGridView({ subGrid }: { subGrid: SubGrid }) {
  return <div className="row justify-content-center">
    <div className="col-md-8">
      {subGrid.title && <h5>{subGrid.title}</h5>}
      <div className="row">
        {subGrid.photoBios.map((bio, index) => <PhotoBioView photoBio={bio} key={index}/>)}
      </div>
    </div>
  </div>
}

/** renders a single bio with pic */
function PhotoBioView({ photoBio }: { photoBio: PhotoBio }) {
  return <div className="col-md-4 gx-3 gy-3 d-flex flex-column">
    <PearlImage image={photoBio.image}/>
    <div>{photoBio.name} <span className="detail">{photoBio.title}</span></div>
    <div className="detail">
      <ReactMarkdown>{photoBio.blurb ? photoBio.blurb : ''}</ReactMarkdown>
    </div>
  </div>
}


export default PhotoBlurbGrid
