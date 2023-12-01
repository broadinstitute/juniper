import React, {useState} from 'react'
import {LocalSiteContent} from "@juniper/ui-core";
import Modal from "react-bootstrap/Modal";
import {useLoadingEffect} from "../../api/api-utils";
import Api, {getImageUrl, SiteImageMetadata} from "../../api/api";
import {filterPriorVersions} from "../images/SiteImageList";
import useReactSingleSelect from "../../util/react-select-utils";
import LoadingSpinner from "../../util/LoadingSpinner";
import Select from "react-select";

const imageOptionLabel = (image: SiteImageMetadata, portalShortcode: string) => <div>
    {image.cleanFileName} <img style={{maxHeight: '1.5em'}}
                               src={getImageUrl(portalShortcode, image!.cleanFileName, image!.version)}/>
</div>

export default function BrandingModal({onDismiss, localContent, updateLocalContent, portalShortcode}: {
    onDismiss: () => void, localContent: LocalSiteContent, updateLocalContent: (newContent: LocalSiteContent) => void,
    portalShortcode: string
}) {
    const [images, setImages] = React.useState<SiteImageMetadata[]>([])
    const [selectedNavLogo, setSelectedNavLogo] = useState<SiteImageMetadata>()

    const {selectInputId, selectedOption, options, onChange} = useReactSingleSelect(
        images,
        image => ({label: imageOptionLabel(image, portalShortcode), value: image}),
        setSelectedNavLogo, selectedNavLogo)

    const { isLoading: isImageListLoading, reload } = useLoadingEffect(async () => {
        const result = await Api.getPortalImages(portalShortcode)
        /** Only show the most recent version of a given image in the list */
        const imageList = filterPriorVersions(result).sort((a, b) => a.cleanFileName.localeCompare(b.cleanFileName))
        setImages(imageList)
        setSelectedNavLogo(imageList.find(image => image.cleanFileName === localContent.navLogoCleanFileName))
    }, [portalShortcode])

    const [color, setColor] = useState(localContent.primaryBrandColor)

   return <Modal show={true}
                       onHide={() => {
                           onDismiss()
                       }}>
        <Modal.Header closeButton>
            <Modal.Title>Branding</Modal.Title>
        </Modal.Header>
        <Modal.Body>
            <form onSubmit={e => e.preventDefault()}>
                <label htmlFor="inputPageTitle">Navbar logo</label>
                <LoadingSpinner isLoading={isImageListLoading}>
                    <Select inputId={selectInputId} options={options} value={selectedOption} onChange={onChange}/>
                </LoadingSpinner>
                <label htmlFor="colorInput" className="mt-3">Primary Brand Color
                    <span className="px-2 ms-3" style={{backgroundColor: color}}/>
                </label>
                <input type="text" className="form-control" id="colorInput"
                       value={color}
                       onChange={event => {
                           setColor(event.target.value)
                       }}/>


            </form>
        </Modal.Body>
        <Modal.Footer>
            <button
                className="btn btn-primary"
                onClick={() => {
                    updateLocalContent({
                        ...localContent,
                        primaryBrandColor: color,
                        navLogoCleanFileName: selectedNavLogo?.cleanFileName ?? '',
                        navLogoVersion: selectedNavLogo?.version ?? 0
                    })
                    onDismiss()
                }}
            >Ok</button>
            <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
        </Modal.Footer>
    </Modal>
}