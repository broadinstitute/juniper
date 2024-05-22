import React, { useState } from 'react'
import { LocalSiteContent } from '@juniper/ui-core'
import Modal from 'react-bootstrap/Modal'
import { doApiLoad, useLoadingEffect } from 'api/api-utils'
import Api, { getMediaUrl, SiteMediaMetadata } from 'api/api'
import { filterPriorVersions } from '../media/SiteMediaList'
import useReactSingleSelect from 'util/react-select-utils'
import Select from 'react-select'
import InfoPopup from 'components/forms/InfoPopup'
import LoadingSpinner from '../../util/LoadingSpinner'

/** controls for primary color and nav logo */
export default function BrandingModal({ onDismiss, localContent, updateLocalContent, portalShortcode }: {
    onDismiss: () => void, localContent: LocalSiteContent, updateLocalContent: (newContent: LocalSiteContent) => void,
    portalShortcode: string
}) {
  const [images, setImages] = React.useState<SiteMediaMetadata[]>([])
  const [selectedNavLogo, setSelectedNavLogo] = useState<SiteMediaMetadata>()
  const [selectedFavicon, setSelectedFavicon] = useState<SiteMediaMetadata>()

  const { isLoading: isImageListLoading } = useLoadingEffect(async () => {
    const result = await Api.getPortalMedia(portalShortcode)
    /** Only show the most recent version of a given image in the list */
    const imageList = filterPriorVersions(result).sort((a, b) => a.cleanFileName.localeCompare(b.cleanFileName))
    setImages(imageList)
    setSelectedNavLogo(imageList.find(image => image.cleanFileName === localContent.navLogoCleanFileName))
    setSelectedFavicon(imageList.find(image => image.cleanFileName === 'favicon.ico'))
  }, [portalShortcode])

  const updateFavicon = async (portalShortcode: string, siteMediaId: string) => {
    doApiLoad(async () => {
      await Api.renamePortalMedia(portalShortcode, siteMediaId, { newCleanFileName: 'favicon.ico' })
    })
  }

  const [primaryBrandColor, setPrimaryBrandColor] = useState(localContent.primaryBrandColor)
  const [backgroundColor, setBackgroundColor] = useState(localContent.dashboardBackgroundColor)

  return <Modal show={true} className={'modal-lg'}
    onHide={() => {
      onDismiss()
    }}>
    <Modal.Header closeButton>
      <Modal.Title>Branding</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <LoadingSpinner isLoading={isImageListLoading}>
        <BrandingOption label={'Primary Logo'}
          description={'The logo that appears in the top left corner of the participant navbar'}>
          <ImageSelector images={images} portalShortcode={portalShortcode}
            selectedImage={selectedNavLogo} setSelectedImage={setSelectedNavLogo}/>
        </BrandingOption>
        <BrandingOption label={'Site Favicon'}
          description={'The favicon for the participant website'}>
          <ImageSelector images={images} portalShortcode={portalShortcode}
            selectedImage={selectedFavicon} setSelectedImage={setSelectedFavicon}/>
        </BrandingOption>
        <BrandingOption label={'Primary Brand Color'}
          description={
            <span>Color for links and action buttons for participants. This should be specified as a
            CSS color string, either hex (<code>#33aabb</code>) or RGB (<code>rgb(25,180,100)</code>
            </span>}>
          <ColorPicker color={primaryBrandColor} setColor={setPrimaryBrandColor} />
        </BrandingOption>
        <BrandingOption
          label={'Dashboard Background Color'}
          description={
            <span>Background color for the participant&lsquo;s dashboard. This should be specified as a
            CSS color string, either hex (<code>#33aabb</code>), RGB (<code>rgb(25,180,100)</code>),
            or any other valid CSS color string (e.g., <code>linear-gradient</code>)
            </span>}>
          <ColorPicker color={backgroundColor} setColor={setBackgroundColor} />
        </BrandingOption>
      </LoadingSpinner>
    </Modal.Body>
    <Modal.Footer className="d-flex justify-content-between">
      <div className="col fst-italic text-muted">
              Note: your changes will not be saved until you click &quot;Save&quot; in the main content editor.
      </div>
      <div className="d-flex">
        <button
          className="btn btn-primary me-2"
          disabled={isImageListLoading}
          onClick={async () => {
            updateLocalContent({
              ...localContent,
              primaryBrandColor,
              dashboardBackgroundColor: backgroundColor,
              navLogoCleanFileName: selectedNavLogo?.cleanFileName ?? '',
              navLogoVersion: selectedNavLogo?.version ?? 0
            })
            if (selectedFavicon && selectedFavicon.cleanFileName !== 'favicon.ico') {
              await updateFavicon(portalShortcode, selectedFavicon.id)
            }
            onDismiss()
          }}
        >Confirm
        </button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </div>
    </Modal.Footer>
  </Modal>
}

/**
 *
 */
export function BrandingOption({ label, description, children }: {
    label: string, description?: React.ReactNode, children?: React.ReactNode
}) {
  return (
    <>
      <div className="d-flex align-items-center">
        <div className="col">
          <label>{label}</label>
          { description && <InfoPopup content={description}/> }
        </div>
        <div className="col d-flex align-items-right">
          <div className="w-100">{children}</div>
        </div>
      </div>
      <hr style={{ borderTop: '1px solid #ccc', margin: '0.5rem 0' }} />
    </>
  )
}

const imageOptionLabel = (image: SiteMediaMetadata, portalShortcode: string) => (
  <div className='d-flex'>
    {image.cleanFileName}
    <img alt={image.cleanFileName}
      style={{ height: '1.5rem', width: 'auto' }} className={'ms-2'}
      src={getMediaUrl(portalShortcode, image!.cleanFileName, image!.version)}
    />
  </div>
)

/**
 * A dropdown selector for images
 */
export function ImageSelector({ portalShortcode, images, selectedImage, setSelectedImage }: {
  portalShortcode: string, images: SiteMediaMetadata[], selectedImage?: SiteMediaMetadata,
  setSelectedImage: React.Dispatch<React.SetStateAction<SiteMediaMetadata | undefined>>
}) {
  const {
    selectInputId, selectedOption,
    options, onChange
  } = useReactSingleSelect(
    images,
    image => ({ label: imageOptionLabel(image, portalShortcode), value: image }),
    setSelectedImage, selectedImage)

  return <Select inputId={selectInputId} options={options} value={selectedOption}
    isSearchable={false} onChange={onChange}/>
}


/**
 * A text input for a color string with a preview of the color
 */
export function ColorPicker({ color, setColor }: { color?: string, setColor: (newColor: string) => void }) {
  return <div className="d-flex">
    <input type="text" className="form-control" value={color} onChange={event => setColor(event.target.value)}/>
    <span className="px-4 ms-2" style={{ background: color }} title="color preview"/>
  </div>
}
