import React, { useEffect, useState } from 'react'
import { useDropzone } from 'react-dropzone'
import './DocumentRequestUploader.css'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCaretDown,
  faCaretUp,
  faFile, faFileImage,
  faFilePdf, faSquareMinus, faSquarePlus,
  faUpload
} from '@fortawesome/free-solid-svg-icons'
import { isNil } from 'lodash'
import { StudyEnvParams } from 'src/types/study'
import LoadingSpinner from '@juniper/ui-admin/src/util/LoadingSpinner'
import { saveBlobAsDownload } from '../../util/downloadUtils'
import { ParticipantFile } from '../../types/participantFile'
import { useApiContext } from '../../participant/ApiProvider'
import { useI18n } from '../../participant/I18nProvider'

export const DocumentRequestUploader = (
  {
    studyEnvParams,
    enrolleeShortcode,
    selectedFileNames,
    setSelectedFileNames
  } : {
        studyEnvParams: StudyEnvParams,
        enrolleeShortcode: string,
        selectedFileNames: string[]
        setSelectedFileNames: (fileNames: string[]) => void
    }) => {
  const [files, setFiles] = useState<ParticipantFile[]>([])
  const [selectedFiles, setSelectedFiles] = useState<ParticipantFile[]>([])

  const Api = useApiContext()
  const { i18n } = useI18n()

  const [uploadingFile, setUploadingFile] = useState<string>()

  useEffect(() => {
    Api.listParticipantFiles({ studyEnvParams, enrolleeShortcode }).then(files => {
      setFiles(files)
      setSelectedFiles(files.filter(f => selectedFileNames.includes(f.fileName)))
    })
  }, [studyEnvParams])

  const selectFile = (newFile: ParticipantFile) => {
    if (!selectedFiles.find(f => f.id === newFile.id)) {
      setSelectedFiles(oldSelected => [...oldSelected.filter(old => old.fileName !== newFile.fileName), newFile])
      setSelectedFileNames([...selectedFileNames.filter(old => old !== newFile.fileName), newFile.fileName])
    }
  }

  const unselectFile = (file: ParticipantFile) => {
    setSelectedFiles(oldFiles => oldFiles.filter(f => f.id !== file.id))
    setSelectedFileNames(selectedFileNames.filter(f => f !== file.fileName))
  }


  const uploadAndSelectFile = async (fileData: File) => {
    setUploadingFile(fileData.name)
    const newFile = await Api.uploadParticipantFile({ studyEnvParams, enrolleeShortcode, file: fileData })
    setUploadingFile(undefined)

    setFiles(oldFiles => [newFile, ...oldFiles.filter(f => f.fileName !== newFile.fileName)])
    selectFile(newFile)
  }

  const downloadFile = async (file: ParticipantFile) => {
    const response = await Api.downloadParticipantFile({ studyEnvParams, enrolleeShortcode, fileName: file.fileName })

    saveBlobAsDownload(await response.blob(), file.fileName)
  }

  return <div className='pt-2'>
    <div className='mb-2'>
      <SelectedFileList
        i18n={i18n}
        selectedFiles={selectedFiles}
        removeFile={unselectFile}
        onDownload={downloadFile}/>
    </div>
    <div className='mb-2'>
      {/* show on desktops */}
      <div className='d-none d-lg-block'>
        <DragAndDrop i18n={i18n} uploadNewFile={uploadAndSelectFile}/>
      </div>
      {/* show on mobile */}
      <div className={'d-lg-none my-4'}>
        <FileUpload uploadNewFile={uploadAndSelectFile}/>
      </div>

    </div>
    <DocumentLibrary
      i18n={i18n}
      uploadingFile={uploadingFile}
      files={files}
      selectFile={selectFile}
      unselectFile={unselectFile}
      selectedFiles={selectedFiles}
      downloadFile={downloadFile}
    />
  </div>
}

const SelectedFileList = (
  {
    i18n,
    selectedFiles,
    removeFile,
    onDownload
  }: {
        i18n: (key: string) => string,
        selectedFiles: ParticipantFile[],
        removeFile: (file: ParticipantFile) => void,
        onDownload?: (file: ParticipantFile) => void
    }
) => {
  return <div className='card'>
    <p className='card-header p-3'>{i18n('documentUploaderSelectedDocuments')} ({selectedFiles.length})</p>
    <div className='card-body'>
      {selectedFiles.length > 0 && <div className="fst-italic text-muted text-wrap pb-2">
        {i18n('documentUploaderIncludedInResponse')}:
      </div>}
      {selectedFiles.length === 0 && <div className='fst-italic text-wrap text-muted my-2'>
        {i18n('documentUploaderNoDocumentsSelected')}
      </div>}
      {selectedFiles.map(selectedFile => {
        return <FileRow
          fileType={selectedFile.fileType}
          fileName={selectedFile.fileName}
          isUploading={false}
          isSelected={true}
          onUnselect={() => removeFile(selectedFile)}
          onSelect={() => removeFile(selectedFile)}
          onDownload={onDownload ? () => onDownload(selectedFile) : undefined}
          key={selectedFile.id}
        />
      })}
    </div>
  </div>
}

