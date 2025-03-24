import Api from '../api/api'
import {
  saveBlobAsDownload,
  StudyEnvParams
} from '@juniper/ui-core'


export const downloadFile= async (
  studyEnvParams: StudyEnvParams, enrolleeShortcode: string, fileName: string
) => {
  const response = await Api.downloadParticipantFile({
    studyEnvParams, enrolleeShortcode, fileName
  })
  saveBlobAsDownload(await response.blob(), fileName)
}
