import { PortalEnvContext } from '../../../PortalRouter'
import { getMediaUrl, SiteMediaMetadata } from 'api/api'
import React, { useState } from 'react'
import useReactSingleSelect from 'util/react-select-utils'
import Select from 'react-select'
import { ImageConfig } from '@juniper/ui-core'

/**
 * Helper component that returns an image selector for website image elements
 */
export const ImageSelector = ({ portalEnvContext, imageList, image, onChange }: {
    portalEnvContext: PortalEnvContext,
    imageList: SiteMediaMetadata[], image: ImageConfig, onChange: (image: SiteMediaMetadata) => void
}) => {
  const initial = imageList.find(media => media.cleanFileName === image.cleanFileName)
  const [, setSelectedImage] = useState<SiteMediaMetadata | undefined>(initial)

  const imageOptionLabel = (image: SiteMediaMetadata, portalShortcode: string) => <div>
    {image.cleanFileName} <img style={{ maxHeight: '1.5em' }} alt={image.cleanFileName}
      src={getMediaUrl(portalShortcode, image!.cleanFileName, image!.version)}/>
  </div>

  const {
    onChange: imageOnChange, options, selectedOption, selectInputId
  } = useReactSingleSelect(
    imageList,
    (media: SiteMediaMetadata) => ({ label: imageOptionLabel(media, portalEnvContext.portal.shortcode), value: media }),
    setSelectedImage,
    initial
  )

  return (
    <div>
      <Select
        placeholder={'Select an image'}
        isSearchable={false}
        inputId={selectInputId}
        options={options}
        value={selectedOption}
        onChange={opt => {
          if (opt != undefined) {
            imageOnChange(opt)
            onChange(opt.value)
          }
        }}
      />
    </div>
  )
}
