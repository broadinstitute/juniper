import React, { useState } from 'react'
import Api, { getImageUrl, PortalEnvironment, SiteImageMetadata } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  ColumnDef,
  getCoreRowModel, getPaginationRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout, useRoutableTablePaging } from 'util/tableUtils'
import { instantToDefaultString } from '@juniper/ui-core'
import { LoadedPortalContextT } from '../PortalProvider'
import { useLoadingEffect } from 'api/api-utils'
import TableClientPagination from 'util/TablePagination'
import { Modal } from 'react-bootstrap'
import SiteImageUploadModal, { allowedImageTypes } from './SiteImageUploadModal'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faClipboard, faPlus } from '@fortawesome/free-solid-svg-icons'
import { Button } from 'components/forms/Button'
import { renderPageHeader } from 'util/pageUtils'

/** shows a list of images in a table */
export default function SiteImageList({ portalContext, portalEnv }:
                                            {portalContext: LoadedPortalContextT, portalEnv: PortalEnvironment}) {
  const [images, setImages] = React.useState<SiteImageMetadata[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{
    id: 'cleanFileName', desc: false
  }])
  const { paginationState, preferredNumRowsKey } = useRoutableTablePaging('siteImageList')
  const [previewImage, setPreviewImage] = useState<SiteImageMetadata>()
  const [updatingImage, setUpdatingImage] = useState<SiteImageMetadata>()
  const [showUploadModal, setShowUploadModal] = useState(false)

  const updateImage = (image: SiteImageMetadata) => {
    setUpdatingImage(image)
    setShowUploadModal(true)
  }

  const supportsPreview = (image: SiteImageMetadata) => {
    return allowedImageTypes.some(ext => image.cleanFileName.endsWith(ext))
  }

  const columns: ColumnDef<SiteImageMetadata>[] = [{
    header: 'File name',
    accessorKey: 'cleanFileName'
  }, {
    header: 'version',
    accessorKey: 'version'
  }, {
    header: 'created',
    accessorKey: 'createdAt',
    cell: info => instantToDefaultString(info.getValue() as number)
  }, {
    header: '',
    id: 'thumbnail',
    cell: ({ row: { original: image } }) => supportsPreview(image) ?
      <button onClick={() => setPreviewImage(image)}
        style={{
          minHeight: '44px', minWidth: '90px',
          maxHeight: '44px', maxWidth: '90px'
        }}
        className="border-1 bg-white"
        title="show full-size preview">
        <img src={getImageUrl(portalContext.portal.shortcode, image.cleanFileName, image.version)}
          style={{ maxHeight: '40px', maxWidth: '80px' }}
        />
      </button> :
      <span className="text-muted">no preview</span>
  }, {
    header: '',
    id: 'actions',
    cell: ({ row: { original: image } }) => <button onClick={() => updateImage(image)}
      className="btn btn-secondary">
      update
    </button>
  }, {
    header: '',
    id: 'copy',
    cell: ({ row: { original: image } }) => <button onClick={() => {
      navigator.clipboard.writeText(getImageUrl(portalContext.portal.shortcode,
        image.cleanFileName,
        image.version))
    }}
    className="btn btn-secondary">
      <FontAwesomeIcon icon={faClipboard} className="fa-lg"/> Copy Path
    </button>
  }]

  const table = useReactTable({
    data: images,
    columns,
    state: {
      sorting
    },
    initialState: {
      pagination: paginationState
    },
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getSortedRowModel: getSortedRowModel()
  })


  const onSubmitUpload = () => {
    reload()
    setUpdatingImage(undefined)
    setShowUploadModal(false)
  }


  const { isLoading, reload } = useLoadingEffect(async () => {
    const result = await Api.getPortalImages(portalContext.portal.shortcode)
    /** Only show the most recent version of a given image in the list */
    setImages(filterPriorVersions(result))
  }, [portalContext.portal.shortcode, portalEnv.environmentName])


  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Site Media')}
    <Button onClick={() => setShowUploadModal(true)}
      variant="light" className="border m-1">
      <FontAwesomeIcon icon={faPlus} className="fa-lg"/> Upload
    </Button>
    <LoadingSpinner isLoading={isLoading}>
      {basicTableLayout(table)}
      <TableClientPagination table={table} preferredNumRowsKey={preferredNumRowsKey}/>
    </LoadingSpinner>
    { !!previewImage && <Modal show={true} onHide={() => setPreviewImage(undefined)} size="xl"
      animation={false}>
      <img src={getImageUrl(portalContext.portal.shortcode,
        previewImage.cleanFileName,
        previewImage.version)} alt={`full-size preview of ${previewImage.cleanFileName}`}/>
    </Modal> }
    { showUploadModal && <SiteImageUploadModal portalContext={portalContext}
      onDismiss={() => {
        setShowUploadModal(false)
        setUpdatingImage(undefined)
      }}
      existingImage={updatingImage}
      onSubmit={onSubmitUpload}/> }
  </div>
}

/** Return an array of only the most recent of each image version */
export const filterPriorVersions = (imageList: SiteImageMetadata[]): SiteImageMetadata[] => {
  const latestVersions: Record<string, SiteImageMetadata> = {}
  imageList.forEach(image => {
    if (image.version > (latestVersions[image.cleanFileName]?.version ?? -1)) {
      latestVersions[image.cleanFileName] = image
    }
  })
  return Object.values(latestVersions)
}
