/** escapes double quotes with an extra ", and adds double quotes around any values that contain commas or newlines */
export const escapeCsvValue = (value?: string) => {
  if (!value) {
    return ''
  }
  value = value.replaceAll('"', '""')
  if (value && (value.includes(',') || value.includes('\n') || value.includes('"'))) {
    return `"${value}"`
  }
  return value
}
