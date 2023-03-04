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
function PhotoBlurbGrid({ anchorRef, config: { background, backgroundColor, color, subGrids, title } }:
                          { anchorRef: string, config: PhotoBlurbGridProps }) {
  return <div id={anchorRef} style={{ background, backgroundColor, color }}>
    {title && <h1 className="fs-1 fw-normal lh-sm text-center">
      {title}
    </h1>}
    {(subGrids ?? []).map((subGrid, index) => <SubGridView key={index} subGrid={subGrid}/>)}
  </div>
}

/** renders a subgrouping of photos (e.g. "Our researchers") */
function SubGridView({ subGrid }: { subGrid: SubGrid }) {
  return <div className="row mx-0">
    <div className="col-12 col-sm-10 col-lg-8 mx-auto">
      {subGrid.title && <h3 className="text-center mt-3">{subGrid.title}</h3>}
      <div className="row mx-0">
        {subGrid.photoBios.map((bio, index) => <PhotoBioView key={index} photoBio={bio}/>)}
      </div>
    </div>
  </div>
}

/** renders a single bio with pic */
function PhotoBioView({ photoBio }: { photoBio: PhotoBio }) {
  return <div className="col-sm-6 col-md-4 gx-5 gy-3">
    <PearlImage image={photoBio.image} className="img-fluid"/>
    <div className="my-2">{photoBio.name} <span className="detail">{photoBio.title}</span></div>
    {!!photoBio.blurb && (
      <div className="detail" style={{ lineHeight: 1 }}>
        <ReactMarkdown>{photoBio.blurb}</ReactMarkdown>
      </div>
    )}
  </div>
}


export default PhotoBlurbGrid