const DocumentLibrary = (
  {
    i18n,
    uploadingFile,
    files,
    selectedFiles,
    selectFile,
    unselectFile,
    downloadFile
  }: {
        i18n: (key: string) => string,
        uploadingFile?: string,
        files: ParticipantFile[],
        selectedFiles: ParticipantFile[],
        selectFile: (file: ParticipantFile) => void,
        unselectFile: (file: ParticipantFile) => void,
        downloadFile: (file: ParticipantFile) => void
    }
) => {
  const isSelected = (file: ParticipantFile) => {
    return !!selectedFiles.find(f => f.id === file.id)
  }

  const [expanded, setExpanded] = React.useState(true)

  const unselectedFiles = files.filter(f => !isSelected(f))

  return <div className='card'>
    <p className='card-header p-3'>
      {i18n('documentUploaderAvailableDocuments')} <span>({unselectedFiles.length})</span>
      <button className='btn btn-link p-0 ps-2' onClick={() => setExpanded(!expanded)}>
        <FontAwesomeIcon icon={expanded ? faCaretUp : faCaretDown}/>
      </button>
    </p>
    {expanded && <div className='card-body'>
      {uploadingFile && <FileRow
        fileType={''}
        fileName={uploadingFile}
        isUploading={true}
        isSelected={false}
      />}
      <>
        {unselectedFiles.length > 0 && <div className="fst-italic text-muted text-wrap pb-2">
          {i18n('documentUploaderNotIncludedInResponse')}:
        </div>}
        {unselectedFiles.length === 0 &&
            <div className='fst-italic text-muted'>{i18n('documentUploaderNoDocuments')}</div>}
        {unselectedFiles.map(file => {
          return <FileRow
            fileType={file.fileType}
            fileName={file.fileName}
            isUploading={false}
            isSelected={isSelected(file)}
            onUnselect={() => unselectFile(file)}
            onSelect={() => selectFile(file)}
            onDownload={() => downloadFile(file)}
            key={file.id}
          />
        })}</>
    </div>}

  </div>
}

const FileRow = ({
  fileType,
  fileName,
  isUploading,
  isSelected,
  onSelect,
  onUnselect,
  onDownload
}: {
    fileType: string,
    fileName: string,
    isUploading: boolean,
    isSelected: boolean,
    onSelect?: () => void,
    onUnselect?: () => void,
    onDownload?: () => void
}) => {
  return <div
    key={fileName}
    className={'border border-1 rounded-1 bg-light-subtle p-2 d-flex align-items-center justify-content-between mb-2'}>
    <div className='d-flex align-items-center justify-content-between'>
      <FileIcon mimeType={fileType}/>
      <button onClick={onDownload} className='btn btn-link text-wrap text-start'>{fileName}</button>
    </div>
    {isUploading && <LoadingSpinner/>}

    <div className='d-flex justify-content-end'>
      {isSelected
        ? <button onClick={onUnselect}
          className='float-end btn btn-outline-primary text-decoration-none border-0'>
          <FontAwesomeIcon icon={faSquareMinus}/>
        </button>
        : <button onClick={onSelect}
          className='float-end btn btn-outline-primary text-decoration-none border-0'>
          <FontAwesomeIcon icon={faSquarePlus}/>
        </button>}
    </div>
  </div>
}

const getIcon = (mimeType: string) => {
  if (mimeType.startsWith('image')) {
    return faFileImage
  } else if (mimeType === 'application/pdf') {
    return faFilePdf
  } else {
    return faFile
  }
}

const FileIcon = ({ mimeType }: { mimeType: string }) => {
  const icon = getIcon(mimeType)
  return <FontAwesomeIcon icon={icon}/>
}

const DragAndDrop = ({ i18n, uploadNewFile }: {
    i18n: (key: string) => string,
    uploadNewFile: (file: File) => void }) => {
  const onDrop = React.useCallback((acceptedFiles: File[]) => {
    acceptedFiles.forEach((file: File) => {
      uploadNewFile(file)
    })
  }, [])

  const { getRootProps, getInputProps } = useDropzone({ onDrop })

  return <div {...getRootProps()}>
    <input {...getInputProps()} />
    <div className="file-dropper w-100 my-4">
      <div className={'d-flex w-100 h-100 align-items-center justify-content-center flex-column'}>
        <span className='py-5'>
          <FontAwesomeIcon icon={faUpload} className={'text-primary me-1'}/>
          <a type="button" className='text-decoration-underline'>{i18n('documentUploaderClickToUpload')}</a>
        </span>
      </div>
    </div>
  </div>
}

const FileUpload = ({ uploadNewFile }: { uploadNewFile: (file: File) => void }) => {
  return <div>
    <input type="file" data-testid="fileInput" className={'form-control'} onChange={e => {
      const file = e.target.files?.item(0) || null
      if (!isNil(file)) {
        uploadNewFile(file)
      }
    }}/>
  </div>
}
