/**
 * due to import issues from jest, mock micromark with a trival function that just surrounds the markup with a
 * <p> tag
 */
export function micromark(markup) {
  markup = markup ?? ''
  return `<p>${markup}</p>`
}
