/** escapes double quotes with an extra ", and adds double quotes around any values that contain commas or newlines */
export const escapeCsvValue = (value: string) => {
  console.log('value', value)
  value = value.replaceAll('"', '""')
  if (value && (value.includes(',') || value.includes('\n') || value.includes('"'))) {
    return `"${value}"`
  }
  return value
}

/** takes a blob and saves it to the users computer as a download */
export const saveBlobAsDownload = (blob: Blob, fileName: string) => {
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  a.click()
}
